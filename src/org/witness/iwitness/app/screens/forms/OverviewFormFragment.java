package org.witness.iwitness.app.screens.forms;

import org.witness.informacam.models.IMedia;
import org.witness.informacam.utils.TimeUtility;
import org.witness.informacam.utils.Constants.App;
import org.witness.iwitness.R;
import org.witness.iwitness.app.EditorActivity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class OverviewFormFragment extends Fragment implements OnClickListener, OnEditorActionListener {
	View rootView, detailsView, topLevelAnnotationsView, tagsAndMessagesView;
	LinearLayout overviewFormRoot;
	
	Activity a;
	IMedia media;
	
	TextView alias, dateCaptured, timeCaptured, location, quickNoteHolder, messagesHolder, tagsHolder;
	EditText quickNotePrompt;
	ImageButton audioNotePrompt, audioNotePlayToggle;
	
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
		
		detailsView = li.inflate(R.layout.forms_media_details, null);
		topLevelAnnotationsView = li.inflate(R.layout.forms_media_top_level_annotations, null);
		tagsAndMessagesView = li.inflate(R.layout.forms_media_tags_and_messages, null);
		
		alias = (TextView) detailsView.findViewById(R.id.media_details_alias);
		dateCaptured = (TextView) detailsView.findViewById(R.id.media_date_captured);
		timeCaptured = (TextView) detailsView.findViewById(R.id.media_time_captured);
		location = (TextView) detailsView.findViewById(R.id.media_details_location);
		
		quickNoteHolder = (TextView) topLevelAnnotationsView.findViewById(R.id.media_quick_note_holder);
		quickNotePrompt = (EditText) topLevelAnnotationsView.findViewById(R.id.media_quick_note_prompt);
		quickNotePrompt.setImeOptions(EditorInfo.IME_ACTION_DONE);
		quickNotePrompt.setOnEditorActionListener(this);
		
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
	
	private void initLayout() {
		overviewFormRoot.addView(detailsView);
		overviewFormRoot.addView(topLevelAnnotationsView);
		overviewFormRoot.addView(tagsAndMessagesView);
		
		initData();
	}
	
	private void initData() {
		media = ((EditorActivity) a).media;
		int displayNumTags = 0;
		if(media.annotations != null && media.annotations.size() > 0) {
			displayNumTags = media.annotations.size();
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
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(event != null) {
			if(!event.isShiftPressed()) {
				quickNoteHolder.setText(quickNotePrompt.getText().toString());
			}
			return true;
		}
		return false;
	}
}
