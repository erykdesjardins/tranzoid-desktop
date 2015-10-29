package com.ryk.tzmarkup;

public class TzMarkupParser {
	TzVocabRepository vocab = TzVocabRepository.getRepo();
	TzDataRepository data = TzDataRepository.getRepo();
	TzFunctionRepository func = TzFunctionRepository.getRepo();
	
	public TzMarkupParser() {
		
	}
	
	public String parse(String input) {
		return vocab.parse(data.parse(func.parse(input)));
	}
}
