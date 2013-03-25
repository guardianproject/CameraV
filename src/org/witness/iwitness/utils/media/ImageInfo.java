package org.witness.iwitness.utils.media;

import org.witness.iwitness.utils.Constants.Codes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageInfo {

	public static int getOrientation(Bitmap b) {
		return b.getWidth() > b.getHeight() ? Codes.Media.ORIENTATION_LANDSCAPE : Codes.Media.ORIENTATION_PORTRAIT;
	}
	
	public static Bitmap drawableToBitmap(Drawable d) {
		if(d instanceof BitmapDrawable) {
			return ((BitmapDrawable) d).getBitmap();
		}
		
		int w = d.getIntrinsicWidth();
		w = w > 0 ? w : 1;
		
		int h = d.getIntrinsicHeight();
		h = h > 0 ? h : 1;
		
		Bitmap b = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas c = new Canvas(b);
		d.setBounds(0, 0, c.getWidth(), c.getHeight());
		d.draw(c);
		
		return b;
	}
}
