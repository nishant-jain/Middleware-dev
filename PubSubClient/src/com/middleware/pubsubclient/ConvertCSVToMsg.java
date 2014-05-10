package com.middleware.pubsubclient;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import au.com.bytecode.opencsv.CSVReader;

public class ConvertCSVToMsg {
	
	//change return type to void: JSONObject returned only or testing
		public static boolean sendAsMessage(File f,String queryNo) {
			StringBuilder data = new StringBuilder();
			CSVReader reader;
			try {
				reader = new CSVReader(new FileReader(f));

				String[] nextLine;

				while ((nextLine = reader.readNext()) != null) {
					int j=0,k=nextLine.length;
					for (int i = 0; i < k; i++) {						
						data.append(nextLine[i]);
						if(j!=k-1)
						{data.append(",");j++;}
					}
					//data.append(System.getProperty("line.seperator"));
					data.append("\n");
				}
				JSONObject servicedData = new JSONObject();
				try {
					//servicedData.put("queryNo", PublishQuery.queryNoAcc);
					servicedData.put("queryNo", queryNo);
					servicedData.put("sensorData", data.toString());
					// System.out.println(servicedData.getString("queryNo"));

					Message sensordata = new Message("server@103.25.231.23",
							Message.Type.chat);
					sensordata.setSubject("Data");
					sensordata.setBody(servicedData.toString());
					RegisterMe.conn.sendPacket(sensordata);
					//System.out.println(sensordata.getBody());
					return true;			//remove after testing
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Exception caught");
					return false;					//remove after testing
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("XMPP connection timed out");
				return false;						//delete after testing
			}
			// System.out.print(data.toString());

		}

}
