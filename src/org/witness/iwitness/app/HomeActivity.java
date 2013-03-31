package org.witness.iwitness.app;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.witness.informacam.InformaCam;
import org.witness.informacam.ui.CameraActivity;
import org.witness.informacam.utils.InformaCamBroadcaster;
import org.witness.informacam.utils.Constants.Actions;
import org.witness.informacam.utils.Constants.InformaCamEventListener;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.InformaCamBroadcaster.InformaCamStatusListener;
import org.witness.informacam.models.IMedia;

import org.witness.iwitness.R;
import org.witness.iwitness.app.screens.CameraFragment;
import org.witness.iwitness.app.screens.GalleryFragment;
import org.witness.iwitness.app.screens.UserManagementFragment;
import org.witness.iwitness.app.screens.menus.MediaActionMenu;
import org.witness.iwitness.app.screens.popups.RenamePopup;
import org.witness.iwitness.app.screens.popups.SharePopup;
import org.witness.iwitness.utils.Constants.App.Home;
import org.witness.iwitness.utils.Constants.HomeActivityListener;
import org.witness.iwitness.utils.Constants;
import org.witness.iwitness.utils.Constants.Codes;
import org.witness.iwitness.utils.Constants.Codes.Routes;
import org.witness.iwitness.utils.actions.ContextMenuAction;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.LinearLayout.LayoutParams;

public class HomeActivity extends SherlockFragmentActivity implements HomeActivityListener, InformaCamEventListener, InformaCamStatusListener {
	Intent init;
	private final static String LOG = Constants.App.Home.LOG;
	private String packageName;
	
	List<Fragment> fragments = new Vector<Fragment>();
	Fragment userManagementFragment, galleryFragment, cameraFragment;
	
	boolean initUploads = true;
	boolean initGallery = false;
	
	int visibility = View.VISIBLE;
	
	LayoutInflater li;
	TabHost tabHost;
	ViewPager viewPager;
	TabPager pager;
	
	InformaCam informaCam;
	
	Handler h = new Handler();
	Waiter waiter;
	MediaActionMenu mam;
	
	List<InformaCamBroadcaster> icb = null;
	Intent toEditor;
	Intent toCamera;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		packageName = getClass().getName();
		
		Log.d(LOG, "hello " + packageName);
		setContentView(R.layout.activity_home);
				
		try {
			Iterator<String> i = savedInstanceState.keySet().iterator();
			while(i.hasNext()) {
				String outState = i.next();
				if(outState.equals(Home.TAG) && savedInstanceState.getBoolean(Home.TAG)) {
					initUploads = false;
					initGallery = true;
				}
			}
		} catch(NullPointerException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
		
		toEditor = new Intent(this, EditorActivity.class);
		toCamera = new Intent(this, CameraActivity.class);
		
		icb = new Vector<InformaCamBroadcaster>();
		icb.add(new InformaCamBroadcaster(this, new IntentFilter(Actions.INFORMACAM_START)));
		icb.add(new InformaCamBroadcaster(this, new IntentFilter(Actions.INFORMACAM_STOP)));
		
		userManagementFragment = Fragment.instantiate(this, UserManagementFragment.class.getName());
		galleryFragment = Fragment.instantiate(this, GalleryFragment.class.getName());
		cameraFragment = Fragment.instantiate(this, CameraFragment.class.getName());
		
		fragments.add(userManagementFragment);
		fragments.add(galleryFragment);
		fragments.add(cameraFragment);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if(icb != null) {
			for(InformaCamBroadcaster icb_ : icb) {
				registerReceiver(icb_, ((InformaCamBroadcaster) icb_).intentFilter);
			}
		}
		
		Log.d(LOG, packageName + " activity is getting informa instance");
		informaCam = InformaCam.getInstance(HomeActivity.this);
		
		if(initUploads) {
			informaCam.uploaderService.init();
		}
		
		initUploads = false;
		initLayout();
	}
	
	private void initLayout() {
		pager = new TabPager(getSupportFragmentManager());
		
		viewPager = (ViewPager) findViewById(R.id.view_pager_root);
		viewPager.setAdapter(pager);
		viewPager.setOnPageChangeListener(pager);
		
		li = LayoutInflater.from(this);
		
		int[] dims = getDimensions();

		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();
		
		TabHost.TabSpec tab = tabHost.newTabSpec(UserManagementFragment.class.getName()).setIndicator(generateTab(li, R.layout.tabs_user_management));
		li.inflate(R.layout.fragment_home_user_management, tabHost.getTabContentView(), true);
		tab.setContent(R.id.user_management_root_view);
		tabHost.addTab(tab);
		
		tab = tabHost.newTabSpec(GalleryFragment.class.getName()).setIndicator(generateTab(li, R.layout.tabs_iwitness));
		li.inflate(R.layout.fragment_home_gallery, tabHost.getTabContentView(), true);
		tab.setContent(R.id.gallery_root_view);
		tabHost.addTab(tab);
		
		tab = tabHost.newTabSpec(CameraFragment.class.getName()).setIndicator(generateTab(li, R.layout.tabs_camera_chooser));
		li.inflate(R.layout.fragment_home_camera_chooser, tabHost.getTabContentView(), true);
		tab.setContent(R.id.camera_chooser_root_view);
		tabHost.addTab(tab);
		
		tabHost.setOnTabChangedListener(pager);
		
		for(int i=0; i<tabHost.getTabWidget().getChildCount(); i++) {
			View tab_ = tabHost.getTabWidget().getChildAt(i);
			if(i == 1) {
				tab_.setLayoutParams(new LinearLayout.LayoutParams((int) (dims[0] * 0.5), LayoutParams.MATCH_PARENT));
			} else {
				tab_.setLayoutParams(new LinearLayout.LayoutParams((int) (dims[0] * 0.25), LayoutParams.MATCH_PARENT));
			}
		}

		viewPager.setCurrentItem(1);
		
		if(initGallery) {
			((GalleryFragment) galleryFragment).updateData();
		}
	}
	
