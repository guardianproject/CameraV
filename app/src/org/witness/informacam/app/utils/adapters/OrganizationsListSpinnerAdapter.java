package org.witness.informacam.app.utils.adapters;

import java.util.List;

import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.app.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class OrganizationsListSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {
	
	private List<IOrganization> organizations;
	
	public OrganizationsListSpinnerAdapter(List<IOrganization> organizations) {
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
		
		convertView = LayoutInflater.from(parent.getContext().getApplicationContext()).inflate(R.layout.adapter_organization_spinner_list_item, null);
		TextView organizationName = (TextView) convertView.findViewById(R.id.organization_name);
		organizationName.setText(organizations.get(position).organizationName);
		
		return convertView;
	}

}
