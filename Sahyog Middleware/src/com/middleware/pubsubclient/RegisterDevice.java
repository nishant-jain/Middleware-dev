package com.middleware.pubsubclient;

import java.io.File;
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StrictMode;
import android.telephony.TelephonyManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class RegisterDevice
	{
		static final String serverIP = "103.25.231.23";
		static int portNo = 5222;
		public static String username;
		public static String password;
		public static final String PREFS_NAME = "Preferences_File";
		static ConnectionConfiguration config;
		public static XMPPConnection conn;
		public static AccountManager am;
		static SharedPreferences chkInstall;
		static SharedPreferences.Editor editPrefs;
		SensorManager sm;
		public static List<Sensor> deviceSensors;
		AlertDialog.Builder showDialog;
		static Message loginWithServer;
		static boolean accountExists;
		public static JSONObject obj;
		Intent intentAcc;
		Intent intentAR;
		// public static Intent intentRequestListener;
		Intent intentPublishQuery;

		static AlarmReceiver alarmReceiver;
		static AlarmReceiverStop alarmReceiverStop;

		static Double latitude = 0.0;
		static Double longitude = 0.0;

		public static void register(Context appContext, SensorManager sm,
				ConnectivityManager cm, TelephonyManager tm)
			{
				XMPPConnection.DEBUG_ENABLED = true;
				deviceSensors = sm.getSensorList(Sensor.TYPE_ALL);
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);

				System.out.println("Establishing connection with server");
				config = new ConnectionConfiguration(serverIP, portNo);
				config.setDebuggerEnabled(true);
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
						e.printStackTrace();
					}

				JSONArray array3 = new JSONArray();
				try
					{
						array3.put(latitude);
						array3.put(longitude);
						array3.put(0);
						array3.put(0);
						obj.put("Location", array3);
					}
				catch (JSONException e)
					{
						e.printStackTrace();
					}

				System.out.println("Checking for play services");
				int status = GooglePlayServicesUtil
						.isGooglePlayServicesAvailable(appContext);
				boolean actRecogException = false;

				try
					{
						if (status == ConnectionResult.SUCCESS)
							{
								System.out.println("Play services present");
								obj.put("ActivityRecognition", "present");
								JSONArray array5 = new JSONArray();
								try
									{
										array5.put("activity");
										array5.put(0);
										array5.put(0);
										array5.put(0);
										array5.put(0);
										obj.put("sensor" + (count++), array5);
									}
								catch (JSONException e)
									{
										e.printStackTrace();
									}
							}
						else
							{
								obj.put("ActivityRecognition", "absent");
							}
					}
				catch (JSONException e)
					{
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
								e.printStackTrace();
							}
					}

				try
					{
						obj.put("DownloadAllowed", "yes");

					}
				catch (JSONException e2)
					{
						e2.printStackTrace();
					}

				try
					{
						obj.put("noSensors", obj.length() - 3);
					}
				catch (JSONException e)
					{
						e.printStackTrace();
					}
				if (isNetworkAvailable(cm))
					{
						try
							{
								conn.connect();
								System.out.println("Connection Established");
								am = conn.getAccountManager();
							}
						catch (XMPPException e1)
							{
								e1.printStackTrace();
							}
					}
				else
					{
						System.out.println("No internet Connection");
					}

				chkInstall = appContext.getSharedPreferences(PREFS_NAME, 0);
				editPrefs = chkInstall.edit();
				boolean installing = chkInstall.getBoolean("firstInstall", true);
				if (installing)
					{
						System.out.println("installing application");
						createUserName(tm);
						registerClient(cm);
					}

				else
					{
						loginToServer();
					}

				alarmReceiver = new AlarmReceiver();
				appContext.registerReceiver(alarmReceiver,
						new IntentFilter("dataRequest"));

				alarmReceiverStop = new AlarmReceiverStop();
				appContext.registerReceiver(alarmReceiverStop, new IntentFilter(
						"dataStopRequest"));
			}

		public static JSONObject getAllPoosibleTopics()
			{
				return obj;
			}

		public static void loginToServer()
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
							}
						catch (XMPPException e)
							{
								e.printStackTrace();
							}
					}
				else
					{
						System.out.println("Not connected to the server");
					}
			}

		void disconnectConnection()
			{
				conn.disconnect();
			}

		private static boolean isNetworkAvailable(ConnectivityManager connectivityManager)
			{
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

		public static void registerClient(ConnectivityManager cm)
			{
				if (isNetworkAvailable(cm) && conn.isConnected())
					{

						if (am.supportsAccountCreation())
							{
								try
									{
										username = chkInstall.getString("username", null);
										password = chkInstall.getString("password", null);
										am.createAccount(username, password);
										loginToServer();

										loginWithServer = new Message("server@"
												+ serverIP, Message.Type.normal);
										loginWithServer.setSubject("Sensor Capabilities");
										loginWithServer.setBody(obj.toString());
										conn.sendPacket(loginWithServer);
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
								System.out
										.println("Account cannot be created on server.");
							}
					}

				else
					{
						System.out.println("not connected to the server");

					}

			}

		public static void createUserName(TelephonyManager mngr)
			{
				String userName;
				String UNIQUE_ID;
				UNIQUE_ID = mngr.getDeviceId();
				userName = UNIQUE_ID;
				editPrefs.putString("username", userName).commit();
				editPrefs.putString("password", UNIQUE_ID).commit();

			}

	}
