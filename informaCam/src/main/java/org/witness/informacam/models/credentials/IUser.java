package org.witness.informacam.models.credentials;

import java.io.Serializable;

import org.witness.informacam.InformaCam;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.models.Model;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

@SuppressWarnings("serial")
public class IUser extends Model implements Serializable {
	public boolean hasBaseImage = false;
	public boolean hasPrivateKey = false;
	public boolean hasCompletedWizard = false;
	public boolean hasCredentials = false;

	public boolean isLoggedIn = false;
	public long lastLogIn = 0L;
	public long lastLogOut = 0L;

	public String alias = null;
	public String email = null;
	public String pgpKeyFingerprint = null;
	
	public boolean isInOfflineMode = false;
	
	private SharedPreferences sp = null;
	
	public IUser() {
		super();
	}
	
	public IUser(JSONObject user) throws InstantiationException, IllegalAccessException {
		super();
		inflate(user);
	}
	
	public Object getPreference(String prefKey, Object defaultObj) {		
		if(sp == null) {
			sp = PreferenceManager.getDefaultSharedPreferences(InformaCam.getInstance());
		}
				
		if(sp.contains(prefKey)) {
			Object value = sp.getAll().get(prefKey);
			
			if(!value.getClass().getName().equals(defaultObj.getClass().getName())) {
				if(defaultObj instanceof Boolean) {
					if(value instanceof String && (value.equals("1") || value.equals("0"))) {						
						return value.equals("1") ? true : false;
					} else if(value instanceof Integer && ((Integer) value == 1 || (Integer) value == 0)) {
						return (Integer) value == 1 ? true : false;
					}
				}
			}
			
			return value;
		}
		
		return defaultObj;
	}
	
	public void setIsLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
		save();
	}
	
	public void setHasBaseImage(boolean hasBaseImage) {
		this.hasBaseImage = hasBaseImage;
		save();
	}
	
	public void setHasCompletedWizard(boolean hasCompletedWizard) {
		this.hasCompletedWizard = hasCompletedWizard;
		save();
	}
	
	public void setHasPrivateKey(boolean hasPrivateKey) {
		this.hasPrivateKey = hasPrivateKey;
		save();
	}
	
	public void setHasCredentials(boolean hasCredentials) {
		this.hasCredentials = hasCredentials;
		save();
	}
	
	public boolean save() {
		return InformaCam.getInstance().saveState(this);
	}
}