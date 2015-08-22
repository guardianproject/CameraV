package org.witness.informacam.utils;

import java.io.Serializable;

import org.witness.informacam.InformaCam;

@SuppressWarnings("serial")
public class BackgroundTask implements Serializable {
	protected BackgroundProcessor backgroundProcessor = null;
	protected InformaCam informaCam = InformaCam.getInstance();
	
	public BackgroundTask(BackgroundProcessor backgroundProcessor) {
		this.backgroundProcessor = backgroundProcessor;
	}
	
	public BackgroundTask getOnBatchCompleteTask() {
		return backgroundProcessor.onBatchComplete;
	}
	
	protected boolean onStart() {
		return true;
	}
	
	protected void onStop() {}
}