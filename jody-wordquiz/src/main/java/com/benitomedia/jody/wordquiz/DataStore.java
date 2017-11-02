package com.benitomedia.jody.wordquiz;

import java.util.List;

public interface DataStore {

	List<DictionaryEntry> getAllEntries();

	DictionaryEntry getEntry(Long id);

	DictionaryEntry getEntry(String word, String partOfSpeech);

	DictionaryEntry saveEntry(String word, String partOfSpeech, String definition, boolean overwrite);
	
	DictionaryEntry removeEntryAndResults(Long id);

	List<QuizResult> getAllQuizResults();

	List<QuizResult> getResultsForEntry(Long entryId);

	List<QuizResult> getResultsForEntry(String word, String partOfSpeech);

	void saveQuizResult(long entryID, String resultString);
	
	void forTestingSaveQuizResult(long entryID, long notNow, String resultString);  // this is to allow dummy results data for testing
	
	boolean removeAllQuizResults(long entryID);

}
