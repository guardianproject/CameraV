/**
 * 
 * This file contains code from the IOCipher Camera Library "CipherCam".
 *
 * For more information about IOCipher, see https://guardianproject.info/code/iocipher
 * and this sample library: https://github.com/n8fr8/IOCipherCameraExample
 *
 * IOCipher Camera Sample is distributed under this license (aka the 3-clause BSD license)
 *
 * Some of this class was originally part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * 
 * @author n8fr8, The JCodec project
 * 
 */
package info.guardianproject.iocipher.camera.viewer;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileInputStream;
import info.guardianproject.iocipher.camera.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class ImageViewerActivity extends Activity {


	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	    WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//prevent screenshots
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
				WindowManager.LayoutParams.FLAG_SECURE);
		
		// This example uses decor view, but you can use any visible view.
		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
		decorView.setSystemUiVisibility(uiOptions);
		
	        
		setContentView(R.layout.pzsimageview);
		
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		
		ImageView iv = (ImageView)findViewById(R.id.imageview);
		
		if (intent.hasExtra("vfs"))
		{
			try
			{
				File file = new File(intent.getExtras().getString("vfs"));
				Bitmap bmp = null;
				
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				 BitmapFactory.decodeStream(new FileInputStream(file),null,options);
				int imageHeight = options.outHeight;
				int imageWidth = options.outWidth;
				String imageType = options.outMimeType;
				
				options.inJustDecodeBounds = false;
				options.inSampleSize = 2;
				bmp =  BitmapFactory.decodeStream(new FileInputStream(file),null,options);
				
				iv.setImageBitmap(bmp);
			}
			catch (Exception e)
			{
				Log.d("Image","error showing vfs image",e);
			}
		}
		else
		{
			iv.setImageURI(intent.getData());
		}
		
	}

	protected void onResume() {
		super.onResume();
		
	}

	protected void onDestroy() {
		super.onDestroy();
		
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
      //  MenuInflater inflater = getMenuInflater();
       // inflater.inflate(R.menu.main, menu);
        
        return true;
	}
	

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	/**
        switch (item.getItemId()) {

        case R.id.menu_camera:
        	
        	Intent intent = new Intent(this,SecureSelfieActivity.class);
        	intent.putExtra("basepath", "/");
        	startActivityForResult(intent, 1);
        	
        	return true;
        case R.id.menu_video:
        	
        	intent = new Intent(this,VideoRecorderActivity.class);
        	intent.putExtra("basepath", "/");
        	startActivityForResult(intent, 1);
        	
        	return true;	
        }	*/
        
        return false;
    }

	
	
		
	
		
		
}
