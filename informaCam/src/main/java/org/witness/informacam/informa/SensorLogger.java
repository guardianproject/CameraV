package org.witness.informacam.informa;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.witness.informacam.json.JSONArray;
import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.models.j3m.ILogPack;
import org.witness.informacam.utils.Constants.SuckerCacheListener;
import org.witness.informacam.utils.Constants.Suckers.CaptureEvent;

import android.content.Context;

public class SensorLogger<T> {
	public T _sucker;
	
	Timer mTimer = new Timer();
	TimerTask mTask;
	
	String tag;
	
	File mLog;
	JSONArray mBuffer;
	
	Context mContext;
	boolean isRunning;
	
	//private final static String LOG = Suckers.LOG; 
		
	public SensorLogger(Context context) {
		isRunning = true;
		
		mContext = context;
	}
	
	public T getSucker() {
		return _sucker;
	}
	
	public void setSucker(T sucker) {
		_sucker = sucker;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String name) {
		this.tag = name;
	}
	
	public JSONArray getLog() {
		return mBuffer;
	}

	public Timer getTimer() {
		return mTimer;
	}
	
	public TimerTask getTask() {
		return mTask;
	}
	
	public void setTask(TimerTask task) {
		mTask = task;
	}
	
	public void setIsRunning(boolean b) {
		isRunning = b;
		if(!b)
			mTimer.cancel();
	}
	
	public boolean getIsRunning() {
		return isRunning;
	}
	
	public JSONObject returnFromLog() {
		JSONObject logged = new JSONObject();
		
		return logged;
	}
	
	public ILogPack forceReturn() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, JSONException {
		
		Class<?> args = null;
		
		if(_sucker.getClass().getDeclaredMethod("forceReturn", args) != null) {
			Method fr = _sucker.getClass().getDeclaredMethod("forceReturn", args);
			
			ILogPack logPack = (ILogPack) fr.invoke(_sucker, args);
			if(logPack.captureTypes == null) {
				logPack.captureTypes = new ArrayList<Integer>();
			}
			logPack.captureTypes.add(CaptureEvent.SENSOR_PLAYBACK);
			
			return logPack;
		}
		
		return null;
	}

	public void sendToBuffer(final ILogPack logPack) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if(logPack.captureTypes == null) {
						logPack.captureTypes = new ArrayList<Integer>();
					}
					logPack.captureTypes.add(CaptureEvent.SENSOR_PLAYBACK);
					if (mSuckerCacheListener != null) {
						mSuckerCacheListener.onUpdate(logPack);
					}
					
					//((SuckerCacheListener) informaCam.informaService).onUpdate(logPack);
				} catch(NullPointerException e) {}
			}
		}).start();
		
	}
	
	private SuckerCacheListener mSuckerCacheListener;
	
	public void setSuckerCacheListener (SuckerCacheListener scl)
	{
		mSuckerCacheListener = scl;
	}
	
	public Context getContext ()
	{
		return mContext;
	}
}
