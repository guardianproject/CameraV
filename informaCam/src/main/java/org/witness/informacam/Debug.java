package org.witness.informacam;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.witness.informacam.informa.embed.VideoConstructor;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models.IUser;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Debug {
	
	public static final String DEBUG_TAG = "ICDEBUG";
	public static final boolean WAIT_FOR_DEBUGGER = false;
	public static final String LOG = "*********************************** " + DEBUG_TAG + "*********************************** ";
	
	public static final boolean DEBUG = true;
	
	public static void testUser_1() {
		InformaCam informaCam = InformaCam.getInstance();
		
	//	Logger.d(LOG, "TEST USER SETTINGS INABLED:");
	//	Logger.d(LOG, informaCam.user.asJson().toString());
		
	//	boolean enc = (Boolean) informaCam.user.getPreference(IUser.ASSET_ENCRYPTION, false);
	//	Logger.d(LOG, "USER ENC: " + enc);
	}
	
	public static void googledriveTest() {
		InformaCam informaCam = InformaCam.getInstance();
		
		informaCam.resendCredentials(informaCam.installedOrganizations.organizations.get(0));
	}
	
	public static void testFFmpeg() {
		try {
			VideoConstructor vc = new VideoConstructor(InformaCam.getInstance());
			vc.testFFmpeg();
		} catch (FileNotFoundException e) {
			Logger.e(LOG, e);
		} catch (IOException e) {
			Logger.e(LOG, e);
		}
	}
	
	/*
	public static void fix_default_asset_encryption() {
		InformaCam informaCam = InformaCam.getInstance();
		
		boolean originalImageHandling = (Boolean) informaCam.user.getPreference("originalImageHandling", false);
		Logger.d(LOG, "USER'S ORIGINAL ENC SETTINGS: " + originalImageHandling);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(InformaCam.getInstance());
		SharedPreferences.Editor ed = sp.edit();
		ed.putString(IUser.ASSET_ENCRYPTION, originalImageHandling ? "0" : "1");
		ed.commit();
		
		Logger.d(LOG, "USER'S ENC SETTINGS NOW: " + (Boolean) informaCam.user.getPreference(IUser.ASSET_ENCRYPTION, false));
	}*/
}
