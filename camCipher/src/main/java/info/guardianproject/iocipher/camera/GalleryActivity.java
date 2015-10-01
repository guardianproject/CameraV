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

import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;
import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileInputStream;
import info.guardianproject.iocipher.FileOutputStream;
import info.guardianproject.iocipher.camera.io.IOCipherContentProvider;
import info.guardianproject.iocipher.camera.io.PgpHelper;
import info.guardianproject.iocipher.camera.viewer.ImageViewerActivity;
import info.guardianproject.iocipher.camera.viewer.MjpegViewerActivity;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import org.spongycastle.openpgp.PGPPublicKey;

public class GalleryActivity extends Activity  implements ICacheWordSubscriber {
	private final static String TAG = "FileBrowser";

	private List<String> item = null;
	private List<String> path = null;
	private String[] items;
	private java.io.File dbFile;
	private String root = "/";
	
	private GridView gridview;
	private HashMap<String,Bitmap> mBitCache = new HashMap<String,Bitmap>();
	private HashMap<String,BitmapWorkerThread> mBitLoaders = new HashMap<String,BitmapWorkerThread>();
	
	private CacheWordHandler mCacheWord;
	
	private final static int REQUEST_TAKE_PICTURE = 1000;
	private final static int REQUEST_TAKE_VIDEO = 1001;
	
	private final static String ACTION_SECURE_STILL_IMAGE_CAMERA = "info.guardianproject.action.SECURE_STILL_IMAGE_CAMERA";
	private final static String ACTION_SECURE_SECURE_VIDEO_CAMERA = "info.guardianproject.action.SECURE_VIDEO_CAMERA";
	
	private Handler h = new Handler();//for UI event handling
	
