package com.veritomyx.actions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatusAction extends BaseAction {
	private static final String action = "STATUS";

	private String jobID;

	public StatusAction(String versionOfApi, String user, String code, String jobID) {
		super(versionOfApi, user, code);

		this.jobID = jobID;
	}

	public String buildQuery() {
		StringBuilder builder = new StringBuilder(super.buildQuery());

		builder.append("Action=" + action + "&");
		builder.append("Job=" + jobID);

		return builder.toString();
	}

	private void preCheck() throws IllegalStateException {
		if (!isReady(action)) {
			throw new IllegalStateException("Response has not been set.");
		}
	}

	public String getJob() {
		preCheck();
		return getStringAttribute("Job");
	}

	public Status getStatus() {
		preCheck();
		return Status.valueOf(getStringAttribute("Status"));
	}

	public Date getDate() throws ParseException {
		preCheck();
		return getDateAttribute("Datetime");
	}

	public int getNumberOfInputScans() {
		preCheck();
		return getIntAttribute("ScansInput");
	}

	public int getNumberOfCompleteScans() {
		preCheck();
		return getIntAttribute("ScansComplete");
	}

	public String getActualCost() {
		preCheck();
		return getStringAttribute("ActualCost");
	}

	public String getLogFilename() {
		preCheck();
		return getStringAttribute("JobLogFile");
	}

	public String getResultsFilename() {
		preCheck();
		return getStringAttribute("ResultsFile");
	}

	@Override
	public String getErrorMessage() {
		preCheck();
		return super.getErrorMessage();
	}

	@Override
	public int getErrorCode() {
		preCheck();
		return super.getErrorCode();
	}

	public enum Status { Running, Done, Deleted };
}
