package org.witness.informacam.transport;


import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;

import org.witness.informacam.InformaCam;
import org.witness.informacam.informa.InformaService;
import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.json.JSONTokener;
import org.witness.informacam.models.Model;
import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.utils.Constants.Actions;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.Constants.Models.ITransportStub.GoogleDrive;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableNotifiedException;

public class GoogleDriveTransport extends Transport {
	GDSubmissionPermission permission;
	String fileId = null;
	AuthToken authToken = null;
	
	int auth_attempts = 0;

	private final static String SIMPLE_API_KEY = "AIzaSyDjCiTp3cof1vqTdurVWN4XVncWSn-dm-Q"; //making this loaded from assets file at some point

	public GoogleDriveTransport() {
		super(Models.ITransportStub.GoogleDrive.TAG);
	}

	@Override
	protected boolean init() {
		//Following lines make debugging Google permission easier. Make sure that
		//the service account permission is revoked (on your google account "security"
		//settings page) and then uncomment the following lines to remove the local
		//cached token.
		//authToken = new AuthToken(AccountManager.get(informaCam).getAccounts()[0]);
		//if(authToken.token != null)
		//	GoogleAuthUtil.invalidateToken(getApplicationContext(), authToken.token);

		// authenticate google drive
		
		AccountManager am =AccountManager.get(informaCam);
		Account[] accounts = am.getAccountsByType("com.google");
		
		if (accounts == null || accounts.length == 0)
		{
			Logger.d(LOG,"No Google Accounts configured on the device");
			finishUnsuccessfully();
			
			return false;
		}
		
		Logger.d(LOG, "Retrieved google accounts from device; found " + accounts.length);
		
		Logger.d(LOG, "Using account: " + accounts[0].name);
		
		if (authToken == null)
			authToken = new AuthToken(am,accounts[0]);
		
		try
		{
			authToken.doAuth();
		} catch (Exception e) {
			Logger.e(LOG, e);
			
			auth_attempts++;
			finishUnsuccessfully();
		
			return false;
			
			
		}
	
		if(authToken.token != null) {
			// TODO: if user uses tor
			if(!super.init(false)) {
				return false;
			}			
			
			// upload to drive, on success: file id is in there
			mBuilder.setProgress(100, 0, false);
			mNotifyManager.notify(NOTIFY_ID, mBuilder.build());
		

			try {
				
				JSONObject subResponse = (JSONObject) doPost(new GDSubmissionMetadata(), transportStub.asset, GoogleDrive.Urls.UPLOAD);
				if(subResponse != null) {
			
						fileId = subResponse.getString("id");
						// share to our google drive person
						subResponse = (JSONObject) doPost(new GDSubmissionPermission(), String.format(GoogleDrive.Urls.SHARE, fileId));
						Logger.d(LOG, "CONFIRM:\n" + transportStub.lastResult);
						mBuilder.setProgress(100, 60, false);
						mNotifyManager.notify(NOTIFY_ID, mBuilder.build());
						
						if(subResponse != null) {
						
							Logger.d(LOG, "CONFIRM:\n" + transportStub.lastResult);
							mBuilder
								.setContentText("Successful upload to: " + repository.asset_root)
								.setTicker("Successful upload to: " + repository.asset_root);
							mBuilder.setAutoCancel(true);
							mBuilder.setProgress(0, 0, false);
							mNotifyManager.notify(NOTIFY_ID, mBuilder.build());
							finishSuccessfully();
						}
					
				}
				
			
			} catch (Exception e) {
				Logger.e(LOG, e);
				
				auth_attempts++;
				finishUnsuccessfully();
			
				return false;
				
				
			}
		
			
		} else {
			Logger.d(LOG, "AUTH TOKEN NULL-- WHAT TO DO?");
			GoogleAuthUtil.invalidateToken(getApplicationContext(), authToken.token);
			auth_attempts++;
			
			if(auth_attempts >= 10) {
				return false;
			}
			
			return init();
		}
		return true;
	}
	
	@Override
	public Object parseResponse(InputStream response) {
		super.parseResponse(response);
		try {
			response.close();
		} catch (IOException e) {
			Logger.e(LOG, e);
		}

		try {
			return (JSONObject) new JSONTokener(transportStub.lastResult).nextValue();
		} catch (JSONException e) {
			Logger.e(LOG, e);
		}

		Logger.d(LOG, "THIS POST DID NOT WORK");
		return null;
	}

