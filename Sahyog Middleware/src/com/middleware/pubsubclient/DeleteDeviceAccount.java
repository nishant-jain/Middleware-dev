package com.middleware.pubsubclient;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DeleteDeviceAccount
	{
		static Message delete;
		static boolean deleted = false;
		static boolean ackReceived = false;

		public static void delete(Context context)
			{
				System.out.println("Deleting account");
				final XMPPConnection conn = RegisterDevice.conn;
				final AccountManager accMgr = RegisterDevice.am;

				try
					{
						delete = new Message("server@" + RegisterDevice.serverIP,
								Message.Type.normal);
						delete.setSubject("Delete Account");
						delete.setBody("Delete this account from the server");
						conn.sendPacket(delete);
						System.out.println("Request sent to server");

						int i = 0;
						while (i < 400000)
							{
								if (ackReceived)
									{
										System.out.println("deleted");
										try
											{
												accMgr.deleteAccount();
												conn.disconnect();
												deleted = true;
											}
										catch (XMPPException e)
											{
												e.printStackTrace();
											}

										break;
									}

							}

						if (deleted)
							{
								SharedPreferences pref = PreferenceManager
										.getDefaultSharedPreferences(context);
								pref.edit().clear().commit();
							}
						else
							{
								System.out
										.println("No confirmation received from server...Please try again later");
							}

					}
				catch (IllegalStateException e1)
					{
						System.out
								.println("You need to login with the server for deleting your account");
					}
				catch (Exception e)
					{
						System.out.println("Not connected to the internet");
					}
			}

	}
