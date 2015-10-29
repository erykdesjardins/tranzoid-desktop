package com.ryk.tzmarkup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TzVocabRepository {
	static TzVocabRepository _this = new TzVocabRepository();
	TzVocab.Languages currentLanguage;
	TzVocab currentVocab;
	
	private TzVocabRepository() {
		_this = this;
	}
	
	public static TzVocabRepository getRepo() {
		return _this;
	}
	
	public void buildDico(TzVocab.Languages lang) {
		currentVocab = new TzVocab(lang);
	}
	
	public String parse(String input) {
		if (input == null) return input;
		
		Pattern p = Pattern.compile("\\[\\#(.*?)\\]");
		Matcher m = p.matcher(input);

		while(m.find()) {
		    input = input.replace("[#" + m.group(1) + "]", currentVocab.definitionOf(m.group(1)));
		}		
		
		return input;
	}
}
