package org.witness.informacam.ui;

import java.util.Iterator;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.R;
import org.witness.informacam.informa.InformaService;
import org.witness.informacam.models.j3m.IDCIMDescriptor.IDCIMSerializable;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.App.Camera;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.InformaCamEventListener;
import org.witness.informacam.utils.InformaCamBroadcaster.InformaCamStatusListener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class CameraActivity extends Activity implements InformaCamStatusListener, InformaCamEventListener {
	private final static String LOG = App.Camera.LOG;

	private boolean doInit = true;
	private Intent cameraIntent = null;
	private ComponentName cameraComponent = null;
	private String cameraIntentFlag = Camera.Intents.CAMERA;
	private int storageType = Storage.Type.FILE_SYSTEM;
	private boolean controlsInforma = true;
	private String parentId = null;

	Bundle bundle;
	Handler h = new Handler();

	private InformaCam informaCam;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_camera_waiter);
		
		informaCam = (InformaCam)getApplication();		
		
		//if suckers are active, then you are NOT controlling the informaservice, so don't turn it on/off
		controlsInforma = !InformaService.getInstance().suckersActive();
		
		setContentView(R.layout.activity_camera_waiter);
		
		if(getIntent().hasExtra(Codes.Extras.MEDIA_PARENT)) {
			parentId = getIntent().getStringExtra(Codes.Extras.MEDIA_PARENT);
		}

		try {
			Iterator<String> i = savedInstanceState.keySet().iterator();
			while(i.hasNext()) {
				String outState = i.next();
				if(outState.equals(Camera.TAG) && savedInstanceState.getBoolean(Camera.TAG)) {
					doInit = false;
				}
			}
		} catch(NullPointerException e) {}

		try {
			
			if(doInit) {
				if(getIntent().hasExtra(Codes.Extras.CAMERA_TYPE)) {
					int cameraType = getIntent().getIntExtra(Codes.Extras.CAMERA_TYPE, -1);
					switch(cameraType) {
					case Camera.Type.CAMERA:
						cameraIntentFlag = Camera.Intents.CAMERA;
						storageType = Storage.Type.FILE_SYSTEM;
						break;
					case Camera.Type.CAMCORDER:
						cameraIntentFlag = Camera.Intents.CAMCORDER;
						storageType = Storage.Type.FILE_SYSTEM;
						break;
					case Camera.Type.SECURE_CAMERA:
						cameraIntentFlag = Camera.Intents.SECURE_CAMERA;
						storageType = Storage.Type.IOCIPHER;
						break;
					case Camera.Type.SECURE_CAMCORDER:
						cameraIntentFlag = Camera.Intents.SECURE_CAMCORDER;
						storageType = Storage.Type.IOCIPHER;
						break;						
					case Camera.Type.USERCONTROLLED:
						cameraIntentFlag = null;
						break;
					}

				}

				init();
			}
		} catch(NullPointerException e) {
			
			Log.e(LOG,"error getting intent",e);
			finish();
		}
	}

	private void init() {
		
		if (cameraIntentFlag != null)
		{
			
			List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(new Intent(cameraIntentFlag), 0);
	
			for(ResolveInfo ri : resolveInfo) {
				String packageName = ri.activityInfo.packageName;
				String name = ri.activityInfo.name;
				
				/*
				 * TODO: the user's perefered camera app should be a settable preference.
				 */
	
				/*
				if(Camera.SUPPORTED.indexOf(packageName) >= 0) {
					cameraComponent = new ComponentName(packageName, name);
					break;
				}
				*/
				
				cameraComponent = new ComponentName(packageName, name);
				break;
			}
	
			if(cameraComponent == null) {
				Toast.makeText(this, getString(R.string.could_not_find_any_camera_activity), Toast.LENGTH_LONG).show();
				setResult(Activity.RESULT_CANCELED);
				finish();
			} else {
			
					startCamera();
				
			}
		}
		else 			
		{
			//this is for when we don't want InformaCam to launch the camera
		
				onInformaStart(null);
			
		}
		
		doInit = false;
	}
	// Our handler for received Intents. This will be called whenever an Intent
	// with an action named "custom-event-name" is broadcasted.
	private BroadcastReceiver mPhotoReceiver = new BroadcastReceiver() {
	  @Override
	  public void onReceive(Context context, Intent intent) {
	    // Get extra data included in the Intent
	    String mediaPath = intent.getStringExtra("media");

		try {
			informaCam.ioService.getDCIMDescriptor().addEntry(mediaPath, false, storageType);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	};
	
	
	private void startCamera ()
	{
		if (controlsInforma)
		{
			Intent intentSuckers = new Intent(this, InformaService.class);
			intentSuckers.setAction(InformaService.ACTION_START_SUCKERS);
			startService(intentSuckers);
			informaCam.ioService.startDCIMObserver(CameraActivity.this, parentId, cameraComponent);
			 LocalBroadcastManager.getInstance(this).registerReceiver(mPhotoReceiver,
				      new IntentFilter("new-media"));
						
		}
		
		if (cameraIntentFlag != null)
		{
			
			cameraIntent = new Intent(cameraIntentFlag);
			cameraIntent.setComponent(cameraComponent);
			startActivityForResult(cameraIntent, Codes.Routes.IMAGE_CAPTURE);
		}
		else
		{
			setContentView(R.layout.activity_informacam_running);
			Button btnStop = (Button)findViewById(R.id.informacam_button);
			btnStop.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View arg0) {
					
					finish();//on destroy will do the rest
				}
				
			});
				
		}
		
	}

	@Override
	public void onResume() {
		super.onResume();
		informaCam.setStatusListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();


	}

	@Override
	public void onDestroy() {
		
		if (cameraIntentFlag == null) //must be external control
			onActivityResult(Codes.Routes.IMAGE_CAPTURE,RESULT_OK,null);
		
		super.onDestroy();

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(Camera.TAG, true);

		super.onSaveInstanceState(outState);
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultMedia) {
		setResult(Activity.RESULT_CANCELED);

		boolean isInformaActive = InformaService.getInstance().suckersActive();
		
		if(isInformaActive) {
						
			if (controlsInforma)
			{

				Intent intentSuckers = new Intent(this, InformaService.class);
				intentSuckers.setAction(InformaService.ACTION_STOP_SUCKERS);
				startService(intentSuckers);
				
				informaCam.ioService.stopDCIMObserver();
				  LocalBroadcastManager.getInstance(this).unregisterReceiver(mPhotoReceiver);

			}
			
			if (resultMedia != null)
			{
				if (resultMedia.hasExtra(MediaStore.EXTRA_OUTPUT))
				{
					Object mExtra = resultMedia.getExtras().get(MediaStore.EXTRA_OUTPUT);
					
					if (mExtra instanceof String)
					{
						try {
							informaCam.ioService.getDCIMDescriptor().addEntry((String)mExtra, false, storageType);
						} catch (InstantiationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else if (mExtra instanceof String[])
					{
						for (String path : (String[])mExtra)
						{
							try {
								informaCam.ioService.getDCIMDescriptor().addEntry(path, false, storageType);
							} catch (InstantiationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				else if (resultMedia.getData() != null)
				{
					//secure cams returned single file
					String path = resultMedia.getData().getPath();
					
					try {
						informaCam.ioService.getDCIMDescriptor().addEntry(path, false, storageType);
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if (resultMedia.hasExtra("paths"))
				{
					
					Parcelable[] parcelableUris = resultMedia.getParcelableArrayExtra("paths");

	                //Java doesn't allow array casting, this is a little hack
	                Uri[] uris = new Uri[parcelableUris.length];
	                System.arraycopy(parcelableUris, 0, uris, 0, parcelableUris.length);

					
					for (Uri uri : uris)
					{
						try {
							informaCam.ioService.getDCIMDescriptor().addEntry(uri.getPath(), false, storageType);
						} catch (InstantiationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			
				
				
			}
			
			
			IDCIMSerializable dcimDescriptor = informaCam.ioService.getDCIMDescriptor().asDescriptor();
			if(dcimDescriptor.dcimList.size() > 0) {
				Intent result = new Intent().putExtra(Codes.Extras.RETURNED_MEDIA, dcimDescriptor);
				setResult(Activity.RESULT_OK, result);
			} else {
				setResult(Activity.RESULT_CANCELED);
			}
			
		} else {
			onInformaStop(null);
		}
		

		finish();
	}

	@Override
	public void onInformaCamStart(Intent intent) {
		
		if(doInit) {
			init();
		}
	}

	@Override
	public void onInformaStart(Intent intent) {
			
		
		
	}

	@Override
	public void onInformaCamStop(Intent intent) {
		
	}

	@Override
	public void onInformaStop(Intent intent) {
		
	}

	@Override
	public void onUpdate(Message message) {
		//Log.d(LOG, "I RECEIVED A MESSAGE (I SHOULDN'T THOUGH)");
		
	}


   @Override
   public void onConfigurationChanged(Configuration newConfig) {
           super.onConfigurationChanged(newConfig);

   }
}
