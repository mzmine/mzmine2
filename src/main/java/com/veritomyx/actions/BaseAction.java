package com.veritomyx.actions;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class BaseAction {
	private static final String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss";

	/** Test Strings */
	public static final String API_SOURCE = "<html><head>\n"
			+ "<!--\n"
			+ "// ===================================================================\n"
			+ "// Veritomyx";
	public static final String ERROR_CREDENTIALS = "{\"Action\":\"ACTION\",\"Error\":3,\"Message\":\"Invalid username or password - can not validate\",\"Location\":\"\"}";

	protected String versionOfApi = null;
	protected String user = null;
	protected String code = null;

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

	public void reset() {
		responseObject = null;
	}

	public void processResponse(String response) throws ResponseFormatException {
		if (response.startsWith("<")) {
			throw new ResponseFormatException(
					"Server response appears to be HTML/XML: " + response.substring(0, 15), this);
		}

		JSONParser parser = new JSONParser();
		try {
			responseObject = (JSONObject) parser.parse(response);
		} catch (ParseException e) {
			throw new ResponseFormatException("Problem parsing JSON: " + e.getMessage(), this);
		}
	}

	public boolean isReady(String action) throws IllegalStateException {
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

	public long getErrorCode() {
		if (responseObject.containsKey("Error")) {
			return getLongAttribute("Error");
		}

		return 0;
	}

	public String getStringAttribute(String attribute) {
		return (String) responseObject.get(attribute);
	}

	public int getIntAttribute(String attribute) {
		return (Integer) responseObject.get(attribute);
	}

	public long getLongAttribute(String attribute) {
		return (Long) responseObject.get(attribute);
	}

	public double getDoubleAttribute(String attribute) {
		return (Double) responseObject.get(attribute);
	}

	public Date getDateAttribute(String attribute) throws java.text.ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		return dateFormat.parse(getStringAttribute("Datetime"));
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

	public class ResponseFormatException extends Exception {
		private BaseAction action = null;

		public ResponseFormatException(String message, BaseAction action) {
			super(message);
			this.action = action;
		}

		public ResponseFormatException(String message, BaseAction action,
				Throwable cause) {
			super(message, cause);
			this.action = action;
		}

		public BaseAction getAction() {
			return action;
		}
	}
}
