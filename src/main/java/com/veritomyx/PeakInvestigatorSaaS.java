/*
 * Copyright 2013-2014 Veritomyx Inc.
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package com.veritomyx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;
import com.veritomyx.actions.*;

/**
 * This class is used to access the Veritomyx SaaS servers
 * 
 * @author Dan Schmidt
 * @author Adam Tenderholt
 */
public class PeakInvestigatorSaaS
{
	// Required CLI version (see https://secure.veritomyx.com/interface/API.php)
	public static final String API_VERSION = "3.0";
	private static final String PAGE_ENCODING = "UTF-8";

	JSch jsch = new JSch();
	private String server = null;
	Session session = null;
	ChannelSftp channel = null;
	private int timeout = 10000; //milliseconds

	// return codes from web pages
	public  static final int W_UNDEFINED =  0;
	public  static final int W_INFO      =  1;
	public  static final int W_RUNNING   =  2;
	public  static final int W_DONE      =  3;
	public  static final int W_SFTP      =  4;
	public  static final int W_PREP      =  5;
	public  static final int W_EXCEPTION = -99;
	public  static final int W_ERROR             = -1;	// these are pulled from API.php
	public  static final int W_ERROR_API         = -2;
	public  static final int W_ERROR_LOGIN       = -3;
	public  static final int W_ERROR_PID         = -4;
	public  static final int W_ERROR_SFTP        = -5;
	public  static final int W_ERROR_INPUT       = -6;
	public  static final int W_ERROR_FILE_WRITE  = -7;
	public  static final int W_ERROR_ACTION      = -8;
	public  static final int W_ERROR_PERMISSIONS = -9;
	public  static final int W_ERROR_JOB_CMD     = -10;
	public  static final int W_ERROR_JOB_RESULTS = -11;
	public  static final int W_ERROR_RECORD      = -12;
	public  static final int W_ERROR_INSUFFICIENT_CREDIT = -13;
	public  static final int W_ERROR_VALUE_MBGT_ZERO = -14;
	public  static final int W_ERROR_JOB_NOT_FOUND = -15;
	public  static final int W_ERROR_JOB_NOT_DONE = -16;
	public  static final int W_ERROR_INVALID_MASS = -17;
	public  static final int W_ERROR_INVALID_SLA = -18;
	public  static final int W_ERROR_INVALID_PI_VERSION = -19;
	public  static final int W_ERROR_USER_NOT_FOUND = -20;
	public  static final int W_ERROR_NUM_SCAN_FILES = -21;
	public  static final int W_ERROR_CANNOT_BE_BLACK = -22;

	private Logger log;

	/**
	 * Constructor
	 * 
	 * @param reqVersion
	 * @param live
	 * @throws JSchException 
	 */
	public PeakInvestigatorSaaS(String server) throws JSchException {
		// without this we get exception in getInputStream
		System.setProperty("java.net.preferIPv4Stack", "true");
		if (server.startsWith("https://")) {
			this.server = server.substring(8);
		} else {
			this.server = server;
		}

		log = Logger.getLogger(this.getClass().getName());
		log.info(this.getClass().getName());

		setupHostKeyRepository();
	}

	public PeakInvestigatorSaaS withTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	protected void setupHostKeyRepository() throws JSchException {
		InputStream stream = getClass().getResourceAsStream(
				"/com/veritomyx/sftp-servers.txt");
		if (stream == null) {
			throw new JSchException("Unable to locate hosts file.");
		}

		jsch.setKnownHosts(stream);
	}

	private void error(String message) {
		System.err.println(message);
	}

