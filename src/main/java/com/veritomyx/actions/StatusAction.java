package com.veritomyx.actions;

import java.text.ParseException;
import java.util.Date;

public class StatusAction extends BaseAction {
	private static final String action = "STATUS";

	public final static String EXAMPLE_RESPONSE_1 = "{\"Action\":\"STATUS\",\"Job\":\"P-504.5148\",\"Status\":\"Running\",\"Datetime\":\"2016-02-03 18:25:09\"}";
	public final static String EXAMPLE_RESPONSE_2 = "{\"Action\":\"STATUS\",\"Job\":\"P-504.5148\",\"Status\":\"Done\",\"Datetime\":\"2016-02-03 18:31:05\",\"ScansInput\":3,\"ScansComplete\":3,\"ActualCost\":0.36,\"JobLogFile\":\"\\/files\\/P-504.5148\\/P-504.5148.log.txt\",\"ResultsFile\":\"\\/files\\/P-504.5148\\/P-504.5148.mass_list.tar\"}";
	public final static String EXAMPLE_RESPONSE_3 = "{\"Action\":\"STATUS\",\"Job\":\"P-504.1463\",\"Status\":\"Deleted\",\"Datetime\":\"2016-02-03 18:36:05\"}";

	public final static String PREPARING_STRING = "Currently doing a pre-run sanity check. Please try again later.";
	public final static String RUNNING_STRING = "Remote job not complete. Please try again later.";
	public final static String DONE_STRING = "Remote job complete. Results will be downloaded and all data deleted from the servers.";
	public final static String DELETED_STRING = "Remote job has been deleted.";

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
		return (int) getLongAttribute("ScansInput");
	}

	public int getNumberOfCompleteScans() {
		preCheck();
		return (int) getLongAttribute("ScansComplete");
	}

	public double getActualCost() {
		preCheck();
		return getDoubleAttribute("ActualCost");
	}

	public String getLogFilename() {
		preCheck();
		return getStringAttribute("JobLogFile");
	}

	public String getResultsFilename() {
		preCheck();
		return getStringAttribute("ResultsFile");
	}

	public String getMessage() {
		switch (getStatus()) {
		case Preparing:
			return PREPARING_STRING;
		case Running:
			return RUNNING_STRING;
		case Done:
			return DONE_STRING;
		case Deleted:
			return DELETED_STRING;
		default:
			throw new IllegalStateException(
					"Unknown status returned from server.");
		}
	}

	@Override
	public String getErrorMessage() {
		preCheck();
		return super.getErrorMessage();
	}

	@Override
	public long getErrorCode() {
		preCheck();
		return super.getErrorCode();
	}

	public enum Status { Preparing, Running, Done, Deleted };
}
