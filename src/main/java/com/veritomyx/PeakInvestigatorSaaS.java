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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.desktop.preferences.MZminePreferences;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.parameters.dialogs.ParameterSetupDialog;
import net.sf.mzmine.util.ExitCode;
import net.sf.opensftp.SftpException;
import net.sf.opensftp.SftpResult;
import net.sf.opensftp.SftpSession;
import net.sf.opensftp.SftpUtil;
import net.sf.opensftp.SftpUtilFactory;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.JOptionPane;

import org.json.simple.parser.ParseException;

import java.util.Map;

import com.veritomyx.PeakInvestigatorInitDialog;
import com.veritomyx.actions.*;

/**
 * This class is used to access the Veritomyx SaaS servers
 * 
 * @author dschmidt
 */
public class PeakInvestigatorSaaS
{
	// Required CLI version (see https://secure.veritomyx.com/interface/API.php)
	public static final String reqVeritomyxCLIVersion = "2.13";

	enum Action { PI_VERSIONS, INIT, SFTP, PREP, RUN, STATUS, DELETE }; 

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

	// page actions
	private static final String dateFormating = "yyyy-MM-dd kk:mm:ss";

	private Logger log;
	private String username;
	private String password;
	private int    aid;
	private String jobID;				// name of the job and the scans tar file
	private String dir;
	private Double funds;
	private Map<String, Double> SLAs;
	private String[] PIversions;
	private String   SLA_key;
	private String   PIversion;
	private String    sftp_host;
	private String    sftp_user;
	private String    sftp_pw;
	private int	  	  sftp_port;
	private SftpUtil  sftp;
	private String 	  sftp_file;
	
	public  enum 	prep_status_type {PREP_ANALYZING, PREP_READY, PREP_ERROR };
	private prep_status_type prep_status;
	private int  	prep_scan_count;
	private String 	prep_ms_type;
 
	private int 	s_scansInput;
	private int 	s_scansComplete;
	private String 	s_actualCost;
	private String 	s_jobLogFile;
	private String  s_resultsFile;
	
	private Date    event_date;
	
	private int    web_result;
	private String web_str;

	/**
	 * Constructor
	 * 
	 * @param reqVersion
	 * @param live
	 */
	public PeakInvestigatorSaaS(boolean live)
	{
		log        = Logger.getLogger(this.getClass().getName());
		log.setLevel(live ? Level.INFO : Level.DEBUG);
		log.info(this.getClass().getName());
		jobID      = null;
		dir        = null;

		sftp_user  = null;
		sftp_pw    = null;
		sftp_port  = 22;
		sftp       = SftpUtilFactory.getSftpUtil();
		sftp_file  = null;
		web_result = W_UNDEFINED;
		web_str    = null;
		funds 	   = null;
		SLAs	   = null;
		PIversions = null;
		SLA_key	   = null;
		PIversion  = null;
		
		prep_status     = prep_status_type.PREP_ANALYZING; // TODO : unncessary?
		prep_scan_count = 0;
		prep_ms_type    = null;
	}

	/**
	 * Define the JobID
	 *  
	 * @param email
	 * @param passwd
	 * @param account
	 * @param existingJobName
	 * @param scanCount
	 * @return
	 */
	public int init(String email, String passwd, int account, String existingJobName, int scanCount, int minMass, int maxMass)
	{
		jobID    = null;	// if jobID is set, this is a valid job
		username = email;
		password = passwd;
		aid      = account;
		boolean pickup = (existingJobName != null);

		// make sure we have access to the Veritomyx Server
		// this also gets the job_id and SFTP credentials
		if (!pickup)
		{
			if (getPage(Action.INIT, scanCount, minMass, maxMass) != W_INFO)
				return web_result;
			// Ask user which SLA and PIversion
			PeakInvestigatorInitDialog dialog = new PeakInvestigatorInitDialog(MZmineCore
                    .getDesktop().getMainWindow(), funds,
	                SLAs, PIversions);
	        dialog.setVisible(true);
			if(dialog.getExitCode() == ExitCode.OK) 
			{
				SLA_key = dialog.getSLA();
				PIversion = dialog.getPIversion();
			} else {
				return web_result;
			}

			if (getPage(Action.SFTP, 0) != W_INFO)
				return web_result;
			if (getPage(Action.PREP, 0) != W_INFO)
				return web_result;
			if (jobID != null)	// we have a valid job
			{
				sftp = SftpUtilFactory.getSftpUtil();
				SftpSession session = openSession();	// open to verify we can
				if (session == null)
				{
					jobID      = null;
					web_str    = "SFTP access not available";
					web_result = W_ERROR_SFTP;
					return web_result;
				}
				closeSession(session);
			}
		}
		else
		{
			// check this job STATUS
			jobID = existingJobName;
			web_result = getPageStatus();
			if (web_result == W_RUNNING) {
//				MZmineCore.getDesktop().displayMessage(MZmineCore.getDesktop().getMainWindow(), "Warning", "Remote job not complete. Please try again later.", log);
			} else if (web_result == W_DONE) {
//				MZmineCore.getDesktop().displayMessage(MZmineCore.getDesktop().getMainWindow(), "Completed", "Remote job complete." + web_str, log);
			} else {
				jobID = null;
				return web_result;
			}
		}

		return web_result;
	}

