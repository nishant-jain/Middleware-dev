package com.middleware.pubsubclient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.view.WindowManager;
import android.widget.Toast;

public class RequestListener extends Service{
	
	int myID;
	PacketFilter filter= null;
	PacketListener listener = null;
	PacketCollector collector = null;
	public static boolean running=false;
	PendingIntent pendIntent;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate() {
		super.onCreate();
		
		//filter = new AndFilter(new PacketTypeFilter(Message.class));
		filter = new MessageTypeFilter(Message.Type.normal);
		/*collector = RegisterMe.conn.createPacketCollector(filter);
		running=true;
		Intent intent = new Intent(this, AccReadings.class);
		//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		pendIntent = PendingIntent.getService(this, 0, intent, 0);
		*/
		
		//will not be needed when actually listening for messages
		//-------------------------------------------------------
		JSONObject json=new JSONObject();		
		try {
			//json.put("sensorType", "Accelerometer");
			//json.put("fromTime",1397627067);
			//json.put("toTime", 1397629067);
			//json.put("Activity", "driving");
			json.put("queryNo", "123456778");
			//json.put("frequency","2");
			json.put("finalStatus", "Rejected");
			json.put("errorMessage", "Already got the required providers! :)");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Message msg= new Message();
		msg.setSubject("FinalConfirmation");
		msg.setBody(json.toString());
		actOnMessage(msg);
		//--------------------------------------------------------
		
		
	}
	
	void actOnMessage(Message message)
	{
		if(message.getSubject().equals("DataRequest"))
		{
			final JSONObject confirmation = new JSONObject();
			long startTime, endTime;
			final String queryNo;
			String sensor, frequency, activity;
			String request= message.getBody();
			try {
				JSONObject o = new JSONObject(request);
				startTime =o.getLong("fromTime");
				endTime=o.getLong("toTime");
				queryNo = o.getString("queryNo");
				sensor = o.getString("sensorType");	//needs to be parsed for multiple sensor types
				frequency= o.getString("frequency");
				activity=o.getString("Activity");
				Date start=new Date(Long.parseLong(String.valueOf(startTime*1000)));
				Date end=new Date(Long.parseLong(String.valueOf(endTime*1000)));				
				
				final Message requestAck = new Message("server@103.25.231.23",Message.Type.chat);
				requestAck.setSubject("ProviderResponse");
				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				String formattedStart = format.format(start);
				String formattedEnd = format.format(end);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("New Request");
				builder.setIcon(R.drawable.ic_launcher);
				builder.setMessage("Are you willing to service this request for "+ sensor + " from "+ formattedStart + " to "+ formattedEnd +" while you are " + activity +" ?");
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int whichButton) {
				        //send confirmation to the server that the request will be serviced
				    	//{“queryNo”: “2312312112”, “status”: “Accepted”}				    	
				    	try {
				    		confirmation.put("queryNo", queryNo);
							confirmation.put("status", "Accepted");
							
							requestAck.setBody(confirmation.toString());
							RegisterMe.conn.sendPacket(requestAck);	
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }
				    });
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int whichButton) { 
				    	//else the request is denied by the provider -- send a negative acknowledgment
				    	//{“queryNo”: “2312312112”, “status”: “Denied”}
				    	try {
				    		confirmation.put("queryNo", queryNo);
							confirmation.put("status", "Denied");
							
							requestAck.setBody(confirmation.toString());
							RegisterMe.conn.sendPacket(requestAck);	
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }
				});
				AlertDialog alert = builder.create();
				alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				alert.show();
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if(message.getSubject().equals("FinalConfirmation"))
		{
			//Dealing only with providers now
			String messageBody=message.getBody();
			try {
				JSONObject o = new JSONObject(messageBody);
				//{“queryNo”: “23121312312”, “finalStatus”: “Confirmed”}  -- collect data in this case
				boolean collectionStatus= o.getString("finalStatus").equals("Confirmed");
				if(collectionStatus)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Thank you");
					builder.setIcon(R.drawable.ic_launcher);
					builder.setMessage("Thank you for your cooperation. Data will be collected in the background.");
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int whichButton) {
					    	//data collection code goes here
							
					    	// start a service at "start" to collect data
							AlarmManager scheduler=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
							//scheduler.set(AlarmManager.RTC_WAKEUP, start.getTime(), pendIntent);
							dialog.dismiss();
					    }
					    });
					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int whichButton) {
					        dialog.dismiss();
					    }
					    });
					AlertDialog alert = builder.create();
					alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					alert.show();
				}
				//{“queryNo”: “23121312312”, “finalStatus”: “Rejected”, "errorMessage": "Already got the required providers! :)"}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Thank you");
					builder.setIcon(R.drawable.ic_launcher);
					builder.setMessage("Thank you for your cooperation. "+ o.getString("errorMessage"));
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int whichButton) {
					        dialog.dismiss();
					    }
					    });
					AlertDialog alert = builder.create();
					alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					alert.show();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}
	
	@Override
	public int onStartCommand(Intent intent_received, int flags, int startId) {
		myID = 1234;
		
		listener = new PacketListener() {
		    @Override
		    public void processPacket(Packet packet) {
		        Message message = (Message) packet;
		        if (message.getBody() != null) {
		            String from = StringUtils.parseBareAddress(message.getFrom());
		            String body = message.getBody();
		            String subject = message.getSubject();
		            System.out.println("Message Received from "+from+": "+subject+"\n"+body);
		            System.out.println("Will act on message now");
		            actOnMessage(message);
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
		running=false;
	}
	
}
