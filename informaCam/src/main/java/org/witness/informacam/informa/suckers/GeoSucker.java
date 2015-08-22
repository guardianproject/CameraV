package org.witness.informacam.informa.suckers;

import org.witness.informacam.informa.SensorLogger;
import org.witness.informacam.models.j3m.ILogPack;

import android.content.Context;

@SuppressWarnings("rawtypes")
public abstract class GeoSucker extends SensorLogger {
	
	
	public GeoSucker(Context context) {
		super(context);
	}

	public abstract ILogPack forceReturn();
	
	public abstract long getTime();
	
	public abstract double[] updateLocation();
	
	public abstract void stopUpdates();

}
