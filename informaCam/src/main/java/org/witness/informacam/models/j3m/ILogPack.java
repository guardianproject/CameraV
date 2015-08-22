package org.witness.informacam.models.j3m;

import info.guardianproject.odkparser.utils.Model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;

import android.util.Log;

public class ILogPack extends Model {
	public List<Integer> captureTypes = null;

	public ILogPack() {
		super();
	}
	
	public ILogPack(String key, Object value) {
		this(key, value, false);
	}

	public ILogPack(String key, Object value, boolean isCaptureEvent) {
		super();
		
		if(isCaptureEvent) {
			if(captureTypes == null) {
				captureTypes = new ArrayList<Integer>();
			}
			
			if(!captureTypes.contains(value)) {
				captureTypes.add((Integer) value);
			}
		} else {
			try {
				this.put(key, value);
			} catch(JSONException e) {}
		}
		
		//Log.d(LOG, asJson().toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void inflate(JSONObject values) {
		super.inflate(values);

		Iterator<String> it = keys();
		while(it.hasNext()) {
			String key = it.next();
			try {
				put(key, values.get(key));
			} catch (JSONException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject asJson() {
		JSONObject j = super.asJson();
		Iterator<String> it = keys();
		while(it.hasNext()) {
			String key = it.next();
			try {
				j.put(key, get(key));
			} catch (JSONException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
		}
		return j;
	}
}
