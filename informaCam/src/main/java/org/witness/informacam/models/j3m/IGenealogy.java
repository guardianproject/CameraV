package org.witness.informacam.models.j3m;

import java.util.List;

import org.witness.informacam.models.Model;

public class IGenealogy extends Model {
	public String localMediaPath = null;
	public long dateCreated = 0L;
	public String createdOnDevice = null;
	public List<String> hashes = null;
	public String j3m_version = "J3M version 1.0";	
}
