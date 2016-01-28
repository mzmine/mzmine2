package com.veritomyx.actions;

public class SftpAction extends BaseAction {
	private static final String action = "SFTP";

	private int projectID;

	SftpAction(String versionOfApi, String user, String code, int projectID) {
		super(versionOfApi, user, code);

		this.projectID = projectID;
	}

	String buildQuery() {
		StringBuilder builder = new StringBuilder(super.buildQuery());
		builder.append("Action=" + action + "&");
		builder.append("ID=" + projectID);

		return builder.toString();

	}
}