package org.witness.informacam.intake;

import org.witness.informacam.models.j3m.IDCIMEntry;
import org.witness.informacam.models.j3m.IDCIMDescriptor.IDCIMSerializable;
import org.witness.informacam.utils.BackgroundProcessor;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.InformaCamEventListener;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

public class Intake extends IntentService {
	
	public Intake() {
		super(Storage.Intake.TAG);		
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		BackgroundProcessor queue = new BackgroundProcessor();
		queue.setOnBatchComplete(new BatchCompleteJob(queue));
		IDCIMSerializable dcimDescriptor = ((IDCIMSerializable) intent.getSerializableExtra(Codes.Extras.RETURNED_MEDIA));
		long timeOffset = intent.getLongExtra(Codes.Extras.TIME_OFFSET, 0L);
		String[] cacheFiles = intent.getStringArrayExtra(Codes.Extras.INFORMA_CACHE);
		
		String parentId = null;
		if(intent.hasExtra(Codes.Extras.MEDIA_PARENT)) {
			parentId = intent.getStringExtra(Codes.Extras.MEDIA_PARENT);
		}
		
		for(IDCIMEntry entry : dcimDescriptor.dcimList) {
			queue.add(new EntryJob(queue, entry, parentId, cacheFiles, timeOffset));
			if(!entry.mediaType.equals(Models.IDCIMEntry.THUMBNAIL)) {
				//queue.numProcessing++;
			}
		}
		
		new Thread(queue).start();
		//queue.stop();
	}

}
