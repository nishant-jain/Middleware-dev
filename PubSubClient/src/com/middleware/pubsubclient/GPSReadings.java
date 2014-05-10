package com.middleware.pubsubclient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

public class GPSReadings extends Service
	{
		private LocationManager locationManager;
		private String provider;
		File gpsFile;
		String path;
		private int myID;
		BufferedWriter writer = null;
		double latitude, longitude, altitude;
		float speed;
		int sampleRate, dataRate, count;

		LocationListener gpsListener = new LocationListener()
			{

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras)
					{
						// TODO Auto-generated method stub

					}

				@Override
				public void onProviderEnabled(String provider)
					{
						// TODO Auto-generated method stub

					}

				@Override
				public void onProviderDisabled(String provider)
					{
						// TODO Auto-generated method stub

					}

				@Override
				public void onLocationChanged(Location location)
					{
						// TODO Auto-generated method stub
						latitude = location.getLatitude();
						longitude = location.getLongitude();
						speed = location.getSpeed();

						try
							{
								writer.write(System.currentTimeMillis() + "," + latitude
										+ "," + longitude + "," + speed + "," + "\n");
								writer.flush();
							}
						catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

					}
			};

		public void onResume()
			{
				locationManager.requestLocationUpdates(provider, 0, 0, gpsListener);
				// System.out.println(latitude+":"+longitude);
			}

		public void onPause()
			{
				locationManager.removeUpdates(gpsListener);

			}

		@Override
		public void onDestroy()
			{
				super.onDestroy();
				locationManager.removeUpdates(gpsListener);
				boolean status = ConvertCSVToMsg.sendAsMessage(gpsFile,
						RequestListener.servingQuery);

				try
					{
						if (writer != null)
							{
								writer.close();
							}
					}
				catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				startService(RegisterMe.intentRequestListener);
			}

		@Override
		public IBinder onBind(Intent intent)
			{
				// TODO Auto-generated method stub
				return null;
			}

		@Override
		public int onStartCommand(Intent intent_received, int flags, int startId)
			{

				File directory;

				Date date = new Date();
				String mFileName = android.text.format.DateFormat.format(
						"MM-dd-yy_kk-mm-ss", date).toString();
				directory = new File(new File(Environment.getExternalStorageDirectory()
				// + "/ReadingsAcc/");
						+ "/DataCollection/").getPath(), "Experiment_" + mFileName + "/");

				if (!directory.exists())
					{
						directory.mkdirs();
					}

				gpsFile = new File(directory + "/gps.csv");
				Toast.makeText(this, "Recording data", Toast.LENGTH_LONG).show();
				try
					{
						writer = new BufferedWriter(new FileWriter(gpsFile, false));
					}
				catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				provider = locationManager.GPS_PROVIDER;
				// Location location =
				// locationManager.getLastKnownLocation(provider);
				locationManager.requestLocationUpdates(provider, 0, 0, gpsListener);

				myID = 1234;
				Intent intent = new Intent(this, GPSReadings.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

				Resources res = this.getResources();
				Notification.Builder builder = new Notification.Builder(this);

				builder.setContentIntent(pendIntent)
						.setSmallIcon(R.drawable.ic_launcher)
						.setLargeIcon(
								BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
						.setWhen(System.currentTimeMillis()).setAutoCancel(true)
						.setContentTitle(res.getString(R.string.app_name))
						.setContentText("Service running");
				// .setContentText(res.getString(R.string.service));
				@SuppressWarnings("deprecation")
				Notification n = builder.getNotification();

				n.flags |= Notification.FLAG_NO_CLEAR;
				startForeground(myID, n);
				return START_STICKY;
			}
	}
