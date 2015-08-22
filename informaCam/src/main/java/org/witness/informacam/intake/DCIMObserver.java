package org.witness.informacam.intake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;

import org.witness.informacam.Debug;
import org.witness.informacam.InformaCam;
import org.witness.informacam.models.j3m.IDCIMDescriptor;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.Logger;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.FileObserver;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

public class DCIMObserver extends BroadcastReceiver {
	private final static String LOG = Storage.LOG;

	public static IDCIMDescriptor dcimDescriptor;
	public ComponentName cameraComponent;
	
	List<ContentObserver> observers;
	InformaCam informaCam = InformaCam.getInstance();

	Handler h;
	private Context mContext;

	private FileMonitor fileMonitor;
	private int raPID = -1;

	private boolean debug = false;

	public DCIMObserver () {}
	
	public DCIMObserver(Context context, String parentId, ComponentName cameraComponent) {

		mContext = context;
		this.cameraComponent = cameraComponent;

		if (cameraComponent != null)
		{
			List<RunningAppProcessInfo> running = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
			for(RunningAppProcessInfo r : running) {
				if(r.processName.equals(cameraComponent.getPackageName())) {
					raPID = r.pid;
					break;
				}
			}
		}

		h = new Handler();

		fileMonitor = new FileMonitor();

		observers = new Vector<ContentObserver>();
		observers.add(new Observer(h, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
		observers.add(new Observer(h, MediaStore.Images.Media.INTERNAL_CONTENT_URI));
		observers.add(new Observer(h, MediaStore.Video.Media.EXTERNAL_CONTENT_URI));
		observers.add(new Observer(h, MediaStore.Video.Media.INTERNAL_CONTENT_URI));
		observers.add(new Observer(h, MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI));
		observers.add(new Observer(h, MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI));
		observers.add(new Observer(h, MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI));
		observers.add(new Observer(h, MediaStore.Video.Thumbnails.INTERNAL_CONTENT_URI));

		for(ContentObserver o : observers) {
			mContext.getContentResolver().registerContentObserver(((Observer) o).authority, false, o);
		}

		dcimDescriptor = new IDCIMDescriptor(parentId, cameraComponent);
		fileMonitor.start();
		dcimDescriptor.startSession();

		//Log.d(LOG, "DCIM OBSERVER INITED");
	}

	public void destroy() {
		dcimDescriptor.stopSession();
		dcimDescriptor = null;

		for(ContentObserver o : observers) {
			mContext.getContentResolver().unregisterContentObserver(o);
		}

		fileMonitor.stop();
		Log.d(LOG, "DCIM OBSERVER STOPPED");

	}

	class FileMonitor extends FileObserver {
		public FileMonitor() {
			super(Storage.DCIM);
			Log.d(LOG, "STARTING FILE OBSERVER ON PATH: " + Storage.DCIM);
		}

		public void start() {
			startWatching();
		}

		public void stop() {			
			stopWatching();
		}

		@SuppressWarnings("unused")
		private void lsof() {
			lsof(true, null);
		}

		private void lsof(boolean mask, String fileToWatch) {
			String line;
			Process check;
			try {
				check = Runtime.getRuntime().exec(String.format("lsof -r1 %s/*", Storage.DCIM));
				BufferedReader br = new BufferedReader(new InputStreamReader(check.getInputStream()));
				while((line = br.readLine()) != null) {
					if(fileToWatch != null) {
						if(!line.contains(fileToWatch)) {
							continue;
						}
					}

					if(!mask || line.contains(String.valueOf(raPID))) {
						Log.d(LOG, line);
					}
				}
			} catch (IOException e) {
				Logger.e(LOG, e);
			}
		}

		private void ls(String fileToWatch) {
			String line;
			Process check;
			try {
				check = Runtime.getRuntime().exec(String.format("ls -la %s", fileToWatch));
				BufferedReader br = new BufferedReader(new InputStreamReader(check.getInputStream()));
				while((line = br.readLine()) != null) {
					Log.d(LOG, line);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onEvent(int event, String path) {
			path = (Storage.DCIM + "/" + path);

			if(debug) {
				switch (event) {
				case  FileObserver.ACCESS:
					Log.d(LOG, "FILE OBSERVER: ACCESS");
					break;
				case FileObserver.MODIFY:
					Log.d(LOG, "FILE OBSERVER: MODIFY");
					break;
				case FileObserver.ATTRIB:
					Log.d(LOG, "FILE OBSERVER: ATTRIB");
					ls(path);
					break;
				case FileObserver.CLOSE_WRITE:
					Log.d(LOG, "FILE OBSERVER: CLOSE_WRITE");
					ls(path);
					break;
				case FileObserver.CLOSE_NOWRITE:
					Log.d(LOG, "FILE OBSERVER: CLOSE_NOWRITE");
					break;
				case FileObserver.OPEN:
					Log.d(LOG, "FILE OBSERVER: OPEN");
					lsof(false, path);
					break;
				case FileObserver.MOVED_FROM:
					Log.d(LOG, "FILE OBSERVER: MOVED_FROM");
					break;
				case FileObserver.MOVED_TO:
					Log.d(LOG, "FILE OBSERVER: MOVED_TO");
					break;
				case FileObserver.CREATE:
					Log.d(LOG, "FILE OBSERVER: CREATE");
					// WHICH PROCESS THOUGH???!!!
					lsof(false, null);
					ls(path);

					break;
				case FileObserver.DELETE:
					Log.d(LOG, "FILE OBSERVER: DELETE");
					break;
				case FileObserver.DELETE_SELF:
					Log.d(LOG, "FILE OBSERVER: DELETE_SELF");
					break;
				case FileObserver.MOVE_SELF:
					Log.d(LOG, "FILE OBSERVER: MOVE_SELF");
					break;
				default:
					Log.d(LOG, "FILE OBSERVER: UNKNOWN");
					lsof(false, path);
					break;

				}


				Log.d(LOG, "THE FILE OBSERVER SAW EVT: " + event + " on path " + path);
			}

		}
	}

	class Observer extends ContentObserver {
		Uri authority;

		public Observer(Handler handler, Uri authority) {
			super(handler);
			this.authority = authority;
		}

		@Override
		public void onChange(boolean selfChange) {
			Log.d(LOG, "ON CHANGE CALLED (no URI)");
			onChange(selfChange, null);

		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			boolean isThumbnail = false;
			
			if(Debug.DEBUG) {
				Logger.d(LOG, "AUTHORITY: " + authority.toString());
				
				if(uri != null) {
					//Log.d(LOG, "ON CHANGE CALLED (with URI!)");
					Logger.d(LOG, "URI: " + uri.toString());
				}
			}

			if(
					authority.equals(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI) || 
					authority.equals(MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI) ||
					authority.equals(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI) || 
					authority.equals(MediaStore.Video.Thumbnails.INTERNAL_CONTENT_URI) 
					) {
				isThumbnail = true;
			}

			try
			{
				dcimDescriptor.addEntry(authority.toString(), isThumbnail,Storage.Type.FILE_SYSTEM);
			}
			catch (Exception e)
			{
				//Logger.d(LOG,"unable to add thumbnail");
				Logger.e(LOG, e);
			}
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

	}
	
	@Override
	public void onReceive(Context context, Intent intent) {

	    Cursor cursor = context.getContentResolver().query(intent.getData(),      null,null, null, null);
	    cursor.moveToFirst();
	    String media_path = cursor.getString(cursor.getColumnIndex("_data"));
	    cursor.close();
	    
	    if (dcimDescriptor != null)
	    {
		    try
			{
				dcimDescriptor.addEntry(media_path, false, Storage.Type.FILE_SYSTEM);
			}
			catch (Exception e)
			{
				//Logger.d(LOG,"unable to add thumbnail");
				Logger.e(LOG, e);
			}
	    }
	}

}
