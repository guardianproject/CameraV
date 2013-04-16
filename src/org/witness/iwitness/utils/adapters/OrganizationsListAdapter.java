package org.witness.iwitness.utils.adapters;

import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.IOrganization;
import org.witness.informacam.utils.Constants.App;
import org.witness.iwitness.R;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class OrganizationsListAdapter extends BaseAdapter {
	List<IOrganization> organizations;
	
	public OrganizationsListAdapter(List<IOrganization> organizations) {
		this.organizations = organizations;
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
		convertView = LayoutInflater.from(InformaCam.getInstance().a).inflate(R.layout.adapter_organization_list_item, null);		
		TextView name = (TextView) convertView.findViewById(R.id.organization_name);
		name.setText(organizations.get(position).organizationName);
		
		TextView details = (TextView) convertView.findViewById(R.id.organization_details);
		details.setText(organizations.get(position).organizationDetails);
		
		TextView isActive = (TextView) convertView.findViewById(R.id.organization_is_active);
		int isActiveText = organizations.get(position).transportCredentials.certificatePath == null ? R.string.unverified : R.string.verified; 
		isActive.setText(InformaCam.getInstance().a.getString(isActiveText));
		
		return convertView;
	}

}
