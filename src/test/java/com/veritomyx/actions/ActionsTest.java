package com.veritomyx.actions;

import static org.junit.Assert.*;

import org.json.simple.parser.ParseException;
import org.junit.Test;

public class ActionsTest {

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
	public void test_InitAction_Query() {
		BaseAction action = new InitAction("2.14", "user", "password", 100, 5,
				0, 50, 100);
		assertEquals(
				action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=INIT&ID=100&ScanCount=5&CalibrationCount=0&MinMass=50&MaxMass=100");
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
	public void test_SftpAction_Query() {
		BaseAction action = new SftpAction("2.14", "user", "password", 100);
		assertEquals(action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=SFTP&ID=100");

		try {
			action.processResponse("{\"Action\":\"SFTP\",\"Host\":\"peakinvestigator.veritomyx.com\",\"Port\":22022,\"Directory\":\"/files\",\"Login\":\"joe\",\"Password\":\"*****\"}");
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		SftpAction temp = (SftpAction) action;
		assertEquals(temp.getHost(), "peakinvestigator.veritomyx.com");
		assertEquals(temp.getSftpUsername(), "joe");
		assertEquals(temp.getSftpPassword(), "*****");
		assertEquals(temp.getPort(), 22022);
	}
// {"Action":"SFTP","Host":"peakinvestigator.veritomyx.com","Port":22022,"Directory":"\/files","Login":"V1022","Password":"2HgNWqnevbP1mi7O"}
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
	public void test_PrepAction_Query() {
		BaseAction action = new PrepAction("2.14", "user", "password", 100,
				"file.tar");
		assertEquals(action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=PREP&ID=100&File=file.tar");
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
	public void test_RunAction_Query() {
		BaseAction action = new RunAction("2.14", "user", "password",
				"job-123", "file.tar", null, "RTO-24", "1.2");
		assertEquals(
				action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=RUN&Job=job-123&InputFile=file.tar&RTO=RTO-24&PIVersion=1.2");
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
	public void test_StatusAction_Query() {
		BaseAction action = new StatusAction("2.14", "user", "password",
				"job-123");
		assertEquals(action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=STATUS&Job=job-123");
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
