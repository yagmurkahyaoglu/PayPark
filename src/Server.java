import java.net.*;

import com.google.gson.JsonObject;

import java.io.*; 

public class Server 
{ 
	//initialize socket and input stream 
	private Socket          socket   = null; 
	private ServerSocket    server   = null; 
	private DataInputStream in       = null;
	static Database db = new Database();

	// constructor with port 
	public Server(int port) 
	{ 
		// starts server and waits for a connection 
		try
		{ 
			server = new ServerSocket(port); 
			System.out.println("Server started"); 

			System.out.println("Waiting for a client ...");

			while(true) {

				socket = server.accept(); 
				System.out.println("Client accepted"); 
				ServerThread thread = new ServerThread(socket);
	            thread.start();
			}
				
		}
		catch(IOException i) 
		{ 
			System.out.println(i); 
		} 

	} 

	public static void main(String args[]) 
	{ 
		Server server = new Server(6000); 
	} 
} 