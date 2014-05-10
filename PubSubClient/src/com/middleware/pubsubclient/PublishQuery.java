package com.middleware.pubsubclient;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jivesoftware.smack.packet.Message;
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

public class PublishQuery extends Activity
	{

		private Spinner deviceCount, selectActivity;
		private EditText minCount, sensDelay;
		public EditText fromDate, fromTime, toDate, toTime, expiryDate, expiryTime, max,
				latitude, longitude;
		private CheckBox accelerometer, gps, gyroscope, microphone;
		SimpleDateFormat df;
		AlertDialog.Builder showMessage;
		// public EditText lat,longi,locName;
		EditText locName;
		private Button getCurrentLocation;
		LocationManager locationManager = null;
		MyListener ml;
		Geocoder geocoder;
		List<Address> addresses;
		JSONObject query;
		Button publish;
		Date fromDt;
		public static String queryNoAcc, queryNoGPS, queryNoGyr, queryNoMicro;
		boolean validity;

		@Override
		public void onDestroy()
			{
				super.onDestroy();
				locationManager.removeUpdates(ml);
			}

		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.layout_publish_query);

				validity = true;
				deviceCount = (Spinner) findViewById(R.id.Spinner02);
				minCount = (EditText) findViewById(R.id.editText6);
				max = (EditText) findViewById(R.id.EditText06);
				accelerometer = (CheckBox) findViewById(R.id.checkBox1);
				gps = (CheckBox) findViewById(R.id.CheckBox03);
				gyroscope = (CheckBox) findViewById(R.id.CheckBox01);
				microphone = (CheckBox) findViewById(R.id.CheckBox02);
				fromDate = (EditText) findViewById(R.id.editText1);
				fromTime = (EditText) findViewById(R.id.editText2);
				toDate = (EditText) findViewById(R.id.EditText01);
				toTime = (EditText) findViewById(R.id.EditText02);
				expiryDate = (EditText) findViewById(R.id.EditText03);
				expiryTime = (EditText) findViewById(R.id.EditText04);
				latitude = (EditText) findViewById(R.id.editText3);
				longitude = (EditText) findViewById(R.id.EditText05);
				selectActivity = (Spinner) findViewById(R.id.spinner1);
				sensDelay = (EditText) findViewById(R.id.editText7);
				df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

				// to be commented later

				DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
				DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
				Date date = new Date();
				String curDate = dateFormat.format(date);
				String curTime = dateFormat2.format(date);
				fromDate.setText(curDate);
				toDate.setText(curDate);
				expiryDate.setText(curDate);
				fromTime.setText(curTime);
				toTime.setText(curTime);
				expiryTime.setText(curTime);

				getCurrentLocation = (Button) findViewById(R.id.button2);
				publish = (Button) findViewById(R.id.button1);
				// lat = (EditText)findViewById(R.id.editText3);
				// longi = (EditText)findViewById(R.id.EditText05);
				locName = (EditText) findViewById(R.id.editText4);
				max = (EditText) findViewById(R.id.EditText06);
				locationManager = (LocationManager) this
						.getSystemService(Context.LOCATION_SERVICE);
				ml = new MyListener();
				geocoder = new Geocoder(this, Locale.getDefault());
				getCurrentLocation.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View arg0)
							{
								locationManager.requestLocationUpdates(
										LocationManager.GPS_PROVIDER, 0, 0, ml);
								locationManager.requestLocationUpdates(
										LocationManager.NETWORK_PROVIDER, 0, 0, ml);
								locationManager.requestLocationUpdates(
										LocationManager.PASSIVE_PROVIDER, 0, 0, ml);
								getCurrentLocation.setText("Finding Location..");
							}
					});

				publish.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View arg0)
							{
								sendQuery(arg0);
							}
					});

				deviceCount.setOnItemSelectedListener(new OnItemSelectedListener()
					{
						@Override
						public void onItemSelected(AdapterView<?> arg0, View arg1,
								int pos, long arg3)
							{
								if (pos == 1)// Exact
									{
										minCount.setHint("Count");
										max.setVisibility(View.INVISIBLE);
									}
								else
									// Range
									{
										minCount.setHint("Min");
										max.setVisibility(View.VISIBLE);
									}
							}

						@Override
						public void onNothingSelected(AdapterView<?> arg0)
							{
							}

					});
			}

		public void onDeviceCountChange()
			{

			}

		public List<JSONObject> generatePayload() throws JSONException
			{
				String sensors;
				int delay;
				Long fromEpoch, toEpoch, expiryEpoch;
				Double lat, lon;
				String activity = "";
				int gpsDelay, countMin = 1, countMax = 1;
				StringBuilder sensorName = new StringBuilder("");

				fromDt = new Date();
				try
					{
						fromDt = df.parse(fromDate.getText().toString() + " "
								+ fromTime.getText().toString());
					}
				catch (ParseException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				// fromTime.
				fromEpoch = fromDt.getTime(); // converts the local time stamp
				// to epoch
				// timestamp

				Date toDt = new Date();
				try
					{
						toDt = df.parse(toDate.getText().toString() + " "
								+ toTime.getText().toString());
					}
				catch (ParseException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				toEpoch = toDt.getTime();

				Date expiryDt = new Date();
				try
					{
						expiryDt = df.parse(expiryDate.getText().toString() + " "
								+ expiryTime.getText().toString());
					}
				catch (ParseException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				expiryEpoch = expiryDt.getTime();

				if (fromEpoch > toEpoch)
					{
						validity = false;
					}

				if (fromEpoch > expiryEpoch)
					{
						validity = false;
					}

				if (toEpoch > expiryEpoch)
					{
						validity = false;
					}

				lat = Double.parseDouble(latitude.getText().toString());
				lon = Double.parseDouble(longitude.getText().toString());
				activity = (String) selectActivity.getSelectedItem();
				delay = Integer.parseInt(sensDelay.getText().toString());
				countMin = Integer.parseInt(minCount.getText().toString());
				if (deviceCount.getSelectedItem().toString().compareTo("Range") == 0)
					{
						countMax = Integer.parseInt(max.getText().toString());
						if (countMin > countMax)
							{
								validity = false;
							}
					}
				else
					{
						countMax = countMin;
					}
				List<JSONObject> all = new ArrayList<JSONObject>();

				if (accelerometer.isChecked())
					{
						queryNoAcc = RegisterMe.username + System.nanoTime();
						// sensorName.append("Accelerometer,");
						query = new JSONObject();
						query.put("username", RegisterMe.username);
						query.put("queryNo", queryNoAcc);
						query.put("dataReqd", "Accelerometer");
						query.put("fromTime", fromEpoch);
						query.put("toTime", toEpoch);
						query.put("expiryTime", expiryEpoch);
						query.put("latitude", lat);
						query.put("longitude", lon);
						query.put("activity", activity);
						query.put("frequency", delay);
						query.put("countMin", countMin);
						query.put("countMax", countMax);
						all.add(query);
					}
				if (gps.isChecked())
					{
						// sensorName.append("GPS,");
						queryNoGPS = RegisterMe.username + System.nanoTime();
						query = new JSONObject();
						query.put("username", RegisterMe.username);
						query.put("queryNo", queryNoGPS);
						query.put("dataReqd", "GPS");
						query.put("fromTime", fromEpoch);
						query.put("toTime", toEpoch);
						query.put("expiryTime", expiryEpoch);
						query.put("latitude", lat);
						query.put("longitude", lon);
						query.put("activity", activity);
						query.put("frequency", delay);
						query.put("countMin", countMin);
						query.put("countMax", countMax);
						all.add(query);
					}
				if (gyroscope.isChecked())
					{
						// queryNo = RegisterMe.username +
						// System.currentTimeMillis();
						// sensorName.append("Gyroscope,");
						queryNoGyr = RegisterMe.username + System.nanoTime();
						query = new JSONObject();
						query.put("username", RegisterMe.username);
						query.put("queryNo", queryNoGyr);
						query.put("dataReqd", "Gyroscope");
						query.put("fromTime", fromEpoch);
						query.put("toTime", toEpoch);
						query.put("expiryTime", expiryEpoch);
						query.put("latitude", lat);
						query.put("longitude", lon);
						query.put("activity", activity);
						query.put("frequency", delay);
						query.put("countMin", countMin);
						query.put("countMax", countMax);
						all.add(query);
					}
				if (microphone.isChecked())
					{
						// sensorName.append("Rotation Vector,");
						queryNoMicro = RegisterMe.username + System.nanoTime();
						query = new JSONObject();
						query.put("username", RegisterMe.username);
						query.put("queryNo", queryNoMicro);
						query.put("dataReqd", "Microphone");
						query.put("fromTime", fromEpoch);
						query.put("toTime", toEpoch);
						query.put("expiryTime", expiryEpoch);
						query.put("latitude", lat);
						query.put("longitude", lon);
						query.put("activity", activity);
						query.put("frequency", delay);
						query.put("countMin", countMin);
						query.put("countMax", countMax);
						all.add(query);
					}
				/*sensors = sensorName.toString().substring(0,
								sensorName.toString().length() - 1);
						System.out.println(sensors);
						query = new JSONObject();
						query.put("username", RegisterMe.username);
						query.put("queryNo", queryNo);
						query.put("dataReqd", sensors);
						query.put("fromTime", fromEpoch);
						query.put("toTime", toEpoch);
						query.put("expiryTime", expiryEpoch);
						query.put("latitude", lat);
						query.put("longitude", lon);
						query.put("activity", activity);
						query.put("frequency", delay);
						query.put("countMin", countMin);
						query.put("countMax", countMax);
						System.out.println(query.toString());
				 */
				return all;
			}

		public void sendQuery(View v)
			{

				showMessage = new Builder(this);

				try
					{
						List<JSONObject> query = generatePayload();
						if (fromDt.getTime() > System.currentTimeMillis())
							{
								if (validity)
									{
										// PubSubManager mgr = new
										// PubSubManager(RegisterMe.conn);
										Iterator<JSONObject> traverse = query.iterator();
										while (traverse.hasNext())
											{
												Message query2 = new Message(
														"server@103.25.231.23",
														Message.Type.chat);
												query2.setSubject("Query");
												query2.setBody(traverse.next().toString());
												// System.out.println(query2.getBody());
												RegisterMe.conn.sendPacket(query2);
											}
										showMessage.setTitle("Query Submission")
												.setMessage("Successful").create().show();
										// System.out.println("Sensors selected:"+
										// query.size());
										/*try {
															Message query2 = new Message("server@103.25.231.23",
															Message.Type.chat);
													query2.setSubject("Query");
													query2.setBody(query.toString());
												//	RegisterMe.conn.sendPacket(query2);
													showMessage.setTitle("Query Submission")
															.setMessage("Successful").create().show();

												} catch (Exception e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}*/
									}
								else
									{
										showMessage.setTitle("Error!")
												.setMessage("Invalid fields!").create()
												.show();
									}
							}
						else
							{
								showMessage.setTitle("Error!")
										.setMessage("Starting Time already passed!")
										.create().show();

							}
					}
				catch (Exception e)
					{
						showMessage.setTitle("Error!")
								.setMessage("Please ensure that data entered is valid")
								.create().show();
					}

			}

		class MyListener implements LocationListener
			{
				@Override
				public void onLocationChanged(Location location)
					{
						// Toast.makeText(getApplicationContext(),
						// ""+location.getLatitude(),
						// Toast.LENGTH_SHORT).show();
						latitude.setText("" + location.getLatitude());
						longitude.setText("" + location.getLongitude());

						try
							{
								addresses = geocoder.getFromLocation(
										location.getLatitude(), location.getLongitude(),
										1);
								String address = addresses.get(0).getAddressLine(0);
								String city = addresses.get(0).getAddressLine(1);
								String country = addresses.get(0).getAddressLine(2);
								locName.setText(address + " " + city + " " + country);
								getCurrentLocation.setText("Get Current Location");
							}
						catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						locationManager.removeUpdates(ml);
					}

				@Override
				public void onProviderDisabled(String arg0)
					{
					}

				@Override
				public void onProviderEnabled(String arg0)
					{
						// TODO Auto-generated method stub

					}

				@Override
				public void onStatusChanged(String arg0, int arg1, Bundle arg2)
					{
						// TODO Auto-generated method stub

					}

			}

	}
