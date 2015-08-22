package org.witness.informacam.models.forms;

import info.guardianproject.odkparser.FormWrapper;
import info.guardianproject.odkparser.utils.QD;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Vector;

import org.witness.informacam.json.JSONObject;
import org.witness.informacam.models.Model;
import org.witness.informacam.utils.Constants;
import org.witness.informacam.utils.Constants.App.Informa;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.MediaHasher;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

@SuppressWarnings("serial")
public class IForm extends Model implements Serializable {
	public String title = null;
	public String namespace = null;
	public String path = null;
	public String answerPath = null;
	public JSONObject answerData = null;
	public String id = null; 

	FormWrapper fw = null;
	Activity a = null;
	
	public static IForm Activate(IForm model, Activity activity, byte[] oldAnswers) throws InstantiationException, IllegalAccessException {
		return new IForm(model, activity, oldAnswers);
	}
	
	public static IForm Activate(IForm model, Activity activity) throws InstantiationException, IllegalAccessException {
		return new IForm(model, activity);
	}
	
	public IForm() {
		super();
	}
	
	public IForm(IForm model, Activity a) throws InstantiationException, IllegalAccessException {
		this(model, a, null);
	}
	
	public IForm(IForm model, Activity a, byte[] oldAnswers) throws InstantiationException, IllegalAccessException {
		super();
		
		this.inflate(model.asJson());
//		Logger.d(LOG, "THIS FORM: " + asJson().toString());
		
		this.a = a;
		String[] answers = null;
		
		try {
			fw = new FormWrapper(new info.guardianproject.iocipher.FileInputStream(path), oldAnswers);
			answers = new String[fw.questions.size()];
			int answer = 0;
			for(QD qd : fw.questions) {
				answers[answer] = qd.initialValue != null ? qd.initialValue : "";
			//	Log.d(LOG, "this has initial value? " + String.valueOf(qd.initialValue));
				answer++;
			}
		} catch (FileNotFoundException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
		
	}
	
	public void answerAll() {
		for(QD qd : fw.questions) {
			qd.answer();
		}
	}
	
	public void clear() {
		for(QD qd : fw.questions) {
			qd.clear();
		}
	}

	public IForm associate(View answerHolder, String questionId) {
		QD questionDef = fw.questions.get(fw.questions.indexOf(getQuestionDefByTitleId(questionId)));
		if(questionDef != null) {
			questionDef.pin(answerHolder);
		}

		return this;
	}

	public List<View> buildUI(int[] inputLayout, int[] selectOneLayout, int[] selectMultiLayout, int[] audioCaptureLayout) {
		LayoutInflater li = LayoutInflater.from(a);
		
		List<View> views = new Vector<View>();

		int v = 0;
		for(QD questionDef : fw.questions) {
			View view = null;
			switch(questionDef.getQuestionDef().getControlType()) {
			case org.javarosa.core.model.Constants.CONTROL_INPUT:
				view = li.inflate(inputLayout[0], null);
				view.setTag(QD.map(inputLayout));
				break;
			case org.javarosa.core.model.Constants.CONTROL_SELECT_ONE:
				view = li.inflate(selectOneLayout[0], null);
				view.setTag(QD.map(selectOneLayout));
				break;
			case org.javarosa.core.model.Constants.CONTROL_SELECT_MULTI:
				view = li.inflate(selectMultiLayout[0], null);
				view.setTag(QD.map(selectMultiLayout));
				break;
			case org.javarosa.core.model.Constants.CONTROL_AUDIO_CAPTURE:
				view = li.inflate(audioCaptureLayout[0], null);
				view.setTag(QD.map(audioCaptureLayout));
				break;
			}

			try
			{
				view = questionDef.buildUI(a, view);
				view.setId(v);
				views.add(view);
				v++;
			}
			catch (NumberFormatException nfe)
			{
				Log.e(Informa.LOG, "Error parsing ODF forms",nfe);
			}
		}

		return views;
	}

	public IForm answer(String questionId) {
		QD questionDef = fw.questions.get(fw.questions.indexOf(getQuestionDefByTitleId(questionId)));
		if(questionDef != null) {
			questionDef.answer();
		}
		
		return this;
	}
	
	public static String appendId() {
		try {
			byte[] idBytes = new String(System.currentTimeMillis() + new String(Constants.App.Crypto.FORM_SALT)).getBytes();			
			return MediaHasher.hash(idBytes, "MD5");
		} catch (NoSuchAlgorithmException e) {
			Logger.e(LOG, e);
		} catch (IOException e) {
			Logger.e(LOG, e);
		}
		
		return null;
	}

	public OutputStream save(OutputStream os) {
		for(QD questionDef : fw.questions) {
			questionDef.commit(fw);
		}
		
		return fw.processFormAsXML(os);
	}
	
	public JSONObject save() {
		for(QD questionDef : fw.questions) {
			questionDef.commit(fw);
		}
		
		return fw.processFormAsJSON();
	}

	public QD getQuestionDefByTitleId(String questionId) {
//		Logger.d(LOG, "looking for question id " + questionId + " among " + fw.questions.size() + " forms");
		for(QD qd : fw.questions) {
	//		Log.d(LOG, "QUESTION DEF ID: " + qd.id);
			if(qd.id.equals(questionId)) {
				return qd;
			}
		}

		return null;
	}
}