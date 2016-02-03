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

	private void preCheck() throws IllegalStateException {
		if (!isReady(action)) {
			throw new IllegalStateException("Response has not been set.");
		}
	}

	public String getCurrentVersion() {
		preCheck();
		return getStringAttribute("Current");
	}

	public String getLastUsedVersion() {
		preCheck();
		return getStringAttribute("LastUsed");
	}

	public String[] getVersions() throws IllegalStateException {
		preCheck();
		return getStringArrayAttribute("Versions");
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
