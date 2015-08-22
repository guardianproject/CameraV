package org.witness.informacam.intake;

import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.models.j3m.IDCIMEntry;
import org.witness.informacam.utils.BackgroundProcessor;
import org.witness.informacam.utils.BackgroundTask;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.InformaCamEventListener;
import org.witness.informacam.utils.Constants.Logger;

import android.os.Bundle;
import android.os.Message;

public class BatchCompleteJob extends BackgroundTask {
	private static final long serialVersionUID = 1323437516938803940L;

	List<IDCIMEntry> thumbnails = new ArrayList<IDCIMEntry>();
	
	protected final static String LOG = "************************** BatchCompleteJob **************************";
	
	public BatchCompleteJob(BackgroundProcessor backgroundProcessor) {
		super(backgroundProcessor);
	}
	
	@Override
	protected boolean onStart() {
		
		persist();
		
		return super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		Bundle data = new Bundle();
		data.putInt(Codes.Extras.MESSAGE_CODE, Codes.Messages.DCIM.STOP);
		Message message = new Message();
		message.setData(data);

		InformaCamEventListener mListener = informaCam.getEventListener();
		if (mListener != null) {
			mListener.onUpdate(message);
		}
	}
	
	public void addThumbnail(IDCIMEntry thumbnail) {
		thumbnails.add(thumbnail);
	}

	public void persist() {
		Logger.d(LOG, "CLEANING UP AFTER DCIM OBSERVER");
		for(IDCIMEntry entry : thumbnails) {
			informaCam.ioService.delete(entry.fileAsset.path, entry.fileAsset.source);
			informaCam.ioService.delete(entry.uri, Type.CONTENT_RESOLVER);
		}
	}

}
