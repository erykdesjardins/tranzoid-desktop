package com.ryk.tzmarkup;

import java.util.HashMap;

public class TzVocab {
	HashMap<String, String> relations;
	Languages language;
	
	public TzVocab(Languages lang) {
		relations = TzVocabBuilder.getLanguage(String.valueOf(lang));
	}
	
	public String definitionOf(String word) {
		String value = relations.get(word);
		return value == null ? word : value;
	}
	
	public static enum Languages {
		fr, en, sp
	}
}
