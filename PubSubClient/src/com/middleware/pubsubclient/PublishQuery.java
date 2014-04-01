package com.middleware.pubsubclient;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class PublishQuery extends Activity {

	private Spinner deviceCount,selectActivity,sensDelay;
	private EditText minCount;
	public EditText fromDate,fromTime,toDate,toTime,expiryDate,expiryTime,max,latitude,longitude;
	private CheckBox accelerometer,gps,gyroscope,rotation;
	SimpleDateFormat df;
	AlertDialog.Builder showMessage;
	//public EditText lat,longi,locName;
	EditText locName;
	private Button getCurrentLocation;
	LocationManager locationManager=null;
	MyListener ml;
	Geocoder geocoder;
	List<Address> addresses;
	JSONObject query;
	Button publish;
		
	@Override
	public void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(ml);
	}
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_publish_query);
		
		deviceCount = (Spinner)findViewById(R.id.Spinner02);
		minCount = (EditText)findViewById(R.id.editText6);
		max = (EditText)findViewById(R.id.EditText06);
		accelerometer=(CheckBox)findViewById(R.id.checkBox1);
		gps=(CheckBox)findViewById(R.id.CheckBox03);
		gyroscope=(CheckBox)findViewById(R.id.CheckBox01);
		rotation=(CheckBox)findViewById(R.id.CheckBox02);
		fromDate=(EditText)findViewById(R.id.editText1);
		fromTime=(EditText)findViewById(R.id.editText2);
		toDate=(EditText)findViewById(R.id.EditText01);
		toTime=(EditText)findViewById(R.id.EditText02);
		expiryDate=(EditText)findViewById(R.id.EditText03);
		expiryTime=(EditText)findViewById(R.id.EditText04);
		latitude=(EditText)findViewById(R.id.editText3);
		longitude=(EditText)findViewById(R.id.EditText05);
		selectActivity=(Spinner)findViewById(R.id.spinner1);
		sensDelay=(Spinner)findViewById(R.id.Spinner01);		
		df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); 
		
		//to be commented later
		fromDate.setText("01-04-2014");
		toDate.setText("05-04-2014");
		expiryDate.setText("20-04-2014");
		fromTime.setText("00:00:00");
		toTime.setText("00:00:00");
		expiryTime.setText("00:00:00");
		
		getCurrentLocation = (Button)findViewById(R.id.button2);
		publish = (Button)findViewById(R.id.button1);
		//lat = (EditText)findViewById(R.id.editText3);
		//longi = (EditText)findViewById(R.id.EditText05);
		locName = (EditText)findViewById(R.id.editText4);
		max = (EditText)findViewById(R.id.EditText06);
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		ml = new MyListener();
		geocoder = new Geocoder(this, Locale.getDefault());
		getCurrentLocation.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ml);
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ml);
				locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, ml);
				getCurrentLocation.setText("Finding Location..");
			}
		});
		
		publish.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				sendQuery(arg0);
			}
		});
		
		deviceCount.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int pos, long arg3) {
				if(pos==1)//Exact
				{
					minCount.setHint("Count");
					max.setVisibility(View.INVISIBLE);
				}
				else//Range
				{
					minCount.setHint("Min");
					max.setVisibility(View.VISIBLE);
				}
		}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
			
		});
	}
	
	public void onDeviceCountChange()
	{
		
	}
	
	public JSONObject generatePayload() throws JSONException
	{
		String sensors,delay;
		Long fromEpoch,toEpoch,expiryEpoch;
		Double lat,lon;
		String activity="";
		int gpsDelay,countMin=1,countMax=1;
		StringBuilder sensorName=new StringBuilder("");
		if(accelerometer.isChecked())
			sensorName.append("Accelerometer,");
		if(gps.isChecked())
			sensorName.append("GPS,");
		if(gyroscope.isChecked())
			sensorName.append("Gyroscope,");
		if(rotation.isChecked())
			sensorName.append("Rotation Vector");
		sensors=sensorName.toString();
		System.out.println(sensors);
		
		Date fromDt=new Date();
		try {
			fromDt = df.parse(fromDate.getText().toString() + " "+fromTime.getText().toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//fromTime.
		fromEpoch=fromDt.getTime();	//converts the local time stamp to epoch timestamp
		
		Date toDt=new Date();
		try {
			toDt = df.parse(toDate.getText().toString() + " "+toTime.getText().toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		toEpoch=toDt.getTime();	
		
		Date expiryDt=new Date();
		try {
			expiryDt = df.parse(expiryDate.getText().toString() + " "+expiryTime.getText().toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		expiryEpoch=expiryDt.getTime();	
		
		lat=Double.parseDouble(latitude.getText().toString());
		lon=Double.parseDouble(longitude.getText().toString());
		activity=(String) selectActivity.getSelectedItem();
		delay=sensDelay.getSelectedItem().toString();
		countMin=Integer.parseInt(minCount.getText().toString());
		if(deviceCount.getSelectedItem().toString().compareTo("Range")==0)
			countMax=Integer.parseInt(max.getText().toString());
		else
			countMax=countMin;
		/*String query="<sensorNames>"+sensors+"</sensorNames>"
					+"<fromTime>"+fromEpoch+"</fromTime>"
					+"<toTime>"+toEpoch+"</toTime>"
					+"<expiryTime>"+expiryEpoch+"</expiryTime>"
					+"<location>"
						+ "<latitude>"+lat+"</latitude>"
							+ "<longitude>"+lon+"</longitude>"
					+ "</location>"
					+"<activity>"+activity+"</activity>"
					+"<frequency>"+delay+"</frequency>"
					+"<count>"
						+"<countMin>"+countMin+"</countMin>"
						+"<countMax>"+countMax+"</countMax>"
					+"</count>";
		System.out.println(query);*/
		
		query = new JSONObject();
		query.put("username", RegisterMe.username);
		query.put("dataReqd",sensors);
		query.put("fromTime", fromEpoch);
		query.put("toTime", toEpoch);
		query.put("expiryTime", expiryEpoch);
		query.put("location", lat);
		query.put("longitude",lon);
		query.put("activity", activity);
		query.put("frequency",delay);
		query.put("countMin", countMin);
		query.put("countMax", countMax);
		/*
		String query="<json xmlns=\"urn:xmpp:json:0\">"
						+ "{"
				+"\"username\":"+"\""+RegisterMe.username+"\""+","
				+"\"dataReqd\":"+ "\"" + sensors + "\"" +","
				+ "\"fromTime\":" + "\"" + fromEpoch + "\"" +","
				+ "\"toTime\":" + "\""+ toEpoch+ "\"" +","
				+ "\"expiryTime\":" + "\""+ expiryEpoch+ "\"" +","
				+ "\"location\":{\"latitude\":"+ "\""+lat+ "\""+",\"longitude\":"+ "\""+lon+ "\""+"},"
				+ "\"activity\":"+ "\""+activity+ "\""+","
				+ "\"frequency\":"+ "\""+delay+ "\""+","
				+ "\"count\":{\"countMin\":"+ "\""+countMin+ "\""+",\"countMax\":"+ "\""+countMax+ "\""+"}"
				+"}"
				+ "</json>";*/
				
		System.out.println(query.toString());
		return query;
	}
	
	public void sendQuery(View v)
	{
		
		showMessage = new Builder(this);
		try
		{
	   JSONObject query=generatePayload();
		
	   PubSubManager mgr=new PubSubManager(RegisterMe.conn);
		
		try {
		/*	showMessage.setTitle("Query Submission")
			.setMessage("Not working")
			.create()
			.show();*/
			
			//ItemId is the query number
			/*LeafNode testNode=mgr.createNode(""+System.currentTimeMillis());
			testNode.sendConfigurationForm(NodeConfig.setNodeConfig());			
			System.out.println("Test node created");			
			testNode.send(new PayloadItem<SimplePayload>(RegisterMe.username+System.currentTimeMillis(), new SimplePayload("query", "pubsub:client:query", "<book xmlns='pubsub:test:book'><title>Lord of the Rings</title></book>")));*/
			
			Message query2 = new Message("server@103.25.231.23",Message.Type.normal);
			query2.setSubject("query");
			query2.setBody(query.toString());
			RegisterMe.conn.sendPacket(query2);	
			showMessage.setTitle("Query Submission")
			.setMessage("Successful")
			.create()
			.show();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		catch(Exception e)
		{
			showMessage.setTitle("Error!")
			.setMessage("Please ensure that data entered is valid")
			.create()
			.show();
		}
		
		
		}
	
	class MyListener implements LocationListener
	{
		@Override
		public void onLocationChanged(Location location) {
			//Toast.makeText(getApplicationContext(), ""+location.getLatitude(),
				//	Toast.LENGTH_SHORT).show();
			latitude.setText(""+location.getLatitude());
			longitude.setText(""+location.getLongitude());
			
			try {
				addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
				String address = addresses.get(0).getAddressLine(0);
				String city = addresses.get(0).getAddressLine(1);
				String country = addresses.get(0).getAddressLine(2);
				locName.setText(address+" "+city+" "+country);
				getCurrentLocation.setText("Get Current Location");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			locationManager.removeUpdates(ml);
		}

		@Override
		public void onProviderDisabled(String arg0) {
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

		}

	}

}
