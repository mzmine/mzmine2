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

package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;

import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.datamodel.impl.RemoteJob;
import net.sf.mzmine.datamodel.impl.SimpleDataPoint;
import net.sf.mzmine.datamodel.impl.RemoteJob.Status;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.InitDialog;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.PeakInvestigatorDefaultDialogFactory;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.PeakInvestigatorDialogFactory;
import net.sf.mzmine.util.ExitCode;
import net.sf.mzmine.util.GUIUtils;
import net.sf.mzmine.util.dialogs.interfaces.BasicDialog;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;
import org.xeustechnologies.jtar.TarOutputStream;

import com.veritomyx.FileChecksum;
import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.InitAction;
import com.veritomyx.actions.PrepAction;
import com.veritomyx.actions.RunAction;
import com.veritomyx.actions.SftpAction;
import com.veritomyx.actions.StatusAction;

/**
 * This class is used to run a set of scans through the Veritomyx SaaS servers
 * 
 * @author dschmidt
 *
 */
public class PeakInvestigatorTask
{
	// utility classes
	private Logger logger;
	private PeakInvestigatorSaaS vtmx = null;
	private PeakInvestigatorDialogFactory dialogFactory = new PeakInvestigatorDefaultDialogFactory();

	// credentials and project info
	private String username;
	private String password;
	private int projectID;

	// keep track of state (both submit and fetch)
	private Boolean launch = null;
	private RawDataFile rawDataFile = null;
	private String jobID = null;
	private String targetName;

	// keep track of state (submit)
	private String selectedRTO = null;

	// keep track of state (fetch)
	private RemoteJob job = null;
	private boolean displayLog;
	private StatusAction statusAction = null;

	private String          desc;
	private int             scanCnt;		// number of scans

	// temporary storage
	private File workingDirectory;
	private File workingFile;
	private TarOutputStream tarfile = null;

	private int             errors;
	
	private static final long minutesCheckPrep = 2;
	private static final long minutesTimeoutPrep = 20;
	
	private static final int numSaaSStartSteps = 16;
	private static final int numSaaSRetrieveSteps = 7;

	public PeakInvestigatorTask(String server, String username,
			String password, int projectID) {

		this.vtmx = new PeakInvestigatorSaaS(server);
		this.username = username;
		this.password = password;
		this.projectID = projectID;

		logger = Logger.getLogger(this.getClass().getName());
		logger.setLevel(MZmineCore.VtmxLive ? Level.INFO : Level.FINEST);
	}

	public PeakInvestigatorTask withRawDataFile(RawDataFile raw) {
		this.rawDataFile = raw;
		return this;
	}

	public PeakInvestigatorTask shouldDisplayLog(boolean displayLog) {
		this.displayLog = displayLog;
		return this;
	}

	public PeakInvestigatorTask withService(PeakInvestigatorSaaS vtmx) {
		this.vtmx = vtmx;
		return this;
	}

	public PeakInvestigatorTask usingDialogFactory(PeakInvestigatorDialogFactory factory) {
		this.dialogFactory = factory;
		return this;
	}

	public void initializeSubmit(String versionOfPi, int scanCount, int[] massRange,
			String target) throws ResponseFormatException,
			ResponseErrorException, IllegalStateException {

		this.launch = true;
		this.targetName = target;

		logger.info("Initializing PeakInvestigator™ Task");
		desc    = "initializing";

		InitAction initAction = InitAction
				.create(PeakInvestigatorSaaS.API_VERSION, username, password)
				.usingProjectId(projectID).withPiVersion(versionOfPi)
				.withScanCount(scanCount, 0)
				.withNumberOfPoints(getMaxNumberOfPoints(rawDataFile))
				.withMassRange(massRange[0], massRange[1]);
		String response = vtmx.executeAction(initAction);
		initAction.processResponse(response);

		if(!initAction.isReady("INIT")) {
			throw new IllegalStateException("Problem initializing submit job.");
		}

		if (initAction.hasError()) {
			throw new ResponseErrorException(initAction.getErrorMessage());
		}

		InitDialog dialog = dialogFactory.createInitDialog(versionOfPi, initAction);
		dialog.setVisible(true);
		if(dialog.getExitCode() != ExitCode.OK) {
			return;
		}

		jobID = initAction.getJob();
		selectedRTO = dialog.getSelectedRTO();

		try {
			initializeTemporaryStorage(jobID, ".scans.tar");
		} catch (IOException e) {
			error("Unable to create temporary diretory.");
			jobID = null;
			return;
		}
	}

