package org.witness.informacam.app;

import org.witness.informacam.share.WebShareService;

import android.app.Activity;
import android.content.Intent;

public class RemoteShareActivity extends Activity {

	
	private void enableRemoteAccess ()
	{
		Intent intent = new Intent(this, WebShareService.class);
		intent.setAction(WebShareService.ACTION_SERVER_START);
		startService(intent);

	}
}
