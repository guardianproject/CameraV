package info.guardianproject.odkparser;

import info.guardianproject.odkparser.utils.QD;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;
import org.xml.sax.SAXException;

import android.os.Environment;
import android.util.Log;


public class FormWrapper implements Constants {
	public FormDef form_def;
	public FormIndex form_index;
	public FormEntryController controller;
	public FormEntryModel fem; 
	public FormEntryPrompt fep;

	public ArrayList<QD> questions;
	public Map<String, String> answers = null;

	public String title;
	public int num_questions = 0;

	static PrototypeFactory pf;
	static {
		PrototypeManager.registerPrototype("org.javarosa.model.xform.XPathReference");
		pf = ExtUtil.defaultPrototypes();
	}

	private static final String LOG = Logger.FORM;

	public interface ODKFormListener {
		public boolean saveForm();
	}

	public FormWrapper(InputStream xml, boolean touch) {
		form_def = loadDefinition(xml);

		if(!touch)
			init(null);
	}

	public FormWrapper(FormDef form_def) {
		this.form_def = form_def;
		init(null);
	}

	public FormWrapper(InputStream xml) {
		form_def = loadDefinition(xml);
		init(null);
	}

	public FormWrapper(InputStream xml, byte[] oldAnswers) {		
		form_def = loadDefinition(xml);
		init(oldAnswers);
	}

	public FormWrapper(FormDef form_def, byte[] oldAnswers) {
		this.form_def = form_def;
		init(oldAnswers);
	}

	@SuppressWarnings("unused")
	private QuestionDef getFirstQuestionDef() {
		controller.jumpToIndex(FormIndex.createBeginningOfFormIndex());
		do {
			FormEntryCaption fec = fem.getCaptionPrompt();
			if(fec.getFormElement() instanceof QuestionDef)
				return (QuestionDef) fec.getFormElement();
		} while(controller.stepToNextEvent() != FormEntryController.EVENT_END_OF_FORM);

		return null;
	}

	@SuppressWarnings("unused")
	private QuestionDef getCurrentQuestionDef() {
		FormEntryCaption fec = fem.getCaptionPrompt();
		if(fec.getFormElement() instanceof QuestionDef)
			return (QuestionDef) fec.getFormElement();

		return null;
	}

	public void inflatePreviousAnswers(byte[] bytes) {		
		TreeElement savedRoot = XFormParser.restoreDataModel(bytes, null).getRoot();

		for(int t=0; t<savedRoot.getNumChildren(); t++) {
			TreeElement childElement = savedRoot.getChildAt(t);

			if(answers == null) {
				answers = new HashMap<String, String>();
			}

			try {
				//Log.d(LOG, "HERE IS " + childElement.getValue().getValue());
				answers.put(childElement.getName(), String.valueOf(childElement.getValue().getValue()));
			} catch(NullPointerException e) {
				// there is no value here
				//Log.e(LOG, "no value for " + childElement.getName());

				continue;
			}
		}

		form_def.preloadInstance(savedRoot);

	}

	public static JSONObject parseXMLAnswersAsJSON(byte[] bytes) {
		TreeElement savedRoot = XFormParser.restoreDataModel(bytes, null).getRoot();
		JSONObject answers = new JSONObject(); 
		for(int t=0; t<savedRoot.getNumChildren(); t++) {
			TreeElement childElement = savedRoot.getChildAt(t);

			try {
				answers.put(childElement.getName(), childElement.getValue().getDisplayText());
			} catch (JSONException e) {
				e.printStackTrace();
				continue;
			} catch (NullPointerException e) {
				e.printStackTrace();
				continue;
			}
		}

		return answers;
	}

	private List<QD> init(byte[] oldAnswers) {
		EvaluationContext ec = new EvaluationContext();		
		form_def.setEvaluationContext(ec);

		fem = new FormEntryModel(form_def);
		controller = new FormEntryController(fem);

		if(oldAnswers != null && oldAnswers.length > 0)
			inflatePreviousAnswers(oldAnswers);
		else
			form_def.initialize(true);

		title = controller.getModel().getForm().getTitle();
		form_index = controller.getModel().getFormIndex();

		controller.jumpToIndex(FormIndex.createBeginningOfFormIndex());
		Localizer l = form_def.getLocalizer();
		l.setDefaultLocale(l.getAvailableLocales()[0]);
		l.setLocale(l.getAvailableLocales()[0]);

		do {
			FormEntryCaption fec = fem.getCaptionPrompt();
			if(fec.getFormElement() instanceof QuestionDef) {
				if(questions == null)
					questions = new ArrayList<QD>();

				QuestionDef qd = (QuestionDef) fec.getFormElement();
				//Log.d(LOG, "this question def textId: " + qd.getTextID());
				QD questionDef = null;

				if(answers != null && answers.containsKey(qd.getTextID()))
					questionDef = new QD(qd, answers.get(qd.getTextID()));
				else
					questionDef = new QD(qd);

				FormEntryPrompt fep = fem.getQuestionPrompt();
				questionDef.questionText = fep.getQuestionText();

				if(fep.getHelpText() != null)
					questionDef.helperText = fep.getHelpText();

				if(fep.getControlType() == org.javarosa.core.model.Constants.CONTROL_SELECT_MULTI || fep.getControlType() == org.javarosa.core.model.Constants.CONTROL_SELECT_ONE) {
					questionDef.selectChoiceText = new ArrayList<String>();
					for(SelectChoice sc : fep.getSelectChoices()) {
						questionDef.selectChoiceText.add(fep.getSelectChoiceText(sc));
					}
				}


				questions.add(questionDef);
			}

		} while(controller.stepToNextEvent() != FormEntryController.EVENT_END_OF_FORM);

		num_questions = questions.size();
		return questions;
	}	

