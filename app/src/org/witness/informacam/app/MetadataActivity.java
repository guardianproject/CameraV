package org.witness.informacam.app;

import java.io.FileNotFoundException;

import org.witness.informacam.InformaCam;
import org.witness.informacam.app.utils.Constants.Codes;
import org.witness.informacam.models.media.IMedia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


public class MetadataActivity extends Activity {

	private InformaCam informaCam;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
			String j3m = ((IMedia) media).buildJ3M(this, signData, new Handler());
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(j3m);
			String prettyJsonString = gson.toJson(je);
			
			TextView txtView = (TextView)findViewById(R.id.textarea_metadata);
			txtView.setText(prettyJsonString);
			
			txtView.setMovementMethod(new ScrollingMovementMethod());
		}
		catch (Exception e)
		{
			finish();
		}
	}

}
