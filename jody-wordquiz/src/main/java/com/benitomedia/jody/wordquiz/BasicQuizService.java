package com.benitomedia.jody.wordquiz;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BasicQuizService implements QuizService {

	private DataStore dataStore;
	
	private static final long RECENT_THRESHOLD = 1000 * 60 * 60 * 12;   // 12 HOURS
	
	public BasicQuizService(DataStore dataStore) {	// constructor -- use to create a new dataStore
		this.dataStore = dataStore;					// this.dataStore is the field that was defined up in the private statement
												// - - regular dataStore is the argument		
	}
	
	// returns null if no words in word list
	@Override
	public DictionaryEntry getWordToTest() {
		List<DictionaryEntry> allEntries = dataStore.getAllEntries();
		
		if(allEntries == null || allEntries.isEmpty()) {
			return null;
		}
		
		Map<Long, Long> mostRecentQuiz = new HashMap<>(); 
		Map<Long, Long> mostRecentSuccess = new HashMap<>();

/*  Made into method: getPartitionedQuizResults
		Map<Long, List<QuizResult>> partitionedResults;
		partitionedResults = allQuizResults.stream()
				.sorted((a,b) -> b.getTimestamp().compareTo(a.getTimestamp()))  // in reverse order by timestamp
				.collect(Collectors.groupingBy(qr -> qr.getEntryId()));
*/
		Map<Long, List<QuizResult>> partitionedResults = getPartitionedQuizResults();
		
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
		if(difference < RECENT_THRESHOLD ) {
			return 1;
		}
		return 0;
	}
	
	// List is grouped by entryId and within each group order by most recent timestamp first
	public Map<Long, List<QuizResult>> getPartitionedQuizResults() {
		List<QuizResult> allQuizResults = dataStore.getAllQuizResults();
		Map<Long, List<QuizResult>> partitionedResults;
		partitionedResults = allQuizResults.stream()
				.sorted((a,b) -> b.getTimestamp().compareTo(a.getTimestamp()))  // in reverse order by timestamp
				.collect(Collectors.groupingBy(qr -> qr.getEntryId()));
		return partitionedResults;
	}
	
	@Override
	public DictionaryEntry createEntry(String word, String partOfSpeech, String definition, boolean overwrite) {
		return dataStore.saveEntry(word, partOfSpeech, definition, overwrite);
	}
	
	@Override
	public DictionaryEntry deleteEntry(long entryId) {	
		return dataStore.removeEntryAndResults(entryId);
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

	@Override
	public List<EntrySummary> getAllSummaries() {
		List<EntrySummary> sortedSummaries = new ArrayList<>();
		List<DictionaryEntry> sortedEntries = getAllWords();
		Map<Long, List<QuizResult>> partitionedResults = getPartitionedQuizResults();
		for(DictionaryEntry entry: sortedEntries) {
			List<QuizResult> currentIdQRList = partitionedResults.get(entry.getId());
			sortedSummaries.add(createSummary(entry, currentIdQRList));	
		}
		return sortedSummaries;
	}
	
	// I'm making an assumption that this list is ordered by reverse timestamp -- as we did above
	public EntrySummary createSummary(DictionaryEntry entry, List<QuizResult> resultList) {
		EntrySummary summary = new EntrySummary();
		
		if(resultList == null) {
			resultList = new ArrayList<>();
		}
		
		// all result numbers and percent will be 0 if word has not been tested
		int successCount = 0;
		int failureCount = 0;
		for (QuizResult qr : resultList) {
			if(qr.getResult().equals(WordQuizConstants.SUCCESS)) {
				successCount++; 
			} else if(qr.getResult().equals(WordQuizConstants.FAILURE)) {
				failureCount++;
			}
		}
		int total = successCount + failureCount;
		float successPercent = 0;
		String successPercentString = "0.0";
		if(total > 0) {
			successPercent = (float) successCount / total * 100;
			successPercentString = String.format("%.1f", successPercent);
		}
		summary.setEntry(entry);
		summary.setSuccessTotal(successCount);
		summary.setFailureTotal(failureCount);
		summary.setSuccessPercent(successPercent);
		summary.setSuccessPercentString(successPercentString);
		
		if(!resultList.isEmpty()) {
			Long mostRecentDate = resultList.get(0).getTimestamp();
			summary.setMostRecentDate(mostRecentDate);
			DateFormat outputFormat = new SimpleDateFormat("MMM dd yyyy");
			Date d = new Date(mostRecentDate);
			summary.setMostRecentDateString(outputFormat.format(d));
			
			Long firstDate = resultList.get(resultList.size() - 1).getTimestamp();
			summary.setFirstDate(firstDate);
			d = new Date(firstDate);
			summary.setFirstDateString(outputFormat.format(d));
			
			// Initialize in case there has been no success
			summary.setMostRecentSuccessDate(0L);
			summary.setMostRecentSuccessDateString("");

			for(QuizResult qr : resultList) {
				if(qr.getResult().equals(WordQuizConstants.SUCCESS)) {
					summary.setMostRecentSuccessDate(qr.getTimestamp());
					d = new Date(qr.getTimestamp());
					summary.setMostRecentSuccessDateString(outputFormat.format(d));
					break;
				}	
			}	
			
		} else {
			summary.setMostRecentDate(0L);
			summary.setMostRecentDateString("");
			summary.setMostRecentSuccessDate(0L);
			summary.setMostRecentSuccessDateString("");
			summary.setFirstDate(0L);
			summary.setFirstDateString("");
		}

		return summary;
	}
	
	@Override
	public EntrySummary getSummary(String word, String partOfSpeech) {
		DictionaryEntry entry = dataStore.getEntry(word, partOfSpeech);
		if(entry == null)
			return null;
		Map<Long, List<QuizResult>> partitionedResults = getPartitionedQuizResults();
		List<QuizResult> currentIdQRList = partitionedResults.get(entry.getId());
		return createSummary(entry, currentIdQRList);
	}
}
