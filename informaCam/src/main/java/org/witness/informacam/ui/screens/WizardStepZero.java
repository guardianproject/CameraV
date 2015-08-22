package org.witness.informacam.ui.screens;

import java.util.Locale;

import org.witness.informacam.InformaCam;
import org.witness.informacam.R;
import org.witness.informacam.models.utils.ILanguageMap;
import org.witness.informacam.ui.WizardActivity;
import org.witness.informacam.utils.Constants.Codes;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class WizardStepZero extends Fragment implements OnClickListener {
	View rootView;
	Activity a;
	
	RadioGroup languageChoices;
	ILanguageMap languageMap;
	Button commit;
	
	int choice = 0;
	String langKey;
	
	Handler handler = new Handler();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);
		rootView = li.inflate(R.layout.fragment_wizard_step_zero, null);
		
		languageChoices = (RadioGroup) rootView.findViewById(R.id.wizard_language_choices);
		languageMap = (ILanguageMap) getArguments().getSerializable(Codes.Extras.SET_LOCALES);
		
		for(String l : languageMap.getLabels()) {
			RadioButton rb = new RadioButton(a);
			rb.setText(l);
			rb.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					for(int l=0; l<languageChoices.getChildCount(); l++) {
						if(v.equals(languageChoices.getChildAt(l))) {
							choice = l;
							break;
						}
					}
					
				}
				
			});

			languageChoices.addView(rb);
		}
		
		langKey = getArguments().getString(Codes.Extras.LOCALE_PREF_KEY);
		
		commit = (Button) rootView.findViewById(R.id.wizard_commit);
		commit.setOnClickListener(this);
		
		return rootView;
	}
	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}
	
	@Override
	public void onClick(View v) {
		if(v == commit) {
			SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(a).edit();
			sp.putString(langKey, String.valueOf(choice)).commit();
			
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// set config
					Configuration configuration = new Configuration();
					configuration.locale = new Locale(languageMap.getCode(choice));
					
					a.getBaseContext().getResources().updateConfiguration(configuration, a.getBaseContext().getResources().getDisplayMetrics());
					
					((WizardActivity) a).onConfigurationChanged(configuration);
				}
			}, 250);
		}
	}
}