	/**
	 * Provide access to some private data
	 * 
	 * @return
	 */
	public String getJobID()            { return jobID; }
	public int    getPageStatus()       { return getPage(Action.STATUS,     0); }
	public int    getPageRun(int count) { return getPage(Action.RUN,    count); }
	public int	  getPagePrep(int count){ return getPage(Action.PREP, 	 count); }
	public int    getPageDone()         { return getPage(Action.DELETE,       0); }
	public int	  getPageSftp()			{ return getPage(Action.SFTP, 		 0); }
	public String getPageStr()          { return web_str; }
	public int	  getPage(Action action, int count) { return getPage(action, count, 0, Integer.MAX_VALUE); }
	
	public prep_status_type getPrepStatus() { return prep_status; }
	
	public Double	getFunds()				{ return funds; }
	public Map<String, Double> getSLAs()	{ return SLAs; }
	public String[] getPIversions()			{ return PIversions; }
	public String   getResultsFilename()	{ return s_resultsFile; }
	public String   getJobLogFilename()		{ return s_jobLogFile; }
	
	public  int 	getScansInput() { return s_scansInput; }
	public  int 	getScansComplete() { return s_scansComplete; }

	/**
	 * Get the first line of a web page from the Veritomyx server
	 * Puts first line of results into web_results String
	 * 
	 * @param action
	 * @param count
	 * @return int
	 */
	private int getPage(Action action, int count, int minMass, int maxMass)
	{

		BufferedReader    in = null;
		HttpURLConnection uc = null;
		String page = null;
		try {
			// build the URL with parameters

			String host = MZmineCore.getConfiguration().getPreferences()
					.getParameter(MZminePreferences.vtmxServer).getValue();
			if (host.startsWith("https://")) {
				host = host.substring(8);
			}
			page = "https://" + host + "/api/";
			
			MZminePreferences preferences = MZmineCore.getConfiguration().getPreferences();
			String username = preferences.getParameter(MZminePreferences.vtmxUsername).getValue();
			String password = preferences.getParameter(MZminePreferences.vtmxPassword).getValue();

			BaseAction actionObject = null;
			switch (action) {
			case PI_VERSIONS:
				actionObject = new PiVersionsAction(reqVeritomyxCLIVersion,
						username, password);
				break;
			case INIT:
				if (jobID == null) {
					actionObject = new InitAction(reqVeritomyxCLIVersion,
							username, password, aid, count, 0, minMass, maxMass);
				}
				break;
			case SFTP:
				actionObject = new SftpAction(reqVeritomyxCLIVersion, username, password, aid);
				break;
			case PREP:
				actionObject = new PrepAction(reqVeritomyxCLIVersion, username,
						password, aid, sftp_file);
				break;
			case RUN:
				// TODO: calibration when available
				actionObject = new RunAction(reqVeritomyxCLIVersion, username,
						password, jobID, sftp_file, null, SLA_key, PIversion);
				break;
			case STATUS:
				actionObject = new StatusAction(reqVeritomyxCLIVersion,
						username, password, jobID);
				break;
			case DELETE:
				actionObject = new DeleteAction(reqVeritomyxCLIVersion,
						username, password, jobID);
				break;
			default:
				web_result = W_ERROR_ACTION;
				web_str = "Invalid action";
				return web_result;
			}

			web_result = W_UNDEFINED;
			web_str = "";

// TODO: remove when refactor code works
//			if ((action == JOB_INIT) && (jobID == null))	// new job request
//			{
//				// Check the MaxMass to ensure it is not bigger than the size of the maximum datapoints in the job.
//				Integer maxMasses = 0;
//				
//				RawDataFile[] files = MZmineCore.getProjectManager().getCurrentProject().getDataFiles();
//				for(RawDataFile file : files) {
//					int[] scanNumbers = file.getScanNumbers();
//					for(int scanNum : scanNumbers) {
//						Scan scan = file.getScan(scanNum);
//						int dpCount = scan.getNumberOfDataPoints();
//						maxMasses = Math.max(maxMasses.intValue(), dpCount);
//					}
//				}
//				if(maxMass > maxMasses) {
//					maxMass = maxMasses;
//				}
//
//			}

			log.debug(page);

			String query = actionObject.buildQuery();
			log.debug(query);

			URL url = new URL(page);
			uc = (HttpURLConnection)url.openConnection();
			uc.setUseCaches(false);
			uc.setRequestMethod("POST");
			uc.setRequestProperty("Content-Type", 
			           "application/x-www-form-urlencoded");
			uc.setRequestProperty("Content-Length", "" + 
			               Integer.toString(query.getBytes().length));
			uc.setRequestProperty("Content-Language", "en-US");  
			uc.setReadTimeout(240 * 1000);	// give it 4 minutes to respond
			System.setProperty("java.net.preferIPv4Stack", "true");	// without this we get exception in getInputStream
			uc.setDoInput(true);
			uc.setDoOutput(true);
			
			//Send request
			OutputStream os = uc.getOutputStream();
			BufferedWriter writer = new BufferedWriter(
			        new OutputStreamWriter(os, "UTF-8"));
			writer.write(query);
			writer.flush();
			writer.close();
			os.close();
			
			uc.connect();

			// Read the response from the HTTP server
			in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = in.readLine()) != null)
			{
				builder.append(line);
			}

