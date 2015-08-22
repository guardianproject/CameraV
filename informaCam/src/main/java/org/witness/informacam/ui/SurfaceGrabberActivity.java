package org.witness.informacam.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.R;
import org.witness.informacam.json.JSONArray;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.Constants.Models.IUser;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class SurfaceGrabberActivity extends Activity implements OnClickListener, SurfaceHolder.Callback, PictureCallback {
	Button button;
	TextView progress;

	SurfaceView view;
	SurfaceHolder holder;
	Camera camera;
	CameraInfo cameraInfo;
	
	private InformaCam informaCam = InformaCam.getInstance();
	private List<String> baseImages = new ArrayList<String>();
	private boolean mPreviewing;

	private final static String LOG = App.ImageCapture.LOG;

	private int mRotation = 0;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayout());

		button = (Button) findViewById(R.id.surface_grabber_button);
		button.setOnClickListener(this);
		
		progress = (TextView) findViewById(R.id.surface_grabber_progress);
		progress.setText(String.valueOf(baseImages.size()));

		view = (SurfaceView) findViewById(R.id.surface_grabber_holder);
		view.setOnClickListener(this);
		
		holder = view.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);		
	}

	protected int getLayout()
	{
		return R.layout.activity_surface_grabber;
	}
	
	protected int getCameraDirection()
	{
		return CameraInfo.CAMERA_FACING_BACK;
	}
	
	/**
     * Whether or not we can default to "other" direction if our preferred facing camera can't be opened
     * @return true to try camera facing other way, false otherwise
     */
    protected boolean canUseOtherDirection()
    {
            return false;
    }

	
	@Override
	public void onResume() {
		super.onResume();

		if (!tryCreateCamera(getCameraDirection()))
        {
                if (!canUseOtherDirection() || !tryCreateCamera(getOtherDirection(getCameraDirection())))
                {
                        finish();
                        return;
                }
        }

		if(camera == null)
			finish();
		
		setCameraDisplayOrientation();
	}

	private int getOtherDirection(int facing)
	{
		return (facing == CameraInfo.CAMERA_FACING_BACK) ? CameraInfo.CAMERA_FACING_FRONT : CameraInfo.CAMERA_FACING_BACK;
	}
	
	private boolean tryCreateCamera(int facing)
	{
	     Camera.CameraInfo info = new Camera.CameraInfo();
	     for (int nCam = 0; nCam < Camera.getNumberOfCameras(); nCam++)
	     {
		     Camera.getCameraInfo(nCam, info);
		     if (info.facing == facing)
		     {
		    	 camera = Camera.open(nCam);
		    	 cameraInfo = info;
		    	 return true;
		     }
	     }
	     return false;
	}
	
	@Override
	public void onPause() {
		if(camera != null)
			camera.release();

		super.onPause();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		camera.startPreview();
		mPreviewing = true;
	}

	protected Size choosePictureSize(List<Size> localSizes)
	{
		Size size = null;
		
		for(Size sz : localSizes) {
			Log.d(LOG, "w: " + sz.width + ", h: " + sz.height);
			if(sz.width > 480 && sz.width <= 640)
				size = sz;
			
			if(size != null)
				break;
		}
		
		if(size == null)
			size = localSizes.get(localSizes.size() - 1);
		return size;
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
			
			Size size = choosePictureSize(camera.getParameters().getSupportedPictureSizes());

			Camera.Parameters params = camera.getParameters();
			params.setPictureSize(size.width, size.height);
			params.setJpegQuality(100);
			params.setJpegThumbnailQuality(100);
			params.setRotation(mRotation); //set rotation to save the picture

			// TODO: set the camera image size that is uniform and small.
			camera.setParameters(params);

		} catch(IOException e) {
			Log.e(LOG, e.toString());
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}

	@Override
	public void onClick(View view) {
		if(mPreviewing) {
			mPreviewing = false;
			camera.takePicture(null, null, this);
		}
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		
		try
		{
			String pathToData = IUser.BASE_IMAGE +"_" + baseImages.size();
			
			if(informaCam.ioService.saveBlob(data, new File(pathToData))) {
				data = null;
				baseImages.add(pathToData);
				progress.setText(String.valueOf(baseImages.size()));
				
				if(baseImages.size() == 6) {
					button.setClickable(false);
					button.setVisibility(View.GONE);
					progress.setBackgroundResource(R.drawable.progress_accepted);
					
					JSONArray ja = new JSONArray();
					for(String bi : baseImages) {
						ja.put(bi);
					}
					
					informaCam.user.put(Models.IUser.PATH_TO_BASE_IMAGE, ja);
					informaCam.user.hasBaseImage = true;
					informaCam.user.save();
					setResult(Activity.RESULT_OK);
					finish();
					return;
					
				}
			}
			
			view.post(new Runnable()
			{
				@Override
				public void run() {
					resumePreview();
				}
			});
		}
		catch (IOException ioe)
		{
			Log.e(LOG,"error saving picture to iocipher",ioe);
		}
	}

	protected void resumePreview()
	{
		if (!mPreviewing)
		{
			camera.startPreview();
			mPreviewing = true;
		}
	}
	
	public void setCameraDisplayOrientation() 
	{        
	     if (camera == null || cameraInfo == null)
	     {
	         return;             
	     }

	     WindowManager winManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
	     int rotation = winManager.getDefaultDisplay().getRotation();

	     int degrees = 0;

	     switch (rotation) 
	     {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int mRotation;
	     
	     if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) 
	     {
	    	 mRotation = (cameraInfo.orientation + degrees) % 360;
	    	 mRotation = (360 - mRotation) % 360;  // compensate the mirror
	     } else {  // back-facing
	    	 mRotation = (cameraInfo.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(mRotation);
	}
}