package uk.co.andymace.radio.mcast2dxnode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DXSpiderNetworkThread implements Runnable {

	private static final Logger Logger = LogManager.getLogger(DXSpiderNetworkThread.class.getName());
	
	private static boolean alreadyInstantiated = false;
	private int port = 0;
	private final String localIPAddress;
	private final DXSpiderThreadController controller;

	private ServerSocket serverSocket;
	private BufferedOutputStream out;
	private BufferedInputStream in;
	
	public DXSpiderNetworkThread (myProperties properties, DXSpiderThreadController controller) 
	{
		Logger.debug("Instatiating the EnCC Thread");
		
		this.controller = controller;
		this.localIPAddress = properties.getProperty("clusterlsnaddress");
		this.port = properties.getIntProperty("clusterlsnport");
		
		if (alreadyInstantiated == true) 
		{ 
			Logger.error("Looks like you're trying to spin up two DXSpider threads. Not good. Exiting.");
			System.exit(-1);			
		}		
		alreadyInstantiated = true;
	}
	
	
	@Override
	public void run() {
		Logger.info("Spinning up enCC Thread.");
		
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
//				

				//Now that we're set up, send the welcome message...
				Logger.info("nCC System has connected: [" + clientSocket.getInetAddress().toString() + "]");
				//Logger.info("Start HeartBeating...");
				
				
				
				
				byte[] inputData = new byte[5000];
				
				//Thread.sleep(300);
				while(true)
				{
					

					while(in.available()>0)
			        {
						inputData = new byte[5000];
			            // read the byte and convert the integer to character
			            int len = in.read(inputData);
			           
			            //TODO: This needs to be done properly....  Not sure how but needs to take byte[] and work out what it is e.t.c
			            //ConnectionStatusMessage message = new ConnectionStatusMessage(inputData, len);
			            
			            //Logger.info("Recieved Heartbeat: " + message);
			            this.controller.startHeartbeatThread();
			        }
					
				
				//Logger.debug("Session Complete. Looping back to wait for more data");
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