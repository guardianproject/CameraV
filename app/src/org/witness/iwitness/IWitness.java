package org.witness.iwitness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.witness.informacam.InformaCam;
import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.ui.CameraActivity;
import org.witness.informacam.ui.WizardActivity;
import org.witness.informacam.utils.InformaCamBroadcaster.InformaCamStatusListener;
import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.HomeActivity;
import org.witness.iwitness.app.LoginActivity;
import org.witness.iwitness.app.screens.wizard.AddOrganizationsPreference;
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
import android.os.Handler;
import android.util.Log;

public class IWitness extends Activity implements InformaCamStatusListener {
	Intent init = null;
	Intent route = null;
	int routeCode;
	
	private final static String LOG = Constants.App.Router.LOG;
	
	private Handler h = new Handler();
	//private ServiceConnection sc;
	
	InformaCam informaCam;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		init = getIntent();
		
		setContentView(R.layout.activity_main);
		
		informaCam = (InformaCam)getApplication();
		informaCam.setStatusListener(this);
		
		/*
		sc = new ServiceConnection() {

			@SuppressWarnings("static-access")
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				informaCam = ((LocalBinder) service).getService().getInstance(IWitness.this); 
				
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				informaCam = null;
			}
			
		};
		bindService(new Intent(this, InformaCam.class), sc, Context.BIND_AUTO_CREATE);
		*/
		
		
		try {
			Iterator<String> i = savedInstanceState.keySet().iterator();
			while(i.hasNext()) {
				String outState = i.next();
				if(outState.equals(Home.TAG) && savedInstanceState.getBoolean(App.TAG)) {
					//onInformaCamStart();
				}
			}
		} catch(NullPointerException e) {}
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		informaCam = (InformaCam)getApplication();
		
		try {
			if(route != null) {
				routeByIntent();
				Log.d(LOG, "we have a route! lets go!");
			} else {
				Log.d(LOG, "route is null now, please wait");
				if(informaCam.isAbsolutelyLoggedIn()) {
					route = new Intent(this, HomeActivity.class);
					routeCode = Home.ROUTE_CODE;
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
	//	unbindService(sc);
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
			route = new Intent(this, HomeActivity.class);
			routeCode = Home.ROUTE_CODE;
			
			switch(requestCode) {
			case Codes.Routes.WIZARD:
				FormUtility.installIncludedForms(this);
				break;
			case Codes.Routes.CAMERA:
				route = new Intent(this, EditorActivity.class);
				routeCode = Editor.ROUTE_CODE;
				
				break;
			case Codes.Routes.EDITOR:
				
				break;
			case Codes.Routes.LOGIN:
				informaCam.startup();
				break;
			case Codes.Routes.HOME:
				if(data != null && data.hasExtra(Codes.Extras.CHANGE_LOCALE)) {
					route.putExtra(Codes.Extras.CHANGE_LOCALE, true);
					break;
				}
				
				break;
			}
			
			routeByIntent();
		}
	}
	
	private void routeByIntent() {
		Log.d(LOG, "intent is: " + init.getAction());

		if(Intent.ACTION_MAIN.equals(init.getAction())) {
			// do some extra logic if necessary (but not yet...)
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
		Log.d(LOG, "STARTING INFORMACAM ON IWITNESS");
		
		int code = intent.getBundleExtra(org.witness.informacam.utils.Constants.Codes.Keys.SERVICE).getInt(org.witness.informacam.utils.Constants.Codes.Extras.MESSAGE_CODE);
		
		switch(code) {
		case org.witness.informacam.utils.Constants.Codes.Messages.Wizard.INIT:
			ArrayList<String> wizardFragments = new ArrayList<String>();
			wizardFragments.add(OriginalImagePreference.class.getName());
			wizardFragments.add(AddOrganizationsPreference.class.getName());
						
			route = new Intent(this, WizardActivity.class);
			route.putStringArrayListExtra(Codes.Extras.WIZARD_SUPPLEMENT, wizardFragments);
			route.putStringArrayListExtra(Codes.Extras.SET_LOCALES, new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.languages_))));
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
