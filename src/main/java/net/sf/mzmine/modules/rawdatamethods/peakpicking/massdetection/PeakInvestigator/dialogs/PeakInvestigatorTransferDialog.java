package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs;

import javax.swing.ProgressMonitor;

import net.sf.mzmine.main.MZmineCore;

import com.jcraft.jsch.SftpProgressMonitor;

public class PeakInvestigatorTransferDialog implements SftpProgressMonitor {

	public static final int POPUP_TIME = 100;

	private ProgressMonitor monitor = null;
	private long transferred = 0;
	private long totalSize;
	
	@Override
	public void init(int direction, String src, String dest, long size) {
		StringBuilder builder = new StringBuilder("Transferring ");
		switch (direction) {
		case SftpProgressMonitor.GET:
			builder.append("results from remote server.");
			break;
		case SftpProgressMonitor.PUT:
			builder.append("scans to remote server.");
			break;
		default:
			throw new IllegalArgumentException("Unknown Sftp direction.");
		}

		String note = "0% completed.";
		monitor = new ProgressMonitor(MZmineCore.getDesktop().getMainWindow(),
				builder.toString(), note, 0, 100);
		totalSize = size;
		monitor.setMillisToDecideToPopup(POPUP_TIME);
		monitor.setMillisToPopup(POPUP_TIME);

	}

	@Override
	public boolean count(long size) {
		transferred += size;

		int progress = (int) (transferred / totalSize);
		monitor.setProgress(progress);

		String note = String.format("%d%% completed.", progress);
		monitor.setNote(note);

		return monitor.isCanceled();
	}

	@Override
	public void end() {
		monitor.close();
	}
}
