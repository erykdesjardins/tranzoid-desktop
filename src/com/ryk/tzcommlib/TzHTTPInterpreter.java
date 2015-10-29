package com.ryk.tzcommlib;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream; 
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.gson.Gson;
import com.ryk.tzandroidutil.TzAudioLibrary;
import com.ryk.tzandroidutil.TzContactUtil;
import com.ryk.tzandroidutil.TzFileBrowser;
import com.ryk.tzandroidutil.TzGalleryManager;
import com.ryk.tzandroidutil.TzMessenger;
import com.ryk.tzandroidutil.TzVideoLibrary;
import com.ryk.tzclientlib.TzActiveSessions;
import com.ryk.tzclientlib.TzCommand;
import com.ryk.tzclientlib.TzHeader;
import com.ryk.tzclientlib.TzPreferences;
import com.ryk.tzclientlib.TzUserSession;
import com.ryk.tzmarkup.TzMarkupParser;

// Interprets HTTP headers and calls corresponding functions using Tranzoid Utilities
public class TzHTTPInterpreter {
	
	private Socket client; // Connection with user's browser
	private PrintWriter w; // Used to send response to client
	
	public TzHTTPInterpreter(Socket cli) {
		// Upon creation, stores client socket pointer and creates outputstream
		try {
			client = cli;
			w = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Main function, will interpret a header
	public void interpret(TzHeader header) {
		header.logContent();
		
		// Checks whether the method is a GET or a POST
		if     (header.getMethod().equals("GET"))  GET(header);
		else if(header.getMethod().equals("POST")) POST(header);		
	}
	
	// A file is requested
	private void GET(TzHeader header) {
		// Stores header's requested page
		String requestedPage;
		requestedPage = header.getRequestedPage();

		// Stream reading the file to be sent
		BufferedReader file;	
		
		// If a file from storage is requested for download
		if (header.isFileDownload() || header.isAbsoluteDownload() || header.isGalleryThumbnail() || header.isWallpaper()) {
			// Get session ID
			TzUserSession currentSession = TzActiveSessions.getSessionReference(header.getSessionID());
			
			// Create file object with requested path
			String absolutePath = (header.isFileDownload() ? currentSession.getCurrentDir() : "") + header.getFileToDownloadName();
			Log.v("ryk", "Opening file : " + absolutePath);
			File fileToDownload = new File(absolutePath);
			FileInputStream input;
			
			// Send OK response header
			sendHeader200(fileToDownload, fileToDownload.getName());
			
			try {
				// Bytes will be read 4096 at a time
				input = new FileInputStream(fileToDownload);
				DataOutputStream output = new DataOutputStream(client.getOutputStream());
				byte [] CurrentData = new byte[4096];
				
				// Read file and write to client at the same time
				while (input.read(CurrentData, 0, 4096) != -1) {
					output.write(CurrentData, 0, 4096);
					output.flush();
				}				

				// Close communication stuff
				output.flush();
				output.close();
				input.close();
				
				Log.v("ryk", "Sent filestream : " + fileToDownload.getName());
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (header.isContactImage()) {
			try {
				// If a contact thumbnail image is requested
				TzContactUtil contacts = new TzContactUtil();
				int contactID = 0;
				
				try {
					// Get image stream with contact ID
					contactID = Integer.parseInt(requestedPage.substring(7));		
				} catch (NumberFormatException e) {
					
				}
				
				// Get photo stream
				InputStream contactPhoto = contacts.getContactThumbnail(contactID);
				
				// Stream to client
				DataOutputStream output = new DataOutputStream(client.getOutputStream());
				byte [] CurrentData = new byte[4096];
				
				if (contactPhoto != null) {
					// Read photo bytes and send read data
					while (contactPhoto.read(CurrentData, 0, 4096) != -1) {
						output.write(CurrentData, 0, 4096);
					}
					
					// Close communication stuff
					output.flush();
					output.close();	
					contactPhoto.close();
				} else {
					// If no image is associated with current contact
					// Close communication stuff
					output.flush();
					output.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
		} else if (requestedPage.startsWith("/ASMS")) {
			// Get all sms and return file
			try {
				// Send OK method
				sendHeader200(null, header.getFileToDownloadName());					
				
				PrintWriter w = new PrintWriter(new DataOutputStream(client.getOutputStream()));
				TzMessenger.GetAllSMS("", 0, 0, w, true);
				
				w.flush();
				w.close();
			}
			catch (IOException ioex) {
				
			}
		} else if (requestedPage.startsWith("/CTNB")) {
			// Get all contact and return file
			try {
				// Send OK method
				sendHeader200(null, header.getFileToDownloadName());					
				
				PrintWriter w = new PrintWriter(new DataOutputStream(client.getOutputStream()));
				TzContactUtil util = new TzContactUtil();
				util.getAllContacts(w);
				
				w.flush();
				w.close();
			}
			catch (IOException ioex) {
				
			}			
		} else if (requestedPage.startsWith("/Contact/")) {
			String id = requestedPage.substring(9);
			
			try {
				PrintWriter w = new PrintWriter(new DataOutputStream(client.getOutputStream()));
				TzContactUtil util = new TzContactUtil();
				util.getContactDetail(id, w);
				
				w.flush();
				w.close();				
			} catch (IOException ioex) {
				
			}
			
		} else if (requestedPage.startsWith("/GAMD")) {	
			// Get all media and return file
			try {	
				// Send OK method		
				sendHeader200(null, header.getFileToDownloadName());	
				
				PrintWriter w = new PrintWriter(new DataOutputStream(client.getOutputStream()));
				
				TzGalleryManager galleryManager = new TzGalleryManager();
				TzAudioLibrary audioLibrary = new TzAudioLibrary();
				TzVideoLibrary videoLibrary = new TzVideoLibrary();
				
				w.print("{");
				galleryManager.getSdCardImages(w);
				w.print(",");
				audioLibrary.getAllSongs(w);
				w.print(",");
				audioLibrary.getAllAlbums(w);
				w.print(",");
				audioLibrary.getAllArtists(w);
				w.print(",");
				videoLibrary.getAllVideos(w);
				w.print("}");
				
				w.flush();
				w.close();
			}
			catch (IOException ioex) {
				
			}	
			
		} else {
			// If not, we are sending a page from the assets folder
			try {

				// Check if root is requested or some other file
				String filename = requestedPage.equals("/") ? TzPreferences.defaultFile : requestedPage.substring(1);	
				file = new BufferedReader(new InputStreamReader(TzPreferences.mainCtx.getAssets().open(filename)));
				
				// Send OK method
				sendHeader200(null, filename);			
				
				try {
					// Check if a webpage or a page implementation was requested (plain text)
					if (requestedPage.endsWith(".js") || requestedPage.endsWith(".css") || requestedPage.equals("/")) {
						String readStr;
						readStr = file.readLine();
						
						// Tranzoid Markup Parser
						TzMarkupParser parser = new TzMarkupParser();
						
						// Read file until the end
						while (readStr != null)
						{
							// Send read line and so on
							w.println(readStr);
							readStr = parser.parse(file.readLine());
							w.flush();
						}
						
						// Send empty line
						w.println("");
						w.flush();
					} else {
						// Reader of a file using binary reading
						// As usual, file will be sent using 4096 bytes bloc
						InputStream input = TzPreferences.mainCtx.getAssets().open(requestedPage.substring(1));
						DataOutputStream output = new DataOutputStream(client.getOutputStream());
						byte [] CurrentData = new byte[4096];
						
						// Read file and send read data
						while (input.read(CurrentData, 0, 4096) != -1) {
							output.write(CurrentData, 0, 4096);
						}
						
						// Close communication stuff
						output.flush();
						input.close();
						output.close();
					}
					
					Log.v("ryk", "Sent file : " + requestedPage.substring(1));
				} catch (IOException e) {
					// Errors can occur, let's just log them and check what can be done 
					e.printStackTrace();
					
					Log.e("ryk", "ERROR READING FILE");
					w.println("");
					w.flush();
				}
				
				file.close();
			} catch (Exception e) {
				sendHeader404();
			}
		}
	}
	
	// A command execution is requested with a returned value
	private void POST(TzHeader header) {
		Log.v("ryk", "Interpreting json : " + header.getData());

		// Verify if the server must accept a file as post data
		if (header.isFileUpload()) {
			w.print("{\"upload\":\"ok\"}");
		} else {
			// Parse parameters from client and create command object
			Gson gson = new Gson();
			TzCommand command = gson.fromJson(header.getData(), TzCommand.class);
	
			// Send header to PostCore upon object creation
			TzPostCore postCore = new TzPostCore(w, header.getSessionID());
			
			// Execute command
			postCore.execute(command);
		}
	}
	
	// Sends a 200 OK header
	@SuppressLint("NewApi")
	private void sendHeader200(File file, String filename) {	
		// Gets current date and time
		String NowTime = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", new java.util.Locale("ENGLISH")).format(Calendar.getInstance().getTime()) + " GMT";	
		TzFileBrowser browser = new TzFileBrowser();
		
		w.println("HTTP/1.1 200 OK");
		w.println("Server: Tranzoid");
		w.println("Date: " + NowTime);
		w.println("Last-Modified: Thu, 10 Mar 2005, 16:08:59 GMT");
		w.println("Content-Type: " + browser.getContentType(filename));
		//if (file != null) w.println("Content-length: " + file.length());
		w.println();
		w.flush();
	}
	
	// Sends a 404 Not Found header
	@SuppressLint("NewApi")
	private void sendHeader404() {
		// Gets current date and time
		String NowTime = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", new java.util.Locale("ENGLISH")).format(Calendar.getInstance().getTime()) + " GMT";	
		w.println("HTTP/1.0 404 Not Found");
		w.println("Date: " + NowTime);
		w.println("Server: Java");
		w.println("Content-Type: text/html");
		w.println();
		w.flush();		
	}
}

