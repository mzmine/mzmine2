package net.sf.mzmine.util.dialogs;

import java.util.logging.Logger;

import net.sf.mzmine.util.dialogs.interfaces.ErrorDialog;

public class HeadlessErrorDialog implements ErrorDialog {
	private String message = null;

	@Override
	public void displayMessage(String message, Logger logger) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
