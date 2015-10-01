package org.witness.informacam;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.spongycastle.openpgp.PGPException;
import org.witness.informacam.crypto.CredentialManager;
import org.witness.informacam.crypto.KeyUtility;
import org.witness.informacam.crypto.SignatureService;
import org.witness.informacam.informa.Cron;
import org.witness.informacam.informa.InformaService;
import org.witness.informacam.json.JSONArray;
import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.json.JSONTokener;
import org.witness.informacam.models.Model;
import org.witness.informacam.models.credentials.IKeyStore;
import org.witness.informacam.models.credentials.ISecretKey;
import org.witness.informacam.models.credentials.IUser;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.forms.IInstalledForms;
import org.witness.informacam.models.j3m.IDCIMDescriptor;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.media.IMediaManifest;
import org.witness.informacam.models.notifications.INotification;
import org.witness.informacam.models.notifications.INotificationsManifest;
import org.witness.informacam.models.organizations.IInstalledOrganizations;
import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.models.transport.ITransportManifest;
import org.witness.informacam.models.transport.ITransportStub;
import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.storage.IOService;
import org.witness.informacam.storage.IOUtility;
import org.witness.informacam.transport.TransportUtility;
import org.witness.informacam.utils.Constants.Actions;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.App.Informa;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.IManifest;
import org.witness.informacam.utils.Constants.InformaCamEventListener;
import org.witness.informacam.utils.Constants.ListAdapterListener;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.Constants.Models.IMedia.MimeType;
import org.witness.informacam.utils.Constants.Suckers;
import org.witness.informacam.utils.InformaCamBroadcaster.InformaCamStatusListener;
import org.witness.informacam.utils.InnerBroadcaster;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Base64;
import android.util.Log;
import dalvik.system.DexFile;

public class InformaCam extends MultiDexApplication {
	/*
	 * set dynamic debug settings in initData
	 */
	
	private final static String LOG = App.LOG;

	private List<InnerBroadcaster> broadcasters = new Vector<InnerBroadcaster>();

	public IMediaManifest mediaManifest = new IMediaManifest();
	public IInstalledOrganizations installedOrganizations = new IInstalledOrganizations();
	public INotificationsManifest notificationsManifest = new INotificationsManifest();
	public ITransportManifest transportManifest = new ITransportManifest();
	
	public IUser user;

	Intent ioServiceIntent, signatureServiceIntent, uploaderServiceIntent, informaServiceIntent;

	public IOService ioService = null;
	public SignatureService signatureService = null;
	public Handler h = new Handler();

	public List<String> models = new ArrayList<String>();

	NotificationManager notificationManager;
	
	private int processId;

	private static InformaCam mInstance = null;
	
	private ListAdapterListener mListAdapterListener = null;
	private InformaCamStatusListener mStatusListener = null;
	private InformaCamEventListener mEventListener = null;
	
	private CredentialManager credentialManager = null;
	private PendingIntent cronPI = null;
	
	
	//we really need to get rid of this
	public static InformaCam getInstance ()
	{
		return mInstance;
	}
	
	public void initBroadcasters() {
		for(BroadcastReceiver br : broadcasters) {
			if(!((InnerBroadcaster) br).isMounted()) {
				((InnerBroadcaster) br).setMounted(true);
				registerReceiver(br, ((InnerBroadcaster) br).intentFilter);
			}
		}
	}

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

