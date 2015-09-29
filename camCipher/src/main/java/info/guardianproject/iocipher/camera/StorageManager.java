/**
 * 
 * This file contains code from the IOCipher Camera Library "CipherCam".
 *
 * For more information about IOCipher, see https://guardianproject.info/code/iocipher
 * and this sample library: https://github.com/n8fr8/IOCipherCameraExample
 *
 * IOCipher Camera Sample is distributed under this license (aka the 3-clause BSD license)
 *
 * @author n8fr8
 * 
 */

package info.guardianproject.iocipher.camera;

import info.guardianproject.iocipher.VirtualFileSystem;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class StorageManager {

	private final static String DEFAULT_PATH = "gallery.db";
	
	public static boolean isStorageMounted ()
	{
		return VirtualFileSystem.get().isMounted();
	}
	
	public static boolean unmountStorage ()
	{
		try
		{
			VirtualFileSystem.get().unmount();
			return true;
		}
		catch (IllegalStateException ise)
		{
			Log.d("IOCipher","error unmounting - still active?",ise);
			return false;
		}
	}
	
	public static boolean mountStorage (Context context, String storagePath, byte[] passphrase)
	{
		File dbFile = null;
		
		if (storagePath == null)
		{
			dbFile = new java.io.File(context.getDir("vfs", Context.MODE_PRIVATE),DEFAULT_PATH);
		}
		else
		{
			dbFile = new java.io.File(storagePath);
		}
		dbFile.getParentFile().mkdirs();
		
		if (!dbFile.exists())
			VirtualFileSystem.get().createNewContainer(dbFile.getAbsolutePath(), passphrase);
		

		if (!VirtualFileSystem.get().isMounted())
		{
			// TODO don't use a hard-coded password! prompt for the password
			VirtualFileSystem.get().mount(dbFile.getAbsolutePath(),passphrase);
		}
		
		
		return true;
	}
	
	public static java.io.File exportToDisk (info.guardianproject.iocipher.File fileIn) throws IOException
	{
		java.io.File fileOut = null;
		
		fileOut = new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),fileIn.getName());
		info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(fileIn);		
		java.io.FileOutputStream fos = new java.io.FileOutputStream(fileOut);
		
		byte[] b = new byte[4096];
		int len;
		while ((len = fis.read(b))!=-1)
		{
			fos.write(b, 0, len);
		}
		
		fis.close();
		fos.flush();
		fos.close();
		
		return fileOut;
		
	}
}
