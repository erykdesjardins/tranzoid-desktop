package com.ryk.tzmarkup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ryk.tzandroidutil.TzPrefDatabase;

public final class TzDataRepository {
	static TzDataRepository _this = new TzDataRepository();
	
	private TzDataRepository() {
		_this = this;
	}
	
	public static TzDataRepository getRepo() {
		return _this;
	}
	
	public String parse(String input) {
		if (input == null) return input;
		
		Pattern p = Pattern.compile("\\[\\@(.*?)\\]");
		Matcher m = p.matcher(input);

		while(m.find()) {
		    input = input.replace("[@" + m.group(1) + "]", getData(m.group(1)));
		}		
		
		return input;
	}
	
	public String getData(String address) {
		String[] addresses = address.split("\\.");
		
		if (addresses[0].equals("pref")) {
			return TzPrefDatabase.getDatabase().getValue(addresses[1]).toString();
		} else if (addresses[0].equals("theme")) {
			return TzPrefDatabase.getDatabase().getValue(addresses[1]).toString();
			// return TzThemeRepository.getRepo().getTheme().getValue(addresses[1]);
		}
		
		return address;
	}
}
