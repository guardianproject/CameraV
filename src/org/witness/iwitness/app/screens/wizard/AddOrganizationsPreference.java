package org.witness.iwitness.app.screens.wizard;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.witness.informacam.InformaCam;
import org.witness.informacam.ui.adapters.OrganizationAdapter;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.WizardListener;
import org.witness.informacam.models.IConnection;
import org.witness.informacam.models.IOrganization;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class AddOrganizationsPreference extends Fragment implements WizardListener {
	View rootView;
	Activity a;
	
	private InformaCam informaCam = InformaCam.getInstance();
	
	ListView organizationsHolder;
	OrganizationAdapter organizationAdapter;
	
	private final static String LOG = App.Wizard.LOG;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);
		
		rootView = li.inflate(R.layout.fragment_wizard_step_five, null);
		
		organizationsHolder = (ListView) rootView.findViewById(R.id.organizations_list_view);
		
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
		initData();
	}
	
	private void initData() {
		try {
			if(a.getAssets().list("includedOrganizations").length > 0) {
				List<IOrganization> includedOrganizations = new Vector<IOrganization>();
				for(String organizationManifest : a.getAssets().list("includedOrganizations")) {
					IOrganization organization = new IOrganization();
					organization.inflate((JSONObject) new JSONTokener(
							new String(
									informaCam.ioService.getBytes(
											("includedOrganizations/" + organizationManifest), 
											Type.APPLICATION_ASSET)
									)
					).nextValue());
					includedOrganizations.add(organization);
				}
				
				organizationAdapter = new OrganizationAdapter(includedOrganizations, a);
				organizationsHolder.setAdapter(organizationAdapter);
			}
		} catch(IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (JSONException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
	}
	
	@Override
	public void onSubFragmentCompleted() {
		for(int i=0; i<organizationAdapter.getCount(); i++) {
			try {
				IOrganization organization = (IOrganization) organizationAdapter.getItem(i);
								
				if(organization.has("isSelected") && organization.getBoolean("isSelected")) {
					IConnection connection = new IConnection();
					connection.url = organization.requestUrl;
					
					informaCam.uploaderService.addToQueue(connection);
				}
			} catch (JSONException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public FragmentManager returnFragmentManager() {
		return null;
	}

	@Override
	public void wizardCompleted() {}
}
