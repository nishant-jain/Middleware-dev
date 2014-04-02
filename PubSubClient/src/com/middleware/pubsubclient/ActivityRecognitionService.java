package com.middleware.pubsubclient;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
// google play specific libraries
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


//implements intentservice, handles the intent from the activity recognition client
//then formats everything and broadcasts it back to the main activity
public class ActivityRecognitionService extends IntentService{

	String detectedActivity;
	SimpleDateFormat formatter;


	private static final String TAG ="ActivityRecognition";

	public ActivityRecognitionService() {
		super("ActivityRecognitionService");
		formatter = new SimpleDateFormat("HH:mm:ss:SSS");
		
	}

	/**
	 * Google Play Services calls this once it has analysed the sensor data
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		if (ActivityRecognitionResult.hasResult(intent)) {
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

			detectedActivity = getActivityName(result.getMostProbableActivity().getType());
			Log.d(TAG, "ActivityRecognitionResult: "+detectedActivity);
			Log.d(TAG, result.toString());

			Intent sendingIntent = new Intent("activityDetected");

			sendingIntent.putExtra("name", detectedActivity);
			sendingIntent.putExtra("confidence", ""+result.getMostProbableActivity().getConfidence());
			sendingIntent.putExtra("time_in_millisecs", ""+result.getTime());
			sendingIntent.putExtra("time", ""+formatter.format(new Date(result.getTime())));
			sendingIntent.putExtra("entireResult", result.toString());
			LocalBroadcastManager.getInstance(this).sendBroadcast(sendingIntent);
			
		}
	}


	private static String getActivityName(int detected_activity_type){
		switch (detected_activity_type ) {
		case DetectedActivity.IN_VEHICLE:	//0
            return "in_vehicle";
        case DetectedActivity.ON_BICYCLE:	//1
            return "on_bicycle";
        case DetectedActivity.ON_FOOT:	//2
            return "on_foot";
        case DetectedActivity.STILL:	//3
            return "still";
        case DetectedActivity.UNKNOWN:	//4
            return "unknown";
        case DetectedActivity.TILTING:	//5
            return "tilting";
    }
    return "unknown";
	}


}
