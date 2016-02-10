package net.sf.mzmine.util.dialogs;

import java.util.logging.Logger;

import net.sf.mzmine.util.dialogs.interfaces.BasicDialog;

public class HeadlessBasicDialog implements BasicDialog {
	private String errorMessage = null;
	private String infoMessage = null;

	@Override
	public void displayErrorMessage(String message, Logger logger) {
		this.errorMessage = message;
	}

	@Override
	public void displayInfoMessage(String message, Logger logger) {
		this.infoMessage = message;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getInfoMessage() {
		return infoMessage;
	}
}
