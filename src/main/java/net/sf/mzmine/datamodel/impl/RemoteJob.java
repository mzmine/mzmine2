/*
 * Copyright 2013-2015 Veritomyx Inc.
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

package net.sf.mzmine.datamodel.impl;

import java.util.logging.Logger;

import com.veritomyx.PeakInvestigatorSaaS;
import com.veritomyx.VeritomyxSettings;
import com.veritomyx.actions.BaseAction.ResponseFormatException;
import com.veritomyx.actions.StatusAction;

import net.sf.mzmine.datamodel.RemoteJobInfo;
import net.sf.mzmine.desktop.preferences.MZminePreferences;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.util.dialogs.DefaultDialogFactory;
import net.sf.mzmine.util.dialogs.interfaces.BasicDialog;
import net.sf.mzmine.util.dialogs.interfaces.DialogFactory;
import net.sf.mzmine.datamodel.RawDataFile;

/**
 * This defines a Veritomyx job
 */
public class RemoteJob implements RemoteJobInfo
{
	public enum Status { ERROR, RUNNING, DONE, DELETED };

	private DialogFactory dialogFactory = new DefaultDialogFactory();
	private Logger logger;

	private String        jobID;
	private RawDataFile   rawDataFile;
	private String        targetName;
	private PeakInvestigatorSaaS vtmx;
	
	public RemoteJob(String name, RawDataFile raw, String target, PeakInvestigatorSaaS vtmxConn)
	{
		logger = Logger.getLogger(this.getClass().getName());

		jobID       = name;
		rawDataFile = raw;
		targetName  = target;
//		if (vtmx != null)
			vtmx = vtmxConn;
//		else
//			this.vtmx = new VeritomyxSaaS(username, password, pid, name);
	}

	protected RemoteJob setDialogFactory(DialogFactory dialogFactory) {
		this.dialogFactory = dialogFactory;
		return this;
	}

    public String      getName()        { return jobID; }
    public String      toString()       { return jobID; }
    public RawDataFile getRawDataFile() { return rawDataFile; }
    public String      getTargetName()  { return targetName; }

	public int getStatus() {
		MZminePreferences preferences = MZmineCore.getConfiguration().getPreferences();
		VeritomyxSettings settings = preferences.getVeritomyxSettings();
		PeakInvestigatorSaaS server = new PeakInvestigatorSaaS(settings.server);

		try {
			return getStatus(server, settings);
		} catch (ResponseFormatException e) {
			error(e.getMessage());
			return Status.ERROR.ordinal();
		}
	}

	protected int getStatus(PeakInvestigatorSaaS server,
			VeritomyxSettings settings) throws ResponseFormatException {

		StatusAction action = new StatusAction(
				PeakInvestigatorSaaS.API_VERSION, settings.username,
				settings.password, jobID);
		String response = server.executeAction(action);
		action.processResponse(response);
		if (action.hasError()) {
			error(action.getErrorMessage());
			return Status.ERROR.ordinal();
		}

		StatusAction.Status status = action.getStatus();
		switch (status) {
		case Running:
			message(action.getMessage());
			return Status.RUNNING.ordinal();
		case Done:
			message(action.getMessage());
			return Status.DONE.ordinal();
		case Deleted:
			message(action.getMessage());
			return Status.DELETED.ordinal();
		default:
			throw new IllegalStateException(
					"Unknown status returned from server.");
		}
	}

    public int 			deleteJob() {
    	MZminePreferences preferences = MZmineCore.getConfiguration().getPreferences();
		String server = preferences.getParameter(MZminePreferences.vtmxServer).getValue();
    	String username = preferences.getParameter(MZminePreferences.vtmxUsername).getValue();
		String password = preferences.getParameter(MZminePreferences.vtmxPassword).getValue();
		Integer account  = preferences.getParameter(MZminePreferences.vtmxProject).getValue();

    	if(vtmx == null)
    		vtmx = new PeakInvestigatorSaaS(server);

    	return vtmx.deleteJob(username, password, account, jobID.substring(4));
    }

	private void error(String message) {
		BasicDialog dialog = dialogFactory.createDialog();
		dialog.displayErrorMessage(message, logger);
	}

	private void message(String message) {
		BasicDialog dialog = dialogFactory.createDialog();
		dialog.displayInfoMessage(message, logger);
	}
}