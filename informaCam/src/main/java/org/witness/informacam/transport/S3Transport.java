package org.witness.informacam.transport;

import java.io.IOException;
import java.io.InputStream;

import org.witness.informacam.R;
import org.witness.informacam.utils.Constants.Models;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class S3Transport extends Transport {

	public S3Transport() {
		super(Models.ITransportStub.S3.TAG);
		
	}
	
	@Override
	protected boolean init() throws IOException {
		if(!super.init()) {
			return false;
		}

		Intent resultIntent = new Intent(Intent.ACTION_VIEW);

		PendingIntent resultPendingIntent =
				PendingIntent.getActivity(
						this,
						0,
						resultIntent,
						PendingIntent.FLAG_UPDATE_CURRENT
						);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle(getString(R.string.app_name) + ' ' + getString(R.string.upload))
		.setContentText(getString(R.string.upload_in_progress) + ' ' + transportStub.organization.organizationName)
		.setTicker(getString(R.string.upload_in_progress))
		.setSmallIcon(android.R.drawable.ic_menu_upload)
		.setContentIntent(resultPendingIntent);
		mBuilder.setProgress(100, 0, false);
		// Displays the progress bar for the first time.
		mNotifyManager.notify(NOTIFY_ID, mBuilder.build());

		InputStream in = informaCam.ioService.getStream(transportStub.asset.assetPath, transportStub.asset.storageType);
		
		try
		{
			String s3bucket = repository.asset_root; //requires public PUT permission
			
			doPut(in, s3bucket, transportStub.asset.mimeType);

			finishSuccessfully();
		}
		catch (Exception ioe)
		{
			finishUnsuccessfully();
			
		}
		
		mBuilder
			.setContentText(getString(R.string.successful_upload_to_) + transportStub.organization.organizationName)
			.setTicker(getString(R.string.successful_upload_to_) + transportStub.organization.organizationName);
		mBuilder.setAutoCancel(true);
		mBuilder.setProgress(0, 0, false);
		// Displays the progress bar for the first time.
		mNotifyManager.notify(NOTIFY_ID, mBuilder.build());

				
			


		return true;
	}
	

}
