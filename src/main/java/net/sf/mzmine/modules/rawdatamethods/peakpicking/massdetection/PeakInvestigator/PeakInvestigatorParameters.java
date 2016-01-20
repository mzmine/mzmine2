/*
 * Copyright 2013-2014 The Veritomyx
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

import java.awt.Window;
import java.lang.Math;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.MassDetectorSetupDialog;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.IntegerParameter;
import net.sf.mzmine.parameters.parametertypes.BooleanParameter;
import net.sf.mzmine.util.ExitCode;
import ucar.ma2.Range;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;

public class PeakInvestigatorParameters extends SimpleParameterSet
{
	
	public static final IntegerParameter minMass = new IntegerParameter(
		    "Min Mass",
		    "The minimum mass in the set of masses to send to the Peak Investigator SaaS.",
		    0);
	public static final IntegerParameter maxMass = new IntegerParameter(
		    "Max Mass",
		    "The maximum mass in the set of masses to send to the Peak Investigator SaaS.",
		    Integer.MAX_VALUE);
	
	public static final BooleanParameter showLog = new BooleanParameter("Display Job Log", "Check this if you wan to display the Peak Investigator job log when retrieving results");
	public PeakInvestigatorParameters()
	{
		super(new Parameter[] { minMass, maxMass, showLog });
	}

	public ExitCode showSetupDialog(Window parent, boolean valueCheckRequired)
	{
		Integer maxMasses = 0, minMasses = Integer.MAX_VALUE;
		
		RawDataFile[] files = MZmineCore.getProjectManager().getCurrentProject().getDataFiles();
		for(RawDataFile file : files) {
			int scanCount = file.getNumOfScans();
			for(int scanNum = 1; scanNum <= scanCount; scanNum++) {
				Scan scan = file.getScan(scanNum);
				int n,x;
				x = (int)Math.ceil(scan.getDataPointMZRange().upperEndpoint());
				maxMasses = Integer.max(x, maxMasses);
				n = (int)Math.floor(scan.getDataPointMZRange().lowerEndpoint());
				minMasses = Integer.min(n, minMasses);
			}
		}
		minMass.setMinMax(minMasses, maxMasses-1);
		maxMass.setMinMax(minMasses+1, maxMasses);
		showLog.setValue(true);
		MassDetectorSetupDialog dialog = new MassDetectorSetupDialog(parent, valueCheckRequired, PeakInvestigatorDetector.class, this);
		dialog.setVisible(true);
		return dialog.getExitCode();
	}
}