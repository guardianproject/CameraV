package org.witness.informacam.models.j3m;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.informa.Cron;
import org.witness.informacam.informa.InformaService;
import org.witness.informacam.intake.Intake;
import org.witness.informacam.models.Model;
import org.witness.informacam.models.media.IAsset;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.Constants.Models.IMedia.MimeType;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.MediaColumns;

@SuppressLint("DefaultLocale")
public class IDCIMDescriptor extends Model {	
	public List<IDCIMEntry> shortDescription = new ArrayList<IDCIMEntry>();
	public Hashtable<String,IDCIMEntry> intakeMasterList = new Hashtable<String,IDCIMEntry>();

	private long startTime = 0L;
	private long timeOffset = 0L;
	private String parentId = null;
	private InformaCam informaCam = InformaCam.getInstance();
	private String cameraComponent = null;

	private final static String LOG = Storage.LOG;

	public IDCIMDescriptor(String parentId, ComponentName cameraComponent) {
		startTime = System.currentTimeMillis()/1000;
		this.parentId = parentId;
		
		if (cameraComponent != null)
			this.cameraComponent = cameraComponent.getPackageName();
	}

	public IDCIMSerializable asDescriptor() {
		return new IDCIMSerializable(shortDescription);
	}

	public void addEntry(String path, boolean isThumbnail, int sourceType) throws InstantiationException, IllegalAccessException {
		final IDCIMEntry entry = new IDCIMEntry();
		
		Uri authority = Uri.parse(path);
		entry.authority = authority.toString();
		
		String sortBy = "date_added DESC";

		if(isThumbnail) {
			sortBy = null;
			entry.mediaType = Models.IDCIMEntry.THUMBNAIL;
		}

		Cursor cursor = null;
		
		if (sourceType !=Storage.Type.IOCIPHER) 
			cursor = InformaCam.getInstance().getContentResolver().query(authority, null, null, null, sortBy);

		if(cursor != null && cursor.moveToFirst()) {
			path = cursor.getString(cursor.getColumnIndexOrThrow(MediaColumns.DATA));
			
			/*
			 * IF the path is not already in out dcimList
			 */
			
			if (intakeMasterList.containsKey(path))
			{
				cursor.close();
				return;
			}
			else
				intakeMasterList.put(path, entry);
			
			entry.fileAsset = new IAsset(path, sourceType);

			if(!isThumbnail) {
				entry.timeCaptured = cursor.getLong(cursor.getColumnIndexOrThrow(MediaColumns.DATE_ADDED));
				if(entry.timeCaptured < startTime) {
				//	Logger.d(LOG, "this media occured too early to count");
					cursor.close();

					return;
				}

				entry.mediaType = cursor.getString(cursor.getColumnIndexOrThrow(MediaColumns.MIME_TYPE));
				entry.cameraComponent = cameraComponent;

				//if(entry.mediaType.equals(MimeType.VIDEO_3GPP)) {
				//entry.mediaType = MimeType.VIDEO;
				//}
			}

			// String pattern = "^([a-zA-Z0-9]+)([a-zA-Z0-9_]*)\\.(jpg|mp4){1}$";
			
			entry.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaColumns._ID));
			cursor.close();
			
			if(!isThumbnail) {
				IDCIMEntry clone = new IDCIMEntry(entry);

				if(!shortDescription.contains(clone)) {
					shortDescription.add(clone);
				}
			}
			
			entry.exif = new IExif();
		
			if (InformaService.getInstance() != null && InformaService.getInstance().getCurrentLocation() != null)
			{
				entry.exif.location = InformaService.getInstance().getCurrentLocation().geoCoordinates;
			}


			List<IDCIMEntry> intakeList = new ArrayList<IDCIMEntry>();
			intakeList.add(entry);
						
			InformaCam informaCam = InformaCam.getInstance();

			Intent intakeIntent = new Intent(informaCam, Intake.class);

			
			if (InformaService.getInstance() != null)
			{
				List<String> cacheFiles = InformaService.getInstance().getCacheFiles();
				intakeIntent.putExtra(Codes.Extras.INFORMA_CACHE, cacheFiles.toArray(new String[cacheFiles.size()]));

				Intent intentSuckers = new Intent(informaCam, InformaService.class);
				intentSuckers.setAction(InformaService.ACTION_RESET_CACHE);
				informaCam.startService(intentSuckers);
			}
			
