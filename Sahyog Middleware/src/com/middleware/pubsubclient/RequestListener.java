package com.middleware.pubsubclient;

import java.util.HashMap;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.IBinder;

public class RequestListener extends Service
	{

		int myID;
		PacketFilter filter = null;
		PacketListener listener = null;
		PacketCollector collector = null;
		static String servingQuery;
		HashMap<String, JSONObject> dataRequests = new HashMap<String, JSONObject>();

		public static boolean running = false;
		PendingIntent pendIntent;

		@Override
		public IBinder onBind(Intent arg0)
			{
				return null;
			}

		@Override
		public void onCreate()
			{
				super.onCreate();

				filter = new MessageTypeFilter(Message.Type.normal);

			}

		void actOnMessage(Message message) throws JSONException
			{
				if (message.getSubject().equals("DataRequest"))
					{
						System.out.println(message.getBody());
						final JSONObject confirmation = new JSONObject();
						final String queryNo;
						String request = message.getBody();
						final JSONObject o = new JSONObject(request);
						queryNo = o.getString("queryNo");
						final Message requestAck = new Message("server@"
								+ RegisterDevice.serverIP, Message.Type.chat);
						requestAck.setSubject("ProviderResponse");

						dataRequests.put(queryNo, o);
						try
							{
								confirmation.put("queryNo", queryNo);
								confirmation.put("status", "Accepted");

								requestAck.setBody(confirmation.toString());
								RegisterDevice.conn.sendPacket(requestAck);
							}
						catch (JSONException e)
							{
								e.printStackTrace();
							}
					}

				else if (message.getSubject().equals("Final Confirmation"))
					{
						JSONObject messageBody = new JSONObject(message.getBody());
						String queryNo = messageBody.getString("queryNo");
						String finalStatus = messageBody.getString("finalStatus");
						if (finalStatus.equals("Confirmed")
								&& dataRequests.containsKey(queryNo))
							{
								servingQuery = queryNo;
								System.out
										.println("Query no: "
												+ queryNo
												+ ". You decided to serve, got selected by the server. Now provide the data :)");
								scheduleDataCollection(queryNo);
							}

					}

				else if (message.getSubject().equals("De-Registration Successful"))
					{
						DeleteDeviceAccount.ackReceived = true;
					}

				else if (message.getSubject().equalsIgnoreCase("isQueryPossible"))
					{

						final JSONObject o = new JSONObject(message.getBody());
						if (!o.get("queryAck").equals("accepted"))
							{
								System.out.println("Sorry. Not enough capable users.");

							}

						else
							{
								System.out.println("Enough capable users. :)");

							}

					}

				else if (message.getSubject().equals("RequestedData"))
					{
						String data = message.getBody();
						JSONObject JSONdata = new JSONObject(data);
						String sensor = JSONdata.getString("sensorType");
						if (sensor.equalsIgnoreCase("Accelerometer")
								|| sensor.equalsIgnoreCase("GPS"))
							{
								ConvertCSVToFile.convert(message);
							}
						if (sensor.equalsIgnoreCase("Microphone"))
							{
								ConvertAudioToFile.convert(message);
							}
					}
			}

		void scheduleDataCollection(String queryNo) throws JSONException
			{
				JSONObject messageBody = dataRequests.get(queryNo);
				String sensors = messageBody.getString("sensorType");
				String frequency = messageBody.getString("frequency");
				String fromTime = messageBody.getString("fromTime");
				String endTime = messageBody.getString("toTime");

				System.out.println("Query No: " + queryNo + ", Start Time: " + fromTime
						+ ", End Time: " + endTime + ", Sensors: " + sensors
						+ ", Frequency: " + frequency);
				Intent intent = new Intent("dataRequest");
				intent.putExtra("whatToDo", "start");
				intent.putExtra("sensorName", sensors);
				intent.putExtra("frequency", frequency);

				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						this.getApplicationContext(), 1234, intent, 0);
				AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
				alarmManager.set(AlarmManager.RTC_WAKEUP, Long.parseLong(fromTime),
						pendingIntent);

				System.out.println("Sent broadcast to start service");

				Intent intent2 = new Intent("dataStopRequest");
				intent2.putExtra("whatToDo", "stop");
				intent2.putExtra("sensorName", sensors);
				intent2.putExtra("frequency", frequency);
				PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
						this.getApplicationContext(), 9876, intent2, 0);
				AlarmManager alarmManager2 = (AlarmManager) getSystemService(ALARM_SERVICE);
				alarmManager2.set(AlarmManager.RTC_WAKEUP, Long.parseLong(endTime),
						pendingIntent2);

				System.out.println("Sent broadcast to stop service");

			}

		@Override
		public int onStartCommand(Intent intent_received, int flags, int startId)
			{
				myID = 1234;

				listener = new PacketListener()
					{
						@Override
						public void processPacket(Packet packet)
							{
								Message message = (Message) packet;
								if (message.getBody() != null)
									{
										String from = StringUtils
												.parseBareAddress(message.getFrom());
										String body = message.getBody();
										String subject = message.getSubject();
										System.out.println("Message Received from "
												+ from + ": " + subject + "\n" + body);
										System.out.println("Will act on message now");

										try
											{
												actOnMessage(message);
											}
										catch (JSONException e)
											{
												e.printStackTrace();
											}
									}
							}
					};

				RegisterDevice.conn.addPacketListener(listener, filter);

				Intent intent = new Intent(this, RequestListener.class);
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
						.setContentText("Listening to query requests");
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
				RegisterDevice.conn.removePacketListener(listener);
				running = false;
			}

	}
