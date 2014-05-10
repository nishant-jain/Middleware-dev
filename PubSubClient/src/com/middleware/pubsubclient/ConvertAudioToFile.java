package com.middleware.pubsubclient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;
import android.util.Base64;

public class ConvertAudioToFile {

	static JSONObject[] messages;
	static File directory;
	static File file;
	static FileOutputStream foStream;

	public static void convert(Message message)
	{
		if (message.getSubject().equals("RequestedData"))
		{
			String messages=message.getBody();
			try {
				JSONObject data=new JSONObject(messages);
				int noOfFiles=data.getInt("noOfFiles");
				String queryDir=data.getString("queryNo");
				directory=new File(new File(Environment.getExternalStorageDirectory()+"/ReceivedFile").getPath(),"/QueryResponse/"+queryDir);
				if (!directory.exists())
				{
					directory.mkdirs();
				}
				for(int i=1;i<=noOfFiles;i++)
				{
					file=new File(directory+"/response"+i+".3gp");
					try {
						
						FileOutputStream fileOuputStream = new FileOutputStream(file);
						String dataString=data.getString("sensorData"+i);
						byte[] decoded = Base64.decode(dataString, 0);
					    fileOuputStream.write(decoded);
					    fileOuputStream.close();
					  
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					++i;	
				}
				//writer.close();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}