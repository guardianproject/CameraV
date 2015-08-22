package org.witness.informacam.models.transport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.Model;
import org.witness.informacam.utils.Constants.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class ITransportManifest extends Model implements Serializable {
	public List<ITransportStub> transports = new ArrayList<ITransportStub>();
	
	private static final long serialVersionUID = -4261623918639178561L;
	
	public ITransportManifest() {
		super();
	}
	
	public ITransportManifest(ITransportManifest transportManifest) throws InstantiationException, IllegalAccessException {
		super();
		inflate(transportManifest);
	}
	
	public void add(ITransportStub transportStub) throws InstantiationException, IllegalAccessException {
		if(getById(transportStub.id) == null) {
			transports.add(transportStub);
		} else {
			getById(transportStub.id).inflate(transportStub);
		}
		
		InformaCam.getInstance().saveState(this);
	}
	
	public ITransportStub getByNotification(final String id) {
		Collection<ITransportStub> transports_ = Collections2.filter(transports, new Predicate<ITransportStub>() {
			@Override
			public boolean apply(ITransportStub transport) {
				Logger.d(LOG, String.format("retrying transport: %s", transport.id));
				if(transport.associatedNotification != null) {
					Logger.d(LOG, String.format("retrying transport notification: %s", transport.associatedNotification._id));
					return transport.associatedNotification._id.equals(id);
				} else {
					Logger.d(LOG, "THERE IS NO NOTIFICATION HERE");
					return false;
				}
			}
		});
		
		try {
			return transports_.iterator().next();
		} catch(NullPointerException e) {
			Logger.e(LOG, e);
		} catch(NoSuchElementException e) {
			Logger.e(LOG, e);
		}
		
		return null;
	}
	
	public ITransportStub getById(final String id) {
		Collection<ITransportStub> transports_ = Collections2.filter(transports, new Predicate<ITransportStub>() {
			@Override
			public boolean apply(ITransportStub transport) {
				return transport.id.equals(id);
			}
		});
		
		if (transports_.iterator().hasNext())
		{
			try {
				return transports_.iterator().next();
			} catch(NullPointerException e) {
				Logger.e(LOG, e);
			} catch(NoSuchElementException e) {
				Logger.e(LOG, e);
			}
		}
		
		return null;
	}
	
	public void save() {
		InformaCam.getInstance().saveState(this);
	}

}
