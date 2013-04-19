package org.witness.iwitness.app.screens.popups;

import info.guardianproject.odkparser.widgets.ODKSeekBar;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class AudioNotePopup extends Popup implements OnClickListener {
	public ODKSeekBar progress;
	ImageButton playPause, recordStop;
	Button commit;

	int playRes, pauseRes, recordRes, stopRes;
	
	Handler h = new Handler();
	java.io.File recordingFile;

	InformaCam informaCam = InformaCam.getInstance();

	public AudioNotePopup(Activity a, IForm form) {
		super(a, R.layout.popup_audio_note);
		
		playPause = (ImageButton) layout.findViewById(R.id.audio_play_pause_toggle);
		playPause.setOnClickListener(this);

		recordStop = (ImageButton) layout.findViewById(R.id.audio_record_stop_toggle);
		recordStop.setOnClickListener(this);

		commit = (Button) layout.findViewById(R.id.audio_commit);
		commit.setOnClickListener(this);

		playRes = R.drawable.ic_videop_play;
		pauseRes = R.drawable.ic_videop_pause;

		progress = (ODKSeekBar) layout.findViewById(R.id.audio_progress);
		
		recordingFile = new java.io.File(Storage.EXTERNAL_DIR, "tmprecord_" + System.currentTimeMillis() + ".3gp");
		progress.init(recordingFile);
		form.associate(progress, Forms.OverviewForm.AUDIO_NOTE_PROMPT);
		
		Show();
	}
	

	

	@Override
	public void onClick(View v) {
		if(v == playPause) {
			if(progress.canPlay) {
				if(progress.isPlaying) {
					progress.pause();
				} else {
					progress.play();
				}
			}
		} else if(v == recordStop) {
			if(progress.canRecord) {
				if(progress.isRecording) {
					progress.stop();
				} else {
					progress.record();
				}
			}
		} else if(v == commit) {
			cancel();
		}

	}

}
