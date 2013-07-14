package org.witness.iwitness;

import java.util.ArrayList;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.utils.ILanguageMap;
import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.ui.CameraActivity;
import org.witness.informacam.ui.WizardActivity;
import org.witness.informacam.utils.InformaCamBroadcaster.InformaCamStatusListener;
import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.HomeActivity;
import org.witness.iwitness.app.LoginActivity;
import org.witness.iwitness.app.screens.wizard.OriginalImagePreference;
import org.witness.iwitness.utils.Constants;
import org.witness.iwitness.utils.Constants.App;
import org.witness.iwitness.utils.Constants.Preferences;
import org.witness.iwitness.utils.Constants.App.Camera;
import org.witness.iwitness.utils.Constants.App.Editor;
import org.witness.iwitness.utils.Constants.App.Home;
import org.witness.iwitness.utils.Constants.App.Login;
import org.witness.iwitness.utils.Constants.App.Wizard;
import org.witness.iwitness.utils.Constants.Codes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class IWitness extends Activity implements InformaCamStatusListener {
	Intent init = null;
	Intent route = null;
	int routeCode;
	
	private final static String LOG = Constants.App.Router.LOG;
		
	InformaCam informaCam;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		init = getIntent();
		
		setContentView(R.layout.activity_main);
		
		informaCam = (InformaCam)getApplication();
		informaCam.setStatusListener(this);
		
		if(getIntent().hasExtra(Codes.Extras.CHANGE_LOCALE) && getIntent().getBooleanExtra(Codes.Extras.CHANGE_LOCALE, false)) {
			onInformaCamStart(getIntent());
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();		
		informaCam = (InformaCam)getApplication();
		
		try {
			if(route != null) {
				routeByIntent();
			} else {
				Log.d(LOG, "route is null now, please wait");
				Log.d(LOG, "hasCredentialManager? " + String.valueOf(informaCam.hasCredentialManager()));
				Log.d(LOG, "credentialManagerStatus? " + informaCam.getCredentialManagerStatus());
				
				if(informaCam.hasCredentialManager()) {
					switch(informaCam.getCredentialManagerStatus()) {
					case org.witness.informacam.utils.Constants.Codes.Status.UNLOCKED:
						route = new Intent(this, HomeActivity.class);
						routeCode = Home.ROUTE_CODE;
						break;
					case org.witness.informacam.utils.Constants.Codes.Status.LOCKED:
						route = new Intent(this, LoginActivity.class);
						routeCode = Login.ROUTE_CODE;
						break;
					}
					
					routeByIntent();
				} else {
					Log.d(LOG, "no, not logged in");
				}
			}
			
		} catch(NullPointerException e) {
			Log.e(LOG, "informacam has not started again yet");
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(App.TAG, true);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_CANCELED) {
			Log.d(LOG, "finishing with request code " + requestCode);
			
			finish();
		} else if(resultCode == Activity.RESULT_OK) {
			Log.d(LOG, "returning with request code " + requestCode);
			
			route = new Intent(this, HomeActivity.class);
			routeCode = Home.ROUTE_CODE;
			
			switch(requestCode) {
			case Codes.Routes.CAMERA:
				route = new Intent(this, EditorActivity.class);
				routeCode = Editor.ROUTE_CODE;
				
				break;
			case Codes.Routes.EDITOR:
				
				break;
			case Codes.Routes.LOGIN:
				
				break;
			case Codes.Routes.HOME:
				if(data != null && data.hasExtra(Codes.Extras.CHANGE_LOCALE)) {
					route.putExtra(Codes.Extras.CHANGE_LOCALE, true);
				}
				
				break;
			}
			
			routeByIntent();
		} else if(resultCode == Activity.RESULT_FIRST_USER) {
			if(data.hasExtra(Codes.Extras.CHANGE_LOCALE) && data.getBooleanExtra(Codes.Extras.CHANGE_LOCALE, false)) {
				route.putExtra(Codes.Extras.CHANGE_LOCALE, true);
			}
			
		}
	}
	
	private void routeByIntent() {
		Log.d(LOG, "intent is: " + init.getAction());

		if(Intent.ACTION_MAIN.equals(init.getAction())) {
			
		} else if("android.media.action.IMAGE_CAPTURE".equals(init.getAction())) {
			route = new Intent(this, CameraActivity.class);
			routeCode = Camera.ROUTE_CODE;
		} else if(Intent.ACTION_VIEW.equals(init.getAction())) {
			route.setData(init.getData());
		}
		
		route.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(route, routeCode);
	}

	@Override
	public void onInformaCamStart(Intent intent) {
		int code = intent.getBundleExtra(org.witness.informacam.utils.Constants.Codes.Keys.SERVICE).getInt(org.witness.informacam.utils.Constants.Codes.Extras.MESSAGE_CODE);
		Log.d(LOG, "STARTING INFORMACAM ON IWITNESS (routeCode = " + code + ")");
		
		switch(code) {
		case org.witness.informacam.utils.Constants.Codes.Messages.Wizard.INIT:
			ArrayList<String> wizardFragments = new ArrayList<String>();
			wizardFragments.add(OriginalImagePreference.class.getName());
						
			route = new Intent(this, WizardActivity.class);
			
			route.putStringArrayListExtra(Codes.Extras.WIZARD_SUPPLEMENT, wizardFragments);
			ILanguageMap languageMap = new ILanguageMap();
			
			for(int l=0; l<getResources().getStringArray(R.array.languages_).length; l++) {
				languageMap.add(getResources().getStringArray(R.array.locales)[l], getResources().getStringArray(R.array.languages_)[l]);
			}
			
			route.putExtra(Codes.Extras.SET_LOCALES, languageMap);
			route.putExtra(Codes.Extras.LOCALE_PREF_KEY, Preferences.Keys.LANGUAGE);
			routeCode = Wizard.ROUTE_CODE;
			break;
		case org.witness.informacam.utils.Constants.Codes.Messages.Login.DO_LOGIN:
			route = new Intent(this, LoginActivity.class);
			routeCode = Login.ROUTE_CODE;
			break;
		case org.witness.informacam.utils.Constants.Codes.Messages.Home.INIT:
			route = new Intent(this, HomeActivity.class);
			routeCode = Home.ROUTE_CODE;
			break;
		}
		
		routeByIntent();
	}

	@Override
	public void onInformaCamStop(Intent intent) {}

	@Override
	public void onInformaStop(Intent intent) {}

	@Override
	public void onInformaStart(Intent intent) {}
}
