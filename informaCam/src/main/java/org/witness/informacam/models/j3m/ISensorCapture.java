package org.witness.informacam.models.j3m;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.models.Model;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Suckers.CaptureEvent;
import org.witness.informacam.utils.Constants.Suckers.Phone;
import org.witness.informacam.utils.MediaHasher;

public class ISensorCapture extends Model {
	public long timestamp = 0L;
	public int captureType = CaptureEvent.SENSOR_PLAYBACK;
	public JSONObject sensorPlayback = null;

	public ISensorCapture() {
		super();
	}

	public ISensorCapture(long timestamp, JSONObject sensorPlayback) {
		super();

		this.timestamp = timestamp;
		
		if(sensorPlayback.has(Phone.Keys.BLUETOOTH_DEVICE_ADDRESS)) {
			try {
				String btAnon = sensorPlayback.getString(Phone.Keys.BLUETOOTH_DEVICE_ADDRESS) + ((sensorPlayback.has(Phone.Keys.BLUETOOTH_DEVICE_NAME) && !sensorPlayback.getString(Phone.Keys.BLUETOOTH_DEVICE_NAME).equals("")) ? sensorPlayback.getString(Phone.Keys.BLUETOOTH_DEVICE_NAME) : "null");
				sensorPlayback.put(Phone.Keys.BLUETOOTH_DEVICE_ADDRESS, MediaHasher.hash(btAnon.getBytes(), "SHA-1"));
			} catch (NoSuchAlgorithmException e) {
				Logger.e(LOG, e);
				e.printStackTrace();
			} catch (JSONException e) {
				Logger.e(LOG, e);
				e.printStackTrace();
			} catch (IOException e) {
				Logger.e(LOG, e);
				e.printStackTrace();
			}
		}
		
		this.sensorPlayback = sensorPlayback;
	}
}
