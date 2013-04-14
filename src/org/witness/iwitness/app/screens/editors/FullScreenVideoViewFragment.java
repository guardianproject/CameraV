package org.witness.iwitness.app.screens.editors;

import java.io.IOException;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.media.IVideo;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.storage.InformaCamMediaScanner;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

public class FullScreenVideoViewFragment extends FullScreenViewFragment implements OnCompletionListener, 
OnErrorListener, OnInfoListener, OnBufferingUpdateListener, OnPreparedListener, OnSeekCompleteListener,
OnVideoSizeChangedListener, SurfaceHolder.Callback, OnTouchListener, MediaController.MediaPlayerControl {
	IVideo media;

	MediaMetadataRetriever retriever = new MediaMetadataRetriever();
	VideoView videoView;
	SurfaceHolder surfaceHolder;

	MediaPlayer mediaPlayer;
	MediaController mediaController;

	LinearLayout videoControlsHolder;
	VideoSeekBar videoSeekBar;
	ImageButton playPauseToggle;

	Uri videoUri;
	java.io.File videoFile;

	long duration = 0L;
	int currentCue = 0;

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);

		media = new IVideo();
		media.inflate(((EditorActivity) a).media.asJson());
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

			mediaPlayer.setDisplay(surfaceHolder);
			mediaPlayer.prepare();
			duration = mediaPlayer.getDuration();

			mediaPlayer.start();
			mediaPlayer.setVolume(1f, 1f);
			mediaPlayer.seekTo(currentCue);
			mediaPlayer.pause();
			
			videoSeekBar.init(mediaPlayer);

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
	public void onDetach() {
		super.onDetach();
		// TODO: save state and cleanup bitmaps!

	}

	@SuppressWarnings("deprecation")
	@Override
	protected void initLayout() {
		super.initLayout();

		View mediaHolder_ = LayoutInflater.from(a).inflate(R.layout.editors_video, null);

		videoView = (VideoView) mediaHolder_.findViewById(R.id.video_view);

		LayoutParams vv_lp = videoView.getLayoutParams();
		vv_lp.width = dims[0];
		vv_lp.height = (int) (((float) media.dcimEntry.exif.height) / ((float) media.dcimEntry.exif.width) * (float) dims[0]);

		videoView.setLayoutParams(vv_lp);
		videoView.setOnTouchListener(this);
		
		surfaceHolder = videoView.getHolder();
		Log.d(LOG, "video view dims: " + videoView.getWidth() + " x " + videoView.getHeight());
		Log.d(LOG, "surface holder dims: " + surfaceHolder.getSurfaceFrame().width() + " x " + surfaceHolder.getSurfaceFrame().height());
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		videoControlsHolder = (LinearLayout) mediaHolder_.findViewById(R.id.video_controls_holder);		

		videoSeekBar = (VideoSeekBar) mediaHolder_.findViewById(R.id.video_seek_bar);
		playPauseToggle = (ImageButton) mediaHolder_.findViewById(R.id.video_play_pause_toggle);
		playPauseToggle.setOnClickListener(this);

		mediaHolder.addView(mediaHolder_);

		new Thread(new Runnable() {
			@Override
			public void run() {
				// copy from iocipher to local :(
				videoFile = new java.io.File(Storage.EXTERNAL_DIR, media.dcimEntry.name);
				informaCam.ioService.saveBlob(informaCam.ioService.getBytes(media.dcimEntry.fileName, Type.IOCIPHER), videoFile, true);
				InformaCamMediaScanner icms = new InformaCamMediaScanner(FullScreenVideoViewFragment.this.a, videoFile) {
					@Override
					public void onScanCompleted(String path, Uri uri) {
						videoUri = uri;
						initVideo();
					}
				};
			}
		}).start();

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.v(LOG, "surfaceChanged called");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v(LOG, "surfaceCreated Called");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v(LOG, "surfaceDestroyed called");

	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		Log.v(LOG, "onVideoSizeChanged called, new width: " + width + ", new height: " + height);
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		Log.v(LOG, "onSeekComplete called (and at position " + mediaPlayer.getCurrentPosition() + ")");

	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		Log.v(LOG, "onPrepared called");
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {}

	@Override
	public boolean onError(MediaPlayer mp, int whatInfo, int extra) {
		Log.v(LOG, "onError called " + whatInfo + " (extra: " + extra + ")");
		if (whatInfo == MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING) {
			Log.v(LOG, "Media Info, Media Info Bad Interleaving " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
			Log.v(LOG, "Media Info, Media Info Not Seekable " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_UNKNOWN) {
			Log.v(LOG, "Media Info, Media Info Unknown " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
			Log.v(LOG, "MediaInfo, Media Info Video Track Lagging " + extra);
		} else if (whatInfo == MediaPlayer.MEDIA_INFO_METADATA_UPDATE) { 
			Log.v(LOG, "MediaInfo, Media Info Metadata Update " + extra); 
		} else if (whatInfo == MediaPlayer.MEDIA_ERROR_IO) {
			Log.v(LOG, "Media Info, Media Info IO error " + extra);
		} else if (whatInfo == -38) {
			Log.v(LOG, "i have no clue what error -38 is");
		}
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.v(LOG, "onCompletion called");

	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		Log.v(LOG, "onInfo called");
		return false;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);

		if(v == playPauseToggle) {
			if(mediaPlayer.isPlaying()) {
				pause();
			} else {
				start();
			}
		}
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return mediaPlayer.getDuration();
	}

	@Override
	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}

	@Override
	public void pause() {
		playPauseToggle.setImageDrawable(a.getResources().getDrawable(R.drawable.ic_videol_play));
		videoSeekBar.pause();
		mediaPlayer.pause();
	}

	@Override
	public void seekTo(int pos) {
		mediaPlayer.seekTo(pos);
		videoSeekBar.update();
	}

	@Override
	public void start() {
		playPauseToggle.setImageDrawable(a.getResources().getDrawable(R.drawable.ic_videol_pause));
		mediaPlayer.start();
		videoSeekBar.play();
	}
}
