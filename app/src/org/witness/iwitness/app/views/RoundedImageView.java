package org.witness.iwitness.app.views;

import org.witness.iwitness.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class RoundedImageView extends ImageView {

	private int mRadius;
	private Bitmap mBitmap;

	public RoundedImageView(Context context) {
		super(context);
		init(null);
	}

	public RoundedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	@SuppressLint("NewApi")
	private void init(AttributeSet attrs)
	{	
		if (attrs != null)
		{
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RoundedImageView);
			mRadius = a.getInt(R.styleable.RoundedImageView_rounding_radius, 25);
			a.recycle();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (oldw != w || oldh != h)
		{
			createRoundedBitmap();
		}
	}

	private void createRoundedBitmap()
	{
		if (mBitmap == null)
			return;
		
	    int targetWidth = getWidth();
	    int targetHeight = getHeight();
	    if (targetWidth == 0 || targetHeight == 0)
	    	return;
	    
	    Bitmap targetBitmap = Bitmap.createBitmap(
	        targetWidth,
	        targetHeight,
	        Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(targetBitmap);
	    
	    Path path = new Path();
	    RectF rect = new RectF(0, 0, targetWidth, targetHeight);
	    path.addRoundRect(rect, mRadius, mRadius, Path.Direction.CW);
	    canvas.clipPath(path);

	    canvas.drawBitmap(
	    		mBitmap,
	        new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()),
	        new Rect(0, 0, targetWidth, targetHeight),
	        null);
	    super.setImageBitmap(targetBitmap);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		mBitmap = bm;
		createRoundedBitmap();
	}
}
