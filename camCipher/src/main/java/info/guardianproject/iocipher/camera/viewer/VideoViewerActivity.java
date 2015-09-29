
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
package info.guardianproject.iocipher.camera.viewer;

import info.guardianproject.iocipher.camera.R;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class VideoViewerActivity extends Activity implements
OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener,
OnVideoSizeChangedListener, SurfaceHolder.Callback {

private static final String TAG = "MediaPlayerDemo";
private int mVideoWidth;
private int mVideoHeight;
private MediaPlayer mMediaPlayer;
private SurfaceView mPreview;
private SurfaceHolder holder;
private Bundle extras;
private static final String MEDIA = "media";
private static final int LOCAL_AUDIO = 1;
private static final int STREAM_AUDIO = 2;
private static final int RESOURCES_AUDIO = 3;
private static final int LOCAL_VIDEO = 4;
private static final int STREAM_VIDEO = 5;
private boolean mIsVideoSizeKnown = false;
private boolean mIsVideoReadyToBePlayed = false;

/**
* 
* Called when the activity is first created.
*/
@Override
public void onCreate(Bundle icicle) {
super.onCreate(icicle);

//prevent screenshots
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
				WindowManager.LayoutParams.FLAG_SECURE);
		
		
setContentView(R.layout.videoview);
mPreview = (SurfaceView) findViewById(R.id.surface_video);
holder = mPreview.getHolder();
holder.addCallback(this);
holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
extras = getIntent().getExtras();

}

private void playVideo(Uri uri) {
doCleanUp();
try {


  // Create a new media player and set the listeners
  mMediaPlayer = new MediaPlayer();
  mMediaPlayer.setDataSource(this, uri);
  mMediaPlayer.setDisplay(holder);
  mMediaPlayer.prepare();
  mMediaPlayer.setOnBufferingUpdateListener(this);
  mMediaPlayer.setOnCompletionListener(this);
  mMediaPlayer.setOnPreparedListener(this);
  mMediaPlayer.setOnVideoSizeChangedListener(this);
  mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

} catch (Exception e) {
  Log.e(TAG, "error: " + e.getMessage(), e);
}
}

public void onBufferingUpdate(MediaPlayer arg0, int percent) {
Log.d(TAG, "onBufferingUpdate percent:" + percent);

}

public void onCompletion(MediaPlayer arg0) {
Log.d(TAG, "onCompletion called");
finish();
}

public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
Log.v(TAG, "onVideoSizeChanged called");
if (width == 0 || height == 0) {
  Log.e(TAG, "invalid video width(" + width + ") or height(" + height
      + ")");
  return;
}
mIsVideoSizeKnown = true;
mVideoWidth = width;
mVideoHeight = height;
if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
  startVideoPlayback();
}
}

public void onPrepared(MediaPlayer mediaplayer) {
Log.d(TAG, "onPrepared called");
mIsVideoReadyToBePlayed = true;
if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
  startVideoPlayback();
}
}

public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
Log.d(TAG, "surfaceChanged called");

}

public void surfaceDestroyed(SurfaceHolder surfaceholder) {
Log.d(TAG, "surfaceDestroyed called");
}

public void surfaceCreated(SurfaceHolder holder) {
	
Log.d(TAG, "surfaceCreated called");
playVideo(getIntent().getData());

}

@Override
protected void onPause() {
super.onPause();
releaseMediaPlayer();
doCleanUp();
}

@Override
protected void onDestroy() {
super.onDestroy();
releaseMediaPlayer();
doCleanUp();
}

private void releaseMediaPlayer() {
if (mMediaPlayer != null) {
  mMediaPlayer.release();
  mMediaPlayer = null;
}
}

private void doCleanUp() {
mVideoWidth = 0;
mVideoHeight = 0;
mIsVideoReadyToBePlayed = false;
mIsVideoSizeKnown = false;
}

private void startVideoPlayback() {
Log.v(TAG, "startVideoPlayback");
holder.setFixedSize(mVideoWidth, mVideoHeight);
mMediaPlayer.start();
}
}