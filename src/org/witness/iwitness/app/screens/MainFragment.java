package org.witness.iwitness.app.screens;

import org.witness.informacam.InformaCam;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App.Home.Tabs;
import org.witness.iwitness.utils.Constants.HomeActivityListener;
import org.witness.iwitness.utils.Constants.MainFragmentListener;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost;

public class MainFragment extends Fragment implements TabHost.OnTabChangeListener, HomeActivityListener {
	View rootView;
	Fragment userManagementFragment, galleryFragment;
	FrameLayout mainFragmentRoot;
	
	boolean cameraChooserShouldShow = false;
	int visibility = View.VISIBLE;
	
	TabHost tabHost;
	LayoutInflater li;
	int[] dims = new int[2];
	String lastTab = null;
	
	Activity a;
	FragmentManager fm;
	
	InformaCam informaCam;
		
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);
		this.li = li;
		
		rootView = li.inflate(R.layout.fragment_home_main, null);
		tabHost = (TabHost) rootView.findViewById(android.R.id.tabhost);
		mainFragmentRoot = (FrameLayout) rootView.findViewById(R.id.main_fragment_root);
		
		return rootView;
	}
	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
		
		dims = ((MainFragmentListener) a).getDimensions();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.fm = ((MainFragmentListener) a).returnFragmentManager();
		initLayout(savedInstanceState);
		
		informaCam = InformaCam.getInstance();
	}
	
	private void initLayout(Bundle savedInstanceState) {
		tabHost.setLayoutParams(new LinearLayout.LayoutParams(dims[0], dims[1]));
		tabHost.setup();
		
		TabHost.TabSpec tab = tabHost.newTabSpec(Tabs.CameraChooser.TAG).setIndicator(generateTab(li, R.layout.tabs_camera_chooser));
		li.inflate(R.layout.fragment_home_camera_chooser, tabHost.getTabContentView(), true);
		tab.setContent(R.id.camera_chooser_root_view);
		tabHost.addTab(tab);
		
		tab = tabHost.newTabSpec(Tabs.Gallery.TAG).setIndicator(generateTab(li, R.layout.tabs_iwitness));
		li.inflate(R.layout.fragment_home_gallery, tabHost.getTabContentView(), true);
		tab.setContent(R.id.gallery_root_view);
		tabHost.addTab(tab);
		
		tab = tabHost.newTabSpec(Tabs.UserManagement.TAG).setIndicator(generateTab(li, R.layout.tabs_user_management));
		li.inflate(R.layout.fragment_home_user_management, tabHost.getTabContentView(), true);
		tab.setContent(R.id.user_management_root_view);
		tabHost.addTab(tab);
		
		tabHost.setOnTabChangedListener(this);
		
		for(int i=0; i<tabHost.getTabWidget().getChildCount(); i++) {
			View tab_ = tabHost.getTabWidget().getChildAt(i);
			if(i == 1) {
				tab_.setLayoutParams(new LinearLayout.LayoutParams((int) (dims[0] * 0.5), LayoutParams.MATCH_PARENT));
			} else {
				tab_.setLayoutParams(new LinearLayout.LayoutParams((int) (dims[0] * 0.25), LayoutParams.MATCH_PARENT));
			}
		}
		
		initFragments();
		
		tabHost.setCurrentTab(1);
	}
	
	private void initFragments() {
		userManagementFragment = new UserManagementFragment();
		galleryFragment = new GalleryFragment();
		
		fm.beginTransaction().add(R.id.main_fragment_root, userManagementFragment).commit();
	}
	
	private static View generateTab(final LayoutInflater li, final int resource) {
		return li.inflate(resource, null);
	}

	
	@Override
	public void onTabChanged(String tabId) {
		cameraChooserShouldShow = false;
		visibility = View.VISIBLE;
		
		Fragment fragment = null;
		
		if(tabId.equals(Tabs.CameraChooser.TAG)) {
			cameraChooserShouldShow = true;
			visibility = View.GONE;
			if(lastTab != Tabs.Gallery.TAG) {
				fragment = galleryFragment;
			}
			
		} else if(tabId.equals(Tabs.Gallery.TAG)) {
			if(lastTab != Tabs.CameraChooser.TAG) {
				fragment = galleryFragment;
			}
		} else if(tabId.equals(Tabs.UserManagement.TAG)) {
			fragment = userManagementFragment;
		}
		
		if(fragment != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.main_fragment_root, fragment);
			ft.addToBackStack(null);
			ft.commit();
		}
		
		((MainFragmentListener) a).toggleCameraChooser(cameraChooserShouldShow);
		lastTab = tabId;
	}

	@Override
	public void toggleCameraChooser() {
		tabHost.getTabWidget().getChildAt(0).setVisibility(visibility);
	}
	
	@Override
	public void updateGalleryData() {
		((GalleryFragment) galleryFragment).initData();
	}
	
}
