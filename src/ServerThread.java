import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.google.gson.JsonObject;

public class ServerThread extends Thread {

	Socket socket;
	Database db = Server.db;

	public ServerThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {

		try{

			// takes input from the client socket 
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			String line = "";
			String message = "";

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
				message = db.gateAvailability();
				System.out.println("Gate opened!");
			}

			message = message + "\n";
			OutputStream os = socket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(message);
			bw.flush();

			System.out.println("Closing connection"); 

			// close connection 
			socket.close(); 
			//is.close(); 

		} catch(IOException i){
			System.out.println(i); 
		}
	}

}
