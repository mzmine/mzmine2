package com.veritomyx.actions;

public class PrepAction extends BaseAction {
	private final static String action = "PREP";
	
	private int projectID;
	private String filename;
	
	public PrepAction(String versionOfApi, String user, String code, int projectID, String filename) {
		super(versionOfApi, user, code);
		
		this.projectID = projectID;
		this.filename = filename;
		
	}
	
	public String buildQuery() {
		StringBuilder builder = new StringBuilder(super.buildQuery());
		builder.append("Action=" + action + "&");
		builder.append("ID=" + projectID + "&");
		builder.append("File=" + filename);

		return builder.toString();

	}

	private void preCheck() throws IllegalStateException {
		if (!isReady(action)) {
			throw new IllegalStateException("Response has not been set.");
		}
	}

	public String getFilename() {
		preCheck();
		return getStringAttribute("File");
	}

	public Status getStatus() {
		preCheck();
		return Status.valueOf(getStringAttribute("Status"));
	}

	public int getScanCount() {
		preCheck();
		return (int) getLongAttribute("ScanCount");
	}

	public String getPercentComplete() {
		preCheck();
		return getStringAttribute("PercentComplete");
	};

	public String getMStype() {
		preCheck();
		return getStringAttribute("MSType");
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

	public enum Status { Analyzing, Ready, Error }

}
