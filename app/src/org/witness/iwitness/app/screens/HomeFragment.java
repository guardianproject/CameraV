package org.witness.iwitness.app.screens;

import info.guardianproject.odkparser.widgets.ODKSeekBar;

import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.utils.Constants.ListAdapterListener;
import org.witness.informacam.utils.Constants.Models;
import org.witness.iwitness.R;
import org.witness.iwitness.app.PreferencesActivity;
import org.witness.iwitness.app.WipeActivity;
import org.witness.iwitness.app.screens.popups.AudioNotePopup;
import org.witness.iwitness.app.screens.popups.SharePopup;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;
import org.witness.iwitness.utils.Constants.App.Home;
import org.witness.iwitness.utils.Constants.Codes.Routes;
import org.witness.iwitness.utils.Constants.HomeActivityListener;
import org.witness.iwitness.utils.Constants.Preferences;
import org.witness.iwitness.utils.adapters.HomePhotoAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class HomeFragment extends SherlockFragment implements
		ListAdapterListener, OnClickListener {
	View rootView;

	Activity a = null;

	Handler h = new Handler();

	HomePhotoAdapter mPhotoAdapter;
	List<IMedia> listMedia = null;

	private static final String LOG = Home.LOG;
	private final InformaCam informaCam = InformaCam.getInstance();
	private ActionMode mActionMode;

	private View mBtnPhoto;

	private View mBtnVideo;

	private View mBtnGallery;

	private ViewPager mPhotoPager;

	private GestureDetector mTapGestureDetector;

	private View mBtnAudioNote;

	private View mBtnShare;

	private boolean mHasShownSwipeHint = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_home_main, null);
		return rootView;
	}

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(LOG, "GALLERY ON ACTIVITY CREATED CALLED");

		initLayout(savedInstanceState);
	}


	private void initData() {

		listMedia = informaCam.mediaManifest
				.sortBy(Models.IMediaManifest.Sort.DATE_DESC);
		if (listMedia != null)
			listMedia = new ArrayList<IMedia>(listMedia);

		mPhotoAdapter = new HomePhotoAdapter(a, listMedia);
		mPhotoPager.setAdapter(mPhotoAdapter);

		mTapGestureDetector = new GestureDetector(a,
				new TapGestureListener(), h);
		mPhotoPager.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mTapGestureDetector.onTouchEvent(event);
				return false;
			}
		});
	}

	private void initLayout(Bundle savedInstanceState) {

		mPhotoPager = (ViewPager) rootView.findViewById(R.id.pagerPhotos);

		initData();

		mBtnPhoto = rootView.findViewById(R.id.btnPhoto);
		mBtnPhoto.setOnClickListener(this);
		mBtnVideo = rootView.findViewById(R.id.btnVideo);
		mBtnVideo.setOnClickListener(this);
		mBtnGallery = rootView.findViewById(R.id.btnGallery);
		mBtnGallery.setOnClickListener(this);

		mBtnAudioNote = rootView.findViewById(R.id.btnAudioNote);
		mBtnAudioNote.setOnClickListener(this);
		mBtnShare = rootView.findViewById(R.id.btnShare);
		mBtnShare.setOnClickListener(this);

		showSwipeHint();
	}

	private void showSwipeHint() {

		synchronized (this)
		{
			if (mHasShownSwipeHint || mPhotoAdapter == null
				|| mPhotoAdapter.getCount() == 0)
				return;
			mHasShownSwipeHint = true;
		}

		final View swipeInfo = rootView.findViewById(R.id.popupInfoSwipe);
		final Animation animation = AnimationUtils.loadAnimation(a,
				R.anim.info_fade_in_fade_out);
		swipeInfo.setAnimation(animation);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				rootView.findViewById(R.id.popupInfoSwipe).setVisibility(
						View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
				rootView.findViewById(R.id.popupInfoSwipe).setVisibility(
						View.VISIBLE);
			}
		});
		rootView.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				animation.startNow();
			}
		}, 3000);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.activity_home, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings: {
			((HomeActivityListener) a).setLocale(PreferenceManager
					.getDefaultSharedPreferences(a).getString(
							Preferences.Keys.LANGUAGE, "0"));
			Intent settingIntent = new Intent(a, PreferencesActivity.class);
			a.startActivity(settingIntent);
		}
			return true;

		case R.id.menu_panic: {
			Intent wipeIntent = new Intent(a, WipeActivity.class);
			a.startActivityForResult(wipeIntent, Routes.WIPE);
		}
			return true;

		case R.id.menu_select: {
			mActionMode = getSherlockActivity().startActionMode(
					mActionModeSelect);
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	private final ActionMode.Callback mActionModeSelect = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			menu.add(Menu.NONE, R.string.menu_share, 0, R.string.menu_share)
					.setIcon(R.drawable.ic_gallery_share);
			menu.add(Menu.NONE, R.string.home_gallery_delete, 0,
					R.string.home_gallery_delete).setIcon(
					R.drawable.ic_gallery_trash);
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.string.menu_done:
				mode.finish(); // Action picked, so close the CAB
				return true;
			case R.string.menu_share:
				return true;
			default:
				return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
	};

	private void updateAdapters() {
		if (listMedia != null) {
			this.mPhotoAdapter.update(listMedia);

			if (this.mPhotoPager != null)
				mPhotoPager.invalidate();

			showSwipeHint();
			// if (listMedia != null && listMedia.size() > 0) {
			// if (noMedia != null)
			// noMedia.setVisibility(View.GONE);
			// } else {
			//
			// if (noMedia != null)
			// noMedia.setVisibility(View.VISIBLE);
			// }
		}

	}

	@Override
	public void updateAdapter(int which) {
		Log.d(LOG, "UPDATING OUR ADAPTERS");
		if (a != null) {
			listMedia = informaCam.mediaManifest
					.sortBy(Models.IMediaManifest.Sort.DATE_DESC);
			if (listMedia != null)
				listMedia = new ArrayList<IMedia>(listMedia);
			updateAdapters();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnPhoto) {
			((HomeActivityListener) a).launchCamera();
		} else if (v == mBtnVideo) {
			((HomeActivityListener) a).launchVideo();
		} else if (v == mBtnGallery) {
			((HomeActivityListener) a).launchGallery();
		} else if (v == mBtnAudioNote) {
			recordNewAudio();
		} else if (v == mBtnShare) {
			IMedia currentMedia = (IMedia) mPhotoAdapter
					.getObjectFromIndex(mPhotoPager.getCurrentItem());
			if (currentMedia != null)
				new SharePopup(a, currentMedia);
		}
	}

	class TapGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			IMedia currentMedia = (IMedia) mPhotoAdapter
					.getObjectFromIndex(mPhotoPager.getCurrentItem());
			if (currentMedia != null)
				((HomeActivityListener) a).launchEditor(currentMedia);
			return true;
		}
	}

	private void recordNewAudio() {

		IMedia currentMedia = (IMedia) mPhotoAdapter
				.getObjectFromIndex(mPhotoPager.getCurrentItem());
		if (currentMedia == null)
			return;

		IForm audioForm = null;
		ODKSeekBar audioFormAnswerHolder = new ODKSeekBar(a);

		IRegion overviewRegion = currentMedia.getTopLevelRegion();
		if (overviewRegion == null) {
			overviewRegion = currentMedia.addRegion(a,
					null);
		}
		// add an audio form!
		for (IForm form : FormUtility.getAvailableForms()) {
			if (form.namespace.equals(Forms.FreeAudio.TAG)) {
				audioForm = new IForm(form, a);
				audioForm.answerPath = new info.guardianproject.iocipher.File(
						currentMedia.rootFolder,
						"form_a" + System.currentTimeMillis())
						.getAbsolutePath();

				overviewRegion.addForm(audioForm);
			}
		}

		// audioForm.associate(audioFormAnswerHolder, Forms.FreeAudio.PROMPT);
		new AudioNoteFormPopup(a, audioForm);
	}

	private class AudioNoteFormPopup extends AudioNotePopup {
		private final IForm mForm;

		public AudioNoteFormPopup(Activity a, IForm f) {
			super(a, f);
			mForm = f;
		}

		@Override
		public void cancel() {
			mForm.answer(Forms.FreeAudio.PROMPT);
			progress.shutDown();
			super.cancel();
		}
	}
}