	@Override
	public void onCreate() {
		super.onCreate();
		
		mInstance = this;
		processId = android.os.Process.myPid();

		updateLocale ();

		Logger.d(LOG, "InformaCam service started via intent");
		
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		broadcasters.add(new InnerBroadcaster(new IntentFilter(Actions.ASSOCIATE_SERVICE), processId) {
			@Override
			public void onReceive(Context context, Intent intent) {
				super.onReceive(context, intent);
				if(!isIntended) {
					return;
				}
				
				
				if(intent.getAction().equals(Actions.ASSOCIATE_SERVICE)) {
					int serviceCode = intent.getIntExtra(Codes.Keys.SERVICE, 0);
					
					switch(serviceCode) {
					case Codes.Routes.INFORMA_SERVICE:
						
						break;
					}
				} 
			}
		});
		
		broadcasters.add(new InnerBroadcaster(new IntentFilter(Actions.UPLOADER_UPDATE), processId) {
			@Override
			public void onReceive(Context context, Intent intent) {
				super.onReceive(context, intent);
				if(!isIntended) {
					return;
				}
				
				if(intent.getAction().equals(Actions.UPLOADER_UPDATE)) {
					switch(intent.getIntExtra(Codes.Keys.UPLOADER, 0)) {
					case Codes.Transport.MUST_INSTALL_TOR:
						break;
					case Codes.Transport.MUST_START_TOR:
						break;
					}
				}
			}
		});
			
		broadcasters.add(new InnerBroadcaster(new IntentFilter(Actions.DISASSOCIATE_SERVICE), processId) {
			@Override
			public void onReceive(Context context, Intent intent) {
				super.onReceive(context, intent);
				if(!isIntended) {
					return;
				}
				
				if(intent.getAction().equals(Actions.DISASSOCIATE_SERVICE)) {
					switch(intent.getIntExtra(Codes.Keys.SERVICE, 0)) {
					case Codes.Routes.INFORMA_SERVICE:
						
						sendBroadcast(new Intent()
							.setAction(Actions.INFORMA_STOP)
							.putExtra(Codes.Extras.RESTRICT_TO_PROCESS, processId));
						break;
					}
				}
			}
		});

		initBroadcasters();

		informaServiceIntent = new Intent(this, InformaService.class);

		signatureService = new SignatureService(InformaCam.this);
		ioService = new IOService(InformaCam.this);
		
		startInforma();
		
		startup();
		
	}

	public void updateLocale ()
	{

		String localeCode = PreferenceManager.getDefaultSharedPreferences(this).getString("iw_language", "0");
		int localeIdx = Integer.parseInt(localeCode);
		String[] lang = getResources().getStringArray(R.array.locales);

		String[] localeString = lang[localeIdx].split("-");

		Locale locale = null;

		if (localeString.length == 1)
			locale = new Locale(localeString[0]);
		else
			locale = new Locale(localeString[0],localeString[1]);

		Locale.setDefault(locale);
		Configuration config = getResources().getConfiguration();
		if (Build.VERSION.SDK_INT >= 17)
			config.setLocale(locale);
		else
			config.locale = locale;
		getResources().updateConfiguration(config, getResources().getDisplayMetrics());

	}

	public void startup() {
		Logger.d(LOG, "NOW we init!");
		user = new IUser();
		
		setModels();

		final int INIT = 1;
		final int LOGIN = 2;
		final int RUN = 3;
		int startCode = LOGIN;

		boolean runForeground = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("prefRunForeground", false);

		try {
			BufferedInputStream fis = new BufferedInputStream(openFileInput(IManifest.USER));

            setCredentialManager(new CredentialManager(this, !ioService.isMounted(),false,runForeground));

            byte[] ubytes = new byte[fis.available()];
            IOUtils.readFully(fis,ubytes);
            user.inflate(ubytes);

            if(credentialManager.getStatus() == Codes.Status.UNLOCKED) {
                startCode = RUN;
            } else if(credentialManager.getStatus() == Codes.Status.LOCKED) {
                startCode = LOGIN;
            }

		} catch (FileNotFoundException e) {
			Logger.d(LOG, "CONSIDERED HANDLED:\n" + e.toString());
			startCode = INIT;

		} catch (Exception e) {
			Logger.e(LOG, e);
			startCode = INIT;
		}

		

		Bundle data = new Bundle();

		switch(startCode) {
		case INIT:
			// we launch our wizard!			
			data.putInt(Codes.Extras.MESSAGE_CODE, Codes.Messages.Wizard.INIT);
			break;
		case LOGIN:
			// we log in!
			user.isLoggedIn = false;
			try
			{
				ioService.saveBlob(user, new java.io.File(IManifest.USER));
				data.putInt(Codes.Extras.MESSAGE_CODE, Codes.Messages.Login.DO_LOGIN);
			}
			catch (IOException ioe)
			{
				Logger.e(LOG, ioe);
				startCode = INIT;	
			}
			break;
		case RUN:
			data.putInt(Codes.Extras.MESSAGE_CODE, Codes.Messages.Home.INIT);
			break;
		}

		Intent intent = new Intent(Actions.INFORMACAM_START)
			.putExtra(Codes.Keys.SERVICE, data)
			.putExtra(Codes.Extras.RESTRICT_TO_PROCESS, processId);
		sendBroadcast(intent);
	}
	
