package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs;

import net.sf.mzmine.util.dialogs.interfaces.DialogFactory;

import com.veritomyx.actions.InitAction;

import com.jcraft.jsch.SftpProgressMonitor;

public interface PeakInvestigatorDialogFactory extends DialogFactory {

	InitDialog createInitDialog(String version, InitAction action);
	SftpProgressMonitor createSftpProgressMonitor();
}
