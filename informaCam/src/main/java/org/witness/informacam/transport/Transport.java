package org.witness.informacam.transport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.witness.informacam.InformaCam;
import org.witness.informacam.R;
import org.witness.informacam.informa.InformaService;
import org.witness.informacam.models.Model;
import org.witness.informacam.models.organizations.IRepository;
import org.witness.informacam.models.transport.ITransportData;
import org.witness.informacam.models.transport.ITransportStub;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.Constants.Models.IMedia.MimeType;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class Transport extends IntentService {
	
	ITransportStub transportStub;
	IRepository repository;
	final String repoName;
	
	InformaCam informaCam;
	protected NotificationCompat.Builder mBuilder;
	protected NotificationManager mNotifyManager;
	public final static int NOTIFY_ID = 7777;
	
	protected final static String LOG = "InformaTRANSPORT";
	
	private final static String URL_USE_TOR_STRING = ".onion"; //if you see this in the url string, use the local Tor proxy
	
	public Transport(String name) {
		super(name);
		
		this.repoName = name;
		
		informaCam = InformaCam.getInstance();
	}
	
	protected boolean init() throws IOException {
		return init(true);
	}
	
	protected boolean init(boolean requiresTor) {
		repository = transportStub.getRepository(repoName);
		
		if(requiresTor) {
			int transportRequirements = checkTransportRequirements();
			if(transportRequirements == -1) {
				transportStub.numTries++;
			} else {
				transportStub.numTries = (Models.ITransportStub.MAX_TRIES + 1);
				Logger.d(LOG, "ACTUALLY NO ORBOT");

				finishUnsuccessfully(transportRequirements);
				// Prompt to start up/install orbot here.

				stopSelf();
				return false;
			}
		}
		
		mNotifyManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle(getString(R.string.app_name) + " Upload")
			.setContentText("Upload in progress to: " + repoName)
			.setTicker("Upload in progress")
			.setSmallIcon(android.R.drawable.ic_menu_upload);
		mBuilder.setProgress(100, 0, false);
		
		Intent intent = new Intent (this, InformaService.class);
		intent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pend = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(pend);
		
		// Displays the progress bar for the first time.
		mNotifyManager.notify(NOTIFY_ID, mBuilder.build());
		
		return true;
	}
	
	protected void send() {}
	
	protected void finishSuccessfully() throws InstantiationException, IllegalAccessException {
		transportStub.resultCode = Models.ITransportStub.ResultCodes.OK;
		
		if(transportStub.associatedNotification != null) {
			transportStub.associatedNotification.taskComplete = true;
			informaCam.updateNotification(transportStub.associatedNotification, informaCam.h);
		}
		
		switch(transportStub.callbackCode) {
		
		case Models.ITransportStub.CallbackCodes.UPDATE_ORGANIZATION_HAS_KEY:
			Logger.d(LOG, "ALSO MARKING KEY AS RECEIVED");
			
			transportStub.organization.keyReceived = true;
			transportStub.organization.save();
			
			break;
		}
		
		stopSelf();
		
	}
	
	protected void finishUnsuccessfully() {
		finishUnsuccessfully(-1);
	}
	
	protected void finishUnsuccessfully(int transportRequirements) {
		try
		{
			if(mBuilder != null) {
				mBuilder
				.setContentText("FAILED upload to: " + repository.asset_root)
				.setTicker("FAILED upload to: " + repository.asset_root);
				mBuilder.setAutoCancel(true);
				mBuilder.setProgress(0, 0, false);
				// Displays the progress bar for the first time.
				mNotifyManager.notify(NOTIFY_ID, mBuilder.build());
			}
			
			if(informaCam.getEventListener() != null) {
				Message message = new Message();
				Bundle data = new Bundle();
				
				if(transportRequirements == -1) {
					data.putInt(Codes.Extras.MESSAGE_CODE, Codes.Messages.Transport.GENERAL_FAILURE);
					data.putString(Codes.Extras.GENERAL_FAILURE, informaCam.getString(R.string.informacam_could_not_send));
				} else {
					data.putInt(Codes.Extras.MESSAGE_CODE, transportRequirements);
				}
				
				message.setData(data);
	
	
				informaCam.getEventListener().onUpdate(message);
			}
			
			if(transportStub.associatedNotification != null) {
				transportStub.associatedNotification.canRetry = true;
				
				transportStub.associatedNotification.save();
					
				informaCam.transportManifest.add(transportStub);
				
			}		
		}
		catch (Exception e)
		{
			Log.e("Transport Error","problem initing transport",e);
		}
	}
	
	public int checkTransportRequirements () {
		if(repository != null && repository.asset_root != null && repository.asset_root.toLowerCase().contains(URL_USE_TOR_STRING)) {

			/**
			OrbotHelper oh = new OrbotHelper(this);
			
			if(!oh.isOrbotInstalled()) {
				return Codes.Messages.Transport.ORBOT_UNINSTALLED;
			} else if(!oh.isOrbotRunning()) {
				return Codes.Messages.Transport.ORBOT_NOT_RUNNING;
			}*/
		}
	
		return -1;
	}
	
	/**
	protected void resend() {
		if(transportStub.numTries <= Models.ITransportStub.MAX_TRIES) {
			Logger.d(LOG, "POST FAILED.  Trying again.");
			init();
		} else {
			finishUnsuccessfully();
			stopSelf();
		}
	}
	 * @throws IOException */
	
	@SuppressLint("DefaultLocale")
	protected Object doPost(ITransportData fileData, String urlString) throws IOException {
		boolean useTorProxy = false;
		
		if (urlString.toLowerCase().contains(URL_USE_TOR_STRING))
			useTorProxy = true;
		
		HttpURLConnection http = buildConnection(urlString, useTorProxy);
		
		InputStream inFile = informaCam.ioService.getStream(fileData.assetPath, fileData.storageType);

        int fixedLength = (int) inFile.available();

		http.setDoOutput(true);
		http.setRequestMethod("POST");
		http.setRequestProperty("Content-Type", fileData.mimeType);
		http.setRequestProperty("Content-Disposition", "attachment; filename=\"" + fileData.assetName + "\"");
		http.setRequestProperty("Connection", "Keep-Alive");
		http.setRequestProperty("Cache-Control", "no-cache");
		
		http.setFixedLengthStreamingMode(fixedLength);
		
		http.setUseCaches(false);
		
		http.connect();
		
		DataOutputStream outNet = new DataOutputStream(http.getOutputStream());
		
		int DEFAULT_BUFFER_SIZE = 1024*4;
		
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = inFile.read(buffer))) {
			outNet.write(buffer, 0, n);
			count += n;
			updateProgress (count, fixedLength);
		}
		
		inFile.close();
		outNet.close();
		
		BufferedInputStream is = new BufferedInputStream(http.getInputStream());
		
		
		if(http.getResponseCode() > -1) {
			return(parseResponse(is));
		}
			
		return null;
	}
	
	private void updateProgress (int count, int total)
	{
		int percentage = (int)(((float)count/(float)total)*100f);
		
		mBuilder.setProgress(100, percentage, false);
		mBuilder.setContentText("Upload in progress to: " + repoName + ' ' + percentage + '%');

		// Displays the progress bar for the first time.
		mNotifyManager.notify(NOTIFY_ID, mBuilder.build());
	}
	
	protected Object doPost(Model postData, ITransportData fileData, String urlString) throws IOException {
		// multipart
		boolean useTorProxy = false;
		
		if (urlString.toLowerCase().contains(URL_USE_TOR_STRING))
			useTorProxy = true;
				
		HttpURLConnection http = buildConnection(urlString, useTorProxy);
		
		String boundary = "==11==22==44==99==InformaCam==";
		String hyphens = "--";
		String lineEnd = "\n";
		
		long bytesWritten = 0;
		List<StringBuffer> contentBuffer = new ArrayList<StringBuffer>();

		StringBuffer sb = new StringBuffer();
		sb.append((hyphens + boundary + lineEnd));
		sb.append(("Content-type: application/json; charset=UTF-8" + lineEnd + lineEnd));
		sb.append((postData.asJson().toString() + lineEnd + lineEnd));
		sb.append((hyphens + boundary + lineEnd));
		sb.append(("Content-type: " + fileData.mimeType + lineEnd + lineEnd));

		bytesWritten += sb.toString().getBytes().length;
		contentBuffer.add(sb);

		sb = new StringBuffer();
		sb.append((lineEnd + lineEnd));
		sb.append((hyphens + boundary + hyphens));
		
		bytesWritten += sb.toString().getBytes().length;
		contentBuffer.add(sb);
		
		InputStream in = informaCam.ioService.getStream(fileData.assetPath, fileData.storageType);
		bytesWritten += in.available();
		
		http.setDoOutput(true);
		
		http.setRequestMethod("POST");
		http.setRequestProperty("Content-Type", "multipart/related; boundary=\"" + boundary + "\"");
		http.setRequestProperty("Content-Length", Long.toString(bytesWritten));
		
		http.setRequestProperty("Connection", "Keep-Alive");
		http.setRequestProperty("Cache-Control", "no-cache");

		int fixedLength = (int)bytesWritten;
		
		http.setFixedLengthStreamingMode((int)bytesWritten);
		http.setUseCaches(false);
		
		http.connect();
		
		BufferedOutputStream out = new BufferedOutputStream(http.getOutputStream());
		out.write(contentBuffer.get(0).toString().getBytes());
		//Logger.d(LOG, contentBuffer.get(0).toString());
		
		int DEFAULT_BUFFER_SIZE = 1024*4;
		
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		while (-1 != (n = in.read(buffer))) {
			out.write(buffer, 0, n);
			count += n;
			updateProgress (count, fixedLength);
		}
		
		in.close();
		//Logger.d(LOG, "[... data ...]");
		
		out.write(contentBuffer.get(1).toString().getBytes());
		//Logger.d(LOG, contentBuffer.get(1).toString());
		out.flush();
		
		Log.i(LOG, "RESPONSE CODE: " + http.getResponseCode());
		Log.i(LOG, "RESPONSE MSG: " + http.getResponseMessage());
		
		int respCode = http.getResponseCode();
		
		if(respCode > -1) {

			InputStream is = new BufferedInputStream(http.getInputStream());
			
			return(parseResponse(is));
		}


		out.close();
		
		
		return null;
	}
	
	protected Object doPost(Model postData, String urlString) throws IOException {
		
		boolean useTorProxy = false;
		
		if (urlString.toLowerCase().contains(URL_USE_TOR_STRING))
			useTorProxy = true;
				
		HttpURLConnection http = buildConnection(urlString, useTorProxy);
			
		http.setDoOutput(true);
		http.setRequestMethod("POST");
		http.setRequestProperty("Content-Type", MimeType.JSON);
		
		http.connect();
		
		http.getOutputStream().write(postData.asJson().toString().getBytes());
		
		InputStream is = new BufferedInputStream(http.getInputStream());
		
		//Logger.d(LOG, "RESPONSE CODE: " + http.getResponseCode());
		//Logger.d(LOG, "RESPONSE MSG: " + http.getResponseMessage());
		
		if(http.getResponseCode() > -1) {
			return(parseResponse(is));
		}
	
		return null;
	}
	
	protected Object doPut(byte[] putData, String urlString, String mimeType) throws IOException {
	
		ByteArrayInputStream in = new ByteArrayInputStream(putData);
		return doPut(in, urlString, mimeType);
	}
	
	protected Object doPut(InputStream in, String urlString, String mimeType) throws IOException {
		
		boolean useTorProxy = false;
		
		if (urlString.toLowerCase().contains(URL_USE_TOR_STRING))
			useTorProxy = true;
		
		HttpURLConnection http = buildConnection(urlString, useTorProxy);
		
		http.setRequestMethod("PUT");
		http.setRequestProperty("Content-Type", mimeType);
		
		http.setUseCaches(false);
		
		http.connect();
		
		BufferedOutputStream out = new BufferedOutputStream(http.getOutputStream());
	
		int DEFAULT_BUFFER_SIZE = 1024*4;
		
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int count = 0;
		int n = 0;
		int totalAvail = in.available();
		
		while (-1 != (n = in.read(buffer))) {
			out.write(buffer, 0, n);
			count += n;
			updateProgress (count, totalAvail);
		}
		
		in.close();
		//Logger.d(LOG, "[... data ...]");
		
		//Logger.d(LOG, contentBuffer.get(1).toString());
		out.flush();
		
		Logger.d(LOG, "RESPONSE CODE: " + http.getResponseCode());
		Logger.d(LOG, "RESPONSE MSG: " + http.getResponseMessage());
		
		if(http.getResponseCode() > -1) {
			InputStream is = new BufferedInputStream(http.getInputStream());
			return(parseResponse(is));
		}
			
		return null;
	}
	
	protected Object doGet(String urlString) throws IOException {
		return doGet(null, urlString);
	}
	
	@SuppressWarnings("unchecked")
	protected Object doGet(Model getData, String urlString) throws IOException {
		if(getData != null) {
			List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
			Iterator<String> it = getData.asJson().keys();
			while(it.hasNext()) {
				String key = it.next();
				nameValuePair.add(new BasicNameValuePair(key, String.valueOf(getData.asJson().get(key))));
				
			}
			urlString += ("?" + URLEncodedUtils.format(nameValuePair, "utf_8"));
		}
		
		boolean useTorProxy = false;
		
		if (urlString.toLowerCase().contains(URL_USE_TOR_STRING))
			useTorProxy = true;
		
		HttpURLConnection http = buildConnection(urlString, useTorProxy);
		
		http.setRequestMethod("GET");
		http.setDoOutput(false);
		
		InputStream is = new BufferedInputStream(http.getInputStream());
		http.connect();
		
	//	Logger.d(LOG, "RESPONSE CODE: " + http.getResponseCode());
	//	Logger.d(LOG, "RESPONSE MSG: " + http.getResponseMessage());
		
		if(http.getResponseCode() > -1) {
			return(parseResponse(is));
		} else {
			try {
				//Logger.d(LOG, String.format(LOG, "ERROR IF PRESENT:\n%s", ((JSONObject) parseResponse(is)).toString()));
			} catch(Exception e) {
				Logger.e(LOG, e);
			}
		}
			
		return null;
	}
	
	protected Object parseResponse(InputStream is) {
		StringBuffer lastResult = new StringBuffer();
		
		try {
			for(String line : IOUtils.readLines(is)) {
				Logger.d(LOG, line);
				lastResult.append(line);
			}
			
			transportStub.lastResult = lastResult.toString();
			return lastResult.toString();
		} catch (IOException e) {
			Logger.e(LOG, e);
		}
		
		return null;
	}
	
	protected HttpURLConnection buildConnection(String urlString, boolean useTorProxy) throws IOException  {
		HttpURLConnection http = null;

		URL url = new URL(urlString == null ? repository.asset_root : urlString);
		
//		Logger.d(LOG,  "URL PROTOCOL: " + url.getProtocol());
		if(url.getProtocol().equals("https")) {	
			// TODO: add memorizing trust manager
		}
		
		if (useTorProxy)
		{
//			Logger.d(LOG, "AND USING TOR PROXY");
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8118));
			http = (HttpURLConnection) url.openConnection(proxy);
		} else {
			http = (HttpURLConnection) url.openConnection();
		}
		
		http.setUseCaches(false);
	
		
		return http;
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		transportStub = (ITransportStub) intent.getSerializableExtra(Models.ITransportStub.TAG);
		//Log.d(LOG, "TRANSPORT:\n" + transportStub.asJson().toString()); 
		
		if(transportStub == null) {
			stopSelf();
		} else {
			try
			{
				init();
			}
			catch (IOException ioe)
			{
				Log.e(LOG,"Unable to initiated transport",ioe);
			}
		}
	}

}