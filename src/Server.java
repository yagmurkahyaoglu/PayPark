import java.net.*;

import com.google.gson.JsonObject;

import java.io.*; 

public class Server 
{ 
	//initialize socket and input stream 
	private Socket          socket   = null; 
	private ServerSocket    server   = null; 
	private DataInputStream in       = null;
	private Database db = new Database();

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

				// takes input from the client socket 
				InputStream is = socket.getInputStream();
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            
				String line = "";
				String message = "";

				try{

					// reads message from client
					line = br.readLine(); 
					System.out.println(line); 
					if(line.contains("open")) {
						message = db.openRecord(line);
					} else if(line.contains("close")){
						message = db.closeRecord(line);
					} else if(line.contains("fare")){
						db.updateBalance(line);
						JsonObject response = new JsonObject();
			    		response.addProperty("status", "success");
			    		message = response.toString();
					} else if(line.contains("gate")){
						JsonObject response = new JsonObject();
			    		response.addProperty("status", "success");
			    		message = response.toString();
			    		System.out.println("Gate opened!");
					}
					
					

					message = message + "\n";
					OutputStream os = socket.getOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(os);
					BufferedWriter bw = new BufferedWriter(osw);
					bw.write(message);
					bw.flush();



				} catch(IOException i){
					System.out.println(i); 
				}

				System.out.println("Closing connection"); 

				// close connection 
				socket.close(); 
				//is.close(); 
			} 
		}
		catch(IOException i) 
		{ 
			System.out.println(i); 
		} 

	} 

	public static void main(String args[]) 
	{ 
		Server server = new Server(5000); 
	} 
} 