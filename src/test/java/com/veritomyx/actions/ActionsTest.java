package com.veritomyx.actions;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;

import org.junit.Test;

import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.InitAction.ResponseTimeCosts;

public class ActionsTest {

	public final static String VERSIONS_RESPONSE_1 = "{\"Action\":\"PI_VERSIONS\",\"Current\":\"1.2\",\"LastUsed\":\"\",\"Count\":2,\"Versions\":[\"1.2\",\"1.0.0\"]}";

	public final static String DELETE_RESPONSE_1 = "{\"Action\":\"DELETE\",\"Job\":\"P-504.4256\",\"Datetime\":\"2016-02-03 18:35:06\"}";

	@Test
	public void test_PiVersionsAction_Query() throws ResponseFormatException {
		BaseAction action = new PiVersionsAction("3.0", "user", "password");
		assertEquals("Version=3.0&User=user&Code=password&Action=PI_VERSIONS",
				action.buildQuery());

		action.processResponse(VERSIONS_RESPONSE_1);

		PiVersionsAction temp = (PiVersionsAction) action;
		assertEquals("1.2", temp.getCurrentVersion());
		assertTrue(temp.getLastUsedVersion().isEmpty());
		assertArrayEquals(new String[] { "1.2", "1.0.0" }, temp.getVersions());
	}

	@Test
	public void test_PiVersionsAction_Error() {
		fail("not implemented");
	}

	@Test
	public void test_InitAction_Query() throws ResponseFormatException {
		BaseAction action = InitAction.create("3.0", "user", "password")
				.withMassRange(50, 100).usingProjectId(100)
				.withPiVersion("1.2").withScanCount(5, 0)
				.withNumberOfPoints(12345);

		assertEquals("Version=3.0&User=user&Code=password&Action=INIT&ID=100&PI_Version=1.2&ScanCount=5&MaxPoints=12345&MinMass=50&MaxMass=100&CalibrationCount=0",
				action.buildQuery());

		action.processResponse(InitAction.EXAMPLE_RESPONSE_1);

		InitAction temp = (InitAction) action;
		assertEquals("V-504.1551", temp.getJob());
		assertEquals(504, temp.getId());
		assertEquals(115.01, temp.getFunds(), 0);

		HashMap<String, ResponseTimeCosts> costs = temp.getEstimatedCosts();
		assertEquals(27.60, costs.get("TOF").getCost("RTO-24"), 0);
		assertEquals(36.22, costs.get("Orbitrap").getCost("RTO-24"), 0);
		assertEquals(32.59, costs.get("IonTrap").getCost("RTO-24"), 0);

		action.reset();

		action.processResponse(InitAction.EXAMPLE_RESPONSE_2);

		assertEquals("V-504.1551", temp.getJob());
		assertEquals(504, temp.getId());
		assertEquals(115.01, temp.getFunds(), 0);

		costs = temp.getEstimatedCosts();
		assertEquals(27.60, costs.get("TOF").getCost("RTO-24"), 0);
		assertEquals(36.22, costs.get("Orbitrap").getCost("RTO-24"), 0);
		assertEquals(32.59, costs.get("IonTrap").getCost("RTO-24"), 0);
		assertEquals(270.60, costs.get("TOF").getCost("RTO-0"), 0);
		assertEquals(360.22, costs.get("Orbitrap").getCost("RTO-0"), 0);
		assertEquals(320.59, costs.get("IonTrap").getCost("RTO-0"), 0);
	}

	@Test
	public void test_InitAction_Error() throws ResponseFormatException {
		BaseAction action = InitAction.create("3.0", "user", "password")
				.withMassRange(50, 100).usingProjectId(100)
				.withPiVersion("1.2").withScanCount(5, 0)
				.withNumberOfPoints(12345);

		String response = BaseAction.ERROR_CREDENTIALS.replace("ACTION", "INIT");
		action.processResponse(response);

		assertEquals("Invalid username or password - can not validate",
				action.getErrorMessage());
		assertEquals(3, action.getErrorCode());
	}

	@Test
	public void test_SftpAction_Query() throws ResponseFormatException {
		BaseAction action = new SftpAction("3.0", "user", "password", 100);
		assertEquals(action.buildQuery(),
				"Version=3.0&User=user&Code=password&Action=SFTP&ID=100");

		action.processResponse(SftpAction.EXAMPLE_RESPONSE_1);

		SftpAction temp = (SftpAction) action;
		assertEquals("peakinvestigator.veritomyx.com", temp.getHost());
		assertEquals("V504", temp.getSftpUsername());
		assertEquals("cB34lxCH0anR952gu", temp.getSftpPassword());
		assertEquals(22022, temp.getPort());
		assertEquals("/files", temp.getDirectory());
	}

	@Test
	public void test_SftpAction_Error() throws ResponseFormatException {
		BaseAction action = new SftpAction("3.0", "user", "password", 100);
		action.processResponse("{\"Action\":\"SFTP\",\"Error\":3,\"Message\":\"Invalid username or password - can not validate\",\"Location\":\"\"}");

		assertEquals("Invalid username or password - can not validate",
				action.getErrorMessage());
		assertEquals(3, action.getErrorCode());
	}

