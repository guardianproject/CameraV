package org.witness.informacam.utils;

import java.util.concurrent.LinkedBlockingQueue;

import org.witness.informacam.utils.Constants.App.Background;

import android.util.Log;

@SuppressWarnings("serial")
public class BackgroundProcessor extends LinkedBlockingQueue<BackgroundTask> implements Runnable {	
	BackgroundTask currentTask = null;
	BackgroundTask onBatchComplete = null;
	
	public static int numProcessing = 0;
	public static int numCompleted = 0;
	
	private boolean stopped = false;
	
	private final static String LOG = Background.LOG;
	
	public void setOnBatchComplete(BackgroundTask onBatchComplete) {
		this.onBatchComplete = onBatchComplete;
	}
	
	public void stop() {
		stopped = true;
		if(onBatchComplete != null) {
			add(onBatchComplete);
		}
	}

	@Override
	public void run() {
		while(!stopped || this.size() > 0) {
			try {
				while((currentTask = take()) != null) {
					Log.d(LOG, "starting a new task. current queue size: " + size());
					if(currentTask.onStart()) {
						currentTask.onStop();
					}
				}
			} catch (InterruptedException e) {
				Log.e(LOG, e.toString(),e);				
			}
		}
	}

}
