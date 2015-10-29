package com.ryk.tzandroidutil;

import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

// Listens to battery state change
public class TzBatteryListener extends BroadcastReceiver {
	int lastReadingLevel = 0;
	
	public TzBatteryListener() {
		
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// Try reading battery intent
		try {
			// Get current level and plugged flag
			lastReadingLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
			int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);
			
			// Create hashmap with level and plugged state
			HashMap<String, String> level = new HashMap<String, String>();
			level.put("level", String.valueOf(lastReadingLevel));
			level.put("charge", plugged == 2 ? "plugged" : "battery");
			
			// Broadcast hashmap
			TzBroadcastGroup.broadcast(new TzBroadcastGroup.BroadcastedPackage(level, TzBroadcastGroup.PackageType.battery));
		} catch (Exception ex) {
			
		}
	}
}