package com.veritomyx.actions;

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
}
