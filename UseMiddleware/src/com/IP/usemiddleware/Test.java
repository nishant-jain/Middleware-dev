package com.IP.usemiddleware;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.middleware.pubsubclient.DeleteDeviceAccount;
import com.middleware.pubsubclient.RegisterDevice;
import com.middleware.pubsubclient.RequestData;
import com.middleware.pubsubclient.RequestListener;
import com.middleware.pubsubclient.UpdateAllowedTopics;

public class Test extends Activity
	{
		SensorManager sm = null;
		ConnectivityManager cm = null;
		TelephonyManager tm = null;
		LocationManager lm = null;
		JSONObject sensors = null;
		Boolean updated = null;
		TextView tv;

		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.activity_test);
				tv = (TextView) findViewById(R.id.tv1);
			}

		public void register(View v)
			{

				sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				RegisterDevice.register(getApplicationContext(), sm, cm, tm);
				ComponentName k = startService(new Intent(this, RequestListener.class));
				sensors = RegisterDevice.getAllPoosibleTopics();
				tv.setText("Logged in.\nRequestListener Started.");
			}

		public void delete(View v)
			{
				DeleteDeviceAccount.delete(getApplicationContext());
			}

		public void stopRequestListener(View v)
			{
				stopService(new Intent(this, RequestListener.class));
			}

		public void update(View v)
			{

				// Modify JSONObject sensors and then send it
				updated = UpdateAllowedTopics.update(cm, sensors);
			}

		public void publish(View v)
			{
				// Date = dd-MM-yyyy, Time =
				// HH:mm:ss, sensors = lowercase (accelerometer, gps,
				// microphone),
				// activity = "Don't Care", "On Foot", "In Vehicle" or
				// "On Bicycle"

				lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				String fromDate = "21-05-2014";
				String fromTime = "17:12:30";
				String toDate = "21-05-2014";
				String toTime = "17:12:45";
				String expiryDate = "22-05-2014";
				String expiryTime = "00:00:00";
				Double lat = 0.0;
				Double lon = 0.0;
				String activity = "Don't Care";
				int delay = 0;
				int countMin = 1;
				int countMax = 1;
				int gpsDelay = 0;
				ArrayList<String> sensors = new ArrayList<String>();
				sensors.add("accelerometer");
				sensors.add("activity");
				// sensors.add("microphone");
				try
					{
						RequestData.publishQuery(fromDate, fromTime, toDate, toTime,
								expiryDate, expiryTime, lat, lon, activity, delay,
								countMin, countMax, gpsDelay, sensors);
					}
				catch (JSONException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}

		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				getMenuInflater().inflate(R.menu.test, menu);
				return true;
			}

	}
