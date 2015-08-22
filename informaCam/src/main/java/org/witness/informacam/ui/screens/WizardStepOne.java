package org.witness.informacam.ui.screens;

import org.witness.informacam.InformaCam;
import org.witness.informacam.R;
import org.witness.informacam.ui.WizardActivity;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.Models.IUser;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WizardStepOne extends Fragment {
	View rootView;
	Activity a;

	EditText alias, email, password, passwordAgain;
	LinearLayout passwordStatus;
	TextView passwordStatusText;

	Handler handler = new Handler();

	int[] allIn = new int[] {0,0};
	final int[] ALL_IN = new int[] {1,1};

	private InformaCam informaCam = InformaCam.getInstance();
	
	private void checkAlias(String s) {
		if(s.length() >= 2) {
				informaCam.user.put(IUser.ALIAS, s);
				allIn[0] = 1;
			
		} else {
			allIn[0] = 0;
		}

		doIfAllIn();
	}
	
	private void checkPassword(String s1, String s2) {
		try {
			if(s1.length() >= 10) {
				if(String.valueOf(s1).equals(s2)) {
					informaCam.user.put(IUser.PASSWORD, s1);
					passwordStatusText.setText(getString(R.string.ok));
					
					allIn[1] = 1;
				} else {
					passwordStatusText.setText(getString(R.string.sorry_your_password_doesnt));
					allIn[1] = 0;
				}
			} else {
				passwordStatusText.setText(getString(R.string.your_password_should_be));
				allIn[1] = 0;
				
			}
		} catch(Exception e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
			
			allIn[1] = 0;
		}
		
		doIfAllIn();
	}

	TextWatcher readAlias = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			checkAlias(s.toString());
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}
	};

	TextWatcher readPassword = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			checkPassword(s.toString(), passwordAgain.getText().toString());
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}

	};

	TextWatcher matchPassword = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			checkPassword(password.getText().toString(), s.toString());
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

	};

	private static final String LOG = App.LOG; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(li, container, savedInstanceState);

		rootView = li.inflate(R.layout.fragment_wizard_step_one, null);
		alias = (EditText) rootView.findViewById(R.id.user_name);
		alias.addTextChangedListener(readAlias);
		
		email = (EditText) rootView.findViewById(R.id.user_email);

		password = (EditText) rootView.findViewById(R.id.user_password);
		password.addTextChangedListener(readPassword);

		passwordAgain = (EditText) rootView.findViewById(R.id.user_password_again);
		passwordAgain.addTextChangedListener(matchPassword);

		passwordStatus = (LinearLayout) rootView.findViewById(R.id.user_password_status);
		passwordStatusText = (TextView) rootView.findViewById(R.id.user_password_status_text);

		return rootView;
	}

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	private void doIfAllIn() {
		Log.d(LOG, "doing if all in...");
		Log.d(LOG, allIn[0] + " " + allIn[1]);
		for(int i: allIn) {
			if(i == 0) {
				return;
			}
		}
		
		informaCam.user.put(IUser.EMAIL, email.getText().toString());
		
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				((WizardActivity) a).autoAdvance();
			}
		}, 250);

	}
}
