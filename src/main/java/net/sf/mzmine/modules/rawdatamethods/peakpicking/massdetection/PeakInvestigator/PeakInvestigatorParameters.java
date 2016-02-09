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
import com.veritomyx.VeritomyxSettings;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.PiVersionsAction;

import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.MassDetectorSetupDialog;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.ErrorDialog;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.PeakInvestigatorDefaultDialogFactory;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.PeakInvestigatorDialogFactory;
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
	private final static int BAD_CREDENTIALS_ERROR_CODE = 3;
	private static PeakInvestigatorDialogFactory dialogFactory = new PeakInvestigatorDefaultDialogFactory();

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

	public static void setDialogFactory(PeakInvestigatorDialogFactory dialogFactory) {
		PeakInvestigatorParameters.dialogFactory = dialogFactory;
	}

	public ExitCode showSetupDialog(Window parent, boolean valueCheckRequired)
	{
		Integer maxMasses = 0, minMasses = Integer.MAX_VALUE;

		PiVersionsAction action = null;
		try {
			action = performPiVersionsCall(MZmineCore.getConfiguration()
					.getPreferences());
		} catch (ResponseFormatException e) {
			e.printStackTrace();
			return ExitCode.ERROR;
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
	 * Make a call into PI_VERSIONS API to have a list of PeakInvestigator
	 * versions available. This also checks that credentials are correct. This
	 * uses the version of the method that takes more parameters, using sensible
	 * defaults.
	 * 
	 * @param preferences
	 *            A MZminePreferences object to get Veritomyx credentials.
	 * 
	 * @return An object containing response from server, or null if credentials
	 *         are wrong and not corrected by the user.
	 * @throws ResponseFormatException
	 */
	protected static PiVersionsAction performPiVersionsCall(
			MZminePreferences preferences) throws ResponseFormatException {

		VeritomyxSettings settings = preferences.getVeritomyxSettings();
		PeakInvestigatorSaaS webService = new PeakInvestigatorSaaS(
				settings.server);
		return performPiVersionsCall(preferences, webService, MZmineCore
				.getDesktop().getMainWindow());
	}

	/**
	 * Make a call into PI_VERSIONS API to have a list of PeakInvestigator
	 * versions available. This also checks that credentials are correct. This
	 * function takes more arguments than necessary to make it testable.
	 * 
	 * @param preferences
	 *            A MZminePreferences instance to get Veritomyx credentials
	 *            from, and display dialog if credentials are incorrect.
	 * @param webService
	 *            A PeakInvestigatorSaaS instance for making calls to the public
	 *            API
	 * @param widnow
	 *            A generic window object (hack to make testable)
	 * 
	 * @return An object containing response from server, or null if credentials
	 *         are wrong and not corrected by the user.
	 * @throws ResponseFormatException
	 */
	protected static PiVersionsAction performPiVersionsCall(
			MZminePreferences preferences, PeakInvestigatorSaaS webService,
			Window window) throws ResponseFormatException {

		VeritomyxSettings settings = preferences.getVeritomyxSettings();
		PiVersionsAction action = new PiVersionsAction(
				PeakInvestigatorSaaS.API_VERSION, settings.username,
				settings.password);

		String response = webService.executeAction(action);
		action.processResponse(response);
		if (!action.isReady("PI_VERSIONS")) {
			return null;
		}

		while (action.hasError()) {
			ErrorDialog dialog = dialogFactory.createErrorDialog();
			dialog.displayMessage(action.getErrorMessage(), null);

			long code = action.getErrorCode();
			// Check if error is credentials problem
			if (code != BAD_CREDENTIALS_ERROR_CODE) {
				return null;
			}

			if (preferences.showSetupDialog(window, false) != ExitCode.OK)
				return null;

			settings = preferences.getVeritomyxSettings();

			action = new PiVersionsAction(PeakInvestigatorSaaS.API_VERSION,
					settings.username, settings.password);
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