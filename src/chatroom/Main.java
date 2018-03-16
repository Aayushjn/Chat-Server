package chatroom;

import javax.swing.JOptionPane;

public class Main{
	private static Object[] selectionValues = {"Server", "Client"};
	private static String initialSelection = "Server";

	public static void main(String[] args) {
		Object selection = JOptionPane.showInputDialog(null, "Login as : ", "ChatRoom", JOptionPane.QUESTION_MESSAGE, null, selectionValues, initialSelection);
		if("Server".equals(selection)){
			String[] arguments = new String[] {};
			new ThreadedServer();
			ThreadedServer.main(arguments);
		}
		else if("Client".equals(selection)){
			String IPServer = JOptionPane.showInputDialog("Enter the server IP address");
			String[] arguments = new String[] {IPServer};
			new ThreadedClient();
			ThreadedClient.main(arguments);
		}
	}
}