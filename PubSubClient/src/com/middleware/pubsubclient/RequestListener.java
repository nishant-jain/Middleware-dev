package com.middleware.pubsubclient;

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

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.IBinder;

public class RequestListener extends Service{
	
	int myID;
	PacketFilter filter= null;
	PacketListener listener = null;
	PacketCollector collector = null;
	public static boolean running=false;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate() {
		super.onCreate();
		//filter = new AndFilter(new PacketTypeFilter(Message.class));
		filter = new MessageTypeFilter(Message.Type.normal);
		//collector = RegisterMe.conn.createPacketCollector(filter);
		running=true;
	}
	
	void actOnMessage(Message message)
	{
		if(message.getSubject().equals("DataRequest"))
		{
			System.out.println("New request received..Decide whether to serve it or not");
			System.out.println("Pop up should be created to ask whether servicing the request or not");
			//process the request in a different thread
		}
		
		else if(message.getSubject().equals("FinalConfirmation"))
		{
			System.out.println("Decided to serve...selected by the server..now provide the data");
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
