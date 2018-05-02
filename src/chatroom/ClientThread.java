package chatroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientThread extends Thread{
	private final ClientThread[] threads;
	private final Socket clientSocket;
	private final int maxClientsCount;
	private PrintWriter out = null;
	private String clientName = null;
	
	ClientThread(Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.threads = ThreadedServer.getThreads();
		maxClientsCount = ThreadedServer.getThreads().length;
	}
	
	public void run() {
		int maxClientsCount = this.maxClientsCount;
		ClientThread[] threads = this.threads;
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			out = new PrintWriter(clientSocket.getOutputStream());
			String name;
			while(true) {
				out.println("Enter your name: ");
				name = in.readLine().trim();
				if(name.indexOf('@') == -1) {
					break;
				}
				else {
					out.println("The name shouldn't contain the '@' character");
				}
			}
			out.println("Welcome to the server, " + name + ". Type /quit to leave");
			
			synchronized(this) {
				for(int i = 0;i < maxClientsCount;i++) {
					if(threads[i] != null && threads[i] == this) {
						this.clientName = "@" + name;
						break;
					}
				}
				for(int i = 0;i < maxClientsCount;i++) {
					if(threads[i] != null && threads[i] != this) {
						out.println("***User " + name + "has joined the chat room***");
					}
				}
			}
			
			while(true) {
				String line = in.readLine();
				if(line.startsWith("/quit")) {
					break;
				}
				if(line.startsWith("@")) {
					String[] words = line.split("\\s", 2);
					if(words.length > 1 && words[1] != null) {
						words[1] = words[1].trim();
						if(!words[1].isEmpty()) {
							synchronized(this) {
								for(int i = 0;i < maxClientsCount;i++) {
									if(threads[i] != null && threads[i] != this
										&& threads[i].clientName != null
										&& threads[i].clientName.equals(words[0])) {
										threads[i].out.println("<" + name + "> " + words[1]);
										this.out.println(">" + name + "> "+ words[1]);
										break;
									}
								}
							}
						}
					}
				}
				else {
					synchronized(this) {
						for(int i = 0;i < maxClientsCount;i++){
							if(threads[i] != null && threads[i].clientName != null){
								threads[i].out.println("<" + name + "> " + line);
							}
						}
					}
				}
			}

			synchronized(this){
				for(int i = 0;i < maxClientsCount;i++){
					if(threads[i] != null && threads[i] != this
						&& threads[i].clientName != null){
						threads[i].out.println("***User "+ name + " is leaving the chatroom***");
					}
				}
			}

			synchronized(this){
				for(int i = 0;i < maxClientsCount;i++){
					if(threads[i] == this){
						threads[i] = null;
					}
				}
			}

			in.close();
			out.close();
			clientSocket.close();
		}
		catch(IOException e) {
			System.out.println("IO error in server thread!");
			e.printStackTrace();
		}
		finally{
			try{
				if(clientSocket != null){
					clientSocket.close();
				}
			}
			catch(IOException e){
				System.out.println("IO error in finally");
				e.printStackTrace();
			}
		}
	}
}
