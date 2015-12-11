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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.datamodel.impl.SimpleDataPoint;
import net.sf.mzmine.desktop.preferences.MZminePreferences;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.util.ExitCode;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;
import org.xeustechnologies.jtar.TarOutputStream;

import com.veritomyx.FileChecksum;
import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.PeakInvestigatorSaaS.prep_status_type;

/**
 * This class is used to run a set of scans through the Veritomyx SaaS servers
 * 
 * @author dschmidt
 *
 */
public class PeakInvestigatorTask
{
	private Logger          logger;
	private boolean         launch;			// launch or retrieve
	private String          jobID;			// name of the job and the scans tar file
	private String          desc;
	private int             scanCnt;		// number of scans
	private int				minMass;		// Minimum mass to process from
	private int				maxMass;		// Maximum mass to process to
	private String          targetName;
	private String          intputFilename;
	private String          outputFilename;
	private PeakInvestigatorSaaS   vtmx;
	private String          username;
	private String          password;
	private int             pid;
	private TarOutputStream tarfile;
	private RawDataFile     rawDataFile;
	private int             errors;
	
	private static final long minutesCheckPrep = 2;
	private static final long minutesTimeoutPrep = 20;

	public PeakInvestigatorTask(RawDataFile raw, String pickup_job, String target, ParameterSet parameters, int scanCount)
	{
		logger  = Logger.getLogger(this.getClass().getName());
		logger.setLevel(MZmineCore.VtmxLive ? Level.INFO : Level.FINEST);
		logger.info("Initializing PeakInvestigator™ Task");
		jobID   = null;
		tarfile = null;
		desc    = "initializing";
		
		minMass = parameters.getParameter(PeakInvestigatorParameters.minMass).getValue();
		maxMass = parameters.getParameter(PeakInvestigatorParameters.maxMass).getValue();

		// pickup all the parameters
		MZminePreferences preferences = MZmineCore.getConfiguration().getPreferences();
		username = preferences.getParameter(MZminePreferences.vtmxUsername).getValue();
		password = preferences.getParameter(MZminePreferences.vtmxPassword).getValue();
		pid      = preferences.getParameter(MZminePreferences.vtmxProject).getValue();
		
		if ((username == null) || username.isEmpty() || (password == null) || password.isEmpty())
		{
			if (preferences.showSetupDialog(MZmineCore.getDesktop().getMainWindow(), false) != ExitCode.OK)
				return;
			username = preferences.getParameter(MZminePreferences.vtmxUsername).getValue();
			password = preferences.getParameter(MZminePreferences.vtmxPassword).getValue();
			pid      = preferences.getParameter(MZminePreferences.vtmxProject).getValue();
		}

		// save the raw data file
		rawDataFile = raw;

		// figure out if this a new job (launch) or not (retrieval)
		launch     = (pickup_job == null);
		targetName = target;

		// make sure we have access to the Veritomyx Server
		// this also gets the job_id and SFTP credentials
		vtmx = new PeakInvestigatorSaaS(MZmineCore.VtmxLive);
		int status = 0;
		while (true)
		{
			status = vtmx.init(username, password, pid, pickup_job, scanCount, minMass, maxMass);
			if (status > 0)
				break;

			desc = vtmx.getPageStr();
			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", desc, logger);
			if ((status != PeakInvestigatorSaaS.W_ERROR_LOGIN) && (status != PeakInvestigatorSaaS.W_ERROR_PID))
				return;

			if (preferences.showSetupDialog(MZmineCore.getDesktop().getMainWindow(), false) != ExitCode.OK)
				return;
			username = preferences.getParameter(MZminePreferences.vtmxUsername).getValue();
			password = preferences.getParameter(MZminePreferences.vtmxPassword).getValue();
			pid      = preferences.getParameter(MZminePreferences.vtmxProject).getValue();
		}

		if (!launch && (status <= 0))
		{
			desc = vtmx.getPageStr();
			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", desc, logger);
			return;
		}

		jobID          = vtmx.getJobID();
		intputFilename = jobID + ".scans.tar";
		outputFilename = jobID + ".vcent.tar";
	}
	
