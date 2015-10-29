package com.ryk.tzclientlib;

import java.util.HashMap;
import com.google.gson.annotations.SerializedName;

// Represents an action to be done requested by server
// Serialized in JSON format
public class TzCommand {
	@SerializedName("action")
	private String commandName; 			// Command name
	
	@SerializedName("data")
	private HashMap<String, String> params; // Command details
	
	// Gets the name of the requested command
	public String getCommandName() {
		return commandName;
	}
	
	// Gets the details associated with the requested command
	public String extractData(String index) {
		return params.get(index);
	}
}
