package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs;

import com.veritomyx.actions.InitAction;

public interface PeakInvestigatorDialogFactory {

	InitDialog createInitDialog(String version, InitAction action);
}