	/**
	 * Initialize the object to start downloading results. First checks status
	 * of results, and if still running or already deleted, display a message
	 * and return. Otherwise, acknowledge job is done and set variables for
	 * later steps.
	 * 
	 * @param compoundJobName
	 * @throws ResponseFormatException
	 * @throws ResponseErrorException 
	 */
	public void initializeFetch(String compoundJobName, boolean displayLog)
			throws ResponseFormatException, ResponseErrorException {

		this.statusAction = null;
		this.job = rawDataFile.getJob(compoundJobName);
		if (this.job == null) {
			return;
		}

		this.launch = false;
		this.displayLog = displayLog;
		this.jobID = RemoteJob.filterJobName(compoundJobName);
		this.targetName = RemoteJob.filterTargetName(compoundJobName);

		StatusAction action = new StatusAction(
				PeakInvestigatorSaaS.API_VERSION, username, password, jobID);
		String response = vtmx.executeAction(action);
		action.processResponse(response);

		if (!action.isReady("STATUS")) {
			throw new IllegalStateException("Problem initializing fetch job.");
		}

		if (action.hasError()) {
			throw new ResponseErrorException(action.getErrorMessage());
		}

		StatusAction.Status status = action.getStatus();
		switch (status) {
		case Running:
			message(action.getMessage());
			this.jobID = null;
			return;
		case Done:
			message(action.getMessage());
			break;
		case Deleted:
			message(action.getMessage());
			this.jobID = null;
			return;
		default:
			throw new IllegalStateException(
					"Unknown status returned from server.");
		}

		// if Job is done, keep track of action
		this.statusAction = action;

		try {
			initializeTemporaryStorage(jobID, ".mass_list.tar");
		} catch (IOException e) {
			error("Unable to create temporary diretory.");
			jobID = null;
			return;
		}
	}

	private void initializeTemporaryStorage(String jobID, String extension) throws IOException {
		Path tempPath = null;
		tempPath = Files.createTempDirectory(jobID + "-");

		workingDirectory = new File(tempPath.toString());
		workingDirectory.deleteOnExit();

		workingFile = new File(tempPath + File.separator + jobID + extension);
		workingFile.deleteOnExit();
	}

	public String getDesc() { return desc; }

	/**
	 * Return the name of this task
	 * 
	 * @return
	 */
	public String getName() { return jobID; }

	public void start() throws FileNotFoundException, ResponseFormatException,
			ResponseErrorException, IOException {

		logger.finest("PeakInvestigatorTask - start");
		if (launch) {
			startLaunch();
		} else {
			startRetrieve();
		}
	}

	/**
	 * Compute the peaks list for the given scan. Method for processing scans
	 * depends whether this instance of PeakInvestigatorTask is for launching or
	 * retrieving jobs.
	 * 
	 * @param scan — Scan to be processed
	 * @return DataPoints comprising the mass list
	 */
	public DataPoint[] processScan(Scan scan) {
		int scan_num = scan.getScanNumber();
		logger.finest("PeakInvestigatorTask - processScan " + scan_num);
		if (launch) {
			return processScanLaunch(scan_num, scan);
		}

		return processScanRetrieve(scan_num);
	}

	/**
	 * Send the bundle of scans to the VTMX cloud processor via the SFTP drop
	 */
	public void finish() {
		logger.finest("PeakInvestigatorTask - finish");
		if (launch) {
			try {
				finishLaunch();
			} catch (InterruptedException interruptedException) {
				// Don't do anything if cancelled
			} catch (IllegalStateException illegalStateException) {
				error(illegalStateException.getMessage());
			} catch (ResponseFormatException responseFormatException) {
				error(responseFormatException.getMessage());
			} catch (ResponseErrorException responseError) {
				error(responseError.getMessage());
			}
		} else
			finishRetrieve();
	}

	private void startLaunch() throws FileNotFoundException, IOException {
		desc = "starting launch";
		logger.info("Preparing to launch new job, " + jobID);
		scanCnt = 0;

		FileOutputStream stream = new FileOutputStream(workingFile);
		tarfile = new TarOutputStream(new BufferedOutputStream(
				new GZIPOutputStream(stream)));

		desc = "launch started";
	}

