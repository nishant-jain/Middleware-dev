package com.middleware.pubsubclient;

import java.io.File;
import java.util.Collection;
import java.util.List;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PubSubManager;


public class RegisterMe extends Activity{
	
	public String username;
	public String password;
	public static final String PREFS_NAME = "Preferences_File";
	ConnectionConfiguration config;
	XMPPConnection conn;
	SharedPreferences chkInstall;
	SharedPreferences.Editor editPrefs;
	SensorManager sm;
			
	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_me);
		
		//Establish connection with the XMPP server: Network tasks take place in background so either implement using AsyncTask or change the thread policy
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
						
		System.out.println("Establishing connection with gtalk server");
		config=new ConnectionConfiguration("talk.google.com",5222,"gmail.com");
		//config=new ConnectionConfiguration("jabber.org",5222);
		conn=new XMPPConnection(config);
								
		try {
				//since android does not support BKS security implementation after icecream sandwich, change it if higher version
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
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG);
				e.printStackTrace();
				
		}
		
		//connect to the gmail server
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
			//if it is a first time installation, then the user nneds to register with the server
			createUserName();		//userName and password generated
			registerClient();		//registering client when the apk is installed
		}
		else
		{
			//else the user simply logins with his credentials and he does not need to remember the details.
			//The details will be saved in the form of shared preferences.
			
			//apk already installed
			//establish connection
			
			System.out.println("trying to login");
			//login using credentials
			try {
				username=chkInstall.getString("username", null);
				password=chkInstall.getString("password", null);
				conn.login(username	,password);
				System.out.println("login successful");
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//TODO code to subscribe to certain topics and publish queries
			//how to create a subscription node with the pubsub service with required configurations
			//still working on this part
			/*
			PubSubManager psm=new PubSubManager(conn);
			try {
				LeafNode ln=psm.getNode("id of the node in the server");
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			
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

	@SuppressLint("ShowToast")
	public void registerClient()
	{			
		if(isNetworkAvailable() && conn.isConnected())
		//{
		//if(conn.isConnected())
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
					//get the list of all sensors present on the device
					//List<Sensor> sensors=sm.getSensorList(Sensor.TYPE_ALL);
					
					username=chkInstall.getString("username", null);
					password=chkInstall.getString("password",null);
					//List<Sensor> sensors=sm.getSensorList(Sensor.TYPE_ALL);
					//System.out.println(sensors.toString());
					am.createAccount(username, password);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				}
		
			else
			{
				//List<Sensor> sensors=sm.getSensorList(Sensor.TYPE_ALL);
				
				System.out.println("Server does not support new account creation");
			}
		}
		else
			System.out.println("not connected to the server");
		/*}
		else
		{
			System.out.println("Can't register...no internet connection");
		}*/
		
			
		//establishConn ecs=new establishConn();
		//ecs.doInBackground();
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
		//SharedPreferences.Editor = 
	}
	
	public void classFromXML(View v)
	{
		DefaultPacketExtension dpf=new DefaultPacketExtension("queryFormat", "http://justtrying.com");
		System.out.println("Element name is "+ dpf.getElementName());		
	}

}
