package com.middleware.pubsubclient;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

public class ConvertAudioToMsg
	{

		public static boolean sendAsMessage(File audioFile, String queryNo)
			{
				JSONObject audioMessage = new JSONObject();
				File file = new File(audioFile.getPath());
				byte[] bytes;

				try
					{
						bytes = FileUtils.readFileToByteArray(file);
						// String encodedAudio = Base64.encodeToString(bytes,
						// 0);
						// Log.d(" Encoded: ", encodedAudio);
						String byteMessage = new String(bytes);
						audioMessage.put("queryNo", queryNo);
						audioMessage.put("sensorData", byteMessage);

						Message sensordata = new Message("server@103.25.231.23",
								Message.Type.chat);
						sensordata.setSubject("Data");
						sensordata.setBody(audioMessage.toString());
						RegisterMe.conn.sendPacket(sensordata);
					}
				catch (IOException e1)
					{
						e1.printStackTrace();
					}
				catch (JSONException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				return true;
			}

	}
