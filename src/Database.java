import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Database {

	String QRFile = "C:\\Users\\casper\\workspace\\CarPark\\src\\QRCodes";
	String balanceFile = "C:\\Users\\casper\\workspace\\CarPark\\src\\Balance";
	BinarySemaphore mutexQR;
	BinarySemaphore mutexMoney;

	public Database() {
		mutexQR = new BinarySemaphore(false);
		mutexMoney = new BinarySemaphore(false);
	}

	public String openRecord(String line) {

		JsonObject jsonObject = new JsonParser().parse(line).getAsJsonObject();
		int code = jsonObject.get("QR").getAsInt();
		String content = "";
		JsonObject message = new JsonObject();
		boolean found = false;
		JsonArray jsonArray;
		try
		{
			content = new String ( Files.readAllBytes( Paths.get(QRFile) ) );
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		if(content.equals("")){
			jsonArray = new JsonArray();
		} else {
			jsonArray = new JsonParser().parse(content).getAsJsonArray();
			for (int i = 0; i < jsonArray.size(); i++) {
				JsonObject explrObject = (JsonObject) jsonArray.get(i);
				if(explrObject.get("QR").getAsInt() == code) {
					found = true;
					if(explrObject.get("mode").getAsInt() == 0) {
						explrObject.remove("mode");
						explrObject.addProperty("mode", 1);
						explrObject.remove("time");
						explrObject.addProperty("time", System.currentTimeMillis());
						message.addProperty("status", "success");

					} else {
						message.addProperty("status", "failure");
					}
				}
			}
		}

		if(!found) {
			JsonObject newQr = new JsonObject();
			newQr.addProperty("QR", code);
			newQr.addProperty("mode", 1);
			newQr.addProperty("time", System.currentTimeMillis());
			jsonArray.add(newQr);
			message.addProperty("status", "success");
		}
		try {
			Files.write(Paths.get(QRFile), jsonArray.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return message.toString();
	}

	public void updateBalance(String line) {

		JsonObject jsonObject = new JsonParser().parse(line).getAsJsonObject();
		int payment = jsonObject.get("fareAmount").getAsInt();
		String content = "";
		JsonObject jsonBalance;
		try
		{
			content = new String ( Files.readAllBytes( Paths.get(balanceFile) ) );
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		if(!content.equals("")) {
			jsonBalance = new JsonParser().parse(content).getAsJsonObject();
			int balance = jsonBalance.get("balance").getAsInt();
			balance += payment;
			jsonBalance.remove("balance");
			jsonBalance.addProperty("balance", balance);
		} else {
			jsonBalance = new JsonObject();
			jsonBalance.addProperty("balance", payment);
		}
		try {
			Files.write(Paths.get(balanceFile), jsonBalance.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String closeRecord(String line) {

		JsonObject jsonObject = new JsonParser().parse(line).getAsJsonObject();
		int code = jsonObject.get("QR").getAsInt();
		String content = "";
		JsonObject message = new JsonObject();
		boolean found = false;
		JsonArray jsonArray;
		try
		{
			content = new String ( Files.readAllBytes( Paths.get(QRFile) ) );
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		if(content.equals("")){
			jsonArray = new JsonArray();
		} else {
			jsonArray = new JsonParser().parse(content).getAsJsonArray();
			for (int i = 0; i < jsonArray.size(); i++) {
				JsonObject explrObject = (JsonObject) jsonArray.get(i);
				if(explrObject.get("QR").getAsInt() == code) {
					found = true;
					if(explrObject.get("mode").getAsInt() == 1) {
						long start = explrObject.get("time").getAsLong();
						long end = System.currentTimeMillis();
						explrObject.remove("mode");
						explrObject.addProperty("mode", 0);
						explrObject.remove("time");
						explrObject.addProperty("time", end);
						message.addProperty("status", "success");
						int payment = calculatePayment((end-start)/60000);
						message.addProperty("fare", payment);
					} else {
						message.addProperty("status", "failure");
					}
				}
			}
		}
		if(!found) {
			message.addProperty("status", "failure");
		}
		try {
			Files.write(Paths.get(QRFile), jsonArray.toString().getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return message.toString();
	}

	public int calculatePayment(long minutes) {
		return 5;
	}

}
