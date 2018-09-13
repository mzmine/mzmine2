package net.sf.mzmine.modules.tools.isotopepatternpreview;

import java.awt.Window;
import net.sf.mzmine.modules.tools.isotopepatternpreview.customparameters.IsotopePatternPreviewCustomParameters;
import net.sf.mzmine.parameters.dialogs.ParameterSetupDialog;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.OptionalModuleParameter;
import net.sf.mzmine.parameters.parametertypes.StringParameter;
import net.sf.mzmine.util.ExitCode;

public class IsotopePatternPreviewParameters extends SimpleParameterSet{
 
  public static final StringParameter molecule = new StringParameter("Element/Molecule", "The element/molecule to calculate the isotope pattern of. Enter a sum formula.");
  
  public static final OptionalModuleParameter optionals = new OptionalModuleParameter("Use custom paramters", "If not checked, default parameters will be used to calculate the isotope pattern", new IsotopePatternPreviewCustomParameters());

  public ExitCode showSetupDialog(Window parent, boolean valueCheckRequired) {
    if ((getParameters() == null) || (getParameters().length == 0))
      return ExitCode.OK;

    ParameterSetupDialog dialog =
        new IsotopePatternPreviewDialog(parent, valueCheckRequired, this);
    dialog.setVisible(true);
    return dialog.getExitCode();
  }
}
