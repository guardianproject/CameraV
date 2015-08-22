package org.witness.informacam.informa;

import org.witness.informacam.InformaCam;
import org.witness.informacam.intake.Intake;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Suckers;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class Cron extends Service {
	private final static String LOG = App.Background.LOG;
	
	@Override
	public void onCreate() {
		//Toast.makeText(this, "Cron.onCreate()", Toast.LENGTH_LONG).show();
		Log.d(LOG, "Cron.onCreate()");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		Log.d(LOG, "Cron.onStartCommand()");
		
		Intent intentSuckers = new Intent(this, InformaService.class);
		intentSuckers.setAction(InformaService.ACTION_START_SUCKERS);
		this.startService(intentSuckers);
		
			
		(new Handler()).postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intentSuckers = new Intent(Cron.this, InformaService.class);
				intentSuckers.setAction(InformaService.ACTION_STOP_SUCKERS);
				startService(intentSuckers);
			}
		},  (long) (60 * 1000 * Suckers.DEFAULT_CRON_ACTIVE_INTERVAL));
	
		
		return START_STICKY;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		//Toast.makeText(this, "Cron.onUnbind()", Toast.LENGTH_LONG).show();
		Log.d(LOG, "Cron.onUnbind()");
		return super.onUnbind(intent);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//Toast.makeText(this, "Cron.onDestroy()", Toast.LENGTH_LONG).show();
		Log.d(LOG, "Cron.onDestroy()");
		
		Intent intentSuckers = new Intent(Cron.this, InformaService.class);
		intentSuckers.setAction(InformaService.ACTION_STOP_SUCKERS);
		startService(intentSuckers);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
