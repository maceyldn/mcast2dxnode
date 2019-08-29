package uk.co.andymace.radio.mcast2dxnode;

import java.io.BufferedOutputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DXSpiderHeartbeatThread implements Runnable {

	private static final Logger Logger = LogManager.getLogger(DXSpiderHeartbeatThread.class.getName());
	
	private BufferedOutputStream out;
		
	public DXSpiderHeartbeatThread (BufferedOutputStream _out) 
	{
		Logger.debug("Instatiating the DXSpider Heartbeat Thread");
		this.out = _out;
	}

	@Override
	public void run() {
		Logger.info("Spinning up DXSpider Heartbeat Thread.");
		
		while(true) {
			try {
				Thread.sleep(15000); // Sleep 15 seconds
				if (this.out != null) {
					
					
					//Logger.debug("Sending Heartbeat: " + message);
					//this.out.write();
					//this.out.flush();
				
				} else {
					Logger.error("No Output Stream");
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void setOutputStream(BufferedOutputStream threadNetworkOutputStream) {
		out = threadNetworkOutputStream;
		
	}	
	
	
	
	
}