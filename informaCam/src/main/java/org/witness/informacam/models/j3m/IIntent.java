package org.witness.informacam.models.j3m;

import org.witness.informacam.models.Model;
import org.witness.informacam.utils.Constants.Models;

public class IIntent extends Model {
	public String alias = null;
	public String pgpKeyFingerprint = null;
	public String intendedDestination = null;
	public int ownershipType = Models.IGenealogy.OwnershipType.INDIVIDUAL;
}
