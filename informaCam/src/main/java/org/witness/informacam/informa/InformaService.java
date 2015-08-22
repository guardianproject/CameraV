package org.witness.informacam.informa;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.witness.informacam.Debug;
import org.witness.informacam.InformaCam;
import org.witness.informacam.R;
import org.witness.informacam.informa.suckers.AccelerometerSucker;
import org.witness.informacam.informa.suckers.EnvironmentalSucker;
import org.witness.informacam.informa.suckers.GeoFusedSucker;
import org.witness.informacam.informa.suckers.GeoHiResSucker;
import org.witness.informacam.informa.suckers.GeoSucker;
import org.witness.informacam.informa.suckers.PhoneSucker;
import org.witness.informacam.intake.Intake;
import org.witness.informacam.json.JSONArray;
import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.models.j3m.ILocation;
import org.witness.informacam.models.j3m.ILogPack;
import org.witness.informacam.models.j3m.ISuckerCache;
import org.witness.informacam.models.j3m.IDCIMDescriptor.IDCIMSerializable;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.ui.AlwaysOnActivity;
import org.witness.informacam.utils.Constants.Actions;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.App.Informa;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.IManifest;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.SuckerCacheListener;
import org.witness.informacam.utils.Constants.Suckers;
import org.witness.informacam.utils.Constants.Suckers.CaptureEvent;
import org.witness.informacam.utils.Constants.Suckers.Phone;
import org.witness.informacam.utils.MediaHasher;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class InformaService extends Service implements SuckerCacheListener {
	private final IBinder binder = new LocalBinder();
	
	private long startTime = 0L;
	private long realStartTime = 0L;

	private int GPS_WAITING = 0;

	private SensorLogger<GeoSucker> _geo;
	private SensorLogger<PhoneSucker> _phone;
	private SensorLogger<AccelerometerSucker> _acc;
	private SensorLogger<EnvironmentalSucker> _env;
	private boolean suckersActive = false;

	private info.guardianproject.iocipher.File cacheFile, cacheRoot;
	private List<String> cacheFiles = new ArrayList<String>();
	private LoadingCache<Long, ILogPack> cache = null;
	
	private final static long CACHE_MAX = 500;
	private Timer cacheTimer;
	
	InformaCam informaCam;
	
	Handler h = new Handler();
	String associatedMedia = null;
	
	Intent stopIntent = new Intent().setAction(Actions.INFORMA_STOP);

	private static InformaService mInstance = null;

	public final static String ACTION_START_SUCKERS = "startsuckers";
	public final static String ACTION_STOP_SUCKERS = "stopsuckers";
	public final static String ACTION_RESET_CACHE = "resetcache";	
	
	private InformaBroadcaster[] broadcasters = {
			new InformaBroadcaster(new IntentFilter(BluetoothDevice.ACTION_FOUND)),
			new InformaBroadcaster(new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
	};

	private final static String LOG = App.Informa.LOG;

	public class LocalBinder extends Binder {
		public InformaService getService() {
			return InformaService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public static InformaService getInstance ()
	{
		return mInstance;
	}
	
	@Override
	public void onCreate() {
		Log.d(LOG, "started.");
		
		if (Debug.WAIT_FOR_DEBUGGER)
			android.os.Debug.waitForDebugger();
		
		mInstance = this;
		informaCam =  (InformaCam)getApplication();

		if (informaCam.ioService == null || (!informaCam.ioService.isMounted()))
		{
			//this seems like an auto-restart; we should stop
			stopSelf();
			return;
		}
		
		cacheRoot = new info.guardianproject.iocipher.File(IManifest.CACHES);
		if(!cacheRoot.exists()) {
			cacheRoot.mkdir();
		}

		sendBroadcast(new Intent()
			.putExtra(Codes.Keys.SERVICE, Codes.Routes.INFORMA_SERVICE)
			.setAction(Actions.ASSOCIATE_SERVICE)
			.putExtra(Codes.Extras.RESTRICT_TO_PROCESS, android.os.Process.myPid()));

		init();

	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (intent != null && intent.getAction()!=null)
		{
			if (intent.getAction().equals(ACTION_START_SUCKERS))
			{
				this.startAllSuckers();
			}
			else if (intent.getAction().equals(ACTION_STOP_SUCKERS))
			{
				this.stopAllSuckers();
			}
			else if (intent.getAction().equals(ACTION_RESET_CACHE))
			{
				resetCacheFiles();
			}
		}
		
		return START_STICKY;//super.onStartCommand(intent, flags, startId);
		
		
	}
	
	
	

	/**
	* Gets the state of Airplane Mode.
	* 
	* @param context
	* @return true if enabled.
	*/
	private static boolean isAirplaneModeOn(Context context) {

	   return Settings.System.getInt(context.getContentResolver(),
	           Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

	}
	
	public ILocation getCurrentLocation() {
		if (_geo != null)
		{
			double[] dLoc = ((GeoSucker) _geo).updateLocation();
			
			if (dLoc != null)
				return new ILocation(dLoc);
			
		}
		 
		return null;
	}

	public long getCurrentTime() {
		//return System.currentTimeMillis() + (realStartTime == 0 ? 0 : (startTime - realStartTime));
		return System.currentTimeMillis();
	}
	
	public long getTimeOffset() {
		return realStartTime == 0 ? 0 : (startTime - realStartTime);
	}

	public void associateMedia(IMedia media) {
		this.associatedMedia = media._id;
	}
	
	public void unassociateMedia() {
		this.associatedMedia = null;
	}
	
	private void init() {
		h.post(new Runnable() {
			@Override
			public void run() {
				
				
				startTime = System.currentTimeMillis();
				long currentTime = 0;
				
				if (_geo != null)
				{
					currentTime = ((GeoSucker) _geo).getTime();				
					
					if(currentTime != 0) {
						realStartTime = currentTime;					
					}
									
					double[] currentLocation = ((GeoSucker) _geo).updateLocation();
					
					if(currentTime == 0 || currentLocation == null) {
						GPS_WAITING++;
	
						if(GPS_WAITING < Suckers.GPS_WAIT_MAX) {
							h.postDelayed(this, 200);
							return;
						} else {
						//Don't show notifications the user can't do anything about
							//Toast.makeText(InformaService.this, getString(R.string.gps_not_available_your), Toast.LENGTH_LONG).show();
							GPS_WAITING = 0; //reset
						}
						
					}
					
					onUpdate(((GeoSucker) _geo).forceReturn());
				}
				
				if (_phone != null)
				{
					
					onUpdate(((PhoneSucker) _phone).forceReturn());
					
				}
				
				if (informaCam != null)
				{
					sendBroadcast(new Intent()
						.setAction(Actions.INFORMA_START)
						.putExtra(Codes.Extras.RESTRICT_TO_PROCESS, informaCam.getProcess()));
				}
			}
		});

	}
	
	public void flushCache() {
		flushCache(null);
	}
	
	public void flushCache(IMedia m) {
		saveCache(true, m);
	}

	private void initCache() {
		try {
			cacheFile = new info.guardianproject.iocipher.File(cacheRoot, MediaHasher.hash(new String(startTime + "_" + System.currentTimeMillis()).getBytes(), "MD5"));
			cacheFiles = new ArrayList<String>();
			cacheFiles.add(cacheFile.getAbsolutePath());
		} catch (NoSuchAlgorithmException e) {
			Logger.e(LOG, e);
		} catch (IOException e) {
			Logger.e(LOG, e);
		}
		
		cacheTimer = new Timer();
		cacheTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				if(cache != null) {
					
					if(cache.size() >= CACHE_MAX) {
						saveCache(true, null);
					}
				}
				
			}
			
		}, 0, 4000L);
		
		startTime = System.currentTimeMillis();
		cache = CacheBuilder.newBuilder()
				.build(new CacheLoader<Long, ILogPack>() {

					@Override
					public ILogPack load(Long timestamp) throws Exception {
						return cache.getUnchecked(timestamp);
					}

				});
		
	}
	
	private void saveCache() {
		saveCache(false, null);
	}

	private Thread mThread = null;
	
	private void saveCache(final boolean restartCache, final IMedia m) {	
		
		if (mThread == null || (!mThread.isAlive()))
		{

			if (cache == null) //service may have been stopped
				return;
			
			Log.d(LOG, "CACHE SIZE SO FAR: " + cache.size() + "\nSaving and restarting cache...");
			
			mThread = new Thread(new Runnable() {
				@Override
				public void run() {
					ISuckerCache suckerCache = new ISuckerCache();
					JSONArray cacheArray = new JSONArray();
	
					Iterator<Entry<Long, ILogPack>> cIt = cache.asMap().entrySet().iterator();
					while(cIt.hasNext()) {
						JSONObject cacheMap = new JSONObject();
						Entry<Long, ILogPack> c = cIt.next();
						try {
							cacheMap.put(String.valueOf(c.getKey()), c.getValue());
							cacheArray.put(cacheMap);
						} catch(JSONException e) {
							Logger.e(LOG, e);
						}
					}
									
					suckerCache.timeOffset = realStartTime;
					suckerCache.cache = cacheArray;
					
					// TODO: XXX: collision errors (ConcurrentModificationException)
					informaCam.ioService.saveBlob(suckerCache.asJson().toString().getBytes(), cacheFile);
	
					if(associatedMedia != null) {
						IMedia media = informaCam.mediaManifest.getById(associatedMedia);
						if(media.associatedCaches == null) {
							media.associatedCaches = new ArrayList<String>();
						}
	
						if(!media.associatedCaches.contains(cacheFile.getAbsolutePath())) { 
							media.associatedCaches.add(cacheFile.getAbsolutePath());
						}
	
						try
						{
				//		Logger.d(LOG, "OK-- I am about to save the cache reference.  is this still correct?\n" + media.asJson().toString());
							media.save();
						}
						catch (Exception e)
						{
							Logger.e(LOG, e);
							return;
						}
					}
					
					if(m != null) {
						associateMedia(m);
					}
					
					InformaService.this.onCacheSaved(restartCache);
				}
			});
			
			mThread.start();		
		}
		else
		{

			Log.d(LOG, "CACHE SAVE IN PROGRESS... WAITING IN LINE ...");
		}
		
	}

	public boolean suckersActive ()
	{
		return suckersActive;
	}
		
	private void startAllSuckers() {
		
		if(suckersActive) {
			return;
		}
		
		Logger.d(LOG, "STARTING INFORMA SUCKERS...");
		for(BroadcastReceiver broadcaster : broadcasters) {
			this.registerReceiver(broadcaster, ((InformaBroadcaster) broadcaster).intentFilter);
		}
		
		initCache();
		
		boolean prefGpsEnableHires = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getBoolean("prefGpsEnableHires",false);
		boolean hasPlayServices = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS;
	
		if (prefGpsEnableHires || (!hasPlayServices))
			_geo = new GeoHiResSucker(this);
		else	
			_geo = new GeoFusedSucker(this);
		
		_geo.setSuckerCacheListener(this);
		
		_phone = new PhoneSucker(this);
		_phone.setSuckerCacheListener(this);
	
		_acc = new AccelerometerSucker(this);
		_acc.setSuckerCacheListener(this);
		
		_env = new EnvironmentalSucker(this);
		_env.setSuckerCacheListener(this);
		
		try {
			
			double[] dLoc = ((GeoSucker) _geo).updateLocation();
			if (dLoc != null)
				((EnvironmentalSucker)_env).updateSeaLevelPressure(dLoc[0], dLoc[1]);
			
		} catch (Exception e) {
			Log.d(Informa.LOG,"error updating sea level pressure",e);
		}
		
		
		suckersActive = true;
		
		showNotification();
	}
	
	
	private void showNotification ()
	{

		  Intent notificationIntent = new Intent(this, AlwaysOnActivity.class);
		    PendingIntent pendingIntent=PendingIntent.getActivity(this, 0,
		            notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

		    Notification notification=new NotificationCompat.Builder(this)
		                                .setSmallIcon(R.drawable.ic_action_camera)
		                                .setContentTitle(getString(R.string.proof_mode_activated))
		                                .setContentIntent(pendingIntent)
		                                .setOngoing(true).build();
		    
		    startForeground(991199, notification);
		    	        
	}
	
	private void stopAllSuckers() {
		if(!suckersActive) {
			return;
		}
		
		Logger.d(LOG, "STOPPING INFORMA SUCKERS...");
		saveCache();

		if (_phone != null)
			_phone.getSucker().stopUpdates();
		
		if (_acc != null)
			_acc.getSucker().stopUpdates();
		
		if (_geo != null)
			_geo.getSucker().stopUpdates();
		
		if (_env != null)
			_env.getSucker().stopUpdates();

		_geo = null;
		_phone = null;
		_acc = null;
		_env = null;

		for(BroadcastReceiver b : broadcasters) {
			
			try
			{
				unregisterReceiver(b);
			}
			catch (IllegalArgumentException iae)
			{
				//some broadcasters may not be registered; don't let this stop us from getting destroyed!
			}
		}
		
		suckersActive = false;
		
		stopForeground(true);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		stopAllSuckers();

		sendBroadcast(stopIntent.putExtra(Codes.Extras.RESTRICT_TO_PROCESS, informaCam.getProcess()));
		
		sendBroadcast(new Intent()
			.putExtra(Codes.Keys.SERVICE, Codes.Routes.INFORMA_SERVICE)
			.setAction(Actions.DISASSOCIATE_SERVICE)
			.putExtra(Codes.Extras.RESTRICT_TO_PROCESS, android.os.Process.myPid()));
	}


	public List<ILogPack> getAllEventsByType(final int type) throws InterruptedException, ExecutionException, JSONException {
		Iterator<Entry<Long, ILogPack>> cIt = cache.asMap().entrySet().iterator();
		List<ILogPack> events = new ArrayList<ILogPack>();
		while(cIt.hasNext()) {
			Entry<Long, ILogPack> entry = cIt.next();
			if(entry.getValue().has(CaptureEvent.Keys.TYPE) && entry.getValue().getInt(CaptureEvent.Keys.TYPE) == type)
				events.add(entry.getValue());
		}

		return events;
	}

	public List<Entry<Long, ILogPack>> getAllEventsByTypeWithTimestamp(final int type) throws JSONException, InterruptedException, ExecutionException {
		Iterator<Entry<Long, ILogPack>> cIt = cache.asMap().entrySet().iterator();
		List<Entry<Long, ILogPack>> events = new ArrayList<Entry<Long, ILogPack>>();
		while(cIt.hasNext()) {
			Entry<Long, ILogPack> entry = cIt.next();
			if(entry.getValue().has(CaptureEvent.Keys.TYPE) && entry.getValue().getInt(CaptureEvent.Keys.TYPE) == type)
				events.add(entry);
		}

		return events;
	}

	public Entry<Long, ILogPack> getEventByTypeWithTimestamp(final int type) throws JSONException, InterruptedException, ExecutionException {
		Iterator<Entry<Long, ILogPack>> cIt = cache.asMap().entrySet().iterator();
		Entry<Long, ILogPack> entry = null;
		while(cIt.hasNext() && entry == null) {
			Entry<Long, ILogPack> e = cIt.next();
			if(e.getValue().has(CaptureEvent.Keys.TYPE) && e.getValue().getInt(CaptureEvent.Keys.TYPE) == type)
				entry = e;
		}

		return entry;
	}

	public ILogPack getEventByType(final int type) throws JSONException, InterruptedException, ExecutionException {
		Iterator<ILogPack> cIt = cache.asMap().values().iterator();
		ILogPack ILogPack = null;
		while(cIt.hasNext() && ILogPack == null) {
			ILogPack lp = cIt.next();

			if(lp.has(CaptureEvent.Keys.TYPE) && lp.getInt(CaptureEvent.Keys.TYPE) == type)
				ILogPack = lp;
		}

		return ILogPack;
	}

	@SuppressWarnings("unchecked")
	public boolean removeRegion(IRegion region) {
		try { 
			ILogPack ILogPack = cache.getIfPresent(region.timestamp);
			if(ILogPack.has(CaptureEvent.Keys.TYPE) && ILogPack.getInt(CaptureEvent.Keys.TYPE) == CaptureEvent.REGION_GENERATED) {
				ILogPack.remove(CaptureEvent.Keys.TYPE);
			}

			Iterator<String> repIt = region.asJson().keys();
			while(repIt.hasNext()) {
				ILogPack.remove(repIt.next());
			}

			return true;
		} catch(NullPointerException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(LOG, e.toString(),e);
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	public void addRegion(IRegion region) {
		ILogPack ILogPack = new ILogPack(CaptureEvent.Keys.TYPE, CaptureEvent.REGION_GENERATED, true);
		
		if (_geo != null)
		{
			ILogPack regionLocationData = ((GeoSucker) _geo).forceReturn();
			try {
				ILogPack.put(CaptureEvent.Keys.REGION_LOCATION_DATA, regionLocationData);
			} catch (Exception e) {
				Log.e(LOG, e.toString(),e);
			}
		}
		
		Iterator<String> rIt = region.asJson().keys();
		while(rIt.hasNext()) {
			String key = rIt.next();
			try {
				ILogPack.put(key, region.asJson().get(key));
			} catch (Exception e) {
				Log.e(LOG, e.toString(),e);
			}
		}

		//Log.d(LOG, "HEY NEW REGION: " + ILogPack.asJson().toString());
		region.timestamp = onUpdate(ILogPack);
	}

	@SuppressWarnings("unchecked")
	public void updateRegion(IRegion region) {
		try {
			ILogPack ILogPack = cache.getIfPresent(region.timestamp);
			Iterator<String> repIt = region.asJson().keys();
			while(repIt.hasNext()) {
				String key = repIt.next();
				ILogPack.put(key, region.asJson().get(key));
			}
		} catch(JSONException e) {
			Log.e(LOG, e.toString(),e);
		} catch(NullPointerException e) {
			Log.e(LOG, "CONSIDERED HANDLED:\n" + e.toString(),e);
			
			addRegion(region);
		}

	}

	@SuppressWarnings({ "unchecked", "unused" })
	private ILogPack JSONObjectToILogPack(JSONObject json) throws JSONException {
		ILogPack ILogPack = new ILogPack();
		Iterator<String> jIt = json.keys();
		while(jIt.hasNext()) {
			String key = jIt.next();
			ILogPack.put(key, json.get(key));
		}
		return ILogPack;
	}

	@SuppressWarnings("unused")
	private void pushToSucker(SensorLogger<?> sucker, ILogPack ILogPack) throws JSONException {
		if(sucker.getClass().equals(PhoneSucker.class))
			_phone.sendToBuffer(ILogPack);
	}
	
	public String getCacheFile() {
		return cacheFile.getAbsolutePath();
	}
	
	public List<String> getCacheFiles() {			
		return cacheFiles;
	}
	
	private void resetCacheFiles ()
	{
		if (cacheFiles.size() > 0)
		{
			String lastCache = cacheFiles.get(cacheFiles.size()-1);
			cacheFiles = new ArrayList<String>();
			cacheFiles.add(lastCache);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onUpdate(long timestamp, ILogPack iLogPack) {
		try {
			if (cache == null)
				initCache();
			
			ILogPack lp = cache.getIfPresent(timestamp);
			if(lp != null) {
				synchronized(iLogPack) //lock access to lp so it is not modified
				{
					Iterator<String> lIt = lp.keys();
					while(lIt.hasNext()) {
						String key = lIt.next();
						iLogPack.put(key, lp.get(key));	
					}
				}
			}

			cache.put(timestamp, iLogPack);
		} catch(JSONException e) {}
	}

	@Override
	public long onUpdate(ILogPack ILogPack) {
		long timestamp = getCurrentTime();
		onUpdate(timestamp, ILogPack);
		
		return timestamp;
	}

	class InformaBroadcaster extends BroadcastReceiver {
		IntentFilter intentFilter;

		public InformaBroadcaster(IntentFilter intentFilter) {
			this.intentFilter = intentFilter;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (_phone != null)
			{
				if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
					try {
						BluetoothDevice bd = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
						ILogPack logPack = new ILogPack(Phone.Keys.BLUETOOTH_DEVICE_ADDRESS, bd.getAddress());					
						logPack.put(Phone.Keys.BLUETOOTH_DEVICE_NAME, bd.getName());					
						onUpdate(logPack);
	
					} catch(JSONException e) {}
	
				} else if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
					try {
						ILogPack ILogPack = new ILogPack(Phone.Keys.VISIBLE_WIFI_NETWORKS, ((PhoneSucker) _phone).getWifiNetworks());
						onUpdate(ILogPack);
					} catch(NullPointerException e) {
						Log.e(LOG, "CONSIDERED HANDLED:\n" + e.toString());
						e.printStackTrace();
					}
	
				}
			}
		}

	}

	private void onCacheSaved(boolean restartCache) {
		cacheTimer.cancel();
		
		if(restartCache) {
			initCache();
		}
	}
}