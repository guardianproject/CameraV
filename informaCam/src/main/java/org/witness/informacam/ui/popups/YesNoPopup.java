package org.witness.informacam.ui.popups;

import java.util.ArrayList;

import org.witness.informacam.R;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class YesNoPopup extends Popup implements OnClickListener {
	protected Button ok, cancel;
	protected LinearLayout infoHolder;
	
	public YesNoPopup(Activity a, ArrayList<View> infoViews) {
		super(a, R.layout.popup_yes_no);
		ok = (Button) layout.findViewById(R.id.yes_no_ok);
		ok.setOnClickListener(this);
		
		cancel = (Button) layout.findViewById(R.id.yes_no_cancel);
		cancel.setOnClickListener(this);
		
		infoHolder = (LinearLayout) layout.findViewById(R.id.info_holder);
		for(View v : infoViews) {
			infoHolder.addView(v);
		}
		
		Show();
	}

	@Override
	public void onClick(View v) {}

}
