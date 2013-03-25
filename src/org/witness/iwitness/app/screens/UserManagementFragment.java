package org.witness.iwitness.app.screens;

import org.witness.informacam.InformaCam;
import org.witness.informacam.utils.models.IUser;
import org.witness.informacam.utils.models.Model;
import org.witness.iwitness.R;
import org.witness.iwitness.app.WipeActivity;
import org.witness.iwitness.utils.Constants.App;
import org.witness.iwitness.utils.Constants.Codes;
import org.witness.iwitness.utils.Constants.Codes.Routes;
import org.witness.iwitness.utils.Constants.MainFragmentListener;
import org.witness.iwitness.utils.Constants.App.Home.Tabs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

public class UserManagementFragment extends Fragment implements OnClickListener {
	View rootView;
	TabHost tabHost;
	
	ImageButton emergencyWipe, toSettings, thumbnail;
	
	TextView alias, connectivity;
	ListView notificationsHolder, organizationsHolder;
	
	LayoutInflater li;
	Activity a;
	
	int[] dims;
	
	InformaCam informaCam = InformaCam.getInstance();
	IUser user = informaCam.user;
	
	private static final String LOG = App.Home.LOG;
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);
		this.li = li;
		
		rootView = li.inflate(R.layout.fragment_home_user_management, null);
		tabHost = (TabHost) rootView.findViewById(android.R.id.tabhost);
		
		emergencyWipe = (ImageButton) rootView.findViewById(R.id.emergency_wipe);
		emergencyWipe.setOnClickListener(this);
		
		toSettings = (ImageButton) rootView.findViewById(R.id.to_settings);
		toSettings.setOnClickListener(this);
		
		thumbnail = (ImageButton) rootView.findViewById(R.id.user_thumbnail);
		thumbnail.setOnClickListener(this);
		
		alias = (TextView) rootView.findViewById(R.id.user_alias);
		connectivity = (TextView) rootView.findViewById(R.id.user_connectivity);
		
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
		initLayout(savedInstanceState);
		initData();
	}
	
	private void initLayout(Bundle savedInstanceState) {
		View v = null;
		
		tabHost.setLayoutParams(new LinearLayout.LayoutParams(dims[0], dims[1]));
		tabHost.setup();
		
		TabHost.TabSpec tab = tabHost.newTabSpec(Tabs.CameraChooser.TAG).setIndicator(generateTab(li, R.layout.user_management_fragment_tab, getResources().getString(R.string.notifications)));
		v = li.inflate(R.layout.fragment_user_management_notifications, tabHost.getTabContentView(), true);
		tab.setContent(R.id.notification_list_root);
		tabHost.addTab(tab);
		
		notificationsHolder = (ListView) v.findViewById(R.id.notifications_list_view);
		
		tab = tabHost.newTabSpec(Tabs.CameraChooser.TAG).setIndicator(generateTab(li, R.layout.user_management_fragment_tab, getResources().getString(R.string.organizations)));
		v = li.inflate(R.layout.fragment_user_management_organizations, tabHost.getTabContentView(), true);
		tab.setContent(R.id.organization_list_root);
		tabHost.addTab(tab);
		
		organizationsHolder = (ListView) v.findViewById(R.id.organizations_list_view);
		
		tabHost.setCurrentTab(0);
		
	}
	
	private void initData() {
		alias.setText(user.alias);
		
	}
	
	private static View generateTab(final LayoutInflater li, final int layout, final String labelText) {
		View tab = li.inflate(layout, null);
		TextView label = (TextView) tab.findViewById(R.id.tab_label);
		label.setText(labelText);
		return tab;
	}

	@Override
	public void onClick(View v) {
		if(v == emergencyWipe) {
			Intent wipeIntent = new Intent(a, WipeActivity.class);
			startActivityForResult(wipeIntent, Routes.WIPE);
		} else if(v == toSettings) {
			
		} else if(v == thumbnail) {
			
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == Activity.RESULT_OK) {
			switch(requestCode) {
			case Codes.Routes.WIPE:
				((MainFragmentListener) a).logoutUser();
				break;
			}
		}
	}
}
