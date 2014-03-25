package com.middleware.pubsubclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.util.AttributeSet;

public class Settings extends PreferenceActivity{
	
	SharedPreferences preferences;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences=getSharedPreferences(RegisterMe.PREFS_NAME,0);
        addPreferencesFromResource(R.xml.settings);
        
        
	}
}

