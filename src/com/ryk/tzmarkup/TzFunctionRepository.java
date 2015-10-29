package com.ryk.tzmarkup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TzFunctionRepository {
	static TzFunctionRepository _this = new TzFunctionRepository();
	
	private TzFunctionRepository() {
		_this = this;
	}
	
	public static TzFunctionRepository getRepo() {
		return _this;
	}	
	
	public String parse(String input) {
		if (input == null) return input;
		
		Pattern p = Pattern.compile("\\{\\=(.*?)\\}");
		Matcher m = p.matcher(input);

		while(m.find()) {
		    input = input.replace("{=" + m.group(1) + "}", executeFunction(m.group(1)));
		}		
		
		return input;
	}
	
	public String executeFunction(String func) {
		if (func.equals("echo")) {
			return func;
		}
		
		return func;
	}
}
