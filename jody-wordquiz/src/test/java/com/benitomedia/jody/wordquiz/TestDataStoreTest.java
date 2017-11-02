package com.benitomedia.jody.wordquiz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

public class TestDataStoreTest {
	
	@Test
	public void basicTestDataStoreTest() {
		DataStore dataStore = new TestDataStore();
		assertNotNull(dataStore);
		// ADD TEST DATA HERE (3 entries)
		// dataStore.saveEntry("word", "partofspeech", "definition", boolean true to overwrite)
		dataStore.saveEntry("pencil", "noun", "writing implement", true);
		dataStore.saveEntry("paper", "noun", "material to write on", true);
		dataStore.saveEntry("erase", "noun", "get rid of pencil marks", true);
	
		List<DictionaryEntry> allEntries = dataStore.getAllEntries();
		assertNotNull(allEntries);
		assertEquals(3, allEntries.size());

	}
	
	@Test
	public void addEntryTest() {
		DataStore dataStore = new TestDataStore();
		// ADD TEST DATA HERE
		// dataStore.saveEntry("word", "partofspeech", "definition", "true to overwrite, false if not")
		dataStore.saveEntry("pencil", "noun", "writing implement", true);
		dataStore.saveEntry("paper", "noun", "material to write on", true);
		dataStore.saveEntry("erase", "noun", "get rid of pencil marks", true);
		
		List<DictionaryEntry> allEntriesFirst = dataStore.getAllEntries();
		// add another entry
		DictionaryEntry newlySavedEntry = dataStore.saveEntry("tablet", "noun", "pad of paper", true);
		assertNotNull(newlySavedEntry);
		assertEquals("tablet", newlySavedEntry.getWord());
		
		List<DictionaryEntry> allEntriesSecond = dataStore.getAllEntries();
		assertEquals(allEntriesFirst.size() + 1, allEntriesSecond.size());
		
	}
	@Test
	public void overwriteEntryTest() {
		DataStore dataStore = new TestDataStore();
		// ADD TEST DATA HERE
		// dataStore.saveEntry("word", "partofspeech", "definition", "true to overwrite, false if not")
		dataStore.saveEntry("pencil", "noun", "writing implement", true);
		dataStore.saveEntry("paper", "noun", "material to write on", true);
		dataStore.saveEntry("eraser", "noun", "get rid of pencil marks", true);
		
		//add new "tablet" entry
		List<DictionaryEntry> allEntriesFirst = dataStore.getAllEntries();
		DictionaryEntry newlySavedEntry = dataStore.saveEntry("tablet", "noun", "pad of paper", true);
		assertNotNull(newlySavedEntry);
		assertEquals("tablet", newlySavedEntry.getWord());
		assertEquals("pad of paper", newlySavedEntry.getDefinition());
		
		List<DictionaryEntry> allEntriesSecond = dataStore.getAllEntries();
		assertEquals(allEntriesFirst.size() + 1, allEntriesSecond.size());
		
		//overwrite existing "tablet" entry
		DictionaryEntry overwrittenEntry = dataStore.saveEntry("tablet", "noun", "dose of medicine in solid form", true);
		assertNotNull(overwrittenEntry);
		assertEquals("tablet", newlySavedEntry.getWord());
		assertEquals("dose of medicine in solid form", newlySavedEntry.getDefinition());
		
		List<DictionaryEntry> allEntriesThird = dataStore.getAllEntries();
		assertEquals(allEntriesSecond.size(), allEntriesThird.size());
		
		//try to add but do NOT overwrite existing "tablet" entry
		DictionaryEntry notoverwrittenEntry = dataStore.saveEntry("tablet", "noun", "Nexus7", false);
		assertNull(notoverwrittenEntry);
		assertEquals("tablet", newlySavedEntry.getWord());
		assertEquals("dose of medicine in solid form", newlySavedEntry.getDefinition());
				
		List<DictionaryEntry> allEntriesFourth = dataStore.getAllEntries();
		assertEquals(allEntriesThird.size(), allEntriesFourth.size());
	}
	
}
