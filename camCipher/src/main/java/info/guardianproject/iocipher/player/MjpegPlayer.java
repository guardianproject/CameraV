package info.guardianproject.iocipher.player;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;
import android.widget.MediaController.MediaPlayerControl;

public class MjpegPlayer extends Thread implements MediaPlayerControl {
	private static final String TAG = "MjpegPlayer";
	
    private SurfaceHolder mSurfaceHolder;
    private MjpegInputStream mIn = null;  
    SeekableStream cacheStream = null;
    private boolean mRun = false;
    
    private int frameCounter = 0;
    private int dispWidth;
    private int dispHeight;
    private int displayMode;
    
    private long start, fpsstart;
    private long pause;
	private int duration;
    private Bitmap fpsoverlay;
    private boolean showFps = false;
    
    public static final int STATE_NOTPLAYING = -1;
    public static final int STATE_PLAYING = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_SEEKED = 2;
	public int state = STATE_NOTPLAYING;

	private Context mContext;

	private PowerManager.WakeLock mWakeLock = null;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;
    
    public MjpegPlayer(SurfaceHolder surfaceHolder, Context context) { 
    	mSurfaceHolder = surfaceHolder; 
    	mContext = context;
    }
  
    
	public MjpegPlayer(SurfaceHolder holder, Context context, MjpegInputStream mIn) {
		this(holder, context);
		setSource(mIn);
	}


