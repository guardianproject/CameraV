package org.witness.iwitness.app.screens.popups;

import java.util.ArrayList;
import java.util.List;

import info.guardianproject.odkparser.Constants.RecorderState;
import info.guardianproject.odkparser.utils.QD;
import info.guardianproject.odkparser.widgets.ODKSeekBar;
import info.guardianproject.odkparser.widgets.ODKSeekBar.OnMediaRecorderStopListener;

import org.witness.informacam.InformaCam;
import org.witness.informacam.informa.embed.VideoConstructor;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.TimeUtility;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AudioNotePopup extends Popup implements OnClickListener, OnCompletionListener, OnMediaRecorderStopListener {
	RelativeLayout actionToggle;
	LinearLayout actionHolder;
	ImageView actionToggleIcon, actionToggleIcon2;
	TextView actionToggleLabel, actionClock;
	Button actionRedo, actionPlay, actionDone;
	public ODKSeekBar progress;
	
	int state = RecorderState.IS_IDLE;
	
	int recordRes, pauseRes, resumeRes, playRes;
	int recordLabel, pauseLabel, resumeLabel;
	int res, label;

	Handler h = new Handler();
	List<java.io.File> recordingFiles;

	IForm form;
	InformaCam informaCam = InformaCam.getInstance();

	public AudioNotePopup(Activity a, IForm form) {
		super(a, R.layout.popup_audio_note);
		this.form = form;
		
		actionToggle = (RelativeLayout) layout.findViewById(R.id.audio_action_toggle);
		actionToggle.setOnClickListener(this);
		
		actionHolder = (LinearLayout) layout.findViewById(R.id.audio_action_holder);
		
		actionToggleIcon = (ImageView) layout.findViewById(R.id.audio_action_toggle_icon);
		actionToggleIcon2 = (ImageView) layout.findViewById(R.id.audio_action_toggle_icon_2);
		
		actionToggleLabel = (TextView) layout.findViewById(R.id.audio_action_toggle_label);
		actionClock = (TextView) layout.findViewById(R.id.audio_action_clock);

		recordRes = R.drawable.ic_audio_btn_record;
		pauseRes = R.drawable.ic_audio_btn_pause;
		resumeRes = R.drawable.ic_audio_btn_resume;
		playRes = R.drawable.ic_audio_play;

		progress = (ODKSeekBar) layout.findViewById(R.id.audio_seekbar);
		
		actionRedo = (Button) layout.findViewById(R.id.audio_action_redo);
		actionRedo.setOnClickListener(this);
		
		actionPlay = (Button) layout.findViewById(R.id.audio_action_play);
		actionPlay.setOnClickListener(this);
		
		actionDone = (Button) layout.findViewById(R.id.audio_action_done);
		actionDone.setOnClickListener(this);
		
		initData();
		Show();
	}
	
	private void updateClock(int time) {
		Log.d(LOG, "counter: " + time);
		actionClock.setText(TimeUtility.millisecondsToStopwatchTime(time));
	}
	
	private void updateLayout() {
		if(progress.mp.getDuration() == 0) {
			updateLayout(false);
		} else {
			updateLayout(true);
		}
	}
	
	private void updateLayout(boolean showHolder) {
		res = recordRes;
		label = R.string.record;
		
		if(showHolder) {
			actionClock.setVisibility(View.VISIBLE);
			actionHolder.setVisibility(View.VISIBLE);
			
			res = resumeRes;
			label = R.string.resume;
			
			updateClock(progress.mp.getDuration());
		} else {
			actionClock.setVisibility(View.INVISIBLE);
			actionHolder.setVisibility(View.INVISIBLE);
		}
		
		if(state == RecorderState.IS_RECORDING) {
			actionToggleIcon2.setVisibility(View.VISIBLE);
			actionToggleLabel.setVisibility(View.GONE);
			res = pauseRes;
		} else {
			actionToggleIcon2.setVisibility(View.GONE);
			actionToggleLabel.setVisibility(View.VISIBLE);
			actionToggleLabel.setText(a.getString(label));
		}
		
		actionToggleIcon.setImageDrawable(a.getResources().getDrawable(res));
		
	}
	
	private void initData() {
		progress.init(new java.io.File(Storage.EXTERNAL_DIR, "tmprecord_" + System.currentTimeMillis() + ".3gp"), this);
		
		QD qd = form.getQuestionDefByTitleId(Forms.OverviewForm.AUDIO_NOTE_PROMPT);
		if(qd.hasInitialValue) {
			progress.setRawAudioData(qd.initialValue.getBytes());
			res = resumeRes;
			label = R.string.resume;
			
			updateClock(progress.mp.getDuration());
		}
		
		updateLayout(qd.hasInitialValue);
		this.form.associate(progress, Forms.OverviewForm.AUDIO_NOTE_PROMPT);
	}
	

	@Override
	public void onClick(View v) {
		if(v == actionToggle) {
			switch(state) {
			case RecorderState.IS_IDLE:
				progress.record();
				state = RecorderState.IS_RECORDING;
				break;
			case RecorderState.IS_RECORDING:
				progress.stop();
				state = RecorderState.IS_IDLE;
				break;
			case RecorderState.IS_PLAYING:
				progress.pause();
				state = RecorderState.IS_IDLE;
				v.performClick();
				break;
			}
		} else if(v == actionDone) {
			cancel();
			return;
		} else if(v == actionPlay) {
			if(state != RecorderState.IS_IDLE) {
				if(state == RecorderState.IS_RECORDING) {
					progress.stop();
				} else if(state == RecorderState.IS_PLAYING) {
					progress.pause();
				}
				state = RecorderState.IS_IDLE;
			}
			
			progress.play();
			state = RecorderState.IS_PLAYING;
		} else if(v == actionRedo) {
			if(recordingFiles != null) {
				recordingFiles.clear();
				recordingFiles = null;
			}
			
			newRecording();
		}
		
		updateLayout();
	}
	
	private java.io.File newRecording() {
		java.io.File recordingFile = new java.io.File(Storage.EXTERNAL_DIR, "tmprecord_" + System.currentTimeMillis() + ".3gp");
		progress.recordingFile = recordingFile;
		
		return recordingFile;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(LOG, "FINISHED PLAYING MEDIA FILE");
		
		progress.pause();
		mp.seekTo(0);
		
		state = RecorderState.IS_IDLE;
	}

	@Override
	public void onMediaRecorderStop() {
		Log.d(LOG, "HERE I CALL ON MEDIA RECORDER STOP");
		
	}

}
