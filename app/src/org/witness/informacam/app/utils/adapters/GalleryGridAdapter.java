package org.witness.informacam.app.utils.adapters;

import info.guardianproject.odkparser.utils.QD;

import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.app.R;
import org.witness.informacam.app.utils.Constants.App.Editor.Forms;
import org.witness.informacam.app.views.RoundedImageView;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.media.IMedia;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class GalleryGridAdapter extends BaseAdapter {
	
	private class MediaInfo
	{
		public boolean hasTags;
		public boolean hasAudio;
		public boolean hasNotes;
		
		public MediaInfo(boolean hasTags, boolean hasAudio, boolean hasNotes)
		{
			this.hasTags = hasTags;
			this.hasAudio = hasAudio;
			this.hasNotes = hasNotes;
		}
	};
	
	private List<? super IMedia> media;
	private List<MediaInfo> mediaInfo;
	private MediaInfo mediaInfoDefault = new MediaInfo(false, false, false);
	
	LayoutInflater li;
	Activity a;
	boolean mInSelectionMode;
	private int mNumLoading;

	
	private AsyncTask<List<? super IMedia>, Void, Void> mUpdateInfoTask;

	//private final static String LOG = App.LOG;

	public GalleryGridAdapter(Activity a, List<? super IMedia> media)
			throws NullPointerException {
		this.a = a;
		li = LayoutInflater.from(a);
		setMediaAndUpdateInfo(media);
	}

	public void setNumLoading(int loading)
	{
		mNumLoading = loading;
		notifyDataSetChanged();
	}
	
	public void update(List<IMedia> newMedia) {
		setMediaAndUpdateInfo(newMedia);
		notifyDataSetChanged();
	}

	@SuppressWarnings("unchecked")
	private void setMediaAndUpdateInfo(List<? super IMedia> newMedia)
	{
		synchronized(this)
		{
			this.media = newMedia;
			this.mediaInfo = null;
		
			if (mUpdateInfoTask != null)
				mUpdateInfoTask.cancel(false);
		}
		
		mUpdateInfoTask = new AsyncTask<List<? super IMedia>, Void, Void>()
		{
			private List<? super IMedia> mediaToProcess = null;
			private List<MediaInfo> mediaInfoProcessed = null;
			
			@Override
			protected Void doInBackground(List<? super IMedia>... params) {

				mediaToProcess = params[0];
				if (mediaToProcess == null || mediaToProcess.size() == 0)
					return null;

				mediaInfoProcessed = new ArrayList<MediaInfo>(mediaToProcess.size());

				for (int i = 0; i < mediaToProcess.size(); i++) {
					if (isCancelled())
						break;

					IMedia m = (IMedia) mediaToProcess.get(i);

					boolean hasAudio = false;
					boolean hasNotes = false;
					boolean hasTags = false;
					try {
						for (IForm form : m.getForms(a)) {
							if (form.namespace.equals(Forms.FreeAudio.TAG)) {
								hasAudio = true;
							} else if (form.namespace.equals(Forms.FreeText.TAG)) {
								QD question = form
										.getQuestionDefByTitleId(Forms.FreeText.PROMPT);
								if (question != null
										&& question.hasInitialValue
										&& !TextUtils
												.isEmpty(question.initialValue))
									hasNotes = true;
							}
						}
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					hasTags = (m.getInnerLevelRegions().size() > 0);

					mediaInfoProcessed.add(new MediaInfo(hasTags, hasAudio,
							hasNotes));
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				synchronized(GalleryGridAdapter.this)
				{
					if (mediaToProcess != null && mediaToProcess == media)
						mediaInfo = mediaInfoProcessed;
					mUpdateInfoTask = null;
				}
				notifyDataSetChanged();
			}
		};
		mUpdateInfoTask.execute(media);
	}
	
	public void setInSelectionMode(boolean inSelectionMode) {
		mInSelectionMode = inSelectionMode;
	}

	@Override
	public int getCount() {
		int ret = 0;
		if (media != null)
			ret = media.size();
		ret += mNumLoading;
		return ret;
	}

	@Override
	public Object getItem(int position) {
		if (position < mNumLoading)
			return null;
		return media.get(position - mNumLoading);
	}

	@Override
	public long getItemId(int position) {
		Object item = getItem(position);
		if (item != null)
			return item.hashCode();
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		View view = null;
		
		if (position < mNumLoading)
		{	
			if (convertView != null && !(((ViewGroup)convertView).getChildAt(0) instanceof RoundedImageView))
				view = convertView;
			else
				view = li.inflate(R.layout.adapter_gallery_grid_placeholder, parent, false);
			ImageView encryptionView = (ImageView) view.findViewById(R.id.flPlaceholder); 
			AnimationDrawable encryptionAnimation = (AnimationDrawable) encryptionView.getDrawable(); 
			encryptionAnimation.start();
		}
		else
		{
			IMedia m = (IMedia) media.get(position - mNumLoading);
			MediaInfo info = mediaInfoDefault;
			
			// Done getting the info?
			synchronized(this)
			{
				if (mediaInfo != null)
					info = mediaInfo.get(position - mNumLoading);
			}
			
			if (convertView != null && (((ViewGroup)convertView).getChildAt(0) instanceof RoundedImageView))
				view = convertView;
			else
				view = li.inflate(R.layout.adapter_gallery_grid, parent, false);

			// Show or hide the selection layer
			view.findViewById(R.id.chkSelect).setVisibility(
				mInSelectionMode ? View.VISIBLE : View.GONE);

			ImageView iv = (ImageView) view.findViewById(R.id.gallery_thumb);

			view.findViewById(R.id.new_media_overlay).setVisibility(m.isNew ? View.VISIBLE : View.GONE);

			try {
				Bitmap bitmap = m.getThumbnail();
				iv.setImageBitmap(bitmap);
			} catch (NullPointerException e) {
				iv.setImageDrawable(a.getResources().getDrawable(
					R.drawable.ic_action_video));
			}
			
			view.findViewById(R.id.llSymbols).setVisibility((info.hasAudio || info.hasNotes || info.hasTags) ? View.VISIBLE : View.GONE);
			view.findViewById(R.id.ivAudioNote).setVisibility(info.hasAudio ? View.VISIBLE : View.GONE);
			view.findViewById(R.id.ivNote).setVisibility(info.hasNotes ? View.VISIBLE : View.GONE);
			view.findViewById(R.id.ivTag).setVisibility(info.hasTags ? View.VISIBLE : View.GONE);
		}
		return view;
	}

}
