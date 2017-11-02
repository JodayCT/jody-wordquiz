package com.benitomedia.jody.wordquiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TestDataStore implements DataStore {

	private Map<Long, DictionaryEntry> entryMap = new ConcurrentHashMap<>();
	
	private List<QuizResult> resultList = new ArrayList<>();
	
	public TestDataStore() {
		// 
	}

	public List<DictionaryEntry> getAllEntries() {
		return entryMap.values().stream().collect(Collectors.toList());
	}

	public DictionaryEntry getEntry(Long id) {
		DictionaryEntry entry = entryMap.get(id);
		return entry;
	}

	public DictionaryEntry getEntry(String word, String partOfSpeech) {
		for (DictionaryEntry entry : entryMap.values()) {
			if (word.equals(entry.getWord()) && partOfSpeech.equals(entry.getPartOfSpeech())) {
				return entry;
			}
		}
		return null;
	}

	public synchronized DictionaryEntry saveEntry(String word, String partOfSpeech, String definition, boolean overwrite) {
		DictionaryEntry existingEntry = getEntry(word, partOfSpeech);
		if (existingEntry == null) {
			long id = getAvailableKey();
			DictionaryEntry newEntry = new DictionaryEntry();  //create a new object
			newEntry.setId(id);
			newEntry.setWord(word);
			newEntry.setPartOfSpeech(partOfSpeech);
			newEntry.setDefinition(definition);
			entryMap.put(id, newEntry);
			return newEntry;
			
		} else {
			if (overwrite) {
				existingEntry.setDefinition(definition);
				return existingEntry;
			} else {
				return null;
			}
		}
	}
	
	public synchronized DictionaryEntry removeEntryAndResults(Long id) {
		DictionaryEntry entry = entryMap.remove(id);
		if (entry != null) {
			removeAllQuizResults(id);
		}
		return entry;
	}
		
	private long getAvailableKey() {
		long i = 1;
		while (entryMap.containsKey(i)) {
			i++;
		}
		return i;
	}
	
	public List<QuizResult> getAllQuizResults() {
		return resultList.stream().collect(Collectors.toList());
	}

	// returns a list of results for a particular word -- or a null list
	public List<QuizResult> getResultsForEntry(Long entryId) {
		/*
		4 different ways to do this loop:
		
		List<QuizResult> entries = new ArrayList<>(); 
		for (int i = 0; i < resultList.size(); i++) {	
			QuizResult entry = resultList.get(i);
			if (entryId.equals(entry.getEntryId()))
					entries.add(entry);		
		}
		
		for (QuizResult entry : resultList) {	
			if (entryId.equals(entry.getEntryId()))
				entries.add(entry);		
		}
		
		Predicate<QuizResult> matches = qr -> qr.getEntryId().equals(entryId);
		return resultList.stream().filter(matches).collect(Collectors.toList());
		
		or		
		 */		
		return resultList.stream()
				.filter(qr -> qr.getEntryId().equals(entryId))
				.collect(Collectors.toList());
	}

	// returns a list of results for a particular word -- or a null list
	public List<QuizResult> getResultsForEntry(String word, String partOfSpeech) {
		DictionaryEntry entry = getEntry(word, partOfSpeech);
		if (entry == null)
			return Collections.emptyList();
		return getResultsForEntry(entry.getId());
	}

	public void saveQuizResult(long entryID, String resultString) {
		long now = System.currentTimeMillis();
		QuizResult newResult = new QuizResult();  //create a new object
		newResult.setEntryId(entryID);
		newResult.setTimestamp(now);
		newResult.setResult(resultString);
		resultList.add(newResult);
		return;
	}
	
	public void forTestingSaveQuizResult(long entryID, long timestamp, String resultString) {
		QuizResult newResult = new QuizResult();  //create a new object
		newResult.setEntryId(entryID);
		newResult.setTimestamp(timestamp);
		newResult.setResult(resultString);
		resultList.add(newResult);
		return;
	}

	public boolean removeAllQuizResults(long entryID) {
		return resultList.removeIf(qr -> qr.getEntryId().equals(entryID));
	}	
	
}
