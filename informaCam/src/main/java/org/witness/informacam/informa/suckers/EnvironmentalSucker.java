package org.witness.informacam.informa.suckers;

import java.util.List;
import java.util.TimerTask;

import org.witness.informacam.informa.SensorLogger;
import org.witness.informacam.json.JSONException;
import org.witness.informacam.models.j3m.ILogPack;
import org.witness.informacam.models.utils.PressureServiceUpdater;
import org.witness.informacam.utils.Constants.Suckers;
import org.witness.informacam.utils.Constants.Suckers.Environment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.util.Log;

@SuppressWarnings("rawtypes")
public class EnvironmentalSucker extends SensorLogger implements SensorEventListener {
	
	SensorManager sm;
	List<Sensor> availableSensors;
	org.witness.informacam.models.j3m.ILogPack currentAmbientTemp, currentDeviceTemp, currentHumidity, currentPressure, currentLight;
	
	float mPressureSeaLevel = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
	
	private final static String LOG = Suckers.LOG;
			
	@SuppressWarnings({ "unchecked", "deprecation" })
	public EnvironmentalSucker(Context context) {
		super(context);
		setSucker(this);
		
		sm = (SensorManager)context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
		availableSensors = sm.getSensorList(Sensor.TYPE_ALL);
		
		for(Sensor s : availableSensors) {
			switch(s.getType()) {
			case Sensor.TYPE_AMBIENT_TEMPERATURE:
				sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
				break;
			case Sensor.TYPE_RELATIVE_HUMIDITY:
				sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
				break;
			case Sensor.TYPE_PRESSURE:
				sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
				break;
			case Sensor.TYPE_LIGHT:
				sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
				break;				
			case Sensor.TYPE_TEMPERATURE:
				sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
				break;
			}
				
		}
		
		setTask(new TimerTask() {
			
			@Override
			public void run() {
					if(currentAmbientTemp != null)
						sendToBuffer(currentAmbientTemp);
					if(currentDeviceTemp != null)
						sendToBuffer(currentDeviceTemp);
					if(currentHumidity != null)
						sendToBuffer(currentHumidity);
					if(currentPressure != null)
						sendToBuffer(currentPressure);
					if(currentLight != null)
						sendToBuffer(currentLight);
			}
		});
		
		getTimer().schedule(getTask(), 0, Environment.LOG_RATE);
	}
	
	public ILogPack forceReturn() throws JSONException {
		ILogPack fr = new ILogPack();
		if (currentAmbientTemp != null)
			fr.put(Environment.Keys.AMBIENT_TEMP, currentAmbientTemp);
		if (currentDeviceTemp != null)
			fr.put(Environment.Keys.DEVICE_TEMP, currentDeviceTemp);
		if (currentHumidity != null)
			fr.put(Environment.Keys.HUMIDITY, currentHumidity);
		if (currentPressure != null)
			fr.put(Environment.Keys.PRESSURE, currentPressure);
		if (currentLight != null)
			fr.put(Environment.Keys.LIGHT, currentLight);
		return fr;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	/**
	 * 
Sensor	Sensor event data	Units of measure	Data description
TYPE_AMBIENT_TEMPERATURE	event.values[0]	°C	Ambient air temperature.
TYPE_LIGHT	event.values[0]	lx	Illuminance.
TYPE_PRESSURE	event.values[0]	hPa or mbar	Ambient air pressure.
TYPE_RELATIVE_HUMIDITY	event.values[0]	%	Ambient relative humidity.
TYPE_TEMPERATURE	event.values[0]	°C	Device temperature.1

	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized(this) {
			if(getIsRunning()) {
				ILogPack sVals = new ILogPack();
				
				try {				
					switch(event.sensor.getType()) {
					case Sensor.TYPE_AMBIENT_TEMPERATURE:
						sVals.put(Environment.Keys.AMBIENT_TEMP_CELSIUS, event.values[0]);
						currentAmbientTemp = sVals;
						break;
					case Sensor.TYPE_TEMPERATURE:
						sVals.put(Environment.Keys.DEVICE_TEMP_CELSIUS, event.values[0]);
						currentDeviceTemp = sVals;
						break;
					case Sensor.TYPE_RELATIVE_HUMIDITY:
						sVals.put(Environment.Keys.HUMIDITY_PERC,event.values[0]);
						currentHumidity = sVals;
						break;
					case Sensor.TYPE_PRESSURE:
						sVals.put(Environment.Keys.PRESSURE_MBAR, event.values[0]);
						
						//TODO we need to get real local sea level pressure here from a dynamic source
						//as the default value doesn't cut it
						float altitudeFromPressure = SensorManager.getAltitude(mPressureSeaLevel, event.values[0]);						
						sVals.put(Environment.Keys.PRESSURE_ALTITUDE, altitudeFromPressure);
						
						currentPressure = sVals;
						break;
					case Sensor.TYPE_LIGHT:
						sVals.put(Environment.Keys.LIGHT_METER_VALUE, event.values[0]);
						currentLight = sVals;
						break;
						
					}
					
				} catch(JSONException e) {}
			}
		}
	}
	
	public void stopUpdates() {
		setIsRunning(false);
		sm.unregisterListener(this);
		Log.d(LOG, "shutting down EnviroSucker...");
	}
	
	public void updateSeaLevelPressure (final double latitude, final double longitude)
	{
		
		new AsyncTask<Double, Void, Float>() {
	        @Override
	        protected Float doInBackground(Double... params) {
	           
	    		return PressureServiceUpdater.GetRefPressure(params[0],params[1]);

	        }

	        @Override
	        protected void onPostExecute(Float result) {
	        	
	        	mPressureSeaLevel = result.floatValue();
	        	Log.d("Pressure","got updated sea level pressure: " + mPressureSeaLevel);
	        }

	        @Override
	        protected void onPreExecute() {
	        }

	        @Override
	        protected void onProgressUpdate(Void... values) {
	        	
	        	
	        }
	    }.execute(latitude, longitude);
	}
	
	
}