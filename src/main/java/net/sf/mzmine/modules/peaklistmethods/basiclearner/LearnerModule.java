/*
 * Copyright 2006-2015 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package net.sf.mzmine.modules.peaklistmethods.basiclearner;

import java.util.Collection;
import javax.annotation.Nonnull;
import net.sf.mzmine.datamodel.MZmineProject;
import net.sf.mzmine.datamodel.PeakList;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

/**
 * A Module creates tasks which are then added queue
 */
public class LearnerModule implements MZmineProcessingModule {
  // #################################################################
  // IMPORTANT
  // do not forget to put your module in the MZmineModulesList
  // in the package: net.sf.mzmine.main
  // #################################################################

  private static final String MODULE_NAME = "Learner module";
  private static final String MODULE_DESCRIPTION = "This module is for learners only";


  @Override
  @Nonnull
  public ExitCode runModule(@Nonnull MZmineProject project, @Nonnull ParameterSet parameters,
      @Nonnull Collection<Task> tasks) {

    // get parameters
    PeakList peakLists[] =
        parameters.getParameter(LearnerParameters.peakLists).getValue().getMatchingPeakLists();

    // create and start one task for each peaklist
    for (final PeakList peakList : peakLists) {
      Task newTask = new PeakListRowLearnerTask(project, peakList, parameters);
      tasks.add(newTask);
    }

    return ExitCode.OK;

  }

  @Override
  public @Nonnull MZmineModuleCategory getModuleCategory() {
    /**
     * Change category: will automatically be added to the linked menu
     */
    return MZmineModuleCategory.PEAKLISTFILTERING;
  }

  @Override
  public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
    return LearnerParameters.class;
  }

  @Override
  public @Nonnull String getName() {
    return MODULE_NAME;
  }

  @Override
  public @Nonnull String getDescription() {
    return MODULE_DESCRIPTION;
  }
}
