package org.witness.iwitness.app.screens.forms;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.IForm;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.TimeUtility;
import org.witness.iwitness.R;
import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.screens.popups.AudioNotePopup;
import org.witness.iwitness.app.screens.popups.RenamePopup;
import org.witness.iwitness.app.screens.popups.TextareaPopup;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverviewFormFragment extends Fragment implements OnClickListener, OnFocusChangeListener {
	View rootView, detailsView, topLevelAnnotationsView, tagsAndMessagesView;
	LinearLayout overviewFormRoot;

	Activity a;
	IMedia media;
	IForm form = null;
	FormUtility formUtility;

	TextView alias, dateCaptured, timeCaptured, location, quickNoteHolder, messagesHolder, tagsHolder;
	EditText quickNotePrompt;
	ImageButton audioNotePrompt, audioNotePlayToggle;

	IRegion overviewRegion;

	private final static String LOG = App.LOG;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_forms_overview, null);
		overviewFormRoot = (LinearLayout) rootView.findViewById(R.id.overview_form_root);
		overviewFormRoot.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		overviewFormRoot.setFocusableInTouchMode(true);

		detailsView = li.inflate(R.layout.forms_media_details, null);
		topLevelAnnotationsView = li.inflate(R.layout.forms_media_top_level_annotations, null);
		tagsAndMessagesView = li.inflate(R.layout.forms_media_tags_and_messages, null);

		alias = (TextView) detailsView.findViewById(R.id.media_details_alias);
		alias.setOnClickListener(this);

		dateCaptured = (TextView) detailsView.findViewById(R.id.media_date_captured);
		timeCaptured = (TextView) detailsView.findViewById(R.id.media_time_captured);
		location = (TextView) detailsView.findViewById(R.id.media_details_location);

		quickNoteHolder = (TextView) topLevelAnnotationsView.findViewById(R.id.media_quick_note_holder);
		quickNotePrompt = (EditText) topLevelAnnotationsView.findViewById(R.id.media_quick_note_prompt);
		quickNotePrompt.setOnClickListener(this);

		audioNotePrompt = (ImageButton) topLevelAnnotationsView.findViewById(R.id.media_audio_note_prompt);
		audioNotePrompt.setOnClickListener(this);

		audioNotePlayToggle = (ImageButton) topLevelAnnotationsView.findViewById(R.id.media_audio_note_play_toggle);
		audioNotePlayToggle.setOnClickListener(this);

		messagesHolder = (TextView) tagsAndMessagesView.findViewById(R.id.media_details_messages_holder);
		tagsHolder = (TextView) tagsAndMessagesView.findViewById(R.id.media_details_tags_holder);

		return rootView;
	}

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		initLayout();
	}

	@Override
	public void onDetach() {
		Log.d(LOG, "SHOULD SAVE FORM STATE!");

		try {
			info.guardianproject.iocipher.File formState = new info.guardianproject.iocipher.File(media.rootFolder, "form_" + System.currentTimeMillis());
			info.guardianproject.iocipher.FileOutputStream fos = new info.guardianproject.iocipher.FileOutputStream(formState);
			if(form.save(fos) != null) {
				//IRegion region = media.addRegion();
				//region.formReference = formState.getAbsolutePath();
			}
		} catch (FileNotFoundException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}

		super.onDetach();
	}

	private void initLayout() {
		overviewFormRoot.addView(detailsView);
		overviewFormRoot.addView(topLevelAnnotationsView);
		overviewFormRoot.addView(tagsAndMessagesView);

		initData();
		initForms();
	}

	private void initData() {
		media = ((EditorActivity) a).media;
		int displayNumTags = 0;
		if(media.associatedRegions != null && media.associatedRegions.size() > 0) {
			displayNumTags = media.getRegionsWithForms().size();
		}
		tagsHolder.setText(a.getResources().getString(R.string.x_tags, displayNumTags, (displayNumTags == 1 ? "" : "s")));

		int displayNumMessages = 0;
		if(media.messages != null && media.messages.size() > 0) {
			displayNumMessages = media.messages.size();
		}
		messagesHolder.setText(a.getResources().getString(R.string.x_messages, displayNumMessages, (displayNumMessages == 1 ? "" : "s")));

		String[] dateAndTime = TimeUtility.millisecondsToDatestampAndTimestamp(media.dcimEntry.timeCaptured);
		dateCaptured.setText(dateAndTime[0]);
		timeCaptured.setText(dateAndTime[1]);

		if(media.alias != null) {
			alias.setText(media.alias);
		}

		if(media.dcimEntry.exif.location != null) {
			location.setText(a.getString(R.string.x_location, media.dcimEntry.exif.location[0], media.dcimEntry.exif.location[1]));
		} else {
			location.setText(a.getString(R.string.location_unknown));
		}

	}

	private void initForms() {
		for(IForm form : ((EditorActivity) a).availableForms) {
			if(form.namespace.equals(Forms.OverviewForm.TAG)) {
				this.form = form;
				break;
			}
		}

		if(media.getRegionsWithForms() != null && media.getRegionsWithForms().size() > 0) {
			overviewRegion = media.getRegionAtRect();
			String[] answers = form.populateAnswers(InformaCam.getInstance().ioService.getBytes(overviewRegion.formPath, Type.IOCIPHER));
			form.associate(a, answers[0], quickNotePrompt, Forms.OverviewForm.QUICK_NOTE_PROMPT);
			form.associate(a, answers[1], audioNotePrompt, Forms.OverviewForm.AUDIO_NOTE_PROMPT);
		} else {
			form.associate(a, quickNotePrompt, Forms.OverviewForm.QUICK_NOTE_PROMPT);
			form.associate(a, audioNotePrompt, Forms.OverviewForm.AUDIO_NOTE_PROMPT);
		}
	}

	public void cleanup() {

	}

	private void swapForTextarea() {
		new TextareaPopup(a, media) {
			@Override
			public void cancel() {
				super.cancel();
				quickNoteHolder.setText(this.prompt.getText().toString());
				quickNotePrompt.setText(this.prompt.getText().toString());
				form.answer(Forms.OverviewForm.QUICK_NOTE_PROMPT);
			}
		};
	}

	private void renameMedia() {
		new RenamePopup(a, media) {
			@Override
			public void cancel() {
				super.cancel();
				alias.setText(media.alias);
			}
		};
	}

	private void recordAudio() {
		new AudioNotePopup(a) {
			@Override
			public void cancel() {
				super.cancel();
				form.answer(Forms.OverviewForm.AUDIO_NOTE_PROMPT);
			}
		};
	}

	@Override
	public void onClick(View v) {
		if(v == alias) {
			renameMedia();
		} else if(v == quickNotePrompt) {
			swapForTextarea();
		} else if(v == audioNotePrompt) {
			recordAudio();
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(hasFocus) {
			swapForTextarea();
		}
	}

}
