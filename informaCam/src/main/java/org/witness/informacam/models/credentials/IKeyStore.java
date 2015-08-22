package org.witness.informacam.models.credentials;

import java.io.Serializable;

import org.witness.informacam.json.JSONObject;
import org.witness.informacam.models.Model;

@SuppressWarnings("serial")
public class IKeyStore extends Model implements Serializable {
	public String path = null;
	public String password = null;
	public long lastModified = 0L;
	
	public IKeyStore() {
		super();
	}
	
	public IKeyStore(IKeyStore keyStore) throws InstantiationException, IllegalAccessException {
		super();
		inflate(keyStore.asJson());
	}
	
	public IKeyStore(JSONObject keyStore) throws InstantiationException, IllegalAccessException {
		super();
		inflate(keyStore);
	}
}
