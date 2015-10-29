package com.ryk.tzcommlib;

import java.io.File;
import java.io.PrintWriter;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Environment;
import android.util.Log;

import com.ryk.tzandroidutil.TzAudioLibrary;
import com.ryk.tzandroidutil.TzBroadcastGroup;
import com.ryk.tzandroidutil.TzContactUtil;
import com.ryk.tzandroidutil.TzDevice;
import com.ryk.tzandroidutil.TzFileBrowser;
import com.ryk.tzandroidutil.TzGalleryManager;
import com.ryk.tzandroidutil.TzPrefDatabase;
import com.ryk.tzandroidutil.TzMessenger;
import com.ryk.tzandroidutil.TzVideoLibrary;
import com.ryk.tzclientlib.TzActiveSessions;
import com.ryk.tzclientlib.TzCommand;
import com.ryk.tzclientlib.TzPreferences;
import com.ryk.tzclientlib.TzUserSession;
import com.ryk.tzmarkup.TzVocab;
import com.ryk.tzmarkup.TzVocabRepository;

// The PostCore contains all action done upon receiving a POST request
public class TzPostCore implements TzBroadcastGroup.Broadcasted {

	private PrintWriter w;					// Outputstream, used to write the response
	private TzCommand c;					// Command received from client
	private String sessionID;				// User's session ID
	private TzUserSession currentSession;	// User's session details contained in an object
	
	// Incoming notification to be sent to user
	private TzBroadcastGroup.BroadcastedPackage notification;
	private String lastPackageID = "";
	
	public TzPostCore(PrintWriter writer, String sessionid) {
		// Initialize pointers
		this.w = writer;
		sessionID = sessionid;
	}
	
	// Called from a syncronized thread, sets received package
	public void setPackage(TzBroadcastGroup.BroadcastedPackage pack) {
		notification = pack;
	}
	
	// Executes a command received as the only parameter
	public void execute(final TzCommand command){
		// Store command pointer
		this.c = command;
		
		// Certain commands can only be executed when user is logged in
		if (TzActiveSessions.isActiveSession(sessionID)){
			// Get current session id
			currentSession = TzActiveSessions.getSessionReference(sessionID);
			
			// Parse command name and execute corresponding function
			if 		(c.getCommandName().equals("ECHO")) ECHO(command.extractData("text")   );
			else if (c.getCommandName().equals("FOLD")) FOLD(							   );
			else if (c.getCommandName().equals("BRWS")) BRWS(command.extractData("path")   );
			else if (c.getCommandName().equals("ISMS")) ISMS(command 					   );				
			else if (c.getCommandName().equals("SDOC")) SDOC(command					   );
			else if (c.getCommandName().equals("CPRF")) CPRF(command					   );
			else if (c.getCommandName().equals("DELF")) DELF(command.extractData("file")   );
			else if (c.getCommandName().equals("ADEL")) ADEL(command.extractData("path")   );
			else if (c.getCommandName().equals("COPY")) COPY(command.extractData("file")   );
			else if (c.getCommandName().equals("MOVE")) MOVE(							   );
			else if (c.getCommandName().equals("CPST")) CPST(							   );
			else if (c.getCommandName().equals("CTNB")) CTNB(							   );
			else if (c.getCommandName().equals("SESH")) SESH(							   );
			else if (c.getCommandName().equals("NOTF")) NOTF(							   );
			else if (c.getCommandName().equals("ASMS")) ASMS(							   );
			else if (c.getCommandName().equals("READ")) READ(command.extractData("number") );
			else if (c.getCommandName().equals("SSMS")) SSMS(command					   );
			else if (c.getCommandName().equals("DCNV")) DCNV(command.extractData("number") );
			else if (c.getCommandName().equals("DSMS")) DSMS(command.extractData("id")	   );
			else if (c.getCommandName().equals("SCTC")) SCTC(command					   );	
			else if (c.getCommandName().equals("GAGP")) GAGP(							   );
			else if (c.getCommandName().equals("GDOC")) GDOC(							   );
			else if (c.getCommandName().equals("CDOC")) CDOC(command.extractData("file")   );
			else if (c.getCommandName().equals("RDOC")) RDOC(command.extractData("file")   );
			else if (c.getCommandName().equals("GPRF")) GPRF(							   );
			else if (c.getCommandName().equals("CLNG")) CLNG(command.extractData("lang")   );
			else if (c.getCommandName().equals("CRNG")) CRNG(command.extractData("type")   );
			else if (c.getCommandName().equals("DVCI")) DVCI(							   );
			else if (c.getCommandName().equals("GAMD")) GAMD(							   );
			else 										ECHO("ECHO"						   );
		} else if (c.getCommandName().equals("PSW")) {
			// If user is not logged in but wants to, check provided password
			PSW(command.extractData("password"));
		} else {
			// If nothing else, a command requiring authentication was called
			// without being logged in
			w.println("{\"error\":\"auth\"}");
			
			Log.v("ryk", "Session ID is not yet logged : " + sessionID);
			TzActiveSessions.logSessions();
		}
		
		w.flush();
	}
	
