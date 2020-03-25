package fr.triedge.game.voicer.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

public class ServerDispatcher implements Runnable{
	
	private int port;
	private boolean running = true;
	private static Logger log;
	private ArrayList<SrvClient> clients = new ArrayList<>();
	private ArrayList<String> messageQueue = new ArrayList<>();
	
	public ServerDispatcher(int port) {
		setPort(port);
		try {
			configureLogger();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		int port = 2020;
		if (args != null &&  args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		ServerDispatcher dispatch = new ServerDispatcher(port);
		Thread th = new Thread(dispatch);
		th.start();
	}

	@Override
	public void run() {
		try {
			ServerSocket listener = new ServerSocket(getPort());
			log.info("Starting server with port: "+getPort());
			while(isRunning()) {
				Socket sok = listener.accept();
				SrvClient client = new SrvClient(sok);
				getClients().add(client);
				Thread th = new Thread(new ProcessClient(client, this));
				th.start();
			}
			listener.close();
			log.info("Server stopped");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
	private void configureLogger() throws FileNotFoundException, IOException {
		// Set configuration file for log4j2
		ConfigurationSource source = new ConfigurationSource(new FileInputStream("conf/log4j2.xml"));
		Configurator.initialize(null, source);
		log = LogManager.getLogger(ServerDispatcher.class);
	}

	public ArrayList<SrvClient> getClients() {
		return clients;
	}

	public void setClients(ArrayList<SrvClient> clients) {
		this.clients = clients;
	}

	public void sendAll(String text) {
		for (SrvClient c : getClients()) {
			c.getWriter().write(text);
			c.getWriter().flush();
		}
		//getMessageQueue().add(text);
	}

	public ArrayList<String> getMessageQueue() {
		return messageQueue;
	}

	public void setMessageQueue(ArrayList<String> messageQueue) {
		this.messageQueue = messageQueue;
	}

}
