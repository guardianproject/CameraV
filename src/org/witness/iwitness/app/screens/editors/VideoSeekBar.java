package org.witness.iwitness.app.screens.editors;

import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;;

public class VideoSeekBar extends SeekBar implements OnSeekBarChangeListener {
	MediaPlayer mp;
	ImageView startThumb, endThumb;
	int thumbInactive = R.drawable.ic_videol_mark_un;
	int thumbActive = R.drawable.ic_videol_mark_selected;
	
	private boolean isPlaying = false;
	public boolean isEditing = false;
	
	private final static String LOG = App.Editor.LOG;
	
	private Runnable progressRunnable = new Runnable() {
		@Override
		public void run() {
			if(isPlaying) {
				VideoSeekBar.this.setProgress(mp.getCurrentPosition());
			}
			
			postDelayed(progressRunnable, 1000L);
		}
	};
	
	public VideoSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		startThumb = new ImageView(context);
		endThumb = new ImageView(context);
	}
	
	public void init(MediaPlayer mp) {
		this.mp = mp;
		Log.d(LOG, "initing seek bar (duration " + mp.getDuration() + ")");
		
		setMax(mp.getDuration());
		post(progressRunnable);
		setOnSeekBarChangeListener(this);
	}
	
	public void update() {
		setProgress(mp.getCurrentPosition());
	}
	
	public void play() {
		isPlaying = true;
	}
	
	public void pause() {
		isPlaying = false;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser) {
			mp.seekTo(progress);
		}
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		Log.d(LOG, "start tracking touch");
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		Log.d(LOG, "stop tracking touch");
		
	}

	

}
