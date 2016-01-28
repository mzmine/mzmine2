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
}
