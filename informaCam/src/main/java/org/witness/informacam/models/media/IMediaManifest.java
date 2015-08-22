package org.witness.informacam.models.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.Model;
import org.witness.informacam.utils.TimeUtility;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.Constants.Models.IMedia.MimeType;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import android.util.Log;

public class IMediaManifest extends Model {
	public List<IMedia> listMedia = new ArrayList<IMedia>();

	public IMediaManifest() {}
	
	public boolean save() {
		synchronized (this)
		{
			return InformaCam.getInstance().saveState(this);
		}
	}
	
	public List<IMedia> getMediaList ()
	{
		return listMedia;
	}
	
	public IMedia getMediaItem (int id)
	{
		return listMedia.get(id);
	}
	
	public int getMediaItemLocation (IMedia media)
	{
		return listMedia.indexOf(media);
	}
	
	public boolean removeMediaItem (IMedia mediaToRemove)
	{
		synchronized (this)
		{
			boolean res = listMedia.remove(mediaToRemove);
			save();
		
			return res;
		}
	}
	
	public boolean addMediaItem (IMedia mediaToAdd)
	{
		synchronized (this)
		{
			boolean res = listMedia.add(mediaToAdd);
			save();
			return res;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List getAllByType(final String mimeType) {
		Collection<IMedia> media_ = Collections2.filter(listMedia, new Predicate<IMedia>() {
			@Override
			public boolean apply(IMedia m) {
				return m.dcimEntry.mediaType.equals(mimeType);
			}
		});
		
		return new ArrayList(media_);
	}
	
	public IMedia getById(final String mediaId) {
		Collection<IMedia> media_ = Collections2.filter(listMedia, new Predicate<IMedia>() {
			@Override
			public boolean apply(IMedia m) {
				return m._id.equals(mediaId);
			}
		});
		
		try {
			return media_.iterator().next();
		} catch(NullPointerException e) {
			return null;
		} catch(NoSuchElementException e) {
			return null;
		}
	}
	
	public List<IMedia> getByDay(long timestamp, String mimeType) {
		return getByDay(timestamp, mimeType, -1);
	}
	
	public List<IMedia> getByDay(long timestamp) {
		return getByDay(timestamp, null, -1);
	}
	
	public List<IMedia> getByDay(final long timestamp, final String mimeType, int limit) {
		@SuppressWarnings("unchecked")
		Collection<IMedia> media_ = Collections2.filter(mimeType == null ? listMedia : getAllByType(mimeType), new Predicate<IMedia>() {
			@Override
			public boolean apply(IMedia m) {
				if(m.dcimEntry.mediaType.equals(MimeType.LOG)) {
					return TimeUtility.matchesDay(timestamp, ((ILog) m).startTime);
				} else {
					return TimeUtility.matchesDay(timestamp, m.dcimEntry.timeCaptured);
				}
			}
		});
		
		if(limit != -1) {
			return new ArrayList<IMedia>(media_).subList(0, Math.min(media_.size(), limit));
		}
		
		return new ArrayList<IMedia>(media_);
	}
	
	public List<IMedia> sortBy(int order) {
		if(listMedia == null || listMedia.size() == 0) {
			Log.d(LOG, "NO IT IS NULL NOW");
			return null;
		}
		
		List<IMedia> _listMedia = new ArrayList<IMedia>();
		
		switch(order) {
		case Models.IMediaManifest.Sort.DATE_DESC:
			Comparator<IMedia> DateDesc = new Comparator<IMedia>() {

				@Override
				public int compare(IMedia m1, IMedia m2) {
					return m1.dcimEntry.timeCaptured > m2.dcimEntry.timeCaptured ? -1 : (m1==m2 ? 0 : 1);
				}
				
			};
			Collections.sort(listMedia, DateDesc);
			return listMedia;
		case Models.IMediaManifest.Sort.DATE_ASC:
			Comparator<IMedia> DateAsc = new Comparator<IMedia>() {

				@Override
				public int compare(IMedia m1, IMedia m2) {
					return m1.dcimEntry.timeCaptured < m2.dcimEntry.timeCaptured ? -1 : (m1==m2 ? 0 : 1);
				}
				
			};
			Collections.sort(listMedia, DateAsc);
			return listMedia;
		case Models.IMediaManifest.Sort.TYPE_PHOTO:
			
			for(IMedia m : listMedia) {
				if(m.dcimEntry.mediaType.equals(MimeType.IMAGE)) {
					_listMedia.add(m);
				}
			}
			break;
		case Models.IMediaManifest.Sort.TYPE_VIDEO:
			for(IMedia m : listMedia) {
				if(m.dcimEntry.mediaType.startsWith(MimeType.VIDEO_BASE)) {
					_listMedia.add(m);
				}
			}
			break;
		}
		
		return _listMedia;
		
	}

	public void setAllAsOld() {
		for(IMedia m : listMedia) {
			m.isNew = false;
		}
		
		//save();
		
	}
	
	
}