	private HttpURLConnection buildConnection(URL url) throws IOException {

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setUseCaches(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Language", "en-US");

		// give it 4 minutes to respond
		connection.setReadTimeout(240 * 1000);
		connection.setDoInput(true);
		connection.setDoOutput(true);

		return connection;
	}
	
	protected String queryConnection(HttpURLConnection connection, String query) {
		connection.setRequestProperty("Content-Length",
				"" + Integer.toString(query.getBytes().length));

		// Send request
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				connection.getOutputStream(), PAGE_ENCODING))) {
			writer.write(query);
			writer.flush();
		} catch (UnsupportedEncodingException encodingException) {
			error("Unsupported encoding: " + PAGE_ENCODING);
			encodingException.printStackTrace();
		} catch (IOException exception) {
			error("Unable to write to connection.");
			exception.printStackTrace();
		}

		try {
			connection.connect();
		} catch (IOException exception) {
			error("Problem connecting to server in queryConnection().");
			exception.printStackTrace();
			return new String();
		}

		// Read the response from the HTTP server
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (IOException e) {
			error("Unable to read response from server.");
			e.printStackTrace();
		}

		return builder.toString();
	}

	public String executeAction(BaseAction action) {
		action.reset();
		String page = "https://" + server + "/api/";
			
		HttpURLConnection connection = null;
		try {
			connection = buildConnection(new URL(page));
		} catch (IOException e) {
			error("Unable to connect to " + page + ".");
			e.printStackTrace();
		}
		
		return queryConnection(connection, action.buildQuery());
	}

	protected void initializeSftpSession(String server, String username,
			String password, int port) throws JSchException {

		log.info("Starting SFTP connection to " + server);

		session = jsch.getSession(username, server, port);
		session.setPassword(password);
		session.connect(timeout);

		channel = (ChannelSftp) session.openChannel("sftp");
		channel.connect(timeout);
	}

	protected void disconnectSftpSession() {
		if (channel != null && !channel.isConnected()) {
			channel.disconnect();
		}

		if (session != null && !session.isConnected()) {
			session.disconnect();
		}
	}

	protected boolean isConnectedForSftp() {
		if (session == null || channel == null) {
			return false;
		}

		return session.isConnected() && channel.isConnected();
	}

	protected ChannelSftp getSftpChannel() {
		return channel;
	}

	/**
	 * Transfer a file to the SFTP drop. Calls initializeSftpSession() so it
	 * should work transparently.
	 * 
	 * @param action
	 *            An SFTP action that has valid response from PeakInvestigator
	 *            SaaS.
	 * @param localFilename
	 *            The local filename of the file to be uploaded.
	 * @param remoteFilename
	 *            The name of the file, including the full path, once uploaded.
	 * @param monitor
	 *            An object implementing the SftpProgressMonitor interface.
	 * @throws JSchException
	 * @throws SftpException
	 */
	public void putFile(SftpAction action, String localFilename,
			String remoteFilename, SftpProgressMonitor monitor)
			throws JSchException, SftpException {

		log.info("Send " + action.getSftpUsername() + "@" + action.getHost()
				+ ":" + remoteFilename);

		initializeSftpSession(action.getHost(), action.getSftpUsername(),
				action.getSftpPassword(), action.getPort());

		channel.put(localFilename, remoteFilename, monitor);

		disconnectSftpSession();
	}

	/**
	 * Transfer a file from the SFTP drop. Calls initializeSftpSession() so it
	 * should work transparently.
	 * 
	 * @param action
	 *            An SFTP action that has valid response from PeakInvestigator
	 *            SaaS.
	 * @param remoteFilename
	 *            The name of the file, including the full path, once uploaded.
	 * @param localFilename
	 *            The local filename of the file to be uploaded.
	 * @param monitor
	 *            An object implementing the SftpProgressMonitor interface.
	 * @throws JSchException
	 * @throws SftpException
	 */
	public void getFile(SftpAction action, String remoteFilename,
			String localFilename, SftpProgressMonitor monitor)
			throws JSchException, SftpException {

		log.info("Retrieve " + action.getSftpUsername() + "@"
				+ action.getHost() + ":" + remoteFilename);

		initializeSftpSession(action.getHost(), action.getSftpUsername(),
				action.getSftpPassword(), action.getPort());

		channel.get(remoteFilename, localFilename, monitor);

		disconnectSftpSession();
	}

}
