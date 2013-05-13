package org.witness.iwitness.app.screens.forms;

import java.io.FileNotFoundException;

import info.guardianproject.odkparser.FormWrapper.ODKFormListener;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.storage.FormUtility;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.iwitness.R;
import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.screens.FullScreenViewFragment;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class TagFormFragment extends Fragment implements ODKFormListener {
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
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		Log.d(LOG, "SHOULD SAVE FORM STATE!");
	}
	
	private void initData() {
		media = ((EditorActivity) a).media;
	}
	
	public boolean initTag(final IRegion region) {
		tagFormRoot.removeAllViews();
		Log.d(LOG, "form at: " + String.valueOf(region.formPath));
		
		for(IForm form : ((EditorActivity) a).availableForms) {
			if(form.namespace.equals(Forms.TagForm.TAG)) {
				byte[] answerBytes = null;
				region.formNamespace = form.namespace;
				if(region.formPath != null) {
					answerBytes = InformaCam.getInstance().ioService.getBytes(region.formPath, Type.IOCIPHER);
				}
				
				this.form = new IForm(form, a, answerBytes);
				h.post(new Runnable() {
					LayoutInflater li = LayoutInflater.from(a);
					
					@Override
					public void run() {
						int[] inputTemplate = new int[] {R.layout.forms_odk_input_template, R.id.odk_question, R.id.odk_hint, R.id.odk_answer};
						int[] selectOneTemplate = new int[] {R.layout.forms_odk_select_one_template, R.id.odk_question, R.id.odk_hint, R.id.odk_selection_holder};
						int[] selectMultipleTemplate = new int[] {R.layout.forms_odk_select_multiple_template, R.id.odk_question, R.id.odk_hint, R.id.odk_selection_holder};
						for(View v : TagFormFragment.this.form.buildUI(inputTemplate, selectOneTemplate, selectMultipleTemplate, null)) {
							tagFormRoot.addView(v);							
						}
					}
				});
				
				break;
			}
		}
		
		return true;
	}
	
	public void saveTagFormData(final IRegion region) {
		Log.d(LOG, "saving form data for current region");
		if(region.formNamespace == null) {
			region.formNamespace = form.namespace;
		}
		
		if(region.formPath == null) {
			region.formPath = new info.guardianproject.iocipher.File(media.rootFolder, "form_" + System.currentTimeMillis()).getAbsolutePath();
		}
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				form.answerAll();
				
				try {
					info.guardianproject.iocipher.FileOutputStream fos = new info.guardianproject.iocipher.FileOutputStream(region.formPath);
					
					if(form.save(fos) != null) {
						InformaCam.getInstance().mediaManifest.save();
					}
				} catch (FileNotFoundException e) {
					Log.e(LOG, e.toString());
					e.printStackTrace();
				}
			}
		}).start();
		
	}

	@Override
	public boolean saveForm() {
		return true;
		
	}
}
