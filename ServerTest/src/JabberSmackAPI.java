
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
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class JabberSmackAPI extends Thread implements MessageListener{
	private static JSONObject obj;
    XMPPConnection connection;
    private static AccountManager am;
    private Thread t;
    private String username;
    private String password;
    private long time;
    private static long totalTime = 0;
    private static long totalCount = 0;
    
    JabberSmackAPI(String username,String password){
    	this.username=username;
    	this.password=password;
    	System.out.println("Connecting with"+username);
    }
    
    public void login(String userName, String password) throws XMPPException, InterruptedException
    {
	    ConnectionConfiguration config = new ConnectionConfiguration("103.25.231.23",5222);
	    connection = new XMPPConnection(config);
        //SASLAuthentication.supportSASLMechanism("PLAIN", 0);
    	time=System.currentTimeMillis();

	    connection.connect();
	    am=connection.getAccountManager();
		obj=new JSONObject();
		int count=1;
		JSONArray array=new JSONArray();
		array.put("AccelerometerFake");
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
	
		array.put("GyroscopeFake");
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
			obj.put("noSensors", count-1);	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		  if(am.supportsAccountCreation()){
			    try{am.createAccount(userName, password); //to create accounts.Comment out if not needed.
			    
			    }catch(XMPPException e){}
			    connection.login(userName,password);
				Message loginWithServer=new Message("server@103.25.231.23",Message.Type.normal);
				loginWithServer.setSubject("Sensor Capabilities");
				loginWithServer.setBody(obj.toString());
				connection.sendPacket(loginWithServer);
		    }	
	Chat chat = connection.getChatManager().createChat("server@103.25.231.23", new MessageListener() {
			public void processMessage(Chat chat, Message message) { // Print out any messages we get back to standard out.
				//System.out.println("Received message: " + message); 
				if(message.getSubject().toString().equalsIgnoreCase("Registration Successful!")){
    	            System.out.println("disconnected "+username);

				            connection.disconnect();
				            time=System.currentTimeMillis()-time;
				            totalTime += time;
				            totalCount++;
//				            connection.disconnect();
				            System.out.println("TotalTime: " + totalTime + "\nTotalCount: " + totalCount + "\nAverage: " + ((totalTime*1.0)/totalCount));
				            
//				            System.out.println("time taken "+username+" "+time);
		            	}
				            } });
		     
		while(connection.isConnected()){
			Thread.sleep(50);
		}	
	  
	    
	 
	
    }
    public void run(){
        try {
			login(username, password);
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
   
 
    	JabberSmackAPI T1;
    // turn on the enhanced debugger
    XMPPConnection.DEBUG_ENABLED = false;
    for(int i=0;i<10;i++){
    T1 = new JabberSmackAPI("username"+i,"1234");
    T1.start();}
    

    }
 
}