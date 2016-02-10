package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs;

import com.veritomyx.actions.InitAction;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.util.dialogs.DefaultDialogFactory;

public class PeakInvestigatorDefaultDialogFactory extends DefaultDialogFactory implements
		PeakInvestigatorDialogFactory {

	@Override
	public InitDialog createInitDialog(String version, InitAction action) {
		return new PeakInvestigatorInitDialog(MZmineCore.getDesktop()
				.getMainWindow(), version, action);
	}

}
