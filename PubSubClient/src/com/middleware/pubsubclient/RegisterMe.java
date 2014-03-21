package com.middleware.pubsubclient;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class RegisterMe extends Activity{
	
	public static String username;
	public String password;
	public static final String PREFS_NAME = "Preferences_File";
	ConnectionConfiguration config;
	public static XMPPConnection conn;
	
	SharedPreferences chkInstall;
	SharedPreferences.Editor editPrefs;
	SensorManager sm;
	//TextView tv;
	public static List<Sensor> deviceSensors;
	AlertDialog.Builder showDialog;
	Message loginWithServer;
	
	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_me);
		XMPPConnection.DEBUG_ENABLED=true;
		showDialog=new Builder(this);
//		tv = (TextView)findViewById(R.id.textView1);
//		tv.setText("List of sensors in this phone:\n");
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		deviceSensors = sm.getSensorList(Sensor.TYPE_ALL);
		//tv.append(deviceSensors.toString());
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
						
		System.out.println("Establishing connection with gtalk server");
		config=new ConnectionConfiguration("server@103.25.231.31",5222, "103.25.231.23");
		config.setDebuggerEnabled(true);
		//config=new ConnectionConfiguration("jabber.org",5222);
		conn=new XMPPConnection(config);
		
								
		try {
				config.setSASLAuthenticationEnabled(true);
				config.setCompressionEnabled(true);
				config.setSecurityMode(SecurityMode.enabled);
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				    config.setTruststoreType("AndroidCAStore");
				    config.setTruststorePassword(null);
				    config.setTruststorePath(null);
				} 
				else {
				    config.setTruststoreType("BKS");
				    String path = System.getProperty("javax.net.ssl.trustStore");
				    if (path == null)
						        path = System.getProperty("java.home") + File.separator + "etc" + File.separator + "security" + File.separator + "cacerts.bks";
							    config.setTruststorePath(path);
				}
		} 
		
		catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
		}
		
		
		if(isNetworkAvailable())
		{
			try {		
			conn.connect();
			System.out.println("Connection Established");
		} catch (XMPPException e1) {
			// TODO Auto-generated catch block
				e1.printStackTrace();
		}		
		}
		else
		{
			System.out.println("No internet Connection");
		}
	
		chkInstall=getSharedPreferences(PREFS_NAME,0);
		editPrefs=chkInstall.edit();
		boolean installing=chkInstall.getBoolean("firstInstall", true);
		
		if(installing)
		{			
			System.out.println("installing application");			
			createUserName();		
			registerClient();		
		}
		
		else
		{
			if(conn.isConnected())
			{
			System.out.println("trying to login");
			try {
				username=chkInstall.getString("username", null);
				password=chkInstall.getString("password", null);
				conn.login(username	,password);
				System.out.println("login successful");
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
			else
				System.out.println("Not connected to the server");
			
		}
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    if(activeNetworkInfo != null && activeNetworkInfo.isConnected())
	    return true;
	    else
	    	return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register_me, menu);
		return true;
		 
	}	

	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		
		switch(item.getItemId())
		{
        case R.id.subscription:
            Intent i=new Intent(getApplicationContext(),SubscribeTopics.class);
            startActivity(i);
            break;
		}
		return false;
		
	}
	
	@SuppressLint("ShowToast")
	public void registerClient()
	{			
		if(isNetworkAvailable() && conn.isConnected())
		{
			AccountManager am=conn.getAccountManager();
			if(am.supportsAccountCreation())
				{
				System.out.println("Server Supports new account creation");
				try{
					/*check how many attributes need to be provided for creating a new account
					 * Collection<String> c=am.getAccountAttributes();
						for (Object o : c)
					    System.out.println(o);
					*/
					StringBuilder sensorList = new StringBuilder();
					username=chkInstall.getString("username", null);
					password=chkInstall.getString("password",null);
					/*
					for(Sensor s : deviceSensors)
					{
						sensorList.append(s.getName()+"\n");	
					}					
					*/
					Map<String, String> attributes = new HashMap<String, String>();
					//attributes.put("sensorInfo", sensorList.toString());   //to send only sensor names
					attributes.put("sensorInfo", deviceSensors.toString());
					//System.out.println(sensorList.toString());
					//am.createAccount(username, password, attributes);
					am.createAccount(username, password);		//creates an account with the XMPP server
					showDialog.setMessage("registered with the XMPP server")
					.create()
					.show();
					
					
					//IQ packets of type SET are used to set new values at the server and ensures a response 
					IQ test = null;
					test.setType(IQ.Type.SET);
					test.setFrom(username);
					test.setProperty("sensors",deviceSensors.toString());
					conn.sendPacket(test);
					
					//a normal message can be sent to the server but will create a problem with receiving acknowledgments 
					/*loginWithServer=new Message("JID of the server",Message.Type.normal);
					loginWithServer.setFrom(username);
					loginWithServer.setSubject("Sensor Capabilities");
					loginWithServer.setBody(deviceSensors.toString());
					conn.sendPacket(loginWithServer);			//sends a normal message to the customServer containing the sensor capabilities
					*/
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				}
		
			else
			{
				System.out.println("Server does not support new account creation");
				showDialog.setMessage("Account cannot be created on the server")
				.create()
				.show();						
			}
		}
		else
			{
			System.out.println("not connected to the server");
			showDialog.setMessage("Not connected to the server")
			.create()
			.show();
			}
	
	}
	
	@SuppressLint("ShowToast")
	public void createUserName()
	{		
		String userName;
		String UNIQUE_ID;
		TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
		UNIQUE_ID=  mngr.getDeviceId();
		userName=UNIQUE_ID.concat("@serverName");
		editPrefs.putString("username", userName).commit();
		editPrefs.putString("password", UNIQUE_ID).commit();
		System.out.println("username created: "+userName);
		System.out.println("password is: "+UNIQUE_ID);
		System.out.println("Proceeding to registeration");
		showDialog
			.setTitle("Installating app...")
			.setMessage("Username and password created");
			
	}
	
	public void launchIntent(View v)
	{
		Intent i=new Intent(getApplicationContext(),PublishQuery.class);		
		startActivity(i);
	}
}
