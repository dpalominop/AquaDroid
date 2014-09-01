package org.aquadroid;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
//import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.net.tftp.TFTP;
import org.apache.commons.net.tftp.TFTPClient;

public class TFTPC {
	private TFTPClient tftp;
	private String hostname;
	private String remoteFilename;
	private String localFilename;
	private int transferMode = TFTP.BINARY_MODE;
	private boolean closed;
	
	public TFTPC(String hostname, String remoteFilename, String localFilename, int transferMode)
	{
		this.hostname = hostname;
		this.remoteFilename = remoteFilename;
		this.localFilename = localFilename;
		this.transferMode = transferMode;
	}
	public boolean getFilename()
	{
		// Create our TFTP instance to handle the file transfer.
        tftp = new TFTPClient();
        
		tftp.setDefaultTimeout(60000);

        // Open local socket
        try
        {
            tftp.open();
        }
        catch (SocketException e)
        {
            System.err.println("Error: could not open local UDP socket.");
            System.err.println(e.getMessage());
            closed = false;
            return closed;
        }
        FileOutputStream output = null;
        File file;

        file = new File(localFilename);

        // If file exists, don't overwrite it.
        if (file.exists())
        {
            file.delete();
        }
     // Try to open local file for writing
        try
        {
            output = new FileOutputStream(file);
        }
        catch (IOException e)
        {
            tftp.close();
            System.err.println("Error: could not open local file for writing.");
            System.err.println(e.getMessage());
            closed = false;
            return closed;
        }

        // Try to receive remote file via TFTP
        try
        {
            tftp.receiveFile(remoteFilename, transferMode, output, hostname);
        }
        catch (UnknownHostException e)
        {
            System.err.println("Error: could not resolve hostname.");
            System.err.println(e.getMessage());
            closed = false;
            return closed;
        }
        catch (IOException e)
        {
            System.err.println("Error: I/O exception occurred while receiving file.");
            System.err.println(e.getMessage());
            closed = false;
            return closed;
        }
        finally
        {
            // Close local socket and output file
            tftp.close();
            try
            {
                output.close();
                closed = true;
            }
            catch (IOException e)
            {
                closed = false;
                System.err.println("Error: error closing file.");
                System.err.println(e.getMessage());
            }
        }

        return closed;
	}
	
}