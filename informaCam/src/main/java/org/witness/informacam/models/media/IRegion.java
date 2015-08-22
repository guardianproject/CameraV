package org.witness.informacam.models.media;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.informa.InformaService;
import org.witness.informacam.models.Model;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.ui.editors.IRegionDisplay;
import org.witness.informacam.utils.Constants;
import org.witness.informacam.utils.Constants.IRegionDisplayListener;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.MediaHasher;

import android.app.Activity;

public class IRegion extends Model {
	public String id = null;
	public int index = -1;
	public long timestamp = 0L;	

	public List<IForm> associatedForms = new ArrayList<IForm>();
	public IRegionBounds bounds = null;

	private IRegionDisplay regionDisplay = null;
	private IRegionDisplayListener mListener = null;
	
	public IRegion() {
		super();
	}

	public IRegion(IRegion region) throws InstantiationException, IllegalAccessException {
		super();
		inflate(region.asJson());
	}

	public void init(Activity context, IRegionBounds bounds, IRegionDisplayListener listener) {
		init(context, bounds, true, listener);
	}

	public void init(Activity context, IRegionBounds bounds, boolean isNew, IRegionDisplayListener listener) {
		
		this.bounds = bounds;
		mListener = listener;
		
		regionDisplay = new IRegionDisplay(context, this, mListener);

		if(isNew) {
			if(mListener != null) {
				this.bounds.calculate(mListener.getSpecs(),context);
			}
			
			try {
				byte[] idBytes = new String(System.currentTimeMillis() + new String(Constants.App.Crypto.REGION_SALT)).getBytes();
				id = MediaHasher.hash(idBytes, "MD5");
			} catch (NoSuchAlgorithmException e) {
				Logger.e(LOG, e);
			} catch (IOException e) {
				Logger.e(LOG, e);
			}
			
			
		}
	}
	
	public boolean isInnerLevelRegion() {
		if(bounds.displayHeight == 0 && bounds.displayWidth == 0) {
			return false;
		}
		
		return true;
	}
	
	public IForm addForm(IForm form) {
		form.id = IForm.appendId();
		associatedForms.add(form);
	//	Logger.d(LOG, "NEW FORM:\n" + form.asJson().toString());
	//	Logger.d(LOG, "This region:\n" + this.asJson().toString());
		
		return form;
	}
	
	public IForm getFormByNamespace(String namespace) {
		/**
		 * Returns the (first) form associated to this region 
		 * by the provided namespace.  Can (should) be used
		 * in instances where only one form of the specified
		 * namespace is bound to be present.
		 * 
		 * __Args:__
		 * # namespace (String)
		 */
		return getFormsByNamespace(namespace).get(0);
	}
	
	public List<IForm> getFormsByNamespace(String namespace) {
		/**
		 * Returns the forms associated to this region 
		 * by the provided namespace.
		 * 
		 * __Args:__
		 * # namespace (String)
		 */
		List<IForm> forms = new ArrayList<IForm>();
		for(IForm form : this.associatedForms) {
			if(form.namespace.equals(namespace)) {
				forms.add(form);
			}
		}
		return forms;
	}

	public IRegionDisplay getRegionDisplay() {
		return regionDisplay;
	}
	
	public void update(Activity a) {
		InformaCam informaCam = InformaCam.getInstance();
		
		if(mListener != null) {
			bounds.calculate(mListener.getSpecs(), a);
		}
		
		InformaService.getInstance().updateRegion(this);
	}

	public void delete(IMedia parent) {
		InformaService.getInstance().removeRegion(this);
		parent.associatedRegions.remove(this);
	}
}
