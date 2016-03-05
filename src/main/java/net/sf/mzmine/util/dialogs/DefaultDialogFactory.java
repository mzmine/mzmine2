package net.sf.mzmine.util.dialogs;

import net.sf.mzmine.util.dialogs.interfaces.DialogFactory;
import net.sf.mzmine.util.dialogs.interfaces.BasicDialog;

public class DefaultDialogFactory implements DialogFactory {

	@Override
	public BasicDialog createDialog() {
		return new DefaultBasicDialog();
	}

}
