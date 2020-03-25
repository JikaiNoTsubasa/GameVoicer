package fr.triedge.game.voicer.server;

import java.io.IOException;

public class ProcessClient implements Runnable{

	private SrvClient client;
	
	private ServerDispatcher dispatcher;
	private String sep = "_:_";

	public ProcessClient(SrvClient client, ServerDispatcher dispatcher) {
		setClient(client);
		setDispatcher(dispatcher);
	}

	@Override
	public void run() {
		boolean closeConnexion = false;
		while(!getClient().getSocket().isClosed()) {
			try {
				
				String response = read();
				String[] text = response.split(sep);
				String sendClient = "";

				switch (text[0]) {
				case "SEND":
					getDispatcher().sendAll("QUEUE_:_"+getClient().getPseudo()+"_:_"+text[1]);
					sendClient = "OK";
					break;
				case "PSEUDO":
					getClient().setPseudo(text[1]);
					sendClient = "OK";
					System.out.println("Registered pseudo: "+getClient().getPseudo());
					getClient().getWriter().write(sendClient);
					getClient().getWriter().flush();
					getDispatcher().sendAll("NEW_:_"+getClient().getPseudo());
					break;
				case "CLOSE":
					closeConnexion = true;
					System.out.println("Client closed connection");
					break;

				default:
					break;
				}

				if(closeConnexion){
					getClient().setWriter(null);
					getClient().setReader(null);
					getClient().getSocket().close();
					break;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private String read() throws IOException{      
		String response = "";
		int stream;
		byte[] b = new byte[4096];
		stream = getClient().getReader().read(b);
		response = new String(b, 0, stream);
		return response;
	}

	public SrvClient getClient() {
		return client;
	}

	public void setClient(SrvClient client) {
		this.client = client;
	}

	public ServerDispatcher getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(ServerDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

}