	public void initData() throws PGPException, IllegalAccessException, InstantiationException, IOException {

		try {
			ISecretKey sKey = (ISecretKey) getModel(new ISecretKey());

			if (sKey != null && sKey.secretKey != null)
				signatureService.initKey(sKey);
		}
		catch (PGPException e)
		{
			Logger.e("PGP key is null",e);
		}

		mediaManifest = (IMediaManifest) getModel(mediaManifest);

		if(mediaManifest.getMediaList().size() > 0) {
			for(IMedia m : mediaManifest.getMediaList()) {
				m.isNew = false;
			}
		}

		installedOrganizations = (IInstalledOrganizations) getModel(installedOrganizations);

		notificationsManifest = (INotificationsManifest) getModel(notificationsManifest);
		if(notificationsManifest.notifications.size() > 0) {
			notificationsManifest.sortBy(Models.INotificationManifest.Sort.DATE_DESC);

			saveState(notificationsManifest);
		}

		transportManifest = (ITransportManifest) getModel(transportManifest);

		/**
		 * XXX
		 * this is to set some testing preferences
		 * i.e. Debug.testUser_1();
		 */
		Debug.testUser_1();

	}
	
	private void setModels() {
		
		new Thread ()
		{
			
			public void run ()
			{
				try {
					String apkName = getPackageResourcePath();
					java.io.File dexDump = getDir(IManifest.DEX, Context.MODE_PRIVATE);
					if(!dexDump.exists()) {
						dexDump.mkdir();
					}
					DexFile dex = DexFile.loadDex(apkName, new java.io.File(dexDump, "dex").getAbsolutePath(), 0);
					for(Enumeration<String> classes = dex.entries(); classes.hasMoreElements() ;) {
						String clz = classes.nextElement();
						if(clz.contains("org.witness.informacam.models")) {
							models.add(clz);
						}
					}
				} catch (IOException e) {
					Logger.e(LOG, e);
				}
			}
		}.start();
			
	}
	
	public void shutdown() {
		for(BroadcastReceiver br : broadcasters) {
			try {
				((InnerBroadcaster) br).setMounted(false);
				unregisterReceiver(br);
			} catch(IllegalArgumentException e) {
				Log.w(LOG,e);
			}
		}

		//saveStates();
			
		try
		{
			ioService.unmount();
		}
		catch (IllegalStateException ise)
		{
			Log.w(Informa.LOG,"Can't unmount cleanly due to async task threads, but that's okay!");
		}
		
		//reset credential manager
		setCredentialManager(new CredentialManager(this, !ioService.isMounted(),false,false));
		
		stopService(informaServiceIntent);
		
		Intent intent = new Intent(Actions.INFORMACAM_STOP)
			.putExtra(Codes.Extras.RESTRICT_TO_PROCESS, processId);
		
		sendBroadcast(intent);
		

	}

