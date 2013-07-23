package org.witness.iwitness.app.views;

import info.guardianproject.odkparser.widgets.ODKSeekBar;

import org.witness.informacam.models.forms.IForm;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AudioNoteInfoView extends LinearLayout
{
	private IForm mForm;
	private TextView tvLabel;
	private ODKSeekBar mSeekBar;

	public AudioNoteInfoView(Context context)
	{
		super(context);
	}

	public AudioNoteInfoView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		tvLabel = (TextView) findViewById(R.id.tvLabel);
		init();
	}

	public IForm getForm()
	{
		return mForm;
	}

	public void setForm(IForm form)
	{
		mForm = form;
		init();
	}

	private void init()
	{
		if (mForm != null && tvLabel != null)
		{
			mSeekBar = new ODKSeekBar(getContext());
			mForm.associate(mSeekBar, Forms.FreeAudio.PROMPT);
			tvLabel.setText("" + mSeekBar.getMax());
		}
	}
}
