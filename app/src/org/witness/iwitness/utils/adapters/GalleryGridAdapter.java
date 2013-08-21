package org.witness.iwitness.utils.adapters;

import java.util.List;

import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.utils.Constants.App;
import org.witness.iwitness.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class GalleryGridAdapter extends BaseAdapter {
	
	private List<? super IMedia> media;
	LayoutInflater li;
	Activity a;
	boolean mInSelectionMode;

	private final static String LOG = App.LOG;

	public GalleryGridAdapter(Activity a, List<? super IMedia> media)
			throws NullPointerException {
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

	public void setInSelectionMode(boolean inSelectionMode) {
		mInSelectionMode = inSelectionMode;
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

	@SuppressWarnings("deprecation")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		IMedia m = (IMedia) media.get(position);
		View view = li.inflate(R.layout.adapter_gallery_grid, null);

		// Show or hide the selection layer
		view.findViewById(R.id.chkSelect).setVisibility(
				mInSelectionMode ? View.VISIBLE : View.GONE);

		ImageView iv = (ImageView) view.findViewById(R.id.gallery_thumb);

		View iv_holder = view.findViewById(R.id.gallery_thumb_holder);
		if (m.isNew) {
			iv_holder.setBackgroundDrawable(a.getResources().getDrawable(
					R.drawable.extras_is_new_background));
		}

		try {
			Bitmap bitmap = m.getThumbnail();
			iv.setImageBitmap(bitmap);
		} catch (NullPointerException e) {
			iv.setImageDrawable(a.getResources().getDrawable(
					R.drawable.ic_action_video));
		}

		return view;
	}

}
