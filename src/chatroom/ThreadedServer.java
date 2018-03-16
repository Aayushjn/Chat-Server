package chatroom;

import java.io.IOException;
import java.io.PrintStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadedServer{
	private static ServerSocket serverSocket = null;
	private static Socket clientSocket = null;

	private static final int maxClientsCount = 10;
	private static final ClientThread[] threads = new ClientThread[maxClientsCount];
	
	public static void main(String[] args){
		int portNumber = 2222;
		
		if(args.length < 1){
			System.out.println("Usage: java ThreadedServer <portNumber>\n" + 
				"Using default port number " + portNumber);
		}
		else{
			portNumber = Integer.valueOf(args[0]).intValue();
		}

		try {
			serverSocket = new ServerSocket(portNumber);
		}
		catch(IOException e) {
			System.out.println("Server error!");
			e.printStackTrace();
		}
		finally {
			if(serverSocket != null) {
				try {
					serverSocket.close();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		while(true) {
			try {
				clientSocket = serverSocket.accept();
				int i = 0;
				for(i = 0; i < maxClientsCount;i++){
					if(threads[i] == null){
						(threads[i] = new ClientThread(clientSocket, threads)).start();
						break;
					}
				}
				if(i == maxClientsCount){
					PrintStream ps = new PrintStream(clientSocket.getOutputStream());
					ps.println("Server too busy. Try later!");
					ps.close();
					clientSocket.close();
				}
			}
			catch(BindException e){
				System.out.println("Bind error!");
			}
			catch(IOException e) {
				System.out.println("Connection error!");
			}
			finally{
				if(serverSocket != null){
					try{
						serverSocket.close();
					}
					catch(IOException e){
						System.out.println("Error closing the server socket!");
						e.printStackTrace();
					}
				}
			}
		}
	}
}
