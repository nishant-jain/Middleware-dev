package com.middleware.pubsubclient;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class UpdateAllowedTopics
	{
		static XMPPConnection conn;
		static boolean sent;

		public static boolean update(final ConnectivityManager cm, JSONObject sensors)
			{
				conn = RegisterDevice.conn;
				sent = false;

				if (isNetworkAvailable(cm) && conn.isConnected())
					{
						Message topics = new Message("server@" + RegisterDevice.serverIP,
								Message.Type.normal);
						topics.setSubject("updateSubscription");
						topics.setBody(sensors.toString());

						conn.sendPacket(topics);
						System.out.println("Sent");
						sent = true;
					}

				return sent;

			}

		private static boolean isNetworkAvailable(ConnectivityManager cm)
			{
				NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
				if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
					{
						return true;
					}
				else
					{
						return false;
					}
			}

	}