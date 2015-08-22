package org.witness.informacam.models.transport;

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

import org.witness.informacam.InformaCam;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.models.Model;
import org.witness.informacam.models.media.IAsset;
import org.witness.informacam.models.notifications.INotification;
import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.models.organizations.IRepository;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.MediaHasher;

@SuppressWarnings("serial")
public class ITransportStub extends Model implements Serializable {
	public String id = null;
	public int numTries = 0;
	public boolean isHeld = false;
	public int method = Models.ITransportStub.Methods.POST;
	public String lastResult = null;
	
	public INotification associatedNotification = null;
	public IOrganization organization = null;
	public ITransportData asset = null;
	
	public int callbackCode = 0;
	public int resultCode = Models.ITransportStub.ResultCodes.FAIL;
	
	public ITransportStub() {
		this(null, null);
	}
	
	public ITransportStub(IOrganization organization) {
		this(organization, null);
	}
	
	public ITransportStub(IOrganization organization, INotification associatedNotification) {
		super();
		
		this.organization = organization;
		this.associatedNotification = associatedNotification;
		
		try {
			this.id = MediaHasher.hash(new String(System.currentTimeMillis() + Models.ITransportStub.ID_HASH).getBytes(), "MD5");
		} catch (NoSuchAlgorithmException e) {
			Logger.e(LOG, e);
		} catch (IOException e) {
			Logger.e(LOG, e);
		}
	}
	
	public ITransportStub(JSONObject transportStub) throws InstantiationException, IllegalAccessException {
		super();
		inflate(transportStub);
	}
	
	public ITransportStub(ITransportStub transportStub) throws InstantiationException, IllegalAccessException {
		super();
		inflate(transportStub.asJson());
	}
	
	public void reset() {
		reset(null);
	}
	
	public void reset(INotification associatedNotification) {
		numTries = 0;
		isHeld = false;
		lastResult = null;
		
		if(associatedNotification != null) {
			this.associatedNotification = associatedNotification;
		}
	}
	
	public void setAsset(String assetName, String assetPath, String mimeType, int storageType) {
		if(asset == null) {
			asset = new ITransportData();
		}
		
		asset.assetName = assetName;
		asset.assetPath = assetPath;
		asset.mimeType = mimeType;
		asset.storageType = storageType;
	}
	
	public void setAsset(IAsset asset, String mimeType, int storageType) {
		setAsset(asset.name, asset.path, mimeType, storageType);
	}
	
	public String getAssetRootOfRepository(String source) {
		for(IRepository repository : organization.repositories) {
			if(repository.source.equals(source)) {
				return repository.asset_root;
			}
		}
		
		return null;
	}
	
	public IRepository getRepository(String source) {
		
		for(IRepository repository : organization.repositories) {
			if(repository.source.equals(source)) {
				return repository;
			}
		}
		
		return null;
	}
	
	public void save() throws InstantiationException, IllegalAccessException {
		InformaCam informaCam = InformaCam.getInstance();
		ITransportStub transport = informaCam.transportManifest.getById(id);
		
		if(transport == null) {
			InformaCam.getInstance().transportManifest.add(this);
		} else {
			transport.inflate(this);
			informaCam.transportManifest.save();
		}
	}
}