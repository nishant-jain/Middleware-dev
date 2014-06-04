
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
    private static long totalTime = 0;
    private static long totalCount = 0;
    
    QueryClass(String username,String password){
    	this.username=username;
    	this.password=password;
    	System.out.println("Connecting with"+username);
    }
    
    public void login(String userName, String password) throws XMPPException, JSONException, InterruptedException
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
		query.put("queryNo", "98765");
		query.put("dataReqd","AccelerometerFake");
		query.put("fromTime", dat);
		query.put("toTime", dat+100);
		query.put("expiryTime", dat+1000);
		query.put("location", "0.0");
		query.put("longitude","1.0");
		query.put("latitude","1.0");
		query.put("activity","Download" );
		query.put("frequency",0);
		query.put("countMin", 8);
		query.put("countMax", 200);
	    final JSONObject query2 = new JSONObject();
	    query2.put("queryNo", "98765");
	    query2.put("status", "Accepted");
	   
	    
     Chat chat = connection.getChatManager().createChat("server@103.25.231.23", new MessageListener() {
    	 public void processMessage(Chat chat, Message message) { // Print out any messages we get back to standard out.
			//time=System.currentTimeMillis()-time;
            //System.out.println("time taken "+username+" "+time);
				//System.out.println("Received message: " + message);

			if(message.getSubject().toString().equalsIgnoreCase("DataRequest")){

				Message loginWithServer=new Message("server@103.25.231.23",Message.Type.chat);
				loginWithServer.setSubject("ProviderResponse");
				loginWithServer.setBody(query2.toString());
				connection.sendPacket(loginWithServer);
			}
			else if (message.getSubject().toString().equalsIgnoreCase("Final Confirmation"))
			{				System.out.println("Received message: " + message);

				connection.disconnect();
				time=System.currentTimeMillis()-time;
	            System.out.println("time taken "+username+" "+time);
	            totalTime += time;
	            totalCount++;
	            connection.disconnect();
	            System.out.println("\nTotalTime: " + totalTime + "\nTotalCount: " + totalCount + "\nAverage: " + ((totalTime*1.0)/totalCount));
			}
	        /*    System.out.println("disconnected "+username);
	            	try {
						connection.getAccountManager().deleteAccount();
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
	            //}*/
	        } 
    	 });
     if(username.equals("username1")){
    	Message loginWithServer=new Message("server@103.25.231.23",Message.Type.chat);
		loginWithServer.setSubject("Query");
		loginWithServer.setBody(query.toString());
		connection.sendPacket(loginWithServer);
		}

		while(connection.isConnected()){
			Thread.sleep(50);}
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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        

		while(connection.isConnected()){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
    //XMPPConnection.DEBUG_ENABLED = true;
    for(int i=0;i<10 ;i++){
    T1 = new QueryClass("username"+i,"1234");
    T1.start();}
   
   
    }
}