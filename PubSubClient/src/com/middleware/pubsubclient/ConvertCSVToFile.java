package com.middleware.pubsubclient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Environment;

public class ConvertCSVToFile
	{

		static JSONObject[] messages;
		static File directory;
		static File file;
		static BufferedWriter writer;

		public static void convert(Message message)
			{
				if (message.getSubject().equals("RequestedData"))
					{
						String messages = message.getBody();
						try
							{
								JSONObject data = new JSONObject(messages);
								int noOfFiles = data.getInt("noOfFiles");
								String queryDir = data.getString("queryNo");
								directory = new File(new File(
										Environment.getExternalStorageDirectory()
												+ "/ReceivedFile").getPath(),
										"/QueryResponse/" + queryDir);
								if (!directory.exists())
									{
										directory.mkdirs();
									}
								for (int i = 1; i <= noOfFiles; i++)
									{
										file = new File(directory + "/response" + i
												+ ".csv");
										try
											{
												writer = new BufferedWriter(
														new FileWriter(file, false));
												String dataString = data
														.getString("sensorData" + i);
												writer.write(dataString);
												writer.flush();

											}
										catch (IOException e)
											{
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										catch (JSONException e)
											{
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}

										writer.close();
									}

							}
						catch (JSONException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

					}
			}

		// public static void convert(JSONObject[] objs) throws IOException
		// {
		// directory=new File(new
		// File(Environment.getExternalStorageDirectory()+"/ReceivedFile").getPath(),"/QueryResponse");
		// if (!directory.exists())
		// {
		// directory.mkdirs();
		// }
		// messages=objs;
		//
		// int i=1;
		// for(JSONObject m : messages)
		// {
		// file=new File(directory+"/response"+i+".csv");
		// try {
		// writer = new BufferedWriter(new FileWriter(file, false));
		// String data=m.getString("sensorData");
		// writer.write(data);
		// writer.flush();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// ++i;
		// }
		// writer.close();
		// }

	}
