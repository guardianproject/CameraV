package org.witness.informacam.models.credentials;

import java.io.Serializable;

import org.witness.informacam.models.Model;

@SuppressWarnings("serial")
public class ICredentials extends Model implements Serializable {
	public String iv = null;
	public String passwordBlock = null;
	
	public ICredentials() {
		super();
	}
}
