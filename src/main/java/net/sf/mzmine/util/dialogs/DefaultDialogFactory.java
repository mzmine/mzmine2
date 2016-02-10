package net.sf.mzmine.util.dialogs;

import net.sf.mzmine.util.dialogs.interfaces.DialogFactory;
import net.sf.mzmine.util.dialogs.interfaces.ErrorDialog;

public class DefaultDialogFactory implements DialogFactory {

	@Override
	public ErrorDialog createErrorDialog() {
		return new DefaultErrorDialog();
	}
}
