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

import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.actions.PiVersionsAction;

import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.MassDetectorSetupDialog;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.ComboParameter;
import net.sf.mzmine.parameters.parametertypes.IntegerParameter;
import net.sf.mzmine.parameters.parametertypes.BooleanParameter;
import net.sf.mzmine.util.ExitCode;
import ucar.ma2.Range;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.desktop.preferences.MZminePreferences;

public class PeakInvestigatorParameters extends SimpleParameterSet
{
	
	private static String username;
	private static String password;
	private static int projectID;
	private PeakInvestigatorSaaS webService = new PeakInvestigatorSaaS(MZmineCore.VtmxLive);

	public static final ComboParameter<String> versions = new ComboParameter<String>(
			"PeakInvestigator™ version",
			"The PeakInvestigator version to use for the analysis.",
			new String[] {});
	public static final IntegerParameter minMass = new IntegerParameter(
		    "Min Mass",
		    "The minimum mass in the set of masses to send to the PeakInvestigator SaaS.",
		    0);
	public static final IntegerParameter maxMass = new IntegerParameter(
		    "Max Mass",
		    "The maximum mass in the set of masses to send to the PeakInvestigator SaaS.",
		    Integer.MAX_VALUE);
	
	public static final BooleanParameter showLog = new BooleanParameter("Display Job Log", "Check this if you wan to display the PeakInvestigator job log when retrieving results");
	public PeakInvestigatorParameters()
	{
		super(new Parameter[] { versions, minMass, maxMass, showLog });
	}

	public ExitCode showSetupDialog(Window parent, boolean valueCheckRequired)
	{
		Integer maxMasses = 0, minMasses = Integer.MAX_VALUE;

		if(!getCredentialsFromPreferences()) {
			return ExitCode.CANCEL;
		}

		PiVersionsAction action = performPiVersionsCall();
		if(action == null) {
			return ExitCode.ERROR;
		}

		versions.setChoices(action.getVersions());

		RawDataFile[] files = MZmineCore.getProjectManager().getCurrentProject().getDataFiles();
		for(RawDataFile file : files) {
		        int[] scanNumbers = file.getScanNumbers();
			for(int scanNum : scanNumbers) {
				Scan scan = file.getScan(scanNum);
				int n,x;
				x = (int)Math.ceil(scan.getDataPointMZRange().upperEndpoint());
				maxMasses = Math.max(x, maxMasses);
				n = (int)Math.floor(scan.getDataPointMZRange().lowerEndpoint());
				minMasses = Math.min(n, minMasses);
			}
		}
		minMass.setMinMax(minMasses, maxMasses-1);
		maxMass.setMinMax(minMasses+1, maxMasses);
		showLog.setValue(true);
		MassDetectorSetupDialog dialog = new MassDetectorSetupDialog(parent, valueCheckRequired, PeakInvestigatorDetector.class, this);
		dialog.setVisible(true);
		return dialog.getExitCode();
	}

	/**
	 * Get credentials stored in preferences. If the credentials are not valid,
	 * show a dialog.
	 * 
	 * @return false if credentials are not valid, and user doesn't set new
	 *         valid ones; otherwise, true.
	 */
	protected boolean getCredentialsFromPreferences() {
		// pickup all the parameters
		MZminePreferences preferences = MZmineCore.getConfiguration()
				.getPreferences();
		username = preferences.getParameter(MZminePreferences.vtmxUsername)
				.getValue();
		password = preferences.getParameter(MZminePreferences.vtmxPassword)
				.getValue();
		projectID = preferences.getParameter(MZminePreferences.vtmxProject)
				.getValue();

		if ((username == null) || username.isEmpty() || (password == null)
				|| password.isEmpty()) {
			if (preferences.showSetupDialog(MZmineCore.getDesktop()
					.getMainWindow(), false) != ExitCode.OK)
				return false;
			username = preferences.getParameter(MZminePreferences.vtmxUsername)
					.getValue();
			password = preferences.getParameter(MZminePreferences.vtmxPassword)
					.getValue();
			projectID = preferences.getParameter(MZminePreferences.vtmxProject)
					.getValue();
		}

		return true;
	}

	/**
	 * Make a call into PI_VERSIONS API to have a list of PeakInvestigator
	 * versions available. This also checks that credentials are correct.
	 * 
	 * @return An object containing response from server, or null if credentials
	 *         are wrong and not corrected by the user.
	 */
	protected PiVersionsAction performPiVersionsCall() {
		PiVersionsAction action = new PiVersionsAction(
				PeakInvestigatorSaaS.API_VERSION, username, password);
		webService.executeAction(action);
		if(!action.isReady("PI_VERSIONS")) {
			return null;
		}

		while(action.hasError()) {
			String errorMessage = action.getErrorMessage();
			int code = action.getErrorCode();
			MZmineCore.getDesktop().displayErrorMessage(
					MZmineCore.getDesktop().getMainWindow(), "Error",
					errorMessage);
			if ((code != PeakInvestigatorSaaS.W_ERROR_LOGIN) && (code != PeakInvestigatorSaaS.W_ERROR_PID))
				return null;

			MZminePreferences preferences = MZmineCore.getConfiguration()
					.getPreferences();
			if (preferences.showSetupDialog(MZmineCore.getDesktop().getMainWindow(), false) != ExitCode.OK)
				return null;
			username = preferences.getParameter(MZminePreferences.vtmxUsername).getValue();
			password = preferences.getParameter(MZminePreferences.vtmxPassword).getValue();
			projectID      = preferences.getParameter(MZminePreferences.vtmxProject).getValue();

			action = new PiVersionsAction(PeakInvestigatorSaaS.API_VERSION,
					username, password);
		}

		return action;
	}
}