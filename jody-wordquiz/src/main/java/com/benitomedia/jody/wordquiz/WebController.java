package com.benitomedia.jody.wordquiz;

import static spark.Spark.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.benitomedia.jody.wordquiz.WordQuizConstants.PartOfSpeech;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

public class WebController {
	
	public static void main (String[] args) {
		int port = 4567;
		if(args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		String dbFile = "mem:test";
		if(args.length > 1) {
			dbFile = args[1];
		}
		// DataStore dataStore = new TestDataStore();
		DataStore dataStore = new H2DataStore(dbFile);
		QuizService quizService = new BasicQuizService(dataStore);
		WebController controller = new WebController(quizService);
		controller.start(port);
	}
	
	private QuizService quizService;
	
	public WebController(QuizService quizService) {
		this.quizService = quizService;
	}

	public void start(int port) {
		port(port);
		setupRoutes();
	}
	
	private void setupRoutes() {
		
		staticFiles.location("/static");
		staticFiles.expireTime(60 * 60 * 24);
		
		get("/", (req, res) -> {
			Map<String, Object> model = new HashMap<>();
			ModelAndView mav = new ModelAndView(model, "templates/root.vm");
			return mav;
		}, new VelocityTemplateEngine());
		
		get("/testWord", (req, res) -> {
			return testWordModelAndView();
		}, new VelocityTemplateEngine());
		
		post("/testWord", (req, res) -> {
			String result = req.queryParams("result");
			String entryId = req.queryParams("entryId");
			quizService.recordResult(Long.parseLong(entryId), result);
			System.out.println("Recording result " + result + " for Entry " + entryId);
			return testWordModelAndView();	
		}, new VelocityTemplateEngine());
		
		get("/createEntry", (req, res) -> {
			return createEntryModelAndView(null, null, null, null, false);
		}, new VelocityTemplateEngine());
		
		post("/createEntry", (req, res) -> {
			String word = req.queryParams("word");
			String partOfSpeech = req.queryParams("partOfSpeech");
			String definition = req.queryParams("definition");
			String feedback;
			
			if(word == null) word = "";
			word = word.trim();
			if(word.isEmpty()) {
				feedback = "Please enter a word";
				return createEntryModelAndView(feedback, null, partOfSpeech, definition, false);
			}
			
			if(partOfSpeech == null) partOfSpeech = "";
			partOfSpeech = partOfSpeech.trim();
			if(!PartOfSpeech.ALL.contains(partOfSpeech)) {
				feedback = "Please choose a valid part of speech";
				return createEntryModelAndView(feedback, word, partOfSpeech, definition, false);
			}
			
			if(definition == null) definition = "";
			definition = definition.trim();
			if(definition.isEmpty()) {
				feedback = "Please enter a definition";
				return createEntryModelAndView(feedback, word, partOfSpeech, null, false);
			}
			
			String submitType = req.queryParams("submitType");
			boolean override = "override".equals(submitType);
			DictionaryEntry result = quizService.createEntry(word, partOfSpeech, definition, override);
					
			if (result == null) {
				DictionaryEntry oldEntry = quizService.getEntry(word, partOfSpeech);
				feedback = "This word already exists:" + oldEntry.toString();
				// show override = true
				return createEntryModelAndView(feedback, word, partOfSpeech, definition, true);	
			}
						
			feedback = "Successfully created an entry for '" + word + "'." ;
			return createEntryModelAndView(feedback, null, null, null, false);
		}, new VelocityTemplateEngine());

		get("/displayAll", (req, res) -> {
			List<EntrySummary> sortedSummaries = quizService.getAllSummaries();
			Map<String, Object> model = new HashMap<>();
				
			model.put("summaries", sortedSummaries);
			ModelAndView mav = new ModelAndView(model, "templates/displayall.vm");
			return mav;
		}, new VelocityTemplateEngine());
		
		get("/displayWord/:partOfSpeech/:word", (req, res) -> {
			String word = req.params(":word");
			String partOfSpeech = req.params(":partOfSpeech");
			EntrySummary summary = quizService.getSummary(word, partOfSpeech);
			Map<String, Object> model = new HashMap<>();
			model.put("summary", summary);
			ModelAndView mav = new ModelAndView(model, "templates/displayword.vm");
			return mav;
		}, new VelocityTemplateEngine());
		
		// delete button is in displayword.vm with click function for confirm in wordquiz.js)
		
		post("/deleteWord", (req, res) -> {
			String entryId = req.queryParams("entryId");
			          
            quizService.deleteEntry(Long.parseLong(entryId));
      
            res.redirect("/displayAll");
            return "";
        });

		
	}
	
	private ModelAndView createEntryModelAndView(String feedback, String defaultWord, String defaultPartOfSpeech, String defaultDefinition,
				boolean showOverride) {
		Map<String, Object> model = new HashMap<>();
		model.put("feedback", feedback);
		model.put("defaultWord", defaultWord);
		model.put("defaultPartOfSpeech", defaultPartOfSpeech);
		model.put("defaultDefinition", defaultDefinition);
		model.put("partsOfSpeech", PartOfSpeech.ALL);
		model.put("showOverride", showOverride);
		ModelAndView mav = new ModelAndView(model, "templates/createentry.vm");
		return mav;
	}
	
	private ModelAndView testWordModelAndView() {
		DictionaryEntry entry = quizService.getWordToTest();
		Map<String, Object> model = new HashMap<>();
		if (entry == null) {
			return new ModelAndView(model, "templates/noword.vm");
		}
	
		model.put("entry", entry);
		ModelAndView mav = new ModelAndView(model, "templates/testword.vm");
		return mav;	
	}
	
}	
	
