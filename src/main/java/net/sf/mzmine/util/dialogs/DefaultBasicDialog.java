package net.sf.mzmine.util.dialogs;

import java.util.logging.Logger;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.util.dialogs.interfaces.BasicDialog;

public class DefaultBasicDialog implements BasicDialog {

	@Override
	public void displayErrorMessage(String message, Logger logger) {
		MZmineCore.getDesktop().displayErrorMessage(
				MZmineCore.getDesktop().getMainWindow(), "Error", message,
				logger);
	}

	@Override
	public void displayInfoMessage(String message, Logger logger) {
		MZmineCore.getDesktop().displayErrorMessage(
				MZmineCore.getDesktop().getMainWindow(), "Info", message,
				logger);
	}

}
