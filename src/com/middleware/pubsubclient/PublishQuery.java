package com.middleware.pubsubclient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.SimplePayload;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class PublishQuery extends Activity {

	private Spinner deviceCount,selectActivity,sensDelay;
	private EditText minCount;
	private EditText fromDate,fromTime,toDate,toTime,expiryDate,expiryTime,max,latitude,longitude;
	private CheckBox accelerometer,gps,gyroscope,rotation;
	SimpleDateFormat df;
	AlertDialog.Builder showMessage;
		
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
	
	public String generatePayload()
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
				+ "</json>";
		
		System.out.println(query);
		return query;
	}
	
	public void sendQuery(View v)
	{
		
		showMessage = new Builder(this);
		try
		{
	   String query=generatePayload();
		
	   PubSubManager mgr=new PubSubManager(RegisterMe.conn);
		
		try {
			showMessage.setTitle("Query Submission")
			.setMessage("Not working")
			.create()
			.show();
			
			//ItemId is the query number
			/*LeafNode testNode=mgr.createNode("testing");
			testNode.sendConfigurationForm(NodeConfig.setNodeConfig());			
			System.out.println("Test node created");			
			testNode.send(new PayloadItem<SimplePayload>(RegisterMe.username+System.currentTimeMillis(), new SimplePayload("query", "pubsub:client:query", query)));
			showMessage.setTitle("Query Submission")
			.setMessage("Successful")
			.create()
			.show();
			*/
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
	
}
