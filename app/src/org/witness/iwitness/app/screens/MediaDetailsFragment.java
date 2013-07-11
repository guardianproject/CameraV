package org.witness.iwitness.app.screens;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.TimeUtility;
import org.witness.iwitness.R;
import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.screens.popups.AudioNotePopup;
import org.witness.iwitness.app.screens.popups.TextareaPopup;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;
import org.witness.iwitness.utils.Constants.EditorActivityListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class MediaDetailsFragment extends Fragment
{
	View rootView;
	Activity a;
	TextView timeCaptured, location;
	EditText notes;
	IForm form = null;

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

		rootView = li.inflate(R.layout.fragment_media_details, container, false);

		timeCaptured = (TextView) rootView.findViewById(R.id.media_time_captured);
		location = (TextView) rootView.findViewById(R.id.media_details_location);

		notes = (EditText) rootView.findViewById(R.id.media_notes);

		rootView.findViewById(R.id.btnAddAudio).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new AudioNotePopup(a, form)
				{
					@Override
					public void cancel()
					{
						form.answer(Forms.OverviewForm.AUDIO_NOTE_PROMPT);
						progress.shutDown();
						super.cancel();
					}
				};
			}
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

		notes.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new TextareaPopup(a, ((EditorActivityListener) a).media())
				{
					@Override
					public void cancel()
					{
						super.cancel();
						notes.setText(this.prompt.getText().toString());
						form.answer(Forms.OverviewForm.QUICK_NOTE_PROMPT);
					}
				};
			}
		});
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
		if (((EditorActivity) a).availableForms != null)
		{
			for (IForm form : ((EditorActivity) a).availableForms)
			{
				if (form.namespace.equals(Forms.OverviewForm.TAG))
				{
					byte[] answerBytes = null;

					IRegion overviewRegion = ((EditorActivityListener) a).media().getRegionAtRect();
					if (overviewRegion != null)
					{
						answerBytes = InformaCam.getInstance().ioService.getBytes(overviewRegion.formPath, Type.IOCIPHER);
						Log.d(LOG, overviewRegion.asJson().toString());
					}
					else
					{
						overviewRegion = ((EditorActivityListener) a).media().addRegion(a, null);
						overviewRegion.formNamespace = form.namespace;
						overviewRegion.formPath = new info.guardianproject.iocipher.File(((EditorActivityListener) a).media().rootFolder, "form_"
								+ System.currentTimeMillis()).getAbsolutePath();
					}

					this.form = new IForm(form, a, answerBytes);
					this.form.associate(notes, Forms.OverviewForm.QUICK_NOTE_PROMPT);
					break;
				}
			}
		}

		// int displayNumTags = 0;
		// if (form != null && ((EditorActivityListener)
		// a).media().associatedRegions != null && ((EditorActivityListener)
		// a).media().associatedRegions.size() > 0)
		// {
		// displayNumTags = ((EditorActivityListener)
		// a).media().getRegionsWithForms(Arrays.asList(new String[] {
		// form.namespace })).size();
		// }
		// tagsHolder.setText(a.getResources().getString(R.string.x_tags,
		// displayNumTags, (displayNumTags == 1 ? "" : "s")));
	}

}
