package com.veritomyx.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class InitAction extends BaseAction {
	private static final String action = "INIT";

	public final static String EXAMPLE_RESPONSE_1 = "{\"Action\":\"INIT\",\"Job\":\"V-504.1461\",\"ProjectID\":504,\"Funds\":115.01,\"EstimatedCost\":{\"TOF\":{\"RTO-24\":0.6},\"Orbitrap\":{\"RTO-24\":0.85},\"Iontrap\":{\"RTO-24\":1.02}}}";
	public final static String EXAMPLE_RESPONSE_2 = "{\"Action\":\"INIT\",\"Job\":\"V-504.1461\",\"SubProjectID\":504,\"Funds\":115.01,\"EstimatedCost\":{\"TOF\":{\"RTO-24\":0.6},\"Orbitrap\":{\"RTO-24\":0.85},\"Iontrap\":{\"RTO-24\":1.02}}}";
	public final static String EXAMPLE_RESPONSE_3 = "{\"Action\":\"INIT\",\"Job\":\"V-504.1461\",\"SubProjectID\":504,\"Funds\":115.01,\"EstimatedCost\":{\"TOF\":{\"RTO-24\":0.6,\"RTO-0\":12.00},\"Orbitrap\":{\"RTO-24\":0.85, \"RTO-0\":24.00},\"Iontrap\":{\"RTO-24\":1.02,\"RTO-0\":26.00}}}";

	private int ID;
	private String versionOfPi;
	private int scanCount;
	private int maxPoints;
	private int minMass;
	private int maxMass;
	private int calibrationCount;

	private HashMap<String, ResponseTimeCosts> estimatedCosts = null;

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

	public void reset() {
		super.reset();
		estimatedCosts = null;
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

		if (estimatedCosts != null) {
			return estimatedCosts;
		}

		estimatedCosts = new HashMap<>();
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

	/**
	 * Convenience function to get the Response Time Objectives from the
	 * Estimated Costs. Assumes that each ResponseTimeCosts value have exactly
	 * the same number and name of RTO.
	 * 
	 * @return List of strings corresponding to RTOs (e.g. "RTO-24", "RTO-0",
	 *         etc.)
	 */
	public String[] getResponseTimeObjectives() {
		ArrayList<ResponseTimeCosts> costs = new ArrayList<>(
				getEstimatedCosts().values());
		if (costs.size() == 0) {
			return new String[] {};
		}

		Set<String> RTOs = costs.get(0).keySet();
		return RTOs.toArray(new String[RTOs.size()]);
	}

	/**
	 * Convenience function to get the MStypes from the Estimated Costs.
	 * 
	 * @return List of type of mass spec (e.g. TOF, Iontrap, Orbitrap)
	 */
	public String[] getMSTypes() {
		Set<String> keys = getEstimatedCosts().keySet();
		return keys.toArray(new String[keys.size()]);
	}

	/**
	 * Convenience function to get the maximum potential cost from Estimated
	 * Costs for a given Response Time Objective.
	 * 
	 * @param RTO
	 *            Desired Response Time Objective (e.g. RTO-24)
	 * @return Maximum of costs across all MS types for given RTO
	 */
	public double getMaxPotentialCost(String RTO) {
		double maxCost = 0;
		for (ResponseTimeCosts costs : getEstimatedCosts().values()) {
			double cost = costs.getCost(RTO);
			if (maxCost > cost) {
				maxCost = cost;
			}
		}

		return maxCost;
	}

	@Override
	public String getErrorMessage() {
		preCheck();
		return super.getErrorMessage();
	}

	@Override
	public long getErrorCode() {
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
