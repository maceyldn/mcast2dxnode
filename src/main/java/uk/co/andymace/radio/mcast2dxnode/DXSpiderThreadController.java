package uk.co.andymace.radio.mcast2dxnode;


import java.io.BufferedOutputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DXSpiderThreadController implements ScheduledEventFiredListener {

	private static boolean alreadyInstantiated = false;

	private Thread thread;
	private Thread threadheartbeat;
	private static final Logger Logger = LogManager.getLogger(DXSpiderThreadController.class.getName());
	private myProperties properties;
	private BufferedOutputStream threadNetworkOutputStream; 
	
	DXSpiderNetworkThread enccNetworkThread;
	DXSpiderHeartbeatThread enccHeartbeatThread;
	
	
	public DXSpiderThreadController (myProperties properties) 
	{
		Logger.info("Starting EnCC Thread Controller");
		this.properties = properties;
		
		
		// This is a quick and dirty check that we're definitely a singleton.
		if (alreadyInstantiated == true) 
		{ 
			Logger.error("Looks like you're trying to spin up two EnCC Thread Controllers. Not good. Exiting.");
			System.exit(-1);
		}
		

		alreadyInstantiated = true;
				
		enccNetworkThread = new DXSpiderNetworkThread(this.properties, this);		
		thread = new Thread(enccNetworkThread);
		
		//Something is blocking when we get to here..... the EnCCThread never gets to listen
		enccHeartbeatThread = new DXSpiderHeartbeatThread(this.threadNetworkOutputStream);
		threadheartbeat = new Thread(enccHeartbeatThread);
	}
	
	
	public void startUpListener() 
	{
		// Spin up thread to wait for connections
		Logger.debug("Starting DXSpider Thread...");		
		thread.setName("DXSpiderNetworkThread");
		thread.start();
	}
	
	public void gracefulShutdown() 
	{
		Logger.debug("Gracefully shutting down. Sending request to " + thread.getName());
		thread.interrupt();		
	}

	/*
	 @Override
	 
	public void newEventFired(GasMessage message) 
	{	    
	    String messageDataForLogging = new String(message.createMessageByteList());
	    messageDataForLogging = messageDataForLogging.replaceAll("\\s","<non printing char>"); //Remove all the non-printing characters to make logging neater
	    
		
		if (threadNetworkOutputStream == null) 
		{
			Logger.error("Unable to send Gas Message [" + messageDataForLogging + "] to ESS because the OutputBuffer object is null. This could be because the ESS has not yet responded." );
			return;
		}
		
		
		try 
		{			
			threadNetworkOutputStream.write(message.createMessageByteList());
			threadNetworkOutputStream.flush();
			Logger.info("Message [" + messageDataForLogging + "] published to ESS.");
		} catch (IOException exception) 
		{
			Logger.error("IOException: Unable to send Gas Message [" + messageDataForLogging + "] to ESS. Reason: [" +exception.getMessage()+"]"  );
			exception.printStackTrace();
		}
	
	}
*/
	
	
	public void setNetworkOutputStream(BufferedOutputStream out)
	{
		this.threadNetworkOutputStream = out;
		Logger.debug("Sending out to controller. Out is: "+ threadNetworkOutputStream.toString());
		
		
	}
	
	public void startHeartbeatThread() {
		if (!threadheartbeat.isAlive())
		{
		Logger.debug("Starting EnCC Heartbeat Thread...");		
		enccHeartbeatThread.setOutputStream(this.threadNetworkOutputStream);
		threadheartbeat.setName("EnCCHeartbeatThread");
		threadheartbeat.start();
		}
	}


	@Override
	public void newEventFired(String message) {
		// TODO Auto-generated method stub
		
	}
	
}