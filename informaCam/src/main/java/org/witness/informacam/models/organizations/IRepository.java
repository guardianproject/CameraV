package org.witness.informacam.models.organizations;

import java.io.Serializable;

import org.witness.informacam.models.Model;

@SuppressWarnings("serial")
public class IRepository extends Model implements Serializable {
	public String source = null;
	
	public String asset_root = null;
	public String asset_id = null;
	
	public String applicationSignature = null;
	public String packageName = null;
	
}