package uk.co.andymace.radio.mcast2dxnode;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.airlift.command.Command;
import io.airlift.command.Option;
import io.airlift.command.SingleCommand;

@Command(name = "app", description = "app")
public class App {
	
	private static final Logger logger = LogManager.getLogger(App.class);
	
	@Option(name = {"--config"}, title="Configfile", description = "(default: config.properties)")
	private static String configFilename = "config.properties";

	private static myProperties appProperties;
	
	public App() 
	{
	// needed by airline
	}
	public static void main(String[] args) throws Exception {
		   
		logger.info("***********************************");
		logger.info("*    Multicast 2 DXC              *");
		logger.info("*  Andy Mace / M0MUX 27/08/2019   *");
		logger.info("***********************************");
		
		Properties props = System.getProperties();
		props.setProperty("java.net.preferIPv4Stack","true");
		System.setProperties(props);
		
		SingleCommand.singleCommand(App.class).parse(args);
    	
    	ArrayList<String> requiredProperties = new ArrayList<String>(Arrays.asList("appName", "sourceaddress", "multicastaddress", "multicastport", "clusterlsnaddress", "clusterlsnport"));
    	appProperties = new myProperties(configFilename, requiredProperties);

    	logger.info("App Name is : ["+ appProperties.getProperty("appName") +"]");
    	//Logger.info("Listening on: ["+ appProperties.getProperty("localIPAddress") + ":" + appProperties.getProperty("listeningPort")+ "]");
    	logger.info("====================================================");

    	
		logger.info("Setting up Multicast Backend");
		
		int multicastPort = appProperties.getIntProperty("multicastport");
		InetAddress multicastAddress = InetAddress.getByName(appProperties.getProperty("multicastaddress"));
		InetAddress sourceAddress = InetAddress.getByName(appProperties.getProperty("sourceaddress"));
		
		DXSpiderThreadController dxc = new DXSpiderThreadController(appProperties);
		dxc.startUpListener();
		
		MulticastLsnThread mcast = new MulticastLsnThread(multicastAddress, multicastPort, sourceAddress);
		mcast.startUpListener();
		mcast.addListener(dxc);
		
		
		//logger.info("Setting up DXNode");
		
		//dxc.addListener(mcast);
		  
	}

	

}
