package uk.co.andymace.radio.mcast2dxnode;


import java.io.BufferedOutputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;

public class DXSpiderThreadController implements NewSpotListener {

	private static boolean alreadyInstantiated = false;

	private Thread thread;
	private static final Logger Logger = LogManager.getLogger(DXSpiderThreadController.class.getName());
	private myProperties properties;
	private BufferedOutputStream threadNetworkOutputStream; 
	
	DXSpiderNetworkThread dxspiderNetworkThread;

	public DXSpiderThreadController (myProperties properties) 
	{
		Logger.info("Starting DXSpider Thread Controller");
		this.properties = properties;
		
		
		// This is a quick and dirty check that we're definitely a singleton.
		if (alreadyInstantiated == true) 
		{ 
			Logger.error("Looks like you're trying to spin up two DXSpider Thread Controllers. Not good. Exiting.");
			System.exit(-1);
		}
		

		alreadyInstantiated = true;
				
		dxspiderNetworkThread = new DXSpiderNetworkThread(this.properties, this);		
		thread = new Thread(dxspiderNetworkThread);
		
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
	
	public void setNetworkOutputStream(BufferedOutputStream out)
	{
		this.threadNetworkOutputStream = out;
		Logger.debug("Sending out to controller. Out is: "+ threadNetworkOutputStream.toString());
	}

	@Override
	public void newEventFired(String message) {
		try {
		if (threadNetworkOutputStream != null)
		{
			if(dxspiderNetworkThread.state == PCSTATE.initcomplete)
			{
			
				Logger.debug("SEND: " + message);
				Logger.info("Sent Spot");
			threadNetworkOutputStream.write((message+"\n").getBytes(Charsets.UTF_8));
			threadNetworkOutputStream.flush();
		
		}
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}