package com.veritomyx.actions;

public class PiVersionsAction extends BaseAction {
	private static final String action = "PI_VERSIONS";

	public PiVersionsAction(String versionOfApi, String user, String code) {
		super(versionOfApi, user, code);
	}

	@Override
	public String buildQuery() {
		StringBuilder builder = new StringBuilder(super.buildQuery());
		builder.append("Action=" + action);

		return builder.toString();
	}
}
