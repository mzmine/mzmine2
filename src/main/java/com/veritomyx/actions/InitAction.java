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

	public InitAction(String versionOfApi, String user, String code, int ID, String versionOfPi, int scanCount,
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
