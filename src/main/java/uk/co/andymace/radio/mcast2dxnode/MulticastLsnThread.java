package uk.co.andymace.radio.mcast2dxnode;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MulticastLsnThread implements NewSpotListener {

	private static boolean alreadyInstantiated = false;

	private Thread listenerThread;

	private List<NewSpotListener> listeners = new ArrayList<NewSpotListener>();
	private static final Logger Logger = LogManager.getLogger(MulticastLsnThread.class.getName());

	public MulticastLsnThread(InetAddress multicastAddress, int multicastPort, InetAddress sourceip) {
		
		
		Logger.info("Starting Networker");
		
		// This is a quick and dirty check that we're definitely a singleton.
		if (alreadyInstantiated == true) { 
			Logger.error("Looks like you're trying to spin up two Network Listeners. Not good. Exiting.");
			System.exit(-1);
			}
		

		alreadyInstantiated = true;
				
		NetworkThread networkThread = new NetworkThread(multicastAddress, multicastPort, sourceip);		
		networkThread.addListener(this);
		listenerThread = new Thread(networkThread);

	}
	public void startUpListener() {
		// Spin up thread to wait for connections
		Logger.debug("Starting Network Thread... (to listen for DXC MulticastPackets)");		
		listenerThread.setName("NetworkListenerThread");
		listenerThread.start();



	}

	public void gracefulShutdown() {
		Logger.debug("Gracefully shutting down. Sending request to Thread");
		listenerThread.interrupt();
		
	}

	private void notifyListeners(String pcstring) {
        for (NewSpotListener listener : listeners) {
            listener.newEventFired(pcstring);
        }
    }

    public void addListener(NewSpotListener newListener) {
        listeners.add(newListener);
    }
    
	@Override
	public void newEventFired(String message) {
		notifyListeners(message);
		
	}
}