	 public void setWakeMode(Context context, int mode) {
        boolean washeld = false;
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                washeld = true;
                mWakeLock.release();
            }
            mWakeLock = null;
        }

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(mode|PowerManager.ON_AFTER_RELEASE, MjpegPlayer.class.getName());
        mWakeLock.setReferenceCounted(false);
        if (washeld) {
            mWakeLock.acquire();
        }
    }
    
    /**
     * Control whether we should use the attached SurfaceHolder to keep the
     * screen on while video playback is occurring.  This is the preferred
     * method over {@link #setWakeMode} where possible, since it doesn't
     * require that the application have permission for low-level wake lock
     * access.
     * 
     * @param screenOn Supply true to keep the screen on, false to allow it
     * to turn off.
     */
    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (mScreenOnWhilePlaying != screenOn) {
            mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
        }
    }
    
    private void stayAwake(boolean awake) {
        if (mWakeLock != null) {
            if (awake && !mWakeLock.isHeld()) {
                mWakeLock.acquire();
            } else if (!awake && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
        mStayAwake = awake;
        updateSurfaceScreenOn();
    }
	    
	
	private Rect destRect(int bmw, int bmh) {
        int tempx;
        int tempy;
        if (displayMode == MjpegView.SIZE_STANDARD) {
            tempx = (dispWidth / 2) - (bmw / 2);
            tempy = (dispHeight / 2) - (bmh / 2);
            return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
        }
        if (displayMode == MjpegView.SIZE_BEST_FIT) {
            float bmasp = (float) bmw / (float) bmh;
            bmw = dispWidth;
            bmh = (int) (dispWidth / bmasp);
            if (bmh > dispHeight) {
                bmh = dispHeight;
                bmw = (int) (dispHeight * bmasp);
            }
            tempx = (dispWidth / 2) - (bmw / 2);
            tempy = (dispHeight / 2) - (bmh / 2);
            return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
        }
        if (displayMode == MjpegView.SIZE_FULLSCREEN) return new Rect(0, 0, dispWidth, dispHeight);
        return null;
    }

    private void updateSurfaceScreenOn() {
        if (mSurfaceHolder != null) {
            mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
        }
    }
	
    public void setSurfaceSize(int width, int height) {
        synchronized(mSurfaceHolder) {
            dispWidth = width;
            dispHeight = height;
        }
    }
    
    public void setDisplay(int dispWidth,  int dispHeight, int displayMode) {
    	this.dispWidth = dispWidth;
    	this.dispHeight = dispHeight;
    	this.displayMode = displayMode;
    }
    
    public void setDisplayMode(int s) { 
        displayMode = s; 
    }
    
    public void run() {
        start = System.currentTimeMillis();
        fpsstart = System.currentTimeMillis();
        Log.v("MjpegView", "Start at " + start);
        PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
        Bitmap bm;
        Bitmap bm_pause = null;
        Rect destRect = null;
        Canvas c = null;
        Paint p = new Paint();
        String fps = "";
        OverlayHandler overlay = null;
        
        
        
        while (mRun) {
            try {
                c = mSurfaceHolder.lockCanvas();
                if (overlay == null) overlay = new OverlayHandler(c, p);
                
                synchronized (mSurfaceHolder) {
                    try {
                    	overlay.setOvltop(5);
                    	
                    	bm = mIn.readMjpegFrame();
                    	if (bm == null) { 
                    		Log.v(TAG, "Bitmap is null!");
                    		continue;
                    	}
                    	
                    	destRect = destRect(bm.getWidth(),bm.getHeight());
                    	
                    	if (state == STATE_PLAYING) {	
                            c.drawColor(Color.BLACK);
                            c.drawBitmap(bm, null, destRect, p);
                            bm_pause = null;
                    	}
                    	else if (state == STATE_PAUSE){
                    		if (bm_pause == null) bm_pause = bm;
                            c.drawColor(Color.BLACK);
                            c.drawBitmap(bm_pause, null, destRect, p);
                            
                    		String time = "T-" + (duration - (int)(pause-start))/1000 + " sec";
                    		Bitmap ovl1 = overlay.makeOverlay("PAUSED");
                    		Bitmap ovl2 = overlay.makeOverlay(time);
                    		overlay.drawOverlay(ovl1);
                    		overlay.drawOverlay(ovl2);
                    	}
                    	else if (state == STATE_SEEKED) {
                    		c.drawColor(Color.BLACK);
                            c.drawBitmap(bm, null, destRect, p);
                            bm_pause = null;
                            
                            Bitmap ovl1 = overlay.makeOverlay("SEEKED");
                            overlay.drawOverlay(ovl1);
                    	}
                    	
                    	//*/
                        if(showFps) {
                            //p.setXfermode(mode);
                            if(fpsoverlay != null) {                                      
                                overlay.drawOverlay(fpsoverlay);
                            }
                            //p.setXfermode(null);
                            frameCounter++;
                            if((System.currentTimeMillis() - fpsstart) >= 1000) {
                                fps = String.valueOf(frameCounter)+"fps";
                                //Log.v(TAG, "FPS: " + fps);
                                frameCounter = 0; 
                                fpsstart = System.currentTimeMillis();
                                fpsoverlay = overlay.makeOverlay(fps);
                            }
                        }
                        //*/
                    	
                    	setDuration(System.currentTimeMillis()-start);

                    } catch (IOException e) {}
                }
            } finally {
            	if (c != null) {
            		mSurfaceHolder.unlockCanvasAndPost(c); 
            	}
            }
        }
        synchronized(mSurfaceHolder) {
            c = mSurfaceHolder.lockCanvas();
            c.drawColor(Color.BLACK);
            mSurfaceHolder.unlockCanvasAndPost(c);
        }
    }
    
	private void setDuration(long l) {
		duration = (int) l;
	}
    
	public void startPlayback() {
		state = STATE_PLAYING;
		mRun = true;
		this.start();
	}
	
    public boolean isPlaying() {
    	return mRun;
    }
	
    public void stopPlayback() { 
    	mRun = false;
        boolean retry = true;
        while(retry) {
            try {
                this.join();
                retry = false;
            } catch (InterruptedException e) {}
        }
    }
    
	public void pause() {
		pause = System.currentTimeMillis();
		
		if (isPlaying() && state==STATE_PLAYING) {
			state = STATE_PAUSE;
			Log.v(TAG, "new state: pause");
		}
		else {
			state = STATE_PLAYING;
			Log.v(TAG, "new state: play");
		}
	}

	public int getCurrentPosition() {
	
		switch (state) {
		case STATE_PLAYING:
			return duration;
		case STATE_PAUSE:
			//Log.v(TAG, "CurrentPosition: " + (int)(pause-start));
			return (int)(pause-start);
		case STATE_SEEKED:
			double pos_ratio;
			try {
				pos_ratio = mIn.getPosition();
				Log.v(TAG, "File Position Ratio: " + pos_ratio);
			} catch (IOException e) {
				Log.v(TAG, "could not get position...");
				pos_ratio = 0.5;
				e.printStackTrace();
			}
			return (int) (pos_ratio * duration);
		default:
			return 0;
		}
	}

	public int getDuration() {
		return (int)duration;
	}

	public void showFps(boolean b) {
	    showFps = b;
	}

	public void setSource(MjpegInputStream source) {
		mIn = source;
	}

	public void seekTo(int pos) {
		state = STATE_SEEKED;
		Toast.makeText(mContext, "seek to: " +pos,  Toast.LENGTH_SHORT).show();
		
		double ratio = (double)pos / duration;
		Log.v(TAG, "seek to: " + pos + " or better: to " + ratio);
		try {
			mIn.seekTo(ratio);
		} catch (IOException e) {
			Log.e(TAG, "Couldn't seek to " + ratio);
			e.printStackTrace();
		}
	}
	
	public void seekToBegin() {
		seekTo(0);
	}
	
	public void seekToLive() {
		state = STATE_PLAYING;
		try {
			mIn.seekToLive();
		} catch (IOException e) {
			Log.e(TAG, "couldn't seek to live view");
			e.printStackTrace();
		}
	}

	public int getBufferPercentage() {
		return 100;
	}


	public boolean canPause() {
		return true;
	}

	public boolean canSeekBackward() {
		return true;
	}

	public boolean canSeekForward() {
		return true;
	}

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public void release() {
        if (mWakeLock != null) mWakeLock.release();
        updateSurfaceScreenOn();
//        mOnPreparedListener = null;
//        mOnBufferingUpdateListener = null;
//        mOnCompletionListener = null;
//        mOnSeekCompleteListener = null;
//        mOnErrorListener = null;
//        _release();
    }
}