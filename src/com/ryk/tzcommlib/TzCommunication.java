package com.ryk.tzcommlib;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.ryk.tzandroidutil.TzBatteryListener;
import com.ryk.tzandroidutil.TzFileBrowser;
import com.ryk.tzandroidutil.TzMessenger;
import com.ryk.tzclientlib.TzPreferences;
import com.ryk.tzviews.TzEntryPoint;

// Core communication between client's browser and cellphone
// Is a direct representation of an Android background Service
public class TzCommunication extends Service {
	private ServerSocket server;	// Incoming connection listener
	private Boolean listening;		// Active connection flag
	
	private final int MAX_CONNECTIONS = 5; 		// For a maximum of 5 connections
	private int connectionIterator = 0;			// Current number of active connection
	private TzServerConnection[] connections;	// Array containing current connections
	
	private TzMessenger smsListener;			// Listens to incoming SMS and manage existing ones
	private TzBatteryListener batteryListener;	// Listens to battery state changes
	
	// Starts a listening thread
	private void startServer() {
		Thread thread = new Thread() {
			@Override
			public void run() {	 
				try {
					// Initialization of members
					connections = new TzServerConnection[MAX_CONNECTIONS];
					server = new ServerSocket(TzPreferences.incomingPort);
					
					listening = true;
					
					// While service is running, listen to incoming connections and serve respective users
					while (listening) {
						Socket client = server.accept();
						serveClient(client);
					}
					
				} catch (Exception ex) {
					Log.e("ryk", "Global exception caught : " + ex.getMessage());
				}
			}
		};

		thread.start();
	}
	
	// Abort service
	private void stopServer() {
		// Turnoff flag
		listening = false;
		
		// Close existing connections
		for (int i = 0; i < MAX_CONNECTIONS; i++) if (connections[i] != null)
			connections[i].close();
	}
	
	// Serves a new client
	private void serveClient(Socket client) throws IOException {
		// Make sure service is active
		if (listening) {
			// Stores current connection
			connections[connectionIterator] = new TzServerConnection(client, connectionIterator);
			final TzServerConnection reference = connections[connectionIterator];
			
			reference.start();
			
			connectionIterator++;
			
			// Alternate iterator
			if (connectionIterator == MAX_CONNECTIONS) connectionIterator = 0;
		}
	}

	// Returns current given IP address
	public String getHostIP() {
		try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
	        {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
	            {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) 
	                    return inetAddress.getHostAddress();
	            }
	        }
		}
		catch (SocketException se) {
			se.printStackTrace();
		}
		
		return "127.0.0.1";
	}
	
	// Start all listeners 
	private void startListeners() {
		smsListener = new TzMessenger();
		batteryListener = new TzBatteryListener();
		
	    // What to do when an SMS is received
		registerReceiver(smsListener, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		
		// What to do when battery state changes
	    registerReceiver(batteryListener, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); 	
    }	
	
	// Stop listening
	private void stopListeners() {
		unregisterReceiver(smsListener);
		unregisterReceiver(batteryListener);
	}

	// Service overrides
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
    @Override
    public void onCreate() {
    	super.onCreate();
    }	
	
	@SuppressWarnings("deprecation")
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {   
		// Get intent of main class
		Intent i=new Intent(this, TzEntryPoint.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		PendingIntent pi=PendingIntent.getActivity(this, 0, i, 0);

		// Initialize browser
		TzFileBrowser browser = new TzFileBrowser();
		browser.createFolder(Environment.getExternalStorageDirectory().toString() + "/" + TzPreferences.defaultDirectory);
		browser.createFolder(Environment.getExternalStorageDirectory().toString() + "/" + TzPreferences.defaultDirectory + "/" + TzPreferences.defaultDocDir);
		browser.createFolder(Environment.getExternalStorageDirectory().toString() + "/" + TzPreferences.defaultDirectory + "/" + TzPreferences.wallpaperDir);
		
		// Set top notification
        Notification note=new Notification(android.R.drawable.btn_star, "Tranzoid Desktop", System.currentTimeMillis());
		note.setLatestEventInfo(this, "Tranzoid Desktop", getHostIP() + ":9753", pi);
		note.flags|=Notification.FLAG_NO_CLEAR;
		
		startForeground(9753, note);      
        
		// Start listeners, then server
		startListeners();
		startServer();
		
		return super.onStartCommand(intent, flags, startId);
    }		
	
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	// Stop everything
    	stopListeners();
    	stopServer();
    	stopForeground(true);
    }	
}
