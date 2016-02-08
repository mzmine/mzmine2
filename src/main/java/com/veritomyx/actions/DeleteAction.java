package com.veritomyx.actions;

import java.text.ParseException;
import java.util.Date;

public class DeleteAction extends BaseAction {
	private static final String action = "DELETE";

	private String jobID;

	public DeleteAction(String versionOfApi, String user, String code, String jobID) {
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

	public Date getDate() throws ParseException {
		preCheck();
		return getDateAttribute("Datetime");
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
}
