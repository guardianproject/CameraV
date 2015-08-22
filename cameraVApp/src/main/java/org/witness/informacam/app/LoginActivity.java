package org.witness.informacam.app;

import net.hockeyapp.android.CrashManager;

import org.witness.informacam.InformaCam;
import org.witness.informacam.app.utils.Constants;
import org.witness.informacam.app.utils.UIHelpers;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private final static String LOG = Constants.App.Login.LOG;
	private String packageName;

	View rootView;
	EditText password;
//	Button commit;
	ProgressBar waiter;

	Handler h = new Handler();

	InformaCam informaCam = InformaCam.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		packageName = this.getPackageName();

		setContentView(R.layout.activity_login);
		rootView = findViewById(R.id.llRoot);
		
		boolean prefStealthIcon = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("prefStealthIcon",false);
		if (prefStealthIcon)
		{
			ImageView iv = (ImageView)findViewById(R.id.loginLogo);
			iv.setImageResource(R.drawable.ic_launcher_alt);
		}

		password = (EditText) findViewById(R.id.login_password);
		password.setImeOptions(EditorInfo.IME_ACTION_DONE);
		password.setOnEditorActionListener(new OnEditorActionListener ()
		{
			@Override
			public boolean onEditorAction(TextView arg0, int actionId, KeyEvent event) {
				 if (actionId == EditorInfo.IME_ACTION_SEARCH ||
			                actionId == EditorInfo.IME_ACTION_DONE ||
			                event.getAction() == KeyEvent.ACTION_DOWN &&
			                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {			
							doLogin ();
							
						}
					   return true;
			}
			
		});

		/**
		commit = (Button) findViewById(R.id.login_commit);
		commit.setOnClickListener(this);
		*/

		waiter = (ProgressBar) findViewById(R.id.login_waiter);
		
		checkForCrashes();
		checkForUpdates();
	}

	private void toggleStatus(boolean showButton) {
		if(showButton) {
			waiter.setVisibility(View.GONE);
		//	commit.setVisibility(View.VISIBLE);
		} else {
			//commit.setVisibility(View.GONE);
			waiter.setVisibility(View.VISIBLE);
		}
	}

	
	public void doLogin () 
	{

		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
			
		if(password.getText().length() > 0) {
			h.post(new Runnable() {
				@Override
				public void run() {
					toggleStatus(false);
				}
			});

			new Thread(new Runnable() {
				@Override
				public void run() {
					char[] charPass = new char[password.length()];
					password.getText().getChars(0, password.length(), charPass, 0);
					if(informaCam.attemptLogin(charPass)) {
						setResult(Activity.RESULT_OK);
						finish();
					} else {
						h.post(new Runnable() {
							@Override
							public void run() {
								password.setText("");
								toggleStatus(true);
								Toast.makeText(LoginActivity.this, getString(R.string.we_could_not_log), Toast.LENGTH_LONG).show();
							}
						});

					}
				}
			}).start();
		}

	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		boolean handled = super.onTouchEvent(event);
		if (!handled && event.getAction() == MotionEvent.ACTION_UP)
		{
			rootView.requestFocus();
			UIHelpers.hideSoftKeyboard(this);
		}
		return handled;
	}
	

	private void checkForCrashes()
	{
		CrashManager.register(this, InformaActivity.HOCKEY_APP_ID);
	}

	private void checkForUpdates()
	{
		// XXX: Remove this for store builds!
		//UpdateManager.register(this, InformaActivity.HOCKEY_APP_ID);
	}

	
}
