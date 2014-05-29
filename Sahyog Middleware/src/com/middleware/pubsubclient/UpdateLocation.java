package com.middleware.pubsubclient;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;

public class UpdateLocation {

	static XMPPConnection conn;
	static boolean updated;
	public static Double lat,lon;
	static JSONArray locArray;

	public static boolean updateLocation(Context appContext, final ConnectivityManager cm, JSONObject sensors)
	{
		try {
			JSONArray location = (JSONArray) sensors.get("Location");
			lat =location.getDouble(0);
			lon=location.getDouble(1);
			locArray=new JSONArray();
			
			System.out.println("Current location is"+ "," + lat.toString() + "," + lon.toString());
			
			LocationLibrary.initialiseLibrary(appContext, 60* 15 * 1000, 60 * 60 * 1000, "com.middleware.pubsubclient");
			
			final IntentFilter lftIntentFilter = new IntentFilter(LocationLibraryConstants.getLocationChangedPeriodicBroadcastAction());
	        appContext.registerReceiver(lftBroadcastReceiver, lftIntentFilter);
	        if (isNetworkAvailable(cm) && conn.isConnected())
			{
	        	sensors.put("Location",locArray);
				Message topics = new Message("server@" + RegisterDevice.serverIP,
						Message.Type.normal);
				topics.setSubject("updateSubscription");
				//sensors.put("Location", locArray);
				topics.setBody(sensors.toString());

				conn.sendPacket(topics);
				System.out.println("Sent");
				//sent = true;
			}

//		return sent;

		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;		
	}
	
	private final static BroadcastReceiver lftBroadcastReceiver = new BroadcastReceiver() {
        
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			final LocationInfo locationInfo = (LocationInfo) intent.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
			try {
				locArray.put((double)locationInfo.lastLat);
				locArray.put((double)locationInfo.lastLong);
				locArray.put(0);
				locArray.put(0);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    };
	
    private static boolean isNetworkAvailable(ConnectivityManager cm)
	{
		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
		if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
			{
				return true;
			}
		else
			{
				return false;
			}
	}


}