	/**
	 * Add scan to compressed tar file
	 * 
	 * @param scan_num
	 * @param scan
	 * @return
	 */
	private DataPoint[] processScanLaunch(int scan_num, Scan scan)
	{
		desc = "exporting scan " + scan_num;
		// ########################################################################
		// Export all scans to remote processor
		try {
			// export the scan to a file
			String filename = "scan_" + String.format("%04d", scan_num) + ".txt";
			scan.exportToFile("", workingDirectory.toString(), filename);

			// put the exported scan into the tar file
			File f = new File(getFilenameWithPath(filename));
			tarfile.putNextEntry(new TarEntry(f, filename));
			BufferedInputStream origin = new BufferedInputStream(new FileInputStream(f));
			int count;
			byte data[] = new byte[2048];
			while ((count = origin.read(data)) != -1)
				tarfile.write(data, 0, count);
			origin.close();
			f.delete();			// remove the local copy of the scan file
			tarfile.flush();
			scanCnt += 1;		// count this scan
		} catch (IOException e) {
			logger.finest(e.getMessage());
			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", "Cannot write to scans bundle file", logger);
		}
		desc = "scan " + scan_num + " exported";
		return null;	// never return peaks from pass 1
	}

	protected void uploadFileToServer(File file)
			throws ResponseFormatException, IllegalStateException, ResponseErrorException {

		logger.info("Transmit scans bundle, " + file.getName()
				+ ", to SFTP server...");

		SftpAction sftpAction = new SftpAction(
				PeakInvestigatorSaaS.API_VERSION, username, password, projectID);
		String response = vtmx.executeAction(sftpAction);
		sftpAction.processResponse(response);

		if (!sftpAction.isReady("SFTP")) {
			throw new IllegalStateException("Problem getting SFTP credentials.");
		}

		if (sftpAction.hasError()) {
			throw new ResponseErrorException(sftpAction.getErrorMessage());
		}

		vtmx.putFile(sftpAction, file);
	}

	protected PrepAction checkPrepAnalysis(String filename)
			throws ResponseFormatException, IllegalStateException, ResponseErrorException {

		logger.info("Waiting for PREP analysis to complete, "
				+ filename + ", on SaaS server...Please be patient.");

		PrepAction prepAction = new PrepAction(
				PeakInvestigatorSaaS.API_VERSION, username, password,
				projectID, filename);
		String response = vtmx.executeAction(prepAction);
		prepAction.processResponse(response);

		if (!prepAction.isReady("PREP")) {
			throw new IllegalStateException("Problem with PREP analysis.");
		}

		if (prepAction.hasError()) {
			throw new ResponseErrorException(prepAction.getErrorMessage());
		}

		return prepAction;
	}

	protected void initiateRun(String filename, String selectedRTO)
			throws ResponseFormatException, IllegalStateException, ResponseErrorException {

		logger.info("Launch job (RUN), " + jobID + ", on cloud server...");

		RunAction runAction = new RunAction(PeakInvestigatorSaaS.API_VERSION,
				username, password, jobID, selectedRTO, filename, null);
		String response = vtmx.executeAction(runAction);
		runAction.processResponse(response);

		if (!runAction.isReady("RUN")) {
			throw new IllegalStateException("Problem initiating RUN.");
		}

		if (runAction.hasError()) {
			throw new ResponseErrorException(runAction.getErrorMessage());
		}
	}

