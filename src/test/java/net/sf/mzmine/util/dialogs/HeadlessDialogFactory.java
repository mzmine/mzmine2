package net.sf.mzmine.util.dialogs;

import net.sf.mzmine.util.dialogs.interfaces.DialogFactory;
import net.sf.mzmine.util.dialogs.interfaces.BasicDialog;

public class HeadlessDialogFactory implements DialogFactory {
	private BasicDialog dialog = null;

	@Override
	public BasicDialog createDialog() {
		dialog = new HeadlessBasicDialog();
		return dialog;
	}

	public BasicDialog getDialog() {
		return dialog;
	}
}
