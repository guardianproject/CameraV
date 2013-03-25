package org.witness.iwitness.models;

import android.app.Activity;
import org.witness.informacam.utils.models.IUser;


public class User extends IUser {
	
	public User(Activity a) {
		this(a, false);
	}
	
	public User(Activity a, boolean isDummy) {
		if(isDummy) {
			alias = "Harlo";
		}
	}
}
