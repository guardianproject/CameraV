package org.witness.informacam.app.screens.editors;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileInputStream;
import info.guardianproject.iocipher.camera.MediaConstants;
import info.guardianproject.iocipher.camera.encoders.AACHelper;
import info.guardianproject.iocipher.camera.viewer.MjpegInputStream;
import info.guardianproject.iocipher.camera.viewer.MjpegView;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.witness.informacam.InformaCam;
import org.witness.informacam.app.R;
import org.witness.informacam.app.screens.FullScreenViewFragment;
import org.witness.informacam.app.utils.Constants.App.Editor;
import org.witness.informacam.app.utils.Constants.EditorActivityListener;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.models.media.IVideo;
import org.witness.informacam.ui.editors.IRegionDisplay;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.RectF;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.storage.OnObbStateChangeListener;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;

import com.efor18.rangeseekbar.RangeSeekBar;
import com.efor18.rangeseekbar.RangeSeekBar.OnRangeSeekBarChangeListener;

public class FullScreenMJPEGViewFragment extends FullScreenViewFragment implements OnClickListener, 
OnVideoSizeChangedListener, SurfaceHolder.Callback, OnTouchListener,  
OnRangeSeekBarChangeListener<Integer> {
	
	IVideo media_;
	InformaCam informa;
	
	MjpegView videoView;
	SurfaceHolder surfaceHolder;

	AudioTrack at;
    InputStream isAudio = null;
    boolean useAAC = false;
	
	View mediaHolder_;
	LinearLayout videoControlsHolder, endpointHolder;
	VideoSeekBar videoSeekBar;
	ImageButton playPauseToggle;
	
	Uri videoUri;

	long duration = 0L;
	int currentCue = 1;

	private boolean isPlaying = false;
	
	private Handler handler = new Handler();
	
	private final static String LOG = Editor.LOG;
	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		try {
			media_ = new IVideo(((EditorActivityListener) a).media());
		} catch (java.lang.InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		informa = (InformaCam)a.getApplication();

	}
	

	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		
		Display display =getActivity().getWindowManager().getDefaultDisplay();
		dims = new int[] { display.getWidth(), display.getHeight() };

	}

	private void initVideo() throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {

		try {
			videoView.setSource(new MjpegInputStream(new info.guardianproject.iocipher.FileInputStream(media_.dcimEntry.fileAsset.path)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	private void initAudio () throws Exception
	{
		File fileAudio = null;
		
		String ioCipherAudioPath = null;
		String ioCipherVideoPath = media_.dcimEntry.fileAsset.path;
		
		if (ioCipherAudioPath == null)
		{
			fileAudio = new File(ioCipherVideoPath + ".pcm");
		
			if (fileAudio.exists())
			{
				initAudio(fileAudio.getAbsolutePath());
				
			}
		}
	}
	
	public void initAudio(String vfsPath) throws Exception {

    	isAudio = new BufferedInputStream(new FileInputStream(vfsPath));

	
        int minBufferSize = AudioTrack.getMinBufferSize(MediaConstants.sAudioSampleRate,
        		MediaConstants.sChannelConfigOut, AudioFormat.ENCODING_PCM_16BIT)*8;

        at = new AudioTrack(AudioManager.STREAM_MUSIC, MediaConstants.sAudioSampleRate,
        		MediaConstants.sChannelConfigOut, AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize, AudioTrack.MODE_STREAM);
        
	     
    }
    
    public void playAudio ()
    {
    
    	new Thread ()
		{
			public void run ()
			{
				try {							

			        try{
			        	byte[] music = null;
			        	music = new byte[512];
			            at.play();

			            int i = 0;
			            while(isPlaying && (i = isAudio.read(music)) != -1)
			                at.write(music, 0, i);

			        } catch (IOException e) {
			            e.printStackTrace();
			        }

			        at.stop();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		
    	
    }
	
	
	private void updateRegionView(final long timestamp) {
		
		if (media_ == null || media_.associatedRegions == null || mediaHolder == null)
			return;
		
		for(IRegion r : media_.associatedRegions) {
			if(r.getRegionDisplay() != null && r.bounds.displayWidth != 0 && r.bounds.displayHeight != 0) {
				
				IRegionDisplay rd = (IRegionDisplay) mediaHolder.getChildAt(r.getRegionDisplay().indexOnScreen);
				
				if(timestamp >= r.bounds.startTime && timestamp <= r.bounds.endTime) {
					rd.setVisibility(View.VISIBLE);
					
					// TODO: update region display with new bounds from trail
					
					//IRegionBounds rb = ((IVideoRegion) r).getBoundsAtTime(mediaPlayer.getCurrentPosition());
					//Log.d(LOG, rb.asJson().toString());
				} else {
					rd.setVisibility(View.GONE);
				}
				
			}
		}
			
	}
	
	
	@SuppressWarnings("deprecation")
	@Override
	protected void initLayout() {
		super.initLayout();
	
		mediaHolder_ = LayoutInflater.from(getActivity()).inflate(R.layout.editors_mjpeg_video, null);

		videoView = (MjpegView) mediaHolder_.findViewById(R.id.video_view);
		videoView.setDisplayMode(MjpegView.SIZE_BEST_FIT);
		videoView.setFrameDelay(50); //we need to better sync each frame to the audio

	//	LayoutParams vv_lp = videoView.getLayoutParams();
//		vv_lp.width = dims[0];
//		vv_lp.height = (int) (((float) media_.dcimEntry.exif.height) / ((float) media_.dcimEntry.exif.width) * dims[0]);

	//	videoView.setLayoutParams(vv_lp);
		videoView.setOnTouchListener(this);

		mediaHolder.addView(mediaHolder_);
		
		surfaceHolder = videoView.getHolder();
		surfaceHolder.addCallback(this);
		
		videoControlsHolder = (LinearLayout) mediaHolder_.findViewById(R.id.video_controls_holder);		

		videoSeekBar = (VideoSeekBar) mediaHolder_.findViewById(R.id.video_seek_bar);
		endpointHolder = (LinearLayout) mediaHolder_.findViewById(R.id.video_seek_bar_endpoint_holder);
		
		playPauseToggle = (ImageButton) mediaHolder_.findViewById(R.id.video_play_pause_toggle);
		playPauseToggle.setOnClickListener(this);	
		
		videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			
				if (progress == 0)
				{
					pause();
					initAndStart();
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
		});
		
	}
	
	
	@Override
	public void onSelected(IRegionDisplay regionDisplay) {		
		
		/*
		((IVideoRegion) regionDisplay.parent).setTimestampInQuestion(mediaPlayer.getCurrentPosition());
		
		setCurrentRegion(regionDisplay.parent);
		videoSeekBar.showEndpoints((IVideoRegion) regionDisplay.parent);
		
		mediaPlayer.seekTo((int) regionDisplay.bounds.startTime);
		**/
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(LOG, "surfaceChanged called");
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(LOG, "surfaceCreated Called");
		initAndStart();
	}
	
	private void initAndStart()
	{
		try {
			initAudio ();
			initVideo();
			start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(LOG, "surfaceDestroyed called");

		/*
		if (mediaPlayer != null)
		{
			mediaPlayer.stop();
			videoSeekBar.disable();
			mediaPlayer.release();
			mediaPlayer = null;
		}*/
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		Log.d(LOG, "onVideoSizeChanged called, new width: " + width + ", new height: " + height);
	}

	public void onSeekComplete(MediaPlayer mp) {
		//videoSeekBar.update();
		mIsSeeking = false;
	}
	
	@Override
	public void onClick(View v) {
	
		
		if(v == playPauseToggle) {

			if(videoView.isPlaying()) {
				pause();
			} else {
				start();
			}
		}
	}

	

	@Override
	public void onPause() {
		
		super.onPause();
		
		pause(); //pause media playback
		
	}

	@Override
	public void onDestroy() {
		
		super.onDestroy();

		if (at != null)
		{
			at.release();
			try {
				isAudio.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}



	public boolean isPlaying() {
		return videoView.isPlaying();
	}

	public void pause() {
		
		playPauseToggle.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_videol_play));

		if (isPlaying)
		{
			videoView.stopPlayback();
				
			if (at != null)
				at.stop();
		}
		
		isPlaying = false;
		
		
	}

	private boolean mIsSeeking = false;
	
	public void seekTo(int pos) {
		
		if (!mIsSeeking)
		{
			/**
			if (pos <= (duration * (mBufferPercent/100)))
			{
				mIsSeeking = true;
				
		//		mediaPlayer.seekTo(pos);
			}*/
		}
		
	}

	public void start() {

		isPlaying = true;
		
		playPauseToggle.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_videol_pause));

		playAudio ();
		videoView.startPlayback();
	}

	@Override
	public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
		Log.d(LOG, "new range: " + minValue + " - " + maxValue);
		if(currentRegion != null) {
			currentRegion.bounds.startTime = minValue;
			currentRegion.bounds.endTime = maxValue;
		}
	}

	@Override
	public void onStartTrackingTouch(RangeSeekBar<?> bar) {}

	@Override
	public void onStopTrackingTouch(RangeSeekBar<?> bar) {}
	
	@Override
	public int[] getSpecs() {
		//Log.d(LOG, "RECALCULATING FOR VIDEO");
		List<Integer> specs = new ArrayList<Integer>(Arrays.asList(ArrayUtils.toObject(super.getSpecs())));
		
		int[] locationInWindow = new int[2];
		videoView.getLocationInWindow(locationInWindow);
		for(int i : locationInWindow) {
			specs.add(i);
		}
		
		// these might not be needed
		specs.add(videoView.getWidth());
		specs.add(videoView.getHeight());
		
		Log.d(LOG, "position on screen : " + locationInWindow[0] + ", " + locationInWindow[1]);
		
		return ArrayUtils.toPrimitive(specs.toArray(new Integer[specs.size()]));
	}
	
	@Override
	public RectF getImageBounds()
	{
		return new RectF(0,0,videoView.getWidth(),videoView.getHeight());
	}

}
