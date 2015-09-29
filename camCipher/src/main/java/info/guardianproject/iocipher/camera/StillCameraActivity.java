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

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileOutputStream;

import java.io.BufferedOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;

public class StillCameraActivity extends CameraBaseActivity {
	
	private String mFileBasePath = null;
	
	private boolean isRequest = false;
	private ArrayList<String> mResultList = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFileBasePath = getIntent().getStringExtra("basepath");
		
		isRequest = getIntent().getAction() != null && getIntent().getAction().equals(MediaStore.ACTION_IMAGE_CAPTURE);
		mResultList = new ArrayList<String>();

		button.setBackgroundResource(R.drawable.ic_action_camera);
		buttonSelfie.setBackgroundResource(R.drawable.ic_action_switch_camera);
	}

	@Override
	public void onPictureTaken(final byte[] data, Camera camera) {		
		File fileSecurePicture;
		try {
			
			if (overlayView != null)
				overlayView.setBackgroundResource(R.color.flash);
			
			long mTime = System.currentTimeMillis();
			fileSecurePicture = new File(mFileBasePath,"secure_image_" + mTime + ".jpg");

			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileSecurePicture));
			out.write(data);
			out.flush();
			out.close();

			mResultList.add(fileSecurePicture.getAbsolutePath());

			Intent intentResult = new Intent().putExtra(MediaStore.EXTRA_OUTPUT, mResultList.toArray(new String[mResultList.size()]));			
			setResult(Activity.RESULT_OK, intentResult);
			
			view.postDelayed(new Runnable()
			{
				@Override
				public void run() {
					overlayView.setBackgroundColor(Color.TRANSPARENT);
					
					resumePreview();
				}
			},100);
			

		} catch (Exception e) {
			e.printStackTrace();
			setResult(Activity.RESULT_CANCELED);

		}

	}

	@Override
	public void onPause() {

		super.onPause();

	}
	
	

}
