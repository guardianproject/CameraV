package org.witness.informacam.app.screens;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.camera.StorageManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.app.R;
import org.witness.informacam.app.RemoteShareActivity;
import org.witness.informacam.app.screens.popups.SharePopup;
import org.witness.informacam.app.utils.Constants.App.Home;
import org.witness.informacam.app.utils.Constants.HomeActivityListener;
import org.witness.informacam.app.utils.Constants.Preferences;
import org.witness.informacam.app.utils.adapters.GalleryFilterAdapter;
import org.witness.informacam.app.utils.adapters.GalleryGridAdapter;
import org.witness.informacam.json.JSONException;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.notifications.INotification;
import org.witness.informacam.share.DropboxSyncManager;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.ListAdapterListener;
import org.witness.informacam.utils.Constants.Models;

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
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class GalleryFragment extends Fragment implements
		OnItemClickListener, OnItemLongClickListener, ListAdapterListener,
		OnItemSelectedListener {
	View rootView;
	GridView mediaDisplayGrid;
	GalleryGridAdapter galleryGridAdapter;

	RelativeLayout noMedia;

	Activity a = null;

	boolean isInMultiSelectMode;
	ArrayList<IMedia> batch = null;
	List<IMedia> listMedia = null;

	private static final String LOG = Home.LOG;
	private final InformaCam informaCam = InformaCam.getInstance();
	private ActionMode mActionMode;
	private static int mCurrentFiltering;
	private MenuItem mMenuItemBatchOperations;
	private View mEncodingMedia;

	private MenuItem miDropbox;
	
	@SuppressWarnings("unused")
	private boolean isDataInitialized;
	
	private ProgressBar progressWait;
	private int mNumLoading;
	
	private int mLastProgress = -1;
	
	private Handler h = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			/*
			if(b.containsKey(Models.IMedia.VERSION)) {

				if (b.getString(Models.IMedia.VERSION) != null) {

					mediaExportUris.add(Uri
							.fromFile(new java.io.File(b
									.getString(Models.IMedia.VERSION))));
					
					h.post(new Runnable ()
					{
							public void run ()
							{
								barProgressDialog.incrementProgressBy(1);

								if (barProgressDialog.getProgress() == barProgressDialog.getMax())
								{
									barProgressDialog.dismiss();
									showShareDialog();
								}
								
							}
					});
					
				}
			} else if(b.containsKey(Codes.Keys.UI.PROGRESS)) {
				
			}
			else
			*/
			if (msg.what == -1)
			{				
				String errMsg = b.getString("msg");
				Toast.makeText(a,errMsg, Toast.LENGTH_LONG).show();
			}
		
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_gallery, null);
		mediaDisplayGrid = (GridView) rootView
				.findViewById(R.id.media_display_grid);
		noMedia = (RelativeLayout) rootView
				.findViewById(R.id.media_display_no_media);
		noMedia.setVisibility(View.GONE);
		
		progressWait = (ProgressBar) rootView.findViewById(R.id.progressWait);
		progressWait.setVisibility(View.GONE);
		
		mEncodingMedia = rootView.findViewById(R.id.media_encoding);
		mEncodingMedia.setVisibility(View.GONE);
		mEncodingMedia.findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mEncodingMedia.setVisibility(View.GONE);
			}
		});
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
		initLayout(savedInstanceState);	
	}

	@Override
	public void onResume()
	{
		super.onResume();
		updateAdapters();
		
		DropboxSyncManager.getInstance(a).finishAuthentication();
		
	}

	public void initData() {
		mCurrentFiltering = 0; //All items
		updateData();
	}
	
	public static List<IMedia> getMediaList()
	{

		int sorting = Models.IMediaManifest.Sort.DATE_DESC;
		if (mCurrentFiltering == 1) // Photos
			sorting = Models.IMediaManifest.Sort.TYPE_PHOTO;
		else if (mCurrentFiltering == 2)
			sorting = Models.IMediaManifest.Sort.TYPE_VIDEO;

		List<IMedia> listSource = InformaCam.getInstance().mediaManifest.sortBy(sorting);
		List<IMedia> listMedia = null;

		if (listSource != null) {

			listMedia = new ArrayList<IMedia>(listSource);

			if (mCurrentFiltering == 3) // Tagged items
			{
				for (int i = listMedia.size() - 1; i >= 0; i--) {
					IMedia m = listMedia.get(i);
					boolean hasTags = (m.getInnerLevelRegions().size() > 0);
					if (!hasTags)
						listMedia.remove(i);
				}
			} else if (mCurrentFiltering == 4) // Shared items
			{
				List<INotification> listNotifications = InformaCam.getInstance().notificationsManifest.sortBy(Models.INotificationManifest.Sort.DATE_DESC);

				for (int i = listMedia.size() - 1; i >= 0; i--) {
					boolean hasBeenShared = false;

					IMedia m = listMedia.get(i);
					for (INotification n : listNotifications) {
						if (m._id.equals(n.mediaId)) {
							if (n.type == Models.INotification.Type.SHARED_MEDIA &&
									n.taskComplete) {
								hasBeenShared = true;
								break;
							}
						}
					}
					if (!hasBeenShared)
						listMedia.remove(i);
				}
			} else if (mCurrentFiltering == 5) // encrypted items
			{

				for (int i = listMedia.size() - 1; i >= 0; i--) {

					IMedia m = listMedia.get(i);
					if (m.dcimEntry.fileAsset.source != Storage.Type.IOCIPHER)
						listMedia.remove(i);
				}
			} else if (mCurrentFiltering == 6) // unencrypted items
			{
				if (listMedia != null) {
					for (int i = listMedia.size() - 1; i >= 0; i--) {

						IMedia m = listMedia.get(i);
						if (m.dcimEntry.fileAsset.source != Storage.Type.FILE_SYSTEM)
							listMedia.remove(i);
					}
				}
			}
		}
		
		return listMedia;
	}
	
	private void onMediaListAvailable()
	{
		//progressWait.setVisibility(View.GONE);
		galleryGridAdapter = new GalleryGridAdapter(a, listMedia);
		galleryGridAdapter.setNumLoading(mNumLoading);
		galleryGridAdapter.setInSelectionMode(isInMultiSelectMode);
		if (mediaDisplayGrid != null) {
			mediaDisplayGrid.setAdapter(galleryGridAdapter);
			mediaDisplayGrid.setOnItemLongClickListener(this);
			mediaDisplayGrid.setOnItemClickListener(this);
		}

		updateAdapters();

		if((listMedia != null && listMedia.size() > 0) || this.mNumLoading > 0) {
			if (noMedia != null)
				noMedia.setVisibility(View.GONE);
		} else {

			if (noMedia != null)
				noMedia.setVisibility(View.VISIBLE);
		}
	}
	
	private void updateData()
	{
		Thread getMediaListThread = new Thread(new Runnable()
		{
			@Override
			public void run() {
				listMedia = getMediaList();
				
				if (a != null)
					a.runOnUiThread(new Runnable()
					{
						@Override
						public void run() {
							onMediaListAvailable();
						}
					});
			}
		});
		getMediaListThread.start();
	}

	public void toggleMultiSelectMode(boolean mode) {
		isInMultiSelectMode = mode;
		if (galleryGridAdapter != null)
			galleryGridAdapter.setInSelectionMode(isInMultiSelectMode);
		toggleMultiSelectMode();
	}

	public void toggleMultiSelectMode() {
		// multiSelect.setImageDrawable(a.getResources().getDrawable(isInMultiSelectMode
		// ? R.drawable.ic_action_selected : R.drawable.ic_action_select));
		if (isInMultiSelectMode) {
			batch = new ArrayList<IMedia>();
			// batchEditHolder.setVisibility(View.VISIBLE);
		} else {
			batch = null;
			// batchEditHolder.setVisibility(View.GONE);

		}
		
		if (galleryGridAdapter != null)
			galleryGridAdapter.update(listMedia);
		// galleryListAdapter.update(listMedia);

	}

	private void initLayout(Bundle savedInstanceState) {
		mediaDisplayGrid.removeAllViewsInLayout();
		toggleMultiSelectMode(false);
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> adapterView, final View view,
			final int position, final long id) {
		
		if (position < mNumLoading)
			return false; // Ignore clicks on incomplete items
		 
		 mActionMode = getActivity().startActionMode(
					mActionModeSelect);


		 h.postDelayed(new Runnable ()
		 {
			 public void run ()
			 {
				int actualPosition = position - mNumLoading;
					
				 onItemClick(adapterView, view, actualPosition, id);
			 }
		 }
		 , 100);
		 
		
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position,
			long id) {
		
		if (position < mNumLoading)
			return; // Ignore clicks on incomplete items
		position -= mNumLoading;
		
		if (!isInMultiSelectMode) {
			((HomeActivityListener) a).launchEditor(listMedia.get(position));
		} else {
			try {
				IMedia m = listMedia.get(position);

				if (!m.has(Models.IMedia.TempKeys.IS_SELECTED)) {
					m.put(Models.IMedia.TempKeys.IS_SELECTED, false);
				}

				if (m.getBoolean(Models.IMedia.TempKeys.IS_SELECTED)) {
					m.put(Models.IMedia.TempKeys.IS_SELECTED, false);
					batch.remove(m);
				} else {
					m.put(Models.IMedia.TempKeys.IS_SELECTED, true);
					batch.add(m);
				}

				CheckBox chkSelected = (CheckBox) view
						.findViewById(R.id.chkSelect);
				chkSelected.setChecked(m
						.getBoolean(Models.IMedia.TempKeys.IS_SELECTED));

				if (mActionMode != null)
					mActionMode.invalidate();
				// LinearLayout ll = (LinearLayout)
				// view.findViewById(R.id.gallery_thumb_holder);
				// ll.setBackgroundDrawable(getResources().getDrawable(selectedColor));
			} catch (JSONException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
		}

	}
	
	private boolean mIsSelectAll = false;
	
	private void selectAll (boolean select)
	{
		mIsSelectAll = select;
		
		for (IMedia m : listMedia)
		{
			m.put(Models.IMedia.TempKeys.IS_SELECTED, select);
			
			if (select)
				batch.add(m);
			else
				batch.remove(m);

			mediaDisplayGrid.invalidate();
			mActionMode.invalidate();
			updateAdapters();
		}
	}

	private void updateAdapters() {

		if (listMedia != null && galleryGridAdapter != null)
			this.galleryGridAdapter.update(listMedia);

		if ((listMedia != null && listMedia.size() > 0) || this.mNumLoading > 0)
		{
			if (noMedia != null)
				noMedia.setVisibility(View.GONE);
			if (mMenuItemBatchOperations != null)
				mMenuItemBatchOperations.setVisible(true);
		}
		else
		{
			if (noMedia != null)
				noMedia.setVisibility(View.VISIBLE);
			if (mMenuItemBatchOperations != null)
				mMenuItemBatchOperations.setVisible(false);
		}
	}

	@Override
	public void updateAdapter(int which) {
		Log.d(LOG, "UPDATING OUR ADAPTERS");
		if (a != null) {
			updateData();
		}
	}

	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.activity_home_gallery, menu);

		MenuItem menuFilter = menu.findItem(R.id.menu_filter);
		Spinner spinner = (Spinner) menuFilter.getActionView();
		GalleryFilterAdapter spinnerAdapter = new GalleryFilterAdapter(spinner.getContext(), this.getResources().getTextArray(R.array.filter_options));
		spinner.setAdapter(spinnerAdapter);
		spinner.setOnItemSelectedListener(this);
		
		mMenuItemBatchOperations = menu.findItem(R.id.menu_select);
		
		miDropbox = menu.findItem(R.id.menu_remote_access_dropbox);
		DropboxSyncManager dsm = DropboxSyncManager.getInstance(a);
		if (dsm != null && dsm.isSyncing())
			miDropbox.setChecked(true);
		
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(R.string.home_gallery_title);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setLogo(this.getResources().getDrawable(
				R.drawable.ic_action_up));
		actionBar.setDisplayUseLogoEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			((HomeActivityListener) a).launchMain();
			return true;
		case R.id.menu_select:
			mActionMode = getActivity().startActionMode(
					mActionModeSelect);
			return true;
		case R.id.menu_remote_access_tor:
			enableOnionShare();
			return true;
		case R.id.menu_remote_access_dropbox:
			manageDropboxSync(!item.isChecked());
			return true;
			
		}
		return super.onOptionsItemSelected(item);
	}

	private final ActionMode.Callback mActionModeSelect = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			 MenuInflater inflater = mode.getMenuInflater();
			 inflater.inflate(R.menu.activity_home_gallery_action_mode, menu);
			
			
		//	menu.add(Menu.NONE, R.string.menu_share, 0, R.string.menu_share)
		//		.setIcon(R.drawable.ic_gallery_share);
		//	menu.add(Menu.NONE, R.string.home_gallery_delete, 0,
			//		R.string.home_gallery_delete).setIcon(
				//	R.drawable.ic_gallery_trash);

			toggleMultiSelectMode(true);
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			int nSelected = (batch == null) ? 0 : batch.size();
			mode.setTitle(GalleryFragment.this.getString(
					R.string.home_gallery_selected, nSelected));
			
			 
			 MenuItem miDropbox2 = menu.findItem(R.id.menu_remote_access_dropbox);
				DropboxSyncManager dsm = DropboxSyncManager.getInstance(a);
				if (dsm != null && dsm.isSyncing())
					miDropbox2.setChecked(true);
				
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			
			int shareType = SharePopup.SHARE_TYPE_MEDIA;
			boolean isLocalShare = PreferenceManager.getDefaultSharedPreferences(a).getBoolean("prefExportLocal", false);
			
			switch (item.getItemId()) {
			case R.string.menu_done:
				mode.finish(); // Action picked, so close the CAB
				return true;
			case R.id.menu_home_gallery_select_all:
				selectAll(!mIsSelectAll);
				return true;
			case R.id.menu_share_hash:
				shareHashes();
				return true;
			case R.id.menu_remote_access_tor:
				enableOnionShare();
				return true;
			case R.id.menu_remote_access_dropbox:
				manageDropboxSync(!item.isChecked());
				return true;
			case R.id.menu_share_meta_j3m:
				shareType = SharePopup.SHARE_TYPE_J3M;
			case R.id.menu_share_meta_csv:
				shareType = SharePopup.SHARE_TYPE_CSV;
			case R.id.menu_share:
				
					if (batch.size() > 0)
					{
						new SharePopup(a,batch,false,shareType,isLocalShare);
								
					}
					else
					{

								batch.clear();
								mode.finish();
						
					}						

				return true;
			case R.id.menu_home_gallery_delete:
				((HomeActivityListener) a).waiter(true);
				new Thread(new Runnable() {
					private ActionMode mMode;

					@Override
					public void run() {
						for (IMedia m : batch) {
							m.delete();
						}
						h.post(new Runnable() {
							private ActionMode mMode;

							@Override
							public void run() {
								mMode.finish();
								((HomeActivityListener) a).waiter(false);
								updateData();
							}

							public Runnable init(ActionMode mode) {
								mMode = mode;
								return this;
							}
						}.init(mMode));
					}

					public Runnable init(ActionMode mode) {
						mMode = mode;
						return this;
					}
				}.init(mode)).start();
				return true;
			default:
				return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			toggleMultiSelectMode(false);
			
			DropboxSyncManager dsm = DropboxSyncManager.getInstance(a);
			if (dsm != null && dsm.isSyncing())
				miDropbox.setChecked(true);
			else
				miDropbox.setChecked(false);
		}
	};
	
	private void shareHashes ()
	{
		try
		{
			StringBuffer hexStrings = new StringBuffer();
			
			for (IMedia media : batch)
			{
				String mediaId = ((IMedia) media)._id;
				boolean hasBeenShared = false;
				
				List<INotification> listNotifs = InformaCam.getInstance().notificationsManifest.sortBy(Models.INotificationManifest.Sort.DATE_DESC);
				for (INotification n :listNotifs)
				{
					
					if (mediaId.equals(n.mediaId))
					{
						//this means we sent to an organization, likely the testbed
						if ((n.type == Models.INotification.Type.EXPORTED_MEDIA) &&
							n.taskComplete)
						{
							hasBeenShared = true;
						}
					}
				}
				
				@SuppressWarnings("unused")
				String j3m = ((IMedia) media).buildJ3M(a, false, null);
				
				//generate public hash id from values
				String creatorHash = media.genealogy.createdOnDevice;
				StringBuffer mediaHash = new StringBuffer();
				for(String mHash : media.genealogy.hashes) {
					mediaHash.append(mHash);
				}
				
				MessageDigest md;
				try {
					md = MessageDigest.getInstance("SHA-1");
					md.update((creatorHash+mediaHash.toString()).getBytes());
					
					byte[] byteData = md.digest();
					
					   StringBuffer hexString = new StringBuffer();
				    	for (int i=0;i<byteData.length;i++) {
				    		String hex=Integer.toHexString(0xff & byteData[i]);
				   	     	if(hex.length()==1) hexString.append('0');
				   	     	hexString.append(hex);
				    	}
				    	
				    	hexStrings.append(getString(R.string._id_)).append(hexString).append(' ');
					
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			Intent sendIntent = new Intent();
	    	sendIntent.setAction(Intent.ACTION_SEND);
	    	
	    	//if (!hasBeenShared) //if it hasn't been shared, then just show the hashes
	    	sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string._camerav_notarization_id_) + ' ' + hexStrings.toString().trim());
	    	//else //if it has, then show a URL
	    		//sendIntent.putExtra(Intent.EXTRA_TEXT, "#" + getString(R.string.app_name) + " https://j3m.info/submissions/?hashes=" + hexString + " (media:" + mediaHash + ")");
	    	
	    	sendIntent.setType("text/plain");
	    	startActivity(sendIntent);
		}
		catch (Exception e)
		{
			
		}
		
	}

	@Override
	public void setPending(final int numPending, final int numCompleted) {		
		
		if(a == null) {
			return; 
		}
		
		mNumLoading = numPending - numCompleted;
		
		a.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
				if (galleryGridAdapter != null)
				{
					galleryGridAdapter.setNumLoading(numPending - numCompleted);
				}	
				
				if (numPending > 0 && numCompleted == 0)
				{
					SharedPreferences sp = a.getSharedPreferences(a.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
					int nTimesShown = sp.getInt(Preferences.Keys.HINT_PROCESSING_IMAGES_SHOWN, 0);
					if (nTimesShown < 3)
					{
						nTimesShown++;
						sp.edit().putInt(Preferences.Keys.HINT_PROCESSING_IMAGES_SHOWN, nTimesShown).commit();
						mEncodingMedia.setVisibility(View.VISIBLE);
					}
				}
				else if (mNumLoading == 0)
				{
					mEncodingMedia.setVisibility(View.GONE);
				}
				
			}
		});
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition,
			long itemId) {
		mCurrentFiltering = itemPosition;
		updateAdapter(0);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
	
	private void enableOnionShare ()
	{
		Intent intent = new Intent(a, RemoteShareActivity.class);
		
		if (batch != null && batch.size() > 0)
		{
			ArrayList<String> mediaIds = new ArrayList<String>();
			
			for (IMedia media : batch)
				mediaIds.add(media._id);
			
			intent.putExtra("medialist", mediaIds.toArray(new String[mediaIds.size()]));
			
		}
		
		
		startActivity(intent);

	}

	private void manageDropboxSync (boolean enable)
	{
		miDropbox.setChecked(enable);
		
		DropboxSyncManager dsm = DropboxSyncManager.getInstance(a);
		
		if (enable)
		{
			if (!dsm.isSyncing())
			{
				//do web oauth (doesn't require local app)
				boolean isInit = dsm.start(a);
				
				if (isInit) //if init'd, then backup all existing files that aren't already backed up!
				{
					
					if (batch != null)			
						for (IMedia media : batch)
						{				
							dsm.uploadMediaAsync(media);
						}
				}
			}
			
		}
		else
		{
			dsm.stop();
		}
		
		
	}
	
	
}
