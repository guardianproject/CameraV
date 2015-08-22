package org.witness.informacam.app;

import java.util.List;

import org.witness.informacam.app.utils.Constants.Preferences;
import org.witness.informacam.utils.Constants.Models;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	ListPreference lockScreenMode, language, panicAction;//originalImage, 
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle(R.string.preferences);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setLogo(this.getResources().getDrawable(R.drawable.ic_action_up));
		actionBar.setDisplayUseLogoEnabled(true);
		
		addPreferencesFromResource(R.xml.preferences);
		
//		lockScreenMode = (ListPreference) findPreference(Preferences.Keys.LOCK_SCREEN_MODE);
//		updateSummaryWithChoice(lockScreenMode, lockScreenMode.getValue(), getResources().getStringArray(R.array.lockScreenOptions_));

		language = (ListPreference) findPreference(Preferences.Keys.LANGUAGE);
		updateSummaryWithChoice(language, language.getValue(), getResources().getStringArray(R.array.languages_));
		
		//originalImage = (ListPreference) findPreference(Models.IUser.ASSET_ENCRYPTION);
		//updateSummaryWithChoice(originalImage, originalImage.getValue(), getResources().getStringArray(R.array.originalImageOptions_));

		panicAction = (ListPreference) findPreference(Preferences.Keys.PANIC_ACTION);
		updateSummaryWithChoice(panicAction, panicAction.getValue(), getResources().getStringArray(R.array.panicActionOptions_));
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
		

		boolean prefStealthIcon = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("prefStealthIcon",false);
		setIcon(prefStealthIcon);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//		if (key.equals(Preferences.Keys.LOCK_SCREEN_MODE)) {
//			updateSummaryWithChoice(lockScreenMode, sharedPreferences.getString(key, "1"), getResources().getStringArray(R.array.lockScreenOptions_));
//		} else
		if(key.equals(Preferences.Keys.LANGUAGE)) {
			updateSummaryWithChoice(language, sharedPreferences.getString(key, "0"), getResources().getStringArray(R.array.languages_));
		} //else if(key.equals(Models.IUser.ASSET_ENCRYPTION)) {
		//	updateSummaryWithChoice(originalImage, sharedPreferences.getString(key, "0"), getResources().getStringArray(R.array.originalImageOptions_));
		//}
	else if(key.equals(Preferences.Keys.PANIC_ACTION)) {
			updateSummaryWithChoice(panicAction, sharedPreferences.getString(key, "0"), getResources().getStringArray(R.array.panicActionOptions_));
		}
		
	}
	
	private void updateSummaryWithChoice(Preference pref, String choiceValue, String[] choices) {
		pref.setSummary(choices[Integer.parseInt(choiceValue)]);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
		{
			finish();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}
	
	 
    private void setIcon (boolean enableAltIcon) {
        Context ctx = this;
        PackageManager pm = getPackageManager();
        ActivityManager am = (ActivityManager)getSystemService(Activity.ACTIVITY_SERVICE);
        
        // Enable/disable activity-aliases
        
        pm.setComponentEnabledSetting(
                new ComponentName(ctx, "org.witness.informacam.app.InformaActivity-Alt"), 
                enableAltIcon ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
     
        pm.setComponentEnabledSetting(
                new ComponentName(ctx, "org.witness.informacam.app.InformaActivity"), 
                (!enableAltIcon) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
     
     
        // Find launcher and kill it
        
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> resolves = pm.queryIntentActivities(i, 0);
        for (ResolveInfo res : resolves) {
            if (res.activityInfo != null) {
                am.killBackgroundProcesses(res.activityInfo.packageName);
            }
        }
        
        // Change ActionBar icon
        
    }  

	
	
}
