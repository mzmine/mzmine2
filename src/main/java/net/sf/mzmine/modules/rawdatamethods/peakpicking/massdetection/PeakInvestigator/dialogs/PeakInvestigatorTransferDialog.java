/*
 * Copyright 2013-2016 Veritomyx, Inc.
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs;

import javax.swing.ProgressMonitor;

import net.sf.mzmine.main.MZmineCore;

import com.jcraft.jsch.SftpProgressMonitor;

public class PeakInvestigatorTransferDialog implements SftpProgressMonitor {

	public static final int POPUP_TIME = 500;

	private ProgressMonitor monitor = null;
	private long transferred = 0;
	private long totalSize;
	private boolean isCanceled = false;
	
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

		String note = "Starting.";
		monitor = new ProgressMonitor(MZmineCore.getDesktop().getMainWindow(),
				builder.toString(), note, 0, 100);
		totalSize = size;
		monitor.setMillisToDecideToPopup(POPUP_TIME);
		monitor.setMillisToPopup(POPUP_TIME);

	}

	@Override
	public boolean count(long size) {
		if (monitor.isCanceled()) {
			isCanceled = true;
			return false;
		}

		transferred += size;

		int progress = (int) (transferred * 100 / totalSize);
		monitor.setProgress(progress);

		String note = String.format("%d%% completed.", progress);
		monitor.setNote(note);

		return true;
	}

	@Override
	public void end() {
		monitor.setProgress(100);
		monitor.setNote("Finished.");
		monitor.close();
	}

	public boolean isCanceled() {
		return isCanceled;
	}
}
