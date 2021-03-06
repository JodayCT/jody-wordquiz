package com.benitomedia.jody.wordquiz;

public class EntrySummary {
	
	private DictionaryEntry entry;
	private Integer successTotal;
	private Integer failureTotal;
	private float successPercent;
	private String successPercentString;
	private Long mostRecentDate;
	private String mostRecentDateString;
	private Long mostRecentSuccessDate;
	private String mostRecentSuccessDateString;
	private Long firstDate;
	private String firstDateString;
		
	public DictionaryEntry getEntry() {
		return entry;
	}
	public void setEntry(DictionaryEntry entry) {
		this.entry = entry;
	}
	public int getSuccessTotal() {
		return successTotal;
	}
	public void setSuccessTotal(Integer successTotal) {
		this.successTotal = successTotal;
	}
	public int getFailureTotal() {
		return failureTotal;
	}
	public void setFailureTotal(Integer failureTotal) {
		this.failureTotal = failureTotal;
	}
	public float getSuccessPercent() {
		return successPercent;
	}
	public void setSuccessPercent(float successPercent) {
		this.successPercent = successPercent;
	}
	public String getSuccessPercentString() {
		return successPercentString;
	}
	public void setSuccessPercentString(String successPercentString) {
		this.successPercentString = successPercentString;
	}
	public Long getMostRecentDate() {
		return mostRecentDate;
	}
	public void setMostRecentDate(Long mostRecentDate) {
		this.mostRecentDate = mostRecentDate;
	}
	public String getMostRecentDateString() {
		return mostRecentDateString;
	}
	public void setMostRecentDateString(String mostRecentDateString) {
		this.mostRecentDateString = mostRecentDateString;
	}
	public Long getMostRecentSuccessDate() {
		return mostRecentSuccessDate;
	}
	public void setMostRecentSuccessDate(Long mostRecentSuccessDate) {
		this.mostRecentSuccessDate = mostRecentSuccessDate;
	}
	public String getMostRecentSuccessDateString() {
		return mostRecentSuccessDateString;
	}
	public void setMostRecentSuccessDateString(String mostRecentSuccessDateString) {
		this.mostRecentSuccessDateString = mostRecentSuccessDateString;
	}
	public Long getFirstDate() {
		return firstDate;
	}
	public void setFirstDate(Long firstDate) {
		this.firstDate = firstDate;
	}
	public String getFirstDateString() {
		return firstDateString;
	}
	public void setFirstDateString(String firstDateString) {
		this.firstDateString = firstDateString;
	}
}
