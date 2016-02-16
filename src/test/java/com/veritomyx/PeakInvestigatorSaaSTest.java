package com.veritomyx;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.veritomyx.actions.SftpAction;

public class PeakInvestigatorSaaSTest {

	private String server = null;
	private String username = null;
	private String password = null;
	private int port = 0;
	private String filename = null;

	@Before
	public void setUp() {
		server = System.getProperty("server");
		username = System.getProperty("user");
		password = System.getProperty("password");
		port = Integer.parseInt(System.getProperty("port"));
		filename = System.getProperty("filename");
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

	@Test
	public void testPutFile() throws JSchException, SftpException {
		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server);

		SftpAction action = mock(SftpAction.class);
		when(action.getHost()).thenReturn(server);
		when(action.getSftpUsername()).thenReturn(username);
		when(action.getSftpPassword()).thenReturn(password);
		when(action.getDirectory()).thenReturn("");
		when(action.getPort()).thenReturn(port);

		SftpProgressMonitor sftpMonitor = new HeadlessProgressMonitor();
		File localFile = new File(filename);
		service.putFile(action, filename, localFile.getName(), sftpMonitor);

		HeadlessProgressMonitor monitor = (HeadlessProgressMonitor) sftpMonitor;
		assertEquals(localFile.length(), monitor.transferredSize);
		assertEquals(localFile.length(), monitor.max);
		assertEquals(SftpProgressMonitor.PUT, monitor.direction);
	}

	@Test
	public void testGetFile() throws JSchException, SftpException {
		PeakInvestigatorSaaS service = new PeakInvestigatorSaaS(server);

		SftpAction action = mock(SftpAction.class);
		when(action.getHost()).thenReturn(server);
		when(action.getSftpUsername()).thenReturn(username);
		when(action.getSftpPassword()).thenReturn(password);
		when(action.getDirectory()).thenReturn("");
		when(action.getPort()).thenReturn(port);

		SftpProgressMonitor sftpMonitor = new HeadlessProgressMonitor();
		File localFile = new File(filename);
		service.getFile(action, localFile.getName(), "temp.txt", sftpMonitor);

		HeadlessProgressMonitor monitor = (HeadlessProgressMonitor) sftpMonitor;
		assertEquals(localFile.length(), monitor.transferredSize);
		assertEquals(localFile.length(), monitor.max);
		assertEquals(SftpProgressMonitor.GET, monitor.direction);
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
