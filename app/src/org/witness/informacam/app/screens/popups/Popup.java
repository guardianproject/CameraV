package org.witness.informacam.app.screens.popups;

import org.witness.informacam.app.utils.Constants.App;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;

public class Popup extends AlertDialog.Builder   {
	Activity a;
	
	public Object context;
	public Dialog alert;
	public View layout;
	
	protected final static String LOG = App.Home.LOG;
	
	interface PopupListener {
		public void perform();
	}
			
	
	public Popup(Activity a, int layoutId) {
		super(a);
		this.a = a;
		
		layout = LayoutInflater.from(a).inflate(layoutId, null);
		
		alert = create();
		setView(layout);
	}
	
	public void Show() {
		alert = this.show();
	}
	
	public void cancel() {
		alert.cancel();
	}
	
	public void setObjectContext(Object context) {
		this.context = context;
	}

}