	private static View generateTab(final LayoutInflater li, final int resource) {
		return li.inflate(resource, null);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(Home.TAG, true);
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if(icb != null) {
			for(InformaCamBroadcaster icb_ : icb) {
				this.unregisterReceiver(icb_);
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	public int[] getDimensions() {
		Display display = getWindowManager().getDefaultDisplay();
		return new int[] {display.getWidth(),display.getHeight()};
	}

	@Override
	public void launchEditor(IMedia media) {
		toEditor.putExtra(Codes.Extras.EDIT_MEDIA, media.asJson().toString());
		informaCam.startInforma();
		Log.d(LOG, "launching editor for " + media._id);		
	}
	
	@Override
	public void getContextualMenuFor(final String mediaId) {
		List<ContextMenuAction> actions = new Vector<ContextMenuAction>();
		
		ContextMenuAction action = new ContextMenuAction();
		action.label = getResources().getString(R.string.delete);
		action.ocl = new OnClickListener() {

			@Override
			public void onClick(View v) {
				mam.cancel();
				if(((IMedia) informaCam.mediaManifest.getById(mediaId)).delete()) {
					((GalleryFragment) galleryFragment).updateData();
				}
			}
			
		};
		actions.add(action);
		
		action = new ContextMenuAction();
		action.label = getResources().getString(R.string.rename);
		action.ocl = new OnClickListener() {

			@Override
			public void onClick(View v) {
				mam.cancel();
				new RenamePopup(HomeActivity.this, ((IMedia) informaCam.mediaManifest.getById(mediaId)));
			}
			
		};
		actions.add(action);
		
		action = new ContextMenuAction();
		action.label = getResources().getString(R.string.send);
		action.ocl = new OnClickListener() {

			@Override
			public void onClick(View v) {
				mam.cancel();
				new SharePopup(HomeActivity.this, ((IMedia) informaCam.mediaManifest.getById(mediaId)));
				
			}
			
		};
		actions.add(action);
		
		mam = new MediaActionMenu(this, actions);
		mam.Show();
	}
	
	public void launchCamera() {
		
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				startActivityForResult(toCamera, Routes.CAMERA);
			}
		}, 1000);
		
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
		if(responseCode == Activity.RESULT_OK) {
			switch(requestCode) {
			case Codes.Routes.CAMERA:
				Log.d(LOG, "we returned these values: " + data.getStringExtra(Codes.Extras.RETURNED_MEDIA));
				
				informaCam.mediaManifest.sortBy(Models.IMediaManifest.Sort.DATE_DESC);
				((GalleryFragment) galleryFragment).updateData();
				break;
			case Codes.Routes.LOGOUT:
				logoutUser();
				break;
			case Codes.Routes.EDITOR:
				informaCam.stopInforma();
				break;
			}
		}
	}

	@Override
	public void onUpdate(Message message) {
		// TODO Auto-generated method stub
		
	}
	
	class Waiter extends AlertDialog.Builder {
		public Dialog alert;
		public boolean isShowing = false;
		
		protected Waiter(Context context) {
			super(context);
			setCancelable(false);
			
			alert = create();
			setView(LayoutInflater.from(context).inflate(R.layout.extras_waiter, null));
		}
		
		public void Show() {
			isShowing = true;
			alert = this.show();		
		}
		
		public void cancel() {
			isShowing = false;
			alert.cancel();
		}
	}
	
	class TabPager extends FragmentStatePagerAdapter implements TabHost.OnTabChangeListener, OnPageChangeListener {

		public TabPager(FragmentManager fm) {
			super(fm);
		}

		@Override
		public void onTabChanged(String tabId) {
			Log.d(LOG, tabId);
			int i=0;
			for(Fragment f : fragments) {
				if(f.getClass().getName().equals(tabId)) {
					viewPager.setCurrentItem(i);
					break;
				}

				i++;
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {}

		@Override
		public void onPageSelected(int page) {
			tabHost.setCurrentTab(page);
			Log.d(LOG, "setting current page as " + page);
			if(page == 2) {
				launchCamera();
			}
		}

		@Override
		public Fragment getItem(int which) {
			return fragments.get(which);
		}


		@Override
		public int getCount() {
			return fragments.size();
		}

	}

	@Override
	public void onInformaCamStart() {
		startActivityForResult(toEditor, Routes.EDITOR);
		
	}

	@Override
	public void onInformaCamStop() {
		// TODO Auto-generated method stub
		
	}
}
