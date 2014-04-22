package com.middleware.pubsubclient;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

public class RequestListener extends Service {

	int myID;
	PacketFilter filter = null;
	PacketListener listener = null;
	PacketCollector collector = null;

	HashMap<String, JSONObject> dataRequests = new HashMap<String, JSONObject>();

	public static boolean running = false;
	PendingIntent pendIntent;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		filter = new MessageTypeFilter(Message.Type.normal);

		// will not be needed when actually listening for messages
		// -------------------------------------------------------

		JSONObject json = new JSONObject();
		try {
			json.put("sensorType", "Accelerometer");
			json.put("fromTime", 1397627067);
			json.put("toTime", 1397629067);
			json.put("Activity", "driving");
			json.put("frequency", "2");
			json.put("queryNo", "123456778");
		}

		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Message msg = new Message();
		msg.setSubject("DataRequest");
		msg.setBody(json.toString());
		// try {
		// actOnMessage(msg);
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// final Handler handler = new Handler();
		// handler.postDelayed(new Runnable() {
		// @Override
		// public void run() {
		//
		// JSONObject json2 = new JSONObject();
		// try {
		// // json.put("sensorType", "Accelerometer");
		// // json.put("fromTime",1397627067);
		// // json.put("toTime", 1397629067);
		// // json.put("Activity", "driving");
		// json2.put("queryNo", "123456778");
		// // json.put("frequency","2");
		// json2.put("finalStatus", "Confirmed");
		// // json.put("errorMessage",
		// // "Already got the required providers! :)");
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// Message msg2 = new Message();
		// msg2.setSubject("FinalConfirmation");
		// msg2.setBody(json2.toString());
		// try {
		// actOnMessage(msg2);
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }, 8000);

	}

	void actOnMessage(Message message) throws JSONException {
		if (message.getSubject().equals("DataRequest")) {
			System.out.println(message.getBody());
						final JSONObject confirmation = new JSONObject();
			String startTime;
			String endTime;
			final String queryNo;
			String sensor, frequency, activity;
			String request = message.getBody();
			// try {
			final JSONObject o = new JSONObject(request);
			startTime = o.getString("fromTime");
			endTime = o.getString("toTime");
			queryNo = o.getString("queryNo");
			sensor = o.getString("sensorType"); // needs to be parsed for
												// multiple sensor types
			frequency = o.getString("frequency");
			activity = o.getString("Activity");
			Date start = new Date(Long.parseLong(startTime) * 1000);
			Date end = new Date(Long.parseLong(endTime) * 1000);

			final Message requestAck = new Message("server@103.25.231.23",
					Message.Type.chat);
			requestAck.setSubject("ProviderResponse");
			SimpleDateFormat format = new SimpleDateFormat(
					"dd/MM/yyyy HH:mm:ss");
			String formattedStart = format.format(start);
			String formattedEnd = format.format(end);

			/*
			 * AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 * builder.setTitle("New Request");
			 * builder.setIcon(R.drawable.ic_launcher);
			 * builder.setMessage("Are you willing to service this request for "
			 * + sensor + " from " + formattedStart + " to " + formattedEnd +
			 * " while you are " + activity + " ?");
			 * builder.setPositiveButton("Yes", new
			 * DialogInterface.OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int
			 * whichButton) { // send Confirmation to Server
			 */

			// Assume YES for now

			dataRequests.put(queryNo, o);
			try {
				confirmation.put("queryNo", queryNo);
				confirmation.put("status", "Accepted");

				requestAck.setBody(confirmation.toString());
				RegisterMe.conn.sendPacket(requestAck);
			} catch (JSONException e) {
				// TODO
				// Auto-generated
				// catch block
				e.printStackTrace();
			}
			/*
			 * } }); builder.setNegativeButton("No", new
			 * DialogInterface.OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int
			 * whichButton) { // Request denied try {
			 * confirmation.put("queryNo", queryNo); confirmation.put("status",
			 * "Denied");
			 * 
			 * requestAck.setBody(confirmation.toString());
			 * RegisterMe.conn.sendPacket(requestAck); } catch (JSONException e)
			 * { // TODO // Auto-generated // catch block e.printStackTrace(); }
			 * } }); AlertDialog alert = builder.create();
			 * alert.getWindow().setType(
			 * WindowManager.LayoutParams.TYPE_SYSTEM_ALERT); alert.show();
			 * 
			 * } catch (JSONException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */

		}

		else if (message.getSubject().equals("Final Confirmation")) {
			JSONObject messageBody = new JSONObject(message.getBody());
			String queryNo = messageBody.getString("queryNo");
			String finalStatus = messageBody.getString("finalStatus");
			if (finalStatus.equals("Confirmed")
					&& dataRequests.containsKey(queryNo)) {
				System.out
						.println("Query no: "
								+ queryNo
								+ ". You decided to serve, got selected by the server. Now provide the data :)");
			}

			scheduleDataCollection(queryNo);

			String messageBody1 = message.getBody();
			try {
				final JSONObject o = new JSONObject(messageBody1);
				// {“queryNo”: “23121312312”, “finalStatus”:
				// “Confirmed”} -- collect data in this case
				boolean collectionStatus = o.getString("finalStatus").equals(
						"Confirmed");
				if (collectionStatus) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Thank you");
					builder.setIcon(R.drawable.ic_launcher);
					builder.setMessage("Thank you for your cooperation. Data will be collected in the background.");
					builder.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {

									try {
										scheduleDataCollection(o
												.getString("queryNo"));
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									dialog.dismiss();
								}
							});
					builder.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.dismiss();
								}
							});
					// AlertDialog alert = builder.create();
					// alert.getWindow().setType(
					// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					// alert.show();
				}
				// {“queryNo”: “23121312312”, “finalStatus”:
				// “Rejected”, "errorMessage":
				// "Already got the required providers! :)"}
				else {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Thank you");
					builder.setIcon(R.drawable.ic_launcher);
					builder.setMessage("Thank you for your cooperation. "
							+ o.getString("errorMessage"));
					builder.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.dismiss();
								}
							});
					// AlertDialog alert = builder.create();
					// alert.getWindow().setType(
					// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					// alert.show();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		else if (message.getSubject().equals("De-Registration Successful")) {
			DeletePreference.ackReceived = true;
		}

		else if (message.getSubject().equalsIgnoreCase("isQueryPossible")) {
			System.out.println("Sorry. Not enough capable users.");
			Intent iQP = new Intent("buildAlert");
			iQP.putExtra("toShow", "Sorry, not enough capable users");
			LocalBroadcastManager.getInstance(this).sendBroadcast(iQP);
		}
	}

	void scheduleDataCollection(String queryNo) throws JSONException {
		JSONObject messageBody = dataRequests.get(queryNo);
		// String sensors = messageBody.getString("sensorType");
		// String frequency = messageBody.getString("frequency");
		// String activity = messageBody.getString(activity");
		// String fromTime = messageBody.getString("fromTime");
		// String endTime = messageBody.getString("endTime");

		Intent intent = new Intent("dataRequest");
		intent.putExtra("whatToDo", "start");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				this.getApplicationContext(), 1234, intent, 0);
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + (5000), pendingIntent);

		// alarmManager.set(AlarmManager.RTC_WAKEUP, Long.parseLong(fromTime),
		// pendingIntent);

		System.out.println("Set start");

		Intent intent2 = new Intent("dataStopRequest");
		intent2.putExtra("whatToDo", "stop");
		PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
				this.getApplicationContext(), 9876, intent2, 0);
		AlarmManager alarmManager2 = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager2.set(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + (9000), pendingIntent2);

		// alarmManager2.set(AlarmManager.RTC_WAKEUP,
		// System.currentTimeMillis() + (50000), pendingIntent2);
		// System.out.println("Set stop");

		// scheduler.set(AlarmManager.RTC_WAKEUP,
		// start.getTime(),
		// pendIntent);
	}

	@Override
	public int onStartCommand(Intent intent_received, int flags, int startId) {
		myID = 1234;

		listener = new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				Message message = (Message) packet;
				if (message.getBody() != null) {
					String from = StringUtils.parseBareAddress(message
							.getFrom());
					String body = message.getBody();
					String subject = message.getSubject();
					System.out.println("Message Received from " + from + ": "
							+ subject + "\n" + body);
					System.out.println("Will act on message now");

					// Act on each message in a separate
					// thread
					try {
						actOnMessage(message);
					} catch (JSONException e) {
						// TODO Auto-generated catch
						// block
						e.printStackTrace();
					}
				}
			}
		};

		RegisterMe.conn.addPacketListener(listener, filter);

		Intent intent = new Intent(this, RequestListener.class);
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
				.setContentText("Listening to query requests");
		// .setContentText(res.getString(R.string.service));
		@SuppressWarnings("deprecation")
		Notification n = builder.getNotification();

		n.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(myID, n);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		RegisterMe.conn.removePacketListener(listener);
		running = false;
	}

}
