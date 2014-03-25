package com.middleware.pubsubclient;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

public class DeletePreference extends DialogPreference {
	Message delete;

	public DeletePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager =(ConnectivityManager)getDialog().getOwnerActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
	          //= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    if(activeNetworkInfo != null && activeNetworkInfo.isConnected())
	    return true;
	    else
	    	return false;
	}
	
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
	    super.onDialogClosed(positiveResult);
	      if(positiveResult)
	    	{
	    	System.out.println("Yes clicked");
	    	XMPPConnection conn=RegisterMe.conn;
	    	AccountManager accMgr=RegisterMe.am;
	    	
	    	try {
	    		delete=new Message("server@103.25.231.23",Message.Type.normal);
				//loginWithServer.setFrom(username);
	    		delete.setSubject("Delete Account");
	    		delete.setBody("Delete this account from the server");
				conn.sendPacket(delete);			//sends a normal message to the customServer containing the sensor capabilities
				System.out.println("Request sent to server@103.25.231.23");
				accMgr.deleteAccount();
				AlertDialog.Builder alert=new Builder(getContext());
				alert.setTitle("Account Deleted")
				.setMessage("Deletion request sent")
				.create()
				.show();
				
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
				pref.edit().clear().commit();
			    
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				AlertDialog.Builder alert=new Builder(getContext());
				alert.setTitle("Error")
				.setMessage("An error occurred..Please try again after some time")
				.create()
				.show();
			}
	    	catch(IllegalStateException e1)
	    	{
	    		AlertDialog.Builder alert=new Builder(getContext());
				alert.setTitle("Error")
				.setMessage("You need to login with the server for deleting your account")
				.create()
				.show();
	    	}
	    	catch(Exception e)
	    	{
	    		AlertDialog.Builder alert=new Builder(getContext());
				alert.setTitle("Error")
				.setMessage("Not connected to the internet")
				.create()
				.show();
	    	}
	    	}
	    
	    else
	    	{
	    	System.out.println("No clicked");
	    	}
	}
}
