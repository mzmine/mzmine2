package net.sf.mzmine.util.dialogs.interfaces;

import java.util.logging.Logger;

public interface BasicDialog {

	public void displayErrorMessage(String errorMessage, Logger logger);

	public void displayInfoMessage(String message, Logger logger);
}
