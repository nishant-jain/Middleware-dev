package com.middleware.pubsubclient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
//import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.IBinder;
//import android.text.format.Time;
//import android.widget.Toast;

public class AccReadings extends Service implements SensorEventListener {

	private SensorManager mSensorManager;
	private int myID;
	File file = null;
	BufferedWriter writer = null;
	private Sensor mAcc = null;

	public void onCreate() {
		super.onCreate();
		//Toast.makeText(this, "Service created to record accelerometer data.",
		//		Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;
		
		try {
			writer.write(event.timestamp+ ","
					+ event.values[0] + "," + event.values[1] + ","
					+ event.values[2] + "," +  "\n");
			writer.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mSensorManager.unregisterListener(this);

		try {
			if (writer != null)
				writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent_received, int flags, int startId) {

		File directory;

		Date date = new Date();
		String time = android.text.format.DateFormat.format(
				"MM-dd-yy_kk-mm-ss", date).toString();
		directory = new File(new File(Environment.getExternalStorageDirectory()
				+ "/DataCollection/").getPath(), "Experiment_" + time + "/");

		file = new File(directory + "/acc.csv");

		try {
			writer = new BufferedWriter(new FileWriter(file, false));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		//Delay would be based on query later
		mSensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_NORMAL);

		myID = 1234;
		Intent intent = new Intent(this, AccReadings.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendIntent = PendingIntent
				.getActivity(this, 0, intent, 0);

		Resources res = this.getResources();
		Notification.Builder builder = new Notification.Builder(this);

		builder.setContentIntent(pendIntent)
				.setSmallIcon(R.drawable.ic_launcher)
				.setLargeIcon(
						BitmapFactory.decodeResource(res,
								R.drawable.ic_launcher))
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
