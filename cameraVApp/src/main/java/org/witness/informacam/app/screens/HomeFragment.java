package org.witness.informacam.app.screens;

import info.guardianproject.odkparser.Constants.RecorderState;
import info.guardianproject.odkparser.widgets.ODKSeekBar;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.app.PreferencesActivity;
import org.witness.informacam.app.R;
import org.witness.informacam.app.WebActivity;
import org.witness.informacam.app.WipeActivity;
import org.witness.informacam.app.screens.popups.AudioNoteSavedPopup;
import org.witness.informacam.app.utils.AudioNoteHelper;
import org.witness.informacam.app.utils.Constants.App.Editor.Forms;
import org.witness.informacam.app.utils.Constants.App.Home;
import org.witness.informacam.app.utils.Constants.Codes.Routes;
import org.witness.informacam.app.utils.Constants.HomeActivityListener;
import org.witness.informacam.app.utils.Constants.Preferences;
import org.witness.informacam.app.utils.adapters.HomePhotoAdapter;
import org.witness.informacam.informa.InformaService;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.ui.AlwaysOnActivity;
import org.witness.informacam.utils.Constants.InformaCamEventListener;
import org.witness.informacam.utils.Constants.ListAdapterListener;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.InformaCamBroadcaster.InformaCamStatusListener;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class HomeFragment extends Fragment implements ListAdapterListener, OnClickListener, InformaCamStatusListener
{
	View rootView;

	Activity a = null;

	Handler h = new Handler();

	HomePhotoAdapter mPhotoAdapter;
	List<IMedia> listMedia = null;

	private static final String LOG = Home.LOG;
	private InformaCam informaCam;
	
	@SuppressWarnings("unused")
	private ActionMode mActionMode;

	private View mBtnPhoto;
	private ImageView mBtnPhotoIcon;
	private View mBtnVideo;
	private ImageView mBtnVideoIcon;

	private View mBtnGallery;

	private ViewPager mPhotoPager;
	private View mNoMedia;

	private GestureDetector mTapGestureDetector;

	private View mBtnAudioNote;
	private View mBtnStopRecording;
	private TextView mTvRecordingTime;

	private View mBtnShare;

	private boolean mHasShownSwipeHint = false;
	private boolean mIsGeneratingKey = false;

	private AudioNoteRecorder mRecorder;

	Switch mActionView;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		
	}

	
	
	@Override
	public void onResume() {
		super.onResume();
		
		initData();

	}
	
	private void initHomeActionBar ()
	{
		if (getActivity() != null)
		{
			ActionBar actionBar = getActivity().getActionBar();
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_SHOW_TITLE);
			actionBar.setTitle(R.string.app_name);			
			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setIcon(R.mipmap.ic_launcher);
			
		}
	}

	@Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
        	initHomeActionBar();
        }
    }


	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_home_main, null);
		return rootView;
	}

	@Override
	public void onAttach(Activity a)
	{
		super.onAttach(a);
		this.a = a;

		informaCam = (InformaCam)a.getApplication();
		
		if (mActionView != null)
		{
			boolean isActive = (InformaService.getInstance() != null && InformaService.getInstance().suckersActive());
			mActionView.setChecked(isActive);
		}

		initHomeActionBar();

		initData();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		//Log.d(LOG, "GALLERY ON ACTIVITY CREATED CALLED");

		initLayout();
	}

	public void initData()
	{
		if (a == null || mPhotoPager == null)
			return;

		//if (informaCam != null && informaCam.mediaManifest != null && mPhotoPager != null)
	//	{
			//just use what the gallery already has
			listMedia = GalleryFragment.getMediaList();

            mPhotoAdapter = new HomePhotoAdapter(a, listMedia);
            mPhotoPager.setAdapter(mPhotoAdapter);
            if (mNoMedia != null)
                mNoMedia.setVisibility(mPhotoAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);


	//	}
	}

	private void initLayout()
	{

		mPhotoPager = (ViewPager) rootView.findViewById(R.id.pagerPhotos);
		mNoMedia = rootView.findViewById(R.id.media_display_no_media);

		mBtnPhoto = rootView.findViewById(R.id.btnPhoto);
		mBtnPhoto.setOnClickListener(this);
		mBtnPhotoIcon = (ImageView) mBtnPhoto.findViewById(R.id.ivPhoto);
		mBtnVideo = rootView.findViewById(R.id.btnVideo);
		mBtnVideo.setOnClickListener(this);
		mBtnVideoIcon = (ImageView) mBtnVideo.findViewById(R.id.ivVideo);
		setIsGeneratingKey(mIsGeneratingKey);
		mBtnGallery = rootView.findViewById(R.id.btnGallery);
		mBtnGallery.setOnClickListener(this);

		/*
		mBtnAudioNote = rootView.findViewById(R.id.btnAudioNote);
		mBtnAudioNote.setOnClickListener(this);
		mBtnStopRecording = rootView.findViewById(R.id.btnStopRecording);
		mBtnStopRecording.setOnClickListener(this);
		mBtnStopRecording.setVisibility(View.GONE);
		mTvRecordingTime = (TextView) rootView.findViewById(R.id.tvRecordingTime);
		mTvRecordingTime.setText("");
		mBtnShare = rootView.findViewById(R.id.btnShare);
		mBtnShare.setOnClickListener(this);
		**/

		mTapGestureDetector = new GestureDetector(a, new TapGestureListener(), h);
		mPhotoPager.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				mTapGestureDetector.onTouchEvent(event);
				return false;
			}
		});
		
		initData();
		
		showSwipeHint();
	}

	private void showSwipeHint()
	{

		synchronized (this)
		{
			if (mHasShownSwipeHint || mPhotoAdapter == null || mPhotoAdapter.getCount() < 2)
				return;
			
			SharedPreferences sp = a.getSharedPreferences(a.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
			int nTimesShown = sp.getInt(Preferences.Keys.HINT_SWIPE_SHOWN, 0);
			if (nTimesShown >= 3)
				return;
			nTimesShown++;
			sp.edit().putInt(Preferences.Keys.HINT_SWIPE_SHOWN, nTimesShown).commit();
			mHasShownSwipeHint = true;
		}

		rootView.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				final View swipeInfo = rootView.findViewById(R.id.popupInfoSwipe);
				final Animation animation = AnimationUtils.loadAnimation(a, R.anim.info_fade_in_fade_out);
				animation.setAnimationListener(new AnimationListener()
				{
					@Override
					public void onAnimationEnd(Animation animation)
					{
						rootView.findViewById(R.id.popupInfoSwipe).setVisibility(View.GONE);
					}

					@Override
					public void onAnimationRepeat(Animation animation)
					{
					}

					@Override
					public void onAnimationStart(Animation animation)
					{
						rootView.findViewById(R.id.popupInfoSwipe).setVisibility(View.VISIBLE);
					}
				});
				swipeInfo.setVisibility(View.INVISIBLE);
				swipeInfo.startAnimation(animation);
			}
		}, 3000);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.activity_home, menu);
 
	    // Get the action view used in your toggleservice item
		/*
	    final MenuItem toggleservice = menu.findItem(R.id.toggleservice);
	    mActionView = (Switch) toggleservice.getActionView();

	    		

	    if (InformaService.getInstance() != null 
	    		&& InformaService.getInstance().suckersActive())
	    	mActionView.setChecked(true);
	    
	    mActionView.setOnCheckedChangeListener(new OnCheckedChangeListener() {

	        @Override
	        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	            
	        	if (isChecked)
	        	{
	        		onInformaStart(null);
	        		
	        	}
	        	else
	        	{

	        		if(InformaService.getInstance() != null && InformaService.getInstance().suckersActive()) {
	        						
	        			InformaService.getInstance().stopAllSuckers();
	        			informaCam.ioService.stopDCIMObserver();
	        			
	        			
	        		}
	        	}
	        	
	        }
	    });*/
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_settings:
			{
				((HomeActivityListener) a).setLocale(PreferenceManager.getDefaultSharedPreferences(a).getString(Preferences.Keys.LANGUAGE, "0"));
				Intent settingIntent = new Intent(a, PreferencesActivity.class);
				a.startActivityForResult(settingIntent, Routes.WIZARD);
			}
				return true;
	
			case R.id.menu_panic:
			{
				Intent wipeIntent = new Intent(a, WipeActivity.class);
				a.startActivityForResult(wipeIntent, Routes.WIPE);
			}
				return true;
	
			case R.id.menu_select:
			{
				mActionMode = getActivity().startActionMode(mActionModeSelect);
				return true;
			}
				
			case R.id.menu_alwayson:
			{
				onInformaStart(null);
				a.startActivity(new Intent(a, AlwaysOnActivity.class));
				  
	        	return true;
			}
			case R.id.menu_lock:
				
				informaCam.attemptLogout();
				getActivity().finish();
				
				return true;
			
			case R.id.menu_creds:
				
				Intent intent = informaCam.exportCredentials();
				if (intent != null)
					getActivity().startActivity(intent);
				
				return true;
				
			case R.id.menu_guide:
				Intent intentWeb = new Intent(getActivity(),WebActivity.class);
				getActivity().startActivity(intentWeb);
				
				return true;
				
				
		}
		
		return super.onOptionsItemSelected(item);
	}

	private final ActionMode.Callback mActionModeSelect = new ActionMode.Callback()
	{

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu)
		{
			menu.add(Menu.NONE, R.string.menu_share, 0, R.string.menu_share).setIcon(R.drawable.ic_gallery_share);
			menu.add(Menu.NONE, R.string.home_gallery_delete, 0, R.string.home_gallery_delete).setIcon(R.drawable.ic_gallery_trash);
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu)
		{
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item)
		{
			switch (item.getItemId())
			{
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
		public void onDestroyActionMode(ActionMode mode)
		{
			mActionMode = null;
		}
	};

	private void updateAdapters()
	{
		if (this.mPhotoAdapter != null)
			this.mPhotoAdapter.update(listMedia);
		if (this.mPhotoPager != null)
			mPhotoPager.invalidate();

		showSwipeHint();
			
		if (this.mNoMedia != null)
			mNoMedia.setVisibility((mPhotoAdapter != null && mPhotoAdapter.getCount() > 0) ? View.GONE : View.VISIBLE);
	}

	@Override
	public void updateAdapter(int which)
	{
		if (a != null)
		{
			//listMedia = informaCam.mediaManifest.sortBy(Models.IMediaManifest.Sort.DATE_DESC);
			initData();
			
			updateAdapters();
		}
	}

	@Override
	public void onClick(View v)
	{
		if (v == mBtnPhoto)
		{
			((HomeActivityListener) a).launchCamera();
		}
		else if (v == mBtnVideo)
		{
			((HomeActivityListener) a).launchVideo();
		}
		else if (v == mBtnGallery)
		{
			((HomeActivityListener) a).launchGallery();
		}
		else if (v == mBtnAudioNote)
		{
			try {
				recordNewAudio();
			} catch (java.lang.InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (v == mBtnStopRecording)
		{
			if (mRecorder != null)
			{
				mRecorder.done();
				mRecorder = null;
			}
		}
		/**
		else if (v == mBtnShare)
		{
			IMedia currentMedia = (IMedia) mPhotoAdapter.getObjectFromIndex(mPhotoPager.getCurrentItem());
			if (currentMedia != null)
				new SharePopup(a, currentMedia);
		}*/
	}

	class TapGestureListener extends GestureDetector.SimpleOnGestureListener
	{
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e)
		{
			IMedia currentMedia = (IMedia) mPhotoAdapter.getObjectFromIndex(mPhotoPager.getCurrentItem());
			if (currentMedia != null)
				((HomeActivityListener) a).launchEditor(currentMedia);
			return true;
		}
	}

	private void recordNewAudio() throws java.lang.InstantiationException, IllegalAccessException
	{

		IMedia currentMedia = (IMedia) mPhotoAdapter.getObjectFromIndex(mPhotoPager.getCurrentItem());
		if (currentMedia == null)
			return;

		IForm audioForm = null;
		
		@SuppressWarnings("unused")
		ODKSeekBar audioFormAnswerHolder = new ODKSeekBar(a);

		IRegion overviewRegion = currentMedia.getTopLevelRegion();
		if (overviewRegion == null)
		{
			overviewRegion = currentMedia.addRegion(a, null);
		}
		// add an audio form!
		for (IForm form : FormUtility.getAvailableForms())
		{
			if (form.namespace.equals(Forms.FreeAudio.TAG))
			{
				audioForm = new IForm(form, a);
				audioForm.answerPath = new info.guardianproject.iocipher.File(currentMedia.rootFolder, "form_a" + System.currentTimeMillis()).getAbsolutePath();

				overviewRegion.addForm(audioForm);
			}
		}

		mRecorder = new AudioNoteRecorder(a, audioForm);
		mRecorder.toggle();
	}

	@Override
	public void setPending(int numPending, int numCompleted)
	{
	}

	private class AudioNoteRecorder extends AudioNoteHelper
	{
		private boolean mIsRecording;

		public AudioNoteRecorder(Activity a, IForm f)
		{
			super(a, f);
			onUpdateClock(0);
		}

		@Override
		protected void onStateChanged()
		{
			super.onStateChanged();
			if (this.getState() == RecorderState.IS_RECORDING)
			{
				mIsRecording = true;
				mBtnAudioNote.setVisibility(View.GONE);
				mBtnStopRecording.setVisibility(View.VISIBLE);
			}
			else
			{
				mBtnAudioNote.setVisibility(View.VISIBLE);
				mBtnStopRecording.setVisibility(View.GONE);

				if (mIsRecording)
				{
					mIsRecording = false;
					form.answer(Forms.FreeAudio.PROMPT);
					try
					{
						form.save(new info.guardianproject.iocipher.FileOutputStream(form.answerPath));

						SharedPreferences sp = a.getSharedPreferences(a.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
						int nTimesShown = sp.getInt(Preferences.Keys.HINT_AUDIO_NOTE_SAVED_SHOWN, 0);
						if (nTimesShown < 2)
						{
							nTimesShown++;
							sp.edit().putInt(Preferences.Keys.HINT_AUDIO_NOTE_SAVED_SHOWN, nTimesShown).commit();
							new AudioNoteSavedPopup(a, this);
						}
					}
					catch (FileNotFoundException e)
					{
						Logger.e(LOG, e);
					}
				}
			}
		}

		@Override
		protected void onUpdateClock(int milliseconds)
		{
			super.onUpdateClock(milliseconds);
			int seconds = ((milliseconds / 1000) % 60);
			int minutes = ((milliseconds / 1000) / 60);
			mTvRecordingTime.setText(String.format("%02d:%02d", minutes, seconds));
		}
	}
	
	public void setIsGeneratingKey(boolean generatingKey)
	{
		
		
		mIsGeneratingKey = generatingKey;
		
		if (rootView != null)
		{
			rootView.post(new Runnable()
			{
				@Override
				public void run()
				{
					if (mBtnPhoto != null)
					{
						mBtnPhoto.setEnabled(!mIsGeneratingKey);
						if (mIsGeneratingKey)
							mBtnPhotoIcon.setImageResource(R.drawable.ic_home_photo_gray);
						else
							mBtnPhotoIcon.setImageResource(R.drawable.ic_home_photo);
					}
					if (mBtnVideo != null)
					{
						mBtnVideo.setEnabled(!mIsGeneratingKey);
						if (mIsGeneratingKey)
							mBtnVideoIcon.setImageResource(R.drawable.ic_home_video_gray);
						else
							mBtnVideoIcon.setImageResource(R.drawable.ic_home_video);
					}
					
					if (mIsGeneratingKey)
					{
						Toast.makeText(a, R.string.wizard_key_is_being_made, Toast.LENGTH_SHORT).show();						
					}
					
				}
			});
		}
	}

	@Override
	public void onInformaCamStart(Intent intent) {
				
		onInformaStart(null);
		
	}

	@Override
	public void onInformaStart(Intent intent) {
		
		if (!InformaService.getInstance().suckersActive())
		{

			Intent intentSuckers = new Intent(informaCam, InformaService.class);
			intentSuckers.setAction("startsuckers");
			informaCam.startService(intentSuckers);
			
			informaCam.ioService.startDCIMObserver(new InformaCamEventListener ()
			{

				@Override
				public void onUpdate(Message message) {
					
				}
				
			}, null, null);

		}
		
		if (mActionView != null)
		{
			boolean isActive = (InformaService.getInstance() != null && InformaService.getInstance().suckersActive());
			mActionView.setChecked(isActive);
		}
	}
	
	@Override
	public void onInformaCamStop(Intent intent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInformaStop(Intent intent) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		
		if (!hidden)
		{
			if (mActionView != null)
			{
				boolean isActive = (InformaService.getInstance() != null && InformaService.getInstance().suckersActive());
				mActionView.setChecked(isActive);
			}
			
			initData();
		}
	}
	

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	
		super.setUserVisibleHint(isVisibleToUser);
		
		onHiddenChanged(!isVisibleToUser);
	}



}
