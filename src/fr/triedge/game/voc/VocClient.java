package fr.triedge.game.voc;

import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class VocClient extends JFrame implements WindowListener{
	
	private static final long serialVersionUID = 4350860911209770732L;
	private Socket socket;
	private PrintWriter writer = null;
	private BufferedInputStream reader = null;
	private JTextArea textArea;
	private JTextArea writeArea;
	private Properties config;
	private String pseudo;
	
	public void build() {
		this.addWindowListener(this);
		this.setTitle("Game Voicer");
		this.setSize(800, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		
		JMenuBar bar = new JMenuBar();
		JMenu vocal = new JMenu("Vocal");
		JMenuItem itemLaunchVocal = new JMenuItem("Lancer Vocal");
		itemLaunchVocal.addActionListener(e -> startVocal());
		
		bar.add(vocal);
		vocal.add(itemLaunchVocal);
		setJMenuBar(bar);
		
		setTextArea(new JTextArea());
		getTextArea().setEditable(false);
		JScrollPane scroll = new JScrollPane(getTextArea());
		
		setWriteArea(new JTextArea());
		getWriteArea().requestFocus();
		JScrollPane scroll2 = new JScrollPane(getWriteArea());
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(e -> sendChat());
		
		JPanel pan = new JPanel(new GridLayout(1, 2));
		pan.add(scroll2);
		pan.add(btnSend);
		
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.add(scroll);
		split.add(pan);
		
		setContentPane(split);
		
		this.setVisible(true);
		
		SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                split.setDividerLocation(0.8);
            }
        });
	}
	
	public void startVocal() {
		print("Starting vocal...");
		print("********************************************************");
		print("* Only people currently in the room will get connected *");
		print("********************************************************");
		Message mes = new Message("VOCAL", "OK");
	}
	
	public void sendChat() {
		String txt = getWriteArea().getText();
		getWriteArea().setText("");
		Message mes = new Message("MES", getPseudo(), txt);
		send(mes);
	}
	
	public void send(Message mes) {
		getWriter().write(mes.toString());
		getWriter().flush();
		//System.out.println("Sent: "+mes.toString());
	}
	
	public void print(String text) {
		SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                getTextArea().append(text+"\r\n");
            }
        });
	}
	
	public String readEntry() {
		Scanner scan = new Scanner(System.in);
		String line = scan.nextLine();
		scan.close();
		return line;
	}
	
	public void start() {
		System.out.println("Entrez votre pseudo:");
		System.out.print("> ");
		setPseudo(readEntry());
		build();
		print("Loading config...");
		try {
			loadConfig();
			print("Config loaded");
			print("Starting chat...");
			print("Connecting to server...");
			connectToServer();
			print("Connected");
			sendJoin();
			startListener();
		} catch (IOException e) {
			printError(e);
		}
	}
	
	private void sendJoin() {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getLocalHost();
			Message mes = new Message("JOIN", getPseudo(), inetAddress.getHostAddress());
			send(mes);
		} catch (UnknownHostException e) {
			printError(e);
		}
	}

	public void connectToServer() throws UnknownHostException, IOException {
		String host = config.getProperty("server.host");
		int port = Integer.parseInt(config.getProperty("server.port"));
		
		setSocket(new Socket(host, port));
		setWriter(new PrintWriter(getSocket().getOutputStream()));
		setReader(new BufferedInputStream(getSocket().getInputStream()));
	}
	
	private void startListener() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (getWriter() != null) {
					while(!getSocket().isClosed()) {
						try {
							String text = receive();
							Message mes = new Message(text);
							//System.out.println("Received: "+mes.toString());
							switch (mes.code) {
							case "MES":
								print(mes.elements[0]+": "+mes.elements[1]);
								break;
							case "SRV":
								print("SERVER: "+mes.elements[0]);
								break;
							default:
								break;
							}
						} catch (IOException e) {
							printError(e);
						}
					}
				}
			}
		}).start();
	}

	public static void main(String[] args) {
		VocClient c = new VocClient();
		c.start();
	}
	
	private void loadConfig() throws FileNotFoundException, IOException {
		setConfig(new Properties());
		getConfig().load(new FileInputStream(new File("conf/client.properties")));
	}
	
	public void printError(Exception e) {
		print(e.getMessage());
		for (StackTraceElement m : e.getStackTrace()) {
			print(m.toString());
		}
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

	public JTextArea getTextArea() {
		return textArea;
	}

	public void setTextArea(JTextArea textArea) {
		this.textArea = textArea;
	}

	public JTextArea getWriteArea() {
		return writeArea;
	}

	public void setWriteArea(JTextArea writeArea) {
		this.writeArea = writeArea;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	private String receive() throws IOException{      
		String response = "";
		int stream;
		byte[] b = new byte[4096];
		stream = reader.read(b);
		response = new String(b, 0, stream);
		return response;
	}

	public Properties getConfig() {
		return config;
	}

	public void setConfig(Properties config) {
		this.config = config;
	}

	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		this.disconnect();
	}

	private void disconnect() {
		Message mes = new Message("CLOSE", "OK");
		send(mes);
		try {
			setWriter(null);
			setReader(null);
			getSocket().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

}
