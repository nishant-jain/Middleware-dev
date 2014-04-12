
import java.util.*;
import java.io.*;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

 
public class JabberSmackAPI implements MessageListener{
	public static JSONObject obj;
    XMPPConnection connection;
    public static AccountManager am;
    public void login(String userName, String password) throws XMPPException
    {
    ConnectionConfiguration config = new ConnectionConfiguration("103.25.231.23",5222);
    connection = new XMPPConnection(config);
    connection.connect();
    am=connection.getAccountManager();
	obj=new JSONObject();
	int count=1;
	JSONArray array=new JSONArray();
	array.put("Accelerometer");
	array.put("1234");
	array.put("123");
	array.put("27849032");
	array.put("789043223");
	try {
		obj.put("sensor"+(count++),array);
	} catch (JSONException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	array=new JSONArray();

	array.put("Gyroscope");
	array.put("1234");
	array.put("123");
	array.put("27849032");
	array.put("789043223");
	try {
		obj.put("sensor"+(count++),array);
	} catch (JSONException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	try {
		
		obj.put("ActivityRecognition", "present");
		obj.put("DownloadAllowed", "yes");
		obj.put("noSensors", obj.length()-2);
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
	
    if(am.supportsAccountCreation()){
    am.createAccount(userName, password);
    connection.login(userName,password);
	Message loginWithServer=new Message("server@103.25.231.23",Message.Type.normal);
	loginWithServer.setSubject("Sensor Capabilities");
	loginWithServer.setBody(obj.toString());
	connection.sendPacket(loginWithServer);}
    
    ChatManager chatmanager = connection.getChatManager();
    connection.getChatManager().addChatListener(new ChatManagerListener()
    {
      public void chatCreated(final Chat chat, final boolean createdLocally)
      {
        chat.addMessageListener(new MessageListener()
        {
          public void processMessage(Chat chat, Message message)
          {
            System.out.println("Received message: " 
              + (message != null ? message.getBody() : "NULL"));
          }
        });
      }
    });

    }
 
    public void sendMessage(String message, String to) throws XMPPException
    {
    Chat chat = connection.getChatManager().createChat(to, this);
    chat.sendMessage(message);
    }
 
    public void displayBuddyList()
    {
    Roster roster = connection.getRoster();
    Collection<RosterEntry> entries = roster.getEntries();
 
    System.out.println("\n\n" + entries.size() + " buddy(ies):");
    for(RosterEntry r:entries)
    {
    System.out.println(r.getUser());
    }
    }
 
    public void disconnect()
    {
    connection.disconnect();
    }
 
    public void processMessage(Chat chat, Message message)
    {
    if(message.getType() == Message.Type.chat)
    System.out.println(chat.getParticipant() + " says: " + message.getBody());
    }
 
    public static void main(String args[]) throws XMPPException, IOException
    {
    // declare variables
    JabberSmackAPI c = new JabberSmackAPI();
    //JabberSmackAPI d = new JabberSmackAPI();
    //JabberSmackAPI e = new JabberSmackAPI();

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String msg;
 
 
    // turn on the enhanced debugger
    XMPPConnection.DEBUG_ENABLED = true;
 
 
    // Enter your login information here
    c.login("new_user2", "1234");
    
 /*
    c.displayBuddyList();
 
    System.out.println("-----");
 
    System.out.println("Who do you want to talk to? - Type contacts full email address:");
    String talkTo = br.readLine();
 
    System.out.println("-----");
    System.out.println("All messages will be sent to " + talkTo);
    System.out.println("Enter your message in the console:");
    System.out.println("-----\n");
 
    while( !(msg=br.readLine()).equals("bye"))
    {
        c.sendMessage(msg, talkTo);
    }

    c.disconnect();
   
    System.exit(0);*/
    }
 
}