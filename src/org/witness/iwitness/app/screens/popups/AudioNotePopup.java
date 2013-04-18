package org.witness.iwitness.app.screens.popups;

import info.guardianproject.iocipher.File;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.Models;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class AudioNotePopup extends Popup implements OnClickListener, OnInfoListener, OnSeekBarChangeListener {
	SeekBar progress;
	ImageButton playPause, recordStop;
	Button commit;

	MediaRecorder mr;
	MediaPlayer mp;

	int playRes, pauseRes, recordRes, stopRes;
	boolean isPlaying = false;
	boolean isRecording = false;
	boolean canPlay = false;
	boolean canRecord = false;

	Handler h = new Handler();
	java.io.File recordingFile;
	byte[] rawAudioData;

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

		progress = (SeekBar) layout.findViewById(R.id.audio_progress);
		progress.setOnSeekBarChangeListener(this);
		progress.setProgress(0);

		mr = new MediaRecorder();
		mr.setAudioSource(MediaRecorder.AudioSource.MIC);
		mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		recordingFile = new java.io.File(Storage.EXTERNAL_DIR, "tmprecord_" + System.currentTimeMillis() + ".3gp");
		
		form.associate(a, progress, Forms.OverviewForm.AUDIO_NOTE_PROMPT);
		
		
		if(rawAudioData != null) {
			this.rawAudioData = rawAudioData;
			init();
		} else {
			canPlay = false;
		}

		mr.setOutputFile(recordingFile.getAbsolutePath());

		mp = new MediaPlayer();
		mp.setOnInfoListener(this);

		h.post(new Runnable() {
			@Override
			public void run() {
				if(isPlaying) {
					if(mp.getCurrentPosition() >= mp.getDuration()) {
						pause();
						mp.seekTo(0);
						return;
					}

					progress.setProgress(mp.getCurrentPosition()/1000);
				}

				h.postDelayed(this, 1000L);
			}

		});

		Show();
	}
	
	private void saveAudio() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream gos = new GZIPOutputStream(baos);
			
			java.io.FileInputStream fis = new java.io.FileInputStream(recordingFile);
			byte[] buf = new byte[1024];
			int b;
			while((b = fis.read(buf)) > 0) {
				gos.write(buf, 0, b);
			}
			
			fis.close();
			gos.finish();
			gos.close();
			
			baos.flush();
			baos.close();
			
			rawAudioData = Base64.encode(baos.toByteArray(), Base64.DEFAULT);
			
		} catch (IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
		
	}
	
	private void processAudio() {
		mp.stop();
		mp.reset();
		
		try {
			mp.setDataSource(recordingFile.getAbsolutePath());
			mp.prepare();
			
			progress.setMax(mp.getDuration()/1000);
			
		} catch (IllegalArgumentException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (SecurityException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
	}

	private void init() {
		// b64-decode, unzip audio bytes and dump it in public
		try {
			java.io.FileOutputStream fos = new java.io.FileOutputStream(recordingFile);
			
			GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(rawAudioData, Base64.DEFAULT)));
			byte[] buf = new byte[1024];
			int b;
			while((b = gzip.read(buf)) > 0) {
				fos.write(buf, 0, b);
			}
			fos.flush();
			fos.close();

			processAudio();
		} catch (IllegalArgumentException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (SecurityException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
	}

	private void play() {
		canRecord = false;
		mp.start();
		isPlaying = true;
	}

	private void pause() {
		canRecord = true;
		mp.pause();
		isPlaying = false;
	}

	private void record() {
		try {
			mr.prepare();
			mr.start();
			
			isRecording = true;
			canPlay = false;
		} catch (IllegalStateException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
		
		
	}

	private void stop() {
		canPlay = true;
		mr.stop();
		mr.release();
		isRecording = false;
		
		processAudio();
		saveAudio();
	}

	@Override
	public void onClick(View v) {
		if(v == playPause) {
			if(canPlay) {
				if(isPlaying) {
					pause();
				} else {
					play();
				}
			}
		} else if(v == recordStop) {
			if(canRecord) {
				if(isRecording) {
					stop();
				} else {
					record();
				}
			}
		} else if(v == commit) {
			
		}

	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser) {
			mp.seekTo(progress * 1000);
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}

}