	@Test
	public void test_PrepAction_Query() throws ResponseFormatException {
		BaseAction action = new PrepAction("3.0", "user", "password", 100,
				"file.tar");
		assertEquals(
				"Version=3.0&User=user&Code=password&Action=PREP&ID=100&File=file.tar",
				action.buildQuery());

		// handle Analyzing case
		action.processResponse(PrepAction.EXAMPLE_RESPONSE_1);

		PrepAction temp = (PrepAction) action;
		assertEquals(PrepAction.Status.Analyzing, temp.getStatus());
		assertEquals("90%", temp.getPercentComplete());
		assertEquals(0, temp.getScanCount());
		assertEquals("TBD", temp.getMStype());

		// handle Ready case
		action.reset();
		action.processResponse(PrepAction.EXAMPLE_RESPONSE_2);

		temp = (PrepAction) action;
		assertEquals(PrepAction.Status.Ready, temp.getStatus());
		assertEquals("", temp.getPercentComplete(), "");
		assertEquals(3336, temp.getScanCount(), 3336);
		assertEquals("Orbitrap", temp.getMStype());
	}

	@Test
	public void test_PrepAction_Error() throws ResponseFormatException {
		BaseAction action = new PrepAction("3.0", "user", "password", 100,
				"file.tar");
		action.processResponse("{\"Action\":\"PREP\",\"Error\":3,\"Message\":\"Invalid username or password - can not validate\",\"Location\":\"\"}");

		assertEquals("Invalid username or password - can not validate",
				action.getErrorMessage());
		assertEquals(3, action.getErrorCode());
	}

	@Test
	public void test_RunAction_Query() throws ResponseFormatException {
		BaseAction action = new RunAction("3.0", "user", "password",
				"job-123", "RTO-24", "file.tar", null);
		assertEquals(
				"Version=3.0&User=user&Code=password&Action=RUN&Job=job-123&RTO=RTO-24&InputFile=file.tar",
				action.buildQuery());

		action.processResponse(RunAction.EXAMPLE_RESPONSE_1);

		RunAction temp = (RunAction) action;
		assertEquals("P-504.1463", temp.getJob());
	}

	@Test
	public void test_RunAction_Error() throws ResponseFormatException {
		BaseAction action = new RunAction("3.0", "user", "password",
				"job-123", "RTO-24", "file.tar", null);
		action.processResponse("{\"Action\":\"RUN\",\"Error\":3,\"Message\":\"Invalid username or password - can not validate\",\"Location\":\"\"}");

		assertEquals("Invalid username or password - can not validate",
				action.getErrorMessage());
		assertEquals(3, action.getErrorCode());
	}

	@Test
	public void test_StatusAction_Query() throws ResponseFormatException, ParseException {
		BaseAction action = new StatusAction("3.0", "user", "password",
				"job-123");
		assertEquals(
				"Version=3.0&User=user&Code=password&Action=STATUS&Job=job-123",
				action.buildQuery());

		action.processResponse(StatusAction.EXAMPLE_RESPONSE_1);

		StatusAction temp = (StatusAction) action;
		assertEquals("P-504.5148", temp.getJob());
		assertEquals(StatusAction.Status.Running, temp.getStatus());

		// test date
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, 1, 3, 18, 25, 9);
		assertEquals(calendar.getTime().toString(), temp.getDate().toString());

		// start of second test
		action.reset();
		action.processResponse(StatusAction.EXAMPLE_RESPONSE_2);
		temp = (StatusAction) action;
		assertEquals("P-504.5148", temp.getJob());
		assertEquals(StatusAction.Status.Done, temp.getStatus());

		// test date
		calendar.set(2016, 1, 3, 18, 31, 5);
		assertEquals(calendar.getTime().toString(), temp.getDate().toString());

		assertEquals(3, temp.getNumberOfInputScans());
		assertEquals(3, temp.getNumberOfCompleteScans());
		assertEquals(0.36, temp.getActualCost(), 0);
		assertEquals("/files/P-504.5148/P-504.5148.mass_list.tar",
				temp.getResultsFilename());
		assertEquals("/files/P-504.5148/P-504.5148.log.txt",
				temp.getLogFilename());

		// start of 3rd test
		action.reset();
		action.processResponse(StatusAction.EXAMPLE_RESPONSE_3);
		temp = (StatusAction) action;
		assertEquals(StatusAction.Status.Deleted, temp.getStatus());

		// test date
		calendar.set(2016, 1, 3, 18, 36, 5);
		assertEquals(calendar.getTime().toString(), temp.getDate().toString());
	}

	@Test
	public void test_StatusAction_Error() throws ResponseFormatException {
		BaseAction action = new StatusAction("3.0", "user", "password",
				"job-123");
		action.processResponse("{\"Action\":\"STATUS\",\"Error\":3,\"Message\":\"Invalid username or password - can not validate\",\"Location\":\"\"}");

		assertEquals("Invalid username or password - can not validate",
				action.getErrorMessage());
	}

	@Test
	public void test_DeleteAction_Query() throws ResponseFormatException, ParseException {
		BaseAction action = new DeleteAction("3.0", "user", "password",
				"job-123");
		assertEquals(
				"Version=3.0&User=user&Code=password&Action=DELETE&Job=job-123",
				action.buildQuery());

		action.processResponse(DELETE_RESPONSE_1);

		DeleteAction temp = (DeleteAction) action;
		assertEquals("P-504.4256", temp.getJob());

		// test date
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, 1, 3, 18, 35, 6);
		assertEquals(calendar.getTime().toString(), temp.getDate().toString());
	}

	@Test
	public void test_DeleteAction_Error() {
		fail("not implemented");
	}

}
