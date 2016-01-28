package com.veritomyx.actions;

import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class BaseAction {
	private String versionOfApi = null;
	private String user = null;
	private String code = null;

	protected JSONObject responseObject = null;

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

	public void processResponse(String response)
			throws UnsupportedOperationException, ParseException {
		if (response.startsWith("<")) {
			throw new UnsupportedOperationException(
					"Server response appears to be HTML/XML.");
		}

		JSONParser parser = new JSONParser();
		responseObject = (JSONObject) parser.parse(response);
	}

	protected boolean isReady(String action) throws IllegalStateException {
		if (responseObject == null) {
			return false;
		}

		if (!getStringAttribute("Action").equals(action)) {
			throw new IllegalStateException("Response is from another action: "
					+ getStringAttribute("Action"));
		}

		return true;
	}

	public boolean hasError() {
		if (responseObject == null) {
			throw new IllegalStateException();
		}

		return responseObject.containsKey("Error");
	}

	public String getErrorMessage() {
		return getStringAttribute("Message");
	}

	public String getStringAttribute(String attribute) {
		return (String) responseObject.get(attribute);
	}

	public int getIntAttribute(String attribute) {
		return Integer.parseInt(getStringAttribute(attribute));
	}

	public String[] getStringArrayAttribute(String attribute) {
		JSONArray array = (JSONArray) responseObject.get(attribute);
		if (array == null) {
			return null;
		}

		String[] retval = new String[array.size()];
		for (int p = 0; p < array.size(); p++) {
			retval[p] = (String) array.get(p);
		}

		return retval;
	}

}
