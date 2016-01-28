package com.veritomyx.actions;

public class RunAction extends BaseAction {
	private final static String action = "RUN";

	private String job;
	private String inputFilename;
	private String calibrationFilename;
	private String RTO;
	private String PiVersion;

	/** Create object for RUN API call.
	 * 
	 * @param versionOfApi
	 * @param user
	 * @param code
	 * @param job
	 * @param inputFilename
	 * @param calibrationFilename
	 * @param RTO
	 * @param PiVersion
	 */
	public RunAction(String versionOfApi, String user, String code, String job,
			String inputFilename, String calibrationFilename, String RTO, String PiVersion) {
		super(versionOfApi, user, code);

		this.job = job;
		this.inputFilename = inputFilename;
		this.calibrationFilename = calibrationFilename;
		this.RTO = RTO;
		this.PiVersion = PiVersion;

	}

	public String buildQuery() {
		StringBuilder builder = new StringBuilder(super.buildQuery());
		builder.append("Action=" + action + "&");
		builder.append("Job=" + job + "&");
		builder.append("InputFile=" + inputFilename + "&");

		if (calibrationFilename != null) {
			builder.append("CalibrationFile=" + calibrationFilename + "&");
		}

		builder.append("RTO=" + RTO + "&");
		builder.append("PIVersion=" + PiVersion);

		return builder.toString();

	}
}
