package org.witness.informacam.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.witness.informacam.InformaCam;
import org.witness.informacam.app.utils.Constants.Codes;
import org.witness.informacam.json.JSONArray;
import org.witness.informacam.models.j3m.ISensorCapture;
import org.witness.informacam.models.media.IMedia;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ValueFormatter;


public class ChartsActivity extends Activity {

	private InformaCam informaCam;
	private LinearLayout viewChartGroup;
	private IMedia media;
	private Bitmap bmMap;
	private ArrayList<LineChart> listCharts = new ArrayList<LineChart>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("prefBlockScreenshots", false))
		{
	  		getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
	  				WindowManager.LayoutParams.FLAG_SECURE);      
		}
		
		setContentView(R.layout.activity_charts);

		viewChartGroup = (LinearLayout)findViewById(R.id.chartGroup);
		viewChartGroup.setVisibility(View.GONE);
		viewChartGroup.setDrawingCacheEnabled(true);
		
		informaCam = (InformaCam) getApplication();

		media = informaCam.mediaManifest.getById(getIntent().getStringExtra(Codes.Extras.EDIT_MEDIA));

		setTitle(media._id);
		
		Utils.init(getResources());
		
		initCharts();

		viewChartGroup.setVisibility(View.VISIBLE);   
        
	}
	
	private void addMap (ArrayList<String> alPoints)
	{
		String baseMap = "https://maps.googleapis.com/maps/api/staticmap?size=600x400";
		String basePath = "&path=color:0x0000ff|weight:5";
		
		StringBuffer mapUrl = new StringBuffer();
		mapUrl.append(baseMap);
		mapUrl.append(basePath);
		
		int max = 20;
		
		for (int i = 0; i < alPoints.size()&& i < max; i++)
		{
			mapUrl.append('|');
			mapUrl.append(alPoints.get(i));
		}
		
		ImageView iv = new ImageView(this);
		
		int dpHeight = 300;	       
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpHeight, getResources().getDisplayMetrics());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, height);             
        viewChartGroup.addView(iv,params);
		
		new DownloadImageTask(iv).execute(mapUrl.toString());	
	}	
	
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	    ImageView bmImage;

	    public DownloadImageTask(ImageView bmImage) {
	        this.bmImage = bmImage;
	    }

	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        
	        Log.d("Map","getting map: " + urldisplay);
	        
	        try {
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            bmMap = BitmapFactory.decodeStream(in);
	        } catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
	        return bmMap;
	    }

	    protected void onPostExecute(Bitmap result) {
	        bmImage.setImageBitmap(result);
	    }    
	}

	private void initCharts ()
	{
		

		try
		{

			((IMedia) media).buildJ3M(this, false, null);
			
			ArrayList<ISensorCapture> listSensorEvents = new ArrayList<ISensorCapture>(media.data.sensorCapture);
			
			Collections.sort(listSensorEvents,new Comparator<ISensorCapture>()
			{
	
				@Override
				public int compare(ISensorCapture lhs, ISensorCapture rhs) {
					
					if (lhs.timestamp < rhs.timestamp)
						return -1;
					else if (lhs.timestamp == rhs.timestamp)
						return 0;
					else
						return 1;
				}
				
			});
			
			DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);				


			ArrayList<String> alPoints = new ArrayList<String>();
			
			//do map
			for (ISensorCapture sensor : listSensorEvents)
			{				
				if (sensor.sensorPlayback.has("gps_coords"))
				{										
					String latLon = (String)sensor.sensorPlayback.get("gps_coords");
					alPoints.add(latLon.substring(1,latLon.length()-1));
				}
			}
			
			if (alPoints.size() > 0)
				addMap(alPoints);
			
			//do charts
			final String[] sensorLabels = {getString(R.string.gps_accuracy),getString(R.string.gps_speed),getString(R.string.gps_altitude),getString(R.string.light),getString(R.string.air_pressure),getString(R.string.orientation),getString(R.string.motion),getString(R.string.wifi_networks)};
			final String[][] sensorTypes = {{"gps_accuracy"},{"gps_speed"},{"gps_altitude"},{"lightMeterValue"},{"pressureHPAOrMBAR"},{"pitch","roll","azimuth"},{"acc_x","acc_y","acc_z"},{"visibleWifiNetworks"}};
			
			int labelIdx = 0;
			
			for (String[] sensorTypeSet : sensorTypes)
			{
							
			    ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
			    
			    int[] colors = ColorTemplate.JOYFUL_COLORS;
			    int colorIdx = 0;

				ArrayList<String> xVals = null;
				
				for (String sensorType : sensorTypeSet)
				{
					
					xVals = new ArrayList<String>();	//only the last time through will set the values
					
					 int i = 0;
					 ArrayList<Entry> yVals = new ArrayList<Entry>();
					 long lastTimeStamp = -1;
					 
					for (ISensorCapture sensor : listSensorEvents)
					{
						
						if (sensor.sensorPlayback.has(sensorType))
						{																			
							Object val = sensor.sensorPlayback.get(sensorType);			
							lastTimeStamp = sensor.timestamp;

							xVals.add(dateFormat.format(new Date(sensor.timestamp)));	
							
							if (val instanceof Integer)				
							{		
								yVals.add(new Entry(((Integer)val).intValue(), i++));
							}
							else if (val instanceof Double)							
							{
	
								yVals.add(new Entry(((Double)val).floatValue(), i++));	
							}
							else if (val instanceof Float)		
							{
								yVals.add(new Entry(((Float)val).floatValue(), i++));			
							}
							else if (val instanceof JSONArray)
							{
								yVals.add(new Entry(((JSONArray)val).length(), i++));
							}
							else
							{
								try
								{

									float fval = Float.parseFloat(((String)val));	
									yVals.add(new Entry(fval, i++));	
									
								}
								catch (Exception e)
								{
									//couldn't parse double
									Log.w("Chart","couldn't parse value: " + val,e);
								}
							}
						}
					}

					if (!yVals.isEmpty())
					{	
						if (yVals.size()==1)
						{
							xVals.add(dateFormat.format(new Date(lastTimeStamp+1)));
							Entry entry = yVals.get(0).copy();
							entry.setXIndex(entry.getXIndex()+1);
							yVals.add(entry);
						}
						
						LineDataSet dataSet = addLineDataSet(sensorType, yVals);
						dataSet.setColor(colors[colorIdx++]);
				        dataSets.add(dataSet); // add the datasets	
					}
				}
				
				if (!dataSets.isEmpty())
				{
			        // create a data object with the datasets
			        LineData data = new LineData(xVals, dataSets);		
			        LineChart chart = addChart(sensorLabels[labelIdx],data);
			        
			        
			        /*
			        LimitLine limitCapture = new LimitLine(media.dcimEntry.timeCaptured, "Capture");
			        limitCapture.setLineColor(Color.RED);
			        limitCapture.setLineWidth(4f);
			        limitCapture.enableDashedLine(10f, 10f, 0f);		        
			        limitCapture.setTextSize(10f);		        
			        chart.getXAxis().addLimitLine(limitCapture);
			        */
			        
			        listCharts.add(chart);
				}
				
				labelIdx++;
				
			}
			

			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private LineDataSet addLineDataSet (String dataset, ArrayList<Entry> yVals)
	{
	        // create a dataset and give it a type
	        LineDataSet set1 = new LineDataSet(yVals, dataset);

	        // set the line to be drawn like this "- - - - - -"
//	        set1.enableDashedLine(10f, 5f, 0f);
	        
	        set1.setLineWidth(3f);
	        set1.setCircleSize(0f);
	        //set1.setCircleSize(5f);
	        //set1.setDrawCircleHole(false);
	        //set1.setValueTextSize(9f);
	        //set1.setFillAlpha(65);

	        return set1;
	}
	
	private LineChart addChart (String label, LineData data)
	{
		LineChart chart = new LineChart(this);
        //chart.setOnChartGestureListener(this);
        //chart.setOnChartValueSelectedListener(this);

		chart.getAxisLeft().setStartAtZero(false);
		
        // no description text
        chart.setDescription("");
        chart.setNoDataTextDescription("");

        // enable value highlighting
        chart.setHighlightEnabled(true);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        // chart.setScaleXEnabled(true);
        // chart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        // set data
        chart.setData(data);        
       
        TextView tv = new TextView (this);
        tv.setText(label);        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);     
        
        viewChartGroup.addView(tv,params);
        
        int dpHeight = 300;
       
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpHeight, getResources().getDisplayMetrics());

        params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, height);     
        
        int dpMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        params.setMargins(dpMargin,dpMargin,dpMargin,dpMargin);
        
        chart.setLayoutParams(params);              
        
        viewChartGroup.addView(chart,params);
        
        return chart;
	}
	
	public File saveBitmap(Bitmap b, String name) throws IOException {
			    
	    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	    
	    if (!path.canWrite())
	    {
	    	path = getExternalFilesDir(null);
	    	
	    	if (!path.canWrite())
	    		path = getFilesDir();
	    }
	    
	    File imageFile = new File(path, name+ ".jpg");
	    FileOutputStream fileOutPutStream = new FileOutputStream(imageFile);
	    b.compress(Bitmap.CompressFormat.JPEG, 80, fileOutPutStream);
	    fileOutPutStream.flush();
	    fileOutPutStream.close();
	    
	    imageFile.setReadable(true, false);
	    
	    return imageFile;
	}

	private void shareGraphic ()
	{
		try {
			
			ArrayList<Bitmap> listBitmap = new ArrayList<Bitmap>();
			
			int chartWidth = listCharts.get(0).getWidth();
			
			listBitmap.add(media.getThumbnail(chartWidth));
			listBitmap.add(bmMap);
			
			for (LineChart chart : listCharts)
			{
				Bitmap b = chart.getChartBitmap();
				listBitmap.add(b);
			}
			
			Bitmap bmOut = combineImageIntoOne(listBitmap);
			File fileImage = saveBitmap(bmOut,media._id);
			
			String summary = media.buildSummary(this, null);
			
			Intent intent = new Intent(Intent.ACTION_SEND);
			Uri uriData = Uri.fromFile(fileImage);
			intent.setData(uriData);
			intent.putExtra(Intent.EXTRA_TITLE,media._id);
			intent.putExtra(Intent.EXTRA_TEXT, summary);
			intent.putExtra(Intent.EXTRA_STREAM, uriData);
			intent.setType("image/jpeg");
			startActivity(intent);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Bitmap combineImageIntoOne(ArrayList<Bitmap> bitmap) {
        int w = 0, h = 0;
        for (int i = 0; i < bitmap.size()-1; i++) {
			if (bitmap.get(i)!=null) {
				if (i < bitmap.size() - 1) {
					w = bitmap.get(i).getWidth() > bitmap.get(i + 1).getWidth() ? bitmap.get(i).getWidth() : bitmap.get(i + 1).getWidth();
				}
				h += bitmap.get(i).getHeight();
			}
        }

        Bitmap temp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp);
        int top = 0;
        for (int i = 0; i < bitmap.size(); i++) {
			if (bitmap.get(i)!=null) {
				Rect dest = new Rect(0, top, w, top + bitmap.get(i).getHeight());
				canvas.drawBitmap(bitmap.get(i), null, dest, null);
				top += bitmap.get(i).getHeight();
			}
        }
        return temp;
}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_charts, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		
		switch (item.getItemId())
		{
			case R.id.menu_share:
			{		
				shareGraphic();
				
				return true;
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	 private Bitmap loadMediaBitmap () throws IOException
	 {
		Bitmap result;
		
		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inJustDecodeBounds = true;
		bfo.inPreferredConfig = Bitmap.Config.RGB_565;
		
		InputStream is = InformaCam.getInstance().ioService.getStream(media.dcimEntry.fileAsset.path, media.dcimEntry.fileAsset.source);
		
		if (is == null)
			return null;
		
		BitmapFactory.decodeStream(is, null, bfo);
		is.close();
			
		bfo.inSampleSize = 4;
		bfo.inJustDecodeBounds = false;

		is = InformaCam.getInstance().ioService.getStream(media.dcimEntry.fileAsset.path, media.dcimEntry.fileAsset.source);
		result = BitmapFactory.decodeStream(is, null, bfo);
		is.close();

		if (media.dcimEntry.exif.orientation == ExifInterface.ORIENTATION_ROTATE_90) {
		//	Log.d(LOG, "Rotating Bitmap 90");
			Matrix rotateMatrix = new Matrix();
			rotateMatrix.postRotate(90);
			result = Bitmap.createBitmap(result,0,0,result.getWidth(),result.getHeight(),rotateMatrix,false);
		} else if (media.dcimEntry.exif.orientation == ExifInterface.ORIENTATION_ROTATE_270) {

			Matrix rotateMatrix = new Matrix();
			rotateMatrix.postRotate(270);
			result = Bitmap.createBitmap(result,0,0,result.getWidth(),result.getHeight(),rotateMatrix,false);
		}

		return result;
	}

}