	public void saveStates() {
		try {
			//saveState(user, new java.io.File(IManifest.USER));
			saveState(mediaManifest, new info.guardianproject.iocipher.File(IManifest.MEDIA));
		
		} catch(NullPointerException e) {
			Logger.e(LOG, e);
		}
		catch(IOException e) {
			Logger.e(LOG, e);
		}
		
	}

	@SuppressWarnings("unused")
	private boolean saveState(Model model, java.io.File cache) throws IOException {
		return ioService.saveBlob(model.asJson().toString().getBytes(), cache);
	}

	private boolean saveState(Model model, info.guardianproject.iocipher.File cache) throws IOException {
		
		return ioService.saveBlob(model.asJson().toString().getBytes(), cache);
	}
	
	public boolean saveState(Model model) {
		return saveState(model, 0);
	}

	private boolean saveState(Model model, int tries) {		
		boolean result = false;

		synchronized (model)
		{
		try
		{
			if(model.getClass().getName().equals(IKeyStore.class.getName())) {
				
				result = saveState(model, new info.guardianproject.iocipher.File(IManifest.KEY_STORE_MANIFEST));
			
			} else if(model.getClass().getName().equals(IInstalledOrganizations.class.getName())) {
			
				result = saveState(model, new info.guardianproject.iocipher.File(IManifest.ORGS));
				
				if(mListAdapterListener != null) {
					h.post(new Runnable() {
						@Override
						public void run() {
							mListAdapterListener.updateAdapter(Codes.Adapters.ORGANIZATIONS);	
						}
					});
				}
			
			} else if(model.getClass().getName().equals(IMediaManifest.class.getName())) {
				
				result = saveState(model, new info.guardianproject.iocipher.File(IManifest.MEDIA));
			} else if(model.getClass().getName().equals(ISecretKey.class.getName())) {
			
				result = saveState(model, new info.guardianproject.iocipher.File(Models.IUser.SECRET));
			
			} else if(model.getClass().getName().equals(INotificationsManifest.class.getName())) {
				
				result = saveState(model, new info.guardianproject.iocipher.File(IManifest.NOTIFICATIONS));
				
				if(mListAdapterListener != null) {
					
					h.post(new Runnable() {
						@Override
						public void run() {
							mListAdapterListener.updateAdapter(Codes.Adapters.NOTIFICATIONS);	
						}
					});
				}
				
			} else if(model.getClass().getName().equals(IDCIMDescriptor.class.getName())) {
				
				result = saveState(model, new info.guardianproject.iocipher.File(IManifest.DCIM));
				
			} else if(model.getClass().getName().equals(IUser.class.getName())) {
				
				result = ioService.saveBlob(model, new java.io.File(IManifest.USER));
			
			} else if(model.getClass().getName().equals(ITransportManifest.class.getName())) {
				
				result = saveState(model, new info.guardianproject.iocipher.File(IManifest.TRANSPORT));
			
			} else if(model.getClass().getName().equals(IInstalledForms.class.getName())) {
				
				result = saveState(model, new info.guardianproject.iocipher.File(IManifest.FORMS));
			}
		}
		catch (IOException ioe)
		{
			Logger.e(LOG, ioe);
		}
		}
		return result;
	}

	public Model getModel(Model model) throws IOException, IllegalAccessException, InstantiationException {
		byte[] bytes = null;
		
		if(model.getClass().getName().equals(IInstalledOrganizations.class.getName())) {
			bytes = ioService.getBytes(IManifest.ORGS, Type.IOCIPHER);
		} else if(model.getClass().getName().equals(IKeyStore.class.getName())) {
			bytes = ioService.getBytes(IManifest.KEY_STORE_MANIFEST, Type.IOCIPHER);
		} else if(model.getClass().getName().equals(IMediaManifest.class.getName())) {
			bytes = ioService.getBytes(IManifest.MEDIA, Type.IOCIPHER);
		} else if(model.getClass().getName().equals(ISecretKey.class.getName())) {
			bytes = ioService.getBytes(Models.IUser.SECRET, Type.IOCIPHER);
		} else if(model.getClass().getName().equals(INotificationsManifest.class.getName())) {
			bytes = ioService.getBytes(IManifest.NOTIFICATIONS, Type.IOCIPHER);
		} else if(model.getClass().getName().equals(ITransportManifest.class.getName())) {
			bytes = ioService.getBytes(IManifest.TRANSPORT, Type.IOCIPHER);
		} else if(model.getClass().getName().equals(IInstalledForms.class.getName())) {
			bytes = ioService.getBytes(IManifest.FORMS, Type.IOCIPHER);
		}

		if(!Arrays.equals(bytes, new byte[0])) {
			Log.d(Informa.LOG,"Inflating model: " + model.getClass().getName());
			model.inflate(bytes);
		}


		return model;

	}


