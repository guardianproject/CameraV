package org.witness.informacam.app.screens.editors;

import org.witness.informacam.app.utils.Constants.App;
import org.witness.informacam.models.media.IVideoRegion;
import org.ibanet.informacam.R;

import com.efor18.rangeseekbar.RangeSeekBar;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;;

public class VideoSeekBar extends SeekBar implements OnSeekBarChangeListener {
	MediaPlayer mp;
	int thumbInactive = R.drawable.ic_videol_mark_un;
	int thumbActive = R.drawable.ic_videol_mark_selected;
	
	private boolean isPlaying = false;
	public boolean isEditing = false;
	
	RangeSeekBar<Integer> endpointBar;
	Context context;
	
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
		this.context = context;
	}
	
	public RangeSeekBar<Integer> init(MediaPlayer mp) {
		this.mp = mp;
		
		setMax(mp.getDuration());
		post(progressRunnable);
		setOnSeekBarChangeListener(this);
		
		endpointBar = new RangeSeekBar<Integer>(0, mp.getDuration(), context);		
		return endpointBar;
		
	}
	
	public void update() {
		setProgress(mp.getCurrentPosition());
	}
	
	public void play() {
		isPlaying = true;
		hideEndpoints();
	}
	
	public void pause() {
		isPlaying = false;
	}
	
	public void showEndpoints(IVideoRegion region) {
		Log.d(LOG, "showing endpoints for " + region.asJson().toString());
		
		endpointBar.setVisibility(View.VISIBLE);
		setVisibility(View.GONE);
		
		endpointBar.setSelectedMinValue((int) region.bounds.startTime);
		endpointBar.setSelectedMaxValue((int) region.bounds.endTime);
	}
	
	public void hideEndpoints() {
		setVisibility(View.VISIBLE);
		endpointBar.setVisibility(View.GONE);
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
