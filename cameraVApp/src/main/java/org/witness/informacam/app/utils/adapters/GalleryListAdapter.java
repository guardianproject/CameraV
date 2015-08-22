package org.witness.informacam.app.utils.adapters;

import java.util.List;

import org.witness.informacam.app.R;
import org.witness.informacam.json.JSONException;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Models;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GalleryListAdapter extends BaseAdapter {
	
	private List<? super IMedia> media;
	LayoutInflater li;
	Activity a;
	
	private final static String LOG = App.LOG;
	
	public GalleryListAdapter(Activity a, List<? super IMedia> media) throws NullPointerException {
		this.media = media;
		this.a = a;
		li = LayoutInflater.from(a);
	}
	
	public void update(List<IMedia> newMedia) {
		media = newMedia;
		notifyDataSetChanged();
	}
	
	public void update(IMedia newMedia) {
		media.add(newMedia);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if (media != null)
			return media.size();
		else
			return 0;
	}

	@Override
	public Object getItem(int position) {
		return media.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		IMedia m = (IMedia) media.get(position);
		
		if (convertView == null)
			convertView = li.inflate(R.layout.adapter_gallery_list, null);
		
		ImageView iv = (ImageView) convertView.findViewById(R.id.gallery_list);
		
		Bitmap bitmap = m.getBitmap(m.dcimEntry.thumbnail);
		iv.setImageBitmap(bitmap);
		
		TextView tv = (TextView) convertView.findViewById(R.id.gallery_details);
		tv.setText(m.renderDetailsAsText(1));
		
		try {
			if(!m.getBoolean(Models.IMediaManifest.Sort.IS_SHOWING)) {
				convertView.setVisibility(View.GONE);
			}
		} catch (JSONException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
		
		return convertView;
	}

}