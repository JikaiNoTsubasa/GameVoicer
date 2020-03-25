package fr.triedge.game.voicer.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import fr.triedge.game.voicer.common.NetSession;

public class VoiceServer implements Runnable{
	
	private ArrayList<NetSession> clients = new ArrayList<>();
	private boolean running = true;
	private Properties config = new Properties();
	private long currentClientId = 0;
	private static Logger log;

	public static void main(String[] args) {
		VoiceServer server = new VoiceServer();
		try {
			server.init();
			Thread th = new Thread(server);
			th.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void init() throws FileNotFoundException, IOException {
		// Config logger
		configureLogger();
		
		// Load config
		getConfig().load(new FileInputStream(new File("conf/server.properties")));
	}
	
	private void configureLogger() throws FileNotFoundException, IOException {
		// Set configuration file for log4j2
		ConfigurationSource source = new ConfigurationSource(new FileInputStream("conf/log4j2.xml"));
		Configurator.initialize(null, source);
		log = LogManager.getLogger(VoiceServer.class);
	}

	public ArrayList<NetSession> getClients() {
		return clients;
	}

	public void setClients(ArrayList<NetSession> clients) {
		this.clients = clients;
	}

	@Override
	public void run() {
		int port = Integer.parseInt(getConfig().getProperty("listener.port", "1989"));
		log.info("Starting server with port "+port+"...");
		try {
			ServerSocket server = new ServerSocket(port);
			while(isRunning()) {
				Socket sok = server.accept();
				log.info("Connection received from "+sok.getInetAddress().getHostAddress()+":"+sok.getPort());
				NetSession session = new NetSession();
				session.setId(++currentClientId);
				session.setSocket(sok);
				session.setIn(new ObjectInputStream(sok.getInputStream()));
				session.setOut(new ObjectOutputStream(sok.getOutputStream()));
				getClients().add(session);
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public Properties getConfig() {
		return config;
	}

	public void setConfig(Properties config) {
		this.config = config;
	}

	public long getCurrentClientId() {
		return currentClientId;
	}

	public void setCurrentClientId(long currentClientId) {
		this.currentClientId = currentClientId;
	}

}