			intakeIntent.putExtra(Codes.Extras.RETURNED_MEDIA, new IDCIMSerializable(intakeList));
			intakeIntent.putExtra(Codes.Extras.TIME_OFFSET, timeOffset);
			if(parentId != null) {
				intakeIntent.putExtra(Codes.Extras.MEDIA_PARENT, parentId);
			}

			informaCam.startService(intakeIntent);
			
			
		}
		else
		{
			
			
			if (intakeMasterList.containsKey(path))
			{
				return; //we got it already
			}
			else
				intakeMasterList.put(path, entry);
			
			entry.fileAsset = new IAsset(path, sourceType);

			if(!isThumbnail) {
				entry.timeCaptured = new java.util.Date().getTime();
				if(entry.timeCaptured < startTime) {
					Logger.d(LOG, "this media occured too early to count");
					cursor.close();

					return;
				}

				entry.mediaType = MimeType.IMAGE;
				if (path.endsWith("mp4")||path.endsWith("ts")||path.endsWith("mov"))
					entry.mediaType = MimeType.VIDEO_MP4;
				else if (path.endsWith("3gp"))
					entry.mediaType = MimeType.VIDEO_3GPP;
					
				entry.cameraComponent = cameraComponent;

				
			}

			if(!isThumbnail) {
				IDCIMEntry clone = new IDCIMEntry(entry);

				if(!shortDescription.contains(clone)) {
					shortDescription.add(clone);
				}
			}
			
			entry.exif = new IExif();
		
			if (InformaService.getInstance() != null && InformaService.getInstance().getCurrentLocation() != null)
			{
				entry.exif.location = InformaService.getInstance().getCurrentLocation().geoCoordinates;
			}


			List<IDCIMEntry> intakeList = new ArrayList<IDCIMEntry>();
			intakeList.add(entry);
						
			InformaCam informaCam = InformaCam.getInstance();

			Intent intakeIntent = new Intent(informaCam, Intake.class);

			intakeIntent.putExtra(Codes.Extras.RETURNED_MEDIA, new IDCIMSerializable(intakeList));
						
			List<String> cacheFiles = InformaService.getInstance().getCacheFiles();
			intakeIntent.putExtra(Codes.Extras.INFORMA_CACHE, cacheFiles.toArray(new String[cacheFiles.size()]));			

			Intent intentSuckers = new Intent(informaCam, InformaService.class);
			intentSuckers.setAction(InformaService.ACTION_RESET_CACHE);
			informaCam.startService(intentSuckers);
			
			intakeIntent.putExtra(Codes.Extras.TIME_OFFSET, timeOffset);
			if(parentId != null) {
				intakeIntent.putExtra(Codes.Extras.MEDIA_PARENT, parentId);
			}

			informaCam.startService(intakeIntent);
		}
		
		
	}

	public void startSession() {
		InformaCam informaCam = InformaCam.getInstance();
		timeOffset = InformaService.getInstance().getTimeOffset();
		
		Logger.d(LOG, "starting dcim session");
	}

	public void stopSession() {
		// start up intake queue
		/*
		if(!intakeList.isEmpty()) {
			InformaCam informaCam = InformaCam.getInstance();

			Intent intakeIntent = new Intent(informaCam, Intake.class);

			intakeIntent.putExtra(Codes.Extras.RETURNED_MEDIA, new IDCIMSerializable(intakeList));
			
			List<String> cacheFiles = InformaService.getInstance().getCacheFiles();
			intakeIntent.putExtra(Codes.Extras.INFORMA_CACHE, cacheFiles.toArray(new String[cacheFiles.size()]));
			
			intakeIntent.putExtra(Codes.Extras.TIME_OFFSET, timeOffset);
			if(parentId != null) {
				intakeIntent.putExtra(Codes.Extras.MEDIA_PARENT, parentId);
			}

			informaCam.startService(intakeIntent);
			Logger.d(LOG, "saved a dcim descriptor");
		} else {
			Logger.d(LOG, "there were no entries.");
		}*/
	}

	public static class IDCIMSerializable extends Model implements Serializable {
		private static final long serialVersionUID = 3688700992408456583L;
		
		public List<IDCIMEntry> dcimList;

		public IDCIMSerializable(List<IDCIMEntry> dcimList) {
			super();
			this.dcimList = dcimList;
		}
	}
}