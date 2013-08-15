package org.witness.iwitness.app;

import info.guardianproject.odkparser.FormWrapper.ODKFormListener;

import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.utils.Constants.IRegionDisplayListener;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.Constants.Models.IMedia.MimeType;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.media.IImage;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.media.IVideo;
import org.witness.informacam.ui.editors.IRegionDisplay;
import org.witness.iwitness.R;
import org.witness.iwitness.app.screens.DetailsViewFragment;
import org.witness.iwitness.app.screens.editors.FullScreenImageViewFragment;
import org.witness.iwitness.app.screens.editors.FullScreenVideoViewFragment;
import org.witness.iwitness.app.screens.popups.SharePopup;
import org.witness.iwitness.utils.Constants;
import org.witness.iwitness.utils.Constants.Codes;
import org.witness.iwitness.utils.Constants.EditorActivityListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class EditorActivity extends SherlockFragmentActivity implements OnClickListener, EditorActivityListener, IRegionDisplayListener {
	Intent init;

	int fullscreenProxy, detailsProxy;

	Fragment fullscreenView, detailsView, currentFragment;
	public FragmentManager fm;

	ActionBar actionBar;
	ImageButton abNavigationBack, abShareMedia;


	private final static String LOG = Constants.App.Editor.LOG;

	private InformaCam informaCam;
	public IMedia media;
	private String mediaId;
	public List<IForm> availableForms;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		informaCam = (InformaCam)getApplication();

		initData();
		
		if (media.bitmapPreview != null)
		{

			setContentView(R.layout.activity_editor);
	
			actionBar = getSupportActionBar();
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);
	
			fm = getSupportFragmentManager();
			
		}
		else
		{
			Toast.makeText(this, "Could not open image", Toast.LENGTH_LONG).show();
			finish();
		}
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

		mediaId = getIntent().getStringExtra(Codes.Extras.EDIT_MEDIA);
		media = informaCam.mediaManifest.getById(mediaId);
		if(media == null) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		
		if(media.dcimEntry.mediaType.equals(MimeType.IMAGE)) {
			IImage image = new IImage(media);
			media = image;
		} else if(media.dcimEntry.mediaType.equals(MimeType.VIDEO)) {			
			IVideo video = new IVideo(media);
			media = video;
		}
		informaCam.informaService.associateMedia(media);

		availableForms = FormUtility.getAvailableForms();
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
		fullscreenViewArgs.putString("mediaId", mediaId);
		detailsViewArgs.putInt(Codes.Extras.SET_ORIENTATION, detailsOrientation);
		detailsViewArgs.putString("mediaId", mediaId);

		if(media.dcimEntry.mediaType.equals(Models.IMedia.MimeType.IMAGE)) {
			fullscreenView = Fragment.instantiate(this, FullScreenImageViewFragment.class.getName(), fullscreenViewArgs);
		} else if(media.dcimEntry.mediaType.equals(Models.IMedia.MimeType.VIDEO)) {
			fullscreenView = Fragment.instantiate(this, FullScreenVideoViewFragment.class.getName(), fullscreenViewArgs);
		}
		
		detailsView = Fragment.instantiate(this, DetailsViewFragment.class.getName(), detailsViewArgs);

		View actionBarView = LayoutInflater.from(this).inflate(R.layout.action_bar_editor, null);

		abNavigationBack = (ImageButton) actionBarView.findViewById(R.id.ab_navigation_back);
		abNavigationBack.setOnClickListener(this);

		abShareMedia = (ImageButton) actionBarView.findViewById(R.id.ab_share_media);
		abShareMedia.setOnClickListener(this);

		actionBar.setCustomView(actionBarView);

		int currentOrientation = getResources().getConfiguration().orientation;
		if(currentOrientation == detailsProxy) {
			swapLayout(detailsView);
		} else if(currentOrientation == fullscreenProxy) {
			swapLayout(fullscreenView);
		}
	}

	private void swapLayout(Fragment fragment) {
		try {
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
		} catch(IllegalStateException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
	}

	private void saveStateAndFinish() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(((ODKFormListener) currentFragment).saveForm()) {
					media.save();
				}
			}
		}).start();
		
		setResult(Activity.RESULT_OK);
		finish();
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);

		if(((ODKFormListener) currentFragment).saveForm()) {

			if(config.orientation == detailsProxy) {
				swapLayout(detailsView);
			} else if(config.orientation == fullscreenProxy) {
				swapLayout(fullscreenView);
			}

			Log.d(LOG, "new orientation: " + config.orientation);
		}
	}

	@Override
	public void onClick(View v) {
		if(v == abNavigationBack) {
			saveStateAndFinish();
		} else if(v == abShareMedia) {
			new SharePopup(this, media);
		}

	}

	@Override
	public void onBackPressed() {
		saveStateAndFinish();
	}

	@Override
	public void onMediaScanned(Uri uri) {
		((EditorActivityListener) fullscreenView).onMediaScanned(uri);		
	}

	@Override
	public void onSelected(IRegionDisplay regionDisplay) {
		((IRegionDisplayListener) fullscreenView).onSelected(regionDisplay);
	}

	@Override
	public void waiter(boolean show) {
		
	}

	@Override
	public IMedia media() {
		return media;
	}

	@Override
	public int[] getSpecs() {
		return ((IRegionDisplayListener) fullscreenView).getSpecs();
	}
}