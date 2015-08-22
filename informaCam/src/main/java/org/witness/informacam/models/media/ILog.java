package org.witness.informacam.models.media;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileWriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.spongycastle.openpgp.PGPException;
import org.witness.informacam.InformaCam;
import org.witness.informacam.R;
import org.witness.informacam.crypto.EncryptionUtility;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.models.j3m.IDCIMEntry;
import org.witness.informacam.models.notifications.INotification;
import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.models.transport.ITransportStub;
import org.witness.informacam.storage.IOUtility;
import org.witness.informacam.transport.TransportUtility;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.Constants.Models.IMedia.MimeType;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;

public class ILog extends IMedia {
	public long autoLogInterval = 10 * (60 * 1000);	// 10 minutes?
	public boolean shouldAutoLog = false;

	public long startTime = 0L;
	public long endTime = 0L;

	public List<String> attachedMedia = new ArrayList<String>();
	
	private Map<String, InputStream> j3mZip;
	
	public ILog() {
		super();
		
		_id = generateId("log_" + System.currentTimeMillis());		
		
		dcimEntry = new IDCIMEntry();
		dcimEntry.mediaType = MimeType.LOG;
		
		info.guardianproject.iocipher.File rootFolder = new info.guardianproject.iocipher.File(_id);
		if(!rootFolder.exists()) {
			rootFolder.mkdir();
		}

		this.rootFolder = rootFolder.getAbsolutePath();
	}

	public ILog(IMedia media) throws InstantiationException, IllegalAccessException {
		super();
		
		inflate(media.asJson());
	}
	
