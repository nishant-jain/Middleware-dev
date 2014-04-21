package com.middleware.pubsubclient;

import java.io.File;
import java.util.Iterator;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Application;
import android.content.Context;
import android.util.Log;

public class UploadFile extends Application{	
	
	public static void upload(File file,Context context)	//pass applicationcontext as a parameter to this function
	{
		FileTransferManager ftm;
		OutgoingFileTransfer oft;
		File dataFile;
		AlertDialog.Builder notify;
		
	notify = new Builder(context);
	notify.setTitle("File Uploading");
	dataFile=file;
	ftm=new FileTransferManager(RegisterMe.conn);
	//Iterator<Presence> presences= RegisterMe.conn.getRoster().getPresences("server@103.25.231.23");
	//while(presences.hasNext()){
    //    Log.v("Gabriel",presences.next().getFrom());
	//}
	oft= ftm.createOutgoingFileTransfer("server@103.25.231.31/Smack");
	try {
		oft.sendFile(dataFile, "Data from "+RegisterMe.username.toString());
		while(!oft.isDone())
		{
			System.out.println(String.valueOf(oft.getProgress()*100));
		}
		//Thread.sleep(1000);
	} catch (XMPPException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();		
	} 	
	}
}
