package com.veritomyx.actions;

public class SftpAction extends BaseAction {
	private static final String action = "SFTP";

	private int projectID;

	public SftpAction(String versionOfApi, String user, String code, int projectID) {
		super(versionOfApi, user, code);

		this.projectID = projectID;
	}

	public String buildQuery() {
		StringBuilder builder = new StringBuilder(super.buildQuery());
		builder.append("Action=" + action + "&");
		builder.append("ID=" + projectID);

		return builder.toString();

	}

	private void preCheck() throws IllegalStateException {
		if (!isReady(action)) {
			throw new IllegalStateException("Response has not been set.");
		}
	}

	public String getHost() {
		preCheck();
		return getStringAttribute("Host");
	}

	public int getPort() {
		preCheck();
		return (int) getLongAttribute("Port");
	}

	public String getDirectory() {
		preCheck();
		return getStringAttribute("Directory");
	}

	public String getSftpUsername() {
		preCheck();
		return getStringAttribute("Login");
	}

	public String getSftpPassword() {
		preCheck();
		return getStringAttribute("Password");
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
}