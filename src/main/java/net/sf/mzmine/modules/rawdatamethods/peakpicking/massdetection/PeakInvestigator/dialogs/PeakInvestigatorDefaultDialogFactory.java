package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs;

import com.veritomyx.actions.InitAction;
import java.awt.Window;

public class PeakInvestigatorDefaultDialogFactory implements
		PeakInvestigatorDialogFactory {

	@Override
	public InitDialog createInitDialog(Object parent, String version,
			InitAction action) {

		return new PeakInvestigatorInitDialog((Window) parent, version, action);
	}

}
