package com.middleware.pubsubclient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

//foreground service - calls MyGooglePlayClass
public class ActivityRecognitionCallingService extends Service
	{

		private int myID;
		MyGooglePlayClass mgpc;
		File file = null;
		File file2 = null;
		BufferedWriter writer = null;
		BufferedWriter writer2 = null;

		// File directory;
		Date date;
		String detectedActivity;
		String confidence;
		String time_in_millisecs;

		@Override
		public void onCreate()
			{
				super.onCreate();
				myID = 1234;
			}

		@Override
		public void onDestroy()
			{
				super.onDestroy();
				mgpc.stopActivityRecognition();

				ConvertCSVToMsg.sendAsMessage(file, RequestListener.servingQuery);
				try
					{
						writer.close();
						writer2.close();
					}
				catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}

		@Override
		public IBinder onBind(Intent intent)
			{
				// TODO Auto-generated method stub
				return null;
			}

		private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
					{
						detectedActivity = intent.getStringExtra("name");
						confidence = intent.getStringExtra("confidence");
						time_in_millisecs = intent.getStringExtra("time_in_millisecs");
						try
							{
								writer.write(time_in_millisecs + "," + detectedActivity
										+ "," + confidence + "\n");
								writer2.write(intent.getStringExtra("entireResult")
										+ "\n");
							}
						catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
			};

		@Override
		public int onStartCommand(Intent intent_received, int flags, int startId)
			{

				File directory;

				Date date = new Date();
				String time = android.text.format.DateFormat.format("MM-dd-yy_kk-mm-ss",
						date).toString();
				directory = new File(new File(Environment.getExternalStorageDirectory()
						+ "/DataCollection/").getPath(), "Experiment_" + time + "/");

				file = new File(directory + "/" + "_ActivityRecogMostProbable.csv");
				file2 = new File(directory + "/" + "_ActivityRecogFullData.csv");

				try
					{
						writer = new BufferedWriter(new FileWriter(file, false));
						writer2 = new BufferedWriter(new FileWriter(file2, false));
					}
				catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				LocalBroadcastManager.getInstance(this).registerReceiver(
						mMessageReceiver, new IntentFilter("activityDetected"));
				// instantiate the activity recognition class, send the context
				// of the application
				// and start the recognition
				mgpc = new MyGooglePlayClass(this, intent_received.getIntExtra(
						"act_recog_interval", 0));
				mgpc.startActivityRecognition();
				// Toast.makeText(getApplicationContext(), "On start",
				// Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(this, ActivityRecognitionService.class);
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
