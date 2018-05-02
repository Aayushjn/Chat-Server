package chatroom;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ThreadedClient{
	public static void main(String[] args) {
		String server = args[0];
		int port = 4444;
		ChatAccess access = new ChatAccess();

		JFrame frame = new ChatFrame(access);
		frame.setTitle("ChatRoom -- Connected to " + server + ":" + port);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);

		try{
			access.initSocket(server, port);
		}
		catch(IOException e){
			System.out.println("Cannot connect to " + server + ":" + port);
			e.printStackTrace();
			System.exit(0);
		}
	}

	static class ChatAccess extends Observable{
		private static final String CRLF = "\r\n";
		private Socket socket = null;
		private OutputStream os = null;

		@Override
		public void notifyObservers(){
			super.setChanged();
			super.notifyObservers();
		}

		void initSocket(String server, int port) throws IOException{
			socket = new Socket(server, port);
			os = socket.getOutputStream();

			Thread receivingThread = new Thread(() -> {
				try{
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					while((br.readLine()) != null){
						notifyObservers();
					}
				}
				catch(IOException e){
					notifyObservers(e);
				}
			});
			receivingThread.start();
		}

		void send(String text){
			try{
				os.write((text + CRLF).getBytes());
				os.flush();
			}
			catch(IOException e){
				notifyObservers(e);
			}
		}

		void close(){
			try{
				os.close();
				socket.close();
			}
			catch(IOException e){
				notifyObservers(e);
			}
		}
	}

	static class ChatFrame extends JFrame implements Observer{
		private final ChatAccess chatAccess;
		private JButton sendButton;
		private JTextArea textArea;
		private JTextField inputTextField;

		ChatFrame(ChatAccess chatAccess){
			this.chatAccess = chatAccess;
			chatAccess.addObserver(this);
			buildGUI();
		}

		private void buildGUI(){
			textArea = new JTextArea(20, 50);
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			add(new JScrollPane(textArea), BorderLayout.CENTER);

			Box box = Box.createHorizontalBox();
			add(box, BorderLayout.SOUTH);
			inputTextField = new JTextField();
			sendButton = new JButton("Send");
			box.add(inputTextField);
			box.add(sendButton);

			ActionListener sendListener = e -> {
				String str = inputTextField.getText();
				if(str != null && str.trim().length() > 0){
					chatAccess.send(str);
				}
				inputTextField.selectAll();
				inputTextField.requestFocus();
				inputTextField.setText("");
			};
			inputTextField.addActionListener(sendListener);
			sendButton.addActionListener(sendListener);

			this.addWindowListener(new WindowAdapter(){
				public void windowClosing(WindowEvent e){
					chatAccess.close();
				}
			});
		}

		public void update(Observable o, Object obj){
			final Object finalObj = obj;
			SwingUtilities.invokeLater(() -> {
				textArea.append(finalObj.toString());
				textArea.append("\n");
			});
		}
	}
}
