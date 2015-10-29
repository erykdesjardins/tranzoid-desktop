package com.ryk.tzcommlib;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

import com.ryk.tzandroidutil.TzFileBrowser;
import com.ryk.tzclientlib.TzHeader;
import android.util.Log;

public class TzServerConnection extends Thread implements Runnable {

	private Socket client;							// Direct connection with user's browser
	private java.io.BufferedReader readerClient;	// Input stream, Read
	private java.io.PrintWriter writerClient;		// Output stream, Write
	private int tableIndex;							// Index of connections table
	private TzHTTPInterpreter httpInterpreter;		// Interpreter of HTTP commands
	
	@SuppressWarnings("unused")
	private Boolean connectionIsActive = false;		// Activity flag
	
	public TzServerConnection(Socket onClient, int index) throws IOException {
		// Object initialization
		client = onClient;
		httpInterpreter = new TzHTTPInterpreter(client);
		
		// Getting main client's streams 
		readerClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
		writerClient = new PrintWriter(client.getOutputStream());
		
		tableIndex = index;
	}
	
	public void close() {
		try {
			// Closing all streams and sockets
			readerClient.close();
			writerClient.close();
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
	}
	
	// Index getter
	public int getIndex() {
		return tableIndex;
	}
	
	// Since this class is intended to be used as a Thread, this method is executed when thread starts
	@Override
	public void run() {
		// Array used to store header
		ArrayList<String> fromServer = new ArrayList<String>();
		TzHeader header = null;
		connectionIsActive = true;
		Boolean readingHeader = true;
			
		try {
			// Read header until an empty line is read
			// Broken if connection is broken while reading header or when header is read successfully
			while (readingHeader) {
				String strFromServer = readerClient.readLine();
				
				if (!strFromServer.trim().equals("")) fromServer.add(strFromServer);
				else readingHeader = false;
			}			
			
			// If line is empty (or a line break), create header object using previous table
			Log.v("ryk", " - DONE READING HEADER");
			header = new TzHeader(fromServer);
			
			// If the header is a POST method
			if (header.getMethod().equals("POST")) {
				// Get post data into array using content length read from header
				int dataSize = header.getDataLength();
				char[] data = new char[dataSize];
				
				// Begin parsing data
				Log.v("ryk", "Reading POST data which is " + String.valueOf(dataSize) + " bytes");
				int dataRead = readerClient.read(data);
				
				// If data size is same number as content length from header
				if (dataRead == dataSize) {
					// Store read data into header object
					header.setData(String.valueOf(data));
					Log.v("ryk", "POST data : " + header.getData());
				} else {
					// If data is shorter, it's probably an encoding error
					// Simply remove the trailing corrupted characters and keep the read bytes
					header.setData(String.valueOf(data).trim().substring(0, dataRead));
					Log.w("ryk", "WARNING! Read " + String.valueOf(dataRead) + " bytes from POST data but should've read " + String.valueOf(dataSize));
				}
			} else if (header.getMethod().equals("PUT")) {
				// Get posted file length
				int dataSize = header.getDataLength();
				int offset = 0;
				byte[] fileRead = new byte[1024*32];
				String filename = UUID.randomUUID().toString() + ".temp";
				TzFileBrowser browser = new TzFileBrowser();
				
				// Begin parsing data
				Log.v("rykupload", "Reading POST file which is " + String.valueOf(dataSize) + " bytes");
				
				BufferedInputStream byteStream = new BufferedInputStream(client.getInputStream());
				FileOutputStream fileStream = new FileOutputStream(browser.getNewFileAtDefaultDirectory(filename));
				
				// Number of byte read from a block
				int byteRead = 0;
				do {
					// Number of bytes requested
					int tryingToRead = dataSize < offset + fileRead.length ? dataSize - offset : fileRead.length;
					
					try {
						byteRead = byteStream.read(fileRead, 0, tryingToRead);
						fileStream.write(fileRead, 0, byteRead);
						fileStream.flush();
						
						offset += byteRead;
					} catch (Exception ex) {
						byteRead = -1;
					}
					
					if (byteRead != -1) Log.v("rykupload", "Read " + byteRead + " bytes from POST with offset of " + offset + "/" + dataSize );
				} while (byteRead != -1);
				
				fileStream.close();
				
				if (offset == dataSize) Log.v("rykupload", "File from POST has the good size");
				else Log.w("rykupload", "WARNING! Read " + String.valueOf(offset) + " bytes from POST file but should've read " + String.valueOf(dataSize));
				
				header.setData(filename);				
		
			}
			
			connectionIsActive = false;
		
			
		} catch(IOException io) {
			Log.e("ryk", "IOException in TzServerConnection main Thread");
			connectionIsActive = false;
		} catch(NullPointerException npe) {
			Log.e("ryk", "NullPointerException in TzServerConnection main Thread");
			connectionIsActive = false;
		} catch(IndexOutOfBoundsException ioobe) {
			Log.e("ryk", "IndexOutOfBoundsException in TzServerConnection main Thread");
			connectionIsActive = false;				
		} catch(Exception ex) {
			Log.wtf("ryk", "Exception in TzServerConnection main Thread");
			connectionIsActive = false;					
		}
		
		
		// Interpret header using interpreter object
		Log.v("ryk", "HTTP Interpretation will now begin");
		if (header != null) httpInterpreter.interpret(header);
		
		// Close everything and wait for another request
		close();
		Log.v("ryk", "Connection is now closed");
	}
	
}
