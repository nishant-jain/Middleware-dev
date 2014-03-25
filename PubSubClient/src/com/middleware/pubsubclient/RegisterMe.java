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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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


@SuppressLint("UseValueOf")
public class RegisterMe extends Activity{
	
	public static String username;
	public String password;
	public static final String PREFS_NAME = "Preferences_File";
	ConnectionConfiguration config;
	public static XMPPConnection conn;
	public static AccountManager am;
	SharedPreferences chkInstall;
	SharedPreferences.Editor editPrefs;
	SensorManager sm;
	//TextView tv;
	public static List<Sensor> deviceSensors;
	AlertDialog.Builder showDialog;
	Message loginWithServer;
	boolean accountExists;
	public static JSONObject obj;
	
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
						
		System.out.println("Establishing connection with server");
		config=new ConnectionConfiguration("103.25.231.23",5222);
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
		
		obj=new JSONObject();
		for(Sensor s : deviceSensors)
		{
			JSONArray array=new JSONArray();
			try {
				array.put(s.getMaximumRange());
				array.put(s.getMinDelay());
				array.put((Number)s.getPower());
				array.put((Number)s.getResolution());
				obj.put(findType(s.getType()),array);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		try {
			obj.put("noSensors", obj.length());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(obj.toString());
		if(isNetworkAvailable())
		{
			try {		
			conn.connect();
			System.out.println("Connection Established");
			am=conn.getAccountManager();
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
			loginToServer();
		}
	}
	
	public void loginToServer()
	{
		if(conn.isConnected())
		{
		System.out.println("trying to login");
		try {
			
			username=chkInstall.getString("username", null);
			password=chkInstall.getString("password", null);
			conn.login(username	,password);
			System.out.println("login successful");
			showDialog.setTitle("Login successful")
			.setMessage("connected to the server")
			.create()
			.show();
			
			
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			showDialog.setTitle("Login failed")
			.setMessage("Unable to login...Make sure you are registered with the server")
			.create()
			.show();
			
		}
		}
		else
		{
			System.out.println("Not connected to the server");
		}
	}
	
	protected void onDestroy()
	{
		super.onDestroy();
		conn.disconnect();
		System.out.println("Connection terminated");
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
        case R.id.action_settings:
        	Intent i2=new Intent(getApplicationContext(),Settings.class);
        	startActivity(i2);
        	break;
		}
		return false;
		
	}
	
	
	public String findType(int i)
	{
		switch(i)
		{
		case 1:
			return "Accelerometer";
		case 2:
			return "Magnetic Field";
		case 3:
			return "Orientation";
		case 4:
			return "Gyroscope";
		case 5:
			return "Light";
		case 6:
			return "Pressure";
		case 7:
			return "Temperature";
		case 8:
			return "Proximity";
		case 9:
			return "Gravity";
		case 10:
			return "Linear Acceleration";
		case 11:
			return "Rotation Vector";
		case 12:
			return "Relative Humidity";
		case 13:
			return "Ambient Temperature";
		case 14:
			return "Uncalibrated Magnetic Field";
		case 15:
			return "Game Rotation Vector";
		case 16:
			return "Uncalibrated Gyroscope";
		case 17:
			return "Significant motion";
		case 18:
			return "Step Detector";
		case 19:
			return "Step Counter";
		case 20:
			return "Geomagnetic Rotation Vector";
		default:
			return "Type unknown";
		}
		
	}

	@SuppressLint("ShowToast")
	public void registerClient()
	{			
		if(isNetworkAvailable() && conn.isConnected())
		{
			
			if(am.supportsAccountCreation())
				{
				System.out.println("Server Supports new account creation");
				try{
					username=chkInstall.getString("username", null);
					password=chkInstall.getString("password",null);
					am.createAccount(username, password);		//creates an account with the XMPP server
					loginToServer();
					
					loginWithServer=new Message("server@103.25.231.23",Message.Type.normal);
					loginWithServer.setSubject("Sensor Capabilities");
					loginWithServer.setBody(obj.toString());
					conn.sendPacket(loginWithServer);			//sends a normal message to the customServer containing the sensor capabilities
					showDialog.setMessage("Sensor information sent to the server (No acknowledgement received")
					.create()
					.show();
					
				}
				catch(Exception e)
				{
					accountExists=true;
					if(accountExists)
						loginToServer();
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
		//userName=UNIQUE_ID.concat("@serverName");
		userName=UNIQUE_ID;
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
