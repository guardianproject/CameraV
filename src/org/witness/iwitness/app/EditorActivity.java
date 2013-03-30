package org.witness.iwitness.app;

import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.IManifest;
import org.witness.informacam.models.IForm;
import org.witness.informacam.models.IMedia;
import org.witness.informacam.models.IMediaManifest;
import org.witness.iwitness.R;
import org.witness.iwitness.app.screens.DetailsViewFragment;
import org.witness.iwitness.app.screens.FullScreenViewFragment;
import org.witness.iwitness.utils.Constants;
import org.witness.iwitness.utils.Constants.Codes;
import org.witness.iwitness.utils.Constants.EditorActivityListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class EditorActivity extends SherlockFragmentActivity implements OnClickListener, EditorActivityListener {
	Intent init;

	int fullscreenProxy, detailsProxy;

	Fragment fullscreenView, detailsView, currentFragment;
	public FragmentManager fm;

	ActionBar actionBar;
	ImageButton abNavigationBack, abShareMedia, abToIBA;
	

	private final static String LOG = Constants.App.Editor.LOG;
	private String packageName;

	private InformaCam informaCam;
	public IMedia media;
	public List<IForm> availableForms;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		packageName = getClass().getName();

		Log.d(LOG, "hello " + packageName);
		informaCam = InformaCam.getInstance();

		initData();

		setContentView(R.layout.activity_editor);

		actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);

		fm = getSupportFragmentManager();
	}

	@Override
	public void onResume() {
		super.onResume();
		initLayout();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initData() {
		if(!getIntent().hasExtra(Codes.Extras.EDIT_MEDIA)) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}

		// TODO: actually, you would init without the "true" flag, and then call inflate(mediaId)
		IMediaManifest manifest = new IMediaManifest();
		manifest.inflate(informaCam.ioService.getBytes(IManifest.MEDIA, Type.IOCIPHER));		
		media = new IMedia();
		media.inflate(getIntent().getStringExtra(Codes.Extras.EDIT_MEDIA).getBytes());
		
		availableForms = FormUtility.getAvailableForms();

		if(media == null) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}

		Log.d(LOG, "INITING MEDIA FOR EDIT:\n" + media.asJson().toString());		
	}

	private void initLayout() {
		Bundle fullscreenViewArgs = new Bundle();
		Bundle detailsViewArgs = new Bundle();

		int fullscreenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		fullscreenProxy = 1;

		int detailsOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		detailsProxy = 2;


		if(media.dcimEntry.exif.height < media.dcimEntry.exif.width) {
			fullscreenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			fullscreenProxy = 2;

			detailsOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			detailsProxy = 1;
		}

		fullscreenViewArgs.putInt(Codes.Extras.SET_ORIENTATION, fullscreenOrientation);
		detailsViewArgs.putInt(Codes.Extras.SET_ORIENTATION, detailsOrientation);

		fullscreenView = Fragment.instantiate(this, FullScreenViewFragment.class.getName(), fullscreenViewArgs);
		detailsView = Fragment.instantiate(this, DetailsViewFragment.class.getName(), detailsViewArgs);

		View actionBarView = LayoutInflater.from(this).inflate(R.layout.action_bar_editor, null);

		abNavigationBack = (ImageButton) actionBarView.findViewById(R.id.ab_navigation_back);
		abNavigationBack.setOnClickListener(this);

		abShareMedia = (ImageButton) actionBarView.findViewById(R.id.ab_share_media);
		abShareMedia.setOnClickListener(this);

		abToIBA = (ImageButton) actionBarView.findViewById(R.id.ab_to_fullscreen);
		abToIBA.setOnClickListener(this);

		actionBar.setCustomView(actionBarView);

		int currentOrientation = getResources().getConfiguration().orientation;
		if(currentOrientation == detailsProxy) {
			swapLayout(detailsView);
		} else if(currentOrientation == fullscreenProxy) {
			swapLayout(fullscreenView);
		}
	}

	private void swapLayout(Fragment fragment) {
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.main_fragment_root, fragment);
		ft.addToBackStack(null);
		ft.commit();

		if(fragment == fullscreenView) {
			actionBar.hide();
		} else if(fragment == detailsView) {
			actionBar.show();
		}

		currentFragment = fragment;
	}

	private void saveStateAndFinish() {
		setResult(Activity.RESULT_OK);
		finish();
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
		if(config.orientation == detailsProxy) {
			swapLayout(detailsView);
		} else if(config.orientation == fullscreenProxy) {
			swapLayout(fullscreenView);
		}

		Log.d(LOG, "new orientation: " + config.orientation);
	}

	@Override
	public void onClick(View v) {
		if(v == abNavigationBack) {
			saveStateAndFinish();
		} else if(v == abShareMedia) {

		} else if(v == abToIBA) {

		}

	}

	@Override
	public void onBackPressed() {
		saveStateAndFinish();
	}
}
