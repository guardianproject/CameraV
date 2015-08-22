package org.witness.informacam.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.witness.informacam.utils.Constants.Time.DateFormats;

import android.annotation.SuppressLint;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class TimeUtility {
	
	public final static String getTimezone() {
		Calendar cal = Calendar.getInstance();
		TimeZone tz = cal.getTimeZone();
		
		return tz.getID();
	}
	
	public final static long timestampToMillis(String ts, String dateFormat) throws ParseException {
		//2012:06:12 10:42:04
		if(dateFormat == null)
			dateFormat = DateFormats.EXIF_DATE_FORMAT;
		
		try {
			DateFormat df = new SimpleDateFormat(dateFormat, Locale.getDefault());
		
			Date d = (Date) df.parse(ts);
			return d.getTime();
		} catch(ParseException e) {
			Log.e(Constants.Time.LOG, e.toString());
			e.printStackTrace();
		} catch(NullPointerException e) {
			Log.e(Constants.Time.LOG, e.toString());
			e.printStackTrace();
		}
		return Long.parseLong(ts);
	}
	
	/*
	public final static long resolveTimestampWithGPSTime(String timestampString) {
		long assumedTimestamp = 0L;
		try {
			assumedTimestamp= Long.parseLong(timestampString);
		} catch(NumberFormatException e) {
			try {
				assumedTimestamp = timestampToMillis(timestampString);
			} catch (ParseException e1) {
				Log.e(Constants.Time.LOG, e1.toString());
				e1.printStackTrace();
			}
		}
		
		long[] relativeTimestamps = InformaService.getInstance().getInitialGPSTimestamp();
		
		long difference = relativeTimestamps[0] - relativeTimestamps[1];
		
		Log.d(Constants.Time.LOG, "timestamp reported: " + assumedTimestamp);
		Log.d(Constants.Time.LOG, "rel timestamps: " + relativeTimestamps[0] + ", " + relativeTimestamps[1] + "(offset: " + difference + ")");
		Log.d(Constants.Time.LOG, "real timestamp?: " + (assumedTimestamp + difference));
		
		return (assumedTimestamp + difference);
	}
	*/
	
	public final static long timestampToMillis(String ts) throws ParseException {
		return timestampToMillis(ts, null);
	}
	
	public final static String millisecondsToTimestamp(long ms, long max) {
		return millisecondsToTimestamp(Math.min(ms, max));
	}
	
	@SuppressLint("SimpleDateFormat")
	public final static String millisecondsToDatestamp(long ms) {
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		Date d = new Date(ms);
		return dateFormat.format(d);
	}
	
	@SuppressLint("SimpleDateFormat")
	public final static String millisecondsToDayOnly(long ms) {
		DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
		Date d = new Date(ms);
		return dateFormat.format(d);
	}
	
	public final static long minusOneDay(long ms) {
		return ms - (24 * 60 * 60 * 1000);
	}
	
	public final static long plusOneDay(long ms) {
		return ms + (24 * 60 * 60 * 1000);
	}
	
	public final static int millisecondsToHours(long ms) {
		return (int) (((ms/1000)/60)/60);
	}
	
	@SuppressLint("SimpleDateFormat")
	public final static String[] millisecondsToDatestampAndTimestamp(long ms) {
		DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
		DateFormat timeFormat = new SimpleDateFormat("hh:mm a");
		
		Date d = new Date(ms);
		return new String[] {dateFormat.format(d), timeFormat.format(d)};
	}
	
	public final static String millisecondsToStopwatchTime(long ms) {
		DateFormat timeFormat = new SimpleDateFormat("hh:mm");
		
		Date d = new Date(ms);
		return timeFormat.format(d);
	}
	
	public final static String millisecondsToTimestamp(long ms) {
		int s = (int) (ms/1000);
		int hours = s/3600;
		int remainder = s%3600;
		int min = remainder/60;
		int sec = remainder%60;
		
		String ts = ((hours < 10 ? "0" : "") + hours + ":" + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec);
		if(ts.contains("-"))
			ts = ts.replace("-","0.");
		return ts;
	}

	public static boolean matchesDay(long t1, long t2) {
		//Log.d(LOG, "COMPARING " + millisecondsToDayOnly(t1) + " to " + millisecondsToDayOnly(t2));
		return millisecondsToDayOnly(t1).equals(millisecondsToDayOnly(t2));
	}
	
	public static boolean matchesWeek(long t1, long t2) {
		// TODO
		return false;
	}
}
