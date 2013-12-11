package org.witness.informacam.app.utils.adapters;

import java.util.List;

import org.witness.informacam.app.R;
import org.witness.informacam.models.forms.IForm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TagFormSpinnerAdapter extends BaseAdapter {
	private List<IForm> forms;
	
	public TagFormSpinnerAdapter(List<IForm> forms) {
		this.forms = forms;
	}
	
	@Override
	public int getCount() {
		return forms.size();
	}

	@Override
	public Object getItem(int position) {
		return forms.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = LayoutInflater.from(parent.getContext().getApplicationContext()).inflate(R.layout.adapter_tag_form_spinner_list_item, null);
		
		TextView formNamespace = (TextView) convertView.findViewById(R.id.form_namespace);
		formNamespace.setText(forms.get(position).namespace);
		
		return convertView;
	}

}
