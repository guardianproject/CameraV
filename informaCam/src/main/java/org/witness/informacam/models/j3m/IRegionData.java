package org.witness.informacam.models.j3m;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.models.Model;
import org.witness.informacam.models.forms.IForm;
import org.witness.informacam.models.media.IRegion;
import org.witness.informacam.models.media.IRegionBounds;
import org.witness.informacam.storage.IOUtility;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Models;

public class IRegionData extends Model {
	public List<IForm> associatedForms = null;
	public IRegionBounds regionBounds = null;
	public ILocation location = null;
	public long timestamp = 0L;
	public String id = null;
	public String index = null;
	
	public IRegionData() {
		super();
	}
	
	@Override
	public void inflate(JSONObject values) {
		try {
			if(values.has(Models.IRegion.INDEX)) {
				values = values.put(Models.IRegion.INDEX, Integer.toString(values.getInt(Models.IRegion.INDEX)));
			}
		
		
			super.inflate(values);
		} catch (Exception e) {
			Logger.e(LOG, e);
		}
	}
	
	@Override
	public JSONObject asJson() {
		JSONObject obj = super.asJson();
		Logger.d(LOG, obj.toString());
		
		try {
			obj = obj.put(Models.IRegion.INDEX, Integer.parseInt(index));
		} catch (NumberFormatException e) {}
		catch (JSONException e) {
			Logger.e(LOG, e);
		}
		
		return obj;
	}
	
	public IRegionData(IRegion region) throws FileNotFoundException {
		this(region, null);
	}
	
	public IRegionData(IRegion region, ILocation location) throws FileNotFoundException {
		super();
		
		timestamp = region.timestamp;
		id = region.id;
		
		this.location = location;
		
		if(region.isInnerLevelRegion()) {
			this.regionBounds = region.bounds;
			
			// The reason why it's cast to a string is because if public field is null, it will be omitted.
			// you can't set an int to null, though, so...
			if(region.index > -1) {
				this.index = Integer.toString(region.index);
			}
		}		
		
		for(IForm form : region.associatedForms) {
			
				info.guardianproject.iocipher.File file = new info.guardianproject.iocipher.File(form.answerPath);
				if (file.exists())
				{
					info.guardianproject.iocipher.FileInputStream is = new info.guardianproject.iocipher.FileInputStream(file);
					
					JSONObject answerData = IOUtility.xmlToJson(is);
					if(answerData.length() == 0) {
						continue;
					}
					
					form.answerData = answerData;
					form.answerPath = null;
					form.title = null;
					
					if(associatedForms == null) {
						associatedForms = new ArrayList<IForm>();
					}
					
					associatedForms.add(form);
				}
			
		}
	}
}
