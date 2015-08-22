package org.witness.informacam.app;

import org.witness.informacam.InformaCam;
import org.witness.informacam.app.utils.Constants.Codes;
import org.witness.informacam.models.media.IMedia;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class MetadataActivity extends Activity {

	private InformaCam informaCam;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("prefBlockScreenshots", false))
		{
	  		getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
	  				WindowManager.LayoutParams.FLAG_SECURE);      
		}
		
		setContentView(R.layout.activity_metadata);
		

		informaCam = (InformaCam) getApplication();
		
		
		
	}

	
	@Override
	protected void onResume() {
		
		super.onResume();
		
		try
		{
			boolean signData = false;
			IMedia media = informaCam.mediaManifest.getById(getIntent().getStringExtra(Codes.Extras.EDIT_MEDIA));
			String j3m = ((IMedia) media).buildJ3M(this, signData, null);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(j3m);
			String prettyJsonString = gson.toJson(je);
			
			EditText txtView = (EditText)findViewById(R.id.textarea_metadata);
			txtView.setText(prettyJsonString);
			
			txtView.setMovementMethod(new ScrollingMovementMethod());
		}
		catch (Exception e)
		{
			finish();
		}
	}

}
