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
import info.guardianproject.iocipher.FileInputStream;
import info.guardianproject.iocipher.FileOutputStream;
import info.guardianproject.iocipher.camera.encoders.AACHelper;
import info.guardianproject.iocipher.camera.encoders.ImageToMJPEGMOVMuxer;
import info.guardianproject.iocipher.camera.io.IOCipherFileChannelWrapper;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;

import org.jcodec.common.ArrayUtil;
import org.jcodec.common.SeekableByteChannel;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class VideoCameraActivity extends CameraBaseActivity {
	
	private final static String LOG = "VideoJPEGRecorder";
	
	private String mFileBasePath = null;
	private boolean mIsRecording = false;
	
	private ArrayDeque<VideoFrame> mFrameQ = null;
	
	private int mLastWidth = -1;
	private int mLastHeight = -1;
	private int mPreviewFormat = -1;

	private ImageToMJPEGMOVMuxer muxer;
	
	private AACHelper aac;
	private boolean useAAC = false;
	private byte[] audioData;
	private AudioRecord audioRecord;
	
	private boolean mPreCompressFrames = true;
	private OutputStream outputStreamAudio;
	private info.guardianproject.iocipher.File fileAudio;

	private int mFpsCounter = 0;
	private long start = 0;
	private long lastTime = 0;
	private int mFramesTotal = 0;
	private int mFPS = 15; //default is 15fps
	
	private boolean isRequest = false;
	private ArrayList<String> mResultList = null;

	private boolean mInTopHalf = false;

	private info.guardianproject.iocipher.File fileOut;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		mFileBasePath = getIntent().getStringExtra("basepath");
		
		isRequest = getIntent().getAction() != null && getIntent().getAction().equals(MediaStore.ACTION_VIDEO_CAPTURE);
		mResultList = new ArrayList<String>();
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mIsRecording)
			stopRecording();
	}

	@Override
	protected int getLayout()
	{
		return R.layout.base_camera;
	}

	private float mDownX = -1,mLastX = -1;
	private float mDownY = -1,mLastY=-1;
	private boolean mIsOnClick = false;
	private final float SCROLL_THRESHOLD = 10;

	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		
		//if short tap then take a picture
		
		//if long and hold then start video, then end on release
		
		//if location is on top half then front camera, on bottom have then back camera
		
		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            mDownX = ev.getX();
            mDownY = ev.getY();
            mIsOnClick = true;

			if (!mIsRecording)
            	handler.postDelayed(mLongPressed, 1000);
            
            mInTopHalf = mDownY < (mLastHeight/2);

            toggleCamera(mInTopHalf);
            
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
			handler.removeCallbacks(mLongPressed);

			if (!mIsRecording) {

				try {
					//take a picture
					mPreviewing = false;
					camera.takePicture(null, null, this);
				}
				catch (RuntimeException re)
				{
					//hardware failure of take picture
				}
            	
            }
            else
            {
            	stopRecording();
            }
            
            break;
        case MotionEvent.ACTION_MOVE:
        	
        	mLastX = ev.getX();
        	mLastY = ev.getY();

            mInTopHalf = mLastY < (mDownY-100);

            toggleCamera(mInTopHalf);
            
            if (mIsOnClick && (Math.abs(mDownX - ev.getX()) > SCROLL_THRESHOLD || Math.abs(mDownY - ev.getY()) > SCROLL_THRESHOLD)) {
                mIsOnClick = false;
                
            }
            break;
        default:
            break;
	    }
	    return true;
		
	}
	
	final Handler handler = new Handler(); 
	Runnable mLongPressed = new Runnable() { 
	    public void run() { 
	        Log.i("", "Long press!");
	        
	        startRecording();
	        
	    }   
	};

	
	@Override
	public void onClick(View view) {
		
		if (view == button)
		{
			if (!mIsRecording)
			{
				
				startRecording();
				
				
			}
			else
			{
				stopRecording ();
			}
		}
		
	}
	
	
	private void startRecording ()
	{
		mFrameQ = new ArrayDeque<VideoFrame>();
		
		mFramesTotal = 0;
		mFpsCounter = 0;
		
		lastTime = System.currentTimeMillis();
		
		String fileName = "camerav_video_" + new java.util.Date().getTime() + ".mp4";
		fileOut = new info.guardianproject.iocipher.File(mFileBasePath,fileName);
		
		mResultList.add(fileOut.getAbsolutePath());
		Intent intentResult = new Intent().putExtra(MediaStore.EXTRA_OUTPUT, mResultList.toArray(new String[mResultList.size()]));			
		setResult(Activity.RESULT_OK, intentResult);

		try {
			mIsRecording = true;
			
			if (useAAC)
				initAudio(fileOut.getAbsolutePath()+".aac");
			else
				initAudio(fileOut.getAbsolutePath()+".pcm");
			
			boolean withEmbeddedAudio = true;
			
			Encoder encoder = new Encoder(fileOut,mFPS,withEmbeddedAudio);
			encoder.start();
			//start capture
			startAudioRecording();
			
			progress.setText("[REC]");

		} catch (Exception e) {
			Log.d("Video","error starting video",e);
			Toast.makeText(this, "Error init'ing video: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	private void stopRecording ()
	{
		progress.setText("[SAVING]");
		h.sendEmptyMessageDelayed(1, 2000);
		progress.setText("");
		
	}
	
	private void toggleCamera (boolean isSelfie)
	{
		if (isSelfie != mIsSelfie)
		{
			mIsSelfie = isSelfie;
			releaseCamera();
			initCamera();
		}
	}

	//support still pictures if you tap on the screen
	@Override
	public void onPictureTaken(final byte[] data, Camera camera) {		
		File fileSecurePicture;
		try {

			overlayView.setBackgroundResource(R.color.flash);
			
			long mTime = System.currentTimeMillis();
			fileSecurePicture = new File(mFileBasePath,"camerav_image_" + mTime + ".jpg");

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
	public void onPreviewFrame(byte[] data, Camera camera) {
		
		//even when not recording, we'll compress frames in order to estimate our FPS
		
	    Camera.Parameters parameters = camera.getParameters();
	    mLastWidth = parameters.getPreviewSize().width;
	    mLastHeight = parameters.getPreviewSize().height;
	    
		if (mRotation > 0) //flip height and width
		{
			mLastWidth =parameters.getPreviewSize().height;
			mLastHeight =parameters.getPreviewSize().width;
		}
	    
	    mPreviewFormat = parameters.getPreviewFormat();
	    
	    byte[] dataResult = data;
		
		if (mPreCompressFrames)
		{
			if (mRotation > 0)
			{
				dataResult = rotateYUV420Degree90(data,mLastHeight,mLastWidth);
				
				 if (getCameraDirection() == CameraInfo.CAMERA_FACING_FRONT)
				 {						 
					 dataResult = rotateYUV420Degree90(dataResult,mLastWidth,mLastHeight);
					 dataResult = rotateYUV420Degree90(dataResult,mLastHeight,mLastWidth);						 
				 }
				
			}
			
			YuvImage yuv = new YuvImage(dataResult, mPreviewFormat, mLastWidth, mLastHeight, null);
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    yuv.compressToJpeg(new Rect(0, 0, mLastWidth, mLastHeight), MediaConstants.sJpegQuality, out);				    
		    dataResult = out.toByteArray();
		}   
		
		if (mFramesTotal == 0 && fileOut != null)
		{
			try {
				info.guardianproject.iocipher.FileOutputStream fosThumb = new info.guardianproject.iocipher.FileOutputStream(new info.guardianproject.iocipher.File(fileOut.getAbsolutePath() + ".thumb.jpg"));
				fosThumb.write(dataResult);
				fosThumb.flush();
				fosThumb.close();
			
			} catch (Exception e) {

				Log.e("VideoCam","can't save thumb",e);
			}
		}
		
		if (mIsRecording && mFrameQ != null)
			if (data != null)
			{

				VideoFrame vf = new VideoFrame();
				vf.image = dataResult;
				vf.duration = 1;//this is frame duration, not time //System.currentTimeMillis() - lastTime;
				vf.fps = mFPS;

				mFrameQ.add(vf);

				mFramesTotal++;

			}

			
		mFpsCounter++;
        if((System.currentTimeMillis() - start) >= 1000) {
        	mFPS = mFpsCounter;
        	mFpsCounter = 0; 
            start = System.currentTimeMillis();
        }
		
	}
	
	private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) 
	{
	    byte [] yuv = new byte[imageWidth*imageHeight*3/2];
	    // Rotate the Y luma
	    int i = 0;
	    for(int x = 0;x < imageWidth;x++)
	    {
	        for(int y = imageHeight-1;y >= 0;y--)                               
	        {
	            yuv[i] = data[y*imageWidth+x];
	            i++;
	        }
	    }
	    // Rotate the U and V color components 
	    i = imageWidth*imageHeight*3/2-1;
	    for(int x = imageWidth-1;x > 0;x=x-2)
	    {
	        for(int y = 0;y < imageHeight/2;y++)                                
	        {
	            yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
	            i--;
	            yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
	            i--;
	        }
	    }
	    return yuv;
	}

	private class VideoFrame 
	{
		byte[] image;
		long fps;
		long duration;
	}
	
	private class Encoder extends Thread {
		private static final String TAG = "ENCODER";

		private File fileOut;
		private FileOutputStream fos;
		
		public Encoder (File fileOut, int baseFramesPerSecond, boolean withEmbeddedAudio) throws IOException
		{
			this.fileOut = fileOut;

			fos = new info.guardianproject.iocipher.FileOutputStream(fileOut);
			SeekableByteChannel sbc = new IOCipherFileChannelWrapper(fos.getChannel());

			org.jcodec.common.AudioFormat af = null;
			
			if (withEmbeddedAudio)
				af = new org.jcodec.common.AudioFormat(org.jcodec.common.AudioFormat.MONO_S16_LE(MediaConstants.sAudioSampleRate));
			
			muxer = new ImageToMJPEGMOVMuxer(sbc,af,baseFramesPerSecond);			
		}
		
		public void run ()
		{

			try {
				
				while (mIsRecording || (!mFrameQ.isEmpty()))
				{
					if (mFrameQ.peek() != null)
					{
						VideoFrame vf = mFrameQ.pop();
						
						muxer.addFrame(mLastWidth, mLastHeight, ByteBuffer.wrap(vf.image),vf.fps,vf.duration);	
						
					}

				}
				
				//now write audio
				
            	FileInputStream fis = new FileInputStream(fileAudio);
            	byte[] audioBuffer = new byte[1024*32];
            	int bytesRead = -1;
            	
            	while ((bytesRead = fis.read(audioBuffer))!=-1)
            	{
            		muxer.addAudio(ByteBuffer.wrap(audioBuffer, 0, bytesRead));
            	}

				muxer.finish();
				
				fis.close();
			//	fos.close();
				
				
			} catch (Exception e) {
				Log.e(TAG, "IO", e);
			}

		}
		
	
		
	}
	
	Handler h = new Handler ()
	{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			if (msg.what == 0)
			{
				int frames = msg.getData().getInt("frames");
				
					if (!mIsRecording)
						if (frames == 0)
							progress.setText("");
						else
							progress.setText("Processing: " + (mFramesTotal-frames) + '/' +  mFramesTotal);
					else
						progress.setText("Recording: " + mFramesTotal);
			}
			else if (msg.what == 1)
			{
				mIsRecording = false; //stop recording
				
				if (aac != null)
					aac.stopRecording();
				
			}
		}
		
	};
	
	 private void initAudio(final String audioPath) throws Exception {

			fileAudio  = new File(audioPath); 
			
			   outputStreamAudio = new BufferedOutputStream(new info.guardianproject.iocipher.FileOutputStream(fileAudio),8192*8);
				
			   if (useAAC)
			   {
				   aac = new AACHelper();
				   aac.setEncoder(MediaConstants.sAudioSampleRate, MediaConstants.sAudioChannels, MediaConstants.sAudioBitRate);
			   }
			   else
			   {
			   
				   int minBufferSize = AudioRecord.getMinBufferSize(MediaConstants.sAudioSampleRate, 
					MediaConstants.sChannelConfigIn, 
				     AudioFormat.ENCODING_PCM_16BIT)*8;
				   
				   audioData = new byte[minBufferSize];
	
				   int audioSource = MediaRecorder.AudioSource.CAMCORDER;
				   
				   if (this.getCameraDirection() == CameraInfo.CAMERA_FACING_FRONT)
				   {
					   audioSource = MediaRecorder.AudioSource.MIC;
					   
				   }
				   
				   audioRecord = new AudioRecord(audioSource,
						   MediaConstants.sAudioSampleRate,
						   MediaConstants.sChannelConfigIn,
				     AudioFormat.ENCODING_PCM_16BIT,
				     minBufferSize);
			   }
	 }
	 
	 private void startAudioRecording ()
	 {
			  
		 
		 Thread thread = new Thread ()
		 {
			 
			 public void run ()
			 {
			 
				 if (useAAC)
				 {
					 try {
						aac.startRecording(outputStreamAudio);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 }
				 else
				 {
				   audioRecord.startRecording();
				   
				   while(mIsRecording){
				    int audioDataBytes = audioRecord.read(audioData, 0, audioData.length);
				    if (AudioRecord.ERROR_INVALID_OPERATION != audioDataBytes
		                    && outputStreamAudio != null) {
		                try {
		                	outputStreamAudio.write(audioData,0,audioDataBytes);
		                	
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }
		            }
				   }
				   
				   audioRecord.stop();
				   try {
					   outputStreamAudio.flush();
					outputStreamAudio.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 }
				 
				 
			 }
		 };
		 
		 thread.start();

	 }
	 
	  @Override
	   public void onConfigurationChanged(Configuration newConfig) {
		  
		  	mIsRecording = false;
		  	
	        super.onConfigurationChanged(newConfig);

	   }
	 
	 
	
}
