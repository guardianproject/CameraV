package info.guardianproject.iocipher.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

public class CustomMediaPlayer extends MediaPlayer implements OnCompletionListener {
	
	private static final String TAG = "CustomMediaPlayer";
	MediaPlayer mp;
	private int interval = 0;
	private int count = 0;
	private int repeat;
	
	public CustomMediaPlayer(Context context, int resid) {
		mp = MediaPlayer.create(context, resid);
		mp.setOnCompletionListener(this);
	}
	
	/**
	 * @param repeat play it 'repeat' times
	 */
	public void play(int repeat) {
		this.repeat = repeat;
		Log.v(TAG, "playing: " + repeat + " times");
		
	    int i = 0;
		do {
			if (mp.isPlaying()) { 
				//Log.v(TAG, "already playing...");
				continue;
			}
			
			mp.start();
			i++;
			
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		    
		} while (i<=repeat);
		
	} 

	public void onCompletion(MediaPlayer arg0) {

	}
	
	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
}

