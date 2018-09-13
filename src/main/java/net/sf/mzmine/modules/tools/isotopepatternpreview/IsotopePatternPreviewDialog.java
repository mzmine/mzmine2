package net.sf.mzmine.modules.tools.isotopepatternpreview;

import java.awt.Color;
import java.awt.Window;
import javax.swing.JCheckBox;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import net.sf.mzmine.chartbasics.chartthemes.EIsotopePatternChartTheme;
import net.sf.mzmine.chartbasics.gui.swing.EChartPanel;
import net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner.IsotopePeakScannerParameters;
import net.sf.mzmine.modules.tools.isotopepatternpreview.customparameters.IsotopePatternPreviewCustomParameters;
import net.sf.mzmine.modules.visualization.spectra.datasets.ExtendedIsotopePatternDataSet;
import net.sf.mzmine.modules.visualization.spectra.renderers.SpectraToolTipGenerator;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.dialogs.ParameterSetupDialogWithEmptyPreview;
import net.sf.mzmine.parameters.parametertypes.BooleanParameter;
import net.sf.mzmine.parameters.parametertypes.DoubleComponent;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.OptionalModuleParameter;
import net.sf.mzmine.parameters.parametertypes.PercentComponent;
import net.sf.mzmine.parameters.parametertypes.PercentParameter;
import net.sf.mzmine.parameters.parametertypes.StringComponent;
import net.sf.mzmine.parameters.parametertypes.StringParameter;

public class IsotopePatternPreviewDialog extends ParameterSetupDialogWithEmptyPreview {

  private EChartPanel pnlChart;
  private JFreeChart chart;
  private XYPlot plot;
  private EIsotopePatternChartTheme theme;
  
  private StringComponent cmpMolecule;
  private PercentComponent cmpMinAbundance;
  private DoubleComponent cmpMinIntensity, cmpMergeWidth;
  private JCheckBox cmpCustom;
  
  private DoubleParameter pMinIntensity, pMergeWidth;
  private PercentParameter pMinAbundance;
  private StringParameter pMolecule;
  private OptionalModuleParameter pCustom;
  
  private ExtendedIsotopePatternDataSet dataset;
  private SpectraToolTipGenerator ttGen;
  
  Color aboveMin, belowMin;

  ParameterSet customParameters;
  
  public IsotopePatternPreviewDialog(Window parent, boolean valueCheckRequired,
      ParameterSet parameters) {
    super(parent, valueCheckRequired, parameters);
    
    cmpMolecule =
        (StringComponent) this.getComponentForParameter(IsotopePatternPreviewParameters.molecule);
    
    pMolecule = parameterSet.getParameter(IsotopePatternPreviewParameters.molecule);
    pCustom = parameterSet.getParameter(IsotopePatternPreviewParameters.optionals);
    
    customParameters = pCustom.getEmbeddedParameters();
    pMinIntensity = customParameters.getParameter(IsotopePatternPreviewCustomParameters.minPatternIntensity);
    pMinAbundance = customParameters.getParameter(IsotopePatternPreviewCustomParameters.minAbundance);
    pMergeWidth = customParameters.getParameter(IsotopePatternPreviewCustomParameters.mergeWidth);
  }

  @Override
  protected void addDialogComponents() {
    super.addDialogComponents();
    
    
  }
}
