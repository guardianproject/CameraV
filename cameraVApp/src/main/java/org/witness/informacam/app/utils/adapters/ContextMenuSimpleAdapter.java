package org.witness.informacam.app.utils.adapters;

import java.util.List;

import org.witness.informacam.app.R;
import org.witness.informacam.app.utils.actions.ContextMenuAction;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ContextMenuSimpleAdapter extends BaseAdapter {
	
	private List<ContextMenuAction> options;
	Activity a;
	
	public ContextMenuSimpleAdapter(List<ContextMenuAction> options, Activity a) {
		this.options = options;
		this.a = a;
	}
	
	@Override
	public int getCount() {
		return options.size();
	}

	@Override
	public Object getItem(int position) {
		return options.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View layout = LayoutInflater.from(a).inflate(R.layout.adapter_context_menu_simple, null);
		
		TextView label = (TextView) layout.findViewById(R.id.context_menu_item_label);
		label.setText(options.get(position).label);
		
		label.setOnClickListener(options.get(position).ocl);
		
		return layout;
	}

}
