package org.witness.iwitness.utils.adapters;

import java.util.List;

import org.json.JSONException;
import org.witness.informacam.models.IMedia;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Models;
import org.witness.iwitness.R;

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
	List<IMedia> media;
	LayoutInflater li;
	Activity a;
	
	private final static String LOG = App.LOG;
	
	public GalleryListAdapter(Activity a, List<IMedia> media) throws NullPointerException {
		this.media = media;
		this.a = a;
		li = LayoutInflater.from(a);
	}
	
	@Override
	public int getCount() {
		return media.size();
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
		View view = li.inflate(R.layout.adapter_gallery_list, null);
		
		ImageView iv = (ImageView) view.findViewById(R.id.gallery_list);
		
		Bitmap bitmap = media.get(position).getBitmap(media.get(position).bitmapPreview);
		iv.setImageBitmap(bitmap);
		
		TextView tv = (TextView) view.findViewById(R.id.gallery_details);
		tv.setText(media.get(position).renderDetailsAsText(1));
		
		try {
			if(!media.get(position).getBoolean(Models.IMediaManifest.Sort.IS_SHOWING)) {
				view.setVisibility(View.GONE);
			}
		} catch (JSONException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
		
		return view;
	}

}
