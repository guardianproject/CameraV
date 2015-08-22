package org.witness.informacam.models.media;

import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.models.Model;
import org.witness.informacam.utils.Constants.Models;

import android.util.Log;

public class IVideoTrail extends Model {
	public long timestamp = 0L;
	public IRegionBounds bounds = null;
	
	public IVideoTrail() {
		super();
	}
	
	public IVideoTrail(long timestamp, IRegionBounds bounds) {
		super();
		
		this.timestamp = timestamp;
		this.bounds = bounds;
	}
	
	@Override
	public JSONObject asJson() {
		JSONObject vt = super.asJson();
		try {
			JSONObject b = vt.getJSONObject(Models.IRegion.BOUNDS);
			b.remove(Models.IRegion.Bounds.DURATION);
			b.remove(Models.IRegion.Bounds.START_TIME);
			b.remove(Models.IRegion.Bounds.END_TIME);
		} catch (JSONException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
		
		return vt;
	}
}
