package org.witness.informacam.models.organizations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.models.Model;
import org.witness.informacam.models.forms.IForm;

@SuppressWarnings("serial")
public class IOrganization extends Model implements Serializable {
	public String organizationName = null;
	public String organizationDetails = null;
	public String publicKey = null;
	public String organizationFingerprint = null;
	public String organizationIcon = null;
	public boolean keyReceived = false;
	public List<IRepository> repositories = new ArrayList<IRepository>();
	public List<IForm> forms = new ArrayList<IForm>();
	
	public IOrganization() {
		super();
	}
	
	public IOrganization(JSONObject organization) throws InstantiationException, IllegalAccessException {
		super();
		inflate(organization);
	}
	
	public void save() throws InstantiationException, IllegalAccessException {
		InformaCam informaCam = InformaCam.getInstance();
		informaCam.installedOrganizations.getByFingerprint(organizationFingerprint).inflate(this);
		informaCam.installedOrganizations.save();
	}
}
