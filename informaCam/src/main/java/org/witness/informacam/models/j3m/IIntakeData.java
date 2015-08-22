package org.witness.informacam.models.j3m;

import org.witness.informacam.InformaCam;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.models.Model;
import org.witness.informacam.utils.Constants.Logger;

import android.util.Base64;

public class IIntakeData extends Model {
	public byte[] data = null;
	public String signature = null;
	
	public IIntakeData() {
		super();
	}
	
	public IIntakeData(IIntakeData intakeData) throws InstantiationException, IllegalAccessException {
		super();
		inflate(intakeData);
	}
	
	public IIntakeData(long timeCreated, String timezone, long timeOffset, String originalHash, String cameraComponentPackageName) {
		super();
		InformaCam informaCam = InformaCam.getInstance();
				
		JSONObject dataObj = new JSONObject();
		
		try {
			dataObj.put("timezone", timezone);
			dataObj.put("timeCreated", timeCreated);
			dataObj.put("timeOffset", timeOffset);
			dataObj.put("cameraComponentPackageName", cameraComponentPackageName);
			dataObj.put("originalHash", originalHash);
			
			data = Base64.encode(dataObj.toString().getBytes(), Base64.DEFAULT);
			signature = new String(informaCam.signatureService.signData(data));
		} catch(Exception e) {
			Logger.e(LOG, e);
		}
	}
}