	// Simple test command, return provided text
	private void ECHO(String params) {
		w.println("{\"response\":\"" + params + "\"}");
	}
	
	// Generate a json containing all SMS from everyone ordered by date descending
	private void ASMS() {
		TzMessenger.GetAllSMS("", 0, 0, w, true);
	}
	
	// Send an SMS
	private void SSMS(TzCommand command) {
		String number = command.extractData("number");
		String content = command.extractData("content");
		
		TzMessenger.SendSMS(number, content, w);
	}
	
	// Delete SMS from Database
	// Identificator represents the SMS ID
	private void DSMS(String id) {
		TzMessenger.deleteSMS(id);
		
		w.println("{\"response\":\"ok\"}");
	}
	
	// Insert SMS into Database
	private void ISMS(TzCommand command) {
		String id = TzMessenger.insertSMS(command.extractData("id"), command.extractData("writer"), command.extractData("time"), command.extractData("body"), command.extractData("type"), command.extractData("read"));

		w.println("{\"response\":\"ok\",\"id\":\""+id+"\",\"writer\":\""+command.extractData("writer")+"\"}");
	}
	
	// Get all media : Photos, Songs and Videos
	private void GAMD() {
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
	}
	
	// Delete a file in current directory
	// Receives the filename
	private void DELF(String filename) {
		TzFileBrowser browser = new TzFileBrowser();
		
		if (browser.deleteFile(currentSession.getCurrentDir(), filename)) w.println("{\"response\":\"ok\"}");
		else w.println("{\"response\":\"fail\"}");
	}
	
	// Delete a file at a given location
	// Receives the absolute path of the file
	private void ADEL(String path) {
		TzFileBrowser browser = new TzFileBrowser();
		
		if (browser.deleteFile(path)) w.println("{\"response\":\"ok\"}");
		else w.println("{\"response\":\"fail\"}");		
	}
	
	// Delete an entire conversation with a contact
	// Identificator represents the contact's phone number
	private void DCNV(String number) {
		TzMessenger.deleteConversation(number);
		
		w.println("{\"response\":\"ok\"}");
	}
	
	// Sets given file into clipboard
	private void COPY(String path) {
		currentSession.setClipboard(currentSession.getCurrentDir() + "/" + path);
		
		w.println("{\"response\":\"ok\"}");
	}
	
	// Move file from clipboard into new directory
	// Receives new path from current directory
	private void MOVE() {
		// Get file from clipboard
		String filepath = currentSession.getClipboard();
		String newpath = currentSession.getCurrentDir();
		TzFileBrowser browser = new TzFileBrowser();
		
		// Build file objects
		File source = new File(filepath);
		File destination = new File(newpath + "/" + source.getName());
		
		// Empty clipboard
		currentSession.setClipboard("");
		
		// Move files using file browsing utility
		if (browser.moveFile(source, destination)) 
			w.println("{\"response\":\"ok\"}");
		else 
			w.println("{\"response\":\"fail\"}");
	}
	
