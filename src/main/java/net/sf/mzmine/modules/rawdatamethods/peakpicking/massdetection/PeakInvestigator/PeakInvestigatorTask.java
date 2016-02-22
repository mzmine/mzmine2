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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.datamodel.impl.RemoteJob;
import net.sf.mzmine.datamodel.impl.SimpleDataPoint;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.InitDialog;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.PeakInvestigatorDefaultDialogFactory;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.PeakInvestigatorDialogFactory;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.PeakInvestigatorTransferDialog;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.PeakInvestigatorLogDialog;
import net.sf.mzmine.util.ExitCode;
import net.sf.mzmine.util.dialogs.interfaces.BasicDialog;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;
import org.xeustechnologies.jtar.TarOutputStream;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.veritomyx.FileChecksum;
import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.DeleteAction;
import com.veritomyx.actions.InitAction;
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

	// temporary storage
	private File workingDirectory;
	private File workingFile;
	private TarOutputStream tarfile = null;

	public PeakInvestigatorTask(String server, String username,
			String password, int projectID) throws JSchException {

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
			ResponseErrorException, IllegalStateException, IOException {

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
	 * @throws IOException 
	 */
	public void initializeFetch(String compoundJobName, boolean displayLog)
			throws ResponseFormatException, ResponseErrorException, IOException {

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
		case Preparing:
		case Running:
		case Deleted:
			message(action.getMessage());
			this.jobID = null;
			return;
		case Done:
			break;
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
			ResponseErrorException, IOException, SftpException, JSchException {

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
				illegalStateException.printStackTrace();
			} catch (ResponseFormatException | ResponseErrorException responseException) {
				error(responseException.getMessage());
				responseException.printStackTrace();
			} catch (JSchException | SftpException sftpException) {
				error(sftpException.getMessage());
				sftpException.printStackTrace();
			} catch (IOException exception) {
				error(exception.getMessage());
				exception.printStackTrace();
			}
		} else
			try {
				finishRetrieve();
			} catch (ResponseFormatException | ResponseErrorException responseException) {
				error(responseException.getMessage());
				responseException.printStackTrace();
			} catch (JSchException | SftpException sftpException) {
				error(sftpException.getMessage());
				sftpException.printStackTrace();
			} catch (IOException exception) {
				error(exception.getMessage());
				exception.printStackTrace();
			}
	}

	private void startLaunch() throws FileNotFoundException, IOException {
		desc = "starting launch";
		logger.info("Preparing to launch new job, " + jobID);

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
		} catch (IOException e) {
			logger.finest(e.getMessage());
			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", "Cannot write to scans bundle file", logger);
		}
		desc = "scan " + scan_num + " exported";
		return null;	// never return peaks from pass 1
	}

	protected void uploadFileToServer(File file)
			throws ResponseFormatException, IllegalStateException,
			ResponseErrorException, SftpException, JSchException,
			InterruptedException, IOException {

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

		String remoteFilename = sftpAction.getDirectory() + File.separator
				+ file.getName();

		SftpProgressMonitor monitor = dialogFactory.createSftpProgressMonitor();
		vtmx.putFile(sftpAction, file.getAbsolutePath(), remoteFilename,
				monitor);

		if (monitor instanceof PeakInvestigatorTransferDialog) {
			PeakInvestigatorTransferDialog dialog = (PeakInvestigatorTransferDialog) monitor;
			if (dialog.isCanceled()) {
				throw new InterruptedException("Upload canceled.");
			}
		}
	}

	protected void initiateRun(String filename, String selectedRTO)
			throws ResponseFormatException, IllegalStateException,
			ResponseErrorException, IOException {

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
	 * @throws SftpException 
	 * @throws SftpTransferException 
	 * @throws JSchException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private void finishLaunch() throws InterruptedException,
			ResponseFormatException, IllegalStateException,
			ResponseErrorException, JSchException, SftpException, IOException {

		desc = "finishing launch";

		try {
			tarfile.close();
		} catch (IOException e) {
			logger.finest(e.getMessage());
			error("Cannot close scans bundle file.");
		}

		uploadFileToServer(workingFile);

		initiateRun(workingFile.getName(), selectedRTO);
		
		// job was started - record it
		logger.info("Job, " + jobID + ", launched");

		rawDataFile.addJob("job-" + jobID, rawDataFile, targetName);	// record this job start

		desc = "launch finished";
	}

	/**
	 * Start the retrieval of the job
	 * Wait for job to finish in cloud and pickup the results
	 * @throws ResponseErrorException 
	 * @throws ResponseFormatException 
	 * @throws SftpException 
	 * @throws SftpTransferException 
	 * @throws JSchException 
	 * @throws IOException 
	 */
	private void startRetrieve() throws ResponseFormatException,
			ResponseErrorException, JSchException,
			SftpException, IOException {

		desc = "downloading results";
		logger.info("Downloading job, " + jobID + ", results...");

		File remoteResultsFile = new File(statusAction.getResultsFilename());
		downloadFileFromServer(remoteResultsFile, workingFile);

		extractScansFromTarball(workingFile);

		desc = "results downloaded";
	}

	/**
	 * Function to download file from Veritomyx servers. Calls the public API
	 * for SFTP credentials.
	 * 
	 * @param remoteFile
	 *            A file object representing the file to be downloaded from the
	 *            remote server.
	 * @param localFile
	 *            A file object representing the file once downloaded to local
	 *            disk.
	 * @throws ResponseFormatException
	 * @throws ResponseErrorException
	 * @throws SftpException 
	 * @throws SftpTransferException 
	 * @throws JSchException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	protected void downloadFileFromServer(File remoteFile, File localFile)
			throws ResponseFormatException, ResponseErrorException,
			JSchException, SftpException, IOException {

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

		vtmx.getFile(sftpAction, remoteFile.getAbsolutePath(),
				localFile.getAbsolutePath(),
				dialogFactory.createSftpProgressMonitor());
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
		desc = "parsing scan " + scan_num;
		List<DataPoint> mzPeaks = new ArrayList<DataPoint>();;

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

// TODO: handle checksums
			if (!fchksum.verify(false)) {
//				error("File has invalid checksum: " + basename);
//				return new DataPoint[0];
			}

			List<String> lines = fchksum.getFileStrings();
			for (String line:lines)
			{
				// skip comment or blank lines
				if (line.startsWith("#") || line.isEmpty()) {
					continue;
				}
	
				Scanner sc = new Scanner(line);
				double mz  = sc.nextDouble();
				double y   = sc.nextDouble();
				mzPeaks.add(new SimpleDataPoint(mz, y));
				sc.close();
			}

		} catch (FileNotFoundException e) {
			error(e.getMessage());
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			error(e.getMessage());
			e.printStackTrace();
		}

		desc = "scan " + scan_num + " parsed";
		return mzPeaks.toArray(new DataPoint[mzPeaks.size()]);
	}

	/**
	 * Finish the retrieval process
	 * @throws ResponseErrorException 
	 * @throws ResponseFormatException 
	 * @throws SftpException 
	 * @throws SftpTransferException 
	 * @throws JSchException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private void finishRetrieve() throws ResponseFormatException,
			ResponseErrorException, JSchException, SftpException, IOException {

		desc = "finishing retrieve";
		logger.info("Finishing retrieval of job " + jobID);

		File remoteFile = new File(statusAction.getLogFilename());
		File logFile = new File(getFilenameWithPath(remoteFile.getName()));
		downloadFileFromServer(remoteFile, logFile);

		if (displayLog) {
			displayJobLog(logFile);
		}

		String mesg = "PeakInvestigator results successfully downloaded.\n"
				+ "All your job files will now be deleted from the Veritomyx servers.\n"
				+ "Remember to save your project before closing MZmine.";
		message(mesg);

		rawDataFile.removeJob("job-" + jobID);
		desc = "retrieve finished";

		String shouldDelete = System.getProperty("PeakInvestigatorTask.deleteJob");
		if (shouldDelete != null && shouldDelete.equals("false")) {
			logger.info("Job " + jobID + " not being deleted as requested.");
			return;
		}

		deleteJob(jobID);
	}

	protected void deleteJob(String jobID) throws ResponseFormatException,
			ResponseErrorException, IOException {

		DeleteAction action = new DeleteAction(
				PeakInvestigatorSaaS.API_VERSION, username, password, jobID);
		String response = vtmx.executeAction(action);
		action.processResponse(response);

		if (!action.isReady("DELETE")) {
			throw new IllegalStateException("Problem with DELETE call.");
		}

		if (action.hasError()) {
			throw new ResponseErrorException(action.getErrorMessage());
		}

		logger.info("Job " + jobID + " deleted from server");
	}

	protected void displayJobLog(File localFile) throws IOException {
		PeakInvestigatorLogDialog dialog = new PeakInvestigatorLogDialog(
				MZmineCore.getDesktop().getMainWindow(), localFile);
		dialog.setVisible(true);
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

		private static final long serialVersionUID = 1L;

		public ResponseErrorException(String message) {
			super(message);
		}
	}

}