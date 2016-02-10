package net.sf.mzmine.util.dialogs;

import java.util.logging.Logger;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.util.dialogs.interfaces.ErrorDialog;

public class DefaultErrorDialog implements ErrorDialog {

	@Override
	public void displayMessage(String message, Logger logger) {
		MZmineCore.getDesktop().displayErrorMessage(
				MZmineCore.getDesktop().getMainWindow(), "Error", message,
				logger);
	}

}
