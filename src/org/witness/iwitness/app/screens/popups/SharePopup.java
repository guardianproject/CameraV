package org.witness.iwitness.app.screens.popups;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.IInstalledOrganizations;
import org.witness.informacam.models.media.IMedia;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App.Home.Tabs;
import org.witness.iwitness.utils.adapters.OrganizationsListAdapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

public class SharePopup extends Popup implements OnClickListener, OnCancelListener, OnDismissListener {
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

	public SharePopup(Activity a, final Object context) {
		this(a, context, false);
	}

	public SharePopup(Activity a, final Object context, boolean startsInforma) {
		super(a, R.layout.popup_share);
		this.context = context;

		informaCam = InformaCam.getInstance();

		tabHost = (TabHost) layout.findViewById(android.R.id.tabhost);
		li = LayoutInflater.from(a);
		initLayout();

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
		if(installedOrganizations.organizations != null && installedOrganizations.organizations.size() > 0) {
			encryptHolder.setVisibility(View.VISIBLE);

			noOrganizations.setVisibility(View.GONE);
			organizationList.setVisibility(View.VISIBLE);
			organizationList.setAdapter(new OrganizationsListAdapter(installedOrganizations.organizations));
		}
	}

	private static View generateTab(final LayoutInflater li, final int layout, final String labelText) {
		View tab = li.inflate(layout, null);
		TextView label = (TextView) tab.findViewById(R.id.tab_label);
		label.setText(labelText);
		return tab;
	}

	@Override
	public void onClick(View v) {
		if(v == encryptToggle) {
			if(((CheckBox) v).isChecked()) {
				encryptList.performClick();
			}
		} else if(v == encryptCommit) {
			((IMedia) context).export();
			// TODO: with params.
		}

	}

	@Override
	public void onDismiss(DialogInterface dialog) {

	}

	@Override
	public void onCancel(DialogInterface dialog) {

	}

}
