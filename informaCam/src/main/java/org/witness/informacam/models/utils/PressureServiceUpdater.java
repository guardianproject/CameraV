package org.witness.informacam.models.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import org.witness.informacam.json.JSONObject;

import android.hardware.SensorManager;
import android.util.Log;

public class PressureServiceUpdater {

	 
	  
	  public static float GetRefPressure(double longitude, double latitude) {
	    InputStream is = null;
	    float pressure = 0.0f;
	    try {
	    	
	    String strUrl = String.format(Locale.US, "http://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f",latitude, longitude);
	    	
	    Log.d("PressureLookup","url: " + strUrl);
	    
	      URL text = new URL(strUrl);
	      URLConnection connection = text.openConnection();
	      connection.setReadTimeout(30000);
	      connection.setConnectTimeout(30000);

	      is = connection.getInputStream();
	      
	      ByteArrayOutputStream baos = new ByteArrayOutputStream();
	      int b = -1;
	      while ((b = is.read())!=-1)
	    	  baos.write(b);
	      
	      String strJson = new String(baos.toByteArray());
	      
	      JSONObject json = new JSONObject(strJson);
	      json = json.getJSONObject("main");
	      
	      pressure = (float)json.getDouble("pressure");
	    //  pressure = (float)json.getDouble("sea_level");
	      
	      /**
	       * {
	       * "coord":{"lon":139,"lat":35},
	       * "sys":{"message":0.1094,"country":"JP","sunrise":1400614568,"sunset":1400665503},
	       * "weather":[{"id":501,"main":"Rain","description":"moderate rain","icon":"10d"}],
	       * "base":"cmc stations",
	       * "main":{"temp":289.125,"temp_min":289.125,"temp_max":289.125,"pressure":967.62,"sea_level":1009.99,"grnd_level":967.62,"humidity":100},"wind":{"speed":2.71,"deg":355.502},"rain":{"3h":4.25},"clouds":{"all":92},"dt":1400640069,"id":1851632,"name":"Shuzenji","cod":200}
	       * 
	       */
	      

	    } catch (Exception e) {
	      Log.e("Pressure", "Error in network call", e);
	      pressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
	    } finally {
	      try {
	        if(is!=null)
	          is.close(); 
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	    }
	    return pressure;
	  }
	  
	  
}