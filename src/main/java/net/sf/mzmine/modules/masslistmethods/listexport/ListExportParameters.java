/*
 * Copyright 2006-2012 The MZmine 2 Development Team
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

package net.sf.mzmine.modules.masslistmethods.listexport;

import java.awt.Window;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.BooleanParameter;
import net.sf.mzmine.parameters.parametertypes.filenames.DirectoryParameter;
import net.sf.mzmine.parameters.parametertypes.MassListParameter;
import net.sf.mzmine.parameters.parametertypes.selectors.RawDataFilesParameter;
import net.sf.mzmine.util.ExitCode;

public class ListExportParameters extends SimpleParameterSet
{
	public static final RawDataFilesParameter dataFiles = new RawDataFilesParameter();
	public static final     MassListParameter massList  = new MassListParameter(false);
	public static final      BooleanParameter dumpScans = new BooleanParameter(
			"Export original scans",
			"If checked, original scan data will also be exported to text files");
	public static final DirectoryParameter saveDirectory = new DirectoryParameter("Save to directory",
			"Directory to save scans");

	public ListExportParameters()
	{
		super(new Parameter[] { dataFiles, dumpScans, massList, saveDirectory });
	}

	public ExitCode showSetupDialog()
	{
		ListExportSetupDialog dialog = new ListExportSetupDialog(MZmineCore.getDesktop().getMainWindow(),
			    true, this);
		dialog.setVisible(true);
		return dialog.getExitCode();
	}
}
