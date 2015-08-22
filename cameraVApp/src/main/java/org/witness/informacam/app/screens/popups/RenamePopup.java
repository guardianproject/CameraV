package org.witness.informacam.app.screens.popups;

import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.app.R;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RenamePopup extends Popup {
	public EditText prompt;
	Button commit;
	
	public RenamePopup(Activity a, final Object context) {
		super(a, R.layout.popup_rename);
		this.context = context;
		
		prompt = (EditText) layout.findViewById(R.id.rename_prompt);
		if(((IMedia) context).alias != null) {
			prompt.setText(((IMedia) context).alias);
		}
		commit = (Button) layout.findViewById(R.id.rename_commit);
		commit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((IMedia) context).rename(prompt.getText().toString());
				cancel();
			}
		});
		
		Show();
	}
}
