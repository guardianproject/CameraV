package org.witness.iwitness.app.screens.popups;

import org.witness.iwitness.R;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class TextareaPopup extends Popup implements OnClickListener {
	protected EditText prompt;
	Button commit;
	
	public TextareaPopup(Activity a, final Object context) {
		super(a, R.layout.popup_textarea);
		
		prompt = (EditText) layout.findViewById(R.id.textarea_prompt);
		commit = (Button) layout.findViewById(R.id.textarea_commit);
		commit.setOnClickListener(this);
		
		this.context = context;
		
		Show();
	}

	@Override
	public void onClick(View v) {
		if(v == commit && prompt.getText().length() > 0) {
			cancel();
		}
		
	}

}
