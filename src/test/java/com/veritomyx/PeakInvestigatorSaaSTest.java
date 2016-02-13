package com.veritomyx;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.jcraft.jsch.JSchException;

public class PeakInvestigatorSaaSTest {

	private String server = null;
	private String username = null;
	private String password = null;
	private int port = 0;

	@Before
	public void setUp() {
		server = System.getProperty("server");
		username = System.getProperty("user");
		password = System.getProperty("password");
		port = Integer.parseInt(System.getProperty("port"));
	}

	@Test
	public void testInitializeSftpSession_OK() throws JSchException {
		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server);
		service.initializeSftpSession(server, username, password, port);

		assertTrue(service.isConnectedForSftp());
	}

	@Test(expected = JSchException.class)
	public void testInitializeSftpSession_BadUsername() throws JSchException {
		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server);
		service.initializeSftpSession(server, "joe", password, port);

		fail("Should not reach here.");
	}

	@Test(expected = JSchException.class)
	public void testInitializeSftpSession_BadPassword() throws JSchException {
		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server);
		service.initializeSftpSession(server, username, "code", port);

		fail("Should not reach here.");
	}

	@Test(expected = JSchException.class)
	public void testInitializeSftpSession_BadPort() throws JSchException {
		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server)
				.withTimeout(1);
		service.initializeSftpSession(server, username, password, 80);

		fail("Should not reach here.");
	}

	@Test(expected = JSchException.class)
	public void testInitializeSftpSession_BadServer() throws JSchException {
		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS("unknown.com")
				.withTimeout(500);
		service.initializeSftpSession("unknown.com", username, password, port);

		fail("Should not reach here.");
	}

	@Test
	public void testIntializeSftpSession_NotStarted() throws JSchException {
		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server);
		assertFalse(service.isConnectedForSftp());
	}
}
