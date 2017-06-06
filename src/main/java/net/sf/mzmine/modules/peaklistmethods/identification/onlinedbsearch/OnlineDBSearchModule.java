/*
 * Copyright 2006-2015 The MZmine 2 Development Team
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

package net.sf.mzmine.modules.peaklistmethods.identification.onlinedbsearch;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.sf.mzmine.datamodel.MZmineProject;
import net.sf.mzmine.datamodel.PeakList;
import net.sf.mzmine.datamodel.PeakListRow;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

/**
 * Module for identifying peaks by searching on-line databases.
 */
public class OnlineDBSearchModule implements MZmineProcessingModule {

    private static final String MODULE_NAME = "Online database search";
    private static final String MODULE_DESCRIPTION = "This module attepts to find those peaks in a peak list, which form an isotope pattern.";

    @Override
    public @Nonnull String getName() {
        return MODULE_NAME;
    }

    @Override
    public @Nonnull String getDescription() {
        return MODULE_DESCRIPTION;
    }

    @Override
    @Nonnull
    public ExitCode runModule(@Nonnull MZmineProject project,
            @Nonnull ParameterSet parameters, @Nonnull Collection<Task> tasks) {

        final PeakList[] peakLists = parameters
                .getParameter(PeakListIdentificationParameters.peakLists)
                .getValue().getMatchingPeakLists();
        for (final PeakList peakList : peakLists) {
            Task newTask = new PeakListIdentificationTask(parameters, peakList);
            tasks.add(newTask);
        }

        return ExitCode.OK;
    }

    /**
     * Show dialog for identifying a single peak-list row.
     * 
     * @param row
     *            the peak list row.
     */
    public static void showSingleRowIdentificationDialog(final PeakListRow row) {

        final ParameterSet parameters = new SingleRowIdentificationParameters();

        // Set m/z.
        parameters.getParameter(SingleRowIdentificationParameters.NEUTRAL_MASS)
                .setIonMass(row.getAverageMZ());

        // Set charge.
        final int charge = row.getBestPeak().getCharge();
        if (charge > 0) {

            parameters.getParameter(
                    SingleRowIdentificationParameters.NEUTRAL_MASS).setCharge(
                    charge);
        }

        // Run task.
        if (parameters.showSetupDialog(MZmineCore.getDesktop().getMainWindow(),
                true) == ExitCode.OK) {

            MZmineCore.getTaskController().addTask(
                    new SingleRowIdentificationTask(parameters
                            .cloneParameterSet(), row));
        }
    }

    @Override
    public @Nonnull MZmineModuleCategory getModuleCategory() {
        return MZmineModuleCategory.IDENTIFICATION;
    }

    @Override
    public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
        return PeakListIdentificationParameters.class;
    }
}
