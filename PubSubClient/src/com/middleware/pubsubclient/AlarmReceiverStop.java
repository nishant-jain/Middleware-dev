package com.middleware.pubsubclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.widget.Toast;

public class AlarmReceiverStop extends BroadcastReceiver
	{

		/*
		 * @Override public void onCreate() { super.onCreate();
		 * LocalBroadcastManager.getInstance(this).registerReceiver(
		 * mMessageReceiver, new IntentFilter("newTask")); }
		 * 
		 * private final BroadcastReceiver mMessageReceiver = new
		 * BroadcastReceiver() {
		 * 
		 * @Override public void onReceive(Context context, Intent intent) {
		 * 
		 * } };
		 * 
		 * @Override public IBinder onBind(Intent arg0) { // TODO Auto-generated
		 * method stub return null; }
		 */

		@Override
		public void onReceive(Context context, Intent intent)
			{
				Toast.makeText(context, "Received broadcast to stop service",
						Toast.LENGTH_LONG).show();
				System.out.println("Received broadcast to stop service");
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
						i = new Intent(context, MicReadings.class);
					}

				return i;
			}
	}
