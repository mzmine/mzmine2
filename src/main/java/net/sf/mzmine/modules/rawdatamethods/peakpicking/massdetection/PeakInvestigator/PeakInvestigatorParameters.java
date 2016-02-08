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
import java.util.Arrays;
import java.util.List;

import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.PiVersionsAction;

import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.MassDetectorSetupDialog;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.ComboParameter;
import net.sf.mzmine.parameters.parametertypes.IntegerParameter;
import net.sf.mzmine.parameters.parametertypes.BooleanParameter;
import net.sf.mzmine.util.ExitCode;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.datamodel.Scan;
import net.sf.mzmine.desktop.preferences.MZminePreferences;

public class PeakInvestigatorParameters extends SimpleParameterSet
{
	
	private static String username;
	private static String password;
	private static String server;

	public static final ComboParameter<String> versions = new ComboParameter<String>(
			"PeakInvestigator™ version",
			"The PeakInvestigator version to use for the analysis.",
			new String[] { "lastUsed" });
	public static final IntegerParameter minMass = new IntegerParameter(
		    "Minimum m/z",
		    "The minimum nominal m/z in the data to be used for analysis.",
		    0);
	public static final IntegerParameter maxMass = new IntegerParameter(
		    "Maximum m/z",
		    "The maximum nominal m/z in the data to be used for analysis.",
		    Integer.MAX_VALUE);

	public static final BooleanParameter showLog = new BooleanParameter(
			"Display Job Log",
			"Check this if you want to display the PeakInvestigator job log when retrieving results");

	public PeakInvestigatorParameters()
	{
		super(new Parameter[] { versions, minMass, maxMass, showLog });
		versions.setValue("lastUsed");
	}

	public ExitCode showSetupDialog(Window parent, boolean valueCheckRequired)
	{
		Integer maxMasses = 0, minMasses = Integer.MAX_VALUE;

		if(!getCredentialsFromPreferences()) {
			return ExitCode.CANCEL;
		}

		PiVersionsAction action = null;
		try {
			action = performPiVersionsCall();
		} catch (ResponseFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(action == null) {
			return ExitCode.ERROR;
		}

		versions.setChoices(formatPiVersions(action));

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
	 * Convenience function to create a list of versions that identifies which,
	 * if any, are the current versions and previously used versions.
	 * 
	 * @param action
	 *            Valid PiVersionsAction that contains list of versions.
	 * @return List of versions with identifications appended
	 */
	protected static String[] formatPiVersions(PiVersionsAction action) {
		List<String> choices = Arrays.asList(action.getVersions());
		int currentIndex = choices.indexOf((String) action.getCurrentVersion());
		int lastIndex = choices.indexOf((String) action.getLastUsedVersion());
		if (currentIndex >= 0 && lastIndex == currentIndex) {
			String newString = choices.get(currentIndex)
					+ " (current and last used)";
			choices.set(currentIndex, newString);
		} else {
			if (currentIndex >= 0) {
				String newString = choices.get(currentIndex) + " (current)";
				choices.set(currentIndex, newString);
			}
			if (lastIndex >= 0) {
				String newString = choices.get(lastIndex) + " (last used)";
				choices.set(lastIndex, newString);
			}
		}

		return choices.toArray(new String[choices.size()]);
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
		server = preferences.getParameter(MZminePreferences.vtmxServer)
				.getValue();
		int projectID = preferences.getParameter(MZminePreferences.vtmxProject)
				.getValue();

		if ((username == null) || username.isEmpty() || (password == null)
				|| password.isEmpty() || (server == null) || server.isEmpty()) {
			if (preferences.showSetupDialog(MZmineCore.getDesktop()
					.getMainWindow(), false) != ExitCode.OK)
				return false;
			username = preferences.getParameter(MZminePreferences.vtmxUsername)
					.getValue();
			password = preferences.getParameter(MZminePreferences.vtmxPassword)
					.getValue();
			server = preferences.getParameter(MZminePreferences.vtmxServer)
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
	protected PiVersionsAction performPiVersionsCall() throws ResponseFormatException {
		PeakInvestigatorSaaS webService = new PeakInvestigatorSaaS(server);
		PiVersionsAction action = new PiVersionsAction(
				PeakInvestigatorSaaS.API_VERSION, username, password);

		String response = webService.executeAction(action);
		action.processResponse(response);
		if(!action.isReady("PI_VERSIONS")) {
			return null;
		}

		while(action.hasError()) {
			String errorMessage = action.getErrorMessage();
			long code = action.getErrorCode();
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
			int projectID = preferences.getParameter(MZminePreferences.vtmxProject).getValue();

			action = new PiVersionsAction(PeakInvestigatorSaaS.API_VERSION,
					username, password);
			response = webService.executeAction(action);
			action.processResponse(response);
		}

		return action;
	}

	public String getPiVersion() {
		return versions.getValue();
	}

	public int[] getMassRange() {
		return new int[] { minMass.getValue(), maxMass.getValue() };
	}

	public boolean shouldDisplayLog() {
		return showLog.getValue();
	}
}