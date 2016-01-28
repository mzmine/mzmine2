package com.veritomyx.actions;

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
}
