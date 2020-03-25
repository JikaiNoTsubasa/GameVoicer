package fr.triedge.game.voc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;

public class VocServer implements Runnable{
	
	private Properties config;
	private boolean running = true;
	private ArrayList<ClientProcess> clients = new ArrayList<>();

	public static void main(String[] args) {
		VocServer s = new VocServer();
		new Thread(s).start();
	}

	public Properties getConfig() {
		return config;
	}

	public void setConfig(Properties config) {
		this.config = config;
	}

	@Override
	public void run() {
		try {
			loadConfig();
			ServerSocket server = new ServerSocket(Integer.parseInt(getConfig().getProperty("server.port")));
			System.out.println("Server started...");
			while(isRunning()) {
				Socket sok = server.accept();
				System.out.println("Connection received");
				ClientProcess proc = new ClientProcess();
				proc.setSocket(sok);
				new Thread(proc).start();
				getClients().add(proc);
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadConfig() throws FileNotFoundException, IOException {
		setConfig(new Properties());
		getConfig().load(new FileInputStream(new File("conf/server.properties")));
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public void sendAll(Message mes) {
		for (ClientProcess p : getClients()) {
			p.send(mes.toString());
		}
	}
	
	public ArrayList<ClientProcess> getClients() {
		return clients;
	}

	public void setClients(ArrayList<ClientProcess> clients) {
		this.clients = clients;
	}

	class ClientProcess implements Runnable{
		private Socket socket;
		private PrintWriter writer = null;
		private BufferedInputStream reader = null;
		private boolean running = true;
		private String pseudo, host;

		public Socket getSocket() {
			return socket;
		}

		public void setSocket(Socket socket) {
			this.socket = socket;
			try {
				setWriter(new PrintWriter(socket.getOutputStream()));
				setReader(new BufferedInputStream(socket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void close() {
			setRunning(false);
			setWriter(null);
			setReader(null);
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			getClients().remove(this);
		}
		
		public void send(String text) {
			getWriter().write(text);
			getWriter().flush();
		}
		
		public void send(Message mes) {
			send(mes.toString());
		}

		@Override
		public void run() {
			System.out.println("ClientProcess started");
			while(!socket.isClosed()) {
				try {
					System.out.println("Waiting for message");
					String text = receive();
					Message mes = new Message(text);
					System.out.println("Received message: "+mes.toString());
					switch (mes.code) {
					case "MES":
						sendAll(mes);
						break;
					case "CLOSE":
						close();
						break;
					case "JOIN":
						actionJoin(mes);
						break;
					case "VOCAL":
						startVocal(mes);
						break;
					default:
						break;
					}
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
				
			}
			close();
		}
		
		private void startVocal(Message mes) {
			//getClients()
		}

		private void actionJoin(Message mes) {
			setPseudo(mes.elements[0]);
			setHost(mes.elements[1]);
			Message rep = new Message("SRV", getPseudo()+" joined");
			sendAll(rep);
		}

		private String receive() throws IOException{      
			String response = "";
			int stream;
			byte[] b = new byte[4096];
			stream = this.reader.read(b);
			response = new String(b, 0, stream);
			return response;
		}

		public PrintWriter getWriter() {
			return writer;
		}

		public void setWriter(PrintWriter writer) {
			this.writer = writer;
		}

		public BufferedInputStream getReader() {
			return reader;
		}

		public void setReader(BufferedInputStream reader) {
			this.reader = reader;
		}

		public boolean isRunning() {
			return running;
		}

		public void setRunning(boolean running) {
			this.running = running;
		}

		public String getPseudo() {
			return pseudo;
		}

		public void setPseudo(String pseudo) {
			this.pseudo = pseudo;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}
		
	}

}
