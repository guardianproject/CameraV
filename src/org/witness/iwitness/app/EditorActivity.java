package org.witness.iwitness.app;

import org.witness.informacam.InformaCam;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.IManifest;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.models.IMedia;
import org.witness.informacam.utils.models.IMediaManifest;
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
	
	Fragment fullscreenView, detailsView, currentFragment;
	FragmentManager fm;
	
	ActionBar actionBar;
	ImageButton abNavigationBack, abShareMedia, abToFullscreen;
	
	private final static String LOG = Constants.App.Editor.LOG;
	private String packageName;
	
	private InformaCam informaCam;
	private IMedia media;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		packageName = getClass().getName();
		
		Log.d(LOG, "hello " + packageName);
		
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
		if(!getIntent().hasExtra(Codes.Extras.MEDIA_ID)) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		
		// TODO: actually, you would init without the "true" flag, and then call inflate(mediaId)
		IMediaManifest manifest = new IMediaManifest();
		manifest.inflate(informaCam.ioService.getBytes(IManifest.MEDIA, Type.IOCIPHER));		
		media = (IMedia) manifest.getObjectByParameter(manifest.media, Models.IMedia._ID, getIntent().getStringExtra(Codes.Extras.MEDIA_ID));
		
		if(media == null) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	}
	
	private void initLayout() {
		Bundle fullscreenViewArgs = new Bundle();
		Bundle detailsViewArgs = new Bundle();
		
		int fullscreenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		int detailsOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		
		if(media.orientation == Codes.Media.ORIENTATION_LANDSCAPE) {
			fullscreenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			detailsOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
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
		
		abToFullscreen = (ImageButton) actionBarView.findViewById(R.id.ab_to_fullscreen);
		abToFullscreen.setOnClickListener(this);
		
		actionBar.setCustomView(actionBarView);
		fm.beginTransaction().add(R.id.main_fragment_root, detailsView).commit();
		currentFragment = detailsView;
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
	public void onClick(View v) {
		if(v == abNavigationBack) {
			saveStateAndFinish();
		} else if(v == abShareMedia) {
			
		} else if(v == abToFullscreen) {
			swapLayout(fullscreenView);
		}
		
	}
	
	@Override
	public void onBackPressed() {
		if(currentFragment == fullscreenView) {
			swapLayout(detailsView);
		} else {
			saveStateAndFinish();
		}
	}

	@Override
	public void lockOrientation(int newOrientation) {
		Log.d(LOG, "changing orientation to " + newOrientation);
		setRequestedOrientation(newOrientation);
	}
}
