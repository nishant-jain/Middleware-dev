package com.middleware.pubsubclient;

import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Sensor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceManager;
import android.preference.PreferenceActivity;

public class SubscribeTopics extends PreferenceActivity{
	
	SharedPreferences preferences ;
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        addPreferencesFromResource(R.xml.subscriptions);
        
        MultiSelectListPreference topicList = (MultiSelectListPreference) findPreference("sensorList");
        topicList.setPersistent(true);
        topicList.setEnabled(true);
        if (topicList != null) {
            List<Sensor> categoryList = RegisterMe.deviceSensors;
            CharSequence entries[] = new String[categoryList.size()];
            CharSequence entryValues[] = new String[categoryList.size()];
            int i = 0;
            for (Sensor category : categoryList) {
                entries[i] = category.getName();
                entryValues[i] = Integer.toString(i);
                i++;
            }
            topicList.setEntries(entries);
            topicList.setEntryValues(entryValues);
        }
       
	
	
    Set<String> selections = preferences.getStringSet("sensorList", null);
    String[] selected= selections.toArray(new String[]{});
     for(String s:selected)
     {
    	 System.out.println(s);
     }
	
	
	PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
			{
        if (key.equals("sensorList"))
  		{
        	updatePrefs();
        }
        }
	});
}
	
	public void updatePrefs()
	{
		System.out.println("Preferences changed....new preferences are");
    	Set<String> selections = preferences.getStringSet("sensorList", null);
        String[] selected= selections.toArray(new String[]{});
        for(String s:selected)
        {
       	 System.out.println(s);
        }
        
    	@SuppressWarnings("deprecation")
		AlertDialog.Builder alertb=new Builder(getPreferenceScreen().getContext());
    	alertb.setTitle("Updating Preferences")
    	.setMessage("Upload to the server?")
    	.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
		})		
		.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
				//add code to upload the preferences to the server
				//send an xmpp iq messages to the server JID containing the updated list of sensors
				if(isNetworkAvailable())
				{
					//send the list of sensors to the server
				}
				else
				{
					AlertDialog.Builder alertb=new Builder(getPreferenceScreen().getContext());
					alertb.setTitle("Network Unavailable");
					alertb.setMessage("Unable to connect to the internet...Please Try again")
					.setPositiveButton("OK", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					})
					.create().show();
				}
			}
		})
    	.create()
    	 .show();
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
	

}