	public JSONObject processFormAsJSON() {
		JSONObject informaObject = new JSONObject();
		form_def.postProcessInstance();

		try {
			XFormSerializingVisitor serializer = new XFormSerializingVisitor();
			ByteArrayPayload payload = new ByteArrayPayload(serializer.serializeInstance(form_def.getInstance()), form_def.getName(), form_def.getID());

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(payload.getPayloadStream());
			doc.getDocumentElement().normalize();

			NodeList answers = doc.getDocumentElement().getChildNodes();
			//Log.d(LOG, "there are " + answers.getLength() + " child nodes");
			for(int n=0; n<answers.getLength(); n++) {
				Node node = answers.item(n);

				//Log.d(LOG, "node: " + node.getNodeName());
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					try {
						informaObject.put(node.getNodeName(), ((Element) node).getElementsByTagName(node.getNodeName()).item(0).getChildNodes().item(0).getNodeValue());
					} catch(NullPointerException e) {
						//Log.e(LOG, "Could not get value for " + node.getNodeName() + "\n" + e.toString());
						e.printStackTrace();
					}

				}
			}


			return informaObject;
		} catch(IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (SAXException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (DOMException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (JSONException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}


		return null;
	}

	public OutputStream processFormAsXML(OutputStream os) {
		Log.d(LOG, "SAVING AS XML NOW!");
		try {
			XFormSerializingVisitor serializer = new XFormSerializingVisitor();
			ByteArrayPayload payload = new ByteArrayPayload(serializer.serializeInstance(form_def.getInstance()), form_def.getName(), form_def.getID());

			InputStream is = payload.getPayloadStream();
			byte[] data = new byte[(int) payload.getLength()];

			int read = is.read(data, 0, (int) payload.getLength());
			if(read > 0) {
				os.write(data);
				os.flush();
				os.close();

				return os;
			}


		} catch (FileNotFoundException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}

		return null;
	}

	public boolean saveTest() {
		try {

			File testDir = new File(Environment.getExternalStorageDirectory(),"odktest");
			if(!testDir.exists())
				testDir.mkdir();


			File testFile = new File(testDir, "text.xml");

			XFormSerializingVisitor serializer = new XFormSerializingVisitor();
			ByteArrayPayload payload = new ByteArrayPayload(serializer.serializeInstance(form_def.getInstance()), form_def.getName(), form_def.getID());

			InputStream is = payload.getPayloadStream();
			byte[] data = new byte[(int) payload.getLength()];

			int read = is.read(data, 0, (int) payload.getLength());
			if(read > 0) {
				OutputStream os = new FileOutputStream(testFile);
				os.write(data);
				os.flush();
				os.close();

				return true;
			}


		} catch (FileNotFoundException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}


		return false;
	}

	public boolean answerQuestion(QuestionDef qd, IAnswerData answer) {

		int event = controller.jumpToIndex(FormIndex.createBeginningOfFormIndex());
		do {
			FormEntryCaption fec = fem.getCaptionPrompt();
			if(fec.getFormElement() instanceof QuestionDef && ((QuestionDef) fec.getFormElement()).equals(qd)) {
				try {
					if(answer.getValue() != null && !answer.getValue().equals("null")) {
						//Log.d(LOG, "fyi answer data: " + answer.hashCode() + " (" + answer.getValue() + ")");
						controller.answerQuestion(answer);

						return controller.saveAnswer(answer);
					}
				} catch(NullPointerException e) {
					Log.d(LOG, e.toString());
					e.printStackTrace();
				}

			}

		} while((event = controller.stepToNextEvent()) != FormEntryController.EVENT_END_OF_FORM);

		return false;
	}

	public static FormDef loadDefinition(InputStream xml) {
		return XFormUtils.getFormFromInputStream(xml);
	}

	public static byte[] getBytesFromFile(File file) throws IOException {
		byte[] bytes = new byte[(int) file.length()];

		FileInputStream fis = new FileInputStream(file);
		fis.read(bytes, 0, bytes.length);
		fis.close();

		return bytes;
	}
}