	public String getDesc() { return desc; }

	/**
	 * Return the name of this task
	 * 
	 * @return
	 */
	public String getName() { return jobID; }

	/**
	 * Start the process
	 * @return 
	 */
	public void start()
	{
		logger.finest("PeakInvestigatorTask - start");
		if (launch) startLaunch();
		else        startRetrieve();
	}

	/**
	 * Compute the peaks list for the given scan
	 * 
	 * @param scan
	 * @param selected
	 * @return
	 */
	public DataPoint[] processScan(Scan scan)
	{
		int scan_num = scan.getScanNumber();
		logger.finest("PeakInvestigatorTask - processScan " + scan_num);
		DataPoint[] peaks;
		if (launch) peaks = processScanLaunch(scan_num, scan);
		else        peaks = processScanRetrieve(scan_num);		// ignore selected flag on retrieval
		return peaks;
	}

	/**
	 * Send the bundle of scans to the VTMX cloud processor via the SFTP drop
	 */
	public void finish()
	{
		logger.finest("PeakInvestigatorTask - finish");
		if (launch) try { finishLaunch(); } catch(InterruptedException ie) {;}
		else        finishRetrieve();
	}

	private void startLaunch()
	{
		desc = "starting launch";
		logger.info("Preparing to launch new job, " + jobID);
		scanCnt = 0;
		try {
			tarfile = new TarOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(intputFilename))));
		} catch (IOException e) {
			logger.finest(e.getMessage());
			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", "Cannot create scans bundle file", logger);
			jobID = null;
		}
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
			scan.exportToFile("", "", filename);

			// put the exported scan into the tar file
			File f = new File(filename);
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

	/**
	 * Finish the job launch process and send job to VTMX SaaS server
	 */
	private void finishLaunch() throws InterruptedException
	{
		desc = "finishing launch";
		try {
			tarfile.close();
		} catch (IOException e) {
			logger.finest(e.getMessage());
			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", "Cannot close scans bundle file.", logger);
		}
		logger.info("Transmit scans bundle, " + intputFilename + ", to SFTP server...");
		vtmx.putFile(intputFilename);

		//####################################################################
		// Prepare for remote job in a loop as it might take some time to analyze
		logger.info("Awaiting PREP analysis, " + intputFilename + ", on SaaS server...");
		int prep_ret = vtmx.getPagePrep(scanCnt);
		prep_status_type prep_status = vtmx.getPrepStatus();

		// timeWait is based on the loop count to keep this as short as possible.
		long timeWait = minutesTimeoutPrep;
		while(prep_ret == PeakInvestigatorSaaS.W_PREP && prep_status == prep_status_type.PREP_ANALYZING && timeWait > 0) {
			logger.info("Waiting for PREP analysis to complete, " + intputFilename + ", on SaaS server...Please be patient.");
			Thread.sleep(minutesCheckPrep * 60000);
			prep_ret = vtmx.getPagePrep(scanCnt);
			prep_status = vtmx.getPrepStatus();
			timeWait -= minutesCheckPrep;
		}
		if(prep_ret != PeakInvestigatorSaaS.W_PREP || prep_status != prep_status_type.PREP_READY) {
			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", "Failed to launch or complete(PREP Phase) " + jobID, logger);
			return;
		}
			
		//####################################################################
		// start for remote job
		logger.info("Launch job (RUN), " + jobID + ", on cloud server...");
		if (vtmx.getPageRun(scanCnt) < PeakInvestigatorSaaS.W_RUNNING)
		{
			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", "Failed to launch (RUN Phase) " + jobID, logger);
			return;
		}

		// job was started - record it
		logger.info("Job, " + jobID + ", launched");

		rawDataFile.addJob("job-" + jobID, rawDataFile, targetName, vtmx);	// record this job start
		logger.finest(vtmx.getPageStr());
		File f = new File(intputFilename);
		f.delete();			// remove the local copy of the tar file
		desc = "launch finished";
	}

	/**
	 * Start the retrieval of the job
	 * Wait for job to finish in cloud and pickup the results
	 */
	private void startRetrieve()
	{
		errors = 0;
		desc = "checking for results";
		int status;
		logger.info("Checking previously launched job, " + jobID);

		// see if remote job is complete
		if ((status = vtmx.getPageStatus()) == PeakInvestigatorSaaS.W_RUNNING)
		{
			desc = "Remote job not complete";
			logger.info("Remote job, " + jobID + ", not complete");
			MZmineCore.getDesktop().displayMessage(MZmineCore.getDesktop().getMainWindow(), "Warning", "Remote job not complete. Please try again later.", logger);
			errors++;
			return;
		}
		desc = "downloading results";
		logger.info("Downloading job, " + jobID + ", results...");
		if (status != PeakInvestigatorSaaS.W_DONE)
		{
			desc = "Error";
			logger.info(vtmx.getPageStr());
			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", vtmx.getPageStr(), logger);
			errors++;
			return;
		}
		
		// check to see if the results were complete.
		// this will be shown in the string returned from the status call to the web.
		
		int valid = vtmx.getScansComplete();
		int scans = vtmx.getScansInput();
		if (valid < scans)
		{
			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", "Only " + valid + " of " + scans + " scans were successful.\n" +
																"The valid results will be loaded now.\n" + 
																"You have been credited for the incomplete scans.", logger);
		}

		// Get the SFTP data for the completed job 
		vtmx.getPageSftp();
		outputFilename = vtmx.getResultsFilename();
		// read the results tar file and extract all the peak list files
		logger.info("Reading centroided data, " + outputFilename + ", from SFTP drop...");
		vtmx.getFile(outputFilename);
		{
			TarInputStream tis = null;
			FileOutputStream outputStream = null;
			try {
				tis = new TarInputStream(new GZIPInputStream(new FileInputStream(outputFilename)));
				TarEntry tf;
				int bytesRead;
				byte buf[] = new byte[1024];
				while ((tf = tis.getNextEntry()) != null)
				{
					if (tf.isDirectory()) continue;
					logger.info("Reading peaks data to " + tf.getName() + " - " + tf.getSize() + " bytes");
					outputStream = new FileOutputStream(tf.getName());
					while ((bytesRead = tis.read(buf, 0, 1024)) > -1)
						outputStream.write(buf, 0, bytesRead);
					outputStream.close();
				}
				tis.close();
				File f = new File(outputFilename);
				f.delete();			// remove the local copy of the results tar file
			} catch (Exception e1) {
				logger.finest(e1.getMessage());
				MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", "Cannot parse results file", logger);
				errors++;
				e1.printStackTrace();
			} finally {
				try { tis.close(); } catch (Exception e) {}
				try { outputStream.close(); } catch (Exception e) {}
			}
		}
		desc = "results downloaded";
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
		String pfilename = "scan_" + String.format("%04d", scan_num) + ".vcent.txt";
		logger.info("Parsing peaks data from " + pfilename);
		try
		{
			File centfile = new File(pfilename);
			FileChecksum fchksum = new FileChecksum(centfile);
			if (!fchksum.verify(false))
				throw new IOException("Invalid checksum");
	
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
			centfile.delete();	// delete the temporary results peaks list file
		}
		catch (FileNotFoundException e) { /* expect some scans might not be included in original processing */ }
		catch (Exception e)
		{
			logger.finest(e.getMessage());
			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", "Cannot parse peaks file, " + pfilename + " (" + e.getMessage() + ")", logger);
		}

		desc = "scan " + scan_num + " parsed";
		return (mzPeaks == null) ? null : mzPeaks.toArray(new DataPoint[0]);
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
		vtmx.getPageDone();
		rawDataFile.removeJob(jobID);
		desc = "retrieve finished";
		MZmineCore.getDesktop().displayMessage(MZmineCore.getDesktop().getMainWindow(), "Warning", "PeakInvestigator results successfully downloaded.\n" + 
											"All your job files will now be deleted from the Veritomyx servers.\n" +
											"Remember to save your project before closing MZminePI.", logger);
	}

}