package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs;

import java.util.logging.Logger;

import net.sf.mzmine.main.MZmineCore;

public class PeakInvestigatorErrorDialog implements ErrorDialog {

	@Override
	public void displayMessage(String message, Logger logger) {
		MZmineCore.getDesktop().displayErrorMessage(
				MZmineCore.getDesktop().getMainWindow(), "Error", message,
				logger);
	}

}
