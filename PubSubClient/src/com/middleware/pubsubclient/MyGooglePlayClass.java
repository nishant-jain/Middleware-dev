package com.middleware.pubsubclient;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.util.Config;
import android.util.Log;
//google play specific classes, which you get from the google play services library
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;

//you register an activityrecognitionclient, connect to it, when connected, associate an intent to a service with the client 
//that will be called whenever an activity update has occured
public class MyGooglePlayClass implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{
	private Context context;
	private static final String TAG = "ActivityRecognition";
	private static ActivityRecognitionClient activity_recognition_client;
	private static PendingIntent callbackIntent;
	private int update_interval = 0;

	//basic constructor
	public MyGooglePlayClass(Context context, int update_interval) {
		this.context=context;
		this.update_interval = update_interval;
	}

	//public function to create a client
	//you create a new activityrecognitionclient and connect to it
	//this is pre-defined function from the google play interfaces
	public void startActivityRecognition(){
		//create client, pass the application context and an instance of this class
		activity_recognition_client	= new ActivityRecognitionClient(context, this, this);
		activity_recognition_client.connect();
		Log.d(TAG,"startActivityRecognition");
	}

	//stops activity recognition - stop the recognition client's activity updates
	public void stopActivityRecognition(){
		try{
			activity_recognition_client.removeActivityUpdates(callbackIntent);
			Log.d(TAG,"stopActivityRecognition");
		} catch (IllegalStateException e){
		}
	}

	//if failed don't do anything
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d(TAG,"onConnectionFailed");
	}

	//implements google play services onConnected
	//when connected, implements a pending intent and whenever activity updates occur,
	//it sends the intent to the service, associates a pending intent with activity updates
	/**
	 * Connection established - start listening now
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		//creates a pending intent directed to the service
		Intent intent = new Intent(context, ActivityRecognitionService.class);
		callbackIntent = PendingIntent.getService(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		activity_recognition_client.requestActivityUpdates(update_interval, callbackIntent); // 0 = fastest update
	}

	@Override
	public void onDisconnected() {
	}

}