package org.witness.iwitness.models;

import java.lang.reflect.Field;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.witness.informacam.utils.Constants.App;

import android.util.Log;

public class Model extends JSONObject {
	public final static String LOG = App.LOG;
	Field[] fields;

	public void inflate(Map<String, Object> values) {


	}
	
	public void inflate(byte[] jsonStringBytes) {
		try {
			inflate((JSONObject) new JSONTokener(new String(jsonStringBytes)).nextValue());
		} catch (JSONException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
	}

	public void inflate(JSONObject values) {
		fields = this.getClass().getDeclaredFields();

		for(Field f : fields) {
			Log.d(LOG, f.getName());

		}
	}

	public JSONObject asJson() {
		fields = this.getClass().getDeclaredFields();
		JSONObject json = new JSONObject();
		
		for(Field f : fields) {
			Log.d(LOG, f.getName());
			f.setAccessible(true);
			
			try {
				Object value = f.get(this);
				json.put(f.getName(), value);
			} catch (IllegalArgumentException e) {
				Log.d(LOG, e.toString());
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				Log.d(LOG, e.toString());
				e.printStackTrace();
			} catch (JSONException e) {
				Log.d(LOG, e.toString());
				e.printStackTrace();
			}
			
		}
		
		return json;
	}

}
