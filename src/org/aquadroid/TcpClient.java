package org.aquadroid;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
//import android.app.Activity;

import android.content.Context;
import android.util.Log;

public class TcpClient {

	private static final String TAG = "TcpClient";

	private Socket socket = null;
    private BufferedWriter bw = null;
    private BufferedReader br = null;
    private InputStream ins = null;
        
    public TcpClient(String hostname, int port, int timeout) throws IOException {
    //public TcpClient(String hostname, int port, String data, String filename) throws IOException
        
        connect(hostname, port, timeout);
    	
    }

    private void connect(String hostname, int port, int timeout) throws IOException
    {
            socket = new Socket();
            SocketAddress endpoint = new InetSocketAddress(hostname, port);
            socket.connect(endpoint, 2000);
            socket.setSoTimeout(timeout);
            
            //VdrDevice vdr = Preferences.getVdr();
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()), 8192);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()), 8192);
            ins = socket.getInputStream();
            //MyLog.v(TAG,"TcpClient instanziert,Socket geoeffnet");

    }
    public void sendData(String data)throws IOException {
        bw.write(data);
        bw.flush();
        //MyLog.v(TAG,"Schreibe Daten in Socket:"+data);
    }
    public String receiveData() throws IOException, SocketTimeoutException {
    	StringBuffer sb = new StringBuffer();
    	String str;
    	while ((str = br.readLine()) != null)
        {
          sb.append(str + "\n");
        }
    
        return sb.toString();

    }
    public void writeFile(Context ctx, String filename) throws IOException {
    	int filesize = 512;
    	int bytesRead;
    	//int current = 0;
    	byte [] mybytearray  = new byte[filesize];
    	    	
    	//FileOutputStream fos = new FileOutputStream(filename);
    	@SuppressWarnings("static-access")
		FileOutputStream fos = ctx.openFileOutput(filename,ctx.MODE_PRIVATE);
    	
    	BufferedOutputStream bos = new BufferedOutputStream(fos);
    	    	
    	do {
    	       bytesRead = ins.read(mybytearray, 0, mybytearray.length);
    	       //Log.e(TAG, "Bytes read = "+ bytesRead+ "data = "+mybytearray.toString());
    	       //bos.write(mybytearray, 0 , bytesRead);
    	       
    	       if(bytesRead >= 0) bos.write(mybytearray, 0 , bytesRead);
    	} while(bytesRead > -1);
    	Log.e(TAG,"close");
    	bos.flush();
    	bos.close();
    }
    public byte[] writeByte() throws IOException {
    	int filesize = 512;
    	int bytesRead;
    	//int current = 0;
    	byte [] mybytearray  = new byte[filesize];
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	    	
    	do {
    	       bytesRead = ins.read(mybytearray, 0, mybytearray.length);
    	       //Log.e(TAG, "Bytes read = "+ bytesRead+ "data = "+mybytearray.toString());
    	       
    	       if(bytesRead >= 0) bos.write(mybytearray, 0 , bytesRead);
    	} while(bytesRead > -1);
    	Log.e(TAG,"close");
    	return bos.toByteArray();
    }
    
	public void close() throws IOException {
    bw.close();
    br.close();
    socket.close();
}


} // end

