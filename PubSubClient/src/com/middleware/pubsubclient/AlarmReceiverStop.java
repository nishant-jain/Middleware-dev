package com.middleware.pubsubclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.widget.Toast;

public class AlarmReceiverStop extends BroadcastReceiver {

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
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "Alarm!", Toast.LENGTH_LONG).show();
		// Vibrate the mobile phone
		Vibrator vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(2000);

		Intent i = new Intent(context, AccReadings.class);
		if (intent.getExtras().get("whatToDo").equals("start")) {
			System.out.println("Starting accReadings");
			context.startService(i);
		}

		if (intent.getExtras().get("whatToDo").equals("stop")) {
			System.out.println("Stopping accReadings");
			context.stopService(i);
		}

	}
}
