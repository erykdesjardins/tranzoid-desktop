package com.ryk.tzclientlib;

import java.util.ArrayList;

import com.ryk.tzandroidutil.TzGalleryManager;

import android.os.Environment;
import android.util.Log;

// Representation of an HTTP header
public class TzHeader {
	String[] headerInfos;	// Contains plain text of each line
	String requestedPage;	// Page that is requested
	String method;			// Method, POST, GET, etc.	
	String data;			// Data of POST method
	String cookieSession;	// Session cookie
	byte[] blob;			// File read from POST
	int dataLength;			// Length in bytes of POST data
	
	// Object creation requires an Array of lines (String)
	public TzHeader(String[] lines) {
		headerInfos = lines;
		
		interpretValues();
	}
	
	// Object creation requires an ArrayList of lines (String)
	public TzHeader(ArrayList<String> lines) {
		headerInfos = new String[lines.size()];
		
		for (int i = 0; i < lines.size(); i++) headerInfos[i] = lines.get(i);

		interpretValues();
	}
	
	// String members initialization
	public void interpretValues() {
		// Method and requested page are on the first line
		method = headerInfos[0].split(" ")[0];
		requestedPage = headerInfos[0].split(" ")[1].replace("%20", " ");
		
		// Get other useful stuff
		for (int i = 0; i < headerInfos.length; i++) {
			if (headerInfos[i].startsWith("Content-Length: "))
				dataLength = Integer.parseInt(headerInfos[i].substring("Content-Length: ".length()));
			else if (headerInfos[i].startsWith("Cookie: "))
				cookieSession = headerInfos[i].substring("Cookie: session=".length());
		}
	}
	
	// Gets POST or GET
	public String getMethod() {
		return method;
	}
	
	// Gets a certain line of the header
	public String getLine(int index) {
		return headerInfos[index];
	}
	
	// Gets the first line as an array of String
	public String[] getMethodParams() {
		return headerInfos[0].split(" ");
	}
	
	// Gets the requested page
	public String getRequestedPage() {
		return requestedPage;
	}
	
	// True if a file was requested as bytestream download
	public Boolean isFileDownload() {
		return requestedPage.endsWith("?tz=DOWNLOAD");
	}
	
	// True if a file was requested as bytestream download
	public Boolean isAbsoluteDownload() {
		return requestedPage.endsWith("?tz=ABSOLUTE");
	}	
	
	// True if a gallery thumbnail was requested 
	public Boolean isGalleryThumbnail() {
		return requestedPage.startsWith("/GALPH?");
	}
	
	// True if a contact profile picture was requested
	public Boolean isContactImage() {
		return requestedPage.startsWith("/CTIMG?");
	}
	
	// True if a wallpaper was requested
	public Boolean isWallpaper() {
		return requestedPage.startsWith("/TZWP");
	}
	
	// True if a file must be accepted as post data
	public Boolean isFileUpload() {
		return requestedPage.equals("/UPLOAD");
	}
	
	// Gets the filename that's been requested
	public String getFileToDownloadName() {
		if (isGalleryThumbnail()) {
			String thumbid = requestedPage.substring(7, requestedPage.length() - 4);
			return TzGalleryManager.getThumbnailFilename(Long.parseLong(thumbid));
		} else if (requestedPage.startsWith("/ASMS")) {
			return requestedPage.substring(5);
		} else if (requestedPage.startsWith("/TZWP")) {
			return Environment.getExternalStorageDirectory().toString() + "/" + 
				   TzPreferences.defaultDirectory + "/" + 
				   TzPreferences.wallpaperDir + 
				   requestedPage.substring(5);
		} else {
			int index = requestedPage.indexOf("?tz=DOWNLOAD");
			index = index == -1 ? requestedPage.indexOf("?tz=ABSOLUTE") : index;
			
			return index == -1 ? requestedPage : requestedPage.substring(0, index);
		}
	}
	
	// Sets POST data
	public void setData(String str) {
		data = str;	
	}
	
	// Gets POST data
	public String getData() {
		return data;
	}
	
	// Sets POST file data
	public void setBlob(byte[] file) {
		blob = file;
	}
	
	// Gets POST file data
	public byte[] getBlob() {
		return blob;
	}
	
	// Gets the user's cookie which contains the session ID
	public String getSessionID() {
		return cookieSession;
	}
	
	// Gets the POST data size in bytes
	public int getDataLength() {
		return dataLength;
	}
	
	// Logs entire header into console as Verbose messages
	public void logContent() {
		for (int i = 0; i < headerInfos.length; i++) 
			Log.v("ryk", "HEADER LOG : " + headerInfos[i]);
	}
}
