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
import info.guardianproject.iocipher.camera.encoders.AACHelper;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class AudioRecorderActivity extends Activity implements OnClickListener {
	
	private final static String LOG = "AudioRecorder";
	
	private String mFileBasePath = null;
	private boolean mIsRecording = false;

	private AACHelper aac;
	private boolean useAAC = false;
	private byte[] audioData;
	private AudioRecord audioRecord;
	
	private OutputStream outputStreamAudio;
	private info.guardianproject.iocipher.File fileAudio;
	
	private boolean isRequest = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFileBasePath = getIntent().getStringExtra("basepath");
		
		isRequest = getIntent().getAction() != null && getIntent().getAction().equals(MediaStore.ACTION_VIDEO_CAPTURE);

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
			
		if (!mIsRecording)
		{
			
			startRecording();
			
			
		}
		else
		{
			stopRecording ();
		}
		
	}
	
	private void startRecording ()
	{
		String fileName = "audio" + new java.util.Date().getTime();
		info.guardianproject.iocipher.File fileOut = new info.guardianproject.iocipher.File(mFileBasePath,fileName);
		
		try {
			mIsRecording = true;
			
			if (useAAC)
				initAudio(fileOut.getAbsolutePath()+".aac");
			else
				initAudio(fileOut.getAbsolutePath()+".pcm");
			
			
			startAudioRecording();
			

		} catch (Exception e) {
			Log.d("Video","error starting video",e);
			Toast.makeText(this, "Error init'ing video: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	private void stopRecording ()
	{
		h.sendEmptyMessageDelayed(1, 2000);
	}
	
	
	Handler h = new Handler ()
	{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			if (msg.what == 0)
			{
				
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
				   // audioSource = MediaRecorder.AudioSource.MIC;
				   
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
		                	
		                	//muxer.addAudio(ByteBuffer.wrap(audioData, 0, audioData.length));
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }
		            }
				   }
				   
				   audioRecord.stop();
				   audioRecord.release();
				   audioRecord = null;
				   
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
	 
	 
	
}
