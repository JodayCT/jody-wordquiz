package com.benitomedia.jody.wordquiz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

public class BasicQuizServiceTest {
	
	private static final long ONE_DAY_MILLIS = 1000 * 60 * 60 * 24;            			
	
	
	@Test
	public void getWordWhenNoData() {
		DataStore dataStore = new TestDataStore();
		QuizService quizService = new BasicQuizService(dataStore);
		DictionaryEntry entryToTest = quizService.getWordToTest();
		assertNull(entryToTest);
	}
	@Test
	public void getWordNoResults() {
		DataStore dataStore = new TestDataStore();
		
		// dataStore.saveEntry("word", "partofspeech", "definition", "true to overwrite, false if not")
		dataStore.saveEntry("banjo", "noun", "musical string instrument", true);
		dataStore.saveEntry("guitar", "noun", "musical string instrument", true);
		dataStore.saveEntry("ukulele", "noun", "musical string instrument", true);
						
		assertEquals(3, dataStore.getAllEntries().size());

		QuizService quizService = new BasicQuizService(dataStore);
		DictionaryEntry entryToTest = quizService.getWordToTest();
		assertEquals("noun", entryToTest.getPartOfSpeech());
		String s = entryToTest.getId().toString() + ": " + entryToTest.getWord() +
				": " + entryToTest.getPartOfSpeech() + ": " + entryToTest.getDefinition();
		System.out.println(s);
	}
	@Test
	public void getWordWhenResultsForAllButOne() {
		DataStore dataStore = new TestDataStore();
				
		// dataStore.saveEntry("word", "partofspeech", "definition", "true to overwrite, false if not")
		dataStore.saveEntry("banjo", "noun", "musical string instrument", true);
		dataStore.saveEntry("guitar", "noun", "musical string instrument", true);
		dataStore.saveEntry("ukulele", "noun", "musical string instrument", true);
		assertEquals(3, dataStore.getAllEntries().size());
		
		// dataStore.forTestingSaveQuizResult(long entryID, long notNow, String resultString)
		Long now = System.currentTimeMillis();
		
		Long banjoEntryId = dataStore.getEntry("banjo", "noun").getId();
		dataStore.forTestingSaveQuizResult(banjoEntryId, now - 2 * ONE_DAY_MILLIS, WordQuizConstants.FAILURE);
		Long ukuleleEntryId = dataStore.getEntry("ukulele", "noun").getId();
		dataStore.forTestingSaveQuizResult(ukuleleEntryId, now - 5 * ONE_DAY_MILLIS, WordQuizConstants.FAILURE);
		
		List<QuizResult> allResults = dataStore.getAllQuizResults();
		assertEquals(2, allResults.size());
		for(QuizResult result : allResults) {
			String s = result.getEntryId().toString() + ": " + result.getTimestamp() + " : " + result.getResult();
			System.out.println(s);
		}	
		
		QuizService quizService = new BasicQuizService(dataStore);
		DictionaryEntry entryToTest = quizService.getWordToTest();
		String s = entryToTest.getId().toString() + ": " + entryToTest.getWord() +
				": " + entryToTest.getPartOfSpeech() + ": " + entryToTest.getDefinition();
		System.out.println(s);
		assertEquals("guitar", entryToTest.getWord());
	}
	
	@Test
	public void getWordForVariousResults() {
		DataStore dataStore = new TestDataStore();
		QuizService quizService = new BasicQuizService(dataStore);
				
		// dataStore.saveEntry("word", "partofspeech", "definition", "true to overwrite, false if not")
		dataStore.saveEntry("banjo", "noun", "musical string instrument", true);
		dataStore.saveEntry("guitar", "noun", "musical string instrument", true);
		dataStore.saveEntry("ukulele", "noun", "musical string instrument", true);
		
		// dataStore.forTestingSaveQuizResult(long entryID, long notNow, String resultString)
		
// choose one that failed longest ago		
		Long banjoEntryId = dataStore.getEntry("banjo", "noun").getId();
		Long guitarEntryId = dataStore.getEntry("guitar", "noun").getId();
		Long ukuleleEntryId = dataStore.getEntry("ukulele", "noun").getId();
		
		dataStore.forTestingSaveQuizResult(banjoEntryId, xHoursAgo(3), WordQuizConstants.SUCCESS);
		dataStore.forTestingSaveQuizResult(guitarEntryId, xHoursAgo(3), WordQuizConstants.FAILURE);
		dataStore.forTestingSaveQuizResult(ukuleleEntryId, xHoursAgo(3), WordQuizConstants.SUCCESS);

		// dataStore.forTestingSaveQuizResult(banjoEntryId, now, "failure");
		// dataStore.forTestingSaveQuizResult(guitarEntryId, now - 2 * ONE_DAY_MILLIS, "success");
		
		List<QuizResult> allResults = dataStore.getAllQuizResults();

		// to help debug
		for(QuizResult result : allResults) {
			System.out.println(result.toString());
		}
		// to help debug
		DictionaryEntry entryToTest = quizService.getWordToTest();
		System.out.println(entryToTest.toString());
		assertEquals("guitar", entryToTest.getWord());
	}
	
	private long xHoursAgo(int x) {
		long hour = 1000 * 60 * 60;
		return System.currentTimeMillis() - x * hour;
	}
}
