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
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

public class StillCameraActivity extends CameraBaseActivity {
	
	private String mFileBasePath = null;
	
	private boolean isRequest = false;
	private ArrayList<String> mResultList = null;

	private Handler mHandler = new Handler();

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
	public void onPictureTaken(byte[] data, Camera camera) {

		overlayView.setBackgroundColor(Color.WHITE);

		new SaveTask().execute(data);

		view.postDelayed(new Runnable() {
			public void run() {
				overlayView.setBackgroundColor(Color.TRANSPARENT);
				resumePreview();
			}
		},10);

	}

	class SaveTask extends AsyncTask<byte[], Void, File>
	{

		@Override
		protected void onPostExecute(File fileResult) {
			super.onPostExecute(fileResult);

			Intent intentResult = new Intent().putExtra(MediaStore.EXTRA_OUTPUT, mResultList.toArray(new String[mResultList.size()]));
			setResult(Activity.RESULT_OK, intentResult);

		}

		@Override
		protected File doInBackground(byte[]... data) {

			try {

				long mTime = System.currentTimeMillis();
				File fileSecurePicture = new File(mFileBasePath, "camerav_image_" + mTime + ".jpg");
				mResultList.add(fileSecurePicture.getAbsolutePath());

				FileOutputStream out = new FileOutputStream(fileSecurePicture);
				out.write(data[0]);
				out.flush();
				out.close();

				return fileSecurePicture;

			} catch (IOException ioe) {
				Log.e(StillCameraActivity.class.getName(), "error saving picture", ioe);
			}

			return null;
		}

	};


}
