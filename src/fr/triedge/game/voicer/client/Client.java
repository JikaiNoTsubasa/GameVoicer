package fr.triedge.game.voicer.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable{
	
	private int port;
	private String host;
	private Socket socket;
	private String pseudo;
	private PrintWriter writer = null;
	private BufferedInputStream reader = null;
	
	public Client(String host, int port) {
		setHost(host);
		setPort(port);
		
	}

	public static void main(String[] args) {
		int port = 2020;
		String host = "localhost";
		if (args != null &&  args.length > 0) {
			host = args[0];
			port = Integer.parseInt(args[1]);
		}
		Client client = new Client(host, port);
		Thread th = new Thread(client);
		th.start();
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		Scanner scan = new Scanner(System.in);
		print("Entrez votre pseudo:");
		follow();
		pseudo = scan.nextLine();
		
		
		
		print("Connecting to "+getHost()+":"+getPort()+"...");
		try {
			setSocket(new Socket(getHost(), getPort()));
			writer = new PrintWriter(getSocket().getOutputStream());
			reader = new BufferedInputStream(getSocket().getInputStream());
			print("Connected to server");
			print("Registering pseudo...");
			send("PSEUDO",pseudo);
			String mes = receive();
			if (mes.equalsIgnoreCase("OK")) {
				print("Server OK!");
			}
			// Start updater
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(!getSocket().isClosed()) {
						try {
							String res = receive();
							String[] ress = res.split("_:_");
							switch (ress[0]) {
							case "OK":
								print("Server OK!");
								break;
							case "QUEUE":
								print(ress[1]+": "+ress[2]);
								break;
							case "NEW":
								print(ress[1]+" est connecté");
								break;

							default:
								print("Unkonwn message: "+ress[0]);
								break;
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
			
			/*
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					send("QUEUE","OK");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
			*/
			
			readEntry(scan);
			send("CLOSE","#");
		} catch (IOException e) {
			print("Cannot contact server");
			print("Exiting...");
		}
		scan.close();
	}
	
	public void readEntry(Scanner scan) {
		String line = "";
		while(!line.equalsIgnoreCase("exit")) {
			follow();
			line = scan.nextLine();
			processLine(line);
		}
	}
	
	private void processLine(String line) {
		send("SEND", line);
	}

	public void send(String code, String text) {
		writer.write(code+"_:_"+text);
		writer.flush();
	}
	
	private String receive() throws IOException{      
		String response = "";
		int stream;
		byte[] b = new byte[4096];
		stream = reader.read(b);
		response = new String(b, 0, stream);
		return response;
	}
	
	public void follow() {
		System.out.print("> ");
	}

	public void print(String text) {
		System.out.println(text);
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}
}
