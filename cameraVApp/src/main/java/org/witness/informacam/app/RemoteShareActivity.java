package org.witness.informacam.app;

import info.guardianproject.netcipher.proxy.OrbotHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;
import org.jcodec.common.IOUtils;
import org.witness.informacam.share.WebShareService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
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
	private Button btnActivate = null;
	private Button btnShare= null;
	
	private String mLocalHost = null;
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
		
		btnShare = (Button)findViewById(R.id.buttonShare);
		btnShare.setOnClickListener(new OnClickListener ()
		{

			@Override
			public void onClick(View v) {
			
				shareWebAddress();
				
			}
			
		});
		btnActivate = (Button)findViewById(R.id.buttonService);
		btnActivate.setOnClickListener(new OnClickListener ()
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
		mOnionHost = WebShareService.getOnionSite();
		
		showStatusMessage ();
		
	}

	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	        }
	    }

	@Override
	protected void onResume() {		
		super.onResume();

		if (getIntent().hasExtra("medialist"))
			mMediaList = getIntent().getStringArrayExtra("medialist");
		
		
		if (mConnectivityMonitor == null)
			mConnectivityMonitor= new BroadcastReceiver()
		      {
		
				@Override
				public void onReceive(Context context, Intent intentChange) {
					
					initLocalHost ();
					
					
				}
		    	  
		      };
	
    registerReceiver(
    		mConnectivityMonitor,
    	      new IntentFilter(
    	            ConnectivityManager.CONNECTIVITY_ACTION));
		
	}
	
	
	
	@Override
	protected void onPause() {
				super.onPause();
		
		unregisterReceiver(mConnectivityMonitor);
	}

	private void initLocalHost ()
	{
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
	    
	    if (cm != null && cm.getActiveNetworkInfo() != null)
	    {
	    	int networkType = cm.getActiveNetworkInfo().getType();
	    	
	    	if (networkType == ConnectivityManager.TYPE_WIFI)
	    	{
	    		mLocalHost = null; //don't set a local host for web share server, so that it binds to public IP
	    	}
	    	else if (mCbOnionShare.isChecked()) 
	    	{
	    		mLocalHost = "127.0.0.1";//use local host for 3G/4G etc, since we are only sharing via Tor Hidden Service
	    	}
	    	else
	    	{
	    		mLocalHost = getLocalIpAddresses()[0];
	    	}
	    	
	    }
	}
	
	private BroadcastReceiver mConnectivityMonitor;
	

	private void manageRemoteAccess (boolean enableService) throws IOException
	{
		
		if (enableService)
		{
			initWebApp ();
			
			initLocalHost ();
			
			Intent intent = new Intent(this, WebShareService.class);
			intent.setAction(WebShareService.ACTION_SERVER_START);
			
			if (mLocalHost != null)
				intent.putExtra("host", mLocalHost);
				
			intent.putExtra("port", mLocalPort);
			
			if (mMediaList != null)
				intent.putExtra("medialist", mMediaList);
			
			startService(intent);
			
			if (mCbOnionShare.isChecked())
				initOnionSite();
		}
		else
		{
			
			
			Intent intent = new Intent(this, WebShareService.class);
			intent.setAction(WebShareService.ACTION_SERVER_STOP);
			startService(intent);
		}
		
		showStatusMessage ();
	}
	
	private void initWebApp () throws IOException
	{
		InputStream is = getResources().openRawResource(R.raw.style);
		OutputStream os = new info.guardianproject.iocipher.FileOutputStream("/style.css");
		IOUtils.copy(is, os);
	}
	
	private void initOnionSite ()
	{
		if (OrbotHelper.isOrbotInstalled(this))
		{
			OrbotHelper.requestStartTor(this);

			if (mOnionHost == null)
				OrbotHelper.requestHiddenServiceOnPort(this, mLocalPort);

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK && data != null)
		{
			mOnionHost = data.getStringExtra("hs_host");
			
			WebShareService.setOnionSite(mOnionHost);
			
			showStatusMessage();
			
		}
	}
	
	private void shareWebAddress ()
	{

		if (mEnableServer)
		{
			String shareUrl = null;
			
			if (mOnionHost != null)
			{
				shareUrl = "http://" + mOnionHost + ":" + mLocalPort;
			}
			else if (mLocalHost != null)
			{
				shareUrl = "http://" + mLocalHost + ":" + mLocalPort;
			}
			else
			{
				shareUrl = "http://" + getLocalIpAddresses()[0]  + ":" + mLocalPort;
			}
			
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.you_can_access_my_camerav_photos_and_videos_here_) + shareUrl);
			sendIntent.setType("text/plain");
			
			startActivity(sendIntent);
			
		}
		
	}
	
	private void showStatusMessage ()
	{
		StringBuffer sbInfo = new StringBuffer();
		
		if (mEnableServer)
		{
			
			sbInfo.append(getString(R.string.remote_web_access_enabled_at)).append("\n");
			
			sbInfo.append("http://" + mLocalHost + ":" + mLocalPort + "\n");
			
			if (mOnionHost != null)
				sbInfo.append("\n\nOnionShare (Tor):\n").append("http://" + mOnionHost + ":" + mLocalPort);
			
			btnActivate.setText(R.string.deactivate_web_share);
			btnShare.setEnabled(true);
			
			if (mOnionHost != null)
				mCbOnionShare.setChecked(true);
			
		}
		else
		{
			
			sbInfo.append(getString(R.string.remote_web_access_disabled));
			btnActivate.setText(R.string.activate_web_share);
			btnShare.setEnabled(false);
			
		}
		

		mTvInfo.setText(sbInfo.toString());
	}
	

	public static String[] getLocalIpAddresses(){
		   try {
			   ArrayList<String> alAddresses = new ArrayList<String>();
			   
		       for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();  
		       en.hasMoreElements();) {
		       NetworkInterface intf = en.nextElement();
		           for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
		           InetAddress inetAddress = enumIpAddr.nextElement();
		                if (!inetAddress.isLoopbackAddress()&& InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
		                	alAddresses.add(inetAddress.getHostAddress());
		                
		                }
		           }
		       }
		       
		       return alAddresses.toArray(new String[alAddresses.size()]);
		       
		       } catch (Exception ex) {
		          Log.e("IP Address", ex.toString());
		      }
		      return null;
		}
	
	
}
