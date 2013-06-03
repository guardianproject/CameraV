package org.witness.iwitness.app;

import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.Preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
ListPreference language;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		language = (ListPreference) findPreference(Preferences.Keys.LANGUAGE);
		updateSummaryWithChoice(language, language.getValue(), getResources().getStringArray(R.array.languages_));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(Preferences.Keys.LANGUAGE)) {
			updateSummaryWithChoice(language, sharedPreferences.getString(key, "0"), getResources().getStringArray(R.array.languages_));
		}
		
	}
	
	private void updateSummaryWithChoice(Preference pref, String choiceValue, String[] choices) {
		pref.setSummary(choices[Integer.parseInt(choiceValue)]);
	}

}
