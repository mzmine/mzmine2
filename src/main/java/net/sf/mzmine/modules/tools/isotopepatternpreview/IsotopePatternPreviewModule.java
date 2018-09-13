package net.sf.mzmine.modules.tools.isotopepatternpreview;

import java.util.Collection;
import javax.annotation.Nonnull;
import net.sf.mzmine.datamodel.MZmineProject;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

public class IsotopePatternPreviewModule implements MZmineProcessingModule {

  private static final String MODULE_NAME = "Isotope pattern preview";
  private static final String MODULE_DESCRIPTION =
      "Calculate and view isotope patterns.";

  @Override
  public @Nonnull String getName() {
    return MODULE_NAME;
  }

  public @Nonnull MZmineModuleCategory getModuleCategory() {
    return MZmineModuleCategory.ISOTOPES;
  }

  @Override
  public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
    return IsotopePatternPreviewParameters.class;
  }

  public @Nonnull String getDescription() {
    return MODULE_DESCRIPTION;
  }

  @Override
  public @Nonnull ExitCode runModule(@Nonnull MZmineProject project,
      @Nonnull ParameterSet parameters, @Nonnull Collection<Task> tasks) {
    
    return ExitCode.OK;
  }

}