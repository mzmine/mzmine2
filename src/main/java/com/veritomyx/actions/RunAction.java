package com.veritomyx.actions;

public class RunAction extends BaseAction {
	private final static String action = "RUN";

	public final static String EXAMPLE_RESPONSE_1 = "{\"Action\":\"RUN\",\"Job\":\"P-504.1463\"}";

	private String job;
	private String RTO;
	private String inputFilename;
	private String calibrationFilename;


	/** Create object for RUN API call.
	 * 
	 * @param versionOfApi
	 * @param user
	 * @param code
	 * @param job
	 * @param RTO
	 * @param inputFilename
	 * @param calibrationFilename
	 */
	public RunAction(String versionOfApi, String user, String code, String job, String RTO,
			String inputFilename, String calibrationFilename) {
		super(versionOfApi, user, code);

		this.job = job;
		this.RTO = RTO;
		this.inputFilename = inputFilename;
		this.calibrationFilename = calibrationFilename;
	}

	public String buildQuery() {
		StringBuilder builder = new StringBuilder(super.buildQuery());
		builder.append("Action=" + action + "&");
		builder.append("Job=" + job + "&");
		builder.append("RTO=" + RTO + "&");
		builder.append("InputFile=" + inputFilename);

		if (calibrationFilename != null) {
			builder.append("&CalibrationFile=" + calibrationFilename + "&");
		}

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
