package com.ryk.tzclientlib;

// Global Tranzoid Desktop Preferences
public final class TzPreferences {
	public static final int incomingPort = 9753;				// Port used for incoming connections
	public static final int maximumSessions = 100;				// Maximum sessions before overflow
	public static final String defaultFile = "main.tzp";		// Default main page file name
	public static final String defaultPassword = "tranzoid";	// Password used to login
	public static android.content.Context mainCtx;				// Main context of application
	public static final String defaultDirectory = "TzDesktop";  // Default desktop for temporary storage
	public static final String defaultDocDir = "TzDocs";		// Default directory for Tranzoid Documents
	public static final String wallpaperDir = "TzWallpapers";	// Directory containing wallpapers
}
