package org.witness.iwitness.app.screens.popups;

import org.witness.iwitness.R;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

public class AudioNotePopup extends Popup implements OnClickListener {
	ProgressBar progress;
	ImageButton playPause, recordStop;
	Button commit;
	
	public AudioNotePopup(Activity a) {
		super(a, R.layout.popup_audio_note);
		
		progress = (ProgressBar) layout.findViewById(R.id.audio_progress);
		playPause = (ImageButton) layout.findViewById(R.id.audio_play_pause_toggle);
		playPause.setOnClickListener(this);
		
		recordStop = (ImageButton) layout.findViewById(R.id.audio_record_stop_toggle);
		recordStop.setOnClickListener(this);
		
		commit = (Button) layout.findViewById(R.id.audio_commit);
		commit.setOnClickListener(this);
		
		Show();
	}

	@Override
	public void onClick(View v) {
		if(v == playPause) {
			
		} else if(v == recordStop) {
			
		} else if(v == commit) {
			
		}
		
	}

}
