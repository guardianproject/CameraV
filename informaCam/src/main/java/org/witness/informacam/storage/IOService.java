package org.witness.informacam.storage;

import info.guardianproject.iocipher.VirtualFileSystem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.witness.informacam.InformaCam;
import org.witness.informacam.intake.DCIMObserver;
import org.witness.informacam.models.Model;
import org.witness.informacam.models.j3m.IDCIMDescriptor;
import org.witness.informacam.models.media.IAsset;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.InformaCamEventListener;
import org.witness.informacam.utils.Constants.Models;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

public class IOService {
	
	private VirtualFileSystem vfs = null;
	private DCIMObserver dcimObserver = null;
	private List<java.io.File> cleanupQueue = new Vector<java.io.File>();

	private final static String LOG = App.Storage.LOG;
	
	private Context mContext = null;
	
	public IOService (Context context)
	{
		mContext = context;
	}

	public void onDestroy() {

		if(vfs != null) {
			vfs.unmount();
		}
		
		for(java.io.File f : cleanupQueue) {
			Log.d(LOG, "removing unsafe file: " + f.getAbsolutePath());
			f.delete();
		}
	}
	
	public boolean saveBlob(InputStream data, IAsset asset) throws IOException {
		if(asset.source == Type.IOCIPHER) {
			return saveBlob(data, new info.guardianproject.iocipher.File(asset.path));
		} else {
			return saveBlob(data, new java.io.File(asset.path), true);
		}
	}
	
	public boolean saveBlob(byte[] data, IAsset asset) throws IOException { 
		if(asset.source == Type.IOCIPHER) {
			return saveBlob(data, new info.guardianproject.iocipher.File(asset.path));
		} else {
			return saveBlob(data, new java.io.File(asset.path), true);
		}
	}

	public boolean saveBlob(byte[] data, java.io.File file, boolean isPublic)  throws IOException {
		if(!isPublic) {
			return saveBlob(data, file);
		} else {
			try {
				java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
				fos.write(data);
				fos.flush();
				fos.close();
				
				return true;

			} catch (FileNotFoundException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}

			return false;
		}
	}
	
