package eth.fimeier.bitcoin.webserver;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;

public class bitcoinv15_Web_Start {

	public static void main(String[] args) throws Exception {

		/**
		 * BitcoinV15 for VM fm
		 * 
		 *
		 *
		 */
		
		String bitcoinVersion = "Bitcoin 0.15.0";
		
		String serverName = InetAddress.getLocalHost().getHostName();
		
		/*
		 * Webserver Settings
		 */
		int webServerPort = 8000;
		//String pathStaticPages = "/home/fimeier/Dropbox/00ETH/Bachelor-Arbeit/00Code/vmubuntu/Bitcoin-JSON-RPC-Client/src/eth/fimeier/bitcoin/webserver/staticPages/";
		String absolutePath = new File("").getAbsolutePath() ;
	//	String abP = new File(Webserver.pa .getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getPath();
		
		//String pathStaticPages = absolutePath + "\\src\\eth\\fimeier\\bitcoin\\webserver\\staticPages\\";
		String pathStaticPages = absolutePath + "/src/eth/fimeier/bitcoin/webserver/staticPages/";

	
		/*
		 * RPC-Settings
		 */
		String user = "fimeier";
		String password = "freefood";
		String host = "127.0.0.1";
		String port = "8332";
		
		
		String url =  "http://" + user + ':' + password + "@" + host + ":" + port;
		URL rpcUrl = new URL(url);

		System.out.println("Start webserver for "+bitcoinVersion);
		Webserver web_bitcoin = new Webserver(webServerPort, rpcUrl, bitcoinVersion, pathStaticPages, serverName);
		System.out.println("Return from: Start webserver for "+bitcoinVersion);

	}
}



