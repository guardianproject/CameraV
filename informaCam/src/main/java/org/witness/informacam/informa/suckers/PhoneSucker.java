package org.witness.informacam.informa.suckers;

import java.util.TimerTask;

import org.witness.informacam.informa.SensorLogger;
import org.witness.informacam.json.JSONArray;
import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.models.j3m.ILogPack;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Suckers;
import org.witness.informacam.utils.Constants.Suckers.Phone;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

@SuppressWarnings("rawtypes")
public class PhoneSucker extends SensorLogger {
	TelephonyManager tm;
	BluetoothAdapter ba;
	WifiManager wm;
	
	boolean hasBluetooth = false;
	boolean hasWifi;
	boolean wifiWasOn = false;
	boolean hasTele = false;
	
	private final static String LOG = Suckers.LOG;
	
	@SuppressWarnings("unchecked")
	public PhoneSucker(Context context) {
		super (context);
		setSucker(this);
				
		tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		ba = BluetoothAdapter.getDefaultAdapter();
		wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		
		if (tm != null)
		{
			if (tm.getSimState() == TelephonyManager.SIM_STATE_READY)
			{
				hasTele = true;
			}
		}
		
		if(ba != null && ba.isEnabled()) //only use bluetooth if the user has it on
		{
			hasBluetooth = true;
	
		}
		else
			Log.d(LOG,"no bt?");
		
		if(wm != null && wm.isWifiEnabled()) { //only use wifi if it is on
			// is wifi on?
			
			hasWifi = true;
			wifiWasOn = true;
		
			
		}
		
		setTask(new TimerTask() {
			
			@Override
			public void run() {
				if(getIsRunning()) {
					try {
						
						if (hasTele)
						{
							ILogPack logPack = new ILogPack(Phone.Keys.CELL_ID, getCellId());
							logPack.put(Phone.Keys.LAC, getLAC());
							logPack.put(Phone.Keys.MCC, getNetworkOperator());
							sendToBuffer(logPack);
							
						}
						
						// find other bluetooth devices around
						if(hasBluetooth && !ba.isDiscovering())
							ba.startDiscovery();
						
						// scan for network ssids
						if(hasWifi && !wm.startScan()) {
							// TODO: alert user to this error
							
						}
							
						
					} catch(NullPointerException e) {}
					catch (JSONException e) {
						Logger.e(LOG, e);
					}
				}
			}
		});
		
		getTimer().schedule(getTask(), 0, Phone.LOG_RATE);
	}
	
	private String getLAC() {
		try {
			if(tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
				GsmCellLocation gLoc = (GsmCellLocation) tm.getCellLocation();
				if(gLoc != null) {
					return Integer.toString(gLoc.getLac());
				}
			}						
		} catch(NullPointerException e) {
			Logger.e(LOG, e);
		}
		
		return null;
	}
	
	private String getNetworkOperator() {
		try {			
			
			return tm.getNetworkOperator();
		} catch(NullPointerException e) {
			Logger.e(LOG, e);
		}
		
		return null;
	}
	
	
	private String getCellId() {	
		try {			
			String out = "";
			if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
				final GsmCellLocation gLoc = (GsmCellLocation) tm.getCellLocation();
				out = Integer.toString(gLoc.getCid());
			} else if(tm.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
				final CdmaCellLocation cLoc = (CdmaCellLocation) tm.getCellLocation();
				out = Integer.toString(cLoc.getBaseStationId());
			}
			return out;
		} catch(NullPointerException e) {
			return null;
		}
	}
	
	
	public JSONArray getWifiNetworks() {
		JSONArray wifi = new JSONArray();
		
		if (hasWifi)
		{
			for(ScanResult wc : wm.getScanResults()) {
				JSONObject scanResult = new JSONObject();
				try {				
					scanResult.put(Phone.Keys.WIFI_FREQ, wc.frequency);
					scanResult.put(Phone.Keys.WIFI_LEVEL, wc.level);
					scanResult.put(Phone.Keys.BSSID, wc.BSSID);
					scanResult.put(Phone.Keys.SSID, wc.SSID);
					wifi.put(scanResult);
				} catch (JSONException e) {
					Log.e(LOG, e.toString(),e);
					continue;
				}
				
			}
		}
		
		return wifi;
	}
		
	public ILogPack forceReturn() throws JSONException {
		// TODO: anonymize this value
		ILogPack fr = new ILogPack();
		if(ba != null && hasBluetooth) {
			fr.put(Phone.Keys.BLUETOOTH_DEVICE_ADDRESS, ba.getAddress());
			fr.put(Phone.Keys.BLUETOOTH_DEVICE_NAME, ba.getName());
		}
		
		String cId = getCellId() ;
		if(cId != null) {
			fr.put(Phone.Keys.CELL_ID, cId);
		}
		
		String lac = getLAC();
		if(lac != null) {
			fr.put(Phone.Keys.LAC, lac);
		}
		
		String mcc = getNetworkOperator();
		if(mcc != null) {
			fr.put(Phone.Keys.MCC, mcc);
		}
		
		
		return fr;
	}
	
	public void stopUpdates() {
		setIsRunning(false);
		if(hasBluetooth && ba.isDiscovering()) {
			ba.cancelDiscovery();
			
			//ba.disable(); //leave bluetooth on
			
		}
		
		/*
		if(hasWifi && !wifiWasOn) {
			wm.setWifiEnabled(false);
		}
		*/
		
		
		
		Log.d(LOG, "shutting down PhoneSucker...");
	}

}