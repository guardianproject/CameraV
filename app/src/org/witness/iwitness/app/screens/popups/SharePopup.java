package org.witness.iwitness.app.screens.popups;

import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.organizations.IInstalledOrganizations;
import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.Models;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App.Home.Tabs;
import org.witness.iwitness.utils.adapters.OrganizationsListAdapter;
import org.witness.iwitness.utils.adapters.OrganizationsListSpinnerAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

public class SharePopup extends Popup implements OnClickListener, OnCancelListener, OnDismissListener, OnItemClickListener {
	TabHost tabHost;
	LayoutInflater li;
	Object context;

	InformaCam informaCam;

	TextView noOrganizations, warning;
	ListView organizationList;
	RelativeLayout encryptHolder;
	CheckBox encryptToggle;
	Spinner encryptList;
	Button encryptCommit;
	ProgressBar inProgressBar;
	LinearLayout inProgressRoot;

	List<IOrganization> organizations;
	IOrganization encryptTo = null;
	boolean isShare = false;
	
	Handler h;

	public SharePopup(Activity a, final Object context) {
		this(a, context, false);
	}

	@SuppressLint("HandlerLeak")
	public SharePopup(final Activity a, final Object context, boolean startsInforma) {
		super(a, R.layout.popup_share);
		this.context = context;

		informaCam = InformaCam.getInstance();

		tabHost = (TabHost) layout.findViewById(android.R.id.tabhost);
		inProgressRoot = (LinearLayout) layout.findViewById(R.id.share_in_progress_root);
		inProgressBar = (ProgressBar) layout.findViewById(R.id.share_in_progress_bar);
		li = LayoutInflater.from(a);
		initLayout();
		
		h = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Bundle b = msg.getData();
				if(b.containsKey(Models.IMedia.VERSION)) {
					inProgressBar.setProgress(100);
					SharePopup.this.cancel();
					
					if(isShare && b.getString(Models.IMedia.VERSION) != null) {
						Intent intent = new Intent()
							.setAction(Intent.ACTION_SEND)
							.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new java.io.File(b.getString(Models.IMedia.VERSION))))
							.setType("*/*");

						a.startActivity(Intent.createChooser(intent, a.getString(R.string.send)));
					}
				} else if(b.containsKey(Codes.Keys.UI.PROGRESS)) {
					inProgressBar.setProgress(b.getInt(Codes.Keys.UI.PROGRESS));
				}
			}
		};

		Show();

	}

	private void initLayout() {
		View v = null;
		WindowManager.LayoutParams dims = alert.getWindow().getAttributes();

		tabHost.setLayoutParams(new LinearLayout.LayoutParams(dims.width, dims.height));
		tabHost.setup();

		TabHost.TabSpec tab = tabHost.newTabSpec(Tabs.SharePopup.TAG).setIndicator(generateTab(li, R.layout.user_management_fragment_tab, a.getResources().getString(R.string.send_to_org)));
		v = li.inflate(R.layout.popup_share_send_direct, tabHost.getTabContentView(), true);
		tab.setContent(R.id.send_direct_root);
		tabHost.addTab(tab);

		noOrganizations = (TextView) v.findViewById(R.id.send_directly_no_organizations);
		organizationList = (ListView) v.findViewById(R.id.send_directly_organization_list);

		tab = tabHost.newTabSpec(Tabs.SharePopup.TAG).setIndicator(generateTab(li, R.layout.user_management_fragment_tab, a.getResources().getString(R.string.share_via)));
		v = li.inflate(R.layout.popup_share_send_via, tabHost.getTabContentView(), true);
		tab.setContent(R.id.send_via_root);
		tabHost.addTab(tab);

		encryptHolder = (RelativeLayout) v.findViewById(R.id.send_via_encrypt_holder);

		encryptToggle = (CheckBox) v.findViewById(R.id.send_via_encrypt_toggle);
		encryptToggle.setOnClickListener(this);

		encryptList = (Spinner) v.findViewById(R.id.send_via_encrypt_list);

		encryptCommit = (Button) v.findViewById(R.id.send_via_commit);
		encryptCommit.setOnClickListener(this);

		warning = (TextView) v.findViewById(R.id.send_via_warning);
		warning.setText(a.getResources().getString(R.string.sharing_outside_of_informacam, a.getResources().getString(R.string.app_name)));

		initData();

		tabHost.setCurrentTab(0);
	}

	private void initData() {
		informaCam = InformaCam.getInstance();
		IInstalledOrganizations installedOrganizations = (IInstalledOrganizations) informaCam.getModel(new IInstalledOrganizations());
		organizations = new ArrayList<IOrganization>();

		if(installedOrganizations.organizations != null && installedOrganizations.organizations.size() > 0) {
			if(installedOrganizations.organizations != null && installedOrganizations.organizations.size() > 0) {
				for(IOrganization org : installedOrganizations.organizations) {
					organizations.add(org);
				}
			}
		}

		if(organizations.size() > 0) {
			encryptHolder.setVisibility(View.VISIBLE);

			noOrganizations.setVisibility(View.GONE);
			organizationList.setVisibility(View.VISIBLE);
			organizationList.setAdapter(new OrganizationsListAdapter(installedOrganizations.organizations));
			organizationList.setOnItemClickListener(this);

			if(organizations.size() == 1) {
				encryptToggle.setText(a.getString(R.string.send_encrypted_to_x, installedOrganizations.organizations.get(0).organizationName));
			} else {
				encryptList.setAdapter(new OrganizationsListSpinnerAdapter(organizations));
			}
		}
	}

	private static View generateTab(final LayoutInflater li, final int layout, final String labelText) {
		View tab = li.inflate(layout, null);
		TextView label = (TextView) tab.findViewById(R.id.tab_label);
		label.setText(labelText);
		return tab;
	}
	
	private void export(final boolean isShare) {
		tabHost.setVisibility(View.GONE);
		inProgressRoot.setVisibility(View.VISIBLE);
		this.isShare = isShare;
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				((IMedia) context).export(a, h, encryptTo, isShare);
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		if(v == encryptToggle) {
			if(((CheckBox) v).isChecked()) {
				if(organizations.size() > 1) {
					encryptList.performClick();
				} else {
					encryptTo = organizations.get(0);
				}
			}
		} else if(v == encryptCommit) {
			export(true);
		}

	}

	@Override
	public void onDismiss(DialogInterface dialog) {

	}

	@Override
	public void onCancel(DialogInterface dialog) {

	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int viewId, long position) {
		encryptTo = organizations.get((int) position);
		Log.d(LOG, "now exporting to " + encryptTo.organizationName);
		export(false);
	}

}
