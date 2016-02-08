package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator;

import java.util.logging.Logger;

import com.veritomyx.actions.InitAction;

import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.ErrorDialog;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.InitDialog;
import net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs.PeakInvestigatorDialogFactory;

public abstract class AbstractTestDialogFactory implements
		PeakInvestigatorDialogFactory {

	private ErrorDialog dialog = null;

	@Override
	public InitDialog createInitDialog(String version, InitAction action) {
		return null;
	}

	@Override
	public ErrorDialog createErrorDialog() {
		dialog = new EmptyErrorDialog();
		return dialog;
	}

	public ErrorDialog getDialog() {
		return dialog;
	}

	/**
	 * ErrorDialog implementation that keeps track of error messages.
	 */
	public class EmptyErrorDialog implements ErrorDialog {

		private String message = null;

		@Override
		public void displayMessage(String message, Logger logger) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
	}
}
