 package com.ryk.tzandroidutil;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

// All objects joining this group must be in a seperate thread
// Which is always possible because all the methods are static
// Objects part of this group are waiting for a package to be broadcasted
// Once the package is broadcasted, all objects are notified
public final class TzBroadcastGroup {

	static Boolean locked = false; 											// If the broadcasts are locked and not notifying
	static ArrayList<Broadcasted> members = new ArrayList<Broadcasted>(); 	// List of all members of group
	
	// Adds a broadcasted object to the group
	public static void joinGroup(Broadcasted obj) {
		synchronized(obj) {
			members.add(obj);
			Log.v("ryk", "Added object (index #" + String.valueOf(members.size()) + ") to broadcast group : " + obj.getClass().toString());
		}
	}
	
	// Removes a broadcasted object from the group
	public static void leaveGroup(Broadcasted obj) {
		members.remove(obj);
		Log.v("ryk", "Removed object from broadcast group : " + obj.getClass().toString());
	}
	
	// Removes all null objects
	public static void validateGroup() {
		for (int i = members.size() - 1; i >= 0; i--) {
			if (null == members.get(i)) members.remove(i);
		}
	}
	
	// Broadcast a package to all group members
	public static Boolean broadcast(BroadcastedPackage pack) {
		Log.v("ryk", "Broadcasting event : " + pack.getPackageType().toString());
		locked = true;
		Boolean broadcasted = false;
		validateGroup();
		
		// For all existing members of group
		for (Broadcasted member : members) {
			// Synchronize them to this thread
			synchronized(member) {
				// Sets broadcasted package and notify
				member.setPackage(pack);
				member.notify();
				
				broadcasted = true;
			}
		}
		
		// Unlock groups
		locked = false;
		
		// Return if anything was broadcasted
		return broadcasted;
	}
	
	// Flag for if the group is broadcasting or not
	public static Boolean isLocked() {
		return locked;
	}
	
	// Represents the content of a broadcast
	public static class BroadcastedPackage {
		HashMap<String, String> data;	// Data with String as a key
		PackageType type;				// Type of package
		String ID;						// Package unique identificator
		
		// Can be buily usingonly a type without data
		public BroadcastedPackage(PackageType type) {
			data = new HashMap<String, String>();
			this.type = type;
			this.ID = java.util.UUID.randomUUID().toString();
		}
		
		// Data must be set on construction
		public BroadcastedPackage(HashMap<String, String> d, PackageType type) {
			data = d;
			this.type = type;
			this.ID = java.util.UUID.randomUUID().toString();
		}
		
		// Returns data according to a certain key
		public String getData(String index) {
			return data.get(index);
		}
		
		// Gets the package type
		public PackageType getPackageType() {
			return this.type;
		}
		
		// Gets the package unique ID
		public String getID() {
			return this.ID;
		}
	}
	
	// All possible types of packages
	public static enum PackageType {
		rebound, sms, battery
	}
	
	// Objects contained within this group must implements this interface
	public static interface Broadcasted {
		public void setPackage(BroadcastedPackage pack);
	}
}