	public boolean attemptLogout() {
		if(credentialManager.logout()) {
			shutdown();
			return true;
		}
		
		return false;
	}

	public boolean attemptLogin(char[] password) {
		return credentialManager.login(password);
	}
	
	public int getProcess() {
		return processId;
	}

	public void update(Bundle data) {
		Message message = new Message();
		message.setData(data);

		if (mEventListener != null)
			mEventListener.onUpdate(message);
	}

	public void updateNotification(INotification notification, Handler callback) throws InstantiationException, IllegalAccessException {
		notificationsManifest.getById(notification._id).inflate(notification.asJson());
		saveState(notificationsManifest);
		
		/*
		if(saveState(notificationsManifest) && mListAdapterListener != null) {
			callback.post(new Runnable() {
				@Override
				public void run() {
					mListAdapterListener.updateAdapter(Codes.Adapters.NOTIFICATIONS);
				}
			});
		}
		*/
	}
	
	public void addNotification(INotification notification, Handler callback) {
		addNotification(notification, false, callback);
	}
	
	public void addNotification(INotification notification, boolean showOnTop, Handler callback) {
		if(notificationsManifest.notifications == null) {
			notificationsManifest.notifications = new ArrayList<INotification>();
		}
		
		notificationsManifest.notifications.add(notification);
		
		saveState(notificationsManifest);
	}

	public void setListAdapterListener (ListAdapterListener lal)
	{
		mListAdapterListener = lal;
	}
	
	public void setStatusListener (InformaCamStatusListener sl)
	{
		mStatusListener = sl;
	}
	
	public InformaCamStatusListener getStatusListener ()
	{
		return mStatusListener;
	}
	
	public void setEventListener (InformaCamEventListener sl)
	{
		mEventListener = sl;
	}
	
	public InformaCamEventListener getEventListener ()
	{
		return mEventListener;
	}
	
