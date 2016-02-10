package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs;

import net.sf.mzmine.util.dialogs.interfaces.DialogFactory;

import com.veritomyx.actions.InitAction;

public interface PeakInvestigatorDialogFactory extends DialogFactory {

	InitDialog createInitDialog(String version, InitAction action);
}
