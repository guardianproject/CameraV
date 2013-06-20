package org.witness.iwitness.app;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.witness.informacam.InformaCam;
import org.witness.informacam.models.connections.IConnection;
import org.witness.informacam.models.connections.IMessage;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.notifications.INotification;
import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.ui.CameraActivity;
import org.witness.informacam.utils.Constants.InformaCamEventListener;
import org.witness.informacam.utils.Constants.ListAdapterListener;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.InformaCamBroadcaster.InformaCamStatusListener;
import org.witness.iwitness.R;
import org.witness.iwitness.app.screens.CameraFragment;
import org.witness.iwitness.app.screens.GalleryFragment;
import org.witness.iwitness.app.screens.UserManagementFragment;
import org.witness.iwitness.app.screens.menus.MediaActionMenu;
import org.witness.iwitness.app.screens.popups.RenamePopup;
import org.witness.iwitness.app.screens.popups.SharePopup;
import org.witness.iwitness.app.screens.popups.TextareaPopup;
import org.witness.iwitness.app.screens.popups.WaitPopup;
import org.witness.iwitness.utils.Constants;
import org.witness.iwitness.utils.Constants.App.Home;
import org.witness.iwitness.utils.Constants.Codes;
import org.witness.iwitness.utils.Constants.Codes.Routes;
import org.witness.iwitness.utils.Constants.HomeActivityListener;
import org.witness.iwitness.utils.Constants.Preferences;
import org.witness.iwitness.utils.actions.ContextMenuAction;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
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
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class HomeActivity extends SherlockFragmentActivity implements HomeActivityListener, InformaCamStatusListener, InformaCamEventListener, ListAdapterListener {
	Intent init, route;
	private final static String LOG = Constants.App.Home.LOG;
	private String lastLocale = null;

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

	static Handler h = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Bundle msgData = msg.getData();
			if(msgData.containsKey(Models.INotification.CLASS)) {
				switch(msgData.getInt(Models.INotification.CLASS)) {
				case Models.INotification.Type.NEW_KEY:

					break;
				}
			}
		}
	};

	MediaActionMenu mam;
	WaitPopup waiter;

	Intent toEditor, toCamera;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		informaCam = (InformaCam)getApplication();
		informaCam.setStatusListener(this);
		informaCam.setEventListener(this);
		informaCam.setListAdapterListener(this);
		
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
		} catch(NullPointerException e) {}

		toEditor = new Intent(this, EditorActivity.class);
		toCamera = new Intent(this, CameraActivity.class);
		route = null;

		userManagementFragment = Fragment.instantiate(this, UserManagementFragment.class.getName());
		galleryFragment = Fragment.instantiate(this, GalleryFragment.class.getName());
		cameraFragment = Fragment.instantiate(this, CameraFragment.class.getName());

		fragments.add(userManagementFragment);
		fragments.add(galleryFragment);
		fragments.add(cameraFragment);

		init = getIntent();
		
		initLayout();
		checkForUpdates();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if(getIntent().hasExtra(Constants.Codes.Extras.CHANGE_LOCALE)) {
			getIntent().removeExtra(Constants.Codes.Extras.CHANGE_LOCALE);
		}
		
		String currentLocale = PreferenceManager.getDefaultSharedPreferences(this).getString(Preferences.Keys.LANGUAGE, "0");
		
		if(lastLocale != null && !lastLocale.equals(currentLocale)) {
			setNewLocale(currentLocale);
			return;
		}
		
		 checkForCrashes();

		informaCam = (InformaCam)getApplication();

		if(initUploads) {
			informaCam.initUploads();
		}

		if(init.getData() != null) {
			final Uri ictdURI = init.getData();
			Log.d(LOG, "INIT KEY: " + ictdURI);

			h.post(new Runnable() {
				@Override
				public void run() {
					IOrganization organization = informaCam.installICTD(ictdURI, h);
					if(organization != null) {
						viewPager.setCurrentItem(0);
					} else {
						// TODO: handle error
					}
				}
			});
		}
	}
	
	private void setNewLocale(String locale_code) {
		Log.d(LOG, "***************** setting new LOCALE!");
		Configuration configuration = new Configuration();
		switch(Integer.parseInt(locale_code)) {
		case Preferences.Locales.DEFAULT:
			configuration.locale = new Locale(Locale.getDefault().getLanguage());
			break;
		case Preferences.Locales.EN:
			configuration.locale = new Locale("en");
			break;
		case Preferences.Locales.ES:
			configuration.locale = new Locale("es");
			break;
		case Preferences.Locales.FR:
			configuration.locale = new Locale("fr");
			break;
		case Preferences.Locales.AR:
			configuration.locale = new Locale("ar");
			break;
		}
		getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

		getIntent().putExtra(Constants.Codes.Extras.CHANGE_LOCALE, true);
		setResult(Activity.RESULT_OK, new Intent().putExtra(Constants.Codes.Extras.CHANGE_LOCALE, true));
		finish();
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

	public void launchCamera() {
		route = toCamera;

		waiter = new WaitPopup(this);
		informaCam.startInforma();
	}

	@Override
	public void launchEditor(IMedia media) {
		toEditor.putExtra(Codes.Extras.EDIT_MEDIA, media._id);

		route = toEditor;
		waiter = new WaitPopup(this);
		informaCam.startInforma();
		Log.d(LOG, "launching editor for " + media._id);		
	}

	@Override
	public void getContextualMenuFor(final INotification notification) {
		List<ContextMenuAction> actions = new Vector<ContextMenuAction>();

		ContextMenuAction action = new ContextMenuAction();
		action.label = getResources().getString(R.string.delete);
		action.ocl = new OnClickListener() {
			@Override
			public void onClick(View v) {
				mam.cancel();
				informaCam.notificationsManifest.getById(notification._id).delete();

				((ListAdapterListener) userManagementFragment).updateAdapter(Codes.Adapters.NOTIFICATIONS);
			}
		};
		actions.add(action);

		mam = new MediaActionMenu(this, actions);
		mam.Show();
	}

	@Override
	public void getContextualMenuFor(final IOrganization organization) {
		List<ContextMenuAction> actions = new Vector<ContextMenuAction>();

		ContextMenuAction action = new ContextMenuAction();
		action.label = getResources().getString(R.string.send_message);
		action.ocl = new OnClickListener() {

			@Override
			public void onClick(View v) {
				mam.cancel();
				new TextareaPopup(HomeActivity.this, organization) {
					@Override
					public void cancel() {
						IConnection connection = new IMessage(organization, this.prompt.getText().toString());
						informaCam.uploaderService.addToQueue(connection);

						super.cancel();
					}
				};
			}
		};
		actions.add(action);

		mam = new MediaActionMenu(this, actions);
		mam.Show();
	}

	@Override
	public void getContextualMenuFor(final IMedia media) {
		List<ContextMenuAction> actions = new Vector<ContextMenuAction>();

		ContextMenuAction action = new ContextMenuAction();
		action.label = getResources().getString(R.string.delete);
		action.ocl = new OnClickListener() {

			@Override
			public void onClick(View v) {
				mam.cancel();
				if((informaCam.mediaManifest.getById(media._id)).delete()) {
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
				new RenamePopup(HomeActivity.this, informaCam.mediaManifest.getById(media._id));
			}

		};
		actions.add(action);

		action = new ContextMenuAction();
		action.label = getResources().getString(R.string.send);
		action.ocl = new OnClickListener() {

			@Override
			public void onClick(View v) {
				mam.cancel();
				new SharePopup(HomeActivity.this, informaCam.mediaManifest.getById(media._id), true);
			}

		};
		actions.add(action);

		mam = new MediaActionMenu(this, actions);
		mam.Show();
	}

	@Override
	public void onBackPressed() {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	@Override
	public void logoutUser() {
		getIntent().putExtra(Codes.Extras.LOGOUT_USER, true);
		setResult(Activity.RESULT_CANCELED);
		finish();
	}

	@Override
	public void onActivityResult(int requestCode, int responseCode, Intent data) {
		if(responseCode == Activity.RESULT_OK) {
			informaCam.setStatusListener(this);
			
			switch(requestCode) {
			case Codes.Routes.CAMERA:
				viewPager.setCurrentItem(1);
				
				informaCam.mediaManifest.sortBy(Models.IMediaManifest.Sort.DATE_DESC);
				Log.d(LOG, "new dcim:\n" + data.getStringExtra(Codes.Extras.RETURNED_MEDIA));
				try {
					JSONArray newMedia = (JSONArray) new JSONTokener(data.getStringExtra(Codes.Extras.RETURNED_MEDIA)).nextValue();
					((GalleryFragment) galleryFragment).updateData(newMedia);
				} catch (JSONException e) {
					Log.e(LOG, e.toString());
					e.printStackTrace();
				}
				
				informaCam.stopInforma();
				route = null;
				break;
			case Codes.Routes.LOGOUT:
				logoutUser();
				break;
			case Codes.Routes.EDITOR:
				informaCam.stopInforma();
				route = null;
				break;
			case Codes.Routes.WIPE:
				logoutUser();
				break;
			}
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
		public void onPageScrollStateChanged(int state) {}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {}

		@Override
		public void onPageSelected(int page) {
			tabHost.setCurrentTab(page);
			if(page == 2) {
				launchCamera();
			} else {
				updateAdapter(0);
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
	public void onInformaCamStart(Intent intent) {}

	@Override
	public void onInformaCamStop(Intent intent) {}

	@Override
	public void onInformaStop(Intent intent) {
		route = null;
	}

	@Override
	public void onInformaStart(Intent intent) {
		waiter.cancel();
		if(route != null) {
			if(route.equals(toEditor)) {
				startActivityForResult(toEditor, Routes.EDITOR);
			} else if(route.equals(toCamera)) {
				startActivityForResult(toCamera, Routes.CAMERA);
			}
		}
	}

	@Override
	public void waiter(boolean show) {
		if(show) {
			waiter = new WaitPopup(this);
		} else {
			if(waiter != null) {
				waiter.cancel();
			}
		}
	}

	@Override
	public void updateData(INotification notification, Message message) {}

	@Override
	public void updateData(IOrganization organization, Message message) {}
	
	private final static String HOCKEY_APP_ID = "819d2172183272c9d84cd3a4dbd9296b";
	
	 private void checkForCrashes() {
	   CrashManager.register(this, HOCKEY_APP_ID);
	 }

	 private void checkForUpdates() {
	   // XXX: Remove this for store builds!
	   UpdateManager.register(this, HOCKEY_APP_ID);
	 }

	@Override
	public void updateAdapter(int which) {
		if(informaCam.getCredentialManagerStatus() == org.witness.informacam.utils.Constants.Codes.Status.UNLOCKED) {
			Fragment f= fragments.get(viewPager.getCurrentItem());

			if (f instanceof ListAdapterListener)
			{
				((ListAdapterListener)f).updateAdapter(which);
			}
		}
	}

	@Override
	public void setLocale(String newLocale) {
		lastLocale = newLocale;
	}

	@Override
	public String getLocale() {
		return lastLocale;
	}

	@Override
	public void onUpdate(Message message) {
		int code = message.getData().getInt(Codes.Extras.MESSAGE_CODE);
		
		switch(code) {
		case org.witness.informacam.utils.Constants.Codes.Messages.DCIM.STOP:
			mHandlerUI.sendEmptyMessage(0);
			break;
		}
	}
	
	private Handler mHandlerUI = new Handler ()
	{

		@Override
		public void handleMessage(Message msg) {
			
			super.handleMessage(msg);
			
			updateAdapter(msg.what);
		}
		
	};

}