	public IOrganization installICTD(JSONObject ictd, Handler callback, Context a) {
		IOrganization organization = null;
		JSONArray forms = null;
		
		try {
			forms = (JSONArray) ictd.get(Models.IOrganization.FORMS);
		} catch (JSONException e) {
			Logger.e(LOG, e);
		}
		
		if(forms != null) {
			for(int f=0; f<forms.length(); f++) {
				try {
					ByteArrayOutputStream baos = IOUtility.unGZipBytes(Base64.decode(forms.getString(f).getBytes(), Base64.DEFAULT));
					IForm form = FormUtility.importAndParse(new ByteArrayInputStream(baos.toByteArray()));
					if(form != null) {
				//		Logger.d(LOG, String.format("adding form\n%s", form.asJson().toString()));
						forms.put(f, form.asJson());
					} 
					baos.close();
				} catch (JSONException e) {
					Logger.e(LOG, e);
				} catch (Exception e) {
					Logger.e(LOG, e);
				}
			}
		}
		
		try {
			ByteArrayOutputStream baos = IOUtility.unGZipBytes(Base64.decode(((String) ictd.get(Models.IOrganization.PUBLIC_KEY)).getBytes(), Base64.DEFAULT));
			try {
				/**
				 * Instead of going by the stated fingerprint in the ICTD,
				 * we'll get that directly from the KeyUtility.
				 * 
				 * (protection against bad actors!)
				 */ 
				String fingerprint = KeyUtility.getFingerprintFromKey(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
				ictd.put(Models.IOrganization.ORGANIZATION_FINGERPRINT, fingerprint);
			} catch (PGPException e) {
				Logger.e(LOG, e);
				return null;
			}
			
			info.guardianproject.iocipher.File publicKeyFile = new info.guardianproject.iocipher.File(Storage.ORGS_ROOT, ictd.getString(Models.IOrganization.ORGANIZATION_FINGERPRINT) + ".asc");
			
			if(InformaCam.getInstance().ioService.saveBlob(baos.toByteArray(), publicKeyFile)) {
				ictd.put(Models.IOrganization.PUBLIC_KEY, publicKeyFile.getAbsolutePath());
			}
			baos.close();
			
			if(ictd.has(Models.IOrganization.ORGANIZATION_ICON)) {
				info.guardianproject.iocipher.File iconFile = new info.guardianproject.iocipher.File(Storage.ORGS_ROOT, ictd.getString(Models.IOrganization.ORGANIZATION_FINGERPRINT) + ".jpg");
				if(IOUtility.unGZipAndSave(
						Base64.decode(((String) ictd.get(Models.IOrganization.ORGANIZATION_ICON)).getBytes(), Base64.DEFAULT), 
						iconFile.getAbsolutePath(), 
						Type.IOCIPHER)
						) {
					ictd.put(Models.IOrganization.ORGANIZATION_ICON, iconFile.getAbsolutePath());
				}
			}
			
			organization = new IOrganization(ictd);
		//	Logger.d(LOG, "HERE IS NEW ORG:\n" + organization.asJson().toString());
		} catch(JSONException e) {
			Logger.e(LOG, e);
			return null;
		} catch (Exception e) {
			Logger.e(LOG, e);
		}
		
		
		if(organization != null) {
			installedOrganizations.addOrganization(organization, a);
			saveState(installedOrganizations);
		
			INotification notification = new INotification(getString(R.string.key_sent), getString(R.string.you_have_successfully_installed, organization.organizationName), Models.INotification.Type.NEW_KEY);			
			addNotification(notification, callback);
		}
		
		return organization;
	}
	
	public IOrganization installICTD(Uri ictdURI, Handler callback, Activity a) {

		try {
			InputStream is = getContentResolver().openInputStream(ictdURI);
			byte[] rc = new byte[is.available()];
			is.read(rc);
			is.close();

			return installICTD((JSONObject) new JSONTokener(new String(rc)).nextValue(), callback, a); 
			
		} catch (FileNotFoundException e) {
			Logger.e(LOG, e);
		} catch (IOException e) {
			Logger.e(LOG, e);
		} catch (SecurityException e) {
			Logger.e(LOG, e);
		} catch (JSONException e) {
			Logger.e(LOG, e);
		}

		return null;
	}

	private void startInforma() {
		startService(informaServiceIntent);
	}

	private void stopInforma() {
		stopService(informaServiceIntent);		
	}

	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		Logger.d(LOG, "InformaCam (Application) is terminating.");
		this.shutdown();
		credentialManager.logout();
		
	}
	
