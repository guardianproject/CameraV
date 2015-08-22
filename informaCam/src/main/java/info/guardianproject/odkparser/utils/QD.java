package info.guardianproject.odkparser.utils;

import info.guardianproject.odkparser.FormWrapper;
import info.guardianproject.odkparser.widgets.ODKSeekBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.data.helper.Selection;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class QD extends Model {
	QuestionDef questionDef;
	IAnswerData answer;
	View answerHolder;
	byte[] attachment = null;

	public String id = null;
	public String initialValue = null;
	public String questionText = null;
	public String helperText = null;
	public boolean hasInitialValue = false;
	public ArrayList<String> selectChoiceText = null;
	
	public QD() {
		super();
	}

	public static Map<String, Integer> map(int[] viewParams) {
		Map<String, Integer> viewMap = new HashMap<String, Integer>();
		viewMap.put("rootViewId", viewParams[0]);
		viewMap.put("questionText", viewParams[1]);
		viewMap.put("helperText", viewParams[2]);
		viewMap.put("answerHolder", viewParams[3]);
		return viewMap;
	}

	public void clear() {
		switch(questionDef.getControlType()) {
		case org.javarosa.core.model.Constants.CONTROL_INPUT:
			((EditText) answerHolder).setText("");
			break;
		case org.javarosa.core.model.Constants.CONTROL_SELECT_ONE:
			RadioGroup rg = ((RadioGroup) answerHolder);
			for(int r = 0; r < selectChoiceText.size(); r++) {
				((RadioButton) rg.getChildAt(r)).setChecked(false);
			}
			break;
		case org.javarosa.core.model.Constants.CONTROL_SELECT_MULTI:
			LinearLayout ll = ((LinearLayout) answerHolder);

			for(int c = 0; c < selectChoiceText.size(); c++) {
				((CheckBox) ll.getChildAt(c)).setChecked(false);				
			}

			answerHolder = ll;
			break;
		case org.javarosa.core.model.Constants.CONTROL_AUDIO_CAPTURE:
			((ODKSeekBar) answerHolder).rawAudioData = null;
			break;

		}
	}

	public void setQuestionDef(QuestionDef questionDef) {
		this.questionDef = questionDef;
		this.id = this.questionDef.getTextID();
	}

	public QD(QuestionDef questionDef) {
		this(questionDef, null);
	}

	public QD(QuestionDef questionDef, String initialValue) {
		setQuestionDef(questionDef);
		if(initialValue != null) {
			this.initialValue = initialValue;
			this.hasInitialValue = true;
		}
	}

	public QD(View answerHolder) {
		this(null, answerHolder);
	}

	public QD(String initialValue, View answerHolder) {		
		this.initialValue = initialValue == "null" ? null : initialValue;
		this.hasInitialValue = this.initialValue == null ? false : true;
		this.answerHolder = answerHolder;
	}

	public QuestionDef getQuestionDef() {
		return questionDef;
	}

	public View getAnswerHolder() {
		return answerHolder;
	}

	@SuppressWarnings("unchecked")
	public View buildUI(Activity a, View view) {
		Map<String, Integer> viewMap = (Map<String, Integer>) view.getTag();

		TextView tv_questionText = (TextView) view.findViewById(viewMap.get("questionText"));
		tv_questionText.setText(questionText);

		TextView tv_helperText = (TextView) view.findViewById(viewMap.get("helperText"));
		if(helperText != null) {
			tv_helperText.setText(helperText);
		} else {
			tv_helperText.setVisibility(View.GONE);
		}


		switch(questionDef.getControlType()) {
		case org.javarosa.core.model.Constants.CONTROL_INPUT:
			answerHolder = (EditText) view.findViewById(viewMap.get("answerHolder"));
			break;
		case org.javarosa.core.model.Constants.CONTROL_SELECT_ONE:
			RadioGroup rg = (RadioGroup) view.findViewById(viewMap.get("answerHolder"));
			for(int r = 0; r < selectChoiceText.size(); r++) {
				RadioButton rb = new RadioButton(a);
				rb.setText(selectChoiceText.get(r));
				rg.addView(rb);				
			}
			answerHolder = rg;

			break;
		case org.javarosa.core.model.Constants.CONTROL_SELECT_MULTI:
			LinearLayout ll = (LinearLayout) view.findViewById(viewMap.get("answerHolder"));


			for(int c = 0; c < selectChoiceText.size(); c++) {
				CheckBox cb = new CheckBox(a);
				cb.setText(selectChoiceText.get(c));
				ll.addView(cb);				
			}

			answerHolder = ll;
			break;
		case org.javarosa.core.model.Constants.CONTROL_AUDIO_CAPTURE:
			answerHolder = (LinearLayout) view.findViewById(viewMap.get("answerHolder"));
			break;
		}

		pin(answerHolder);
		return view;
	}

	public void answer() {
		switch(questionDef.getControlType()) {
		case org.javarosa.core.model.Constants.CONTROL_INPUT:
			if(((EditText) answerHolder).getText().length() > 0) {
				((StringData) answer).setValue(((EditText) answerHolder).getText().toString());
			}
			
			break;
		case org.javarosa.core.model.Constants.CONTROL_SELECT_ONE:
			for(int o=0; o < ((ViewGroup) answerHolder).getChildCount(); o++) {
				if(
						((ViewGroup) answerHolder).getChildAt(o) instanceof RadioButton &&
						((RadioButton) ((ViewGroup) answerHolder).getChildAt(o)).isChecked()
						) {
					((SelectOneData) answer).setValue(questionDef.getChoices().get(o).selection());
					break;
				}
			}
			
			break;
		case org.javarosa.core.model.Constants.CONTROL_SELECT_MULTI:
			List<Selection> choices = new Vector<Selection>();
			int choiceIndex = 0;
			for(int m=0; m< ((ViewGroup) answerHolder).getChildCount(); m++) {
				if(
						((ViewGroup) answerHolder).getChildAt(m) instanceof CheckBox &&
						((CheckBox) ((ViewGroup) answerHolder).getChildAt(m)).isChecked()
						) {
					choices.add(questionDef.getChoices().get(choiceIndex).selection());
				}

				choiceIndex++;
			}

			((SelectMultiData) answer).setValue(choices);

			break;
		case org.javarosa.core.model.Constants.CONTROL_AUDIO_CAPTURE:
			//Log.d(LOG, "JUST CHECKING ON AUDIO: " + String.valueOf(((ODKSeekBar) answerHolder).rawAudioData));
			if(((ODKSeekBar) answerHolder).rawAudioData != null) {
				((UncastData) answer).setValue(new String(((ODKSeekBar) answerHolder).rawAudioData));
			}

			break;
		}
	}

	public void commit(FormWrapper fw) {
		if(fw.answerQuestion(questionDef, answer)) {
			initialValue = String.valueOf(answer.getValue());
		}

	}

	public void pin(View answerHolder) {
		this.answerHolder = answerHolder;

		Log.d(LOG, "pinning to " + answerHolder.getClass().getName());
		switch(questionDef.getControlType()) {
		case org.javarosa.core.model.Constants.CONTROL_INPUT:
			if(initialValue != null && initialValue != "null") {
				answer = new StringData(String.valueOf(initialValue));
				((EditText) this.answerHolder).setText(initialValue);
			} else {
				answer = new StringData("");
				((EditText) this.answerHolder).setHint(questionDef.getHelpText());
			}

			break;
		case org.javarosa.core.model.Constants.CONTROL_SELECT_ONE:
			answer = new SelectOneData();

			if(initialValue != null && initialValue != "null" && (!initialValue.equals("0"))) {
				try {
					Selection selection = questionDef.getChoices().get(Integer.parseInt(initialValue) - 1).selection();
					((SelectOneData) answer).setValue(selection);

					RadioButton rb = (RadioButton) ((ViewGroup) this.answerHolder).getChildAt(Integer.parseInt(initialValue) - 1);
					rb.setChecked(true);
				} catch(ArrayIndexOutOfBoundsException e) {
					Log.e(LOG, e.toString());
					e.printStackTrace();
				}
			}

			break;
		case org.javarosa.core.model.Constants.CONTROL_SELECT_MULTI:
			answer = new SelectMultiData();

			if(initialValue != null && initialValue != "null") {
				Vector<Selection> selections = new Vector<Selection>();
				String[] selectionsString = String.valueOf(initialValue).split(" ");
				for(String s : selectionsString) {
					
					if (!s.equals("0"))
					{
						Selection selection = questionDef.getChoices().get(Integer.parseInt(s) - 1).selection();
						selections.add(selection);
	
						((CheckBox) ((ViewGroup) this.answerHolder).getChildAt(Integer.parseInt(s) - 1)).setChecked(true);
					}
				}

				((SelectMultiData) answer).setValue(selections);
			}
			break;
		case org.javarosa.core.model.Constants.CONTROL_AUDIO_CAPTURE:
			answer = new UncastData("");

			if(initialValue != null && initialValue != "null") {
				answer = new UncastData(initialValue);
				((UncastData) answer).setValue(initialValue);
				((ODKSeekBar) answerHolder).setRawAudioData(initialValue.getBytes());
			}
			
			//Log.d(LOG, "JUST CHECKING ON AUDIO: " + String.valueOf(((ODKSeekBar) answerHolder).rawAudioData));
			//Log.d(LOG, "JUST CHECKING ON AUDIO: " + initialValue);

			break;
		}
	}


}
