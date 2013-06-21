package org.witness.iwitness.app.screens.wizard;

import org.witness.informacam.utils.Constants.WizardListener;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.Preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class OriginalImagePreference extends Fragment implements WizardListener {
	View rootView;
	Activity a;
	
	RadioGroup originalImagePreferenceHolder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);
		
		rootView = li.inflate(R.layout.fragment_wizard_step_four, null);
		originalImagePreferenceHolder = (RadioGroup) rootView.findViewById(R.id.wizard_original_image_handling);
		
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
	public void onSubFragmentCompleted() {
		SharedPreferences sp = a.getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
		SharedPreferences.Editor ed = sp.edit();
		
		for(int i=0; i<originalImagePreferenceHolder.getChildCount(); i++) {
			RadioButton rb = (RadioButton) originalImagePreferenceHolder.getChildAt(i);
			if(rb.isSelected()) {
				ed.putInt(Preferences.Keys.ORIGINAL_IMAGE_HANDLING, i).commit();
			}
		}
		
	}

	@Override
	public FragmentManager returnFragmentManager() {
		return null;
	}

	@Override
	public void wizardCompleted() {}

	@Override
	public void onSubFragmentInitialized() {}

}
