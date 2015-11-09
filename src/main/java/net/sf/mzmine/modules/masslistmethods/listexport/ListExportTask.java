/*
 * Copyright 2006-2012 The MZmine 2 Development Team
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
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.masslistmethods.listexport;

import java.util.logging.Logger;

import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;

/**
 *
 */
public class ListExportTask extends AbstractTask
{
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private RawDataFile dataFile;

	// scan counter
	private int processedScans = 0, totalScans;
	private int[] scanNumbers;

	// User parameters
	private boolean dumpScans;
	private boolean dumpPeaks;
	private String  massListName;
	private String  saveDirectory;
	
	/**
	 * @param dataFile
	 * @param parameters
	 */
	public ListExportTask(RawDataFile dataFile, ParameterSet parameters)
	{
		this.dataFile = dataFile;
		dumpScans     = parameters.getParameter(ListExportParameters.dumpScans).getValue();
		massListName  = parameters.getParameter(ListExportParameters.massList).getValue();
		saveDirectory = parameters.getParameter(ListExportParameters.saveDirectory).getValue().getPath();
		dumpPeaks = (massListName.isEmpty() == false);
	}

	/**
	 * @see net.sf.mzmine.taskcontrol.Task#getTaskDescription()
	 */
	public String getTaskDescription()
	{
		return "Scans and/or peaks(mass) list export of " + dataFile.getName();
	}

	/**
	 * @see net.sf.mzmine.taskcontrol.Task#getFinishedPercentage()
	 */
	public double getFinishedPercentage()
	{
		return (totalScans == 0) ? 0 : (double) processedScans / totalScans;
	}

	public RawDataFile getDataFile()
	{
		return dataFile;
	}

	/**
	 * @see Runnable#run()
	 */
	public void run()
	{
		setStatus(TaskStatus.PROCESSING);
		logger.info("Export of " + (dumpScans ? "Scans" : "") + ((dumpScans && dumpPeaks) ? " and " : "") +
                    (dumpPeaks ? "peaks(mass) list of " + massListName : "") + " from " + dataFile);

		int scansWithMassList = 0;
		scanNumbers = dataFile.getScanNumbers();
		totalScans  = scanNumbers.length;
		// Process all scans
		for (int s = 0; s < totalScans; s++)
		{
			if (isCanceled())
				return;

			Scan scan = dataFile.getScan(scanNumbers[s]);
			if (dumpScans)
				scan.exportToFile("", saveDirectory, "");

			if (dumpPeaks)
				scansWithMassList += (scan.exportToFile(massListName, saveDirectory, "") > 0) ? 1 : 0;

			processedScans++;
		}

		if (dumpPeaks && (scansWithMassList == 0))
		{
			setStatus(TaskStatus.ERROR);
			this.setErrorMessage(dataFile.getName() + " has no mass list called '" + massListName + "'");
		}
		else
			setStatus(TaskStatus.FINISHED);
		logger.info("Finished list export from " + dataFile);
	}

	public Object[] getCreatedObjects()
	{
		return null;
	}
}