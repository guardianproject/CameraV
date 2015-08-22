package org.witness.informacam.ui.screens;

import org.witness.informacam.R;
import org.witness.informacam.crypto.KeyUtility;
import org.witness.informacam.ui.SurfaceGrabberActivity;
import org.witness.informacam.ui.WizardActivity;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.InformaCamEventListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class WizardStepTwo extends Fragment implements OnClickListener {
	View rootView;
	Activity a;
	
	ImageButton toImageCapture;
	
	Handler handler;
	
	@SuppressWarnings("unused")
	private final static String LOG = App.LOG;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);
		
		rootView = li.inflate(R.layout.fragment_wizard_step_two, null);
		toImageCapture = (ImageButton) rootView.findViewById(R.id.to_image_capture);
		toImageCapture.setOnClickListener(this);
		
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
		handler = new Handler();
	}

	@Override
	public void onClick(View v) {
		if(v == toImageCapture) {
			Intent surfaceGrabberIntent = new Intent(a, SurfaceGrabberActivity.class);
			startActivityForResult(surfaceGrabberIntent, Codes.Routes.IMAGE_CAPTURE);
		}
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK) {
			switch(requestCode) {
			case Codes.Routes.IMAGE_CAPTURE:
				// init the key...
				toImageCapture.setClickable(false);
				((WizardActivity) a).autoAdvance();
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						if(KeyUtility.initDevice()) {
							Bundle data = new Bundle();
							data.putInt(Codes.Extras.MESSAGE_CODE, Codes.Messages.UI.REPLACE);
							
							Message message = new Message();
							message.setData(data);
							
							((InformaCamEventListener) a).onUpdate(message);
						}
					}
				}).start();
				break;
			}
		}
	}
}
