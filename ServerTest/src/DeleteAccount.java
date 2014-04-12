
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

public class DeleteAccount extends Thread implements MessageListener{
	private static JSONObject obj;
    XMPPConnection connection;
    private static AccountManager am;
    private Thread t;
    private String username;
    private String password;
    private long time;
    
    DeleteAccount(String username,String password){
    	this.username=username;
    	this.password=password;
    	System.out.println("Connecting with"+username);
    }
    
    public void login(String userName, String password) throws XMPPException
    {
	    ConnectionConfiguration config = new ConnectionConfiguration("103.25.231.23",5222);
	    connection = new XMPPConnection(config);
	    connection.connect();
	    am=connection.getAccountManager();
	
		
		
		
	    
		   
		    connection.login(userName,password);
			Message loginWithServer=new Message("server@103.25.231.23",Message.Type.normal);
			loginWithServer.setSubject("Delete Account");
			loginWithServer.setBody("Delete this account from the server");
			connection.sendPacket(loginWithServer);
	    
	    
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
	        	
	        		if(message.getSubject().toString().equalsIgnoreCase("De-Registration Successful!")){
	    	            System.out.println("disconnected "+username);
	    	            		try {
									am.deleteAccount();
								} catch (XMPPException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
					            connection.disconnect();
					            time=System.currentTimeMillis()-time;
					            System.out.println("time taken "+username+" "+time);
					            
	        	}
	          }
	        });
	      }
	    });
	
    }
    public void run(){
        try {
        	time=System.currentTimeMillis();
			login(username, password);
		} catch (XMPPException e) {
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
//    JabberSmackAPI c = new JabberSmackAPI();
    //JabberSmackAPI d = new JabberSmackAPI();
    //JabberSmackAPI e = new JabberSmackAPI();

  //  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    //String msg;
 
    	DeleteAccount T1;
    // turn on the enhanced debugger
    //XMPPConnection.DEBUG_ENABLED = true;
    for(int i=0;i<10;i++){
    T1 = new DeleteAccount("user"+i,"1234");
    T1.start();}
    
    // Enter your login information here
    //c.login("new_user2", "1234");
    
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