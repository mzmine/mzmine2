package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs;

import com.veritomyx.actions.InitAction;

import java.awt.Window;

import net.sf.mzmine.main.MZmineCore;

public class PeakInvestigatorDefaultDialogFactory implements
		PeakInvestigatorDialogFactory {

	@Override
	public InitDialog createInitDialog(String version, InitAction action) {

		return new PeakInvestigatorInitDialog(MZmineCore.getDesktop()
				.getMainWindow(), version, action);
	}

}
