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

public class GlobaleaksTransport extends Transport {
	GLSubmission submission = null;

	public final static String FULL_DESCRIPTION = "Full description";
	public final static String FILES_DESCRIPTION = "Files description";
	public final static String SHORT_TITLE = "Short title";
	public final static String DEFAULT_SHORT_TITLE = "InformaCam submission from mobile client %s";
	public final static String DEFAULT_FULL_DESCRIPTION = "PGP Fingerprint %s";

	public GlobaleaksTransport() {
		super(Models.ITransportStub.Globaleaks.TAG);
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

		submission = new GLSubmission();
		submission.context_gus = repository.asset_id;

//		transportStub.asset.key = "files";	// (?)

		JSONObject subResponse = null;

		int numTries = 0;
		
		while ((subResponse == null) && numTries++ < Models.ITransportStub.MAX_TRIES)
		{
			try
			{
				// init submission
				subResponse = (JSONObject) doPost(submission, repository.asset_root + "/submission");
		
			}
			catch (IOException e)
			{
				mBuilder.setTicker(getString(R.string.network_error_restarting_upload_));
				mNotifyManager.notify(NOTIFY_ID, mBuilder.build());
				//Logger.w(LOG,e);
			}
		}
			
		if (subResponse == null)
		{
			Logger.d(LOG, "unable to complete upload after multiple tries");
			return false;
		}
		
		try
		{
			submission.inflate(subResponse);
		}
		catch (Exception e)
		{
			Logger.e(LOG,e);
			finishUnsuccessfully();
			
			return false;
		}
		
		if(submission.submission_gus != null) {
			
			
			if(doPost(transportStub.asset, repository.asset_root + "/submission/" + submission.submission_gus + "/file") != null) {
				submission.finalize = true;
				try {
					submission.wb_fields.put(SHORT_TITLE, String.format(DEFAULT_SHORT_TITLE, informaCam.user.alias));
					submission.wb_fields.put(FULL_DESCRIPTION, String.format(DEFAULT_FULL_DESCRIPTION, informaCam.user.pgpKeyFingerprint));
				} catch (JSONException e) {
					Logger.e(LOG, e);
				}

				JSONArray receivers = (JSONArray) doGet(repository.asset_root + "/receivers");
				if(receivers != null) {
					if(receivers.length() > 0) {
						submission.receivers = new ArrayList<String>();

						for(int r=0; r<receivers.length(); r++) {
							try {
								JSONObject receiver = receivers.getJSONObject(r);
								submission.receivers.add(receiver.getString(GLSubmission.RECEIVER_GUS));
							} catch (JSONException e) {
								Logger.e(LOG, e);
							}
						}
					}
				} 

			//	Logger.d(LOG, "ABOUT TO PUT SUBMISSION:\n" + submission.asJson().toString());
				/*
				 * {
				 * 		"files":[],
				 * 		"wb_fields":{
				 * 			"Short title":"InformaCam submission from mobile client jetta pre-14"
				 * 		},
				 * 		"submission_gus":"94c74825-acaa-426a-b2e9-b9ac3c18caff",
				 * 		"receipt":"",
				 * 		"mark":"submission",
				 * 		"download_limit":"3",		#SHOULD BE INT!
				 * 		"context_gus":"19aae9c8-93eb-44ce-9652-46c73a541f83",
				 * 		"access_limit":"50",		#SHOULD BE INT!
				 * 		"escalation_threshold":"0",
				 * 		"receivers":[
				 * 			"5bf0f9de-e64b-4a6e-901c-104009501a7f",
				 * 			"070d828f-c690-4006-89c9-8e2b1cb7c97c",
				 * 			"7bdf2f1e-9b53-4f56-a099-a748cdb78b4f",
				 * 			"0d11e41f-7bb5-4eaf-a735-258b680b1e8f"
				 * 		],
				 * 		"id":"94c74825-acaa-426a-b2e9-b9ac3c18caff",
				 * 		"creation_date":"2013-08-20T14:56:17.053271",
				 * 		"pertinence":"0",
				 * 		"expiration_date":"2013-09-04T14:56:17.053228",
				 * 		"finalize":true
				 * }
				 */

				try {
					JSONObject submissionResult = (JSONObject) doPut(submission.asJson().toString().getBytes(), repository.asset_root + "/submission/" + submission.submission_gus, MimeType.JSON);
					if(submissionResult != null) {
						submission.inflate(submissionResult);
				//		Logger.d(LOG, "OMG HOORAY:\n" + submission.asJson().toString());

						
						mBuilder
							.setContentText(getString(R.string.successful_upload_to_) + transportStub.organization.organizationName)
							.setTicker(getString(R.string.successful_upload_to_) + transportStub.organization.organizationName);
						mBuilder.setAutoCancel(true);
						mBuilder.setProgress(0, 0, false);
						// Displays the progress bar for the first time.
						mNotifyManager.notify(NOTIFY_ID, mBuilder.build());

					}
				

					finishSuccessfully();
				
				} catch(Exception e) {
					Logger.e(LOG, e);
				}
			} else {

				finishUnsuccessfully();
				
				return false;
			}

		}
	
			

		return true;
	}

	@Override
	protected HttpURLConnection buildConnection(String urlString, boolean useTorProxy) throws IOException {
		HttpURLConnection http = super.buildConnection(urlString, useTorProxy);
		http.setRequestProperty("X-XSRF-TOKEN", "antani");
		http.setRequestProperty("Cookie", "XSRF-TOKEN=antani;");

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

	public class GLSubmission extends Model implements Serializable {
		private static final long serialVersionUID = -2831519338966909927L;

		public String context_gus = null;
		public String submission_gus = null;
		public boolean finalize = false;
		public List<String> files = new ArrayList<String>();
		public List<String> receivers = new ArrayList<String>();
		public JSONObject wb_fields = new JSONObject();
		public String pertinence = null;
		public String expiration_date = null;
		public String creation_date = null;
		public String receipt = null;
		public String escalation_threshold = null;
		public String mark = null;
		public String id = null;

		public String download_limit = null;
		public String access_limit = null;

		private final static String DOWNLOAD_LIMIT = "download_limit";
		private final static String ACCESS_LIMIT = "access_limit";
		private final static String RECEIVER_GUS = "receiver_gus";

		public GLSubmission() {
			super();
		}

		public GLSubmission(GLSubmission submission) throws InstantiationException, IllegalAccessException {
			super();
			inflate(submission);
		}

		@Override
		public void inflate(JSONObject values) throws InstantiationException, IllegalAccessException {
			try {
				if(values.has(DOWNLOAD_LIMIT)) {
					values = values.put(DOWNLOAD_LIMIT, Integer.toString(values.getInt(DOWNLOAD_LIMIT)));
				}
			} catch (JSONException e) {}

			try {
				if(values.has(ACCESS_LIMIT)) {
					values = values.put(ACCESS_LIMIT, Integer.toString(values.getInt(ACCESS_LIMIT)));
				}
			} catch (JSONException e) {}

			super.inflate(values);
		}

		@Override
		public JSONObject asJson() {
			JSONObject obj = super.asJson();

			try {
				obj = obj.put(DOWNLOAD_LIMIT, Integer.parseInt(obj.getString(DOWNLOAD_LIMIT)));
			} catch (NumberFormatException e) {}
			catch (JSONException e) {}

			try {
				obj = obj.put(ACCESS_LIMIT, Integer.parseInt(obj.getString(ACCESS_LIMIT)));
			} catch (NumberFormatException e) {}
			catch (JSONException e) {}

			return obj;
		}
	}
}