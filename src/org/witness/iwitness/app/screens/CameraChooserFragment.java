package org.witness.iwitness.app.screens;

import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App;
import org.witness.iwitness.utils.Constants.MainFragmentListener;
import org.witness.iwitness.utils.app.VerticalButton;

import org.witness.informacam.utils.Constants.App.Camera;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class CameraChooserFragment extends Fragment implements OnClickListener {
	View rootView;
	Activity a;
	VerticalButton launch_camera, launch_camcorder;
		
	private final static String LOG = App.Home.LOG;
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);
		
		rootView = li.inflate(R.layout.fragment_home_camera_chooser, null);
		launch_camera = (VerticalButton) rootView.findViewById(R.id.launch_camera);
		launch_camera.setOnClickListener(this);
		
		launch_camcorder = (VerticalButton) rootView.findViewById(R.id.launch_camcorder);
		launch_camcorder.setOnClickListener(this);
		
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
	}
	
	@Override
	public void onClick(View v) {
		if(v == launch_camera) {
			Log.d(LOG, "hello camera");
			((MainFragmentListener) a).launchCamera(Camera.Type.CAMERA);
		} else if(v == launch_camcorder) {
			Log.d(LOG, "hello camcorder");
			((MainFragmentListener) a).launchCamera(Camera.Type.CAMCORDER);
		}		
	}
}
