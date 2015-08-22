package org.witness.informacam.storage;

import info.guardianproject.odkparser.FormWrapper;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.forms.IInstalledForms;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.App.Storage.Type;
import org.witness.informacam.utils.Constants.Forms;
import org.witness.informacam.utils.Constants.IManifest;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.MediaHasher;

import android.app.Activity;
import android.util.Log;

public class FormUtility {
	public final static String LOG = Forms.LOG;
		
	public static List<IForm> getAvailableForms() throws InstantiationException, IllegalAccessException {
		InformaCam informaCam = InformaCam.getInstance();
		IInstalledForms installedForms = new IInstalledForms();
		installedForms.inflate(informaCam.ioService.getBytes(IManifest.FORMS, Type.IOCIPHER));

		return installedForms.installedForms;
	}

	public static boolean installIncludedForms(Activity a) throws InstantiationException, IllegalAccessException {
		return installIncludedForms(a, IManifest.FORMS);
	}

	public static boolean installIncludedForms(Activity a, String formRoot) throws InstantiationException, IllegalAccessException {
		InformaCam informaCam = InformaCam.getInstance();		
		try {
			if(a.getAssets().list(formRoot).length > 0) {
				for(String xmlPath : a.getAssets().list(formRoot)) {
					InputStream xml = new ByteArrayInputStream(informaCam.ioService.getBytes(formRoot + "/" + xmlPath, Type.APPLICATION_ASSET));
					if(importAndParse(xml) == null) {
						return false;
					}
				}

				return true;
			}
		} catch(IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}

		return false;
	}

	public static IForm importAndParse(InputStream xml_stream) throws InstantiationException, IllegalAccessException {
		InformaCam informaCam = InformaCam.getInstance();

		info.guardianproject.iocipher.File formRoot = new info.guardianproject.iocipher.File(Storage.FORM_ROOT);

		if(!formRoot.exists()) {
			formRoot.mkdir();
			Log.d(LOG, "initing the form root directory");
		}

		boolean hasFormDef = false;

		IInstalledForms installedForms = new IInstalledForms();
		installedForms.inflate(informaCam.ioService.getBytes(IManifest.FORMS, Type.IOCIPHER));
		
		if(installedForms.installedForms == null) {
			installedForms.installedForms = new ArrayList<IForm>();
		}

		IForm form = new IForm();
		String xmlHash = null;
		info.guardianproject.iocipher.File xmlFile = null;
		byte[] xml = null;

		try {
			xml = new byte[xml_stream.available()];
			xml_stream.read(xml);
			xml_stream.close();

			xmlHash = MediaHasher.hash(xml, "MD5");
			xmlFile = new info.guardianproject.iocipher.File(Storage.FORM_ROOT, xmlHash + ".xml" );
			form.path = xmlFile.getAbsolutePath();

			if(installedForms.installedForms != null && installedForms.installedForms.size() > 0) {
				for(IForm f : installedForms.installedForms) {
					if(f.path.equals(form.path)){
						form = f;
						hasFormDef = true;
						break;
					}
				}
			}

		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
			return null;
		}

		FormWrapper form_wrapper = new FormWrapper(new ByteArrayInputStream(xml), true);
		if(form_wrapper.form_def == null) {
			Logger.d(LOG, "form wrapper was null, exiting...");
			return null;
		}

		if(!hasFormDef) {
			
			if(informaCam.ioService.saveBlob(xml, xmlFile)) {
				form.title = form_wrapper.form_def.getTitle();
				form.namespace = form_wrapper.form_def.getName();
				installedForms.installedForms.add(form);

				Log.d(LOG, "new form: " + form.asJson().toString());
				informaCam.saveState(installedForms);
				
			}
		}

		return form;
	}

	public static IForm importAndParse(Activity a, java.io.File xml) {
		try {
			return importAndParse(new java.io.FileInputStream(xml));
		} catch (Exception e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
			return null;
		}
	}
}
