package org.witness.iwitness.utils.adapters;

import java.util.List;

import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.utils.Constants.App;
import org.witness.iwitness.R;
import org.witness.iwitness.app.views.RoundedImageView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomePhotoAdapter extends PagerAdapter {
	List<? super IMedia> media;
	LayoutInflater li;
	Activity a;

	private final static String LOG = App.LOG;

	public HomePhotoAdapter(Activity a, List<? super IMedia> media)
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

	@Override
	public int getCount() {
		if (media != null)
			return media.size();
		else
			return 0;
	}

	public Object getObjectFromIndex(int position) {
		if (position >= 0 && position < getCount())
			return media.get(position);
		return null;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return (view == object);
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		destroyItem((ViewGroup) container, position, object);
	}

	@Override
	public Object instantiateItem(View container, int position) {
		return instantiateItem((ViewGroup) container, position);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		IMedia m = (IMedia) media.get(position);
		RoundedImageView view = (RoundedImageView) li.inflate(
				R.layout.adapter_home_photo, container, false);
		try {
			Bitmap bitmap = m.getThumbnail();
			view.setImageBitmap(bitmap);
		} catch (NullPointerException e) {
			view.setImageDrawable(a.getResources().getDrawable(
					R.drawable.ic_action_video));
		}

		view.setTag(m);
		container.addView(view);
		return view;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		View view = (View) object;
		container.removeView(view);
	}


	@Override
	public int getItemPosition(Object object) {
		IMedia m = (IMedia) ((View) object).getTag();
		for (int i = 0; i < media.size(); i++) {
			if (media.get(i) == m)
				return i;
		}
		return POSITION_NONE;
	}

}
