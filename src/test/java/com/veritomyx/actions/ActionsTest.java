package com.veritomyx.actions;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.veritomyx.actions.InitAction.ResponseTimeCosts;

public class ActionsTest {

	public final static String VERSIONS_RESPONSE_1 = "{\"Action\":\"PI_VERSIONS\",\"Current\":\"1.2\",\"LastUsed\":\"\",\"Count\":2,\"Versions\":[\"1.2\",\"1.0.0\"]}";

	public final static String INIT_RESPONSE_1 = "{\"Action\":\"INIT\",\"Job\":\"V-504.1461\",\"ProjectID\":504,\"Funds\":115.01,\"EstimatedCost\":{\"TOF\":{\"RTO-24\":0.6},\"Orbitrap\":{\"RTO-24\":0.85},\"Iontrap\":{\"RTO-24\":1.02}}}";
	public final static String INIT_RESPONSE_2 = "{\"Action\":\"INIT\",\"Job\":\"V-504.1461\",\"SubProjectID\":504,\"Funds\":115.01,\"EstimatedCost\":{\"TOF\":{\"RTO-24\":0.6},\"Orbitrap\":{\"RTO-24\":0.85},\"Iontrap\":{\"RTO-24\":1.02}}}";
	public final static String INIT_RESPONSE_3 = "{\"Action\":\"INIT\",\"Job\":\"V-504.1461\",\"SubProjectID\":504,\"Funds\":115.01,\"EstimatedCost\":{\"TOF\":{\"RTO-24\":0.6,\"RTO-0\":12.00},\"Orbitrap\":{\"RTO-24\":0.85, \"RTO-0\":24.00},\"Iontrap\":{\"RTO-24\":1.02,\"RTO-0\":26.00}}}";
	public final static String SFTP_RESPONSE_1 = "{\"Action\":\"SFTP\",\"Host\":\"peakinvestigator.veritomyx.com\",\"Port\":22022,\"Directory\":\"\\/files\",\"Login\":\"V504\",\"Password\":\"cB34lxCH0anR952gu\"}";

	public final static String PREP_RESPONSE_1 = "{\"Action\":\"PREP\",\"File\":\"WatersQ-TOF.tar\",\"Status\":\"Analyzing\",\"PercentComplete\":\"90%\",\"ScanCount\":0,\"MSType\":\"TBD\"}";
	public final static String PREP_RESPONSE_2 = "{\"Action\":\"PREP\",\"File\":\"Bosch_1_1.tar\",\"Status\":\"Ready\",\"PercentComplete\":\"\",\"ScanCount\":3336,\"MSType\":\"Orbitrap\"}";

	public final static String RUN_RESPONSE_1 = "{\"Action\":\"RUN\",\"Job\":\"P-504.1463\"}";

	public final static String STATUS_RESPONSE_1 = "{\"Action\":\"STATUS\",\"Job\":\"P-504.5148\",\"Status\":\"Running\",\"Datetime\":\"2016-02-03 18:25:09\"}";
	public final static String STATUS_RESPONSE_2 = "{\"Action\":\"STATUS\",\"Job\":\"P-504.5148\",\"Status\":\"Done\",\"Datetime\":\"2016-02-03 18:31:05\",\"ScansInput\":3,\"ScansComplete\":3,\"ActualCost\":0.36,\"JobLogFile\":\"\\/files\\/P-504.5148\\/P-504.5148.log.txt\",\"ResultsFile\":\"\\/files\\/P-504.5148\\/P-504.5148.mass_list.tar\"}";

	public final static String DELETE_RESPONSE_1 = "{\"Action\":\"DELETE\",\"Job\":\"P-504.4256\",\"Datetime\":\"2016-02-03 18:35:06\"}";

	@Test
	public void test_PiVersionsAction_Query() throws UnsupportedOperationException, ParseException {
		BaseAction action = new PiVersionsAction("3.0", "user", "password");
		assertEquals(action.buildQuery(),
				"Version=3.0&User=user&Code=password&Action=PI_VERSIONS");

		action.processResponse(VERSIONS_RESPONSE_1);

		PiVersionsAction temp = (PiVersionsAction) action;
		assertEquals(temp.getCurrentVersion(), "1.2");
		assertEquals(temp.getLastUsedVersion(), "");
		assertArrayEquals(temp.getVersions(), new String[] { "1.2", "1.0.0" });
	}

	@Test
	public void test_PiVersionsAction_Error() {
		fail("not implemented");
	}

