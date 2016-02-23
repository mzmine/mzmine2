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

import com.jcraft.jsch.SftpProgressMonitor;
import com.veritomyx.actions.InitAction;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.util.dialogs.DefaultDialogFactory;

public class PeakInvestigatorDefaultDialogFactory extends DefaultDialogFactory
		implements PeakInvestigatorDialogFactory {

	@Override
	public InitDialog createInitDialog(String version, InitAction action) {
		return new PeakInvestigatorInitDialog(MZmineCore.getDesktop()
				.getMainWindow(), version, action);
	}

	@Override
	public SftpProgressMonitor createSftpProgressMonitor() {
		return new PeakInvestigatorTransferDialog();
	}

}
