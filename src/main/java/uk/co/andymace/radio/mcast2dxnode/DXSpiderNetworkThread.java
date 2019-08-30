package uk.co.andymace.radio.mcast2dxnode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;

public class DXSpiderNetworkThread implements Runnable {

	private static final Logger Logger = LogManager.getLogger(DXSpiderNetworkThread.class.getName());
	
	private static boolean alreadyInstantiated = false;
	private int port = 0;
	private final String localIPAddress;
	private final DXSpiderThreadController controller;

	private ServerSocket serverSocket;
	private BufferedOutputStream out;
	private BufferedInputStream in;

	public PCSTATE state;

	private String mycall;
	
	public DXSpiderNetworkThread (myProperties properties, DXSpiderThreadController controller) 
	{
		Logger.debug("Instatiating the EnCC Thread");
		
		this.controller = controller;
		this.localIPAddress = properties.getProperty("clusterlsnaddress");
		this.port = properties.getIntProperty("clusterlsnport");
		this.mycall = properties.getProperty("clusterlsmycall");
		if (alreadyInstantiated == true) 
		{ 
			Logger.error("Looks like you're trying to spin up two DXSpider threads. Not good. Exiting.");
			System.exit(-1);			
		}		
		alreadyInstantiated = true;
	}
	
	
	@Override
	public void run() {
		Logger.info("Spinning up DXSpider Thread.");
		
		while(true) {
			try {
			    // We will wait for the nCC to connect, so we set up a listening socket.
				serverSocket = new ServerSocket();
				serverSocket.setReuseAddress(true);
				serverSocket.bind(new InetSocketAddress(localIPAddress, port));

				//Wait for a client to connect...
				Logger.info("Listening on socket");
				Socket clientSocket = serverSocket.accept();
								
				out = new BufferedOutputStream(clientSocket.getOutputStream());				
				in  = new BufferedInputStream(clientSocket.getInputStream());
				
				Logger.debug("Sending out to controller. Out is: "+ out.toString());
				this.controller.setNetworkOutputStream(out);

				state = PCSTATE.connected;
				
				//Now that we're set up, send the welcome message...
				Logger.info("Client has connected: [" + clientSocket.getInetAddress().toString() + "]");
				
				out.write("login:".getBytes(Charsets.UTF_8));
				out.flush();
				
				byte[] inputData = new byte[5000];
				String send;
				
				inputData = new byte[5000];
	            int len = in.read(inputData);
	     
	            String callsign = new String(Arrays.copyOf(inputData, len-1), "UTF-8");
	            String[] callsigns = callsign.split("\\r");
				
	            Logger.info("Callsign given: " + callsigns[0]);
	            Logger.info("Lets talk DXSpider PC");
				
	            send = "PC18^mcast2dxc githash^1^";
	            sendData(send);
		        
	            
	            //Thread.sleep(300);
				while(true)
				{
				    while(in.available()>0)
			        {
						len = in.read(inputData);
						
						String incoming = new String(Arrays.copyOf(inputData, len));
				    	//Split return up by \n linebreaks.. sometimes multiples are sent
						String[] pccommands = incoming.split("\\r\\n");
						
						for (String s : pccommands)
						{
							//Process Each in turn
							Logger.debug("RCV: " + s);
							ProcessCommand(s);	
						}
						
			        }
				}
				
				
			} catch (IOException e) {
				Logger.error("Exception caught when trying to listen on IP:port ["+ localIPAddress+":"+ port + "] or listening for a connection. It *might* be in use. Trying again in 5 secs. Error is ["+e.getMessage()+"]");
				try {
					Thread.sleep(5000);
				}
				catch (InterruptedException e1) { // We do this as part of graceful close-down
					closeDownSocket();
				}
			}	
			
		}

	}


	private void ProcessCommand(String s) {
		String pc = s.substring(0, 4);
		switch (pc)
		{
			case "PC19":
				Logger.debug("Seen PC19 ignore");
				Logger.info("Seen Init from Client");
				break;
			case "PC20":
				Logger.debug("Seen PC20, Start My Init");
				sendData("PC19^1^"+mycall+"^0^5457^H15^");
				sendData("PC22^");
				state = PCSTATE.initcomplete;
				Logger.info("Full Init Complete");
				break;
			case "PC51":
					process_ping_request(s);
					break;
		    default:
		    	break;
		}
	}


	private void process_ping_request(String str) {
			String[] s = str.split("\\^");
			
			String opcode = s[0], tocluster = s[1], fromcluster = s[2];
			int pingflag = Integer.parseInt(s[3]);
			
			Logger.info("HEARTBEAT " + s[2]);
			pingflag ^= 1;
			String pc51 = "PC51^"+fromcluster+"^"+tocluster+"^"+pingflag+"^~";
			sendData(pc51);
		}


	private void sendData(String send) {
		
        try {
        	Logger.debug("SEND: " + send);
			out.write((send+"\n").getBytes(Charsets.UTF_8));
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	

	private void closeDownSocket() 
	{
		if (serverSocket != null)
		{
			Logger.debug("Closing down DXSpider listening socket.");
			try {
				serverSocket.close();
			} catch (IOException ioException) {
			    Logger.error("IOException thrown whilst trying to close down the EnCC Thread socket. Reason: [" + ioException.getMessage() +"]");
				ioException.printStackTrace();
			}
		}
	}
	
	
	
	
	
}