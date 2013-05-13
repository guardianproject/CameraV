package org.witness.iwitness.app;

import org.witness.informacam.InformaCam;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class WipeActivity extends Activity implements OnClickListener {
	Intent init;
	private final static String LOG = Constants.App.Wipe.LOG;
	private String packageName;
	
	Button changeSettings, cancel, logout;
	ImageView wipeTrigger;
	
	InformaCam informaCam = InformaCam.getInstance();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		packageName = getClass().getName();
		
		Log.d(LOG, "hello " + packageName);
		
		setContentView(R.layout.activity_wipe);
		
		changeSettings = (Button) findViewById(R.id.wipe_change_settings);
		changeSettings.setOnClickListener(this);
		
		cancel = (Button) findViewById(R.id.wipe_cancel);
		cancel.setOnClickListener(this);
		
		logout = (Button) findViewById(R.id.wipe_logout);
		logout.setOnClickListener(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if(v == logout) {
			if(informaCam.attemptLogout()) {
				setResult(Activity.RESULT_OK);
				finish();
			}
		} else if(v == cancel) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		
	}
}
