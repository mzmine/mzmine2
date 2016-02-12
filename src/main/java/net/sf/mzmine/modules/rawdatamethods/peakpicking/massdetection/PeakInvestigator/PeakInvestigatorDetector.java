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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.veritomyx.VeritomyxSettings;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.PiVersionsAction;

import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.datamodel.impl.RemoteJob;
import net.sf.mzmine.desktop.preferences.MZminePreferences;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.MassDetector;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.PeakInvestigatorTask.ResponseErrorException;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.opensftp.SftpException;

public class PeakInvestigatorDetector implements MassDetector
{
	private Logger logger;	
	private ArrayList<PeakInvestigatorTask> jobs;
	private String desc;

	public PeakInvestigatorDetector()
	{
		logger = Logger.getLogger(this.getClass().getName());
		logger.setLevel(MZmineCore.VtmxLive ? Level.INFO : Level.FINEST);
		logger.info("Initializing Veritomyx " + this.getName());
		jobs = new ArrayList<PeakInvestigatorTask>();
	}

	public String getName() { return "PeakInvestigator™"; }

	public String getDescription(String orig, String str)
	{
		String jobName = RemoteJob.filterJobName(orig);
		String target  = RemoteJob.filterTargetName(orig);
		desc = "target: " + target + "; ";
		PeakInvestigatorTask job = null;
		if (jobName == null)
		{
			desc = "Preparing and transmitting scans to/from " + this.getName() + " - " + str;
		}
		else
		{
			job = getJobFromName(jobName);
			String word = (job == null) ? "Processing results" : job.getDesc();
			desc = jobName + " " + word + " from " + this.getName();
		}
		//debug("getDescription", desc);
		return(desc);
	}

	@Override
	public Class<? extends ParameterSet> getParameterSetClass() { return PeakInvestigatorParameters.class; }

	/**
	 * Given a compound name that identifies a job and its target (i.e. future
	 * mass list), return the target.
	 * 
	 * @param compoundName
	 *            Takes the form "|job-####-###[target]
	 * @return target
	 */
	public String filterTargetName(String compoundName)
	{
		return RemoteJob.filterTargetName(compoundName);
	}

	/**
	 * Create a new job task from the given parameters
	 * 
	 * @param raw
	 * @param name
	 *            When launching job, the name of the mass list after
	 *            centroiding. When retrieving job, name of job plus mass list
	 *            (e.g. |job-C-1022.1483[PI]).
	 * @param parameters
	 * @param scanCount
	 * @return
	 */
	public String startMassValuesJob(RawDataFile raw, String name,
			ParameterSet parameterSet, int scanCount) {

		MZminePreferences preferences = MZmineCore.getConfiguration()
				.getPreferences();
		VeritomyxSettings settings = preferences.getVeritomyxSettings();

		PeakInvestigatorTask job = new PeakInvestigatorTask(settings.server,
				settings.username, settings.password, settings.projectID)
				.withRawDataFile(raw);

		PeakInvestigatorParameters parameters = (PeakInvestigatorParameters) parameterSet;
		try {

			// not only does this get versions, it validates credentials
			String selectedPiVersion = selectPiVersion(parameters,
					preferences);

			if (!name.startsWith("|")) {
				int[] massRange = parameters.getMassRange();
				logger.info(String.format(
						"Starting analysis on mass range %d - %d.",
						massRange[0], massRange[1]));
				job.initializeSubmit(selectedPiVersion, scanCount,
						parameters.getMassRange(), filterTargetName(name));
			} else {
				logger.info("Checking status of job.");
				job.initializeFetch(name, parameters.shouldDisplayLog());
			}

		} catch (IllegalStateException | ResponseFormatException | ResponseErrorException e) {
			error(e.getMessage());
			e.printStackTrace();
			return null;
		}

		String job_name = job.getName();
		logger.finest("startMassValuesJob " + RemoteJob.filterJobName(name) + " - "
				+ job_name + " - " + ((job != null) ? job.getDesc() : "nojob"));

		if (job_name == null) {
			return null;
		}

		try {
			job.start();
		} catch (FileNotFoundException | ResponseFormatException | ResponseErrorException e) {
			error(e.getMessage());
			e.printStackTrace();
			return null;
		} catch (IOException | SftpException e) {
			error(e.getMessage());
			e.printStackTrace();
			return null;
		}

		jobs.add(job);

		return job_name;
	}

	public String selectPiVersion(PeakInvestigatorParameters parameters,
			MZminePreferences preferences) throws ResponseFormatException {

		String selectedPiVersion = parameters.getPiVersion();

		if (selectedPiVersion == PeakInvestigatorParameters.LAST_USED_STRING) {
			PiVersionsAction action = PeakInvestigatorParameters
					.performPiVersionsCall(preferences);
			if (!action.getLastUsedVersion().isEmpty()) {
				selectedPiVersion = action.getLastUsedVersion();
			} else {
				selectedPiVersion = action.getCurrentVersion();
			}
		}

		return selectedPiVersion;
	}

	/**
	 * Compute the peaks list for the given scan
	 * 
	 * @param scan
	 * @param parameters
	 * @param jobName
	 * @return
	 * @throws FileNotFoundException 
	 */
	public DataPoint[] getMassValues(Scan scan, String jobName, ParameterSet parameters)
	{
		// get the thread-safe job from jobs list using the jobName
		PeakInvestigatorTask job = getJobFromName(jobName);
		logger.finest("getMassValues " + jobName + " - " + ((job != null) ? job.getDesc() : "nojob"));
		if (job != null)
		{
			return job.processScan(scan);
		}
		return null;
	}

	/**
	 * Mark the job done
	 * 
	 * @param parameters
	 * @return
	 */
	public void finishMassValuesJob(String job_name)
	{
		PeakInvestigatorTask job = getJobFromName(job_name);
		logger.finest("finishMassValuesJobs " + job_name + " - " + ((job != null) ? job.getDesc() : "nojob"));
		if (job != null)
		{
			job.finish();
			jobs.remove(job);
		}
	}

	/**
	 * Retrieve the job task from a job name
	 * 
	 * @param jobName
	 * @return
	 */
	private PeakInvestigatorTask getJobFromName(String jobName)
	{
		// get the job from jobs list using the jobName
		for (PeakInvestigatorTask job:jobs)
		{
			if (job.getName().equals(jobName))
				return job;
		}
		return null;
	}

	private void error(String message) {
		MZmineCore.getDesktop().displayErrorMessage(
				MZmineCore.getDesktop().getMainWindow(), "Error", message,
				logger);
	}

}