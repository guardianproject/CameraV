package org.witness.informacam.utils;

import org.witness.informacam.InformaCam;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Codes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class InnerBroadcaster extends BroadcastReceiver {
	private final static String LOG = App.LOG;

	public IntentFilter intentFilter;
	public int processId = -1;
	public boolean isIntended = true;
	
	private boolean isMounted = false;

	public InnerBroadcaster(IntentFilter intentFilter, int processId) {
		this.intentFilter = intentFilter;
		this.processId = processId;
	}
	
	public void setMounted(boolean isMounted) {
		this.isMounted = isMounted;
	}
	
	public boolean isMounted() {
		return isMounted;
	}

	@Override
	public void onReceive(Context context, Intent intent) {		
		InformaCam informaCam = InformaCam.getInstance();
		if(informaCam == null) {
			isIntended = false;
			return;
		}
		
		if(intent.hasExtra(Codes.Extras.RESTRICT_TO_PROCESS)) {
			int restrictToProcess = intent.getIntExtra(Codes.Extras.RESTRICT_TO_PROCESS, -1);
			Log.d(LOG, "this broadcast should be restricted to pid " + restrictToProcess + " (my pid: " + informaCam.getProcess() + ")");
			if(restrictToProcess != informaCam.getProcess()) {
				isIntended = false;
				return;
			}
		}
	}
}
