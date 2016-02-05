package com.veritomyx.actions;

import java.util.HashMap;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class InitAction extends BaseAction {
	private static final String action = "INIT";

	private int ID;
	private String versionOfPi;
	private int scanCount;
	private int maxPoints;
	private int minMass;
	private int maxMass;
	private int calibrationCount;

	private InitAction(String versionOfApi, String user, String code, int ID, String versionOfPi, int scanCount,
			int maxPoints, int minMass, int maxMass, int calibrationCount) {
		super(versionOfApi, user, code);

		this.ID = ID;
		this.versionOfPi = versionOfPi;
		this.scanCount = scanCount;
		this.maxPoints = maxPoints;
		this.minMass = minMass;
		this.maxMass = maxMass;
		this.calibrationCount = calibrationCount;
	}

	public static InitAction create(String versionOfApi, String user, String code) {
		return new InitAction(versionOfApi, user, code, 0, null, 0, 0, 0, 0, 0);
	}

	public InitAction withPiVersion(String versionOfPi) {
		return new InitAction(this.versionOfApi, this.user, this.code, this.ID,
				versionOfPi, this.scanCount, this.maxPoints, this.minMass, this.maxMass,
				this.calibrationCount);
	}

	public InitAction withMassRange(int min, int max) {
		return new InitAction(this.versionOfApi, this.user, this.code, this.ID, this.versionOfPi,
				this.scanCount, this.maxPoints, min, max, this.calibrationCount);
	}

	public InitAction withScanCount(int scanCount, int calibrationCount) {
		return new InitAction(this.versionOfApi, this.user, this.code, this.ID, this.versionOfPi,
				scanCount, this.maxPoints, this.minMass, this.maxMass, calibrationCount);
	}

	public InitAction withNumberOfPoints(int numberOfPoints) {
		return new InitAction(this.versionOfApi, this.user, this.code, this.ID,
				this.versionOfPi, this.scanCount, numberOfPoints, this.minMass,
				this.maxMass, this.calibrationCount);
	}

	public InitAction usingProjectId(int projectID) {
		return new InitAction(this.versionOfApi, this.user, this.code, projectID,
				this.versionOfPi, this.scanCount, this.maxPoints, this.minMass,
				this.maxMass, this.calibrationCount);
	}

	public String buildQuery() {
		StringBuilder builder = new StringBuilder(super.buildQuery());

		builder.append("Action=" + action + "&");
		builder.append("ID=" + ID + "&");
		builder.append("PI_Version=" + versionOfPi + "&");
		builder.append("ScanCount=" + scanCount + "&");
		builder.append("MaxPoints=" + maxPoints + "&");
		builder.append("MinMass=" + minMass + "&");
		builder.append("MaxMass=" + maxMass + "&");
		builder.append("CalibrationCount=" + calibrationCount);

		return builder.toString();
	}

	private void preCheck() throws IllegalStateException {
		if (!isReady(action)) {
			throw new IllegalStateException();
		}
	}

	public String getJob() {
		preCheck();
		return getStringAttribute("Job");
	}

	public long getProjectId() {
		preCheck();
		return getLongAttribute("ProjectID");
	}

	public double getFunds() {
		preCheck();
		return getDoubleAttribute("Funds");
	}

	public HashMap<String, ResponseTimeCosts> getEstimatedCosts() {
		preCheck();

		HashMap<String, ResponseTimeCosts> estimatedCosts = new HashMap<>();
		JSONObject RTOs = (JSONObject) responseObject.get("EstimatedCost");
		for (Object instrument : RTOs.keySet()) {
			ResponseTimeCosts costs = new ResponseTimeCosts();
			JSONObject costsJSON_Object = (JSONObject) RTOs.get(instrument);
			for (Object RTO : costsJSON_Object.keySet()) {
				String stringRTO = (String) RTO;
				costs.put(stringRTO, (Double) costsJSON_Object.get(stringRTO));
			}
			estimatedCosts.put((String) instrument, costs);
		}

		return estimatedCosts;
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

	public class ResponseTimeCosts extends HashMap<String, Double> {

		private static final long serialVersionUID = 1L;

		public String[] getRTOs() {
			Set<String> keys = keySet();
			return keys.toArray(new String[keys.size()]);
		}

		public double getCost(String responseTimeObjective) {
			return get(responseTimeObjective);
		}
	}
}
