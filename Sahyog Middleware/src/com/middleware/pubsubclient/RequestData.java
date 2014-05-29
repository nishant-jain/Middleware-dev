package com.middleware.pubsubclient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class RequestData
	{

		static SimpleDateFormat df;
		LocationManager locationManager = null;
		MyListener ml;
		static JSONObject query;
		static Date fromDt;
		public static String queryNoAcc, queryNoGPS, queryNoMicro;
		static boolean validity;

		static ArrayList<String> querynumbers = new ArrayList<String>();

		Double currLat = null, currLong = null;
		Boolean foundCurrLoc = false;

		public boolean tryFindingCurrentLocation()
			{
				ml = new MyListener();
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
						0, ml);
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
						0, 0, ml);
				locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
						0, 0, ml);

				int i = 0;

				while ((!foundCurrLoc) && i <= 400000)
					{
						i++;
					}

				locationManager.removeUpdates(ml);
				if (foundCurrLoc)
					{
						return true;
					}
				else
					{
						return false;
					}
			}

		public String getCurrentLocation()
			{
				return "" + currLat + " " + currLong;
			}

		public static ArrayList<String> publishQuery(String fromDate, String fromTime,
				String toDate, String toTime, String expiryDate, String expiryTime,
				Double lat, Double lon, String activity, int delay, int countMin,
				int countMax, int gpsDelay, ArrayList<String> sensors)
				throws JSONException
			{
				validity = true;
				df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				Long fromEpoch, toEpoch, expiryEpoch;
				fromDt = new Date();
				try
					{
						fromDt = df.parse(fromDate + " " + fromTime);
					}
				catch (ParseException e)
					{
						e.printStackTrace();
					}
				fromEpoch = fromDt.getTime();
				Date toDt = new Date();
				try
					{
						toDt = df.parse(toDate + " " + toTime);
					}
				catch (ParseException e)
					{
						e.printStackTrace();
					}
				toEpoch = toDt.getTime();

				Date expiryDt = new Date();
				try
					{
						expiryDt = df.parse(expiryDate + " " + expiryTime);
					}
				catch (ParseException e)
					{
						e.printStackTrace();
					}

				expiryEpoch = expiryDt.getTime();

				if (fromEpoch > toEpoch)
					{
						validity = false;
					}

				if (fromEpoch > expiryEpoch)
					{
						validity = false;
					}

				if (toEpoch > expiryEpoch)
					{
						validity = false;
					}

				if (countMin > countMax)
					{
						validity = false;
					}
				List<JSONObject> all = new ArrayList<JSONObject>();

				if (sensors.contains("accelerometer"))
					{
						queryNoAcc = RegisterDevice.username + System.nanoTime();
						query = new JSONObject();
						query.put("username", RegisterDevice.username);
						query.put("queryNo", queryNoAcc);
						query.put("dataReqd", "Accelerometer");
						query.put("fromTime", fromEpoch);
						query.put("toTime", toEpoch);
						query.put("expiryTime", expiryEpoch);
						query.put("latitude", lat);
						query.put("longitude", lon);
						query.put("activity", activity);
						query.put("frequency", delay);
						query.put("countMin", countMin);
						query.put("countMax", countMax);
						all.add(query);
						querynumbers.add(queryNoAcc);
					}
				if (sensors.contains("gps"))
					{
						queryNoGPS = RegisterDevice.username + System.nanoTime();
						query = new JSONObject();
						query.put("username", RegisterDevice.username);
						query.put("queryNo", queryNoGPS);
						query.put("dataReqd", "GPS");
						query.put("fromTime", fromEpoch);
						query.put("toTime", toEpoch);
						query.put("expiryTime", expiryEpoch);
						query.put("latitude", lat);
						query.put("longitude", lon);
						query.put("activity", activity);
						query.put("frequency", delay);
						query.put("countMin", countMin);
						query.put("countMax", countMax);
						all.add(query);
						querynumbers.add(queryNoGPS);
					}

				if (sensors.contains("microphone"))
					{
						queryNoMicro = RegisterDevice.username + System.nanoTime();
						query = new JSONObject();
						query.put("username", RegisterDevice.username);
						query.put("queryNo", queryNoMicro);
						query.put("dataReqd", "Microphone");
						query.put("fromTime", fromEpoch);
						query.put("toTime", toEpoch);
						query.put("expiryTime", expiryEpoch);
						query.put("latitude", lat);
						query.put("longitude", lon);
						query.put("activity", activity);
						query.put("frequency", delay);
						query.put("countMin", countMin);
						query.put("countMax", countMax);
						all.add(query);
						querynumbers.add(queryNoMicro);
					}

				sendQuery(all);

				return querynumbers;
			}

		public static void sendQuery(List<JSONObject> query)
			{

				try
					{
						if (fromDt.getTime() > System.currentTimeMillis())
							{
								if (validity)
									{
										Iterator<JSONObject> traverse = query.iterator();
										while (traverse.hasNext())
											{
												Message query2 = new Message("server@"
														+ RegisterDevice.serverIP,
														Message.Type.chat);
												query2.setSubject("Query");
												query2.setBody(traverse.next().toString());
												RegisterDevice.conn.sendPacket(query2);
											}
										System.out.println("Query submission successful");
									}
								else
									{
										System.out.println("Invalid Fields!");
									}
							}
						else
							{
								System.out.println("Start time already passed!");
							}
					}
				catch (Exception e)
					{
						System.out.println("Invalid data!");
					}

			}

		class MyListener implements LocationListener
			{
				@Override
				public void onLocationChanged(Location location)
					{
						currLat = location.getLatitude();
						currLong = location.getLongitude();
						foundCurrLoc = true;
						locationManager.removeUpdates(ml);
					}

				@Override
				public void onProviderDisabled(String arg0)
					{
					}

				@Override
				public void onProviderEnabled(String arg0)
					{
					}

				@Override
				public void onStatusChanged(String arg0, int arg1, Bundle arg2)
					{
					}

			}

	}
