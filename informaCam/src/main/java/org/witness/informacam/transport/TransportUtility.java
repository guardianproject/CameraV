package org.witness.informacam.transport;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.organizations.IRepository;
import org.witness.informacam.models.transport.ITransportStub;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;
import org.witness.informacam.utils.Constants.App.Transport;

import android.content.Intent;

public class TransportUtility {
	private static final String LOG = Transport.LOG;

	public static void initTransport(ITransportStub transportStub) {
		InformaCam informaCam = InformaCam.getInstance();
		Logger.d(LOG, "TRANSPORT:\n" + transportStub.asJson().toString());

		for(IRepository repository : transportStub.organization.repositories) {

			Intent intent = null;
			if(repository.source.equals(Models.ITransportStub.RepositorySources.GOOGLE_DRIVE)) {
				//intent = new Intent(informaCam, GoogleAccountUtility.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent = new Intent(informaCam, GoogleDriveTransport.class);
			}

			if(repository.source.equals(Models.ITransportStub.RepositorySources.GLOBALEAKS)) {
				intent = new Intent(informaCam, GlobaleaksTransport.class);
			}

			if(repository.source.equals(Models.ITransportStub.RepositorySources.APP)) {
				// look up the APK by package name and signature
				// so, since this is in the transport loop, i pretty much only ever have an encrypted file.  can i share this out as bytes? or must i burn it to SD?

				/*
				Uri uri = URI.create("");
				intent = new Intent(Intent.ACTION_SEND)
					.setPackage(repository.packageName)
					.putExtra(Intent.EXTRA_STREAM, uri);
				 */
			}

			if(intent != null) {
				Logger.d(LOG, "HEY STARTING TO TRANSPORT");

				intent.putExtra(Models.ITransportStub.TAG, transportStub);
				informaCam.startService(intent);
			}
		}
	}
}
