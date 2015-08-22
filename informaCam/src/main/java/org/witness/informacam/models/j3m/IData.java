package org.witness.informacam.models.j3m;

import java.util.List;

import org.witness.informacam.models.Model;

public class IData extends Model {
	public List<ISensorCapture> sensorCapture = null;
	public List<IRegionData> userAppendedData = null;
	public IExif exif = null;
	public List<String> attachments = null;
	public IIntakeData intakeData = null;
}
