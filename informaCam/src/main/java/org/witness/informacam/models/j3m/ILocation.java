package org.witness.informacam.models.j3m;

import org.witness.informacam.models.Model;

public class ILocation extends Model {
	public long cellId;
	public float[] geoCoordinates;
	
	public ILocation(float[] geoCoordinates) {
		this.geoCoordinates = geoCoordinates;
	}
	
	public ILocation(double[] geoCoordinates) {
		this.geoCoordinates = new float[] {(float) geoCoordinates[0], (float) geoCoordinates[1]};
	}
	
	public ILocation(double[] geoCoordinates, long cellId) {
		this(geoCoordinates);
		
		this.cellId = cellId;
	}
	
	public ILocation(float[] geoCoordinates, long cellId) {
		this(geoCoordinates);
		
		this.cellId = cellId;
	}
}
