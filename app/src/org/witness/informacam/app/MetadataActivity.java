package org.witness.informacam.app;

import android.app.Activity;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.TextView;
import org.ibanet.informacam.R;


public class MetadataActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_metadata);
	}

	@Override
	protected void onResume() {
		
		super.onResume();
		
		String displayText = "";
		

		if (getIntent() != null && getIntent().hasExtra("title"))
		{
			setTitle(getIntent().getStringExtra("title"));
		}
		
		
		if (getIntent() != null && getIntent().hasExtra("text"))
		{
			displayText = getIntent().getStringExtra("text");
		}
		
		TextView txtView = (TextView)findViewById(R.id.textarea_metadata);
		txtView.setText(displayText);
		
		txtView.setMovementMethod(new ScrollingMovementMethod());
	}

}
