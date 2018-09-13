package net.sf.mzmine.modules.tools.isotopepatternpreview.customparameters;

import java.text.DecimalFormat;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.PercentParameter;

public class IsotopePatternPreviewCustomParameters  extends SimpleParameterSet{

  public static final PercentParameter minAbundance = new PercentParameter("Minimum abundance",
      "The minimum abundance (%) of Isotopes. Small values ",
      0.01);
  
  public static final DoubleParameter mergeWidth = new DoubleParameter("Merge width(Da)",
      "This will be used to merge peaks in the calculated isotope pattern if they overlap.",
      MZmineCore.getConfiguration().getMZFormat(), 0.0005, 1E-7, 10.0);
  
  public static final DoubleParameter minPatternIntensity =
      new DoubleParameter("Min. pattern intensity",
          "The minimum normalized intensity of a peak in the final calculated isotope pattern. "
              + "Depends on the sensitivity of your MS.\nMin = 0.0, Max = 0.99...",
          new DecimalFormat("0.####"), 0.01, 0.0, 0.99999);
}
