package org.witness.iwitness.app.screens.forms;

import info.guardianproject.odkparser.FormWrapper.ODKFormListener;
import info.guardianproject.odkparser.widgets.ODKSeekBar;

import java.io.FileNotFoundException;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.TimeUtility;
import org.witness.iwitness.R;
import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.screens.popups.AudioNotePopup;
import org.witness.iwitness.app.screens.popups.TextareaPopup;
import org.witness.iwitness.app.views.AudioNoteInfoView;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;
import org.witness.iwitness.utils.Constants.EditorActivityListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverviewFormFragment extends Fragment implements ODKFormListener, OnClickListener
{
	View rootView;
	Activity a;
	TextView timeCaptured, location;
	TextView notes;
	EditText notesAnswerHolder; // Dummy EditText to hold notes
	IForm form = null;
	private IForm textForm;
	LinearLayout llAudioFiles;

	private final static String LOG = App.LOG;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_forms_overview, container, false);

		timeCaptured = (TextView) rootView.findViewById(R.id.media_time_captured);
		location = (TextView) rootView.findViewById(R.id.media_details_location);

		notes = (TextView) rootView.findViewById(R.id.media_notes);
		notes.setText("");
		notesAnswerHolder = new EditText(container.getContext());
		notesAnswerHolder.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void afterTextChanged(Editable s)
			{
				notes.setText(s);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
			}

		});

		llAudioFiles = (LinearLayout) rootView.findViewById(R.id.llAudioFiles);

		rootView.findViewById(R.id.btnAddAudio).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				recordNewAudio();
			};
		});

		return rootView;
	}

	@Override
	public void onAttach(Activity a)
	{
		super.onAttach(a);
		this.a = a;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		initLayout();
	}

	@Override
	public void onDetach()
	{
		Log.d(LOG, "SHOULD SAVE FORM STATE!");
		super.onDetach();
	}

	private void initLayout()
	{
		initData();
		initForms();
	}

	private void initData()
	{
		IMedia media = ((EditorActivityListener) a).media();

		// int displayNumMessages = 0;
		// if (((EditorActivityListener) a).media().messages != null &&
		// ((EditorActivityListener) a).media().messages.size() > 0)
		// {
		// displayNumMessages = ((EditorActivityListener)
		// a).media().messages.size();
		// }
		// messagesHolder.setText(a.getResources().getString(R.string.x_messages,
		// displayNumMessages, (displayNumMessages == 1 ? "" : "s")));

		String[] dateAndTime = TimeUtility.millisecondsToDatestampAndTimestamp(((EditorActivityListener) a).media().dcimEntry.timeCaptured);
		timeCaptured.setText(dateAndTime[0] + " " + dateAndTime[1]);

		// if (((EditorActivityListener) a).media().alias != null)
		// {
		// alias.setText(((EditorActivityListener) a).media().alias);
		// }

		if (media.dcimEntry.exif.location != null && media.dcimEntry.exif.location != new float[] { 0.0f, 0.0f })
		{
			location.setText(a.getString(R.string.x_location, media.dcimEntry.exif.location[0], media.dcimEntry.exif.location[1]));
		}
		else
		{
			location.setText(a.getString(R.string.location_unknown));
		}

	}

	private void initForms()
	{
		Logger.d(LOG, ((EditorActivityListener) a).media().asJson().toString());

		IRegion overviewRegion = ((EditorActivityListener) a).media().getTopLevelRegion();
		if (overviewRegion == null)
		{
			overviewRegion = ((EditorActivityListener) a).media().addRegion(a, null);
		}

		for (IForm form : ((EditorActivityListener) a).media().getForms(a))
		{
			if (form.namespace.equals(Forms.FreeText.TAG))
			{
				textForm = form;
			}
		}
		if (textForm == null)
		{
			// No text form found, add one!
			for (IForm form : ((EditorActivity) a).availableForms)
			{
				if (form.namespace.equals(Forms.FreeText.TAG))
				{
					textForm = new IForm(form, a);
					textForm.answerPath = new info.guardianproject.iocipher.File(((EditorActivityListener) a).media().rootFolder, "form_t"
							+ System.currentTimeMillis()).getAbsolutePath();

					overviewRegion.addForm(textForm);
				}
			}
		}

		textForm.associate(notesAnswerHolder, Forms.FreeText.PROMPT);

		updateAudioFiles();
	}

	private void updateAudioFiles()
	{
		llAudioFiles.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(this.getActivity());

		IRegion overviewRegion = ((EditorActivityListener) a).media().getTopLevelRegion();
		if (overviewRegion != null)
		{
			for (IForm form : ((EditorActivityListener) a).media().getForms(a))
			{
				if (form.namespace.equals(Forms.FreeAudio.TAG))
				{
					AudioNoteInfoView view = (AudioNoteInfoView) inflater.inflate(R.layout.audio_note_info_view, llAudioFiles, false);
					view.setOnClickListener(this);
					view.setForm(form);
					llAudioFiles.addView(view);
				}
			}
		}

		if (llAudioFiles.getChildCount() > 0)
			llAudioFiles.setVisibility(View.VISIBLE);
		else
			llAudioFiles.setVisibility(View.GONE);
	}

	@Override
	public boolean saveForm()
	{
		Log.d(LOG, "OK I AM SAVING FORM");

		try
		{
			textForm.save(new info.guardianproject.iocipher.FileOutputStream(textForm.answerPath));
		}
		catch (FileNotFoundException e)
		{
			Logger.e(LOG, e);
		}

		// try {
		// audioForm.save(new
		// info.guardianproject.iocipher.FileOutputStream(audioForm.answerPath));
		// } catch (FileNotFoundException e) {
		// Logger.e(LOG, e);
		// }

		return InformaCam.getInstance().mediaManifest.save();
	}

	// private IForm getTopLevelTextNotes(boolean createIfNotPresent)
	// {
	// IRegion region = media.getTopLevelRegion();
	// if (region == null && createIfNotPresent)
	// {
	// region = media.addRegion(this, null);
	// }
	//
	// if (region != null)
	// {
	// for (IForm form : media.getForms(this))
	// {
	// if (form.namespace.equals(Forms.FreeText.TAG))
	// {
	// return form;
	// }
	// }
	// }
	// return null;
	// }

	public void editNotes()
	{
		new TextareaPopup(a, ((EditorActivityListener) a).media(), true)
		{
			@Override
			public void Show()
			{
				prompt.setText(notesAnswerHolder.getText());
				prompt.setSelection(prompt.getText().length());
				// if (textForm != null &&
				// textForm.getQuestionDefByTitleId(Forms.FreeText.PROMPT).initialValue
				// != null)
				// {
				// prompt.setText(textForm.getQuestionDefByTitleId(Forms.FreeText.PROMPT).initialValue);
				// }
				super.Show();
			}

			@Override
			protected void onSave()
			{
				if (textForm != null)
				{
					notesAnswerHolder.setText(prompt.getText());
					textForm.answer(Forms.FreeText.PROMPT);
				}
				super.onSave();
			}
		};

	}

	private void recordNewAudio()
	{
		IForm audioForm = null;
		ODKSeekBar audioFormAnswerHolder = new ODKSeekBar(a);

		IRegion overviewRegion = ((EditorActivityListener) a).media().getTopLevelRegion();
		if (overviewRegion == null)
		{
			overviewRegion = ((EditorActivityListener) a).media().addRegion(a, null);
		}
		// add an audio form!
		for (IForm form : ((EditorActivity) a).availableForms)
		{
			if (form.namespace.equals(Forms.FreeAudio.TAG))
			{
				audioForm = new IForm(form, a);
				audioForm.answerPath = new info.guardianproject.iocipher.File(((EditorActivityListener) a).media().rootFolder, "form_a"
						+ System.currentTimeMillis()).getAbsolutePath();

				overviewRegion.addForm(audioForm);
			}
		}

		// audioForm.associate(audioFormAnswerHolder, Forms.FreeAudio.PROMPT);
		new AudioNoteFormPopup(a, audioForm);
	}

	private class AudioNoteFormPopup extends AudioNotePopup
	{
		private final IForm mForm;

		public AudioNoteFormPopup(Activity a, IForm f)
		{
			super(a, f);
			mForm = f;
		}

		@Override
		public void cancel()
		{
			mForm.answer(Forms.FreeAudio.PROMPT);
			progress.shutDown();
			super.cancel();
			updateAudioFiles();
		}
	}

	@Override
	public void onClick(View v)
	{
		if (v instanceof AudioNoteInfoView)
		{
			AudioNoteInfoView view = (AudioNoteInfoView) v;
			new AudioNotePopup(a, view.getForm());
		}
	}
}