	@Test
	public void test_InitAction_Query() throws UnsupportedOperationException, ParseException {
		BaseAction action = InitAction.create("3.0", "user", "password")
				.withMassRange(50, 100).usingProjectId(100)
				.withPiVersion("1.2").withScanCount(5, 0)
				.withNumberOfPoints(12345);
		assertEquals(
				action.buildQuery(),
				"Version=3.0&User=user&Code=password&Action=INIT&ID=100&PI_Version=1.2&ScanCount=5&MaxPoints=12345&MinMass=50&MaxMass=100&CalibrationCount=0");

		action.processResponse(INIT_RESPONSE_1);

		InitAction temp = (InitAction) action;
		assertEquals(temp.getJob(), "V-504.1461");
		assertEquals(temp.getProjectId(), 504);
		assertEquals(temp.getFunds(), 115.01, 0);

		HashMap<String, ResponseTimeCosts> costs = temp.getEstimatedCosts();
		assertEquals(costs.get("TOF").getCost("RTO-24"), 0.6, 0);
		assertEquals(costs.get("Orbitrap").getCost("RTO-24"), 0.85, 0);
		assertEquals(costs.get("Iontrap").getCost("RTO-24"), 1.02, 0);

		action.processResponse(INIT_RESPONSE_3);
		temp = (InitAction) action;

		costs = temp.getEstimatedCosts();
		assertEquals(costs.get("TOF").getCost("RTO-24"), 0.6, 0);
		assertEquals(costs.get("TOF").getCost("RTO-0"), 12.00, 0);
		assertEquals(costs.get("Orbitrap").getCost("RTO-24"), 0.85, 0);
		assertEquals(costs.get("Orbitrap").getCost("RTO-0"), 24.00, 0);
		assertEquals(costs.get("Iontrap").getCost("RTO-24"), 1.02, 0);
		assertEquals(costs.get("Iontrap").getCost("RTO-0"), 26.00, 0);
	}

	@Test
	public void test_InitAction_Error() {
		BaseAction action = InitAction.create("3.0", "user", "password")
				.withMassRange(50, 100).usingProjectId(100)
				.withPiVersion("1.2").withScanCount(5, 0)
				.withNumberOfPoints(12345);

		try {
			action.processResponse("{\"Action\":\"INIT\",\"Error\":3,\"Message\":\"Invalid username or password - can not validate\",\"Location\":\"\"}");
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			fail("Trying to parse HTML.");
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Problem parsing JSON.");
		}

		assertEquals(action.getErrorMessage(),
				"Invalid username or password - can not validate");
	}

	@Test
	public void test_SftpAction_Query() throws UnsupportedOperationException,
			ParseException {
		BaseAction action = new SftpAction("3.0", "user", "password", 100);
		assertEquals(action.buildQuery(),
				"Version=3.0&User=user&Code=password&Action=SFTP&ID=100");

		action.processResponse(SFTP_RESPONSE_1);

		SftpAction temp = (SftpAction) action;
		assertEquals(temp.getHost(), "peakinvestigator.veritomyx.com");
		assertEquals(temp.getSftpUsername(), "V504");
		assertEquals(temp.getSftpPassword(), "cB34lxCH0anR952gu");
		assertEquals(temp.getPort(), 22022);
		assertEquals(temp.getDirectory(), "/files");
	}

	@Test
	public void test_SftpAction_Error() {
		BaseAction action = new SftpAction("3.0", "user", "password", 100);
		try {
			action.processResponse("{\"Action\":\"SFTP\",\"Error\":3,\"Message\":\"Invalid username or password - can not validate\",\"Location\":\"\"}");
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			fail("Trying to parse HTML.");
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Problem parsing JSON.");
		}

		assertEquals(action.getErrorMessage(),
				"Invalid username or password - can not validate");
	}

	@Test
	public void test_PrepAction_Query() throws UnsupportedOperationException,
			ParseException {
		BaseAction action = new PrepAction("3.0", "user", "password", 100,
				"file.tar");
		assertEquals(action.buildQuery(),
				"Version=3.0&User=user&Code=password&Action=PREP&ID=100&File=file.tar");

		// handle Analyzing case
		action.processResponse(PREP_RESPONSE_1);

		PrepAction temp = (PrepAction) action;
		assertEquals(temp.getStatus(), PrepAction.Status.Analyzing);
		assertEquals(temp.getPercentComplete(), "90%");
		assertEquals(temp.getScanCount(), 0);
		assertEquals(temp.getMStype(), "TBD");

		// handle Ready case
		action.processResponse(PREP_RESPONSE_2);

		temp = (PrepAction) action;
		assertEquals(temp.getStatus(), PrepAction.Status.Ready);
		assertEquals(temp.getPercentComplete(), "");
		assertEquals(temp.getScanCount(), 3336);
		assertEquals(temp.getMStype(), "Orbitrap");
	}

