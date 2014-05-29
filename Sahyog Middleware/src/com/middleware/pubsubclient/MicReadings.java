package com.middleware.pubsubclient;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class MicReadings extends Service
	{

		MediaRecorder m = null;
		MediaPlayer mp = null;
		public static File audiofile = null;
		File directory;
		public static String mFileName;
		private int myID;

		@Override
		public void onCreate()
			{
				super.onCreate();
			}

		// public void convert()
		// {
		// File file = new File(Environment.getExternalStorageDirectory() +
		// "/hello-4.wav");
		// byte[] bytes;
		// try {
		// bytes = FileUtils.readFileToByteArray(file);
		// } catch (IOException e1) {
		// // TODO Auto-generated catch block
		// bytes=null;
		// e1.printStackTrace();
		// }
		//
		// String encoded = Base64.encodeToString(bytes, 0);
		// Log.d(" Encoded: ", encoded);

		// byte[] decoded = Base64.decode(encoded, 0);
		// Log.d("Decoded: ", Arrays.toString(decoded));
		//
		// try
		// {
		// File file2 = new File(Environment.getExternalStorageDirectory() +
		// "/hello-5.wav");
		// FileOutputStream os = new FileOutputStream(file2, true);
		// os.write(decoded);
		// os.close();
		// }
		// catch (Exception e)
		// {
		// e.printStackTrace();
		// }
		//
		//
		//
		// }

		@Override
		public int onStartCommand(Intent intent_received, int flags, int startId)
			{
				System.out.println("Will now record microphone data");
				m = new MediaRecorder();
				Date date = new Date();
				mFileName = android.text.format.DateFormat.format("MM-dd-yy_kk-mm-ss",
						date).toString();
				directory = new File(new File(Environment.getExternalStorageDirectory()
				// + "/ReadingsAcc/");
						+ "/DataCollection/").getPath(), "Experiment_" + mFileName + "/");

				if (!directory.exists())
					{
						directory.mkdirs();
					}
				audiofile = new File(directory + "/mic.3gp");

				m.setAudioSource(MediaRecorder.AudioSource.MIC);
				m.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				m.setAudioEncoder(AudioEncoder.AMR_NB);
				m.setOutputFile(audiofile.getAbsolutePath());

				try
					{
						m.prepare();
					}
				catch (IllegalStateException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.v("TAG", e.getMessage());
					}
				m.start();

				myID = 1234;
				Intent intent = new Intent(this, MicReadings.class);
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

		@Override
		public void onDestroy()
			{
				super.onDestroy();
				m.stop();
				m.release();
				m = null;

				ConvertAudioToMsg.sendAsMessage(audiofile, RequestListener.servingQuery); // will
																							// not
																							// work
																							// as
																							// the
																							// file
																							// is
																							// not
																							// in
																							// csv
																							// format

				// JSONObject[] array=new JSONObject[1]; //only for tesing, must
				// be replaced by the actual messages arriving
				// //size of the array = number of samples requested
				// array[0]=message; //move to a proper location.
				// try {
				// ConvertAccToFile.convert(array);
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }

				// startService(RegisterDevice.intentRequestListener);
				startService(new Intent(this.getApplicationContext(),
						RequestListener.class));
			}

		public void onPause()
			{

			}

		@Override
		public IBinder onBind(Intent intent)
			{
				// TODO Auto-generated method stub
				return null;
			}

	}