			String decodedString = builder.toString();

			log.debug(decodedString);
			try {
				actionObject.processResponse(decodedString);
			} catch (Exception e) {
				log.error(e);
				if (e instanceof ParseException) {
					log.error("JSON Parse Error at position: "
							+ ((ParseException) e).getPosition());
				}

				JOptionPane
						.showMessageDialog(
								MZmineCore.getDesktop().getMainWindow(),
								"The response from Peak Investigator Saas was not understood.",
								MZmineCore.MZmineName,
								JOptionPane.ERROR_MESSAGE);
				web_str = decodedString;
				return (web_result = W_ERROR);
			}		
			
			if (web_result == W_UNDEFINED) {
				
				if(!actionObject.hasError()) {

					if (actionObject instanceof InitAction) {
						web_result = W_INFO;

						InitAction temp = (InitAction) actionObject;
						jobID = temp.getJob();
						funds = temp.getFunds();
						PIversions = temp.getPiVersions();
						SLAs = temp.getRTOs();
					} else if (actionObject instanceof SftpAction) {
					web_result = W_SFTP;
					
					SftpAction temp = (SftpAction) actionObject;
					sftp_host = temp.getHost();
					sftp_port = temp.getPort();
					dir = temp.getDirectory();
					sftp_user = temp.getSftpUsername();
					sftp_pw = temp.getSftpPassword();
				} else if (actionObject instanceof PrepAction) {
						web_result = W_PREP;

						PrepAction temp = (PrepAction) actionObject;
						switch (temp.getStatus()) {
						case Ready:
							prep_status = prep_status_type.PREP_READY;
							prep_ms_type = temp.getMStype();
							prep_scan_count = temp.getScanCount();
							if (prep_scan_count != count) {
								// TODO Need to check the return value and
								// process
								String mesg = "Peak Investigator Saas returned a Scan Count that does not match "
										+ "the Scan Count determined by MZMine.  Do you want to continue "
										+ "submitting the job?";
								JOptionPane.showMessageDialog(MZmineCore
										.getDesktop().getMainWindow(), mesg,
										MZmineCore.MZmineName,
										JOptionPane.QUESTION_MESSAGE);
							}
							break;
						case Analyzing:
							prep_status = prep_status_type.PREP_ANALYZING;
							break;
						default:
							String mesg = "Peak Investigator Saas returned an error in the PREP phase.";
							JOptionPane.showMessageDialog(MZmineCore
									.getDesktop().getMainWindow(), mesg,
									MZmineCore.MZmineName,
									JOptionPane.ERROR_MESSAGE);
						}
					} else if (actionObject instanceof StatusAction) {
						web_result = W_RUNNING;

						StatusAction temp = (StatusAction) actionObject;
						event_date = temp.getDate();
						switch (temp.getStatus()) {
						case Done:
							web_result = W_DONE;
							s_scansInput = temp.getNumberOfInputScans();
							s_scansComplete = temp.getNumberOfCompleteScans();
							s_actualCost = temp.getActualCost();
							s_jobLogFile = temp.getLogFilename();
							s_resultsFile = temp.getResultsFilename();
							web_str += "\nScans Input: " + s_scansInput
									+ "\nScans Completed: " + s_scansComplete
									+ "\nActual Cost:  " + s_actualCost
									+ "\nJob Log File: " + s_jobLogFile
									+ "\nResults File: " + s_resultsFile;
							break;
						case Running:
							break;
						default:
							break;
						}
				} else if (actionObject instanceof RunAction) { 
					web_result = W_RUNNING;
				} else if (actionObject instanceof DeleteAction) {
					web_result = W_DONE;
					event_date = ((DeleteAction) actionObject).getDate();
				} else {
					long err = actionObject.getErrorCode(); // "Error":#
					web_result = -(int)err;
					web_str = actionObject.getErrorMessage();
				}
			}
		  }
		}
		catch (Exception e)
		{
			log.error(e.getMessage());
			web_result = W_EXCEPTION;
			web_str    = "Web exception - Unabled to connect to server:\n" + page;
		}
		try { in.close();      } catch (Exception e) { }
		try { uc.disconnect(); } catch (Exception e) { }
		log.debug("Web results: [" + web_result + "] '" + web_str + "'");
		return web_result;
	}

	/**
	 * Open the SFTP session
	 * 
	 * @return boolean
	 */
	private SftpSession openSession()
	{
		SftpSession session;
		try {
			session = sftp.connectByPasswdAuth(sftp_host, sftp_port, sftp_user, sftp_pw, SftpUtil.STRICT_HOST_KEY_CHECKING_OPTION_NO, 6000);
		} catch (SftpException e) {
			session = null;
			web_result = W_ERROR_SFTP;
			web_str    = "Cannot connect to SFTP server " + sftp_user + "@" + sftp_host;
			return null;
		}
		SftpResult result = sftp.cd(session, dir);	// cd into the account directory
		if (!result.getSuccessFlag())
		{
			result = sftp.mkdir(session, dir);
			if (!result.getSuccessFlag())
			{
				web_result = W_ERROR_SFTP;
				web_str    = "Cannot create remote directory, " + dir;
				return null;
			}
			sftp.chmod(session, 0770, dir);
			result = sftp.cd(session, dir);
/*			result = sftp.mkdir(session, jobID);
			if (!result.getSuccessFlag())
			{
				web_result = W_ERROR_SFTP;
				web_str    = "Cannot create remote directory, " + dir + "//" + jobID;
				return null;
			}
			sftp.chmod(session, 0770, jobID);
*/			
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
	 */
	public boolean putFile(String fname)
	{
		SftpResult result;
		sftp_file = fname;
		log.info("Transmit " + sftp_user + "@" + sftp_host + ":" + dir + "/" + fname);
		SftpSession session = openSession();
		if (session == null)
			return false;

		sftp.cd(session, dir);
		sftp.rm(session, fname);
		sftp.rm(session, fname + ".filepart");
		result = sftp.put(session, fname, fname + ".filepart");
//		sftp.cd(session, "..");
		if (!result.getSuccessFlag())
		{
			closeSession(session);
			web_result = W_ERROR_SFTP;
			web_str    = "Cannot write file: " + fname;
			return false;
		}
		else
		{
//			sftp.cd(session, "batches");
			result = sftp.rename(session, fname + ".filepart", fname); //rename a remote file
			sftp.cd(session, "..");
			if (!result.getSuccessFlag())
			{
				closeSession(session);
				web_result = W_ERROR_SFTP;
				web_str    = "Cannot rename file: " + fname;
				return false;
			}
		}
		closeSession(session);
		return true;
	}

	/**
	 * Transfer the given file from SFTP drop
	 * 
	 * @param fname
	 */
	public boolean getFile(String fname)
	{
		SftpResult result;
		log.info("Retrieve " + sftp_user + "@" + sftp_host + ":" + fname);
		SftpSession session = openSession();
		if (session == null)
			return false;

		result = sftp.get(session, fname);
		if (!result.getSuccessFlag())
		{
			closeSession(session);
			web_result = W_ERROR_SFTP;
			web_str    = "Cannot read file: " + fname;
			return false;
		}

		closeSession(session);
		return true;
	}
	
	public int deleteJob(String email, String passwd, int account, String existingJobName) {
		if(existingJobName == null) return W_ERROR_JOB_NOT_FOUND;
		username = email;
		password = passwd;
		aid      = account;
		jobID = existingJobName;
		return getPageDone();
	}
}
