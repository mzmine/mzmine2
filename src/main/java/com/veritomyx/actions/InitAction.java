package com.veritomyx.actions;

import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

	private void preCheck() throws IllegalStateException {
		if (!isReady(action)) {
			throw new IllegalStateException();
		}
	}

	public String getJob() {
		preCheck();
		return getStringAttribute("Job");
	}

	public int getProjectId() {
		preCheck();
		return getIntAttribute("ProjectID");
	}

	public double getFunds() {
		preCheck();
		return Double.parseDouble(getStringAttribute("Funds").substring(1));
	}

	public HashMap<String, Double> getRTOs() {
		preCheck();

		HashMap<String, Double> RTOs = new HashMap<>();
		JSONArray rtos = (JSONArray) responseObject.get("RTOs");
		for (int r = 0; r < rtos.size(); r++) {
			JSONObject tmp = (JSONObject) rtos.get(r);
			String key = tmp.get("RTO").toString();
			RTOs.put(
					key,
					Double.parseDouble(tmp.get("EstCost").toString()
							.substring(1)));
		}

		return RTOs;
	}

	public String[] getPiVersions() {
		preCheck();
		return getStringArrayAttribute("PI_versions");
	}
}
