package com.benitomedia.jody.wordquiz;

public class QuizResult {
	
	private Long entryId;
	private Long timestamp;
	private String result;

	public Long getEntryId() {
		return entryId;
	}

	public void setEntryId(Long entryId) {
		this.entryId = entryId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	@Override
	public String toString() {
		return entryId + " : " + timestamp + " : " + result;
 	}

}
