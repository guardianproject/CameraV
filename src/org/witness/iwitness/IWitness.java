package org.witness.iwitness;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.ui.CameraActivity;
import org.witness.informacam.InformaCam;
import org.witness.informacam.ui.WizardActivity;
import org.witness.informacam.utils.Constants.Actions;
import org.witness.informacam.utils.Constants.InformaCamEventListener;
import org.witness.informacam.utils.InformaCamBroadcaster;
import org.witness.informacam.utils.InformaCamBroadcaster.InformaCamStatusListener;

import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.HomeActivity;
import org.witness.iwitness.app.LoginActivity;
import org.witness.iwitness.app.screens.wizard.AddOrganizationsPreference;
import org.witness.iwitness.app.screens.wizard.OriginalImagePreference;
import org.witness.iwitness.utils.Constants;
import org.witness.iwitness.utils.Constants.App.Editor;
import org.witness.iwitness.utils.Constants.Codes;
import org.witness.iwitness.utils.Constants.App.Camera;
import org.witness.iwitness.utils.Constants.App.Home;
import org.witness.iwitness.utils.Constants.App.Login;
import org.witness.iwitness.utils.Constants.App.Wizard;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class IWitness extends Activity implements InformaCamEventListener, InformaCamStatusListener {
	Intent init, route;
	int routeCode;
	
	private final static String LOG = Constants.App.Router.LOG;
	private String packageName;
	
	private Handler h = new Handler();
	
	InformaCam informaCam;
	List<InformaCamBroadcaster> icb;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		icb = new Vector<InformaCamBroadcaster>();
		icb.add(new InformaCamBroadcaster(this, new IntentFilter(Actions.INFORMACAM_START)));
		icb.add(new InformaCamBroadcaster(this, new IntentFilter(Actions.INFORMACAM_STOP)));
		
		packageName = getClass().getName();
		
		Log.d(LOG, "hello " + packageName);
		init = getIntent();
		
		setContentView(R.layout.activity_main);
		startService(new Intent(this, InformaCam.class));
	}
	
	@Override
	public void onResume() {
		super.onResume();
		for(InformaCamBroadcaster icb_ : icb) {
			registerReceiver(icb_, ((InformaCamBroadcaster) icb_).intentFilter);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		for(InformaCamBroadcaster icb_ : icb) {
			unregisterReceiver(icb_);
		}
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_CANCELED) {
			Log.d(LOG, "finishing with request code " + requestCode);
			// TODO: LOG OUT IF PREFERENCES SAY SO
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
				Log.d(LOG, "hi i logged in!");
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
		}
		
		startActivityForResult(route, routeCode);
	}

	@Override
	public void onUpdate(Message message) {
		Log.d(LOG, "updating with " + message.getData().toString());
		
		int code = message.getData().getInt(org.witness.informacam.utils.Constants.Codes.Extras.MESSAGE_CODE);
		
		switch(code) {
		case org.witness.informacam.utils.Constants.Codes.Messages.Wizard.INIT:
			ArrayList<String> wizardFragments = new ArrayList<String>();
			wizardFragments.add(OriginalImagePreference.class.getName());
			wizardFragments.add(AddOrganizationsPreference.class.getName());
			
			route = new Intent(this, WizardActivity.class);
			route.putStringArrayListExtra(Codes.Extras.WIZARD_SUPPLEMENT, wizardFragments);
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
	public void onInformaCamStart() {
		Log.d(LOG, "STARTING INFORMACAM ON IWITNESS");
		h.post(new Runnable() {
			@Override
			public void run() {
				Log.d(LOG, packageName + " activity is getting informa instance");
				informaCam = InformaCam.getInstance(IWitness.this);
				
				if(informaCam.isAbsolutelyLoggedIn()) {
					Log.d(LOG, "WE CAN BYPASS AUTH (isAbsolutelyLoggedIn = " + String.valueOf(informaCam.isAbsolutelyLoggedIn()) + ")");
					route = new Intent(IWitness.this, HomeActivity.class);
					routeCode = Home.ROUTE_CODE;
					
					routeByIntent();
				}
			}
		});
	}

	@Override
	public void onInformaCamStop() {
		// TODO Auto-generated method stub
		
	}
}
