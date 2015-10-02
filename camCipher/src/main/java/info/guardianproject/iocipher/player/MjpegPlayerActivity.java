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

package info.guardianproject.iocipher.player;


import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileInputStream;
import info.guardianproject.iocipher.camera.MediaConstants;
import info.guardianproject.iocipher.camera.R;
import info.guardianproject.iocipher.camera.encoders.AACHelper;

public class MjpegPlayerActivity extends Activity {
    private static final String TAG = "MjpegActivity";

	MjpegView videoView;
    private AudioTrack at;

    InputStream isAudio = null;
    AACHelper aac = null;
    boolean useAAC = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

      //prevent screenshots
      		getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
					WindowManager.LayoutParams.FLAG_SECURE);

		setContentView(R.layout.mpegview);

        final String ioCipherVideoPath = getIntent().getExtras().getString("video");
        final String ioCipherAudioPath = getIntent().getExtras().getString("audio");

        try {

			File fileAudio = null;
			
			if (ioCipherAudioPath == null)
			{
				fileAudio = new File(ioCipherVideoPath + ".pcm");
				if (!fileAudio.exists())
				{
					fileAudio = new File(ioCipherVideoPath + ".aac");
					useAAC = true;
				}
			}
			else
			{
				fileAudio = new File(ioCipherAudioPath);
				
				if (getIntent().getExtras().containsKey("useAAC"))
					useAAC = getIntent().getExtras().getBoolean("useAAC",false);
				else if (ioCipherAudioPath.endsWith(".aac"))
					useAAC = true;
			}
			
			if (fileAudio.exists())
			{
				initAudio(fileAudio.getAbsolutePath());
				new Thread ()
				{
					public void run ()
					{
						try {							
							playAudio();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}.start();
			}

			initVideo(new FileInputStream(ioCipherVideoPath));

		        
	    	} 
        catch (Exception e) {
			
			Log.e(TAG,"an error occcured init'g video playback",e);
		}
    }

	private void initVideo (InputStream is)
	{

		videoView = (MjpegView) findViewById(R.id.videoView1);
		MediaController mediaController = new MediaController(this);
		mediaController.setAnchorView(videoView);

		videoView.setMediaController(mediaController);

		videoView.setSource(new MjpegInputStream(is, getCacheDir()));
		videoView.start();

	}
    
    public void initAudio(String vfsPath) throws Exception {

    	isAudio = new BufferedInputStream(new FileInputStream(vfsPath));

    	if (useAAC)
    	{
    		aac = new AACHelper();
    		aac.setDecoder(MediaConstants.sAudioSampleRate, MediaConstants.sAudioChannels, MediaConstants.sAudioBitRate);
    	}
    	else
    	{
	
	        int minBufferSize = AudioTrack.getMinBufferSize(MediaConstants.sAudioSampleRate,
	        		MediaConstants.sChannelConfigOut, AudioFormat.ENCODING_PCM_16BIT)*8;
	
	        at = new AudioTrack(AudioManager.STREAM_MUSIC, MediaConstants.sAudioSampleRate,
	        		MediaConstants.sChannelConfigOut, AudioFormat.ENCODING_PCM_16BIT,
	            minBufferSize, AudioTrack.MODE_STREAM);
	        
    	}
         
    }
    
    public void playAudio () throws IOException
    {
    	if (useAAC)
    		aac.startPlaying(isAudio);
    	else
    	{
	        try{
	        	byte[] music = null;
	        	music = new byte[512];
	            at.play();
	
	            int i = 0;
	            while((i = isAudio.read(music)) != -1)
	                at.write(music, 0, i);
	
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	
	        at.stop();
	        at.release();
	        isAudio.close();
	        at = null;
    	}
    }

    public void onPause() {
        super.onPause();

        videoView.stopPlayback();

        if (at!=null)
        	at.stop();
        
    }

}