	/**
	 * Finish the job launch process and send job to VTMX SaaS server
	 */
	private void finishLaunch() throws InterruptedException, ResponseFormatException, IllegalStateException, ResponseErrorException
	{
		desc = "finishing launch";
		ProgressMonitor progressMonitor = new ProgressMonitor(MZmineCore.getDesktop().getMainWindow(),
                "Peak Investigator SaaS transmission",
                "", 0, numSaaSStartSteps);
		progressMonitor.setMillisToPopup(0);
		progressMonitor.setMillisToDecideToPopup(0);
		progressMonitor.setNote("Sending scans to PeakInvestigator service.");
		
		try {
			tarfile.close();
		} catch (IOException e) {
			logger.finest(e.getMessage());
			error("Cannot close scans bundle file.");
		}
		progressMonitor.setProgress(1);

		uploadFileToServer(workingFile);
		
		if (progressMonitor.isCanceled()) {
		    progressMonitor.close();
		    logger.info("Job, " + jobID + ", canceled");
		    return;
		}
		
		progressMonitor.setProgress(2);

		//####################################################################
		// Prepare for remote job in a loop as it might take some time to analyze
		logger.info("Awaiting PREP analysis, " + workingFile.getName()
				+ ", on SaaS server...");
		progressMonitor.setNote("Performing a pre-check analysis.");
		

		progressMonitor.setProgress(3);
		progressMonitor.setProgress(4);

		// timeWait is based on the loop count to keep this as short as possible.
		long timeWait = minutesTimeoutPrep;
		int count = 0;
		PrepAction prepAction = checkPrepAnalysis(workingFile.getName());
		while (prepAction.getStatus() == PrepAction.Status.Analyzing
				&& timeWait > 0) {
			Thread.sleep(minutesCheckPrep * 60000);
			timeWait -= minutesCheckPrep;
			if (progressMonitor.isCanceled()) {
				progressMonitor.close();
				logger.info("Job, " + jobID + ", canceled");
				return;
			}
			prepAction = checkPrepAnalysis(workingFile.getName());
			progressMonitor.setProgress(4 + count);
			count++;
		}

		if(prepAction.getStatus() != PrepAction.Status.Ready) {
			error("Failed to launch or complete PREP Phase for " + jobID);
			return;
		}
		
		progressMonitor.setProgress(15);
		//####################################################################
		// start for remote job
		progressMonitor.setNote("Requesting job to be run...");
		initiateRun(workingFile.getName(), selectedRTO);

		progressMonitor.setNote("Finished");
		progressMonitor.close();
		
		// job was started - record it
		logger.info("Job, " + jobID + ", launched");

		rawDataFile.addJob("job-" + jobID, rawDataFile, targetName, vtmx);	// record this job start
		logger.finest(vtmx.getPageStr());

		desc = "launch finished";
	}

	/**
	 * Start the retrieval of the job
	 * Wait for job to finish in cloud and pickup the results
	 * @throws ResponseErrorException 
	 * @throws ResponseFormatException 
	 * @throws FileNotFoundException 
	 */
	private void startRetrieve() throws ResponseFormatException, ResponseErrorException, FileNotFoundException
	{
		errors = 0;
		desc = "checking for results";
		int status;
		logger.info("Checking previously launched job, " + jobID);
		
		ProgressMonitor progressMonitor = new ProgressMonitor(MZmineCore.getDesktop().getMainWindow(),
                "Preparing to start job...",
                "", 0, numSaaSRetrieveSteps);
		progressMonitor.setMillisToPopup(0);
		progressMonitor.setMillisToDecideToPopup(0);
		progressMonitor.setNote("Retrieving results from PeakInvestigator service.");

//		progressMonitor.setProgress(1);
		progressMonitor.setNote("Downloading job, " + jobID + " results...");
		
		desc = "downloading results";
		logger.info("Downloading job, " + jobID + ", results...");
		
//		// check to see if the results were complete.
//		// this will be shown in the string returned from the status call to the web.
//		
//		int valid = vtmx.getScansComplete();
//		int scans = vtmx.getScansInput();
//		if (valid < scans)
//		{
//			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", "Only " + valid + " of " + scans + " scans were successful.\n" +
//																"The valid results will be loaded now.\n" + 
//																"You have been credited for the incomplete scans.", logger);
//		}

		// Get the SFTP data for the completed job 
		progressMonitor.setProgress(2);
		File remoteResultsFile = new File(statusAction.getResultsFilename());
		progressMonitor.setNote("Reading centroided data, " + remoteResultsFile.getName() + ", from SFTP drop...");
		downloadFileFromServer(remoteResultsFile, workingFile);
		
		if (progressMonitor.isCanceled()) {
		    progressMonitor.close();
		    logger.info("Job, " + jobID + ", canceled");
		    return;
		}

		{
			progressMonitor.setProgress(3);
			
			extractScansFromTarball(workingFile);




			progressMonitor.setProgress(5);
			if (progressMonitor.isCanceled()) {
			    progressMonitor.close();
			    logger.info("Job, " + jobID + ", canceled");
			    return;
			}


			progressMonitor.setNote("Finished");
			progressMonitor.setProgress(7);
		}
		progressMonitor.close();
		
		desc = "results downloaded";
	}

