package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs;

import net.sf.mzmine.util.ExitCode;

public interface InitDialog {

	public ExitCode getExitCode();

	public String getSelectedRTO();

	public void setVisible(boolean visibility);
}
