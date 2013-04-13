package org.witness.iwitness.app.screens;

import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.media.IRegion;
import org.witness.iwitness.R;
import org.witness.iwitness.app.EditorActivity;
import org.witness.iwitness.app.screens.forms.TagFormFragment;
import org.witness.iwitness.utils.Constants.App;
import org.witness.iwitness.utils.Constants.Codes;
import org.witness.iwitness.utils.Constants.App.Editor.Mode;
import org.witness.iwitness.utils.actions.ContextMenuAction;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
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
import android.widget.ScrollView;
import android.widget.TextView;

public class FullScreenViewFragment extends Fragment implements OnClickListener, OnTouchListener  {
	protected View rootView;
	protected Activity a;

	protected InformaCam informaCam;

	protected ImageButton toggleControls;
	protected LinearLayout controlsHolder;
	protected RelativeLayout mediaHolder;
	protected Canvas regionDisplay;
	protected ScrollView scrollRoot;
	protected boolean controlsAreShowing = false;

	protected FrameLayout formHolder;
	protected Fragment tagFormFragment;
	protected RelativeLayout mediaHolderParent;

	protected int[] dims;
	protected int scrollTarget;
	
	protected IRegion currentRegion = null; 
	
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
	
	protected Paint activePaint, inactivePaint;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_editor_fullscreen_view, null);
		
		scrollRoot = (ScrollView) rootView.findViewById(R.id.scroll_root);
		
		toggleControls = (ImageButton) rootView.findViewById(R.id.toggle_controls);
		toggleControls.setOnClickListener(this);

		int controlHolder = R.id.controls_holder_portrait;
		scrollTarget = InformaCam.getInstance().getDimensions()[0];

		if(getArguments().getInt(Codes.Extras.SET_ORIENTATION) == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			controlHolder = R.id.controls_holder_landscape;
			scrollTarget = InformaCam.getInstance().getDimensions()[1];
		}
			
		controlsHolder = (LinearLayout) rootView.findViewById(controlHolder);
				
		mediaHolderParent = (RelativeLayout) rootView.findViewById(R.id.media_holder_parent);
		mediaHolder = (RelativeLayout) rootView.findViewById(R.id.media_holder);
		formHolder = (FrameLayout) rootView.findViewById(R.id.fullscreen_form_holder);

		return rootView;
	}
	
	protected void initLayout() {
		mediaHolderParent.setLayoutParams(new LinearLayout.LayoutParams(dims[0], dims[1]));
		showForms();
		registerControls();
		toggleControls();
		
		activePaint = new Paint();
		
		inactivePaint = new Paint();
	}
	
	protected void showForm() {
		scrollRoot.scrollTo(0, scrollTarget);
	}
	
	protected void registerControls() {
		List<ContextMenuAction> controls = new ArrayList<ContextMenuAction>();
		
		ContextMenuAction action = new ContextMenuAction();
		action.label = a.getString(R.string.notes);
		action.iconResource = R.drawable.ic_edit_notes;
		action.ocl = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(LOG, "clicked on notes");
				toggleControls();
				showForm();
			}
		};
		controls.add(action);
		
		action = new ContextMenuAction();
		action.label = a.getString(R.string.delete_tag);
		action.iconResource = R.drawable.ic_edit_delete;
		action.ocl = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(LOG, "clicked on delete");
				toggleControls();
				
			}
		};
		controls.add(action);
		
		for(ContextMenuAction cma : controls) {
			View control = LayoutInflater.from(a).inflate(R.layout.adapter_context_menu_editor, null);
			control.setLayoutParams(toggleControls.getLayoutParams());
			
			ImageView icon = (ImageView) control.findViewById(R.id.context_menu_item_icon);
			icon.setImageDrawable(a.getResources().getDrawable(cma.iconResource));
			
			TextView label = (TextView) control.findViewById(R.id.context_menu_item_label);
			label.setText(cma.label);
			
			control.setOnClickListener(cma.ocl);
			controlsHolder.addView(control);
		}
		
	}

	protected void showForms() {
		tagFormFragment = Fragment.instantiate(a, TagFormFragment.class.getName());

		FragmentTransaction ft = ((EditorActivity) a).fm.beginTransaction();
		ft.add(R.id.fullscreen_form_holder, tagFormFragment);
		ft.addToBackStack(null);
		ft.commit();
	}

	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
		
		informaCam = InformaCam.getInstance();
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
		initLayout();
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
		Log.d(LOG, "on touch called at " + event.getX() + "," + event.getY());
		
		return false;
	}


}
