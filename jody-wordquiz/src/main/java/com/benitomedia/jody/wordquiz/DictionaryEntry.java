package com.benitomedia.jody.wordquiz;

public class DictionaryEntry {

	private Long id;
	private String word;
	private String partOfSpeech;
	private String definition;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getPartOfSpeech() {
		return partOfSpeech;
	}

	public void setPartOfSpeech(String partOfSpeech) {
		this.partOfSpeech = partOfSpeech;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}
	
	@Override
	public String toString() {
		return id + ": " + word + " (" + partOfSpeech + "): " + definition;
 	}
	


	// in Eclipse, press Shift-Alt-S-R to automatically generate getters & setters.

}