	public java.io.File sealLog(boolean doLocalShare, IOrganization organization, INotification notification) throws IOException, NoSuchProviderException, PGPException, InstantiationException, IllegalAccessException {
		InformaCam informaCam = InformaCam.getInstance();
		
		java.io.File log = null;
		
		
		// zip up everything, encrypt if required
		String logName = ("log_" + System.currentTimeMillis() + ".zip");
		
		if(doLocalShare) {

			log = new java.io.File(Storage.EXTERNAL_DIR, logName);
			IOUtility.zipFiles(j3mZip, log.getAbsolutePath(), Type.FILE_SYSTEM);
			

			if(organization != null) {
				
				//we can't read form a file and write to it simultaneously
				java.io.File logEncrypted = new java.io.File(Storage.EXTERNAL_DIR, logName + ".pgp"); //add pgp encryption extension
				EncryptionUtility.encrypt(informaCam.ioService.getStream(log.getAbsolutePath(), Type.FILE_SYSTEM), new FileOutputStream(logEncrypted),Base64.encode(informaCam.ioService.getBytes(organization.publicKey, Type.IOCIPHER), Base64.DEFAULT));
				
				log.delete();//delete unecrypted export
				
				log = logEncrypted;
				
			}
			


		} else {
			log = new info.guardianproject.iocipher.File(rootFolder, logName);
			IOUtility.zipFiles(j3mZip, log.getAbsolutePath(), Type.IOCIPHER);

			if(organization != null) {

				info.guardianproject.iocipher.File logEncrypted = new info.guardianproject.iocipher.File(rootFolder, logName + ".pgp"); //add pgp encryption extension
				
				OutputStream os = new info.guardianproject.iocipher.FileOutputStream(logEncrypted.getAbsolutePath());
				os = new Base64OutputStream(os, Base64.DEFAULT);
				
				EncryptionUtility.encrypt(informaCam.ioService.getStream(log.getAbsolutePath(), Type.IOCIPHER), os,Base64.encode(informaCam.ioService.getBytes(organization.publicKey, Type.IOCIPHER), Base64.DEFAULT));

				os.flush();
				os.close();
				
				log.delete();
				
				log = logEncrypted;
	
				ITransportStub submission = new ITransportStub(organization, notification);
				submission.setAsset(log.getName(), log.getAbsolutePath(), MimeType.LOG, Storage.Type.IOCIPHER);
				
				
				TransportUtility.initTransport(submission);
			}
		}
		
		reset();
		
		return log;
	}
	
	
	@Override
	public IAsset export(final Context context, Handler h, final IOrganization organization, final boolean includeSensorLogs, final boolean isLocalShare, final boolean doSubmission)
			throws FileNotFoundException, InstantiationException, IllegalAccessException {
		
		InformaCam informaCam = InformaCam.getInstance();
		
		j3mZip = new HashMap<String, InputStream>();

		final INotification notification = new INotification();

		int progress = 0;

		// append its data sensory data, form data, etc.
		mungeData();
		
		if (includeSensorLogs)
			mungeSensorLogs(h);
		
		progress += 5;
		sendMessage(Codes.Keys.UI.PROGRESS, progress, h);
		
		mungeGenealogyAndIntent();
		genealogy.dateCreated = this.startTime;
		progress += 5;
		sendMessage(Codes.Keys.UI.PROGRESS, progress, h);

		notification.label = context.getString(R.string.export);
		notification.mediaId = this._id;
		notification.content = context.getString(R.string.you_exported_this_x, "log");
		if(organization != null) {
			intent.intendedDestination = organization.organizationName;
			notification.content = context.getString(R.string.you_exported_this_x_to_x, "log", organization.organizationName);
		}
		progress += 5;
		sendMessage(Codes.Keys.UI.PROGRESS, progress, h);

		JSONObject j3mObject = null;
		try {
			j3mObject = new JSONObject();
			JSONObject j3m = new JSONObject();
			
			j3m.put(Models.IMedia.j3m.DATA, data.asJson());
			j3m.put(Models.IMedia.j3m.GENEALOGY, genealogy.asJson());
			j3m.put(Models.IMedia.j3m.INTENT, intent.asJson());
			
			info.guardianproject.iocipher.File fileTmp = new info.guardianproject.iocipher.File("tmp-export");
			info.guardianproject.iocipher.FileWriter fwTmp = new info.guardianproject.iocipher.FileWriter(fileTmp);
			j3m.write(fwTmp);
			fwTmp.flush();
			fwTmp.close();
			
			ByteArrayOutputStream bSig = new ByteArrayOutputStream();
			informaCam.signatureService.signData(new info.guardianproject.iocipher.FileInputStream(fileTmp),bSig);
			fileTmp.delete();
			
			j3mObject.put(Models.IMedia.j3m.SIGNATURE, new String(bSig.toByteArray()));
			j3mObject.put(Models.IMedia.j3m.J3M, j3m);

			fwTmp = new info.guardianproject.iocipher.FileWriter(fileTmp);
			j3mObject.write(fwTmp);
			fwTmp.flush();
			fwTmp.close();
			
			j3mZip.put("log.j3m", new info.guardianproject.iocipher.FileInputStream(fileTmp));

			progress += 5;
			sendMessage(Codes.Keys.UI.PROGRESS, progress, h);

			notification.generateId();
			notification.taskComplete = false;

			informaCam.addNotification(notification, h);

			if(attachedMedia != null && attachedMedia.size() > 0) {
				data.attachments = getAttachedMediaIds();
				
				int progressIncrement = (int) (50/(attachedMedia.size() * 2));
	
				boolean doMediaSubmission = false; //don't submit media individually
				boolean includeMediaSensorLogs = false; //we have all the sensor data in the main log
				
				for(final String s : attachedMedia) {
					
					IMedia m = InformaCam.getInstance().mediaManifest.getById(s);
					
					if(m.associatedCaches == null) {
						m.associatedCaches = new ArrayList<String>();
					}
					
					if (associatedCaches != null)
						m.associatedCaches.addAll(associatedCaches);
					
					try {
						IAsset assetExport = m.export(context, h, organization, includeMediaSensorLogs, isLocalShare, doMediaSubmission);
						
						if (assetExport.source == Type.IOCIPHER)
						{
							info.guardianproject.iocipher.File fileMedia = new info.guardianproject.iocipher.File(assetExport.path);
							j3mZip.put(fileMedia.getName(),new info.guardianproject.iocipher.FileInputStream(fileMedia));
						}
						else if (assetExport.source == Type.FILE_SYSTEM)
						{							
							File fileMedia = new File(assetExport.path);
							j3mZip.put(fileMedia.getName(),new FileInputStream(fileMedia));
						}
						
					} catch (FileNotFoundException e) {
						Logger.e(App.LOG,e);
					}
					
					progress += progressIncrement;
					sendMessage(Codes.Keys.UI.PROGRESS, progress, h);
					
				}
			} 
			
			java.io.File fileExport = sealLog(isLocalShare, organization, notification);
			
			if (h != null)
			{
				Message msgExportComplete = new Message();
				msgExportComplete.what = 999;
				msgExportComplete.getData().putString("file", fileExport.getAbsolutePath());
				msgExportComplete.getData().putBoolean("localShare", isLocalShare);
				h.sendMessage(msgExportComplete);
			}

			IAsset assetExport = new IAsset();
			assetExport.path = fileExport.getAbsolutePath();
			assetExport.source = Type.FILE_SYSTEM;
			return assetExport;
		
		} catch(Exception e) {
			Log.e(LOG, e.toString(),e);
			return null;
		}
		

	}
	
	private List<String> getAttachedMediaIds() {
		return attachedMedia;
	}

}