	private boolean mUseBuiltInLockScreen = true;
	private boolean isExternalLaunch = false;


    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }


    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//prevent screenshots
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
				WindowManager.LayoutParams.FLAG_SECURE);

		setContentView(R.layout.activity_gallery);
		
		gridview = (GridView) findViewById(R.id.gridview);

        mCacheWord = new CacheWordHandler(this, this);
        mCacheWord.connectToService(); 
        
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if (intent.hasExtra(Intent.EXTRA_STREAM)) {
				Log.i(TAG, "save extra stream URI");
				handleSendUri((Uri) intent.getExtras().get(Intent.EXTRA_STREAM));
			} else {
				Log.i(TAG, "save data");
				handleSendUri(intent.getData());
			}
		}
		else if (MediaStore.ACTION_IMAGE_CAPTURE.equals(action)
				|| ACTION_SECURE_STILL_IMAGE_CAMERA.equals(action)
				)
		{
			//REQUEST_TAKE_PICTURE

			Intent intentCapture = new Intent(this,StillCameraActivity.class);
			intentCapture.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			intentCapture.putExtra("basepath", "/");
			intentCapture.putExtra("selfie", false);
			startActivityForResult(intentCapture, REQUEST_TAKE_PICTURE);
			isExternalLaunch = true;
		}		
		else if (MediaStore.ACTION_VIDEO_CAPTURE.equals(action)
				|| ACTION_SECURE_SECURE_VIDEO_CAMERA.equals(action)
				)
		{
			//REQUEST_TAKE_VIDEO
			Intent intentCapture = new Intent(this,VideoCameraActivity.class);
			intentCapture.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
			intentCapture.putExtra("basepath", "/");
			intentCapture.putExtra("selfie", false);
			startActivityForResult(intentCapture, REQUEST_TAKE_VIDEO);
			isExternalLaunch = true;
			
		}
		
		setIntent(null);
		
	}
	
	 
	  @Override
	   public void onConfigurationChanged(Configuration newConfig) {
		  
		  	
	        super.onConfigurationChanged(newConfig);

	   }
	 
	
	protected void onResume() {
		super.onResume();
		
		if (!StorageManager.isStorageMounted())
		{
			goToLockScreen ();
		}
		else
		{
			mCacheWord.reattach();
			
		}
		
	}

	@Override
	public void onCacheWordLocked() {
	
		if (StorageManager.isStorageMounted())
		{
			//if storage is mounted, then we should lock it
			boolean unmounted = StorageManager.unmountStorage();
		}
		
		goToLockScreen ();
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PICTURE)
        {
        	 if (resultCode == RESULT_OK)
             {
	        	String[] ioCipherFile = data.getExtras().getStringArray(MediaStore.EXTRA_OUTPUT);
	        	
	        	if (ioCipherFile != null && ioCipherFile.length > 0)
	        	{
	        		String sharePath = IOCipherContentProvider.addShare(ioCipherFile[0], IOCipherContentProvider.DEFAULT_AUTHORITY);
		        	Uri uri = Uri.parse(sharePath);	        		        
					String mimeType = "image/*";				
					data.setDataAndType(uri, mimeType);
					data.putExtra(Intent.EXTRA_STREAM, uri);					
					data.putExtra(MediaStore.EXTRA_OUTPUT, ioCipherFile);

					setResult(resultCode,data);	
	        	}
             }
        	
        }
        else if (requestCode == REQUEST_TAKE_VIDEO)
        {
        	 if (resultCode == RESULT_OK)
             {
	        	String[] ioCipherFile = data.getExtras().getStringArray(MediaStore.EXTRA_OUTPUT);
	        	
	        	if (ioCipherFile != null && ioCipherFile.length > 0)
	        	{
	        		String sharePath = IOCipherContentProvider.addShare(ioCipherFile[0], IOCipherContentProvider.DEFAULT_AUTHORITY);
		        	Uri uri = Uri.parse(sharePath);	  
					String mimeType = "video/*";				
					data.setDataAndType(uri, mimeType);
					data.putExtra(Intent.EXTRA_STREAM, uri);
					data.putExtra(MediaStore.EXTRA_OUTPUT, ioCipherFile);

		        	setResult(resultCode,data);
	        	}
             }
        	 
        }
        
        if (isExternalLaunch)
        	finish();
        else
        	getFileList(root);
        
        
	}

	@Override
	public void onCacheWordOpened() {

        mCacheWord.setTimeout(0);
		//great!
        getFileList(root);
	}

	@Override
	public void onCacheWordUninitialized() {
		
		goToLockScreen();
		
	}
	
	private void goToLockScreen ()
	{
		try
		{
			mCacheWord.disconnectFromService();
		}
		catch (IllegalArgumentException iae)
		{
			Log.d(TAG,"error disconnecting from cacheword service",iae);
		}
		
		
		if (mUseBuiltInLockScreen && (!isExternalLaunch))
		{
			Intent intent = new Intent(this,LockScreenActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
		}
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		

		mCacheWord.detach();
		
	}

	protected void onDestroy() {
		super.onDestroy();
		
		mCacheWord.disconnectFromService();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        
        return true;
	}
	

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	Intent intent = null;
    	
        int itemId = item.getItemId();
        
		if (itemId == R.id.menu_camera) {
			intent = new Intent(this,StillCameraActivity.class);
			intent.putExtra("basepath", "/");
			intent.putExtra("selfie", false);
			startActivityForResult(intent, 1);
			return true;
		} else if (itemId == R.id.menu_video) {
			intent = new Intent(this,VideoCameraActivity.class);
			intent.putExtra("basepath", "/");
			intent.putExtra("selfie", false);
			startActivityForResult(intent, 1);
			return true;
		} else if (itemId == R.id.menu_lock) {
			if (StorageManager.isStorageMounted())
    		{
    			//if storage is mounted, then we should lock it
    			boolean unmounted = StorageManager.unmountStorage();
    			
    			if (!unmounted)    			
    			{
    				Toast.makeText(this, "Storage is busy... cannot lock yet.",Toast.LENGTH_LONG).show();
    			}
    			else
    			{
    				mCacheWord.lock();
    			}
    		}
			return true;
		}	
        
		
        return false;
    }

	private void handleSendUri(Uri dataUri) {
		try {
			ContentResolver cr = getContentResolver();
			InputStream in = cr.openInputStream(dataUri);
			Log.i(TAG, "incoming URI: " + dataUri.toString());
			String fileName = dataUri.getLastPathSegment();
			File f = new File("/" + fileName);
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
			readBytesAndClose(in, out);
			Log.v(TAG, f.getAbsolutePath() + " size: " + String.valueOf(f.length()));
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	private void readBytesAndClose(InputStream in, OutputStream out)
			throws IOException {
		try {
			int block = 8 * 1024; // IOCipher works best with 8k blocks
			byte[] buff = new byte[block];
			while (true) {
				int len = in.read(buff, 0, block);
				if (len < 0) {
					break;
				}
				out.write(buff, 0, len);
			}
		} finally {
			in.close();
			out.flush();
			out.close();
		}
	}

	// To make listview for the list of file
	public void getFileList(String dirPath) {

		item = new ArrayList<String>();
		path = new ArrayList<String>();

		File file = new File(dirPath);
		File[] files = file.listFiles();

		if (!dirPath.equals(root)) {
			item.add(root);
			path.add(root);// to get back to main list

			item.add("..");
			path.add(file.getParent()); // back one level
		}

		for (int i = files.length-1; i >= 0; i--) {

			File fileItem = files[i];
			path.add(fileItem.getPath());
			if (fileItem.isDirectory()) {
				// input name directory to array list
				item.add("[" + fileItem.getName() + "]");
			} else {
				// input name file to array list
				item.add(fileItem.getName());
			}
		}
		
		// declare array with specific number of items
		items = new String[item.size()];
		// send data arraylist(item) to array(items)
		item.toArray(items);
	    gridview.setAdapter(new IconicList());

	    gridview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v,
	                int position, long id) {
	    
					File file = new File(path.get(position));
					
					if (file.isDirectory()) {
							if (file.canRead()) {
								getFileList(path.get(position));
							} else {
								//show error
				
							}
					} else {
						showItem (file);
					}
	        }
					
	     });
	    
	    gridview.setOnItemLongClickListener(new OnItemLongClickListener () {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				
				File file = new File(path.get(position));
				if (file.isDirectory()) {
					if (file.canRead()) {
						getFileList(path.get(position));
						return true;
					} else {
						//show error
		
					}
				} else {
					showItemDialog (file);
					return true;
				}
				
				return false;
			}
	    	
	    });
	    
	}
	
	private void showItemDialog (final File file)
	{
		
		new AlertDialog.Builder(GalleryActivity.this)
				.setTitle("[" + file.getName() + "]")
				.setNegativeButton("Delete",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
												int which) {

								file.delete();
								getFileList(root);
							}

						})
				.setNeutralButton("Time Campsule...", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog,
										int which) {

                        new PGPEncryptTask(GalleryActivity.this).execute(file);

					}

				}).show();
                /*
				.setPositiveButton("Share...",
						new DialogInterface.OnClickListener() {

							// @Override
							public void onClick(DialogInterface dialog,
												int which) {

								//Log.i(TAG,"open URL: " + Uri.parse(IOCipherContentProvider.FILES_URI + file.getName()));
								String sharePath = IOCipherContentProvider.addShare(file.getName(), IOCipherContentProvider.DEFAULT_AUTHORITY);
								Uri uri = Uri.parse(sharePath);


								//java.io.File exportFile = exportToDisk(file);
								//Uri uriExport = Uri.fromFile(exportFile);

								Intent intent = new Intent(Intent.ACTION_SEND);

								String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
								String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
								if (fileExtension.equals("mp4") || fileExtension.equals("mkv") || fileExtension.equals("mov"))
									mimeType = "video/*";
								if (mimeType == null)
									mimeType = "application/octet-stream";

								intent.setDataAndType(uri, mimeType);
								intent.putExtra(Intent.EXTRA_STREAM, uri);
								intent.putExtra(Intent.EXTRA_SUBJECT, file.getName());
								intent.putExtra(Intent.EXTRA_TITLE, file.getName());

								try {
									startActivity(Intent.createChooser(intent, "Share this!"));
								} catch (ActivityNotFoundException e) {
									Log.e(TAG, "No relevant Activity found", e);
								}
							}
						}).show();*/
	}

    private void shareExternalFile (java.io.File fileExtern)
    {
        Uri uri = Uri.fromFile(fileExtern);

        Intent intent = new Intent(Intent.ACTION_SEND);

        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        if (fileExtension.equals("mp4") || fileExtension.equals("mkv") || fileExtension.equals("mov"))
            mimeType = "video/*";
        if (mimeType == null)
            mimeType = "application/octet-stream";

        intent.setDataAndType(uri, mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, fileExtern.getName());
        intent.putExtra(Intent.EXTRA_TITLE, fileExtern.getName());

        try {
            startActivity(Intent.createChooser(intent, "Share this!"));
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No relevant Activity found", e);
        }
    }

	private void showItem (File file)
	{
		try {
			String fileExtension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
			String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
			if (fileExtension.equals("ts"))
				mimeType = "application/mpeg*";
			
			if (mimeType == null)
				mimeType = "application/octet-stream";

			if (mimeType.startsWith("image"))
			{
				 Intent intent = new Intent(GalleryActivity.this,ImageViewerActivity.class);
				  intent.setType(mimeType);
				  intent.putExtra("vfs", file.getAbsolutePath());
				  startActivity(intent);	
			}
			else if (fileExtension.equals("mp4") || mimeType.startsWith("video"))
			{
				Intent intent = new Intent(GalleryActivity.this,MjpegViewerActivity.class);
				  intent.setType(mimeType);
				  intent.putExtra("video", file.getAbsolutePath());
				  
				  startActivity(intent);	
				
			}
			else {
				
			  String sharePath = IOCipherContentProvider.addShare(file.getName(), IOCipherContentProvider.DEFAULT_AUTHORITY);
			  Uri uri = Uri.parse(sharePath);	  
				
	          Intent intent = new Intent(Intent.ACTION_VIEW);													
			  intent.setDataAndType(uri, mimeType);
			  startActivity(intent);
			}
			 
			
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "No relevant Activity found", e);
		}
	}

	static class ViewHolder {
		  
		  ImageView icon;		  
		  
		}
	
	class IconicList extends ArrayAdapter<Object> {

		public IconicList() {
			super(GalleryActivity.this, R.layout.gallery_gridsq, items);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			
			ViewHolder holder = null;
			
			if (convertView == null)
				convertView = inflater.inflate(R.layout.gallery_gridsq, null);							
			else 
				holder = (ViewHolder)convertView.getTag();
			
			if (holder == null)
			{
				holder = new ViewHolder();
			
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);

				holder.icon.setImageResource(R.drawable.text);
			}
			
			
			File f = new File(path.get(position)); // get the file according the
												// position
		
			String mimeType = null;

			String[] tokens = f.getName().split("\\.(?=[^\\.]+$)");
			
			if (tokens.length > 1)
				mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(f.getName().split("\\.")[1]);
			
			if (mimeType == null)
				mimeType = "application/octet-stream";
			
			if (f.isDirectory()) {
				holder.icon.setImageResource(R.drawable.folder);
			} else if (mimeType.startsWith("image")){
				
				try
				{
					Bitmap b = getPreview(f);
					if (b != null)
						holder.icon.setImageBitmap(b);
					else
						holder.icon.setImageResource(R.drawable.jpeg);//placeholder while its loading
				}
				catch (Exception e)
				{
					Log.d(TAG,"error showing thumbnail",e);
					holder.icon.setImageResource(R.drawable.text);	
				}
			}			
			else if (mimeType.startsWith("audio")||f.getName().endsWith(".pcm")||f.getName().endsWith(".aac"))
			{
				holder.icon.setImageResource(R.drawable.audio);
			}
			else if (mimeType.startsWith("video")||f.getName().endsWith(".mp4")||f.getName().endsWith(".mov"))
			{
				holder.icon.setImageResource(R.drawable.mp4);
			}
			else
			{
				holder.icon.setImageResource(R.drawable.text);
			}
				
			
			
			return (convertView);
		}

	}

	private Bitmap getPreview(File fileImage) throws FileNotFoundException {

		Bitmap b = null;

		b = mBitCache.get(fileImage.getAbsolutePath());

		if (b == null && mBitLoaders.get(fileImage.getAbsolutePath())==null)
		{
			BitmapWorkerThread bwt = new BitmapWorkerThread(fileImage);
			mBitLoaders.put(fileImage.getAbsolutePath(),bwt);
			bwt.start();

		}

		return b;
	}
	
	class BitmapWorkerThread extends Thread
	{
		private File fileImage;
		
		public BitmapWorkerThread (File fileImage)
		{
			this.fileImage = fileImage;
		}
		
		public void run ()
		{
			BitmapFactory.Options bounds = new BitmapFactory.Options();	    
			bounds.inSampleSize = 8;	 	    
			Bitmap b;
			try {
				FileInputStream fis = new FileInputStream(fileImage);
				b = BitmapFactory.decodeStream(fis, null, bounds);
				fis.close();
				mBitCache.put(fileImage.getAbsolutePath(), b);
				mBitLoaders.remove(fileImage.getAbsolutePath());

				h.post(new Runnable()
				{
					public void run ()
					{
						((IconicList)gridview.getAdapter()).notifyDataSetChanged();
					}
				});
				
				//VirtualFileSystem.get().detachThread();
		    
			} catch (Exception e) {
				Log.e(TAG,"error decoding bitmap preview",e);
			}
		}
	}
	
	/*
	class BitmapWorkerTask extends AsyncTask<File, Void, Bitmap> {

	    // Decode image in background.
	    @Override
	    protected Bitmap doInBackground(File... fileImage) {

	        BitmapFactory.Options bounds = new BitmapFactory.Options();	    
			bounds.inSampleSize = 8;	 	    
			Bitmap b;
			try {
				FileInputStream fis = new FileInputStream(fileImage[0]);
				b = BitmapFactory.decodeStream(fis, null, bounds);
				fis.close();
				mBitCache.put(fileImage[0].getAbsolutePath(), b);
				
				return b;
			} catch (Exception e) {
				Log.e(TAG,"error decoding bitmap preview",e);
			}
			
	        return null;
	        
	    }

	    // Once complete, see if ImageView is still around and set bitmap.
	    @Override
	    protected void onPostExecute(Bitmap bitmap) {	    	
	    	((IconicList)gridview.getAdapter()).notifyDataSetChanged();
			
	    }
	}*/


	 public void setUseBuiltInLockScreen(boolean useBuiltInLockScreen) {
		this.mUseBuiltInLockScreen = useBuiltInLockScreen;
	}

    class PGPEncryptTask extends AsyncTask<File, Integer, java.io.File> {
        private Activity activity;
        private ProgressDialog dialog;

        public PGPEncryptTask(Activity activity){
            this.activity = activity;
            this.dialog = new ProgressDialog(activity);
            this.dialog.setTitle("Exporting to Time Capsule");
            this.dialog.setMessage("please wait...");
            if(!this.dialog.isShowing()){
                this.dialog.show();
            }
        }

        @Override
        protected java.io.File doInBackground(File... params) {

            try {
				String fileName = "/sdcard/" + new Date().getTime() + ".pgp.asc";
                java.io.File fileTimeCap = new java.io.File(fileName);
                java.io.FileOutputStream osTimeCap = new java.io.FileOutputStream(fileTimeCap);

                boolean asciiArmor = true;
                boolean integrityCheck = true;

                PgpHelper pgpHelper = PgpHelper.getInstance();

                PGPPublicKey encKey = pgpHelper.readPublicKey(new java.io.FileInputStream(new java.io.File("/sdcard/jack.asc")));

                pgpHelper.encryptStream(osTimeCap, new FileInputStream(params[0]), params[0].getName(), params[0].length(), new Date(), encKey, asciiArmor, integrityCheck);

                params[0].delete();

				return fileTimeCap;
            }
            catch (Exception ioe)
            {
                Log.e("PGPExport","unable to export",ioe);
                Toast.makeText(activity,"Unable to export to PGP: " + ioe.getMessage(),Toast.LENGTH_LONG).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(java.io.File fileTimeCap){

            this.dialog.dismiss();
            getFileList(root);

            if (fileTimeCap != null)
                shareExternalFile(fileTimeCap);

        }
    }
	
}
