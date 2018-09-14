package net.sf.mzmine.modules.tools.isotopepatternpreview;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import net.sf.mzmine.chartbasics.chartthemes.EIsotopePatternChartTheme;
import net.sf.mzmine.chartbasics.gui.swing.EChartPanel;
import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.PolarityType;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner.ExtendedIsotopePattern;
import net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner.IsotopePeakScannerParameters;
import net.sf.mzmine.modules.tools.isotopepatternpreview.customparameters.IsotopePatternPreviewCustomParameters;
import net.sf.mzmine.modules.visualization.spectra.datasets.ExtendedIsotopePatternDataSet;
import net.sf.mzmine.modules.visualization.spectra.renderers.SpectraToolTipGenerator;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.dialogs.ParameterSetupDialog;
import net.sf.mzmine.parameters.dialogs.ParameterSetupDialogWithEmptyPreview;
import net.sf.mzmine.parameters.parametertypes.BooleanParameter;
import net.sf.mzmine.parameters.parametertypes.DoubleComponent;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.OptionalModuleParameter;
import net.sf.mzmine.parameters.parametertypes.PercentComponent;
import net.sf.mzmine.parameters.parametertypes.PercentParameter;
import net.sf.mzmine.parameters.parametertypes.StringComponent;
import net.sf.mzmine.parameters.parametertypes.StringParameter;
import net.sf.mzmine.util.ExitCode;

public class IsotopePatternPreviewDialog extends ParameterSetupDialog {
  private Logger logger = Logger.getLogger(this.getClass().getName());
  private NumberFormat mzFormat = MZmineCore.getConfiguration().getMZFormat();
  private NumberFormat relFormat = new DecimalFormat("0.0000");
  
  private double minAbundance, minIntensity, mergeWidth;
  private String molecule;
  
  private EChartPanel pnlChart;
  private JFreeChart chart;
  private XYPlot plot;
  private EIsotopePatternChartTheme theme;
  private JPanel newMainPanel;
  private JScrollPane pnText;
  private JTextArea textArea;
  private JButton btnCalc;
  private JSplitPane pnSplit;
  
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
    
    aboveMin = new Color(30, 255, 30);
    belowMin = new Color(255, 30, 30);
    
  }

  @Override
  protected void addDialogComponents() {
    super.addDialogComponents();
    
    // panels
    newMainPanel = new JPanel(new BorderLayout());
    pnText = new JScrollPane();
    pnlChart = new EChartPanel(chart);
    pnSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlChart, pnText);
    
    pnText.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    pnText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    
    pnText.setMinimumSize(new Dimension(350, 300));
    pnlChart.setMinimumSize(new Dimension(350, 200));
    
    // controls
    textArea = new JTextArea();
    btnCalc = new JButton("Calculate");
    ttGen = new SpectraToolTipGenerator();
    theme = new EIsotopePatternChartTheme();
    theme.initialize();
    btnCalc.addActionListener(this);
    textArea.setEditable(false);
    
    //reorganize
    getContentPane().remove(mainPanel);
    newMainPanel.add(mainPanel, BorderLayout.SOUTH);
    newMainPanel.add(pnSplit, BorderLayout.CENTER);
    mainPanel.add(btnCalc);
    getContentPane().add(newMainPanel);
    pnlButtons.remove(super.btnCancel);
    
    chart =  ChartFactory.createXYBarChart("Isotope pattern preview", "m/z", false, "Abundance",
        new XYSeriesCollection(new XYSeries("")));
    theme.apply(chart);
    pnlChart.setChart(chart);
    pnText.setViewportView(textArea);
    
    updateMinimumSize();
    pack();
  }
  
  public void actionPerformed(ActionEvent ae) {
    if(ae.getSource() == btnOK) {
      this.closeDialog(ExitCode.CANCEL);
    }
    
    if(ae.getSource() == btnCalc) {
      updateParameterSetFromComponents();
      updatePreview();

    }
  }
//-----------------------------------------------------
//methods
//-----------------------------------------------------  
  private void updatePreview() {
    if (!updateParameters()) {
      logger.warning(
          "updatePreview() failed. Could not update parameters or parameters are invalid. Please check the parameters.");
      return;
    }
  
    ExtendedIsotopePattern pattern = calculateIsotopePattern();
    if (pattern == null) {
      logger.warning("Could not calculate isotope pattern. Please check the parameters.");
      return;
    }
  
    textArea.setText("Mass / Da\tIntensity\tIsotope composition\n");
    DataPoint[] dp = pattern.getDataPoints();
    for(int i = 0; i < pattern.getNumberOfDataPoints(); i++) {
      textArea.append(mzFormat.format(dp[i].getMZ()) + " \t"
        + relFormat.format(dp[i].getIntensity()) + "\t"
        + pattern.getDetailedPeakDescription(i) + "\n");
    }
    textArea.setPreferredSize(textArea.getMinimumSize());
    updateChart(pattern);
  }
  
  private void updateChart(ExtendedIsotopePattern pattern) {
    dataset = new ExtendedIsotopePatternDataSet(pattern, minIntensity, mergeWidth);
    chart =
        ChartFactory.createXYBarChart("Isotope pattern preview", "m/z", false, "Abundance", dataset);
    theme.apply(chart);
    plot = chart.getXYPlot();
    plot.addRangeMarker(new ValueMarker(minIntensity, belowMin, new BasicStroke(1.0f)));
    XYItemRenderer r = plot.getRenderer();
    r.setSeriesPaint(0, aboveMin);
    r.setSeriesPaint(1, belowMin);
    r.setDefaultToolTipGenerator(ttGen);
    pnlChart.setChart(chart);
  }
  
  private boolean updateParameters() {
    updateParameterSetFromComponents();
    if (!checkParameters()) {
      logger.info("updateParameters() failed due to invalid input.");
      return false;
    }
      
  
    molecule = pMolecule.getValue(); //TODO
    minAbundance = pMinAbundance.getValue();
    mergeWidth = pMergeWidth.getValue();
    minIntensity = pMinIntensity.getValue();
    return true;
  }
  
  private boolean checkParameters() {
    if (/* pElement.getValue().equals("") */pMolecule.getValue() == null ||  pMolecule.getValue().equals("")) {
      logger.info("Invalid input or Element == \"\" and no autoCarbon");
      return false;
    }
    if (pMinAbundance.getValue() == null || pMinAbundance.getValue() > 1.0d || pMinAbundance.getValue() <= 0.0d) {
      logger.info("Minimun abundance invalid. " + pMinAbundance.getValue());
      return false;
    }
    if (pMinIntensity.getValue() == null || pMinIntensity.getValue() > 1.0d || pMinIntensity.getValue() <= 1E-12) {
      logger.info("Minimum intensity invalid. " + pMinIntensity.getValue());
      return false;
    }
    if(pMergeWidth.getValue() == null || pMergeWidth.getValue() <= 1E-12) {
      logger.info("Merge width invalid. " + pMergeWidth.getValue());
      return false;
    }
      
    logger.info("Parameters valid");
    return true;
  }
  
  private ExtendedIsotopePattern calculateIsotopePattern() {
    ExtendedIsotopePattern pattern = new ExtendedIsotopePattern();
  
    if (!checkParameters())
      return null;
    
    if (molecule.equals(""))
      return null;
    
    logger.info("Calculating isotope pattern: " + molecule);
  
    // *0.2 so the user can see the peaks below the threshold
    pattern.setUpFromFormula(molecule, minAbundance, mergeWidth, minIntensity * 0.2);
    return pattern;
  }
}
