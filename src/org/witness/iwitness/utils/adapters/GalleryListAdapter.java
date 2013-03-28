package org.witness.iwitness.utils.adapters;

import java.util.List;

import org.witness.informacam.utils.models.IMedia;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.MainFragmentListener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GalleryListAdapter extends BaseAdapter {
	List<IMedia> media;
	LayoutInflater li;
	Activity a;
		
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
		
		Bitmap bitmap = media.get(position).getBitmap(media.get(position).bitmapList);
		iv.setImageBitmap(bitmap);
		
		iv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainFragmentListener) a).launchEditor(media.get(position)._id);
			}
			
		});
		
		TextView tv = (TextView) view.findViewById(R.id.gallery_details);
		tv.setText(media.get(position).detailsAsText);
		
		return view;
	}

}
