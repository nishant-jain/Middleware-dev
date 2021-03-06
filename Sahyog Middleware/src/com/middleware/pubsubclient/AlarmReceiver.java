package com.middleware.pubsubclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

public class AlarmReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
			{
				// Toast.makeText(context,
				// "Received broadcast to start service",
				// Toast.LENGTH_LONG).show();
				System.out.println("Received broadcast to start service");
				// Vibrate the mobile phone
				Vibrator vibrator = (Vibrator) context
						.getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(2000);
				String sensorName = intent.getExtras().getString("sensorName");
				Intent i = returnRelevantIntent(context, sensorName);
				if (intent.getExtras().get("whatToDo").equals("start"))
					{
						System.out.println("Starting service for " + sensorName);
						context.startService(i);
					}

				if (intent.getExtras().get("whatToDo").equals("stop"))
					{
						System.out.println("Stoping service for " + sensorName);
						context.stopService(i);
					}

			}

		public Intent returnRelevantIntent(Context context, String sensorName)
			{
				Intent i = null;
				System.out.println("Finding relevant intent");
				System.out.println("Sensor name = " + sensorName);

				if (sensorName.equalsIgnoreCase("accelerometer"))
					{
						i = new Intent(context, AccReadings.class);
					}

				else if (sensorName.equalsIgnoreCase("gps"))
					{
						i = new Intent(context, GPSReadings.class);
					}

				else if (sensorName.equalsIgnoreCase("microphone"))
					{
						System.out.println("microphone matched");
						i = new Intent(context, MicReadings.class);
					}

				else if (sensorName.equalsIgnoreCase("activity"))
					{
						System.out.println("activity matched");
						i = new Intent(context, ActivityRecognitionCallingService.class);
					}

				return i;
			}
	}
