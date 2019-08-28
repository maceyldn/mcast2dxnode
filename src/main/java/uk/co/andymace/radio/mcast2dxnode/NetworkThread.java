package uk.co.andymace.radio.mcast2dxnode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class NetworkThread implements Runnable 
{

	private static final Logger logger = LogManager.getLogger(NetworkThread.class.getName());

	private NetworkInterface ni;
	private MulticastSocket socket;
	protected byte[] buf = new byte[6144];
	private InetAddress group;
	
		
	public NetworkThread (InetAddress multicastAddress, int multicastPort, InetAddress sourceip )
	{
		logger.debug("Instatiating a MulticastListenerThread");
		try {
			socket = new MulticastSocket(multicastPort);
			group = multicastAddress;
            ni = NetworkInterface.getByInetAddress(sourceip);
			
			if (ni == null)
			{
				logger.fatal("Cannot bind to network interface");
				System.exit(0);
			}
			
			socket.setNetworkInterface(ni);
	        socket.joinGroup(group); 
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	@Override
	public void run() 
	{		
		logger.info("The NetworkThread thread is now spinning.");				
		while(true) 
		{
			listenformulticastpackets();
		}

	}
	
	
	/**
	 *  Will listen and wait for connections from potential Subscribers to register.
	 */
	private void listenformulticastpackets()
	{
		try 
		{
			  DatagramPacket packet = new DatagramPacket(buf, buf.length);
	            socket.receive(packet);
	            String received = new String(packet.getData(), 0, packet.getLength());
 	           logger.info(received);
 	           

		} catch (IOException e) {
			//Logger.error("Exception caught when trying to listen on IP:port ["+ localIPAddress+":"+ port + "] inetSocket: [" + inetSocket.getHostString() + "] or listening for a connection. It *might* be in use. Trying again in " + retryTimeInMS + " msecs. Error is ["+e.getMessage()+"]");
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException exception) { // We do this as part of graceful close-down
				logger.error("Received an InterruptedException. [" + exception.getMessage() + "]. Closing down the NetworkThread socket.");
				closeDownSocket();
			}
		}			
	}
	
	private void closeDownSocket() 
	{
		 try {
			socket.leaveGroup(group);
			 socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		
	}


	

}