	@Test
	public void test_PrepAction_Error() {
		BaseAction action = new PrepAction("3.0", "user", "password", 100,
				"file.tar");
		try {
			action.processResponse("{\"Action\":\"PREP\",\"Error\":3,\"Message\":\"Invalid username or password - can not validate\",\"Location\":\"\"}");
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			fail("Trying to parse HTML.");
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Problem parsing JSON.");
		}

		assertEquals(action.getErrorMessage(),
				"Invalid username or password - can not validate");
	}

	@Test
	public void test_RunAction_Query() throws UnsupportedOperationException,
			ParseException {
		BaseAction action = new RunAction("3.0", "user", "password",
				"job-123", "RTO-24", "file.tar", null);
		assertEquals(
				action.buildQuery(),
				"Version=3.0&User=user&Code=password&Action=RUN&Job=job-123&RTO=RTO-24&InputFile=file.tar");

		action.processResponse(RUN_RESPONSE_1);

		RunAction temp = (RunAction) action;
		assertEquals(temp.getJob(), "P-504.1463");
	}

	@Test
	public void test_RunAction_Error() {
		BaseAction action = new RunAction("3.0", "user", "password",
				"job-123", "RTO-24", "file.tar", null);
		try {
			action.processResponse("{\"Action\":\"RUN\",\"Error\":3,\"Message\":\"Invalid username or password - can not validate\",\"Location\":\"\"}");
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			fail("Trying to parse HTML.");
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Problem parsing JSON.");
		}

		assertEquals(action.getErrorMessage(),
				"Invalid username or password - can not validate");
	}

	@Test
	public void test_StatusAction_Query() throws UnsupportedOperationException, ParseException, java.text.ParseException {
		BaseAction action = new StatusAction("3.0", "user", "password",
				"job-123");
		assertEquals(action.buildQuery(),
				"Version=3.0&User=user&Code=password&Action=STATUS&Job=job-123");

		action.processResponse(STATUS_RESPONSE_1);

		StatusAction temp = (StatusAction) action;
		assertEquals(temp.getJob(), "P-504.5148");
		assertEquals(temp.getStatus(), StatusAction.Status.Running);

		// test date
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, 1, 3, 18, 25, 9);
		assertEquals(temp.getDate().toString(), calendar.getTime().toString());

		action.processResponse(STATUS_RESPONSE_2);
		temp = (StatusAction) action;
		assertEquals(temp.getJob(), "P-504.5148");
		assertEquals(temp.getStatus(), StatusAction.Status.Done);

		// test date
		calendar.set(2016, 1, 3, 18, 31, 5);
		assertEquals(temp.getDate().toString(), calendar.getTime().toString());

		assertEquals(temp.getNumberOfInputScans(), 3);
		assertEquals(temp.getNumberOfCompleteScans(), 3);
		assertEquals(temp.getActualCost(), 0.36, 0);
		assertEquals(temp.getResultsFilename(),
				"/files/P-504.5148/P-504.5148.mass_list.tar");
		assertEquals(temp.getLogFilename(),
				"/files/P-504.5148/P-504.5148.log.txt");
	}

	@Test
	public void test_StatusAction_Error() {
		BaseAction action = new StatusAction("3.0", "user", "password",
				"job-123");
		try {
			action.processResponse("{\"Action\":\"STATUS\",\"Error\":3,\"Message\":\"Invalid username or password - can not validate\",\"Location\":\"\"}");
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			fail("Trying to parse HTML.");
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Problem parsing JSON.");
		}

		assertEquals(action.getErrorMessage(),
				"Invalid username or password - can not validate");
	}

	@Test
	public void test_DeleteAction_Query() throws UnsupportedOperationException, ParseException, java.text.ParseException {
		BaseAction action = new DeleteAction("3.0", "user", "password",
				"job-123");
		assertEquals(action.buildQuery(),
				"Version=3.0&User=user&Code=password&Action=DELETE&Job=job-123");

		action.processResponse(DELETE_RESPONSE_1);

		DeleteAction temp = (DeleteAction) action;
		assertEquals(temp.getJob(), "P-504.4256");

		// test date
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, 1, 3, 18, 35, 6);
		assertEquals(temp.getDate().toString(), calendar.getTime().toString());
	}

	@Test
	public void test_DeleteAction_Error() {
		fail("not implemented");
	}

}