	// Updates an existing contact
	// Represented by a JSON string, deserialized as an object
	private void SCTC(TzCommand command) {
		// Extract all contact associated data
		String id = command.extractData("id");
		String name = command.extractData("name");
		String[] address = new String[] {
			command.extractData("street"),
			command.extractData("city"),
			command.extractData("region"),
			command.extractData("country"),
			command.extractData("postcode")
		};
		String[] phone = new String[] {
			command.extractData("number1"),
			command.extractData("number2"),
			command.extractData("number3")
		};
		String[] email = new String[] {
			command.extractData("email1"),
			command.extractData("email2")	
		};
		
		// Update contact and wait for message
		TzContactUtil contactUtil = new TzContactUtil();
		String response = contactUtil.updateExistingContact(id, name, address, phone, email);
		
		// Send response to client
		if (!response.equals("ok")) {
			w.println("{\"response\":\""+response+"\"}");
		} else {
			contactUtil.getContactDetail(id, w);
		}
	}
	
	// Copy file from clipboard into new directory
	// Receives new path from current directory	
	private void CPST() {
		// Get file from clipboard
		String filepath = currentSession.getClipboard();
		String newpath = currentSession.getCurrentDir();
		TzFileBrowser browser = new TzFileBrowser();
		
		// Build file objects
		File source = new File(filepath);
		File destination = new File(newpath + "/" + source.getName());
		
		// Copy files using file browsing utility
		if (browser.copyFile(source, destination)) 
			w.println("{\"response\":\"ok\"}");
		else 
			w.println("{\"response\":\"fail\"}");
	}
	
	// Gets all gallery's photo filename
	private void GAGP() {
		TzGalleryManager manager = new TzGalleryManager();
		
		manager.getSdCardImages(w);
	}
	
	// Generate a json containing all contacts and their associated phone numbers
	private void CTNB() {
		// Gets json using contacts utilities
		TzContactUtil contacts = new TzContactUtil();
		
		// Sends the whole thing
		contacts.getAllContacts(w);
	}
	
	// Login user
	private void PSW(String password) {	
		// Compare provided password with server's
		if (password.equals(TzPreferences.defaultPassword)) {
			// Renerate a unique identificator
			String session = UUID.randomUUID().toString();
			
			// Return the user ID to the browser, a cookie will (should) be created on client side
			w.println("{\"response\":\"ok\", \"session\":\"" + session + "\"}");
			
			// Add session to current sessions array
			TzActiveSessions.addSession(session);
		} else { 
			// Passwords do not match
			w.println("{\"response\":\"fail\"}");
		}
	}
	
	// Returns to the client if he of she is logged in
	private void SESH() {
		w.println("{\"response\":\"ok\"}");
	}
	
	// Returns current folder of user session
	private void FOLD() {
		TzFileBrowser browser = new TzFileBrowser();
		
		browser.getFolderContent(currentSession.getCurrentDir(), w);	
	}
	
	// Navigate to another adjacent directory (parent or children) and store new current folder
	private void BRWS(String path) {
		// Get current directory
		String currentFolder = currentSession.getCurrentDir();
		TzFileBrowser browser = new TzFileBrowser();
		
		// Checks if the user has requested a parent of a children directory, gets it and sets it
		currentFolder = path.equals("../") ? browser.getParentPath(currentFolder) : currentFolder + (currentFolder.equals("/") ? "" : "/") + path;
		currentSession.setCurrentDir(currentFolder);
		
		// Return folder details and content as json
		browser.getFolderContent(currentFolder, w);
	}
	
	// Gets all Tranzoid Documents names
	private void GDOC() {
		TzFileBrowser browser = new TzFileBrowser();
		
		String dir = Environment.getExternalStorageDirectory().toString() + "/" + TzPreferences.defaultDirectory + "/" + TzPreferences.defaultDocDir;
		browser.getFolderContent(dir, w);
	}
	
	// Creates a new file representing a document
	private void CDOC(String filename) {
		TzFileBrowser browser = new TzFileBrowser();
		String dir = Environment.getExternalStorageDirectory().toString() + "/" + TzPreferences.defaultDirectory + "/" + TzPreferences.defaultDocDir + "/";
		
		String newName = browser.createFile(new File(dir + filename));
		w.println("{\"name\":\""+newName+"\"}");
	}
	
	// Reads an existing Tranzoid Document
	private void RDOC(String filename) {
		TzFileBrowser browser = new TzFileBrowser();
		String filepath = Environment.getExternalStorageDirectory().toString() + "/" + TzPreferences.defaultDirectory + "/" + TzPreferences.defaultDocDir + "/" + filename;
		
		browser.printFile(filepath, w);
	}	
	
