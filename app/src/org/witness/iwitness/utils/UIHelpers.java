package org.witness.iwitness.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;

public class UIHelpers
{
	public static void showSoftKeyboard(Activity activity, View view)
	{
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}

	public static void hideSoftKeyboard(Activity activity)
	{
		UIHelpers.hideSoftKeyboard(activity, null);
	}

	public static void hideSoftKeyboard(Activity activity, View view)
	{
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (view == null)
			view = activity.getCurrentFocus();
		if (view != null)
			inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
	public static int getRelativeLeft(View myView)
	{
		if (myView.getParent() == myView.getRootView())
			return myView.getLeft();
		else
			return myView.getLeft() + UIHelpers.getRelativeLeft((View) myView.getParent());
	}

	public static int getRelativeTop(View myView)
	{
		if (myView.getParent() == myView.getRootView())
			return myView.getTop();
		else
			return myView.getTop() + UIHelpers.getRelativeTop((View) myView.getParent());
	}

	/**
	 * Get the coordinates of a view relative to another anchor view. The anchor
	 * view is assumed to be in the same view tree as this view.
	 * 
	 * @param anchorView
	 *            View relative to which we are getting the coordinates
	 * @param view
	 *            The view to get the coordinates for
	 * @return A Rect containing the view bounds
	 */
	public static Rect getRectRelativeToView(View anchorView, View view)
	{
		Rect ret = new Rect(getRelativeLeft(view) - getRelativeLeft(anchorView), getRelativeTop(view) - getRelativeTop(anchorView), 0, 0);
		ret.right = ret.left + view.getWidth();
		ret.bottom = ret.top + view.getHeight();
		return ret;
	}
	
	
	/**
	 * Fade the alpha of the view to 0.
	 * 
	 * @param view
	 *            The view to fade
	 * @param duration
	 *            Number of ms for the animation
	 */
	public static void fadeOut(final View view, int duration)
	{
		AlphaAnimation alpha = new AlphaAnimation((duration == 0) ? 0 : 1.0f, 0);
		alpha.setDuration(duration);
		alpha.setFillAfter(true);
		alpha.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation animation)
			{
				view.setVisibility(View.GONE);
				view.clearAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationStart(Animation animation)
			{
			}
		});
		view.clearAnimation();
		view.startAnimation(alpha);
	}

	public static void fadeIn(final View view, final int duration)
	{
		AlphaAnimation alpha = new AlphaAnimation((duration == 0) ? 1 : 0, 1);
		alpha.setDuration(duration);
		alpha.setFillAfter(true);
		alpha.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation animation)
			{
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationStart(Animation animation)
			{
				view.setVisibility(View.VISIBLE);
			}
		});
		view.clearAnimation();
		view.startAnimation(alpha);
	}
}
