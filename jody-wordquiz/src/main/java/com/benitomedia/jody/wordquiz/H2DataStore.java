package com.benitomedia.jody.wordquiz;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2DataStore implements DataStore {

	private static final String DB_DRIVER = "org.h2.Driver";
	private static final String DB_CONNECTION_PREFIX = "jdbc:h2:";
	private static final String DB_USER = "";
	private static final String DB_PASSWORD = "";
	
	private static final String ENTRY_TABLE = "WORDENTRIES";
	private static final String RESULTS_TABLE = "QUIZRESULTS";

	private final String dbFile;

	private Connection connection;
	
	private static final Logger logger = LoggerFactory.getLogger(H2DataStore.class);
	
	public H2DataStore(String filename) {
		dbFile = DB_CONNECTION_PREFIX + filename;
		setupDB();
	}

	private void setupDB() {
		try(Connection c = getDBConnection()) {			
			DatabaseMetaData md = c.getMetaData();
			ResultSet rs = md.getTables(null, null, "%", null);
			Set<String> tableNames = new HashSet<>();
			while (rs.next()) {
				String tableName = rs.getString(3);
				tableNames.add(tableName);
			}
			if(!tableNames.contains(ENTRY_TABLE))
				createEntriesTable(c);
			else
				logger.info("Already had table " + ENTRY_TABLE);
			
			if(!tableNames.contains(RESULTS_TABLE))
				createResultsTable(c);
			else
				logger.info("Already had table " + RESULTS_TABLE);
					
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createEntriesTable(Connection c) throws SQLException {
		String createQuery = "CREATE TABLE WORDENTRIES(id bigint auto_increment primary key, word varchar(50), partOfSpeech varchar(20), definition varchar(255))";
		Statement s = c.createStatement();
		s.executeUpdate(createQuery);
		s.close();
		c.commit();
		logger.info("Created WORDENTRIES");
	}
	
	private void createResultsTable(Connection c) throws SQLException {
		String createQuery = "CREATE TABLE QUIZRESULTS(entryid bigint, timestamp bigint, result varchar(20))";
		Statement s = c.createStatement();
		s.executeUpdate(createQuery);
		s.close();
		c.commit();
		logger.info("Created QUIZRESULTS");
	}
	
	private List<DictionaryEntry> loadAllEntries(Connection c) throws SQLException {
		String query = "SELECT * FROM " + ENTRY_TABLE;
		// ResultSetProcessor<List<DictionaryEntry>> p = rs -> parseEntries(rs);
		List<DictionaryEntry> entries = runQuery(c, query, rs -> parseEntries(rs));
		return entries;
	}
	
	private List<QuizResult> loadAllQuizResults(Connection c) throws SQLException {
		String query = "SELECT * FROM " + RESULTS_TABLE;
		List<QuizResult> qrs = runQuery(c, query, rs -> parseQuizResults(rs));
		return qrs;
	}
	
	private List<DictionaryEntry> parseEntries(ResultSet rs) throws SQLException {
		List<DictionaryEntry> entries = new ArrayList<>();
		while (rs.next()) {
			Long id = rs.getLong("id");
			String word = rs.getString("word");
			String partOfSpeech = rs.getString("partOfSpeech");
			String definition = rs.getString("definition");
			
			DictionaryEntry entry = new DictionaryEntry();
			entry.setId(id);
			entry.setWord(word);
			entry.setPartOfSpeech(partOfSpeech);
			entry.setDefinition(definition);
			entries.add(entry);
		}
		return entries;
	}
	
	private List<QuizResult> parseQuizResults(ResultSet rs) throws SQLException {
		List<QuizResult> results = new ArrayList<>();
		while (rs.next()) {
			Long entryId = rs.getLong("entryid");
			Long timestamp = rs.getLong("timestamp");
			String result = rs.getString("result");

			QuizResult qr = new QuizResult();
			qr.setEntryId(entryId);
			qr.setTimestamp(timestamp);
			qr.setResult(result);
			results.add(qr);
		}
		return results;
	}
	
	private <T> T runQuery(Connection c, String query, ResultSetProcessor<T> processor) throws SQLException {
		Statement s = c.createStatement();
		ResultSet rs = s.executeQuery(query);
		T ret = processor.process(rs);
		s.close();
		return ret;
	}

	private Connection getDBConnection() throws SQLException {
		if(connection == null || connection.isClosed()) {
			try {
				Class.forName(DB_DRIVER);
			} catch (ClassNotFoundException e) {
				System.out.println(e.getMessage());
			}
			connection = DriverManager.getConnection(dbFile, DB_USER, DB_PASSWORD);
		}
		return connection;
	}

	@Override
	public List<DictionaryEntry> getAllEntries() {
		try(Connection c = getDBConnection()) {
			return loadAllEntries(c);
		} catch (SQLException e) {
			 e.printStackTrace();
		}
		return null;
	}

	@Override
	public DictionaryEntry getEntry(Long id) {
		return getAllEntries().stream()
				.filter(e -> e.getId().equals(id))
				.findAny()
				.orElse(null);
	}

	@Override
	public DictionaryEntry getEntry(String word, String partOfSpeech) {
		return getAllEntries().stream()
				.filter(e -> e.getWord().equals(word) && e.getPartOfSpeech().equals(partOfSpeech))
				.findAny()
				.orElse(null);
	}

	@Override
	public DictionaryEntry saveEntry(String word, String partOfSpeech, String definition, boolean overwrite) {
		DictionaryEntry existingEntry = getEntry(word, partOfSpeech);
		if (existingEntry == null) {
			return insertEntry(word, partOfSpeech, definition);
		} else {
			if (overwrite) {
				updateEntry(existingEntry.getId(), definition);
				existingEntry.setDefinition(definition);
				return existingEntry;
			} else {
				return null;
			}
		}
	}
	
	private DictionaryEntry insertEntry(String word, String partOfSpeech, String definition) {
		try(Connection c = getDBConnection()) {
			String query = "INSERT INTO WORDENTRIES(word, partOfSpeech, definition) values(?,?,?)";
			PreparedStatement s = c.prepareStatement(query);
			s.setString(1, word);
			s.setString(2, partOfSpeech);
			s.setString(3, definition);
			s.executeUpdate();
			ResultSet keys = s.getGeneratedKeys();
			
			keys.next();
			long id = keys.getLong(1);
			
			s.close();
			
			DictionaryEntry entry = new DictionaryEntry();
			entry.setId(id);
			entry.setWord(word);
			entry.setPartOfSpeech(partOfSpeech);
			entry.setDefinition(definition);
			
			System.out.println("Created entry: " + entry.toString());
			
			return entry;
	
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private boolean updateEntry(Long id, String definition) {
		try(Connection c = getDBConnection()) {
			String query = "UPDATE WORDENTRIES SET definition=? WHERE id=?";
			PreparedStatement s = c.prepareStatement(query);
			s.setString(1, definition);
			s.setLong(2, id);
			s.executeUpdate();
			
			s.close();

			return true;
	
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public DictionaryEntry removeEntryAndResults(Long id) {
		// TODO!!!
		return null;
	}

	@Override
	public List<QuizResult> getAllQuizResults() {
		try(Connection c = getDBConnection()) {
			return loadAllQuizResults(c);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<QuizResult> getResultsForEntry(Long entryId) {
		return getAllQuizResults().stream().filter(qr -> qr.getEntryId().equals(entryId)).collect(Collectors.toList());
	}

	@Override
	public List<QuizResult> getResultsForEntry(String word, String partOfSpeech) {
		DictionaryEntry entry = getEntry(word, partOfSpeech);
		if(entry == null) return new ArrayList<>();
		return getResultsForEntry(entry.getId());
	}

	@Override
	public void saveQuizResult(long entryID, String resultString) {
		try(Connection c = getDBConnection()) {
			String query = "INSERT INTO QUIZRESULTS(entryid, timestamp, result) values(?,?,?)";
			PreparedStatement s = c.prepareStatement(query);
			s.setLong(1, entryID);
			s.setLong(2, System.currentTimeMillis());
			s.setString(3, resultString);
			s.executeUpdate();
			s.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void forTestingSaveQuizResult(long entryID, long notNow, String resultString) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean removeAllQuizResults(long entryID) {
		// TODO Auto-generated method stub
		return false;
	}
	
	interface ResultSetProcessor<T> {
		T process(ResultSet rs) throws SQLException;
	}

}
