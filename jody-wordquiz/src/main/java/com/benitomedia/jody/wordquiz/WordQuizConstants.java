package com.benitomedia.jody.wordquiz;

import java.util.Arrays;
import java.util.List;

public class WordQuizConstants {
	
	public static final String SUCCESS = "success";
	public static final String FAILURE = "failure";
	
	public static class PartOfSpeech {
		public static final String NOUN = "noun";
		public static final String VERB = "verb";
		public static final String ADJECTIVE = "adjective";
		public static final String ADVERB = "adverb";
		public static final String PREPOSITION = "preposition";

		public static final List<String> ALL = Arrays.asList(NOUN, VERB, ADJECTIVE, ADVERB, PREPOSITION);
	}
}
