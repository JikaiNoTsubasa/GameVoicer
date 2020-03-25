package fr.triedge.game.voicer.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


public class SrvClient {

	private Socket socket;
	private PrintWriter writer = null;
	private BufferedInputStream reader = null;
	private String pseudo;
	
	public SrvClient(Socket socket) throws IOException {
		setSocket(socket);
		setWriter(new PrintWriter(getSocket().getOutputStream()));
		setReader(new BufferedInputStream(getSocket().getInputStream()));
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
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

	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}
}
