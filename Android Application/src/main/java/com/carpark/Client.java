package com.carpark;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client extends AsyncTask<Void, Void, String> {

    private String message = "";
    private Socket socket = null;
    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncResponse delegate = null;

    public Client(String message, AsyncResponse delegate){
        this.message = message;
        this.delegate = delegate;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String answer = "";
        try
        {
            //String host = "192.168.43.55";
            String host = "18.194.220.198";
            int port = 6000;
            socket = new Socket(host, port);
            Log.d("client" ,"connected");
            //Send the message to the server
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);

            String sendMessage = this.message;
            bw.write(sendMessage);
            bw.newLine();
            bw.flush();
            Log.d("client","Message sent to the server : " + sendMessage);

            //Get the return message from the server
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            answer = br.readLine();
            Log.d("client","Message received from the server : " +answer);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        finally
        {
            //Closing the socket
            try
            {
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        return answer;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        delegate.processFinish(s);
    }
}
