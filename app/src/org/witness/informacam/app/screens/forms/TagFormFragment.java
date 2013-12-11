package org.witness.informacam.app.screens.forms;

import info.guardianproject.odkparser.FormWrapper.ODKFormListener;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.app.EditorActivity;
import org.witness.informacam.app.R;
import org.witness.informacam.app.utils.Constants.App.Editor;
import org.witness.informacam.app.utils.Constants.App.Editor.Forms;
import org.witness.informacam.app.utils.Constants.EditorActivityListener;
import org.witness.informacam.app.utils.adapters.TagFormSpinnerAdapter;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.Logger;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class TagFormFragment extends Fragment implements ODKFormListener, OnItemSelectedListener
{
	View rootView;
	LinearLayout tagFormRoot;
	Spinner tagFormChooser;
	boolean tagFormChooserInited = false;

	IRegion region;
	Activity a;
	List<IForm> form = null;
	IForm formNote = null;
	FormUtility formUtility;
	
	int[] inputTemplate = new int[] { R.layout.forms_odk_input_template, R.id.odk_question, R.id.odk_hint, R.id.odk_answer };
	int[] selectOneTemplate = new int[] { R.layout.forms_odk_select_one_template, R.id.odk_question, R.id.odk_hint, R.id.odk_selection_holder };
	int[] selectMultipleTemplate = new int[] { R.layout.forms_odk_select_multiple_template, R.id.odk_question, R.id.odk_hint,
			R.id.odk_selection_holder };

	Handler h;

	private final static String LOG = Editor.LOG;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_forms_fullscreen, null);
		tagFormRoot = (LinearLayout) rootView.findViewById(R.id.details_form_list);
		
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

		h = new Handler();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (this.a != null && this.a instanceof EditorActivity)
		{
			((EditorActivity) a).onFragmentResumed(this);
		}
	}
	
	@Override
	public void onDetach()
	{
		super.onDetach();
	}
	
	public void renderTag(final IForm mForm) {
		h.post(new Runnable() {			
			@Override
			public void run()
			{
				// UI for the note
				for (View v : mForm.buildUI(inputTemplate, selectOneTemplate, selectMultipleTemplate, null))
				{
					tagFormRoot.addView(v);
				}

				LayoutInflater inflater = LayoutInflater.from(a);
				View divider = inflater.inflate(R.layout.extras_divider_blue, tagFormRoot, false);
				tagFormRoot.addView(divider);
			}
		});
	}
	
	private IForm activateNonMasterForm(IForm form) {
		IForm mForm = IForm.Activate(form, a);
		mForm.answerPath = new info.guardianproject.iocipher.File(((EditorActivityListener) a).media().rootFolder, "form_"
			+ System.currentTimeMillis()).getAbsolutePath();
	
		this.form.add(mForm);
		
		renderTag(this.form.get(this.form.size() - 1));
		
		return this.form.get(this.form.size() -1);
	}

	public boolean initTag(IRegion region)
	{
		this.region = region;
		
		// Reset
		tagFormRoot.removeAllViews();
		this.form = new ArrayList<IForm>();
		this.formNote = null;
		
		// add the forms we already have
		if (!region.associatedForms.isEmpty())
		{
			for (IForm form : region.associatedForms)
			{
				if (!form.isMaster)
				{
					this.form.add(IForm.Activate(form, a, InformaCam.getInstance().ioService.getBytes(form.answerPath, Type.IOCIPHER)));
				}
				else if (form.namespace.equals(Forms.FreeText.TAG))
				{
					this.formNote = IForm.Activate(form, a, InformaCam.getInstance().ioService.getBytes(form.answerPath, Type.IOCIPHER));
				}
			}
		}
		
		// init the formNote (default form)
		if(this.formNote == null) {
			for(IForm form : ((EditorActivity) a).availableForms) {
				if(form.namespace.equals(Forms.FreeText.TAG)) {
					this.formNote = IForm.Activate(form, a);
					this.formNote.answerPath = new info.guardianproject.iocipher.File(((EditorActivityListener) a).media().rootFolder, "form_t"
							+ System.currentTimeMillis()).getAbsolutePath();
					
					this.region.addForm(this.formNote);
				}
			}
		}
		
		renderTag(this.formNote);
		
		List<IForm> nonMasterForms = new ArrayList<IForm>();
		for(IForm mForm : ((EditorActivity) a).availableForms) {
			if(!mForm.isMaster) {
				nonMasterForms.add(mForm);
			}
		}
		
		// add the chooser for adding new forms if there is more than one installed non-master form
		if(nonMasterForms.size() > 1) {
			tagFormChooser = new Spinner(a);
			tagFormChooser.setAdapter(new TagFormSpinnerAdapter(nonMasterForms));
			tagFormChooser.setOnItemSelectedListener(this);
			
			tagFormRoot.addView(tagFormChooser);
		}
		
		if(this.form.size() > 0) {
			// init the non-master forms we already have
			for(IForm mForm : this.form){
				renderTag(mForm);
			}
		} else {
			if(nonMasterForms.size() == 1) {
				this.region.addForm(activateNonMasterForm(nonMasterForms.get(0)));
			}
		}

		return true;
	}

	public void saveTagFormData(final IRegion region)
	{
		Log.d(LOG, "saving form data for current region");

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					formNote.answerAll();
					formNote.save(new info.guardianproject.iocipher.FileOutputStream(formNote.answerPath));
					
					for(IForm mForm : TagFormFragment.this.form) {
						Logger.d(LOG, "SAVING: \n" + mForm.asJson());
						
						mForm.answerAll();
						mForm.save(new info.guardianproject.iocipher.FileOutputStream(mForm.answerPath));
					}
					
					((EditorActivityListener) a).media().save();
					Logger.d(LOG, ((EditorActivityListener) a).media().asJson().toString());
				}
				catch (FileNotFoundException e)
				{
					Logger.e(LOG, e);
				}
			}
		}).start();

	}

	@Override
	public boolean saveForm()
	{
		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
		if(!tagFormChooserInited) {
			tagFormChooserInited = true;
		} else {
			this.region.addForm(activateNonMasterForm(((IForm) tagFormChooser.getAdapter().getItem(position))));
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {}
}
