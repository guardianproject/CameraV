package org.witness.informacam.app.utils.adapters;

import info.guardianproject.odkparser.utils.QD;

import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.app.R;
import org.witness.informacam.app.utils.Constants.App.Editor.Forms;
import org.witness.informacam.app.views.RoundedImageView;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.Models;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
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

    class ViewHolder 
    {
    	ImageView imageView;
    	CheckBox checkBox;
    	View viewOverlay;
    	View viewSymbols;
    	View viewAudio;
    	View viewNote;
    	View viewTag;
    	View viewEncrypted;
        AsyncTask<Integer, Void, Bitmap> loadTask;
    }

    
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		View view = null;
	    ViewHolder holder;

		if (position < mNumLoading)
		{	
			if (convertView != null && !(((ViewGroup)convertView).getChildAt(0) instanceof RoundedImageView))
				view = convertView;
			else
				view = li.inflate(R.layout.adapter_gallery_grid_placeholder, parent, false);
			
	        holder = (ViewHolder)view.getTag();
	        
	        if (holder == null)
	        {
				holder = new ViewHolder();

	        	holder.imageView = (ImageView) view.findViewById(R.id.flPlaceholder); 
	        	AnimationDrawable encryptionAnimation = (AnimationDrawable) holder.imageView.getDrawable(); 
	        	encryptionAnimation.start();
	        	
	        	view.setTag(holder);
	        }
	        else
	        {
	        	AnimationDrawable encryptionAnimation = (AnimationDrawable) holder.imageView.getDrawable(); 
	        	encryptionAnimation.start();
	        }
		}
		else
		{
			IMedia m = (IMedia) media.get(position - mNumLoading);
			MediaInfo info = mediaInfoDefault;
			
			 if (mediaInfo != null && mediaInfo.size() > 0)
					info = mediaInfo.get(Math.min(mediaInfo.size()-1,position - mNumLoading));
			 
			if (convertView != null && (((ViewGroup)convertView).getChildAt(0) instanceof RoundedImageView))
				view = convertView;
			else
				view = li.inflate(R.layout.adapter_gallery_grid, parent, false);

			holder = (ViewHolder)view.getTag();
			
			if (holder == null)
			{
				holder = new ViewHolder();
			
				holder.checkBox = (CheckBox) view
				.findViewById(R.id.chkSelect);

				holder.imageView = (ImageView) view.findViewById(R.id.gallery_thumb);
				holder.viewOverlay = view.findViewById(R.id.new_media_overlay);
				holder.viewSymbols = view.findViewById(R.id.llSymbols);
				holder.viewAudio = view.findViewById(R.id.ivAudioNote);
				holder.viewNote = view.findViewById(R.id.ivNote);
				holder.viewTag = view.findViewById(R.id.ivTag);
				holder.viewEncrypted = view.findViewById(R.id.ivEncrypted);
				
				view.setTag(holder);
			}
				
			holder.checkBox.setVisibility(
					mInSelectionMode ? View.VISIBLE : View.GONE);
		
			if (m.has(Models.IMedia.TempKeys.IS_SELECTED))
			{
				holder.checkBox.setChecked(m
					.getBoolean(Models.IMedia.TempKeys.IS_SELECTED));
			}
			else
			{
				holder.checkBox.setChecked(false);	
			}

            if (holder.loadTask != null)
                holder.loadTask.cancel(true);

			if (!m.hasThumbnail())
			{

				holder.imageView.setImageResource(R.drawable.ic_home_gallery);
				holder.loadTask = new ThumbnailTask(holder,m);
                holder.loadTask.execute(128);
			}
			else
			{
				holder.imageView.setImageBitmap(m.getThumbnail(128));
			}
			
			holder.viewOverlay.setVisibility(m.isNew ? View.VISIBLE : View.GONE);

			
			holder.viewSymbols.setVisibility((info.hasAudio || info.hasNotes || info.hasTags || (m.dcimEntry.fileAsset.source == Storage.Type.IOCIPHER)) ? View.VISIBLE : View.GONE);
			holder.viewAudio.setVisibility(info.hasAudio ? View.VISIBLE : View.GONE);
			holder.viewNote.setVisibility(info.hasNotes ? View.VISIBLE : View.GONE);
			holder.viewTag.setVisibility(info.hasTags ? View.VISIBLE : View.GONE);
			holder.viewEncrypted.setVisibility((m.dcimEntry.fileAsset.source == Storage.Type.IOCIPHER) ? View.VISIBLE : View.GONE);
			
		}
		return view;
	}
	
	private static class ThumbnailTask extends AsyncTask<Integer, Void, Bitmap> {
	    
	    private ViewHolder mHolder;
	    private IMedia mMedia;
	    public ThumbnailTask(ViewHolder holder, IMedia media) {	        
	        mHolder = holder;
	        mMedia = media;
	    }


		@Override
		protected void onPostExecute(Bitmap result) {
			
				Bitmap bitmap = (Bitmap)result;
				mHolder.imageView.setImageBitmap(bitmap);

		}



		@Override
		protected Bitmap doInBackground(Integer... size) {

			Bitmap bitmap = mMedia.getThumbnail(size[0]);			
			return bitmap;
		}
	}


}
