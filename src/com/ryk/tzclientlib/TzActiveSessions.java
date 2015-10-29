package com.ryk.tzclientlib;

import android.util.Log;

// Represents a wrapper of all active sessions
public final class TzActiveSessions {
	// Array of active sessions
	private static TzUserSession[] sessions = new TzUserSession[TzPreferences.maximumSessions];
	private static int sessionCount = 0;
	
	// Should be called on application starts
	public static void init() {
		// Initialization of all session objects
		for (int i = 0; i < TzPreferences.maximumSessions; i++) 
			sessions[i] = new TzUserSession();
	}
	
	// Adds a new session item from an unique identificator
	public static void addSession(String id) {
		// The identificator cannot be null
		if (id != null) {
			// Creates the object and adds it to the array
			sessions[sessionCount] = new TzUserSession();
			sessions[sessionCount].Login(id);
			Log.v("ryk","SESSION CREATED {" + id + "} @ index " + String.valueOf(sessionCount));
			
			// Increment count
			sessionCount++;
		}
	}
	
	// Removes a session item from an unique identificator
	public static void killSession(String id) {
		// Gets the current session array index
		int index = getSessionIndex(id);
		
		// If the session still exists
		if (index != -1) {
			// Shift all sessions from found index to right
			for (; index < sessionCount; index++)
				sessions[index] = sessions[index+1];
		
			sessionCount--;
		}
	}
	
	// Returns true if the specified identificator represents an active session
	public static Boolean isActiveSession(String id) {
		if (id != null)
			for (int i = 0; i < sessionCount; i++) 
				if (sessions[i].getSessionID().equals(id) && sessions[i].isActive()) return true;
		
		return false;
	}
	
	// Returns the array index of a session represented by the specified unique identificator
	// Returns -1 if not found
	public static int getSessionIndex(String id) {
		for (int i = 0; i < sessionCount; i++) 
			if (sessions[i].getSessionID().equals(id)) return i;
		
		return -1;		
	}
	
	// Logs all session into console
	public static void logSessions() {
		for (int i = 0; i < sessionCount; i++) 
			Log.v("ryk", "Session #" + String.valueOf(i) + " : " + sessions[i].getSessionID());
	}
	
	// Returns a final references to a session
	// The reference cannot be altered
	public static final TzUserSession getSessionReference(String id) {
		for (int i = 0; i < sessionCount; i++) 
			if (sessions[i].getSessionID().equals(id)) return sessions[i];	
		
		return null;
	}
}
