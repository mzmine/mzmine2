package com.veritomyx.actions;

import static org.junit.Assert.*;

import org.junit.Test;

public class ActionsTest {

	@Test
	public void testPiVersionsAction() {
		BaseAction action = new PiVersionsAction("2.14", "user", "password");
		assertEquals(action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=PI_VERSIONS");
	}

	@Test
	public void testInitAction() {
		BaseAction action = new InitAction("2.14", "user", "password", 100, 5,
				0, 50, 100);
		assertEquals(
				action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=INIT&ID=100&ScanCount=5&CalibrationCount=0&MinMass=50&MaxMass=100");
	}
	
	@Test
	public void testSftpAction() {
		BaseAction action = new SftpAction("2.14", "user", "password", 100);
		assertEquals(action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=SFTP&ID=100");
	}
	
	@Test
	public void testPrepAction() {
		BaseAction action = new PrepAction("2.14", "user", "password", 100,
				"file.tar");
		assertEquals(action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=PREP&ID=100&File=file.tar");
	}
	
	@Test
	public void testRunAction() {
		BaseAction action = new RunAction("2.14", "user", "password",
				"job-123", "file.tar", null, "RTO-24", "1.2");
		assertEquals(
				action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=RUN&Job=job-123&InputFile=file.tar&RTO=RTO-24&PIVersion=1.2");
	}
	
	@Test
	public void testStatusAction() {
		BaseAction action = new StatusAction("2.14", "user", "password",
				"job-123");
		assertEquals(action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=STATUS&Job=job-123");
	}

	@Test
	public void testDeleteAction() {
		BaseAction action = new DeleteAction("2.14", "user", "password",
				"job-123");
		assertEquals(action.buildQuery(),
				"Version=2.14&User=user&Code=password&Action=DELETE&Job=job-123");
	}

}
