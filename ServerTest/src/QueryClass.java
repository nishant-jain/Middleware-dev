
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

import java.util.Date;

public class QueryClass extends Thread implements MessageListener{
	private static JSONObject obj;
    XMPPConnection connection;
    private static AccountManager am;
    private Thread t;
    private String username;
    private String password;
    private long time;
    
    QueryClass(String username,String password){
    	this.username=username;
    	this.password=password;
    	System.out.println("Connecting with"+username);
    }
    
    public void login(String userName, String password) throws XMPPException, JSONException
    {
	    ConnectionConfiguration config = new ConnectionConfiguration("103.25.231.23",5222);
	    connection = new XMPPConnection(config);
	    connection.connect();
	 //   am=connection.getAccountManager();
	    connection.login(userName, password);
	    Date dt=new Date();
	    Long dat = dt.getTime();
	    JSONObject query = new JSONObject();
		query.put("username", username);
		query.put("queryNo", "4");
		query.put("dataReqd","Accelerometer");
		query.put("fromTime", dat);
		query.put("toTime", dat+100);
		query.put("expiryTime", dat+1000);
		query.put("location", "0.0");
		query.put("longitude","1.0");
		query.put("latitude","1.0");
		query.put("Activity","Download" );
		query.put("frequency",0);
		query.put("countMin", 10);
		query.put("countMax", 20);
		
     Chat chat = connection.getChatManager().createChat("server@103.25.231.23", new MessageListener() {
    	 public void processMessage(Chat chat, Message message) { // Print out any messages we get back to standard out.
			System.out.println("Received message: " + message);
			time=System.currentTimeMillis()-time;
            System.out.println("time taken "+username+" "+time);
			/*if(message.getSubject().toString().equalsIgnoreCase("De-Registration Successful!")){
	            System.out.println("disconnected "+username);
	            	try {
						connection.getAccountManager().deleteAccount();
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
	            //}*/
	        } 
    	 });
     if(username.equals("user1")){
    	Message loginWithServer=new Message("server@103.25.231.23",Message.Type.chat);
		loginWithServer.setSubject("Query");
		loginWithServer.setBody(query.toString());
		connection.sendPacket(loginWithServer);
		}
	}
    
    public void run(){
        try {
        	time=System.currentTimeMillis();
			login(username, password);
			/*Message loginWithServer=new Message("server@103.25.231.23",Message.Type.normal);
			loginWithServer.setSubject("Delete Account");
			loginWithServer.setBody("Delete this account from the server");
			sendMessage(loginWithServer,"server@103.25.231.23" );*/
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        

    	
    }
    public void start(){
    	System.out.println("Starting " +  username );
    	if (t == null)
        {
           t = new Thread (this, username);
           
           t.start ();
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
 
    	QueryClass T1;
   
    // turn on the enhanced debugger
    XMPPConnection.DEBUG_ENABLED = true;
    for(int i=0;i<30 ;i++){
    T1 = new QueryClass("user"+i,"1234");
    T1.start();}
   
   
    }
 
}