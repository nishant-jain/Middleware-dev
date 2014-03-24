package com.middleware.pubsubclient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Sensor;
import android.hardware.SensorListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceManager;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class SubscribeTopics extends PreferenceActivity{
	
	SharedPreferences preferences ;
	String entries[];
	StringBuilder list;
	XMPPConnection conn;
	boolean sent;
	AlertDialog.Builder alertb;
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // preferences=getSharedPreferences(RegisterMe.PREFS_NAME, 0);
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        conn=RegisterMe.conn;
        sent=false;
        addPreferencesFromResource(R.xml.subscriptions);
        
        MultiSelectListPreference topicList = (MultiSelectListPreference) findPreference("sensorList");
        topicList.setPersistent(true);
        topicList.setEnabled(true);
        topicList.setDefaultValue(true);
        if (topicList != null) 
        {
           	Iterator check=RegisterMe.obj.keys();
            List<String> elements = new ArrayList<String>();
           while(check.hasNext())
           {
        	   elements.add(check.next().toString());
        	 }
           
            entries = new String[elements.size()-1];
            String entryValues[] = new String[elements.size()-1];
            int i=0;
            for (String category : elements) {
            	if(!category.equals("noSensors"))
                {
            		entries[i] = category;
	                entryValues[i] = Integer.toString(i);
	                i++;
                }
            }
            topicList.setEntries(entries);
            topicList.setEntryValues(entryValues);
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
		alertb=new Builder(getPreferenceScreen().getContext());
		System.out.println("Preferences changed....new preferences are");
		 Set<String> selections = preferences.getStringSet("sensorList", null);
	        String[] selected = selections.toArray(new String[] {});
	        list=new StringBuilder();
		    for(String s:selected)
		    {
		    	list.append(entries[Integer.parseInt(s)]+"\n");		    	
		    }
		
        System.out.println(list.toString());
        alertb.setTitle("Updating Preferences");
        if(isNetworkAvailable() && conn.isConnected())
		{
			//send the list of sensors to the server
			Message topics=new Message("server@103.25.231.23",Message.Type.normal);
			topics.setSubject("Subscription");
			topics.setBody(list.toString());
			conn.sendPacket(topics);
			System.out.println("Sent");
			sent=true;
			alertb.setMessage("Preferences uploaded").create().show();
		}
        else
        {
        	alertb.setMessage("Error Uploading...Try again").create().show();
        }
        
        /*
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
				if(isNetworkAvailable() && conn.isConnected())
				{
					//send the list of sensors to the server
					Message topics=new Message("server@103.25.231.23",Message.Type.normal);
					topics.setSubject("Subscription");
					topics.setBody(list.toString());
					conn.sendPacket(topics);
					System.out.println("Sent");
					sent=true;
				}
				else
				{
					/*AlertDialog.Builder hc=new Builder(getApplicationContext());
					hc.setMessage("testing");
						//hc.create().show();
					AlertDialog.Builder alertb=new Builder(getPreferenceScreen().getContext());
					alertb.setTitle("Network Unavailable");
					alertb.setMessage("Unable to connect to the internet...Please Try again")
					.setPositiveButton("OK", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					});
					//.create().show();
					 * 
					 
					sent=false;
				}
			}
		})
    	.create()
    	 .show();
    	//alertb=new Builder(getPreferenceScreen().getContext());
    	if(sent)
    	{    		
    		new UploadPrefs().show(getFragmentManager(), "MyDialog");
    	}
    	else 
    		alertb.setMessage("Error uploading...Try again").create().show();
    		*/
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