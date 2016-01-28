package com.veritomyx.actions;

public class InitAction extends BaseAction {
	private static final String action = "INIT";

	private int ID;
	private int scanCount;
	private int calibrationCount;
	private int minMass;
	private int maxMass;

	public InitAction(String versionOfApi, String user, String code, int ID, int scanCount,
			int calibrationCount, int minMass, int maxMass) {
		super(versionOfApi, user, code);

		this.ID = ID;
		this.scanCount = scanCount;
		this.calibrationCount = calibrationCount;
		this.minMass = minMass;
		this.maxMass = maxMass;
	}

	public String buildQuery() {
		StringBuilder builder = new StringBuilder(super.buildQuery());

		builder.append("Action=" + action + "&");
		builder.append("ID=" + ID + "&");
		builder.append("ScanCount=" + scanCount + "&");
		builder.append("CalibrationCount=" + calibrationCount + "&");
		builder.append("MinMass=" + minMass + "&");
		builder.append("MaxMass=" + maxMass);

		return builder.toString();
	}
	
}
