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
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.MediaController.MediaPlayerControl;

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "MjpegView";
	
    public final static int SIZE_STANDARD   = 1; 
    public final static int SIZE_BEST_FIT   = 4;
    public final static int SIZE_FULLSCREEN = 8;

    //private MjpegViewThread thread;
    private MjpegInputStream mIn = null;    
    //private boolean mRun = false;
    private boolean mIsPrepared = false;
    private boolean surfaceDone = false;    

    private MediaController mMediaController;
	private boolean alreadyStarted = false;
	private Context mContext;

	private MjpegPlayer mPlayer;
    
/*
    public class MjpegViewThread extends Thread {
        private SurfaceHolder mSurfaceHolder;
        private int frameCounter = 0;
        private long start, fpsstart;
        private long pause;
        private Bitmap ovl;
        private int ovlpostop = 5;
        
        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) { mSurfaceHolder = surfaceHolder; }

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

        public void setSurfaceSize(int width, int height) {
            synchronized(mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            }
        }

        private Bitmap makeOverlay(Paint p, String text) {
            Rect b = new Rect();
            p.getTextBounds(text, 0, text.length(), b);
            int bwidth  = b.width()+2;
            int bheight = b.height()+2;
            Bitmap bm = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bm);
            p.setColor(overlayBackgroundColor);
            c.drawRect(0, 0, bwidth, bheight, p);
            p.setColor(overlayTextColor);
            c.drawText(text, -b.left+1, (bheight/2)-((p.ascent()+p.descent())/2)+1, p);
            return bm;           
        }

		private void drawOverlay(Canvas c, Paint p, Bitmap overlay) {
			c.drawBitmap(overlay, 5, ovlpostop, p);
			ovlpostop += 5 + overlay.getHeight();
		}
        
        public void run() {
            start = System.currentTimeMillis();
            fpsstart = System.currentTimeMillis();
            Log.v("MjpegView", "Start at " + start);
            PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
            Bitmap bm;
            int left, top;
            Rect destRect = null;
            Canvas c = null;
            Paint p = new Paint();
            String fps = "";
            
            while (mRun) {
                if(surfaceDone) {
                    try {
                        c = mSurfaceHolder.lockCanvas();
                        synchronized (mSurfaceHolder) {
                            try {
                            	ovlpostop = 5;	//set start position (top) for overlay-table
                            	
                            	if (state == STATE_PLAY) {
                            		
                                	bm = mIn.readMjpegFrame();
                                	destRect = destRect(bm.getWidth(),bm.getHeight());
                                    c.drawColor(Color.BLACK);
                                    c.drawBitmap(bm, null, destRect, p);
                                    
                                    //*
                                    if(showFps) {
                                        //p.setXfermode(mode);
                                        if(ovl != null) {                                      
                                            top = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom-ovl.getHeight();
                                            left = ((ovlPos & 8) == 8) ? destRect.left : destRect.right -ovl.getWidth();
                                            //c.drawBitmap(ovl, left, top, p);
                                            drawOverlay(c, p, ovl);
                                        }
                                        //p.setXfermode(null);
                                        frameCounter++;
                                        if((System.currentTimeMillis() - start) >= 1000) {
                                            fps = String.valueOf(frameCounter)+"fps";
                                            Log.v(TAG, "FPS: " + fps);
                                            frameCounter = 0; 
                                            fpsstart = System.currentTimeMillis();
                                            ovl = makeOverlay(overlayPaint, fps);
                                        }
                                    }
                                    //*
                            	}
                            	else if (state == STATE_PAUSE){
                            		String time = "T-" + (System.currentTimeMillis()-pause)/1000 + " sec";
                            		Bitmap ovl1 = makeOverlay(overlayPaint, "PAUSED");
                            		//c.drawBitmap(ovl1, 5, 5, p);
                            		
                            		Bitmap ovl2 = makeOverlay(overlayPaint, time);
                            		drawOverlay(c, p, ovl1);
                            		drawOverlay(c, p, ovl2);
                                    //c.drawBitmap(ovl1, 5, 5+2*ovl1.getHeight(), p);
                            	}
                            	setDuration(System.currentTimeMillis()-start);


                                
                            } catch (IOException e) {}
                        }
                    } finally {
                    	if (c != null) {
                    		mSurfaceHolder.unlockCanvasAndPost(c); 
                    	}
                    }
                }
            }
            synchronized(mSurfaceHolder) {
	            c = mSurfaceHolder.lockCanvas();
	            c.drawColor(Color.BLACK);
	            mSurfaceHolder.unlockCanvasAndPost(c);
            }
        }
    }
*/
	
	
    private void init(Context context) {
    	mContext = context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        //thread = new MjpegViewThread(holder, context);
        setFocusable(true);
        
        mPlayer = new MjpegPlayer(holder, mContext, mIn);
        mPlayer.setDisplay(getWidth(), getHeight(), MjpegView.SIZE_BEST_FIT);
        mIsPrepared = true;
        attachMediaController();
        mPlayer.showFps(true);
    }
    
    public void startPlayback() {
    	if (alreadyStarted) {
            Log.v(TAG, "Creating new MjpegPlayer (MjpegPlayer)");
    		//thread = new MjpegViewThread(holder, mContext);
            init(mContext);
    	}
    	
    	if(mIn != null) {
            //mRun = true;
    		Log.v(TAG, "Starting Playback:");
            alreadyStarted = true;
            mPlayer.startPlayback();
            //thread.start();
        }
    }

    public void stopPlayback() {
        //mRun = false;
        mIsPrepared = false;
        mPlayer.stopPlayback();
        
        /*
        boolean retry = true;
        while(retry) {
            try {
                //thread.join();
                retry = false;
            } catch (InterruptedException e) {}
        }*/
    }

    public MjpegView(Context context, AttributeSet attrs) { super(context, attrs); init(context); }
    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) { mPlayer.setSurfaceSize(w, h); }

    public void surfaceDestroyed(SurfaceHolder holder) { 
        surfaceDone = false; 
        stopPlayback(); 
    }

    public MjpegView(Context context) { 
        super(context); 
        init(context); 
        }    
    public void surfaceCreated(SurfaceHolder holder) { 
        surfaceDone = true; 
        }
    public void showFps(boolean b) { 
        mPlayer.showFps(b);
        }
    public void setSource(MjpegInputStream source) { 
        mIn = source; 
        mPlayer.setSource(mIn);
    }
    
    /*
    public void setOverlayPaint(Paint p) { 
        overlayPaint = p; 
        }
    public void setOverlayTextColor(int c) { 
        overlayTextColor = c; 
        }
    public void setOverlayBackgroundColor(int c) { 
        overlayBackgroundColor = c; 
        }
    public void setOverlayPosition(int p) { 
        ovlPos = p; 
        }
	*/
    
    public void setMediaController(MediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
        
        //enable the MediaController. Without this the buttons will not work! (in the emulator...)
        
    }

    private View.OnClickListener next = new OnClickListener() {
		public void onClick(View v) {
			Log.v(TAG, "Clicked NEXT");
			mPlayer.seekToLive();
		}
	};
    
	private View.OnClickListener prev = new OnClickListener() {
		public void onClick(View v) {
			Log.v(TAG, "Clicked PREV");
			mPlayer.seekToBegin();
		}
	};
	
	private void attachMediaController() {
        if (mMediaController != null) {
            mMediaController.setMediaPlayer(mPlayer);
            View anchorView = this.getParent() instanceof View ?
                    (View)this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(mIsPrepared);
            mMediaController.setPrevNextListeners(next, prev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mIsPrepared && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }
    
    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (mIsPrepared && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (mIsPrepared &&
                keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL &&
                //mMediaPlayer != null &&
                mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    mMediaController.show();
                } else {
                	start();
                    mMediaController.hide();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }
    
    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) { 
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }
	
//	public void pause() {
//		mPlayer.pause();
//		
//		//this.thread.pause = System.currentTimeMillis();
//		Log.v(TAG, "Pause Button pressed");
//		/*
//		if (isPlaying() && state==STATE_PLAY) {
//			state = STATE_PAUSE;
//			Log.v(TAG, "new state: pause");
//		}
//		else {
//			state = STATE_PLAY;
//			Log.v(TAG, "new state: play");
//		}
//		*/
//		
//	}

	public void start() {
		//Toast.makeText(mContext, "START!",  Toast.LENGTH_SHORT).show();
		Log.v(TAG, "START!");
		startPlayback();
	}

    
	public boolean isPlaying() {
		return mPlayer.isPlaying();
	}
    
}
