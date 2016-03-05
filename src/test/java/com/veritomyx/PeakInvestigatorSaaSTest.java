package com.veritomyx;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import static org.mockito.Mockito.*;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.veritomyx.actions.BaseAction;
import com.veritomyx.actions.SftpAction;

public class PeakInvestigatorSaaSTest {

	private String server = null;
	private String username = null;
	private String password = null;
	private Integer port = null;
	private String filename = null;
	private boolean isInitialized = false;

	private static int TIMEOUT = 500; // milliseconds

	@Rule public ExpectedException thrown = ExpectedException.none();
	@Rule public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() {
		server = System.getProperty("server");
		username = System.getProperty("user");
		password = System.getProperty("password");
		try {
			port = Integer.parseInt(System.getProperty("port"));
		} catch (NumberFormatException e) {
			// don't do anything;
		}

		filename = System.getProperty("filename");

		isInitialized = server != null && username != null && password != null
				&& port != null && filename != null;
	}

	@Test
	public void testExecuteAction_BadServerTimeout() throws JSchException,
			IOException {

		assumeTrue(isInitialized);

		thrown.expect(SocketTimeoutException.class);
		thrown.expectMessage("timed out");

		BaseAction action = mock(BaseAction.class);
		when(action.buildQuery()).thenReturn("Bad string");

		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(
				"unknown.veritomyx.com").withTimeout(TIMEOUT);
		service.executeAction(action);

		fail("Should not reach here.");
	}

	@Test
	public void testExecuteAction_BadServerName() throws JSchException,
			IOException {

		assumeTrue(isInitialized);

		thrown.expect(UnknownHostException.class);
		thrown.expectMessage("unknown host");

		BaseAction action = mock(BaseAction.class);
		when(action.buildQuery()).thenReturn("Bad string");

		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(
				"http://unknown.veritomyx.com").withTimeout(TIMEOUT);
		service.executeAction(action);

		fail("Should not reach here.");
	}

	@Test
	public void testInitializeSftpSession_OK() throws JSchException {
		assumeTrue(isInitialized);

		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server);
		service.initializeSftpSession(server, username, password, port);

		assertTrue(service.isConnectedForSftp());
	}

	@Test
	public void testInitializeSftpSession_BadUsername() throws JSchException {
		assumeTrue(isInitialized);

		thrown.expect(JSchException.class);
		thrown.expectMessage("Auth fail");

		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server);
		service.initializeSftpSession(server, "joe", password, port);

		fail("Should not reach here.");
	}

	@Test
	public void testInitializeSftpSession_BadPassword() throws JSchException {
		assumeTrue(isInitialized);

		thrown.expect(JSchException.class);
		thrown.expectMessage("Auth fail");

		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server);
		service.initializeSftpSession(server, username, "code", port);

		fail("Should not reach here.");
	}

	@Test
	public void testInitializeSftpSession_BadPort() throws JSchException {
		assumeTrue(isInitialized);

		thrown.expect(JSchException.class);
		thrown.expectMessage("connection is closed");

		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server)
				.withTimeout(TIMEOUT);
		service.initializeSftpSession(server, username, password, 80);

		fail("Should not reach here.");
	}

	@Test
	public void testInitializeSftpSession_BadServer() throws JSchException {
		assumeTrue(isInitialized);

		thrown.expect(JSchException.class);
		thrown.expectMessage("timeout");

		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS("unknown.veritomyx.com")
				.withTimeout(TIMEOUT);
		service.initializeSftpSession("unknown.com", username, password, port);

		fail("Should not reach here.");
	}

	@Test
	public void testIntializeSftpSession_NotStarted() throws JSchException {
		assumeTrue(isInitialized);

		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server);
		assertFalse(service.isConnectedForSftp());
	}

	private SftpAction mockSftpAction() {
		SftpAction action = mock(SftpAction.class);
		when(action.getHost()).thenReturn(server);
		when(action.getSftpUsername()).thenReturn(username);
		when(action.getSftpPassword()).thenReturn(password);
		when(action.getDirectory()).thenReturn("");
		when(action.getPort()).thenReturn(port);

		return action;
	}

	@Test
	public void testPutFile() throws JSchException, SftpException {
		assumeTrue(isInitialized);

		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server);
		SftpProgressMonitor sftpMonitor = new HeadlessProgressMonitor();
		File localFile = new File(filename);

		service.putFile(mockSftpAction(), filename, localFile.getName(),
				sftpMonitor);

		HeadlessProgressMonitor monitor = (HeadlessProgressMonitor) sftpMonitor;
		assertEquals(localFile.length(), monitor.transferredSize);
		assertEquals(localFile.length(), monitor.max);
		assertEquals(SftpProgressMonitor.PUT, monitor.direction);
		assertFalse(service.isConnectedForSftp());
	}

	@Test
	public void testGetFile() throws JSchException, SftpException, IOException {
		assumeTrue(isInitialized);

		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server);
		SftpProgressMonitor sftpMonitor = new HeadlessProgressMonitor();
		File localFile = new File(filename);
		File remoteFile = tempFolder.newFile(localFile.getName());

		service.getFile(mockSftpAction(), localFile.getName(),
				remoteFile.getAbsolutePath(), sftpMonitor);

		HeadlessProgressMonitor monitor = (HeadlessProgressMonitor) sftpMonitor;
		assertEquals(localFile.length(), monitor.transferredSize);
		assertEquals(localFile.length(), monitor.max);
		assertEquals(SftpProgressMonitor.GET, monitor.direction);
		assertFalse(service.isConnectedForSftp());
	}

	@SuppressWarnings("unused")
	private class HeadlessProgressMonitor implements SftpProgressMonitor {

		int direction;
		String src;
		String dst;
		long max;
		ArrayList<Long> counts = new ArrayList<>();
		long transferredSize = 0;

		@Override
		public void init(int direction, String src, String dst, long max) {
			this.direction = direction;
			this.src = src;
			this.dst = dst;
			this.max = max;
		}

		@Override
		public boolean count(long count) {
			transferredSize += count;
			counts.add(count);
			return true;
		}

		@Override
		public void end() {
			counts.trimToSize();
		}

	}
}
