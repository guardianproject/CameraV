package org.witness.informacam.models.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.witness.informacam.InformaCam;
import org.witness.informacam.informa.InformaService;
import org.witness.informacam.utils.Constants.IRegionDisplayListener;
import org.witness.informacam.utils.Constants.Logger;

import android.app.Activity;
import android.util.Log;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

public class IVideoRegion extends IRegion {
	public List<IVideoTrail> trail = null;
	
	private long timestampInQuestion = 0;

	private IRegionDisplayListener mListener;
	
	public IVideoRegion() {
		super();
	}
	
	public IVideoRegion(IRegion region) throws InstantiationException, IllegalAccessException {
		super();
		inflate(region);
	}

	@Override
	public void init(Activity context, IRegionBounds bounds, IRegionDisplayListener listener) {
		trail = new ArrayList<IVideoTrail>();
		Log.d(LOG, "start time: " + bounds.startTime);
		
		IVideoTrail v = new IVideoTrail(bounds.startTime, bounds);		
		trail.add(v);
		mListener = listener;
		super.init(context, bounds, listener);
	}
	
	@Override
	public void update(Activity a) {
		
		getBoundsAtTimestampInQuestion().calculate(mListener.getSpecs(),a);
		
		if (InformaService.getInstance() != null)
		InformaService.getInstance().updateRegion(this);
	}
	
	public void setBoundsAtTime(long timestamp, IRegionBounds bounds) {
		IVideoTrail v = new IVideoTrail(timestamp, bounds);
		if(trail == null) {
			trail = new ArrayList<IVideoTrail>();
		}
		
		trail.add(v);
	}
	
	public IRegionBounds getBoundsAtTimestampInQuestion() {
		Log.d(LOG, "TIMESTAMP IN QUESTION: " + timestampInQuestion);
		return getBoundsAtTime(timestampInQuestion);
	}
	
	public void setTimestampInQuestion(long timestamp) {
		timestampInQuestion = timestamp;
	}
	
	public IRegionBounds getBoundsAtTime(final long timestamp) {
		// TODO: TreeSet logic...
		Log.d(LOG, "TIMESTAMP : " + timestamp);
		Log.d(LOG, "startTime: " + bounds.startTime);
		Log.d(LOG, "endTime: " + bounds.endTime);
		
		if(trail != null) {
			// TODO: binary search would be better here...
			
			// if timestamp matches, then return that, otherwise
			Collection<IVideoTrail> trailAtTime = Collections2.filter(trail, new Predicate<IVideoTrail>() {
				@Override
				public boolean apply(IVideoTrail videoTrail) {
					return videoTrail.timestamp == timestamp;
				}
			});
			
			if (trailAtTime.size()>0)
			{
				try {
					return trailAtTime.iterator().next().bounds;
				} catch(NullPointerException e) {
					Logger.d(LOG, "we didn't get an exact match... moving on...");
					// otherwise...
				} catch(NoSuchElementException e) {
					Logger.d(LOG, "we didn't get an exact match... moving on...");
					// otherwise...
				}
			}
			
			// get the subset (2) of objs whose timestamp is before ts, and after
			IVideoTrail higher = null;
			IVideoTrail lower = null;
			
			Collection<IVideoTrail> lowerEnd = Collections2.filter(trail, new Predicate<IVideoTrail>() {

				@Override
				public boolean apply(IVideoTrail videoTrail) {
					return videoTrail.timestamp < timestamp;
				}
			});
			
			if (lowerEnd.size() > 0)
			{
				try
				{
					Log.d(LOG, "lower end size: " + lowerEnd.size());
					lower = Iterables.getLast(lowerEnd);
				//	Log.d(LOG, "LOWER:\n" + lower.asJson().toString());
				}
				catch (NoSuchElementException e)
				{
					Log.d(LOG, "lower end size not found",e);
					lower = new IVideoTrail();
					
				}
			}
			
			Collection<IVideoTrail> higherEnd = Collections2.filter(trail, new Predicate<IVideoTrail>() {

				@Override
				public boolean apply(IVideoTrail videoTrail) {
					return videoTrail.timestamp > timestamp;
				}
			});
			try {
				higher = higherEnd.iterator().next();
			} catch(NoSuchElementException e) {
				Logger.d(LOG, "nothing higher...");
				return lower.bounds;
			}
			
			// return the closest of those 2
			try {
				return Math.abs(higher.timestamp - timestamp) < Math.abs(lower.timestamp - timestamp) ? higher.bounds : lower.bounds;
			} catch(NullPointerException e) {
				if(higher != null) {
					return higher.bounds;
				} else if(lower != null) {
					return lower.bounds;
				}
			}
		}
		
		return null;
	}
}
