package org.witness.informacam.ui.screens;

import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.R;
import org.witness.informacam.ui.WizardActivity;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.InformaCamEventListener;
import org.witness.informacam.utils.Constants.WizardListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class WizardStepThree extends Fragment implements OnClickListener, InformaCamEventListener {
	
	View rootView;
	FrameLayout subFragmentRoot;
	LinearLayout subFragmentProgress, keyGenProgressHolder, keyGenSuccessHolder;
	ImageButton subFragmentNext;
	Button saveAndContinue;
	ProgressBar keyGenProgress;
	
	Activity a;
	
	Handler handler = new Handler();
	
	List<Fragment> subFragments;
	int idx = 0;
	
	private final static String LOG = App.LOG;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		((InformaCam)getActivity().getApplication()).setEventListener(this);

	}
	
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);
		this.setRetainInstance(true);
		
		rootView = li.inflate(R.layout.fragment_wizard_step_three, null);
		keyGenProgress = (ProgressBar) rootView.findViewById(R.id.wizard_keygen_progress);
		keyGenProgressHolder = (LinearLayout) rootView.findViewById(R.id.key_gen_progress_holder);
		keyGenSuccessHolder = (LinearLayout) rootView.findViewById(R.id.key_gen_success_holder);
		
		saveAndContinue = (Button) rootView.findViewById(R.id.wizard_save_and_continue);
		saveAndContinue.setOnClickListener(this);
		
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
		Log.d(LOG, "fyi activity created called");
		initLayout();
	}
	
	private void initLayout() {
		subFragments = ((WizardActivity) a).subFragments;
		if(subFragments != null) {
			initSubFragmentProgress();
		} else {
			FrameLayout noSubFragmentsHolder = (FrameLayout) rootView.findViewById(R.id.wizard_no_sub_fragments_root);
			noSubFragmentsHolder.setVisibility(View.VISIBLE);
			
			FragmentTransaction ft = ((WizardListener) a).returnFragmentManager().beginTransaction();
			ft.replace(R.id.wizard_no_sub_fragments_root, new WizardSubFragmentFinish());
			ft.addToBackStack(null);
			ft.commit();
		}
	}
	
	@SuppressWarnings("unused")
	private void initSubFragmentProgress() {
		RelativeLayout subFragmentHolder = (RelativeLayout) rootView.findViewById(R.id.wizard_sub_fragments_holder);
		subFragmentHolder.setVisibility(View.VISIBLE);
		
		subFragmentProgress = (LinearLayout) rootView.findViewById(R.id.wizard_sub_fragments_progress);
		
		for(int i=0; i<subFragmentProgress.getChildCount(); i++) {
			try {
				View v = subFragmentProgress.getChildAt(i);
				((LinearLayout) v.getParent()).removeView(v);
			} catch(NullPointerException e) {}
		}
		
		for(Fragment f : subFragments) {
			ImageView p = new ImageView(a);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lp.setMargins(5, 0, 5, 0);
			p.setLayoutParams(lp);
			subFragmentProgress.addView(p);
		}
		
		subFragmentNext = (ImageButton) rootView.findViewById(R.id.wizard_sub_fragment_next);
		subFragmentNext.setOnClickListener(this);
		
		advanceWizardSubFragment(subFragments.get(0));
	}
	
	private void advanceWizardSubFragment(Fragment fragment) {
		idx=0;
		for(Fragment f : subFragments) {
			if(fragment.getClass().getSimpleName().equals(f.getClass().getSimpleName())) {
				((WizardListener) subFragments.get(0)).onSubFragmentInitialized();
				break;
			}
			idx++;
		}
		
		Log.d(LOG, "subfragments size: " + subFragments.size());
		
		if(idx == 0) {
			subFragmentNext.setClickable(true);
		} else if(idx == subFragments.size() - 1) {
			subFragmentNext.setClickable(false);
		}
		
		try {
			((WizardListener) subFragments.get(idx - 1)).onSubFragmentCompleted();
		} catch(ArrayIndexOutOfBoundsException e) {}
		
		FragmentTransaction ft = ((WizardListener) a).returnFragmentManager().beginTransaction();
		ft.replace(R.id.wizard_sub_fragments_root, fragment);
		ft.addToBackStack(null);
		ft.commit();
		
		for(int i=0; i<subFragmentProgress.getChildCount(); i++) {
			int dot = R.drawable.progress_inactive;
			if(i == idx) {
				dot = R.drawable.progress_active;
			}
			
			((ImageView) subFragmentProgress.getChildAt(i)).setImageDrawable(getResources().getDrawable(dot));
		}
		
		
		
	}

	@Override
	public void onClick(View v) {
		if(v == subFragmentNext) {
			idx++;
			advanceWizardSubFragment(subFragments.get(idx));
		} else if(v == saveAndContinue) {
			((WizardListener) a).wizardCompleted();
		}
		
	}

	@Override
	public void onUpdate(Message message) {
		Log.d(LOG, message.getData().toString());
		
		int code = message.getData().getInt(Codes.Extras.MESSAGE_CODE);
		switch(code) {
		case Codes.Messages.UI.UPDATE:
			Log.d(LOG, "updating progress bar");
			keyGenProgress.setProgress((Integer) message.getData().get(Codes.Keys.UI.PROGRESS));
			break;
		case Codes.Messages.UI.REPLACE:
			handler.post(new Runnable() {
				@Override
				public void run() {
					keyGenProgressHolder.setVisibility(View.GONE);
					keyGenSuccessHolder.setVisibility(View.VISIBLE);
				}
			});
			
			break;
		}
		
	}
}
