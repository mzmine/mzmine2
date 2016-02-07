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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.veritomyx.actions.BaseAction.ResponseFormatException;

import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.desktop.preferences.MZminePreferences;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.MassDetector;
import net.sf.mzmine.parameters.ParameterSet;

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
		String jobName = filterJobName(orig);
		String target  = filterTargetName(orig);
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
	 * Return target name and filter out possible job name from "|job-########-#####[target]"
	 * 
	 * @param compoundName
	 * @return
	 */
	public String filterTargetName(String compoundName)
	{
		if (compoundName.startsWith("|job-"))
			return compoundName.substring(compoundName.indexOf('[') + 1, compoundName.indexOf(']'));
		return compoundName;
	}

	/**
	 * Return job name and filter out target name from "|job[target]"
	 * 
	 * @param compoundName
	 * @return
	 */
	private String filterJobName(String compoundName)
	{
		if (compoundName.startsWith("|job-"))
			return compoundName.substring(5, compoundName.indexOf('['));
		return null;
	}

	/**
	 * Create a new job task from the given parameters
	 * 
	 * @param raw
	 * @param targetName
	 * @param parameters
	 * @param scanCount
	 * @return
	 */
	public String startMassValuesJob(RawDataFile raw, String name,
			ParameterSet parameterSet, int scanCount) {
		MZminePreferences preferences = MZmineCore.getConfiguration()
				.getPreferences();
		String server = preferences.getParameter(MZminePreferences.vtmxServer)
				.getValue();
		String username = preferences.getParameter(
				MZminePreferences.vtmxUsername).getValue();
		String password = preferences.getParameter(
				MZminePreferences.vtmxPassword).getValue();
		int projectID = preferences.getParameter(MZminePreferences.vtmxProject)
				.getValue();

		PeakInvestigatorTask job = new PeakInvestigatorTask(server, username,
				password, projectID).withRawDataFile(raw);

		PeakInvestigatorParameters parameters = (PeakInvestigatorParameters) parameterSet;
		try {
			job.initialize(parameters.getPiVersion(), scanCount,
					parameters.getMassRange(), filterTargetName(name));
		} catch (IllegalStateException | ResponseFormatException e) {
			// TODO Auto-generated catch block
			error(e.getMessage());
			e.printStackTrace();
		}

		String job_name = job.getName();
		logger.finest("startMassValuesJob " + filterJobName(name) + " - "
				+ job_name + " - " + ((job != null) ? job.getDesc() : "nojob"));
		if (job_name != null) {
			jobs.add(job);
			job.start();
		}
		return job_name;
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