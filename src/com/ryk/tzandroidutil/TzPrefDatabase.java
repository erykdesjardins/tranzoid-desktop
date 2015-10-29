package com.ryk.tzandroidutil;

import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

// Preference database
// Contains all user preferences and defaults
// A singleton
public final class TzPrefDatabase {
	private static TzPrefDatabase _this = new TzPrefDatabase();
	private PrefSet prefs;
	private final String filename = "pref.json";
	
	// Constructor
	private TzPrefDatabase() {
		_this = this;
		_this.prefs = new PrefSet();
		_this.load();
	}
	
	// Singleton accessor
	public static TzPrefDatabase getDatabase() {
		return _this;
	}
	
	public Object getValue(String pref) {
		return prefs.get(pref);
	}
	
	public TzPrefDatabase setValue(String pref, Object value) {
		String rep = value.toString();
		
		if (rep.equals(Boolean.toString(true))) value = true;
		else if (rep.equals(Boolean.toString(false))) value = false;
		
		prefs.put(pref, value);
		return this;
	}
	
	public String asJSON() {
		return prefs.toJson();
	}
	
	public void load() {
		TzFileBrowser browser = new TzFileBrowser();
		String json = browser.readSingleString(browser.getNewFileAtDefaultDirectory(filename));
		
		if (json.equals("")) {
			prefs.loadDefault();
			save();
		} else {
			prefs = (new Gson()).fromJson(json, PrefSet.class);
		}
		
		TzThemeRepository.getRepo().setTheme(prefs.get("theme").toString());
		
		return;
	}
	
	public void save() {
		TzFileBrowser browser = new TzFileBrowser();
		browser.overwriteFileContent(browser.getNewFileAtDefaultDirectory(filename), prefs.toJson());
	}
	
	private class PrefSet {
		@SerializedName("prefs")
		private HashMap<String, Object> sets;
		
		public PrefSet() {
			sets = new HashMap<String, Object>();
		}
		
		public Object get(String key) {
			Object val = sets.get(key); 
			return val == null ? "" : val;
		}
		
		public void put(String key, Object value) {
			sets.put(key, value);
			
			if (key.equals("theme")) {
				TzThemeRepository.getRepo().setTheme(value.toString());
				
				for (Entry<String, String> pref : TzThemeRepository.getRepo().getTheme().getMap().entrySet()) {
					put(pref.getKey(), pref.getValue());
				}
			}
		}
		
		public void clear() {
			sets.clear();
		}
		
		public void loadDefault() {
			clear();
			
			put("notificationAudio", true);
			put("interfaceAudio", true);
			put("addAudio", true);
			put("popupAudio", true);	
			
			put("incomingColor", "rgba(47, 0, 79, 0.7)");
			put("outgoingColor", "rgba(0, 10, 81, 0.7)");		
			
			put("lang", "en");
			put("theme", "blurrysky");
			
			put("color1", "#606c88");
			put("color2", "#3f4c6b");
			put("bar1", "#aebcbf");
			put("bar2", "#6ebf74");
			put("light1", "#b0d4e3");
			put("light2", "#88bacf");
			
			put("loading", "rgba(96,108,136,0.9)");
			
			put("hoverColor", "rgba(0, 0, 0, 0.5)");
			put("fontColor", "#FFF");
			put("subfontColor", "#EEE");
			
			put("messageIn", "rgba(47, 0, 79, 0.7)");
			put("messageOut", "rgba(0, 10, 81, 0.7)");

			put("wallpaper", "../mediaimg/centerbg.png");
		}
		
		public String toJson() {
			return "{\"prefs\":" + (new Gson()).toJson(sets) + "}";
		}
	}
}
