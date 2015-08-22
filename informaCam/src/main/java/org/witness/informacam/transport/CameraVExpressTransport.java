package org.witness.informacam.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.R;
import org.witness.informacam.json.JSONArray;
import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.json.JSONTokener;
import org.witness.informacam.models.Model;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.Constants.Models.IMedia.MimeType;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class CameraVExpressTransport extends Transport {
	
	public final static String FULL_DESCRIPTION = "Full description";
	public final static String FILES_DESCRIPTION = "Files description";
	public final static String SHORT_TITLE = "Short title";
	public final static String DEFAULT_SHORT_TITLE = "InformaCam submission from mobile client %s";
	public final static String DEFAULT_FULL_DESCRIPTION = "PGP Fingerprint %s";

	public CameraVExpressTransport() {
		super(Models.ITransportStub.RepositorySources.CAMERAV_EXPRESS);
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

		
	
			

		return true;
	}

	@Override
	protected HttpURLConnection buildConnection(String urlString, boolean useTorProxy) throws IOException {
		HttpURLConnection http = super.buildConnection(urlString, useTorProxy);
//		http.setRequestProperty("X-XSRF-TOKEN", "antani");
//		http.setRequestProperty("Cookie", "XSRF-TOKEN=antani;");

		return http;
	}

	@Override
	public Object parseResponse(InputStream response) {
		super.parseResponse(response);
		try {
			response.close();
		} catch (IOException e) {
			Logger.e(LOG, e);
		}

		if(transportStub.lastResult.charAt(0) == '[') {
			try {
				return (JSONArray) new JSONTokener(transportStub.lastResult).nextValue();
			} catch (JSONException e) {
				Logger.e(LOG, e);
			}
		} else {
			try {
				return (JSONObject) new JSONTokener(transportStub.lastResult).nextValue();
			} catch (JSONException e) {
				Logger.e(LOG, e);
			}
		}

		Logger.d(LOG, "THIS POST DID NOT WORK");
		return null;
	}

}