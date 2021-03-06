package com.middleware.pubsubclient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Sensor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SubscribeTopics extends PreferenceActivity
	{

		SharedPreferences preferences;
		String entries[];
		StringBuilder list;
		XMPPConnection conn;
		boolean sent;
		AlertDialog.Builder alertb;

		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				// preferences=getSharedPreferences(RegisterMe.PREFS_NAME, 0);
				preferences = PreferenceManager
						.getDefaultSharedPreferences(getBaseContext());
				conn = RegisterMe.conn;
				sent = false;
				addPreferencesFromResource(R.xml.subscriptions);

				MultiSelectListPreference topicList = (MultiSelectListPreference) findPreference("sensorList");
				topicList.setPersistent(true);
				topicList.setEnabled(true);
				topicList.setDefaultValue(true);
				if (topicList != null)
					{
						// Iterator check = RegisterMe.obj.keys();
						Iterator<Sensor> sensorNames = RegisterMe.deviceSensors
								.iterator();
						Iterator sensorKeys = RegisterMe.obj.keys();

						List<String> toDisplay = new ArrayList<String>();
						List<String> toSend = new ArrayList<String>();

						while (sensorNames.hasNext())
							{
								toDisplay.add(RegisterMe.findType(sensorNames.next()
										.getType()));

							}

						toDisplay.add("ActivityRecognition");
						toDisplay.add("DownloadAllowed");
						toDisplay.add("GPS");
						toDisplay.add("Microphone");

						entries = new String[toDisplay.size()];
						String entryValues[] = new String[toDisplay.size()];
						int i = 0;
						for (String category : toDisplay)
							{
								if (!category.equals("noSensors"))
									{
										entries[i] = category;
										entryValues[i] = Integer.toString(i);
										i++;
									}
							}
						topicList.setEntries(entries);
						topicList.setEntryValues(entryValues);
					}

				PreferenceManager.getDefaultSharedPreferences(this)
						.registerOnSharedPreferenceChangeListener(
								new OnSharedPreferenceChangeListener()
									{
										@Override
										public void onSharedPreferenceChanged(
												SharedPreferences sharedPreferences,
												String key)
											{
												if (key.equals("sensorList"))
													{
														updatePrefs();
													}
											}
									});
			}

		@SuppressWarnings("deprecation")
		public void updatePrefs()
			{
				alertb = new Builder(getPreferenceScreen().getContext());
				System.out.println("Preferences changed....new preferences are");
				Set<String> selections = preferences.getStringSet("sensorList", null);
				String[] selected = selections.toArray(new String[]
					{});
				JSONObject sensors = new JSONObject();
				// list=new StringBuilder();
				for (String s : selected)
					{
						// list.append(entries[Integer.parseInt(s)]+"\n");
						try
							{
								sensors.put(entries[Integer.parseInt(s)],
										RegisterMe.obj.get(entries[Integer.parseInt(s)]));
							}
						catch (NumberFormatException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						catch (JSONException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}

				// System.out.println(list.toString());
				alertb.setTitle("Updating Preferences");
				if (isNetworkAvailable() && conn.isConnected())
					{
						// send the list of sensors to the server
						Message topics = new Message("server@103.25.231.23",
								Message.Type.chat);
						topics.setSubject("updateSubscription");
						topics.setBody(sensors.toString());
						conn.sendPacket(topics);
						System.out.println("Sent");
						sent = true;
						alertb.setMessage("Preferences uploaded").create().show();
					}
				else
					{
						alertb.setMessage("Error Uploading...Try again").create().show();
					}

				/*
				 * @SuppressWarnings("deprecation") AlertDialog.Builder alertb=new
				 * Builder(getPreferenceScreen().getContext());
				 * alertb.setTitle("Updating Preferences")
				 * .setMessage("Upload to the server?") .setNegativeButton("Cancel", new
				 * OnClickListener() {
				 * 
				 * @Override public void onClick(DialogInterface arg0, int arg1) { //
				 * TODO Auto-generated method stub
				 * 
				 * } }) .setPositiveButton("OK", new OnClickListener() {
				 * 
				 * @Override public void onClick(DialogInterface arg0, int arg1) { //
				 * TODO Auto-generated method stub
				 * 
				 * //add code to upload the preferences to the server //send an xmpp iq
				 * messages to the server JID containing the updated list of sensors
				 * if(isNetworkAvailable() && conn.isConnected()) { //send the list of
				 * sensors to the server Message topics=new
				 * Message("server@103.25.231.23",Message.Type.normal);
				 * topics.setSubject("Subscription"); topics.setBody(list.toString());
				 * conn.sendPacket(topics); System.out.println("Sent"); sent=true; }
				 * else { /*AlertDialog.Builder hc=new Builder(getApplicationContext());
				 * hc.setMessage("testing"); //hc.create().show(); AlertDialog.Builder
				 * alertb=new Builder(getPreferenceScreen().getContext());
				 * alertb.setTitle("Network Unavailable");
				 * alertb.setMessage("Unable to connect to the internet...Please Try again"
				 * ) .setPositiveButton("OK", new OnClickListener() {
				 * 
				 * @Override public void onClick(DialogInterface dialog, int which) { //
				 * TODO Auto-generated method stub
				 * 
				 * } }); //.create().show();
				 * 
				 * 
				 * sent=false; } } }) .create() .show(); //alertb=new
				 * Builder(getPreferenceScreen().getContext()); if(sent) { new
				 * UploadPrefs().show(getFragmentManager(), "MyDialog"); } else
				 * alertb.setMessage("Error uploading...Try again").create().show();
				 */
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

	}