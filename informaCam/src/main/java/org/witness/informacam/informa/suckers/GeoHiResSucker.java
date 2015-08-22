package org.witness.informacam.informa.suckers;

import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

import org.witness.informacam.models.j3m.ILogPack;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Suckers;
import org.witness.informacam.utils.Constants.Suckers.Geo;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GeoHiResSucker extends GeoSucker implements LocationListener {
	LocationManager lm;
	Criteria criteria;
	long lastNmeaTime = 0L;
	String lastNmeaMessage = null;
	private Location mLastLocation = null;
	
	private final static String LOG = Suckers.LOG;
	
	@SuppressWarnings("unchecked")
	public GeoHiResSucker(Context context) {
		super(context);
		setSucker(this);
		
		lm = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		
		if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		} else {
			Log.d(LOG, "NETWORK PROVIDER is unavailable");
		}
		
		if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		} else {
			Log.d(LOG, "GPS PROVIDER is unavailable");
		}
				
		lm.addNmeaListener(new NmeaListener() {

			/**
			 * Used for receiving NMEA sentences from the GPS. NMEA 0183 is a standard for communicating with marine electronic devices and is a common method for receiving data from a GPS, typically over a serial port. See NMEA 0183 for more details. You can implement this interface and call addNmeaListener(GpsStatus.NmeaListener) to receive NMEA data from the GPS engine.
			 */
			@Override
			public void onNmeaReceived(long timestamp, String nmea) {
			
				lastNmeaTime = timestamp;
				lastNmeaMessage = nmea;
			}
			
		});
		
		criteria = new Criteria();
		
		try
		{
			criteria.setAccuracy(Criteria.ACCURACY_HIGH);
		}
		catch (IllegalArgumentException iae){}
	
		try
		{
			criteria.setPowerRequirement(Criteria.POWER_HIGH);
		}
		catch (IllegalArgumentException iae){}
	
	
		
		
		setTask(new TimerTask() {

			@Override
			public void run() throws NullPointerException {
				if(getIsRunning()) {
					try {
						double[] loc = updateLocation();
						if (loc != null)
							sendToBuffer(new ILogPack(Geo.Keys.GPS_COORDS, "[" + loc[0] + "," + loc[1] + "]"));
					} catch(NullPointerException e) {
						Log.e(LOG, "location NPE", e);
					}
				}
			}
		});
		
		getTimer().schedule(getTask(), 0, Geo.LOG_RATE);
	}
	
	
	public ILogPack forceReturn() {
		
		double[] loc = updateLocation();
		if(loc == null) {
			Log.d(LOG, "location was null");
			loc = new double[] {0d, 0d};
		}
		
		ILogPack iLogPack = new ILogPack(Geo.Keys.GPS_COORDS, "[" + loc[0] + "," + loc[1] + "]");
		
		if (mLastLocation != null){
			try {
				
				if (mLastLocation.hasAccuracy())			
						iLogPack.put(Geo.Keys.GPS_ACCURACY, mLastLocation.getAccuracy()+"");
				
				if (mLastLocation.hasAltitude())			
					iLogPack.put(Geo.Keys.GPS_ALTITUDE, mLastLocation.getAltitude()+"");
			
				if (mLastLocation.hasSpeed())			
					iLogPack.put(Geo.Keys.GPS_SPEED, mLastLocation.getSpeed()+"");
			
				if (mLastLocation.hasBearing())			
					iLogPack.put(Geo.Keys.GPS_BEARING, mLastLocation.getBearing()+"");	
				
				if (lastNmeaTime != 0L)				
					iLogPack.put(Geo.Keys.NMEA_TIME, lastNmeaTime);
				
				if (lastNmeaMessage != null)
					iLogPack.put(Geo.Keys.NMEA_MESSAGE, lastNmeaMessage);				
				
			} catch (Exception e) {
				Log.d(LOG,"json exception in location data",e);
			}
			
		}
		
		
		
		return iLogPack;
	}
	
	public long getTime() {
		return lastNmeaTime;
	}
	
	public double[] updateLocation() {
		
		double[] location = new double[] {0.0, 0.0};
		double[] isNull = new double[] {0.0, 0.0};

		try {
			List<String> providers = lm.getProviders(criteria, true);

			for(String provider : providers) {
				Logger.d(LOG, String.format("querying location provider %s", provider));
				Location l = lm.getLastKnownLocation(provider);

				if(l == null) {
					Logger.d(LOG, String.format("Location at provider %s is returning null...", provider));
					continue;
				}
				
				mLastLocation = l;

				location = new double[] {l.getLatitude(), l.getLongitude()};
				Logger.d(LOG, String.format("new location: %f, %f", location[0], location[1]));

				
				if(Arrays.equals(location, isNull)) {
					continue;
				} else {
					break;
				}

				
			}
		} catch(NullPointerException e) {
			Logger.e(LOG, e);
		} catch(IllegalArgumentException e) {
			Logger.e(LOG, e);
		}
		
		if(location == isNull) {
			return null;
		}
		
		return location;
	}
	
	public void stopUpdates() {
		setIsRunning(false);
		lm.removeUpdates(this);
		//Log.d(LOG, "shutting down GeoSucker...");
	}

	@Override
	public void onLocationChanged(Location location) {
		
		mLastLocation = location;
		
		if(mLastLocation != null)
		{
			ILogPack iLogPack = forceReturn();
			sendToBuffer(iLogPack);	
		}
	}

	@Override
	public void onProviderDisabled(String provider) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}	
}
