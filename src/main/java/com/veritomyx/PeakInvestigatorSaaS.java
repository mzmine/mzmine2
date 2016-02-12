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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sf.opensftp.SftpException;
import net.sf.opensftp.SftpResult;
import net.sf.opensftp.SftpSession;
import net.sf.opensftp.SftpUtil;
import net.sf.opensftp.SftpUtilFactory;

import org.apache.log4j.Logger;

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

	private String server = null;

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

	private SftpUtil  sftp;

	/**
	 * Constructor
	 * 
	 * @param reqVersion
	 * @param live
	 */
	public PeakInvestigatorSaaS(String server)
	{
		// without this we get exception in getInputStream
		System.setProperty("java.net.preferIPv4Stack", "true");
		if (server.startsWith("https://")) {
			this.server = server.substring(8);
		} else {
			this.server = server;
		}
		
		log        = Logger.getLogger(this.getClass().getName());
		log.info(this.getClass().getName());

		sftp       = SftpUtilFactory.getSftpUtil();
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

	/**
	 * Open a SFTP session on remote server.
	 * 
	 * @param SftpAction
	 *            Action object containing host, port, username, password, and
	 *            directory to upload files.
	 * @return The SftpSession
	 * @throws SftpException
	 *             When unable to connect via password or cannot enter the
	 *             directory, even after trying to make it.
	 */
	protected SftpSession openSession(SftpAction action) throws SftpException {
		SftpSession session = sftp.connectByPasswdAuth(action.getHost(),
				action.getPort(), action.getSftpUsername(),
				action.getSftpPassword(),
				SftpUtil.STRICT_HOST_KEY_CHECKING_OPTION_NO, 6000);

		String directory = action.getDirectory();
		SftpResult result = sftp.cd(session, directory);
		if (!result.getSuccessFlag()) {
			result = sftp.mkdir(session, directory);
			if (!result.getSuccessFlag()) {
				throw new SftpException(
						"Unable to enter remote SFTP directory: " + directory);
			}
			sftp.chmod(session, 0770, directory);
			result = sftp.cd(session, directory);
		}

		return session;
	}

	/**
	 * Close the SFTP session
	 * Call this when we are closing the instance
	 */
	private void closeSession(SftpSession session)
	{
		if ((sftp != null) && (session != null))
			sftp.disconnect(session);
	}

	/**
	 * Transfer the given file to SFTP drop
	 * 
	 * @param fname
	 * @throws SftpException 
	 */
	public void putFile(SftpAction action, File file) throws SftpException
	{
		String filename = file.getName();
		String tempFilename = filename + ".filepart";

		log.info("Transmit " + action.getSftpUsername() + "@"
				+ action.getSftpPassword() + ":" + action.getDirectory() + "/"
				+ filename);

		SftpSession session = openSession(action);

		sftp.cd(session, action.getDirectory());
		sftp.rm(session, filename);
		sftp.rm(session, tempFilename);
		SftpResult result = sftp.put(session, file.toString(), tempFilename);

		if (!result.getSuccessFlag())
		{
			closeSession(session);
			throw new SftpException("Unable to upload file: " + file.toString());
		}
		else
		{
			result = sftp.rename(session, tempFilename, filename); //rename a remote file
			if (!result.getSuccessFlag())
			{
				closeSession(session);
				throw new SftpException("Unable to rename temporary file: " + tempFilename);
			}
		}
		closeSession(session);

	}

	/**
	 * Transfer the given file from SFTP drop
	 * 
	 * @param fname
	 * @throws SftpException 
	 */
	public void getFile(SftpAction action, String remoteFilename, File localFile)
			throws SftpException {
		log.info("Retrieve " + action.getSftpUsername() + "@"
				+ action.getHost() + ":" + remoteFilename);
		SftpSession session = openSession(action);

		SftpResult result = sftp.get(session, remoteFilename,
				localFile.toString());
		if (!result.getSuccessFlag()) {
			closeSession(session);
			throw new SftpException("Unable to download remote file: "
					+ remoteFilename);
		}

		closeSession(session);
	}

}
