/*
 * Copyright 2006-2015 The MZmine 2 Development Team
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

package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection;

import java.util.logging.Logger;

import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.datamodel.impl.SimpleMassList;
import net.sf.mzmine.modules.MZmineProcessingStep;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.parametertypes.selectors.ScanSelection;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;

public class MassDetectionTask extends AbstractTask {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private final RawDataFile dataFile;

    // scan counter
    private int processedScans = 0, totalScans = 0;
    private final ScanSelection scanSelection;

    // User parameters
    private String name;

    // Mass detector
    private MZmineProcessingStep<MassDetector> massDetector;

    /**
     * @param dataFile
     * @param parameters
     */
    public MassDetectionTask(RawDataFile dataFile, ParameterSet parameters) {

        this.dataFile = dataFile;

        this.massDetector = parameters.getParameter(
                MassDetectionParameters.massDetector).getValue();

        this.scanSelection = parameters.getParameter(
                MassDetectionParameters.scanSelection).getValue();

        this.name = parameters.getParameter(MassDetectionParameters.name)
                .getValue();

    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getTaskDescription()
     */
    public String getTaskDescription() {
        return massDetector.getModule().getDescription(name, "Detecting masses in " + dataFile);
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getFinishedPercentage()
     */
    public double getFinishedPercentage() {
        if (totalScans == 0)
            return 0;
        else
            return (double) processedScans / totalScans;
    }

    public RawDataFile getDataFile() {
        return dataFile;
    }

    /**
     * @see Runnable#run()
     */
    public void run() {

        setStatus(TaskStatus.PROCESSING);

        logger.info("Started mass detector on " + dataFile);

        MassDetector detector = massDetector.getModule();
        
        final Scan scans[] = scanSelection.getMatchingScans(dataFile);
        totalScans = scans.length;  

        
        // start the job
     		String job = detector.startMassValuesJob(dataFile, name, massDetector.getParameterSet(), scans.length);
     		name       = detector.filterTargetName(name);	// get the target name, the detector may change it
 
        // Process scans one by one
        for (Scan scan : scans) {

            if (isCanceled())
                return;

            
            DataPoint mzPeaks[] = detector.getMassValues(scan, job, massDetector.getParameterSet());

            if (mzPeaks != null)
		    {
            	SimpleMassList newMassList = new SimpleMassList(name, scan, mzPeaks);
		   
            	// Add new mass list to the scan
            	scan.addMassList(newMassList);
		    }
            processedScans++;
        }
        
        // finish the job
     	detector.finishMassValuesJob(job);
        
     	setStatus(TaskStatus.FINISHED);

        logger.info("Finished mass detector on " + dataFile);

    }

}
