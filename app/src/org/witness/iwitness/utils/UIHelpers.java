package org.witness.iwitness.utils;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

public class UIHelpers
{
	public static void hideSoftKeyboard(Activity activity)
	{
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
	}
}
