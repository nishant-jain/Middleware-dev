package com.middleware.pubsubclient;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

@SuppressLint("UseValueOf")
public class RegisterMe extends Activity
	{

		public static String username;
		public String password;
		public static final String PREFS_NAME = "Preferences_File";
		ConnectionConfiguration config;
		public static XMPPConnection conn;
		public static AccountManager am;
		SharedPreferences chkInstall;
		SharedPreferences.Editor editPrefs;
		SensorManager sm;
		// TextView tv;
		public static List<Sensor> deviceSensors;
		AlertDialog.Builder showDialog;
		Message loginWithServer;
		boolean accountExists;
		public static JSONObject obj;
		Intent intentAcc;
		Intent intentAR;
		public static Intent intentRequestListener;
		Intent intentPublishQuery;

		File file;
		AlarmReceiver alarmReceiver;
		AlarmReceiverStop alarmReceiverStop;

		// Won't be needed later when recording would be done based on queries
		// from
		// server
		Button startRecording;
		Button stopRecording;

		Button stopListeningtoRequests;
		Button startListeningtoRequests;
		Button publishQueryButton;

		Button upload;

		@SuppressLint("ShowToast")
		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(null);
				setContentView(R.layout.activity_register_me);

				upload = (Button) findViewById(R.id.button5);
				upload.setOnClickListener(startUploading);

				startRecording = (Button) findViewById(R.id.button2);
				stopRecording = (Button) findViewById(R.id.button3);
				startRecording.setOnClickListener(startDataRecording);
				stopRecording.setOnClickListener(stopDataRecording);

				stopListeningtoRequests = (Button) findViewById(R.id.button4);
				stopListeningtoRequests.setOnClickListener(stopListening);

				// startListeningtoRequests.setOnClickListener(startListening);

				publishQueryButton = (Button) findViewById(R.id.button1);
				publishQueryButton.setOnClickListener(publishQuery);

				intentAcc = new Intent(RegisterMe.this, AccReadings.class);
				intentAR = new Intent(RegisterMe.this,
						ActivityRecognitionCallingService.class);
				intentRequestListener = new Intent(RegisterMe.this, RequestListener.class);
				intentPublishQuery = new Intent(getApplicationContext(),
						PublishQuery.class);

				file = new File(Environment.getExternalStorageDirectory()
						+ "/DataCollection/Experiment_04-09-14_10-40-21/acc.csv");

				XMPPConnection.DEBUG_ENABLED = true;
				showDialog = new Builder(this);
				// tv = (TextView)findViewById(R.id.textView1);
				// tv.setText("List of sensors in this phone:\n");
				sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
				deviceSensors = sm.getSensorList(Sensor.TYPE_ALL);
				// tv.append(deviceSensors.toString());

				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);

				System.out.println("Establishing connection with server");
				config = new ConnectionConfiguration("103.25.231.23", 5222);
				config.setDebuggerEnabled(true);
				// config=new ConnectionConfiguration("jabber.org",5222);
				conn = new XMPPConnection(config);
				try
					{
						config.setSASLAuthenticationEnabled(true);
						config.setCompressionEnabled(true);
						config.setSecurityMode(SecurityMode.enabled);

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
							{
								config.setTruststoreType("AndroidCAStore");
								config.setTruststorePassword(null);
								config.setTruststorePath(null);
							}
						else
							{
								config.setTruststoreType("BKS");
								String path = System
										.getProperty("javax.net.ssl.trustStore");
								if (path == null)
									{
										path = System.getProperty("java.home")
												+ File.separator + "etc" + File.separator
												+ "security" + File.separator
												+ "cacerts.bks";
									}
								config.setTruststorePath(path);
							}
					}

				catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();

					}

				obj = new JSONObject();
				int count = 1;
				for (Sensor s : deviceSensors)
					{
						JSONArray array = new JSONArray();
						try
							{
								array.put(findType(s.getType()));
								array.put(s.getMaximumRange());
								array.put(s.getMinDelay());
								array.put((Number) s.getPower());
								array.put((Number) s.getResolution());
								obj.put("sensor" + (count++), array);
							}
						catch (JSONException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

					}

				JSONArray array = new JSONArray();
				try
					{
						array.put("Microphone");
						array.put(0);
						array.put(0);
						array.put(0);
						array.put(0);
						obj.put("sensor" + (count++), array);
					}
				catch (JSONException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				JSONArray array2 = new JSONArray();
				try
					{
						array2.put("GPS");
						array2.put(0);
						array2.put(0);
						array2.put(0);
						array2.put(0);
						obj.put("sensor" + (count++), array2);
					}
				catch (JSONException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				System.out.println("Checking for play services");
				int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
				// /JSONArray array=new JSONArray();

				boolean actRecogException = false;

				try
					{
						if (status == ConnectionResult.SUCCESS)
							{
								System.out.println("Play services present");
								// array.put("present");
								obj.put("ActivityRecognition", "present");
							}
						else
							{
								obj.put("ActivityRecognition", "absent");
							}
					}
				catch (JSONException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						actRecogException = true;
					}

				if (actRecogException)
					{
						try
							{
								obj.put("ActivityRecognition", "absent");
							}
						catch (JSONException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}

				// DownloadAllowed for now by default
				try
					{
						obj.put("DownloadAllowed", "yes");
					}
				catch (JSONException e2)
					{
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}

				try
					{
						obj.put("noSensors", obj.length() - 2);
					}
				catch (JSONException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				// System.out.println(obj.toString());
				if (isNetworkAvailable())
					{
						try
							{
								conn.connect();
								System.out.println("Connection Established");
								am = conn.getAccountManager();
							}
						catch (XMPPException e1)
							{
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
					}
				else
					{
						System.out.println("No internet Connection");
					}

				chkInstall = getSharedPreferences(PREFS_NAME, 0);
				editPrefs = chkInstall.edit();
				boolean installing = chkInstall.getBoolean("firstInstall", true);
				// installing = true;
				if (installing)
					{
						System.out.println("installing application");
						createUserName();
						registerClient();
					}

				else
					{
						loginToServer();
					}

				alarmReceiver = new AlarmReceiver();
				this.registerReceiver(alarmReceiver, new IntentFilter("dataRequest"));

				alarmReceiverStop = new AlarmReceiverStop();
				this.registerReceiver(alarmReceiverStop, new IntentFilter(
						"dataStopRequest"));

				LocalBroadcastManager.getInstance(this).registerReceiver(
						mMessageReceiver, new IntentFilter("buildAlert"));

				LocalBroadcastManager.getInstance(this).registerReceiver(
						mMessageReceiver2, new IntentFilter("buildToGetPermissionAlert"));
			}

		private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
					{
						showDialog.setTitle("Message from server")
								.setMessage(intent.getStringExtra("toShow")).create()
								.show();
					}
			};

		private final BroadcastReceiver mMessageReceiver2 = new BroadcastReceiver()
			{
				@Override
				public void onReceive(Context context, Intent intent)
					{
						showDialog
								.setTitle("Sorry")
								.setMessage(
										"Not enough capable users. Query can't be fulfilled.")
								.create().show();
					}
			};

		OnClickListener publishQuery = new OnClickListener()
			{
				@Override
				public void onClick(View v)
					{
						startActivity(intentPublishQuery);
					}
			};

		OnClickListener startUploading = new OnClickListener()
			{

				@Override
				public void onClick(View v)
					{
						// TODO Auto-generated method stub
						UploadFile.upload(file, RegisterMe.this.getApplicationContext());
					}
			};
		OnClickListener startDataRecording = new OnClickListener()
			{
				@Override
				public void onClick(View v)
					{

						Date date = new Date();
						String mFileName = android.text.format.DateFormat.format(
								"MM-dd-yy_kk-mm-ss", date).toString();
						File directory = new File(new File(
								Environment.getExternalStorageDirectory()
								// + "/ReadingsAcc/");
										+ "/DataCollection/").getPath(), "Experiment_"
								+ mFileName + "/");

						if (!directory.exists())
							{
								directory.mkdirs();
							}
						startService(intentAcc);
						startService(intentAR);
					}
			};

		OnClickListener stopDataRecording = new OnClickListener()
			{
				@Override
				public void onClick(View v)
					{
						stopService(intentAcc);
						stopService(intentAR);
					}
			};

		OnClickListener startListening = new OnClickListener()
			{

				@Override
				public void onClick(View v)
					{
						// TODO Auto-generated method stub

						if (startService(intentRequestListener) != null)
							{
								showDialog.setMessage("Service already running");
							}
					}
			};

		OnClickListener stopListening = new OnClickListener()
			{
				@Override
				public void onClick(View v)
					{
						stopService(intentRequestListener);

					}
			};

		public void loginToServer()
			{
				if (conn.isConnected())
					{
						System.out.println("trying to login");
						try
							{

								username = chkInstall.getString("username", null);
								password = chkInstall.getString("password", null);
								conn.login(username, password);
								System.out.println("login successful");
								showDialog.setTitle("Login successful")
										.setMessage("connected to the server").create()
										.show();
								startService(intentRequestListener);

							}
						catch (XMPPException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
								showDialog
										.setTitle("Login failed")
										.setMessage(
												"Unable to login...Make sure you are registered with the server")
										.create().show();

							}
					}
				else
					{
						System.out.println("Not connected to the server");
					}
			}

		@Override
		protected void onDestroy()
			{
				super.onDestroy();
				conn.disconnect();
				System.out.println("Connection terminated");
			}

		private boolean isNetworkAvailable()
			{
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = connectivityManager
						.getActiveNetworkInfo();
				if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
					{
						return true;
					}
				else
					{
						return false;
					}
			}

		@Override
		public boolean onCreateOptionsMenu(Menu menu)
			{
				// Inflate the menu; this adds items to the action bar if it is
				// present.
				getMenuInflater().inflate(R.menu.register_me, menu);
				return true;

			}

		@Override
		public boolean onOptionsItemSelected(MenuItem item)
			{
				super.onOptionsItemSelected(item);

				if (item.getItemId() == R.id.subscription)
					{

						Intent i = new Intent(getApplicationContext(),
								SubscribeTopics.class);
						startActivity(i);
					}
				else if (item.getItemId() == R.id.action_settings)
					{
						Intent i2 = new Intent(getApplicationContext(), Settings.class);
						startActivity(i2);

					}
				return false;
			}

		public static String findType(int i)
			{
				switch (i)
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
				if (isNetworkAvailable() && conn.isConnected())
					{

						if (am.supportsAccountCreation())
							{
								// System.out.println("Server Supports new account creation");
								try
									{
										username = chkInstall.getString("username", null);
										password = chkInstall.getString("password", null);
										am.createAccount(username, password); // creates
																				// an
																				// account
																				// with
																				// the
																				// XMPP
																				// server
										loginToServer();

										loginWithServer = new Message(
												"server@103.25.231.23",
												Message.Type.normal);
										loginWithServer.setSubject("Sensor Capabilities");
										loginWithServer.setBody(obj.toString());
										conn.sendPacket(loginWithServer); // sends
																			// a
																			// normal
																			// message
																			// to
																			// the
																			// customServer
																			// containing
																			// the
																			// sensor
																			// capabilities

										showDialog
												.setMessage(
														"Sensor information sent to the server.")
												.create().show();
										// listeningForMessages();
									}
								catch (Exception e)
									{
										accountExists = true;
										if (accountExists)
											{
												loginToServer();
											}
									}
							}

						else
							{
								// System.out.println("Server does not support new account creation");
								showDialog
										.setMessage(
												"Account cannot be created on the server")
										.create().show();
							}
					}

				else
					{
						System.out.println("not connected to the server");

						showDialog.setMessage("Not connected to the server").create()
								.show();
					}

			}

		/*
		 * public void listeningForMessages() { PacketFilter filter = new
		 * AndFilter(new PacketTypeFilter(Message.class)); PacketCollector collector
		 * = conn.createPacketCollector(filter); int k=0; while (true && k<40000000)
		 * { Packet packet = collector.nextResult(); if (packet instanceof Message)
		 * { Message message = (Message) packet; if (message != null &&
		 * message.getBody() != null) System.out.println("Received message from " +
		 * packet.getFrom() + " : " + (message != null ? message.getBody() :
		 * "NULL")); showDialog.setMessage("Received message from server" + " : " +
		 * (message != null ? message.getBody() : "NULL")) .create() .show();
		 * 
		 * break; } k++; } }
		 */

		@SuppressLint("ShowToast")
		public void createUserName()
			{
				String userName;
				String UNIQUE_ID;
				TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				UNIQUE_ID = mngr.getDeviceId();
				// userName=UNIQUE_ID.concat("@serverName");
				userName = UNIQUE_ID;
				editPrefs.putString("username", userName).commit();
				// System.out.println(UNIQUE_ID);
				editPrefs.putString("password", UNIQUE_ID).commit();
				// System.out.println("username created: "+userName);
				// System.out.println("password is: "+UNIQUE_ID);
				// System.out.println("Proceeding to registeration");
				showDialog.setTitle("Installating app...").setMessage(
						"Username and password created");

			}

	}
