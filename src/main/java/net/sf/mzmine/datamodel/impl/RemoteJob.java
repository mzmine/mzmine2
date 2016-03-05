/*
 * Copyright 2013-2016 Veritomyx Inc.
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

package net.sf.mzmine.datamodel.impl;

import net.sf.mzmine.datamodel.RemoteJobInfo;
import net.sf.mzmine.datamodel.RawDataFile;

/**
 * This defines a Veritomyx job
 */
public class RemoteJob implements RemoteJobInfo
{
	public enum Status { ERROR, RUNNING, DONE, DELETED };

	private String        jobID;
	private RawDataFile   rawDataFile;
	private String        futureMassList;
	
	public RemoteJob(String jobID, RawDataFile raw, String futureMassList)
	{
		this.jobID       = jobID;
		this.rawDataFile = raw;
		this.futureMassList  = futureMassList;
	}

	public String getJobID() {
		return jobID;
	}

	public String toString() {
		return jobID;
	}

	public String getCompoundName() {
		return "|" + jobID + "[" + futureMassList + "]";
	}

	/**
	 * Given a compound name that identifies a job and its target (i.e. future
	 * mass list), return the job name.
	 * 
	 * @param compoundName
	 *            Takes the form "|job-####-###[target]".
	 * @return (e.g. ####-###)
	 */
	public static String filterJobName(String compoundName) {
		if (compoundName.startsWith("|job-"))
			return compoundName.substring(5, compoundName.indexOf('['));
		return null;
	}

	/**
	 * Given a compound name that identifies a job and its target (i.e. future
	 * mass list), return the target name.
	 * 
	 * @param compoundName
	 *            Takes the form "|job-####-###[target]".
	 * @return (e.g. target)
	 */
	public static String filterTargetName(String compoundName) {
		if (compoundName.startsWith("|job-"))
			return compoundName.substring(compoundName.indexOf('[') + 1,
					compoundName.indexOf(']'));
		return compoundName;
	}

	public RawDataFile getRawDataFile() {
		return rawDataFile;
	}

	public String getFutureMassList() {
		return futureMassList;
	}

}