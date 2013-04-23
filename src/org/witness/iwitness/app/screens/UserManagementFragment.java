package org.witness.iwitness.app.screens;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.organizations.IInstalledOrganizations;
import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.models.credentials.IUser;
import org.witness.informacam.models.media.IMedia;
import org.witness.iwitness.R;
import org.witness.iwitness.app.HomeActivity;
import org.witness.iwitness.app.WipeActivity;
import org.witness.iwitness.utils.Constants.App;
import org.witness.iwitness.utils.Constants.Codes;
import org.witness.iwitness.utils.Constants.Codes.Routes;
import org.witness.iwitness.utils.Constants.App.Home.Tabs;
import org.witness.iwitness.utils.Constants.HomeActivityListener;
import org.witness.iwitness.utils.adapters.NotificationsListAdapter;
import org.witness.iwitness.utils.adapters.OrganizationsListAdapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

public class UserManagementFragment extends Fragment implements OnClickListener, OnItemLongClickListener {
	View rootView;
	TabHost tabHost;
	
	ImageButton emergencyWipe, toSettings, thumbnail;
	Button exportCredentials;
	
	TextView alias, connectivity, notificationsNoNotifications;
	ListView notificationsHolder, organizationsHolder;
	
	LayoutInflater li;
	Activity a;
	
	int[] dims;
	
	InformaCam informaCam = InformaCam.getInstance();
	IUser user = informaCam.user;
	IInstalledOrganizations installedOrganizations;
	
	@SuppressWarnings("unused")
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
		
		dims = ((HomeActivityListener) a).getDimensions();
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
		notificationsNoNotifications = (TextView) v.findViewById(R.id.notification_no_notifications);
		
		tab = tabHost.newTabSpec(Tabs.CameraChooser.TAG).setIndicator(generateTab(li, R.layout.user_management_fragment_tab, getResources().getString(R.string.organizations)));
		v = li.inflate(R.layout.fragment_user_management_organizations, tabHost.getTabContentView(), true);
		tab.setContent(R.id.organization_list_root);
		tabHost.addTab(tab);
		
		organizationsHolder = (ListView) v.findViewById(R.id.organizations_list_view);
		exportCredentials = (Button) v.findViewById(R.id.organizations_export_key);
		exportCredentials.setOnClickListener(this);
		
		tabHost.setCurrentTab(0);
		
	}
	
	private void initData() {
		alias.setText(user.alias);
		
		int connectivityLabel = informaCam.uploaderService.isConnectedToTor() ? R.string.connected_to_tor : R.string.not_connected_to_tor;
		connectivity.setText(getResources().getString(connectivityLabel));
		
		installedOrganizations = new IInstalledOrganizations();
		installedOrganizations.inflate(informaCam.getModel(installedOrganizations));
		Log.d(LOG, "installed orgs\n" + installedOrganizations.asJson().toString());
		
		organizationsHolder.setOnItemLongClickListener(this);
		organizationsHolder.setAdapter(new OrganizationsListAdapter(installedOrganizations.organizations));
		
		if(informaCam.notificationsManifest.notifications != null && informaCam.notificationsManifest.notifications.size() > 0) {
			notificationsNoNotifications.setVisibility(View.VISIBLE);
			notificationsHolder.setAdapter(new NotificationsListAdapter(informaCam.notificationsManifest.notifications));
		}
		
	}
	
	private void exportCredentials() {
		java.io.File credentials = informaCam.ioService.getPublicCredentials();
		Intent intent = new Intent()
			.setAction(Intent.ACTION_SEND)
			.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(credentials))
			.setType("file/");
		
		startActivity(Intent.createChooser(intent, getString(R.string.send)));
		
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
			a.startActivityForResult(wipeIntent, Routes.WIPE);
		} else if(v == toSettings) {
			// TODO
		} else if(v == thumbnail) {
			// TODO
		} else if(v == exportCredentials) {
			exportCredentials();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int viewId, long l) {
		IOrganization org = installedOrganizations.organizations.get((int) l);
		if(org.transportCredentials.certificatePath != null) {
			((HomeActivityListener) a).getContextualMenuFor(org);
		}
		return true;
	}
}
