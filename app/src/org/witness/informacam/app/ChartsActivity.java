package org.witness.informacam.app;

import java.util.ArrayList;

import org.witness.informacam.InformaCam;
import org.witness.informacam.app.utils.Constants.Codes;
import org.witness.informacam.models.j3m.ISensorCapture;
import org.witness.informacam.models.media.IMedia;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;


public class ChartsActivity extends Activity {

	private InformaCam informaCam;
	private LineChart mChart;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_charts);

		informaCam = (InformaCam) getApplication();

		initChart();
		
	
		initData();
	}

	
	@Override
	protected void onResume() {
		
		super.onResume();
		
		
	}
	
	private void initData ()
	{
		
		try
		{
			IMedia media = informaCam.mediaManifest.getById(getIntent().getStringExtra(Codes.Extras.EDIT_MEDIA));
			String j3m = ((IMedia) media).buildJ3M(this, false, null);
	
			String sensorType = "lightMeterValue";
			 ArrayList<String> xVals = new ArrayList<String>();
			 ArrayList<Entry> yVals = new ArrayList<Entry>();
			 
			 int i = 0;
			 
			for (ISensorCapture sensor : media.data.sensorCapture)
			{
				xVals.add(sensor.timestamp+"");
				
				if (sensor.sensorPlayback.has(sensorType))
				{
					Object val = sensor.sensorPlayback.get(sensorType);
					
					if (val instanceof Integer)
						yVals.add(new Entry(((Integer)val).intValue(), i++));
					else if (val instanceof Double)
						yVals.add(new Entry(((Double)val).floatValue(), i++));
					
				}
			}
			
			addLineData(sensorType, xVals, yVals);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addLineData (String dataset, ArrayList<String> xVals, ArrayList<Entry> yVals)
	{
		
			/**
	        for (int i = 0; i < count; i++) {

	            float mult = (range + 1);
	            float val = (float) (Math.random() * mult) + 3;// + (float)
	                                                           // ((mult *
	                                                           // 0.1) / 10);
	            yVals.add(new Entry(val, i));
	        }
			*/
		
	        // create a dataset and give it a type
	        LineDataSet set1 = new LineDataSet(yVals, dataset);
	        // set1.setFillAlpha(110);
	        // set1.setFillColor(Color.RED);

	        // set the line to be drawn like this "- - - - - -"
	        set1.enableDashedLine(10f, 5f, 0f);
	        set1.setColor(Color.BLACK);
	        set1.setCircleColor(Color.BLACK);
	        set1.setLineWidth(1f);
	        set1.setCircleSize(3f);
	        set1.setDrawCircleHole(false);
	        set1.setValueTextSize(9f);
	        set1.setFillAlpha(65);
	        set1.setFillColor(Color.BLACK);
//	        set1.setDrawFilled(true);
	        // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
	        // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

	        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
	        dataSets.add(set1); // add the datasets

	        // create a data object with the datasets
	        LineData data = new LineData(xVals, dataSets);

	        // set data
	        mChart.setData(data);
	}
	
	private void initChart ()
	{
		mChart = (LineChart) findViewById(R.id.chart1);
        //mChart.setOnChartGestureListener(this);
        //mChart.setOnChartValueSelectedListener(this);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable value highlighting
        mChart.setHighlightEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
	}

}
