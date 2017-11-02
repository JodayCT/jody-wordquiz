package com.benitomedia.jody.wordquiz;

import java.util.List;

public interface QuizService {
	
	DictionaryEntry getWordToTest();
	
	void recordResult(long entryID, String resultString);
	
	DictionaryEntry createEntry(String word, String partOfSpeech, String definiton, boolean overwrite);
	
	List<DictionaryEntry> getAllWords();
	
	DictionaryEntry getEntry(String word, String partOfSpeech);

}