	public boolean saveBlob(InputStream data, java.io.File file, boolean isPublic) throws IOException {
		if(!isPublic) {
			return saveBlob(data, file);
		} else {
			try {
				java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
				
				IOUtils.copyLarge(data, fos);
				
				fos.flush();
				fos.close();
				
				return true;
			} catch (FileNotFoundException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
			
			return false;
		}
	}
	
	public boolean saveBlob(byte[] data, java.io.File file)  throws IOException {
		return saveBlob(data, file, null);
	}
	
	public boolean saveBlob(InputStream data, java.io.File file) throws IOException {
		return saveBlob(data, file, null);
	}
	
	public boolean saveBlob(InputStream data, java.io.File file, String uriToDelete) throws IOException {
		java.io.FileOutputStream fos = mContext.openFileOutput(file.getName(), mContext.MODE_PRIVATE);		
		
		IOUtils.copyLarge(data, fos);
		
		fos.flush();
		fos.close();
		
		if(uriToDelete != null) {
			mContext.getContentResolver().delete(Uri.parse(uriToDelete), null, null);
		}
		
		return true;
	}

	public boolean saveBlob(byte[] data, java.io.File file, String uriToDelete) throws IOException {		
		java.io.FileOutputStream fos = mContext.openFileOutput(file.getName(), mContext.MODE_PRIVATE);
		
		IOUtils.write(data,  fos);
		
		fos.flush();
		fos.close();
		
		if(uriToDelete != null) {
			mContext.getContentResolver().delete(Uri.parse(uriToDelete), null, null);
		}
		
		return true;

	}
	
	public boolean saveBlob(byte[] data, info.guardianproject.iocipher.File file) {
		
		try
		{
			return saveBlob(data, file, null);
		}
		catch (IOException ioe)
		{
			Log.e(LOG,"iocipher saveState() error",ioe);
			
			return false;
		}
		
	}

	public boolean saveBlob(byte[] data, info.guardianproject.iocipher.File file, String uri) throws IOException {

		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		return saveBlob (bais, file, uri);
		
	}
	
	public boolean saveBlob(InputStream data, info.guardianproject.iocipher.File file) throws IOException {
		return saveBlob(data, file, null);
	}
	
	public boolean saveBlob(InputStream is, info.guardianproject.iocipher.File file, String uri) throws IOException {

		if(vfs == null) {
			// TODO: this should throw exception
			return false;
		}
		
		info.guardianproject.iocipher.FileOutputStream fos = new info.guardianproject.iocipher.FileOutputStream(file);
		
		IOUtils.copyLarge(is,  fos);
		
		if(uri != null) {
			mContext.getContentResolver().delete(Uri.parse(uri), null, null);
		}
		
		return true;

		
	}

	public boolean saveBlob(Model model, java.io.File file)  throws IOException {
		return saveBlob(model.asJson().toString().getBytes(), file);
	}

	public boolean saveBlob(Model model, info.guardianproject.iocipher.File file) throws IOException {
		return saveBlob(model.asJson().toString().getBytes(), file);
	}
	
	public boolean isAvailable(String pathToData, int source) {
		Object file = null;
		switch(source) {
		case Storage.Type.IOCIPHER:
			
			file = new info.guardianproject.iocipher.File(pathToData);
			
			if(((info.guardianproject.iocipher.File) file).length() > 0) {
				return true;
			}
			
			break;
		case Storage.Type.FILE_SYSTEM:
			file = new java.io.File(pathToData);
			
			if(((java.io.File) file).length() > 0) {
				return true;
			}
			
			break;
		}
		
		return false;
	}
	
	public byte[] getBytes(IAsset asset) {
		return getBytes(asset.path, asset.source);
	}
	
	public byte[] getBytes(String pathToData, int source) {
		byte[] bytes = new byte[0];

		switch(source) {
		case Storage.Type.INTERNAL_STORAGE:
			java.io.FileInputStream fis;
			
			try {
				fis = mContext.openFileInput(pathToData);
				bytes = new byte[fis.available()];
				fis.read(bytes);
				fis.close();
			} catch (FileNotFoundException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}

			break;
		case Storage.Type.IOCIPHER:
			
			if(vfs == null) {
				return null;
				/*
				Log.d(LOG, "also, VFS IS NULL SO...");
				
				InformaCam informaCam = (InformaCam)getApplication();
				
				if(!informaCam.attemptLogin()) {
					informaCam.promptForLogin();
					return null;
				}*/
			}
			
			info.guardianproject.iocipher.FileInputStream iFis;
			info.guardianproject.iocipher.File file = new info.guardianproject.iocipher.File(pathToData);
			
			try {
				iFis = new info.guardianproject.iocipher.FileInputStream(file);
				bytes = new byte[iFis.available()];
				iFis.read(bytes);
				iFis.close();
			} catch (FileNotFoundException e) {
				Log.d(LOG, "no, no bytes (" + pathToData + ")");
				return null;
			} catch (IOException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			} catch (Exception e) {
				Log.e(LOG, e.toString());
			}

			break;
		case Storage.Type.APPLICATION_ASSET:
			try {
				InputStream is = mContext.getAssets().open(pathToData, Context.MODE_PRIVATE);
				bytes = new byte[is.available()];
				is.read(bytes);
				is.close();
			} catch (IOException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
			break;
		case Storage.Type.CONTENT_RESOLVER:
			break;
		case Storage.Type.FILE_SYSTEM:
			try {
				java.io.File file_ = new java.io.File(pathToData);
				java.io.FileInputStream fis_ = new java.io.FileInputStream(file_);
				bytes = new byte[fis_.available()];
				fis_.read(bytes);
				fis_.close();
			} catch (FileNotFoundException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
			break;
		}

	//	Log.d(LOG, "(" + pathToData + ") bytes here: " + bytes.length);
		return bytes;
	}
	
	
	public InputStream getStream(IAsset asset) throws IOException {
		return getStream(asset.path, asset.source);
	}
	
	public InputStream getStream(String pathToData, int source) throws IOException {

		InputStream is = null;
		
		switch(source) {
		case Storage.Type.INTERNAL_STORAGE:
			java.io.FileInputStream fis;
			
			try {
				fis = mContext.openFileInput(pathToData);
				is = fis;
			} catch (FileNotFoundException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}

			break;
		case Storage.Type.IOCIPHER:
			
			if(vfs == null) {
				return null;
				/*
				Log.d(LOG, "also, VFS IS NULL SO...");
				
				InformaCam informaCam = (InformaCam)getApplication();
				
				if(!informaCam.attemptLogin()) {
					informaCam.promptForLogin();
					return null;
				}*/
			}
			
			info.guardianproject.iocipher.File file = new info.guardianproject.iocipher.File(pathToData);
			
			try {
				is = new info.guardianproject.iocipher.FileInputStream(file);
				//Log.d(LOG, "getting Stream (" + pathToData + ") available: " + is.available());
			} catch (FileNotFoundException e) {
				//Log.d(LOG, "no, no bytes (" + pathToData + ")");
				return null;
			} catch (Exception e) {
				//Log.e(LOG, e.toString());
			}

			break;
		case Storage.Type.APPLICATION_ASSET:
			try {
				is = mContext.getAssets().open(pathToData, Context.MODE_PRIVATE);
				
			} catch (IOException e) {
				Log.e(LOG, e.toString(),e);
			}
			break;
		case Storage.Type.CONTENT_RESOLVER:
			break;
		case Storage.Type.FILE_SYSTEM:
		
			java.io.File file_ = new java.io.File(pathToData);
			is = new java.io.FileInputStream(file_);
		
			break;
		}

		return is;
	}

	public long getLength(String pathToData, int source) {

		long fileLength = -1;
		
		switch(source) {
		case Storage.Type.INTERNAL_STORAGE:
			java.io.File fileData;
			
				fileData = new java.io.File(pathToData);
				fileLength = fileData.length();
			

			break;
		case Storage.Type.IOCIPHER:
			
			if(vfs == null) {
				return -1;
				/*
				Log.d(LOG, "also, VFS IS NULL SO...");
				
				InformaCam informaCam = (InformaCam)getApplication();
				
				if(!informaCam.attemptLogin()) {
					informaCam.promptForLogin();
					return null;
				}*/
			}
			
				info.guardianproject.iocipher.File file = new info.guardianproject.iocipher.File(pathToData);
				fileLength = file.length();
			

			break;
		case Storage.Type.APPLICATION_ASSET:
			
				java.io.File fileAsset = new java.io.File(pathToData);
				fileLength = fileAsset.length();
			
			break;
		case Storage.Type.CONTENT_RESOLVER:
			break;
		case Storage.Type.FILE_SYSTEM:
			
				java.io.File file_ = new java.io.File(pathToData);
				fileLength = file_.length();
			
			break;
		}

		return fileLength;
	}

	public java.io.File getPublicCredentials() {
		byte[] publicCredentialsBytes = getBytes(Models.IUser.PUBLIC_CREDENTIALS, Type.IOCIPHER);
		if(publicCredentialsBytes != null) {
			try {
				java.io.File externalDir = new java.io.File(Storage.EXTERNAL_DIR);
				if(!externalDir.exists()) {
					externalDir.mkdir();
				}
				
				java.io.File publicCredentials = new java.io.File(Storage.EXTERNAL_DIR, "publicCredentials.zip");
				java.io.FileOutputStream fis = new java.io.FileOutputStream(publicCredentials);
				
				fis.write(publicCredentialsBytes);
				fis.flush();
				fis.close();
				return publicCredentials;
			} catch (FileNotFoundException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
		}
		
		return null;
	}

	public boolean initIOCipher(byte[] authToken) {
		try {
			java.io.File fileExternal = mContext.getExternalFilesDir(null);//Storage.ROOT);
			
			if (fileExternal == null)
			{
				fileExternal = mContext.getFilesDir();				
			}
			
			java.io.File storageRoot = new java.io.File(fileExternal, Storage.IOCIPHER);
			
			vfs = VirtualFileSystem.get();
			
			if (!storageRoot.exists())			
				vfs.createNewContainer(storageRoot.getAbsolutePath(), authToken);							
			
			if (!vfs.isMounted())
				vfs.mount(storageRoot.getAbsolutePath(),authToken);
			
			info.guardianproject.iocipher.File organizationRoot = new info.guardianproject.iocipher.File(Storage.ORGS_ROOT);
			if(!organizationRoot.exists()) {
				organizationRoot.mkdir();
			}
			
			info.guardianproject.iocipher.File formsRoot = new info.guardianproject.iocipher.File(Storage.FORM_ROOT);
			if(!formsRoot.exists()) {
				formsRoot.mkdir();
			}

			return true;
		} catch(IllegalArgumentException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}

		return false;
	}
	
	public boolean clear(String pathToDirectory, int source) {
		List<String> files = new ArrayList<String>();
		
		switch(source) {
		case Storage.Type.IOCIPHER:
			for(info.guardianproject.iocipher.File file : ((info.guardianproject.iocipher.File) new info.guardianproject.iocipher.File(pathToDirectory)).listFiles()) {
				files.add(file.getAbsolutePath());
			}
			
			break;
		}
		
		for(String file : files) {
			delete(file, source);
		}
		
		
		
		return false;
	}
	
	public boolean delete(IAsset asset) {
		return delete(asset.path, asset.source);
	}

	public boolean delete(String pathToFile, int source) {
		// TODO: MAKE SURE FILE IS OBLITERATED!
		
		switch(source) {
		case Storage.Type.INTERNAL_STORAGE:
			return mContext.deleteFile(pathToFile);
		case Storage.Type.IOCIPHER:
			info.guardianproject.iocipher.File file_ioc = new info.guardianproject.iocipher.File(pathToFile);
			if(file_ioc.isDirectory()) {
				for(info.guardianproject.iocipher.File f : file_ioc.listFiles()) {
					f.delete();
				}
			}
			
			return file_ioc.delete();
		case Storage.Type.CONTENT_RESOLVER:
			return mContext.getContentResolver().delete(Uri.parse(pathToFile), null, null) > 0 ? true : false;
		case Storage.Type.FILE_SYSTEM:
			java.io.File file_jif = new java.io.File(pathToFile);
			if(file_jif.isDirectory()) {
				for(java.io.File f : file_jif.listFiles()) {
					f.delete();
				}
			}
			
			return file_jif.delete();
		default:
			return false;
		}

	}

	public void unmount() throws IllegalStateException {
		
		if (vfs != null)
			vfs.unmount();
	}
	
	public void startDCIMObserver(InformaCamEventListener listener, String parentId, ComponentName cameraComponent) {
		//InformaCam.getInstance().mediaManifest.setAllAsOld();
		dcimObserver = new DCIMObserver(mContext, parentId, cameraComponent);
	}
	
	public IDCIMDescriptor getDCIMDescriptor() {
		return dcimObserver.dcimDescriptor;
	}
	
	public void stopDCIMObserver() {
		Handler h = new Handler();
		h.post(new Runnable() {
			@Override
			public void run() {
				dcimObserver.destroy();
			}
		});
	}

	public boolean isMounted() {
		if(vfs != null) {
			return vfs.isMounted();
		}
		
		return false;
	}
}
