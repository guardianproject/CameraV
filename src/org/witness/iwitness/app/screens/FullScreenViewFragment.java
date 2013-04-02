package org.witness.iwitness.app.screens;

import org.witness.informacam.InformaCam;
import org.witness.iwitness.R;
import org.witness.iwitness.utils.Constants.App;
import org.witness.iwitness.utils.Constants.Codes;
import org.witness.iwitness.utils.Constants.App.Editor.Mode;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class FullScreenViewFragment extends Fragment implements OnClickListener, OnTouchListener  {
	protected View rootView;
	protected Activity a;

	protected InformaCam informaCam;

	protected ImageButton toggleControls;
	protected LinearLayout controlsHolder;
	protected ImageView mediaHolder;
	protected boolean controlsAreShowing = false;

	protected FrameLayout formHolder;
	protected Fragment tagFormFragment;
	protected RelativeLayout mediaHolderParent;

	protected int[] dims;
	// We can be in one of these 3 states
	protected int mode = Mode.NONE;

	// For Zooming
	protected float startFingerSpacing = 0f;
	protected float endFingerSpacing = 0f;
	protected PointF startFingerSpacingMidPoint = new PointF();

	// For Dragging
	protected PointF startPoint = new PointF();

	// Don't allow it to move until the finger moves more than this amount
	// Later in the code, the minMoveDistance in real pixels is calculated
	// to account for different touch screen resolutions
	protected float minMoveDistanceDP = 5f;
	protected float minMoveDistance; // = ViewConfiguration.get(this).getScaledTouchSlop();

	protected final static String LOG = App.Editor.LOG;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_editor_fullscreen_view, null);
		toggleControls = (ImageButton) rootView.findViewById(R.id.toggle_controls);

		int controlHolder = R.id.controls_holder_landscape;

		if(getArguments().getInt(Codes.Extras.SET_ORIENTATION) == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			controlHolder = R.id.controls_holder_portrait;
		}

		controlsHolder = (LinearLayout) rootView.findViewById(controlHolder);

		mediaHolderParent = (RelativeLayout) rootView.findViewById(R.id.media_holder_parent);
		mediaHolder = (ImageView) rootView.findViewById(R.id.media_holder);
		formHolder = (FrameLayout) rootView.findViewById(R.id.fullscreen_form_holder);

		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if(getArguments().getInt(Codes.Extras.SET_ORIENTATION) == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			controlsHolder = (LinearLayout) rootView.findViewById(R.id.controls_holder_landscape);
		} else {
			controlsHolder = (LinearLayout) rootView.findViewById(R.id.controls_holder_portrait);
		}
		
		dims = informaCam.getDimensions();
	}
	
	protected void toggleControls() {
		int d = R.drawable.ic_edit_show_tags;

		if(controlsAreShowing) {
			controlsHolder.setVisibility(View.GONE);
			d = R.drawable.ic_edit_hide_tags;
			controlsAreShowing = false;
		} else {
			controlsHolder.setVisibility(View.VISIBLE);
			controlsAreShowing = true;
		}

		toggleControls.setImageDrawable(a.getResources().getDrawable(d));
	}

	@Override
	public void onClick(View v) {
		if(v == toggleControls) {
			toggleControls();
		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
