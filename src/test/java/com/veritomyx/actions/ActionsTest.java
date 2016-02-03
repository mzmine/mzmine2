package com.veritomyx.actions;

import static org.junit.Assert.*;

import org.json.simple.parser.ParseException;
import org.junit.Test;

public class ActionsTest {

	public final static String INIT_RESPONSE_1 = "{\"Action\":\"INIT\",\"Job\":\"C-1022.1449\",\"ProjectID\":1022,\"Funds\":\"$9869.22\",\"RTOs\":[{\"RTO\":\"RTO-0\",\"EstCost\":\"$187.74\"},{\"RTO\":\"RTO-24\",\"EstCost\":\"$68.20\"}],\"PI_Versions\":[\"1.0.0\",\"1.2\"]}";

	public final static String SFTP_RESPONSE_1 = "{\"Action\":\"SFTP\",\"Host\":\"peakinvestigator.veritomyx.com\",\"Port\":22022,\"Directory\":\"\\/files\",\"Login\":\"joe\",\"Password\":\"*****\"}";

	public final static String PREP_RESPONSE_1 = "{\"Action\":\"PREP\",\"File\":\"C-1022.1449.scans.tar\",\"Status\":\"Analyzing\",\"ScanCount\":0,\"MSType\":\"TBD\"}";
	public final static String PREP_RESPONSE_2 = "{\"Action\":\"PREP\",\"File\":\"C-1022.1449.scans.tar\",\"Status\":\"Ready\",\"ScanCount\":341,\"MSType\":\"Orbitrap\"}";

	public final static String RUN_RESPONSE_1 = "{\"Action\":\"RUN\",\"Job\":\"C-1022.1449\"}";

	public final static String STATUS_RESPONSE_1 = "{\"Action\":\"STATUS\",\"Job\":\"C-1022.1432\",\"Status\":\"Done\",\"Datetime\":\"2016-01-26 18:48:45\",\"ScansInput\":341,\"ScansComplete\":341,\"ActualCost\":\"$4.61\",\"JobLogFile\":\"\\/files\\/C-1022.1432\\/C-1022.1432.log.txt\",\"ResultsFile\":\"\\/files\\/C-1022.1432\\/C-1022.1432.mass_list.tar\"}";

	@Test
	public void test_PiVersionsAction_Query() {
		BaseAction action = new PiVersionsAction("2.14", "user", "password");
		assertEquals(action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=PI_VERSIONS");
	}

	@Test
	public void test_PiVersionsAction_Error() {
		fail("not implemented");
	}

	@Test
	public void test_InitAction_Query() throws UnsupportedOperationException, ParseException {
		BaseAction action = new InitAction("2.14", "user", "password", 100, 5,
				0, 50, 100);
		assertEquals(
				action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=INIT&ID=100&ScanCount=5&CalibrationCount=0&MinMass=50&MaxMass=100");

		action.processResponse(INIT_RESPONSE_1);

		InitAction temp = (InitAction) action;
		assertEquals(temp.getJob(), "C-1022.1449");
		assertEquals(temp.getProjectId(), 1022);
		assertEquals(temp.getFunds(), 9869.22, 0);
		assertArrayEquals(temp.getPiVersions(), new String[] { "1.0.0", "1.2" });
		assertEquals(temp.getRTOs().size(), 2);
	}

	@Test
	public void test_InitAction_Error() {
		BaseAction action = new InitAction("2.14", "user", "password", 100, 5,
				0, 50, 100);
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
		BaseAction action = new SftpAction("2.14", "user", "password", 100);
		assertEquals(action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=SFTP&ID=100");

		action.processResponse(SFTP_RESPONSE_1);

		SftpAction temp = (SftpAction) action;
		assertEquals(temp.getHost(), "peakinvestigator.veritomyx.com");
		assertEquals(temp.getSftpUsername(), "joe");
		assertEquals(temp.getSftpPassword(), "*****");
		assertEquals(temp.getPort(), 22022);
	}

	@Test
	public void test_SftpAction_Error() {
		BaseAction action = new SftpAction("2.14", "user", "password", 100);
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
		BaseAction action = new PrepAction("2.14", "user", "password", 100,
				"file.tar");
		assertEquals(action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=PREP&ID=100&File=file.tar");

		// handle Analyzing case
		action.processResponse(PREP_RESPONSE_1);

		PrepAction temp = (PrepAction) action;
		assertEquals(temp.getStatus(), PrepAction.Status.Analyzing);
		assertEquals(temp.getScanCount(), 0);

		// handle Ready case
		action.processResponse(PREP_RESPONSE_2);

		temp = (PrepAction) action;
		assertEquals(temp.getStatus(), PrepAction.Status.Ready);
		assertEquals(temp.getScanCount(), 341);
		assertEquals(temp.getMStype(), "Orbitrap");
	}

	@Test
	public void test_PrepAction_Error() {
		BaseAction action = new PrepAction("2.14", "user", "password", 100,
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
		BaseAction action = new RunAction("2.14", "user", "password",
				"job-123", "file.tar", null, "RTO-24", "1.2");
		assertEquals(
				action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=RUN&Job=job-123&InputFile=file.tar&RTO=RTO-24&PIVersion=1.2");

		action.processResponse(RUN_RESPONSE_1);

		RunAction temp = (RunAction) action;
		assertEquals(temp.getJob(), "C-1022.1449");
	}

	@Test
	public void test_RunAction_Error() {
		BaseAction action = new RunAction("2.14", "user", "password",
				"job-123", "file.tar", null, "RTO-24", "1.2");
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
	public void test_StatusAction_Query() throws UnsupportedOperationException, ParseException {
		BaseAction action = new StatusAction("2.14", "user", "password",
				"job-123");
		assertEquals(action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=STATUS&Job=job-123");

		action.processResponse(STATUS_RESPONSE_1);

		StatusAction temp = (StatusAction) action;
		assertEquals(temp.getJob(), "C-1022.1432");
		assertEquals(temp.getStatus(), StatusAction.Status.Done);
		assertEquals(temp.getNumberOfInputScans(), 341);
		assertEquals(temp.getNumberOfCompleteScans(), 341);
		assertEquals(temp.getActualCost(), "$4.61");
		assertEquals(temp.getResultsFilename(),
				"/files/C-1022.1432/C-1022.1432.mass_list.tar");
		assertEquals(temp.getLogFilename(),
				"/files/C-1022.1432/C-1022.1432.log.txt");
	}

	@Test
	public void test_StatusAction_Error() {
		BaseAction action = new StatusAction("2.14", "user", "password",
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
	public void test_DeleteAction_Query() {
		BaseAction action = new DeleteAction("2.14", "user", "password",
				"job-123");
		assertEquals(action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=DELETE&Job=job-123");
	}

	@Test
	public void test_DeleteAction_Error() {
		fail("not implemented");
	}

}
