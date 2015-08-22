package org.witness.informacam.storage;

import java.io.File;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

public class InformaCamMediaScanner implements MediaScannerConnectionClient {	
	private MediaScannerConnection msc;
	private File f;
	private Context context;
	
	private OnMediaScannedListener mListener;
	
	public interface OnMediaScannedListener {
		public void onMediaScanned(Uri uri);
	}
	
	public InformaCamMediaScanner(Context context, File f, OnMediaScannedListener listener) {
		this.f = f;
		this.context = context;
		
		mListener = listener;
		msc = new MediaScannerConnection(this.context, this);
		msc.connect();
	}
	
	@Override
	public void onMediaScannerConnected() {
		msc.scanFile(f.getAbsolutePath(), null);
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		mListener.onMediaScanned(uri);
	}
	
	public static Uri getUriFromFile(Context context, File file) {
		Uri uri = null;
		
		ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] {BaseColumns._ID}, MediaStore.Images.Media.DATA + "=?", new String[] {file.getAbsolutePath()}, null);
		if(c != null && c.moveToFirst()) {
			uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(c.getLong(c.getColumnIndex(BaseColumns._ID))));
			c.close();
		}
		return uri;
	}
	
	public static void doScanForDeletion(final Context c, final File file) {
		MediaScannerConnection.scanFile(c, new String[] {file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
			
			@Override
			public void onScanCompleted(String path, Uri uri) {
				file.delete();
				c.getContentResolver().delete(uri, null, null);
			}
		});
	}

}