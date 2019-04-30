/*
 * This module was prepared by Abi Sarvepalli, Christopher Jensen, and Zheng Zhang at the Dorrestein
 * Lab (University of California, San Diego).
 * 
 * It is freely available under the GNU GPL licence of MZmine2.
 * 
 * For any questions or concerns, please refer to:
 * https://groups.google.com/forum/#!forum/molecular_networking_bug_reports
 * 
 * Credit to the Du-Lab development team for the initial commitment to the MGF export module.
 */

package net.sf.mzmine.modules.peaklistmethods.io.siriusexport;

import java.util.Collection;
import javax.annotation.Nonnull;
import net.sf.mzmine.datamodel.MZmineProject;
import net.sf.mzmine.datamodel.PeakListRow;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExceptionUtils;
import net.sf.mzmine.util.ExitCode;

public class SiriusExportModule implements MZmineProcessingModule {
  private static final String MODULE_NAME = "Export for SIRIUS";
  private static final String MODULE_DESCRIPTION =
      "This method exports a MGF file that contains for each feature, (1) the deconvoluted MS1 isotopic pattern, and (2) the MS/MS spectrum (highest precursor ion intensity). This file can be open and processed with Sirius, https://bio.informatik.uni-jena.de/software/sirius/.";

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
  public ExitCode runModule(@Nonnull MZmineProject project, @Nonnull ParameterSet parameters,
      @Nonnull Collection<Task> tasks) {
    SiriusExportTask task = new SiriusExportTask(parameters);
    tasks.add(task);
    return ExitCode.OK;

  }


  public static void exportSinglePeakList(PeakListRow row) {

    try {
      ParameterSet parameters =
          MZmineCore.getConfiguration().getModuleParameters(SiriusExportModule.class);

      ExitCode exitCode = parameters.showSetupDialog(MZmineCore.getDesktop().getMainWindow(), true);
      if (exitCode != ExitCode.OK)
        return;
      // Open file
      final SiriusExportTask task = new SiriusExportTask(parameters);
      task.runSingleRow(row);
    } catch (Exception e) {
      e.printStackTrace();
      MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(),
          "Error while exporting feature to SIRIUS: " + ExceptionUtils.exceptionToString(e));
    }

  }


  public static void exportSingleRows(PeakListRow[] row) {
    try {
      ParameterSet parameters =
          MZmineCore.getConfiguration().getModuleParameters(SiriusExportModule.class);

      ExitCode exitCode = parameters.showSetupDialog(MZmineCore.getDesktop().getMainWindow(), true);
      if (exitCode != ExitCode.OK)
        return;
      // Open file
      final SiriusExportTask task = new SiriusExportTask(parameters);
      task.runSingleRows(row);
    } catch (Exception e) {
      e.printStackTrace();
      MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(),
          "Error while exporting feature to SIRIUS: " + ExceptionUtils.exceptionToString(e));
    }
  }

  @Override
  public @Nonnull MZmineModuleCategory getModuleCategory() {
    return MZmineModuleCategory.PEAKLISTEXPORT;
  }

  @Override
  public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
    return SiriusExportParameters.class;
  }

}

/*
 * "If you use the SIRIUS export module, cite MZmine2 and the following articlel. Dührkop, M. Fleischauer, M. Ludwig, A. A. Aksenov, A. V. Melnik, M. Meusel, P. C. Dorrestein, J. Rousu, and S. Böcker, “Sirius 4: a rapid tool for turning tandem mass spectra into metabolite structure information,” Nat methods, 2019.
 * 8:5
 * 
 * [Link](http://dx.doi.org/10.1038/s41592-019-0344-8), and
 * [Link](https://jcheminf.springeropen.com/articles/10.1186/s13321-016-0116-8)
 */
