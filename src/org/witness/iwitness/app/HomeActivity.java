package org.witness.iwitness.app;

import org.witness.informacam.InformaCam;
import org.witness.informacam.ui.CameraActivity;
import org.witness.informacam.utils.Constants.App.Camera;
import org.witness.informacam.utils.Constants.InformaCamEventListener;
import org.witness.iwitness.R;
import org.witness.iwitness.app.screens.CameraChooserFragment;
import org.witness.iwitness.app.screens.MainFragment;
import org.witness.iwitness.utils.Constants.HomeActivityListener;
import org.witness.iwitness.utils.Constants.MainFragmentListener;
import org.witness.iwitness.utils.Constants;
import org.witness.iwitness.utils.Constants.Codes;
import org.witness.iwitness.utils.Constants.Codes.Routes;

import com.deaux.fan.FanView;
import com.deaux.fan.FanView.FanViewListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Display;

public class HomeActivity extends FragmentActivity implements MainFragmentListener, FanViewListener, InformaCamEventListener {
	Intent init;
	private final static String LOG = Constants.App.Home.LOG;
	private String packageName;
	
	private FanView mainHolder;
	Fragment mainFragment, cameraChooserFragment;
	boolean cameraChooserIsShowing = false;
	
	InformaCam informaCam;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		packageName = getClass().getName();
		
		Log.d(LOG, "hello " + packageName);
		setContentView(R.layout.activity_home);
		
		mainHolder = (FanView) findViewById(R.id.main_holder);
		mainFragment = new MainFragment();
		cameraChooserFragment = new CameraChooserFragment();
		
		mainHolder.setFragments(mainFragment, cameraChooserFragment);
		mainHolder.associate(HomeActivity.this);
		
		Log.d(LOG, packageName + " activity is getting informa instance");
		informaCam = InformaCam.getInstance();
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
	public void toggleCameraChooser(boolean cameraChooserShouldShow) {
		if(cameraChooserShouldShow && !cameraChooserIsShowing) {
			mainHolder.showMenu();
		} else if(!cameraChooserShouldShow && cameraChooserIsShowing) {
			mainHolder.showMenu();
		}
	}

	@Override
	public void updateStatus(boolean isShowing) {
		cameraChooserIsShowing = isShowing;		
	}

	@Override
	public boolean getCameraChooserIsShowing() {
		return cameraChooserIsShowing;
	}

	@Override
	public FragmentManager returnFragmentManager() {
		return getSupportFragmentManager();
	}

	@Override
	public void notifyAnimationFinished() {
		((HomeActivityListener) mainFragment).toggleCameraChooser();
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public int[] getDimensions() {
		Display display = getWindowManager().getDefaultDisplay();
		return new int[] {display.getWidth(),display.getHeight()};
	}

	@Override
	public void launchEditor(String mediaId) {
		Log.d(LOG, "launching editor for " + mediaId);
		Intent toEditor = new Intent(this, EditorActivity.class).putExtra(Codes.Extras.MEDIA_ID, mediaId);
		startActivityForResult(toEditor, Routes.EDITOR);
		
	}
	
	@Override
	public void launchCamera(int cameraType) {
		Log.d(LOG, "launching camera as type " + cameraType);
		Intent toCamera = new Intent(this, CameraActivity.class).putExtra(Camera.TYPE, cameraType);
		startActivityForResult(toCamera, Routes.CAMERA);
	}
	
	@Override
	public void onBackPressed() {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	@Override
	public void logoutUser() {
		setResult(Activity.RESULT_CANCELED);
		finish();
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int responseCode, Intent data) {
		informaCam.associateActivity(this);
		if(responseCode == Activity.RESULT_OK) {
			switch(requestCode) {
			case Codes.Routes.CAMERA:
				Log.d(LOG, "we returned these values: " + data.getStringExtra(Codes.Extras.RETURNED_MEDIA));
				
				break;
			case Codes.Routes.LOGOUT:
				logoutUser();
				break;
			}
		}
		Log.d(LOG, "HEY WE FINISHED A THING WITH RESULT " + responseCode);
	}

	@Override
	public void onUpdate(Message message) {
		// TODO Auto-generated method stub
		
	}
}
