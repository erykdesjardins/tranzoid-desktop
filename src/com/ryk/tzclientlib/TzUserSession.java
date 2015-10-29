package com.ryk.tzclientlib;

import java.util.Date;

// A user session and all its details
public class TzUserSession {
	private String sessionID;	// Unique identificator
	private String clipboard;	// Users clipboard content
	private String currentDir;	// The current browsing directory
	private Date startTime;		// Date and time when the session started
	private Boolean active;		// Active session flag
	
	// Object initialization
	public TzUserSession() {
		sessionID = "";
		clipboard = "";
		currentDir = "/";
		startTime = null;
		active = false;		
	}
	
	// Reinitialization with an unique identificator
	public void Login(String ID) {
		sessionID = ID;
		startTime = new Date();
		active = true;
		currentDir = "/";
	}
	
	// Object reiniitialization without any ID
	public void Logout() {
		sessionID = "";
		currentDir = "/";
		startTime = null;
		active = false;
	}
	
	// Gets current session identificator
	public String getSessionID() {
		return sessionID;
	}
	
	// Gets current session browsing directory
	public String getCurrentDir() {
		return currentDir;
	}
	
	// Sets current session clipboard
	public void setClipboard(String data) {
		clipboard = String.valueOf(data);
	}
	
	// Gets current session clipboard
	public String getClipboard() {
		return clipboard;
	}
	
	// Sets current session browsing directory
	public void setCurrentDir(String dir) {
		currentDir = dir;
	}
	
	// Gets current session start date and time
	public Date getStartTime() {
		return startTime;
	}
	
	// Returns true if session is active
	public Boolean isActive() {
		return active;
	}
}
