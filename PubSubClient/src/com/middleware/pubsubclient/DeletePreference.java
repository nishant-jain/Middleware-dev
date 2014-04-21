package com.middleware.pubsubclient;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
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
	boolean deleted = false;
	PacketListener listen = null;

	public DeletePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getDialog()
				.getOwnerActivity().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		// = (ConnectivityManager)
		// getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		AlertDialog.Builder alert = new Builder(getContext());
		if (positiveResult) {
			System.out.println("Yes clicked");
			final XMPPConnection conn = RegisterMe.conn;
			final AccountManager accMgr = RegisterMe.am;

			try {
				delete = new Message("server@103.25.231.23",
						Message.Type.normal);
				delete.setSubject("Delete Account");
				delete.setBody("Delete this account from the server");
				conn.sendPacket(delete); // sends a normal message to the
											// customServer containing the
											// sensor capabilities
				System.out.println("Request sent to server@103.25.231.23");

				ChatManager chatmanager = conn.getChatManager();
				conn.getChatManager().addChatListener(
						new ChatManagerListener() {
							@Override
							public void chatCreated(final Chat chat,
									final boolean createdLocally) {
								chat.addMessageListener(new MessageListener() {
									@Override
									public void processMessage(Chat chat,
											Message message) {
										System.out.println("Received message: "
												+ (message != null ? message
														.getBody() : "NULL"));

										if (message
												.getSubject()
												.toString()
												.equalsIgnoreCase(
														"De-Registration Successful")) {
											System.out.println("deleted");
											try {
												accMgr.deleteAccount();
												conn.disconnect();
												deleted = true;
											} catch (XMPPException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}

										}
									}
								});
							}
						});

				if (deleted) {
					alert.setTitle("Account Deleted")
							.setMessage("Deletion successful").create().show();
					SharedPreferences pref = PreferenceManager
							.getDefaultSharedPreferences(getContext());
					pref.edit().clear().commit();
				} else {
					alert.setTitle("Error")
							.setMessage(
									"No confirmation received from server...Please try again later")
							.create().show();
				}

			} catch (IllegalStateException e1) {
				// AlertDialog.Builder alert=new Builder(getContext());
				alert.setTitle("Error")
						.setMessage(
								"You need to login with the server for deleting your account")
						.create().show();
			} catch (Exception e) {
				// AlertDialog.Builder alert=new Builder(getContext());
				alert.setTitle("Error")
						.setMessage("Not connected to the internet").create()
						.show();
			}
		}

		else {
			System.out.println("No clicked");
		}
	}
}
