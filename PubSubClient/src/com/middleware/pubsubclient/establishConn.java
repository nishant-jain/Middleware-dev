package com.middleware.pubsubclient;

import java.net.URL;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.os.AsyncTask;
import android.widget.Toast;

public class EstablishConn extends AsyncTask<Void, Void, Void> {

	@Override
	protected Void doInBackground(Void... v) {
		// TODO Auto-generated method stub
		
		ConnectionConfiguration config=new ConnectionConfiguration("jabber.org",5222);
		config.setDebuggerEnabled(true);
		XMPPConnection conn=new XMPPConnection(config);
		try {
			conn.connect();
			conn.login("username", "password");
			System.out.println("Connection Established");
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("connection established");
		}
		return null;
		
	}

}
