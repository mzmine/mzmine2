package com.veritomyx.actions;

public abstract class BaseAction {
	private String versionOfApi = null;
	private String user = null;
	private String code = null;

	BaseAction(String versionOfApi, String user, String code) {
		this.versionOfApi = versionOfApi;
		this.user = user;
		this.code = code;
	}

	public String buildQuery() {
		StringBuilder builder = new StringBuilder();

		builder.append("Version=" + versionOfApi + "&");
		builder.append("User=" + user + "&");
		builder.append("Code=" + code + "&");

		return builder.toString();
	}

}