	protected void downloadFileFromServer(File remoteFile, File localFile)
			throws ResponseFormatException, ResponseErrorException {

		logger.info("Receive file, " + remoteFile.getName()
				+ ", from SFTP server...");

		SftpAction sftpAction = new SftpAction(
				PeakInvestigatorSaaS.API_VERSION, username, password, projectID);
		String response = vtmx.executeAction(sftpAction);
		sftpAction.processResponse(response);

		if (!sftpAction.isReady("SFTP")) {
			throw new IllegalStateException("Problem getting SFTP credentials.");
		}

		if (sftpAction.hasError()) {
			throw new ResponseErrorException(sftpAction.getErrorMessage());
		}

		vtmx.getFile(sftpAction, remoteFile.getName(), localFile);
	}

	protected void extractScansFromTarball(File workingFile)
			throws FileNotFoundException {

		FileInputStream inputStream = new FileInputStream(workingFile);
		TarInputStream tis = null;
		try {
			tis = new TarInputStream(new GZIPInputStream(inputStream));
			TarEntry tf;

			while ((tf = tis.getNextEntry()) != null) {
				if (tf.isDirectory())
					continue;
// TODO : progress
				// progressMonitor.setNote("Extracting peaks data to " +
				// tf.getName() + " - " + tf.getSize() + " bytes");

				extractScan(tis, getFilenameWithPath(tf.getName()));
			}

			tis.close();
		} catch (IOException e) {
			error("Problem extracting scans from: "
					+ workingFile.getAbsolutePath());
			e.printStackTrace();
		}

// TODO : progress
		// progressMonitor.setProgress(4);

	}

	protected void extractScan(TarInputStream stream, String destinationFilename) {
		// file to be extracted
		File scanFile = new File(destinationFilename);
		scanFile.deleteOnExit();

		try (FileOutputStream outputStream = new FileOutputStream(scanFile)) {
			int bytesRead;
			byte buf[] = new byte[1024];
			while ((bytesRead = stream.read(buf, 0, 1024)) > -1) {
				outputStream.write(buf, 0, bytesRead);
			}
		} catch (IOException e) {
			error("Problem extracting: " + destinationFilename);
			e.printStackTrace();
		}

		logger.info("Extracted peaks data: " + destinationFilename);
	}

	/**
	 * Parse the completed scans
	 * 
	 * @param scan_num
	 * @return
	 */
	private DataPoint[] processScanRetrieve(int scan_num)
	{
		if (errors > 0)
			return null;

		desc = "parsing scan " + scan_num;
		List<DataPoint> mzPeaks = null;

		// read in the peaks for this scan
		// convert filename to expected peak file name
		String basename = "scan_" + String.format("%04d", scan_num) + ".scan.mass_list.txt";
		String pfilename = getFilenameWithPath(basename);
		logger.info("Parsing peaks data from " + basename);
		try
		{
			File centfile = new File(pfilename);
			centfile.deleteOnExit();

			FileChecksum fchksum = new FileChecksum(centfile);
			fchksum.verify(false);
// TODO: handle checksums
//			if (!fchksum.verify(false))
//				throw new IOException("Invalid checksum");
	
			List<String> lines = fchksum.getFileStrings();
			mzPeaks = new ArrayList<DataPoint>();
			for (String line:lines)
			{
				if (line.startsWith("#") || line.isEmpty())	// skip comment lines
					continue;
	
				Scanner sc = new Scanner(line);
				double mz  = sc.nextDouble();
				double y   = sc.nextDouble();
				mzPeaks.add(new SimpleDataPoint(mz, y));
				sc.close();
			}

		}
		catch (FileNotFoundException e) { /* expect some scans might not be included in original processing */ }
		catch (Exception e)
		{
			logger.finest(e.getMessage());
			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", "Cannot parse peaks file, " + pfilename + " (" + e.getMessage() + ")", logger);
		}

		desc = "scan " + scan_num + " parsed";
		return (mzPeaks == null) ? null : mzPeaks.toArray(new DataPoint[mzPeaks.size()]);
	}