	public Intent exportCredentials() {
		java.io.File credentials = getPublicCredentials();
		
		if (credentials != null)
		{
			Intent intent = new Intent()
				.setAction(Intent.ACTION_SEND)
				.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(credentials))
				.setType("*/*");
	
			return Intent.createChooser(intent, getString(R.string.send));
		}
		else
			return null;
	}
	
	public void importAsset(String assetPath, String destinationPath, int assetSource, int destinationSource, Model model) throws InstantiationException, IllegalAccessException {
		byte[] data = ioService.getBytes(assetPath, assetSource);
		if(!Arrays.equals(data, new byte[0])) {
			if(model != null) {
				model.inflate(data);
			}
			
			if(destinationSource == Type.IOCIPHER) {
				info.guardianproject.iocipher.File asset = new info.guardianproject.iocipher.File(destinationPath);
				ioService.saveBlob(data, asset);
			} else if(destinationSource == Type.INTERNAL_STORAGE) {
				java.io.File asset = new java.io.File(destinationPath);
				try {
					ioService.saveBlob(data, asset);
				} catch (IOException e) {
					Logger.e(LOG, e);
				}
			}
			
			
		}
	}
	
	public Intent exportAsset(String assetPath, int source) {
		java.io.File asset = new java.io.File(Storage.EXTERNAL_DIR, System.currentTimeMillis() + "_tempAsset.json");
		try {
			if(ioService.saveBlob(ioService.getBytes(assetPath, source), asset, true)) {
				Intent intent = new Intent()
					.setAction(Intent.ACTION_SEND)
					.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(asset))
					.setType("*/*");
				
				return Intent.createChooser(intent, getString(R.string.send));
			}
		} catch (IOException e) {
			Logger.e(LOG, e);
		}
		
		return null;
	}
	
	public void resendCredentials(IOrganization organization) {
		INotification notification = new INotification(getResources().getString(R.string.key_sent), getResources().getString(R.string.you_have_sent_your_credentials_to_x, organization.organizationName), Models.INotification.Type.NEW_KEY);
		notification.taskComplete = false;
		addNotification(notification, null);
		
		ITransportStub transportStub = new ITransportStub(organization, notification);
		
		transportStub.setAsset(Models.IUser.PUBLIC_CREDENTIALS, Models.IUser.PUBLIC_CREDENTIALS, MimeType.ZIP, Type.IOCIPHER);
		transportStub.callbackCode = Models.ITransportStub.CallbackCodes.UPDATE_ORGANIZATION_HAS_KEY;
		
		TransportUtility.initTransport(transportStub);
	}
	
	public boolean hasCredentialManager() {
		if(credentialManager != null) {
			return true;
		}
		
		return false;
	}
	
	public CredentialManager getCredentialManager ()
	{
		return credentialManager;
	}
	
	public void setCredentialManager(CredentialManager credentialManager) {
		java.io.File informaCamDump = new java.io.File(Storage.EXTERNAL_DIR);
		if(!informaCamDump.exists()) {
			informaCamDump.mkdir();
		} else {
			if(!informaCamDump.isDirectory()) {
				informaCamDump.delete();
				informaCamDump = new java.io.File(Storage.EXTERNAL_DIR);
				informaCamDump.mkdir();
			}
		}
		
		this.credentialManager = credentialManager;
		this.credentialManager.onResume();		
	}
	
	public java.io.File getPublicCredentials() {
		return ioService.getPublicCredentials();
	}
	
	public int getCredentialManagerStatus() {
		if (credentialManager != null)
			return credentialManager.getStatus();
		else
			return Codes.Status.UNINITIALIZED;
	}
	
	public boolean isOutsideTheLoop(String action) {
		if(Arrays.asList(Actions.OUTSIDE_THE_LOOP).contains(action)) {
			return true;
		}
		
		return false;
	}
	
	public boolean isConnectedToTor() {
		/*
		OrbotHelper oh = new OrbotHelper(this);
		if(!oh.isOrbotInstalled() || !oh.isOrbotRunning()) {
			return false;
		}
		*/
		return false;
	}
	
	public void startCron() {
		startCron(Suckers.DEFAULT_CRON_INTERVAL);
	}
	
	public void startCron(int minutes) {
		Log.d(LOG, "START CRON WITH " + minutes);
		
		Intent startCron = new Intent(this, Cron.class);
		cronPI = PendingIntent.getService(this, 0, startCron, 0);
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), minutes * 60 * 1000, cronPI);
	}
	
	public void stopCron() {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(cronPI);
	}
}