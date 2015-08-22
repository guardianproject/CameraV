package org.witness.informacam.app.utils.adapters;

import java.util.List;

import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.app.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class OrganizationsListAdapter extends BaseAdapter {
	private List<IOrganization> organizations;
	
	public OrganizationsListAdapter(List<IOrganization> organizations) {
		this.organizations = organizations;
	}
	
	public void update(List<IOrganization> newOrganizations) {
		organizations = newOrganizations;
		notifyDataSetChanged();
	}
	
	public void update(IOrganization newOrganization) {		
		organizations.add(newOrganization);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return organizations.size();
	}

	@Override
	public Object getItem(int position) {
		return organizations.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Context context = parent.getContext().getApplicationContext();
		
		final IOrganization organization = organizations.get(position);
		convertView = LayoutInflater.from(context).inflate(R.layout.adapter_organization_list_item, null);		
		TextView name = (TextView) convertView.findViewById(R.id.organization_name);
		name.setText(organization.organizationName);
		
		TextView details = (TextView) convertView.findViewById(R.id.organization_details);
		details.setText(organization.organizationDetails);		
		
		
		return convertView;
	}

}
