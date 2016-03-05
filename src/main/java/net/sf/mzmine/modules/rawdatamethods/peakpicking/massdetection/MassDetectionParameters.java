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

import java.awt.Window;

import net.sf.mzmine.datamodel.MassSpectrumType;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.datamodel.impl.RemoteJob;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.PeakInvestigatorDetector;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.centroid.CentroidMassDetector;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.exactmass.ExactMassDetector;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.localmaxima.LocalMaxMassDetector;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.recursive.RecursiveMassDetector;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.wavelet.WaveletMassDetector;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.ModuleComboParameter;
import net.sf.mzmine.parameters.parametertypes.StringParameter;
import net.sf.mzmine.parameters.parametertypes.selectors.RawDataFilesParameter;
import net.sf.mzmine.parameters.parametertypes.selectors.RawDataFilesSelectionType;
import net.sf.mzmine.parameters.parametertypes.selectors.ScanSelection;
import net.sf.mzmine.parameters.parametertypes.selectors.ScanSelectionParameter;
import net.sf.mzmine.parameters.parametertypes.selectors.RawDataFilesSelection;
import net.sf.mzmine.util.ExitCode;

public class MassDetectionParameters extends SimpleParameterSet {

    public static final MassDetector massDetectors[] = {
            new CentroidMassDetector(),
            new ExactMassDetector(),
            new LocalMaxMassDetector(),
            new RecursiveMassDetector(),
            new WaveletMassDetector(),
            new PeakInvestigatorDetector()
    };

    public static final RawDataFilesParameter dataFiles = new RawDataFilesParameter();

    public static final ScanSelectionParameter scanSelection = new ScanSelectionParameter(
            new ScanSelection(1));

    public static final ModuleComboParameter<MassDetector> massDetector = new ModuleComboParameter<MassDetector>(
            "Mass detector",
            "Algorithm to use for mass detection and its parameters",
            massDetectors);

    public static final StringParameter name = new StringParameter(
            "Mass list name",
            "Name of the new mass list. If the processed scans already have a mass list of that name, it will be replaced.",
            "masses");

    public MassDetectionParameters() {
        super(new Parameter[] { dataFiles, scanSelection, massDetector, name });
    }

    @Override
    public ExitCode showSetupDialog(Window parent, boolean valueCheckRequired) {

        ExitCode exitCode = super.showSetupDialog(parent, valueCheckRequired);

        // If the parameters are not complete, let's just stop here
        if (exitCode != ExitCode.OK)
            return exitCode;

        // Do an additional check for centroid/continuous data and show a
        // warning if there is a potential problem
        boolean centroidData = false;
        ScanSelection scanSel = getParameter(scanSelection).getValue();
        RawDataFile selectedFiles[] = getParameter(dataFiles).getValue()
                .getMatchingRawDataFiles();

        // If no file selected (e.g. in batch mode setup), just return
        if ((selectedFiles == null) || (selectedFiles.length == 0))
            return exitCode;

        for (RawDataFile file : selectedFiles) {
            Scan scans[] = scanSel.getMatchingScans(file);
            for (Scan s : scans) {
                if (s.getSpectrumType() == MassSpectrumType.CENTROIDED)
                    centroidData = true;
            }
        }

        // Check the selected mass detector
        String massDetectorName = getParameter(massDetector).getValue()
                .toString();

        if ((centroidData) && (!massDetectorName.startsWith("Centroid"))) {
            String msg = "One or more selected files contains centroided data points. The selected mass detector could give unexpected results.";
            MZmineCore.getDesktop().displayMessage(null, msg);
        }

        if ((!centroidData) && (massDetectorName.startsWith("Centroid"))) {
            String msg = "None one of the selected files contain centroided data points. The selected mass detector could give unexpected results.";
            MZmineCore.getDesktop().displayMessage(null, msg);
        }

        return exitCode;

    }
    
    /**
     * Allow setting the name externally
     * 
     * @param n
     */
    public void setName(String n)
    {
    	name.setValue(n);
    }
    
    /**
     * Setup parameters based on the given raw file and job or, if no job, open a dialog
     * 
     * @param raw
     * @param job
     * @return 
     */
    public ExitCode setJobParams(RawDataFile raw, RemoteJob job)
    {
		ExitCode ret = ExitCode.OK;
		if (job != null) // set Veritomyx parameters
		{
			raw = job.getRawDataFile();
			massDetector.setValueToVeritomxy();
			// encode the job into the name field so to flag as retrieval
			name.setValue(job.getCompoundName());
		}

    	RawDataFile[] newValue = new RawDataFile[1];
    	newValue[0] = raw;
    	RawDataFilesSelection newSelection = new RawDataFilesSelection();
    	newSelection.setSpecificFiles(newValue);
        newSelection.setSelectionType(RawDataFilesSelectionType.SPECIFIC_FILES);
    	dataFiles.setValue(newSelection);
    
    	if (job == null)	// bring up the dialog if all parameters were not set
    		ret = this.showSetupDialog(MZmineCore.getDesktop().getMainWindow(), true);
    	return ret;
    }

}