	@Override
	protected HttpURLConnection buildConnection(String urlString, boolean useTorProxy) throws IOException {
		
		StringBuilder sbUrl = new StringBuilder();
		sbUrl.append(urlString);
		
		/*
		if (urlString.contains("?"))
			sbUrl.append("&");
		else
			sbUrl.append("?");
	
		sbUrl.append("key=");
		sbUrl.append(apiKey);
		*/
		
		HttpURLConnection http = super.buildConnection(sbUrl.toString(), useTorProxy);
		http.setRequestProperty("Authorization", "Bearer " + authToken.token);		
		http.setRequestProperty("key", SIMPLE_API_KEY);
		
		Logger.d(LOG, "Authenticating Google Access with token: " + authToken.token);
		return http;
		
	}

	public static class GoogleDriveEventBroadcaster extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			Logger.d(LOG, intent.getAction());
			Bundle b = intent.getExtras();
			if (b != null)
			{
				for(String k : b.keySet()) {
					Logger.d(LOG, k);
					if(k.equals(Actions.USER_ACCEPT_ACTION)) {
						for(IOrganization organization : InformaCam.getInstance().installedOrganizations.organizations) {
							InformaCam.getInstance().resendCredentials(organization);
						}
					}
				}
			}

		}

	}

	public class GDSubmissionMetadata extends Model implements Serializable {
		private static final long serialVersionUID = -5854206953634303757L;

		public String title;

		public GDSubmissionMetadata() {
			super();
			
			title = GoogleDriveTransport.this.transportStub.asset.assetName;
		}

		public GDSubmissionMetadata(GDSubmissionMetadata metadata) throws InstantiationException, IllegalAccessException {
			super();
			inflate(metadata);
		}
	}

	public class GDSubmissionPermission extends Model implements Serializable {
		private static final long serialVersionUID = 2781623454711408251L;

		public String type;
		public String role;
		public String value;

		public GDSubmissionPermission() {
			super();
			role = Models.ITransportStub.GoogleDrive.Roles.WRITER;
			type = Models.ITransportStub.GoogleDrive.Permissions.USER;
			value = GoogleDriveTransport.this.transportStub.getRepository(Models.ITransportStub.GoogleDrive.TAG).asset_id;
		}

		public GDSubmissionPermission(GDSubmissionPermission permission) throws InstantiationException, IllegalAccessException {
			super();
			inflate(permission);
		}
	}
	
	public class AuthToken {
		public Account account;
		public String token = null;
		private AccountManager am = null;
		public Intent userAcceptCallback = new Intent().setAction(Actions.USER_ACCEPT_ACTION);

		@SuppressWarnings("deprecation")
		public AuthToken(AccountManager am, Account account) {
			this.account = account;
			this.am = am;
		}
		
		public void doAuth () throws OperationCanceledException, AuthenticatorException, IOException, UserRecoverableNotifiedException, GoogleAuthException
		{
			
			Bundle bund = new Bundle();
			token = GoogleAuthUtil.getTokenWithNotification(InformaService.getInstance().getApplicationContext(), account.name, Models.ITransportStub.GoogleDrive.SCOPE,bund);
			
			if (bund.containsKey(AccountManager.KEY_AUTHTOKEN))	
				token = bund.getString(AccountManager.KEY_AUTHTOKEN);

			/**
			if (token != null)
				am.invalidateAuthToken(Models.ITransportStub.GoogleDrive.SCOPE, token);

			
			AccountManagerFuture<Bundle> response = am.getAuthToken(account, Models.ITransportStub.GoogleDrive.SCOPE, true, new AccountManagerCallback<Bundle> () {

				@Override
				public void run(AccountManagerFuture<Bundle> result) {
		            try {
						token = result.getResult().getString(AccountManager.KEY_AUTHTOKEN);
		            	
					} catch (OperationCanceledException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (AuthenticatorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				
			}, null);
			
			Bundle b;
			b = response.getResult();
			token = b.getString(AccountManager.KEY_AUTHTOKEN);
				
						*/
			
		}
	}
}