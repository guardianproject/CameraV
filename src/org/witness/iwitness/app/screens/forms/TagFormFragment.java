package org.witness.iwitness.app.screens.forms;

import org.witness.informacam.models.IForm;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.storage.FormUtility;
import org.witness.iwitness.R;
import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.utils.Constants.App.Editor;
import org.witness.iwitness.utils.Constants.App.Editor.Forms;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class TagFormFragment extends Fragment {
	View rootView;
	LinearLayout tagFormRoot;
	
	Activity a;
	IMedia media;
	IForm form = null;
	FormUtility formUtility;
	
	Handler h;
	
	private final static String LOG = Editor.LOG;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);
		
		rootView = li.inflate(R.layout.fragment_forms_fullscreen, null);
		tagFormRoot = (LinearLayout) rootView.findViewById(R.id.details_form_root);
		
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
		
		h = new Handler();
		initData();
		initForms();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		Log.d(LOG, "SHOULD SAVE FORM STATE!");
	}
	
	private void initData() {
		media = ((EditorActivity) a).media;
	}
	
	private void initLayout() {
		h.post(new Runnable() {
			LayoutInflater li = LayoutInflater.from(a);
			
			@Override
			public void run() {
				int[] inputTemplate = new int[] {R.layout.forms_odk_input_template, R.id.odk_question, R.id.odk_hint, R.id.odk_answer};
				int[] selectOneTemplate = new int[] {R.layout.forms_odk_select_one_template, R.id.odk_question, R.id.odk_hint, R.id.odk_selection_holder};
				int[] selectMultipleTemplate = new int[] {R.layout.forms_odk_select_multiple_template, R.id.odk_question, R.id.odk_hint, R.id.odk_selection_holder};
				for(View v : form.buildUI(a, null, inputTemplate, selectOneTemplate, selectMultipleTemplate, null)) {
					tagFormRoot.addView(v);
					
				}
			}
		});
	}
	
	private void initForms() {
		for(IForm form : ((EditorActivity) a).availableForms) {
			if(form.namespace.equals(Forms.TagForm.TAG)) {
				this.form = form;
				break;
			}
		}
		
		if(form != null) {
			initLayout();
		}
	}
}
