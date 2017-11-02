package com.benitomedia.jody.wordquiz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BasicQuizService implements QuizService {

	private DataStore dataStore;
	
	private static final long ONE_DAY_MILLIS = 1000 * 60 * 60 * 24;
	
	public BasicQuizService(DataStore dataStore) {	// constructor -- use to create a new dataStore
		this.dataStore = dataStore;					// this.dataStore is the field that was defined up in the private statement
												// - - regular dataStore is the argument		
	}
	
	// returns null if no words in word list
	@Override
	public DictionaryEntry getWordToTest() {
		List<DictionaryEntry> allEntries = dataStore.getAllEntries();
		List<QuizResult> allQuizResults = dataStore.getAllQuizResults();
		
		if(allEntries == null || allEntries.isEmpty()) {
			return null;
		}
		
		Map<Long, Long> mostRecentQuiz = new HashMap<>(); 
		Map<Long, Long> mostRecentSuccess = new HashMap<>();
					
		Map<Long, List<QuizResult>> partitionedResults;
		partitionedResults = allQuizResults.stream()
				.sorted((a,b) -> b.getTimestamp().compareTo(a.getTimestamp()))  // in reverse order by timestamp
				.collect(Collectors.groupingBy(qr -> qr.getEntryId()));
		
		for(DictionaryEntry entry : allEntries) {
			
			// if entry has no results (never been tested), return it to be tested
			if (!partitionedResults.containsKey(entry.getId())) {
				return entry;
			}
		
			// other wise, compile mostRecentQuiz and mostRecentSuccess maps (0 if no success) for all DictionaryEntry items
			Long currentId = entry.getId();
			List<QuizResult> currentIdQRList = partitionedResults.get(currentId);
			
			mostRecentQuiz.put(currentId, (currentIdQRList.get(0)).getTimestamp());
						
			mostRecentSuccess.put(currentId, findMostRecentSuccess(currentIdQRList));
		}	
/*		
// FOR DEBUGGING
		System.out.println("mostRecentQuiz");
		for (Map.Entry<Long, Long> entry : mostRecentQuiz.entrySet()) {
		    Long key = entry.getKey();
		    Long timestamp = entry.getValue();
		    System.out.println(key.toString() + ":" + timestamp.toString());
		}
		System.out.println("mostRecentSuccess");
		for (Map.Entry<Long, Long> entry : mostRecentSuccess.entrySet()) {
		    Long key = entry.getKey();
		    Long timestamp = entry.getValue();
		    System.out.println(key.toString() + ":" + timestamp.toString());
		}
// END DEBUGGING
 * */

		// logic to use the 2 maps to choose a word
		Long now = System.currentTimeMillis();
		
		// give preference to ones tested more than a day ago and within that, tested with success longest ago
		List<DictionaryEntry> sortedDictionaryEntries = allEntries.stream()
				.sorted((a,b) -> {
					int isRecentA = isRecent(now, mostRecentQuiz.get(a.getId()));
					int isRecentB = isRecent(now, mostRecentQuiz.get(b.getId()));
					
					if (isRecentA != isRecentB) {
						return Integer.compare(isRecentA, isRecentB);	// returns the one not recent	
					}
					// get timestamp for each of the a and b entries
					Long mostRecentSuccessA = mostRecentSuccess.get(a.getId());
					Long mostRecentSuccessB = mostRecentSuccess.get(b.getId());
				
					return Long.compare(mostRecentSuccessA, mostRecentSuccessB);
				})
				.collect(Collectors.toList());

		return sortedDictionaryEntries.get(0);				
	}
	
	private Long findMostRecentSuccess(List<QuizResult> currentIdQRList) {
		for (QuizResult qr : currentIdQRList) {
			if(qr.getResult().equals(WordQuizConstants.SUCCESS)) {
				return qr.getTimestamp();
			}
		}
		return 0L;  	// timestamp of 0 means there were no successes
	}
	
	// returns 1 if recent, 0 if not
	private int isRecent(Long now, Long timestamp) { 
		Long difference = now - timestamp;
		if(difference < ONE_DAY_MILLIS ) {
			return 1;
		}
		return 0;
	}
	
	@Override
	public DictionaryEntry createEntry(String word, String partOfSpeech, String definition, boolean overwrite) {
		return dataStore.saveEntry(word, partOfSpeech, definition, overwrite);
	}
	
	@Override
	public void recordResult(long entryID, String resultString) {
		dataStore.saveQuizResult(entryID, resultString);
	}
	
	@Override
	public List<DictionaryEntry> getAllWords() {
		List<DictionaryEntry> allEntries = dataStore.getAllEntries();
		List<DictionaryEntry> sortedEntries = allEntries.stream()
				.sorted((a,b) -> a.getWord().compareToIgnoreCase(b.getWord()))
				.collect(Collectors.toList());
		return sortedEntries;
	}
	
	@Override
	public DictionaryEntry getEntry(String word, String partOfSpeech) {
		return dataStore.getEntry(word, partOfSpeech);
	}
	
}
