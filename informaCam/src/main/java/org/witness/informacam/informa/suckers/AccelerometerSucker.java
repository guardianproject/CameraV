package org.witness.informacam.informa.suckers;

import java.text.DecimalFormat;
import java.util.List;
import java.util.TimerTask;

import org.witness.informacam.informa.SensorLogger;
import org.witness.informacam.json.*;
import org.witness.informacam.models.j3m.ILogPack;
import org.witness.informacam.utils.Constants.Suckers;
import org.witness.informacam.utils.Constants.Suckers.Accelerometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

@SuppressWarnings("rawtypes")
public class AccelerometerSucker extends SensorLogger implements SensorEventListener {

	SensorManager sm;
	List<Sensor> availableSensors;
	boolean hasAccelerometer, hasOrientation, hasMagneticField;
	org.witness.informacam.models.j3m.ILogPack currentAccelerometer, currentMagField;

     private DecimalFormat df = new DecimalFormat();

	private final static String LOG = Suckers.LOG;
	

	 // onSensorChanged cached values for performance, not all needed to be declared here.
	 private float[] mGravity = new float[3];
	 private float[] mGeomagnetic = new float[3];

	 private float alpha = 0.09f;// low pass filter factor
	 private boolean useLowPassFilter = false; // set to true if you have a GUI implementation of compass!

	 private int i = 0;
	 private final static int SENSOR_COMPUTE_LIMIT = 6;
		
			
	@SuppressWarnings("unchecked")
	public AccelerometerSucker(Context context) {
		super(context);
		setSucker(this);
		
        df.setMaximumFractionDigits(1);
        df.setPositivePrefix("+");
		
		sm = (SensorManager)context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
		availableSensors = sm.getSensorList(Sensor.TYPE_ALL);
		
		for(Sensor s : availableSensors) {
			switch(s.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				hasAccelerometer = true;
				sm.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				sm.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
				hasOrientation = true;
				break;
				
			
			}
				
		}
		
		setTask(new TimerTask() {
			
			@Override
			public void run() {
				try {
					if(hasAccelerometer)
						readAccelerometer();
					if(hasOrientation)
						readOrientation();
				} catch(JSONException e){}
			}
		});
		
		getTimer().schedule(getTask(), 0, Accelerometer.LOG_RATE);
	}
	
	private void readAccelerometer() throws JSONException, NullPointerException {
		if(currentAccelerometer != null)
			sendToBuffer(currentAccelerometer);
	}
	
	private void readOrientation() throws JSONException, NullPointerException {
		if(currentMagField != null)
		{
			if(currentAccelerometer != null)
			{
			//	float orientation = computeRealOrientation();
			//	currentMagField.put(Accelerometer.Keys.ORIENTATION, orientation+"");
			}
				
			sendToBuffer(currentMagField);
		
			
		}
	}
	
	
	public ILogPack forceReturn() throws JSONException {
		ILogPack fr = new ILogPack(Accelerometer.Keys.ACC, currentAccelerometer);
		fr.put(Accelerometer.Keys.ORIENTATION, currentMagField);
		return fr;
	}

	

 @SuppressWarnings("unused")
private String frm(float sensorValue) {

         return df.format(sensorValue);
 }

 @Override
 public void onAccuracyChanged(Sensor sensor, int accuracy) {

      //   Log.v(TAG, "onAccuracyChanged() accuracy:" + accuracy);
 }

 
	/**
	 * taken from this xclnt project:
	 * https://github.com/matheszabi/PortaitLandscapeCompass/blob/master/src/com/example/compassfix/AccelerometerAndMagnetometerListener.java
	 */
	  @Override
      public void onSensorChanged(SensorEvent event) {

              if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                      // apply a low pass filter: output = alpha*input + (1-alpha)*previous output;
                      if (useLowPassFilter) {
                              mGravity[0] = alpha * event.values[0] + (1f - alpha) * mGravity[0];
                              mGravity[1] = alpha * event.values[1] + (1f - alpha) * mGravity[1];
                              mGravity[2] = alpha * event.values[2] + (1f - alpha) * mGravity[2];
                      } else {
                              mGravity = event.values.clone();
                      }
                     
                     try
                     {
	      				ILogPack sVals = new ILogPack();
	                    sVals.put(Accelerometer.Keys.X, mGravity[0]);
						sVals.put(Accelerometer.Keys.Y, mGravity[1]);
						sVals.put(Accelerometer.Keys.Z, mGravity[2]);
						currentAccelerometer = sVals;
                     }
                     catch (JSONException jse)
                     {
                    	 Log.d(LOG,"json exc",jse);
                     }
              }
              if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                      // apply a low pass filter: output = alpha*input + (1-alpha)*previous output;
                      if (useLowPassFilter) {
                              mGeomagnetic[0] = alpha * event.values[0] + (1f - alpha) * mGeomagnetic[0];
                              mGeomagnetic[1] = alpha * event.values[1] + (1f - alpha) * mGeomagnetic[1];
                              mGeomagnetic[2] = alpha * event.values[2] + (1f - alpha) * mGeomagnetic[2];
                      } else {
                              mGeomagnetic = event.values.clone();
                      }
                      
                      try
                      {
	        			  ILogPack sVals = new ILogPack();
	                      sVals.put(Accelerometer.Keys.AZIMUTH, mGeomagnetic[0]);
						  sVals.put(Accelerometer.Keys.PITCH, mGeomagnetic[1]);
						  sVals.put(Accelerometer.Keys.ROLL, mGeomagnetic[2]);
						  currentMagField = sVals;
                      }
                      catch (JSONException jse)
                      {
                     	 Log.d(LOG,"json exc",jse);
                      }

              }
              
              i++; //increment idx - we don't want to calculate actual bearing every time

              if (i == SENSOR_COMPUTE_LIMIT) {
                      i = 0;
                      
                      if (mGravity != null && mGeomagnetic != null) {
                          float R[] = new float[9];
                          float I[] = new float[9];
                          boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                                  mGeomagnetic);
                          if (success) {
                              float newOrientation[] = new float[3];
                              SensorManager.getOrientation(R, newOrientation);
                              
                              try
    	                      {
                            	  float azimuthInDegress = (float)(Math.toDegrees(newOrientation[0])+360)%360;
                            	  currentMagField.put(Accelerometer.Keys.BEARING_DEGREES, azimuthInDegress);
                            	  
    	                    	  currentMagField.put(Accelerometer.Keys.AZIMUTH_CORRECTED, newOrientation[0]);
    	                    	  currentMagField.put(Accelerometer.Keys.PITCH_CORRECTED, newOrientation[1]);
    	                    	  currentMagField.put(Accelerometer.Keys.ROLL_CORRECTED, newOrientation[2]);
    	                      }
    	                      catch (JSONException jse)
    	                      {
    	                     	 Log.d(LOG,"json exc",jse);
    	                      }
                              
                          }
                      }
                      
                      // separate sensor reference and maybe on new thread too is time consuming:                        
                      
              }
      }

	
	public void stopUpdates() {
		setIsRunning(false);
		sm.unregisterListener(this);
		Log.d(LOG, "shutting down AccelerometerSucker...");
	}
}