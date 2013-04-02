package org.witness.iwitness.app.screens.editors;

import java.io.IOException;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.media.IVideo;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.InformaCamMediaScanner;
import org.witness.iwitness.R;
import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.screens.FullScreenViewFragment;
import org.witness.iwitness.utils.Constants.EditorActivityListener;

import android.app.Activity;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ProgressBar;
import android.widget.VideoView;

public class FullScreenVideoViewFragment extends FullScreenViewFragment implements OnCompletionListener, 
OnErrorListener, OnInfoListener, OnBufferingUpdateListener, OnPreparedListener, OnSeekCompleteListener,
OnVideoSizeChangedListener, SurfaceHolder.Callback, OnTouchListener, EditorActivityListener {
	IVideo media;
	
	MediaMetadataRetriever retriever = new MediaMetadataRetriever();
	VideoView videoView;
	SurfaceHolder surfaceHolder;
	MediaPlayer mediaPlayer;
	
	ProgressBar videoSeekBar;
	
	Uri videoUri;
	java.io.File videoFile;
	
	long duration = 0L;
	int currentCue = 0;
	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;

		media = new IVideo();
		media.inflate(((EditorActivity) a).media.asJson());
		informaCam = InformaCam.getInstance();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// copy from iocipher to local :(
				videoFile = new java.io.File(Storage.EXTERNAL_DIR, media.dcimEntry.name);
				informaCam.ioService.saveBlob(informaCam.ioService.getBytes(media.dcimEntry.fileName, Type.IOCIPHER), videoFile);
				InformaCamMediaScanner icms = new InformaCamMediaScanner(FullScreenVideoViewFragment.this.a, videoFile);
			}
		}).start();
	}
	
	private void initVideo() {
		retriever.setDataSource(videoFile.getAbsolutePath());
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setOnInfoListener(this);
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnSeekCompleteListener(this);
		mediaPlayer.setOnVideoSizeChangedListener(this);
		mediaPlayer.setOnBufferingUpdateListener(this);

		mediaPlayer.setLooping(false);
		mediaPlayer.setScreenOnWhilePlaying(true);
		
		try {
			mediaPlayer.setDataSource(videoUri.toString());
			Log.d(LOG, "setData done.");
		} catch (IllegalArgumentException e) {
			Log.e(LOG, "setDataSource error: " + e.getMessage());
			e.printStackTrace();
			
		} catch (IllegalStateException e) {
			Log.e(LOG, "setDataSource error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG, "setDataSource error: " + e.getMessage());
			e.printStackTrace();
			
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		initLayout();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		// TODO: save state and cleanup bitmaps!
		
	}
	
	private void initLayout() {
		View mediaHolder_ = LayoutInflater.from(a).inflate(R.layout.editors_video, null);
		
		videoSeekBar = (ProgressBar) mediaHolder_.findViewById(R.id.video_seek_bar);
		
		mediaHolder.addView(mediaHolder_);
	}

	@Override
	public void onMediaScanned(Uri uri) {
		videoUri = uri;
		
		initVideo();
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v(LOG, "surfaceCreated Called");
		if (mediaPlayer != null) {
			mediaPlayer.setDisplay(holder);
			try {
				mediaPlayer.prepare();
				duration = mediaPlayer.getDuration();

				videoSeekBar.setMax((int) duration);

			} catch (Exception e) {
				Log.v(LOG, "IllegalStateException " + e.getMessage());
				e.printStackTrace();
				
			}

			mediaPlayer.seekTo(currentCue);
		}

		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}
}