	// Saves an existing Tranzoid Document
	private void SDOC(TzCommand command) {
		String filename = command.extractData("file");
		String content = command.extractData("content");
		
		TzFileBrowser browser = new TzFileBrowser();
		String filepath = Environment.getExternalStorageDirectory().toString() + "/" + TzPreferences.defaultDirectory + "/" + TzPreferences.defaultDocDir + "/" + filename;
		
		if (browser.overwriteFileContent(new File(filepath), content)) w.println("{\"response\":\"ok\"}");
		else w.println("{\"response\":\"error\"}");
	}
	
	// Changes the state of all sms from a contact to "read"
	private void READ(String contactNumber) {
		TzMessenger.ReadSMS(contactNumber);
		
		w.println("{\"response\":\"ok\"}");
	}
	
	// Gets preferences as JSON
	private void GPRF() {
		TzFileBrowser fileBrowser = new TzFileBrowser();
		
		w.println("{\"prefs\":" + TzPrefDatabase.getDatabase().asJSON());
		w.println(",\"wallpapers\":" + fileBrowser.getTzWallpapersJson());
		w.println("}");
	}
	
	// Saves a given preferences with its value
	private void CPRF(TzCommand command) {
		TzPrefDatabase.getDatabase().setValue(command.extractData("pref"), command.extractData("value")).save();
		
		w.println("{\"response\":\"ok\"}");
	}
	
	// Changes the language of main interface
	private void CLNG(String lang) {
		// Initialize language
		TzPrefDatabase.getDatabase().setValue("lang", lang).save();
		
		// Rebuild dictionnary
        TzVocabRepository.getRepo().buildDico(TzVocab.Languages.valueOf(TzPrefDatabase.getDatabase().getValue("lang").toString()));
        
        w.println("{\"newlang\":\""+lang+"\"}");
	}
	
	// Changes the ringtone to silent or vibration
	//   0 - Silent
	//   1 - Vibration
	//   2 - Ringer
	private void CRNG(String type) {
		int ringerType = Integer.parseInt(type);
		
		AudioManager audio = (AudioManager)TzPreferences.mainCtx.getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(ringerType);

		w.println("{\"newtype\":"+String.valueOf(ringerType)+"}");
	}
	
	// Generates a JSON containing various device information
	private void DVCI() {
		w.println(TzDevice.getDevice().getJSON());
	}
	
	// Wait for notifications to be sent to client 
	@SuppressLint("SimpleDateFormat")
	private void NOTF() {
		// Sync current object
		try {
			synchronized(this) {
				// Join broadcast group in order to listen to incoming requests like SMS
				TzBroadcastGroup.joinGroup(this);
				
				// Wait for the group to notify the objects
				wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// If the notification was not already sent
		if (!notification.getID().equals(lastPackageID)) {	
			lastPackageID = notification.getID();
			
			switch (notification.getPackageType()) {
				case rebound: 
					// Write default response if nothing was heard
					w.println("{\"type\":\"rebound\",\"id\":\""+lastPackageID+"\",\"response\":\"ok\"}");			
					break;
					
				case sms:
					// Send SMS information to client machine
					w.println("{\"type\":\"sms\",\"id\":\""+lastPackageID+"\",\"time\":\""+TzMessenger.getFormattedDate()+"\",\"address\":\"" + notification.getData("address") + "\",\"message\":\"" + notification.getData("message") + "\"}");		
					break;
					
				case battery:
					// Send new battery value
					w.println("{\"type\":\"battery\",\"id\":\""+lastPackageID+"\",\"level\":\""+notification.getData("level")+"\",\"charge\":\""+notification.getData("charge")+"\"}");
					break;
					
				default:
					// Default type is a rebound
					w.println("{\"type\":\"rebound\",\"id\":\""+lastPackageID+"\",\"response\":\"ok\"}");					
			}
		} else {
			w.println("{\"type\":\"rebound\",\"id\":\""+lastPackageID+"\",\"response\":\"ok\"}");	
		}
		
		// Leave broadcast group and stop listening
		synchronized(this) {
			TzBroadcastGroup.leaveGroup(this);
		}
	}
}
