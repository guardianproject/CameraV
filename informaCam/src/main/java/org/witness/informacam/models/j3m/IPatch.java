package org.witness.informacam.models.j3m;

import java.util.List;

import org.witness.informacam.models.Model;
import org.witness.informacam.models.forms.IForm;

public class IPatch extends Model {
	public String attachedMedia = null;
	public IForm attachedForm = null;
	public List<String> associatedCaches = null;
	
	public String _id = null;
	
	public IPatch() {
		super();
	}
}
