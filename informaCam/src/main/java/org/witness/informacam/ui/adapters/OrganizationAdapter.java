package org.witness.informacam.ui.adapters;

import java.util.List;

import org.witness.informacam.R;
import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.utils.Constants.App;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class OrganizationAdapter extends BaseAdapter {
	List<IOrganization> organizations;
	Activity a;
	int viewResource = R.layout.list_organization;
	
	private final static String LOG = App.LOG;
	
	public OrganizationAdapter(List<IOrganization> organizations, Activity a) {
		this.organizations = organizations;
		this.a = a;
	}
	
	public OrganizationAdapter(List<IOrganization> organizations, Activity a, int viewResource) {
		this.organizations = organizations;
		this.a = a;
		this.viewResource = viewResource;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = LayoutInflater.from(a).inflate(viewResource, null);
		
		TextView tv = (TextView) view.findViewById(R.id.organization_details);
		StringBuffer txt = new StringBuffer();
		
		txt.append(organizations.get(position).organizationName);
		txt.append(System.getProperty("line.separator"));
		txt.append(organizations.get(position).organizationDetails);
		
		tv.setText(txt.toString());
		
		final CheckBox cb = (CheckBox) view.findViewById(R.id.organization_select);
		cb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(((CheckBox) v).isChecked()) {
					
						organizations.get(position).put("isSelected", ((CheckBox) v).isChecked());
					
				}
				
			}
			
		});
		return view;
	}

}
