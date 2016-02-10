package net.sf.mzmine.util.dialogs;

import net.sf.mzmine.util.dialogs.interfaces.DialogFactory;
import net.sf.mzmine.util.dialogs.interfaces.ErrorDialog;

public class HeadlessDialogFactory implements DialogFactory {
	private ErrorDialog dialog = null;

	@Override
	public ErrorDialog createErrorDialog() {
		dialog = new HeadlessErrorDialog();
		return dialog;
	}

	public ErrorDialog getDialog() {
		return dialog;
	}
}
