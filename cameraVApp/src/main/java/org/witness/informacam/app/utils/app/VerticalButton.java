package org.witness.informacam.app.utils.app;

import org.witness.informacam.app.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;

public class VerticalButton extends Button {

	public VerticalButton(Context context, AttributeSet attrs){
		super(context, attrs);
		final int gravity = getGravity();
		if(Gravity.isVertical(gravity) && (gravity&Gravity.VERTICAL_GRAVITY_MASK) == Gravity.BOTTOM) {
			setGravity((gravity&Gravity.HORIZONTAL_GRAVITY_MASK) | Gravity.TOP);
		}
		
		setPadding(0, 20, 0, 0);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VerticalButton);
		CharSequence s = a.getString(R.styleable.VerticalButton_android_text);
		
		if(s != null) setText(s.toString());
		
		a.recycle();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	@Override
	protected void onDraw(Canvas canvas){
		TextPaint textPaint = getPaint(); 
		textPaint.setColor(Color.WHITE);
		textPaint.drawableState = getDrawableState();
		
		canvas.save();
		canvas.translate(0, getHeight());
		canvas.rotate(-90);
		canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());

		getLayout().draw(canvas);
		canvas.restore();
	}
}