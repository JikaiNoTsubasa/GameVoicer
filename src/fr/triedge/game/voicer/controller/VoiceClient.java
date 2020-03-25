package fr.triedge.game.voicer.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

import javax.sound.sampled.LineUnavailableException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import fr.triedge.game.voicer.common.AudioMessageListener;
import fr.triedge.game.voicer.common.Message;
import fr.triedge.game.voicer.common.NetSession;
import fr.triedge.game.voicer.common.SoundPacket;

public class VoiceClient implements Runnable, AudioMessageListener{

	private static Logger log;
	private ArrayList<NetSession> clients = new ArrayList<>();
	private AudioReader reader;
	private AudioRecorder recorder;
	private NetSession serverSession;
	private Properties config = new Properties();

	public void init() {
		// Load logger
		try {
			configureLogger();
		} catch (IOException e1) {
			log.error("Cannot configure logger",e1);
		}
		
		// Load config
		try {
			log.info("Loading config from conf/client.properties...");
			getConfig().load(new FileInputStream(new File("conf/client.properties")));
		} catch (IOException e1) {
			log.error("Cannot load config file",e1);
			System.exit(-1);
		}

		// Login to server
		try {
			log.info("Connecting to server...");
			connectToServer();
		} catch (IOException e1) {
			log.error("Cannot connect to server",e1);
			System.exit(-1);
		}

		log.info("Starting audio speaker...");
		setReader(new AudioReader());
		runThread(getReader());
		try {
			log.info("Starting Mic reader...");
			setRecorder(new AudioRecorder());
			getRecorder().addAudioMessageListener(this);
			runThread(getRecorder());
		} catch (LineUnavailableException e) {
			log.error("Cannot start audio card",e);
			System.exit(-1);
		}
	}

	private void configureLogger() throws FileNotFoundException, IOException {
		// Set configuration file for log4j2
		ConfigurationSource source = new ConfigurationSource(new FileInputStream("conf/log4j2.xml"));
		Configurator.initialize(null, source);
		log = LogManager.getLogger(VoiceClient.class);
	}

	private void connectToServer() throws UnknownHostException, IOException {
		setServerSession(new NetSession());
		getServerSession().setId(1);
		String serverHost = getConfig().getProperty("server.host", "localhost");
		int serverPort = Integer.parseInt(getConfig().getProperty("server.port", "1989"));
		Socket sok = new Socket(serverHost, serverPort);
		if (sok.isConnected()) {
			getServerSession().setSocket(sok);
			getServerSession().setIn(new ObjectInputStream(sok.getInputStream()));
			getServerSession().setOut(new ObjectOutputStream(sok.getOutputStream()));
			log.error("Connected to server with "+serverHost+":"+serverPort);
		}else {
			log.error("Cannot connect to server with "+serverHost+":"+serverPort);
		}
	}

	private void runThread(Runnable runnable) {
		Thread th = new Thread(runnable);
		th.start();
	}

	public static void main(String[] args) {
		VoiceClient c = new VoiceClient();
		c.init();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	public ArrayList<NetSession> getClients() {
		return clients;
	}

	public void setClients(ArrayList<NetSession> clients) {
		this.clients = clients;
	}

	public AudioReader getReader() {
		return reader;
	}

	public void setReader(AudioReader reader) {
		this.reader = reader;
	}

	public AudioRecorder getRecorder() {
		return recorder;
	}

	public void setRecorder(AudioRecorder recorder) {
		this.recorder = recorder;
	}

	@Override
	public void newMessage(Message m) {
		SoundPacket sp = (SoundPacket)m.getData();
		System.out.println("Mess: "+sp.getData());
	}

	public NetSession getServerSession() {
		return serverSession;
	}

	public void setServerSession(NetSession serverSession) {
		this.serverSession = serverSession;
	}

	public Properties getConfig() {
		return config;
	}

	public void setConfig(Properties config) {
		this.config = config;
	}
}
