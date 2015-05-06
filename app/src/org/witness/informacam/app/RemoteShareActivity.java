package org.witness.informacam.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jcodec.common.IOUtils;
import org.witness.informacam.share.WebShareService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class RemoteShareActivity extends Activity {

	private boolean mEnableServer = false;
	private TextView mTvInfo = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_remote_share);
	
		mTvInfo = (TextView)findViewById(R.id.tvInfo);
		
		Button btn = (Button)findViewById(R.id.buttonService);
		btn.setOnClickListener(new OnClickListener ()
		{

			@Override
			public void onClick(View v) {
			
				mEnableServer = !mEnableServer;
				
				try {
					manageRemoteAccess(mEnableServer);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
	}

	private void manageRemoteAccess (boolean enableService) throws IOException
	{
		
		if (enableService)
		{
			initWebApp ();
			
			Intent intent = new Intent(this, WebShareService.class);
			intent.setAction(WebShareService.ACTION_SERVER_START);
			startService(intent);
			
			StringBuffer sbInfo = new StringBuffer();
			
			sbInfo.append("You are now sharing at http://" + WebShareService.getLocalIpAddress() + ":9999");
			
			mTvInfo.setText(sbInfo.toString());
		}
		else
		{
			StringBuffer sbInfo = new StringBuffer();
			
			sbInfo.append("You are not sharing");
			
			mTvInfo.setText(sbInfo.toString());
			
			Intent intent = new Intent(this, WebShareService.class);
			intent.setAction(WebShareService.ACTION_SERVER_STOP);
			startService(intent);
		}
	}
	
	private void initWebApp () throws IOException
	{
		InputStream is = getResources().openRawResource(R.raw.style);
		OutputStream os = new info.guardianproject.iocipher.FileOutputStream("/style.css");
		IOUtils.copy(is, os);
	}
}
