package org.witness.informacam.utils;

import org.witness.informacam.InformaCam;
import org.witness.informacam.utils.Constants.Actions;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Codes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class InformaCamBroadcaster extends BroadcastReceiver {
	private final static String LOG = App.LOG;

	public interface InformaCamStatusListener {
		public void onInformaCamStart(Intent intent);
		public void onInformaCamStop(Intent intent);
		public void onInformaStop(Intent intent);
		public void onInformaStart(Intent intent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		InformaCam informaCam = InformaCam.getInstance();
		if(informaCam == null) {
			return;
		}

		if(intent.hasExtra(Codes.Extras.RESTRICT_TO_PROCESS)) {
			int restrictToProcess = intent.getIntExtra(Codes.Extras.RESTRICT_TO_PROCESS, -1);
			Log.d(LOG, "this broadcast should be restricted to pid " + restrictToProcess + " (my pid: " + informaCam.getProcess() + ")");
			if(restrictToProcess != informaCam.getProcess()) {
				return;
			}
		}

		InformaCamStatusListener sListener = informaCam.getStatusListener();
		
		if (sListener != null)
		{
			if(intent.getAction().equals(Actions.INFORMACAM_START)) {
				
			//	Log.d(LOG, "HEY INFORMACAM START on my process: " + informaCam.getProcess());
				sListener.onInformaCamStart(intent);
	
			} else if(intent.getAction().equals(Actions.INFORMACAM_STOP)) {
			//	Log.d(LOG, "HEY INFORMACAM STOP on my process: " + informaCam.getProcess());
				sListener.onInformaCamStop(intent);
			} else if(intent.getAction().equals(Actions.INFORMA_START)) {
			//	Log.d(LOG, "HEY INFORMA (SERVICE) START on my process: " + informaCam.getProcess());
				sListener.onInformaStart(intent);
			} else if(intent.getAction().equals(Actions.INFORMA_STOP)) {
				sListener.onInformaStop(intent);
			//	Log.d(LOG, "HEY INFORMA (SERVICE) STOP on my process: " + informaCam.getProcess());
			}
		}
	}

}
