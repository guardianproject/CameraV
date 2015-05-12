package org.witness.informacam.app;

import info.guardianproject.onionkit.ui.OrbotHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.jcodec.common.IOUtils;
import org.witness.informacam.InformaCam;
import org.witness.informacam.models.media.IMedia;
import org.witness.informacam.share.WebShareService;
import org.witness.informacam.utils.Constants.Models;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class RemoteShareActivity extends Activity {

	private boolean mEnableServer = false;
	private TextView mTvInfo = null;
	private CheckBox mCbOnionShare = null;
	
	private int mLocalPort = 9999;
	private String mOnionHost = null;
			
	private String[] mMediaList = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("prefBlockScreenshots", false))
		{
	  		getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
	  				WindowManager.LayoutParams.FLAG_SECURE);      
		}
		
		setContentView(R.layout.activity_remote_share);
		setTitle(R.string.web_sharing);		
		getActionBar().setIcon(R.drawable.ic_action_backup);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	
		if (getIntent().hasExtra("medialist"))
			mMediaList = getIntent().getStringArrayExtra("medialist");
		
		mTvInfo = (TextView)findViewById(R.id.tvInfo);

		mCbOnionShare = (CheckBox) findViewById(R.id.cbOnionShare);
		
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
		
		mEnableServer = WebShareService.isRunning();
		
		if (mEnableServer)
		{
			StringBuffer sbInfo = new StringBuffer();
			
			sbInfo.append(getString(R.string.remote_web_access_enabled_at) + "\n\nhttp://" + WebShareService.getLocalIpAddress() + ":" + mLocalPort);
			
			mTvInfo.setText(sbInfo.toString());	
		}
	}

	@Override
	protected void onResume() {		
		super.onResume();
		

		if (getIntent().hasExtra("medialist"))
			mMediaList = getIntent().getStringArrayExtra("medialist");
		
	}

	private void manageRemoteAccess (boolean enableService) throws IOException
	{
		
		if (enableService)
		{
			initWebApp ();
			
			Intent intent = new Intent(this, WebShareService.class);
			intent.setAction(WebShareService.ACTION_SERVER_START);
			
			if (mMediaList != null)
				intent.putExtra("medialist", mMediaList);
			
			startService(intent);
			
			StringBuffer sbInfo = new StringBuffer();
			
			sbInfo.append(getString(R.string.remote_web_access_enabled_at) + "\n\nhttp://" + WebShareService.getLocalIpAddress() + ":" + mLocalPort);
			
			mTvInfo.setText(sbInfo.toString());
			
			if (mCbOnionShare.isChecked())
				initOnionSite();
		}
		else
		{
			StringBuffer sbInfo = new StringBuffer();
			
			sbInfo.append(getString(R.string.remote_web_access_disabled));
			
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
	
	private void initOnionSite ()
	{
		OrbotHelper oh = new OrbotHelper(this);
		if (oh.isOrbotInstalled())
		{
			if (mOnionHost == null)
				oh.requestHiddenServiceOnPort(this, mLocalPort);
			else if (!oh.isOrbotRunning())
				oh.requestOrbotStart(this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK)
		{
			mOnionHost = data.getStringExtra("hs_host");
			
			if (mOnionHost != null)
			{
				StringBuffer sbInfo = new StringBuffer();
				
				sbInfo.append(getString(R.string.remote_web_access_enabled_at)
						+ "\n\nLocal Area Network:\n\nhttp://" + WebShareService.getLocalIpAddress() + ":" + mLocalPort
						+ "\n\nOnionShare (Tor):\n\nhttp://" + mOnionHost + ":" + mLocalPort
					
						);
				
				mTvInfo.setText(sbInfo.toString());
			
			}
		}
	}
	
	
}
