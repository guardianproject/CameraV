package org.witness.informacam.models.media;

import org.witness.informacam.models.Model;

import android.app.Activity;
import android.util.Log;
import android.view.Display;

public class IRegionBounds extends Model {
	public int top = 0;
	public int left = 0;
	public int width = 0;
	public int height = 0;
	public long startTime = -1L;
	public long endTime = 0L;
	
	public int displayTop = 0;
	public int displayLeft = 0;
	public int displayWidth = 0;
	public int displayHeight = 0;
	
	public IRegionBounds() {}
	
	public IRegionBounds(int top, int left, int width, int height) {
		this(top, left, width, height, -1L, -1L);
	}
	
	public IRegionBounds(int top, int left, int width, int height, long startTime, long endTime) {
		this.displayTop = top;
		this.displayLeft = left;
		this.displayWidth = width;
		this.displayHeight = height;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public long getDuration() {
		if(startTime != -1L) {
			return Math.abs(startTime - endTime);
		} else {
			return 0;
		}
	}
	

	@SuppressWarnings("deprecation")
	public int[] getWindowDimensions(Activity a) {
		if (a == null)
		{
			return new int[] { 0,0 };
		}
		else
		{
			Display display = a.getWindowManager().getDefaultDisplay();
			return new int[] {display.getWidth(),display.getHeight()};
		}
	}

	public void calculate(int[] specs, Activity a) {
		calculate (specs,getWindowDimensions(a));
	}
	
	public void calculate(int[] specs, int windowDimensions[]) {
		/*
		for(int i : specs) {
			Log.d(LOG, "specs (" + specs.length + "): " + i);
		}
		
		for(int i : windowDimensions) {
			Log.d(LOG, "windowDimensions (" + windowDimensions.length + "): " + i);
		}
		*/
		
		//original width/height; location in window (top, left); view width/height
		int originalDimensions[] = new int[] {specs[0], specs[1]};
		int locationInWindow[] = new int[] {specs[2], specs[3]};
		int viewDimensions[] = new int[] {specs[4], specs[5]};
		
		int adjustedDisplayLeft = displayLeft - locationInWindow[1];
		int adjustedDisplayTop = displayTop - locationInWindow[0];
		Log.d(LOG, String.format("adjustedDesplayLeft: %d\nadjustedDisplayTop: %d", adjustedDisplayLeft, adjustedDisplayTop));
		
		int adjustedDisplayWidth = displayWidth - locationInWindow[1];
		int adjustedDisplayHeight = displayHeight - locationInWindow[0];
		Log.d(LOG, String.format("adjustedDisplayWidth: %d\nadjustedDisplayHeight: %d", adjustedDisplayWidth, adjustedDisplayHeight));
		
		double widthRatio = originalDimensions[0]/viewDimensions[0];
		double heightRatio = originalDimensions[1]/viewDimensions[1];
		//Log.d(LOG, String.format("widthRatio: %f\nheightRatio: %f", widthRatio, heightRatio));
		
		top = adjustedDisplayTop >= 0 ? (int) (adjustedDisplayTop * heightRatio) : 0;
		left = adjustedDisplayLeft >= 0 ? (int) (adjustedDisplayLeft * widthRatio) : 0;
		width = (int) (adjustedDisplayWidth * widthRatio);
		height = (int) (adjustedDisplayHeight * heightRatio);
		
		/*
		int paddingHorizontal = (int) Math.abs(viewDimensions[0] - windowDimensions[0])/2;
		int paddingVertical = (int) Math.abs(viewDimensions[1] - windowDimensions[1])/2;
		
		// treat displayTop/left as at these coords instead (because image might be padded)
		int adjustedDisplayLeft = displayWidth - paddingHorizontal;
		int adjustedDisplayTop = displayHeight - paddingVertical;
		
		// multiply by ratio?
		double widthRatio = originalDimensions[0]/viewDimensions[0];
		double heightRatio = originalDimensions[1]/viewDimensions[1];
		
		top = (int) (adjustedDisplayTop * heightRatio);
		left = (int) (adjustedDisplayLeft * widthRatio);
		width = (int) (displayWidth * widthRatio);
		height = (int) (displayHeight * heightRatio);
		*/
	}
}
