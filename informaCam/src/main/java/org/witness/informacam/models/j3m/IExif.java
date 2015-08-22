package org.witness.informacam.models.j3m;

import java.io.Serializable;

import org.witness.informacam.models.Model;

public class IExif extends Model implements Serializable {
	private static final long serialVersionUID = 3982862461387724113L;
	
	public String aperture = null;
	public String make = null;
	public String model = null;
	public String timestamp = null;
	public String exposure = null;
	public String iso = null;
	
	public int width = 0;
	public int height = 0;
	public int flash = 0;
	public int focalLength = 0;
	public int orientation = 0;
	public int whiteBalance = 0;
	
	public long duration = 0L;
	public float[] location = new float[] {0.0f, 0.0f};
	
	public IExif() {
		super();
	}
	
	public IExif(IExif exif) throws InstantiationException, IllegalAccessException {
		super();
		inflate(exif);
	}

}