	/**
	 * Finish the retrieval process
	 */
	private void finishRetrieve()
	{
		if (errors > 0)
			return;

		desc = "finishing retrieve";
		logger.info("Finishing retrieval of job " + jobID);

		if (System.getProperty("PeakInvestigatorTask.deleteJob")
				.equals("false")) {
			logger.info("Job " + jobID + " not being deleted as requested.");
			return;
		}

		// read the job log tar file and extract all the peak list files
//		progressMonitor.setNote("Reading log, " + remoteFile.getName() + ", from SFTP drop...");
//		logger.info("Reading log, " + remoteFilename + ", from SFTP drop...");

		vtmx.getPageDone();
		rawDataFile.removeJob(jobID);
		desc = "retrieve finished";
		MZmineCore.getDesktop().displayMessage(MZmineCore.getDesktop().getMainWindow(), "Warning", "PeakInvestigator results successfully downloaded.\n" + 
											"All your job files will now be deleted from the Veritomyx servers.\n" +
											"Remember to save your project before closing MZminePI.", logger);
	}

	protected void displayJobLog(File localFile) {
		try (BufferedReader br = new BufferedReader(new FileReader(localFile))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}

			String logInfo = sb.toString();
			PeakInvestigatorLogDialog logDialog = new PeakInvestigatorLogDialog(
					logInfo);
			logDialog.setVisible(true);

		} catch (FileNotFoundException e2) {
			MZmineCore.getDesktop().displayErrorMessage(
					MZmineCore.getDesktop().getMainWindow(), "Error",
					"Log file not found", logger);
		} catch (Exception e1) {
			logger.finest(e1.getMessage());
			MZmineCore.getDesktop().displayErrorMessage(
					MZmineCore.getDesktop().getMainWindow(), "Error",
					"Cannot parse log file", logger);
			errors++;
			e1.printStackTrace();
		}
	}

	/**
	 * Convenience function to return an absolute path for given filename in
	 * the (temporary) working directory.
	 * 
	 * @param filename
	 */
	private String getFilenameWithPath(String filename) {
		return workingDirectory + File.separator + filename;
	}

	/**
	 * Convenience function to get the maximum number of points in a scan. Used
	 * for PeakInvestigator InitAction.
	 * 
	 * @param rawData
	 * @return number of data points (in scan with most data points)
	 */
	private int getMaxNumberOfPoints(RawDataFile rawData) {
		int[] scanIndexes = rawData.getScanNumbers();
		int maxNumberOfPoints = 0;
		for (int index : scanIndexes) {
			Scan scan = rawData.getScan(index);
			int numberOfPoints = scan.getNumberOfDataPoints();
			if (numberOfPoints > maxNumberOfPoints) {
				maxNumberOfPoints = numberOfPoints;
			}
		}

		return maxNumberOfPoints;
	}

	private void error(String message) {
		BasicDialog dialog = dialogFactory.createDialog();
		dialog.displayErrorMessage(message, logger);
	}

	private void message(String message) {
		BasicDialog dialog = dialogFactory.createDialog();
		dialog.displayInfoMessage(message, logger);
	}

	/**
	 * Exception for passing Response errors up the call stack.
	 * 
	 */
	public class ResponseErrorException extends Exception {

		public ResponseErrorException(String message) {
			super(message);
		}
	}

	class PeakInvestigatorLogDialog extends JDialog implements ActionListener {

		private static final long serialVersionUID = 1L;

		PeakInvestigatorLogDialog(String contents) {
			super(MZmineCore.getDesktop().getMainWindow(),
					"PeakInvestigator Job Log",
					Dialog.ModalityType.DOCUMENT_MODAL);

			Container verticalBox = Box.createVerticalBox();

			JTextArea textArea = new JTextArea(contents);
			textArea.setEditable(false);

			JScrollPane scrollPane = new JScrollPane(textArea);
			verticalBox.add(scrollPane);

			Container buttonBox = Box.createHorizontalBox();

			@SuppressWarnings("unused")
			JButton closeButton = GUIUtils.addButton(buttonBox, "Close", null,
					this);

			verticalBox.add(buttonBox);

			add(verticalBox);

			setLocationRelativeTo(getParent());
			setMinimumSize(new Dimension(400, 300));

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object object = e.getSource();

			if (object instanceof JButton
					&& ((JButton) object).getText() == "Close") {
				dispose();
			}
		}
	}

}