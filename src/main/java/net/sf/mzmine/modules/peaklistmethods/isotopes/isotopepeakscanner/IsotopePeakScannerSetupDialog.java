package net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.NumberFormatter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import net.sf.mzmine.chartbasics.chartthemes.EIsotopePatternChartTheme;
import net.sf.mzmine.chartbasics.gui.swing.EChartPanel;
import net.sf.mzmine.datamodel.PolarityType;
import net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner.autocarbon.AutoCarbonParameters;
import net.sf.mzmine.modules.visualization.spectra.datasets.ExtendedIsotopePatternDataSet;
import net.sf.mzmine.modules.visualization.spectra.renderers.SpectraToolTipGenerator;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.dialogs.ParameterSetupDialog;
import net.sf.mzmine.parameters.parametertypes.DoubleComponent;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.IntegerComponent;
import net.sf.mzmine.parameters.parametertypes.IntegerParameter;
import net.sf.mzmine.parameters.parametertypes.OptionalModuleComponent;
import net.sf.mzmine.parameters.parametertypes.OptionalModuleParameter;
import net.sf.mzmine.parameters.parametertypes.PercentComponent;
import net.sf.mzmine.parameters.parametertypes.PercentParameter;
import net.sf.mzmine.parameters.parametertypes.StringComponent;
import net.sf.mzmine.parameters.parametertypes.StringParameter;

/**
 *
 *Extension of ParameterSetupDialog to allow a preview window
 *
 *
 * @author Steffen Heuckeroth s_heuc03@uni-muenster.de
 *
 */
public class IsotopePeakScannerSetupDialog extends ParameterSetupDialog {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private Logger logger = Logger.getLogger(this.getClass().getName());

  private JPanel pnlPreview; // this will contain the preview and navigation panels
  private JPanel pnlPreviewButtons; // this will contain the navigation
  private JPanel newMainPanel; // this will be the new main panel
  private JPanel pnlParameters; // this will contain all parameters of the module (the main panel
                                // will be inserted here)

  private EChartPanel pnlChart;
  private JFreeChart chart;
  private XYPlot plot;
  private EIsotopePatternChartTheme theme;


  // components created by this class
  private JButton btnPrevPattern, btnNextPattern, btnUpdatePreview;
  private JFormattedTextField txtCurrentPatternIndex;

  NumberFormatter form;

  // components created by parameters
  PercentComponent cmpMinAbundance;
  DoubleComponent cmpMinIntensity, cmpMergeWidth;
  IntegerComponent cmpCharge, cmpMinC, cmpMaxC, cmpMinSize;
  StringComponent cmpElement;
  OptionalModuleComponent cmpAutoCarbon;
  JCheckBox cmpAutoCarbonCbx, cmpPreview;
  

  // relevant parameters
  IntegerParameter pMinC, pMaxC, pMinSize, pCharge;
  StringParameter pElement;
  DoubleParameter pMinIntensity, pMergeWidth;
  PercentParameter pMinAbundance;
  OptionalModuleParameter pAutoCarbon;

  ExtendedIsotopePatternDataSet dataset;
  private SpectraToolTipGenerator ttGen;

  Color aboveMin, belowMin;

  ParameterSet autoCarbonParameters;

  private double minAbundance, minIntensity, mergeWidth;
  private int charge, minSize, minC, maxC;
  private String element;
  private boolean autoCarbon;

  public IsotopePeakScannerSetupDialog(Window parent, boolean valueCheckRequired,
      ParameterSet parameters) {
    super(parent, valueCheckRequired, parameters);

    aboveMin = new Color(30, 255, 30);
    belowMin = new Color(255, 30, 30);
    theme = new EIsotopePatternChartTheme();
    theme.initialize();
    // stroke = new BasicStroke((float)mergeWidth);
    ttGen = new SpectraToolTipGenerator();
  }

  @Override
  protected void addDialogComponents() {
    super.addDialogComponents();

    // get components
    cmpMinAbundance =
        (PercentComponent) this.getComponentForParameter(IsotopePeakScannerParameters.minAbundance);
    cmpMinIntensity = (DoubleComponent) this
        .getComponentForParameter(IsotopePeakScannerParameters.minPatternIntensity);
    cmpCharge =
        (IntegerComponent) this.getComponentForParameter(IsotopePeakScannerParameters.charge);
    cmpMergeWidth =
        (DoubleComponent) this.getComponentForParameter(IsotopePeakScannerParameters.mergeWidth);
    cmpElement =
        (StringComponent) this.getComponentForParameter(IsotopePeakScannerParameters.element);
    cmpAutoCarbon = (OptionalModuleComponent) this
        .getComponentForParameter(IsotopePeakScannerParameters.autoCarbonOpt);
    cmpAutoCarbonCbx = (JCheckBox) cmpAutoCarbon.getComponent(0);
    cmpPreview =
        (JCheckBox) this.getComponentForParameter(IsotopePeakScannerParameters.showPreview);
    cmpPreview.setSelected(false);; // i want to have the checkbox below the pattern settings
    // but it should be disabled by default. Thats why it's hardcoded here.

    // get parameters
    pMinAbundance = parameterSet.getParameter(IsotopePeakScannerParameters.minAbundance);
    pElement = parameterSet.getParameter(IsotopePeakScannerParameters.element);
    pMinIntensity = parameterSet.getParameter(IsotopePeakScannerParameters.minPatternIntensity);
    pCharge = parameterSet.getParameter(IsotopePeakScannerParameters.charge);
    pMergeWidth = parameterSet.getParameter(IsotopePeakScannerParameters.mergeWidth);
    pAutoCarbon = parameterSet.getParameter(IsotopePeakScannerParameters.autoCarbonOpt);
    autoCarbonParameters = pAutoCarbon.getEmbeddedParameters();
    pMinC = autoCarbonParameters.getParameter(AutoCarbonParameters.minCarbon);
    pMaxC = autoCarbonParameters.getParameter(AutoCarbonParameters.maxCarbon);
    pMinSize = autoCarbonParameters.getParameter(AutoCarbonParameters.minPatternSize);

    //set up gui
    minC = 10;// TODO: dont hardcode this
    maxC = 100;
    form = new NumberFormatter(NumberFormat.getInstance());
    form.setValueClass(Integer.class);
    form.setAllowsInvalid(true);
    form.setMinimum(minC);
    form.setMaximum(maxC);

    pnlPreview = new JPanel(new BorderLayout());
    pnlPreviewButtons = new JPanel(new FlowLayout());
    pnlParameters = new JPanel(new BorderLayout());

    btnPrevPattern = new JButton("Previous");
    btnPrevPattern.addActionListener(this);
    btnPrevPattern.setMinimumSize(btnPrevPattern.getPreferredSize());
    btnPrevPattern.setEnabled(cmpAutoCarbonCbx.isSelected());

    txtCurrentPatternIndex = new JFormattedTextField(form);
    txtCurrentPatternIndex.addActionListener(this);
    txtCurrentPatternIndex.setText(String.valueOf((minC + maxC) / 2));
    txtCurrentPatternIndex.setMinimumSize(new Dimension(50, 12));
    txtCurrentPatternIndex.setEditable(true);
    txtCurrentPatternIndex.setEnabled(cmpAutoCarbonCbx.isSelected());

    btnNextPattern = new JButton("Next");
    btnNextPattern.addActionListener(this);
    btnNextPattern.setPreferredSize(btnNextPattern.getMaximumSize());
    btnNextPattern.setEnabled(cmpAutoCarbonCbx.isSelected());

    btnUpdatePreview = new JButton("Update");
    btnUpdatePreview.addActionListener(this);
    btnUpdatePreview.setPreferredSize(btnUpdatePreview.getMinimumSize());
    btnUpdatePreview.setEnabled(cmpPreview.isSelected());

    chart = ChartFactory.createXYBarChart("Isotope pattern preview", "m/z", false, "Abundance",
        new XYSeriesCollection(new XYSeries("")));
    chart.getPlot().setBackgroundPaint(Color.WHITE);
    chart.getXYPlot().setDomainGridlinePaint(Color.GRAY);
    chart.getXYPlot().setRangeGridlinePaint(Color.GRAY);

    pnlChart = new EChartPanel(chart);

    pnlPreviewButtons.add(btnPrevPattern);
    pnlPreviewButtons.add(txtCurrentPatternIndex);
    pnlPreviewButtons.add(btnNextPattern);
    pnlPreviewButtons.add(btnUpdatePreview);


    // remove
    getContentPane().remove(mainPanel);
    JScrollPane scroll = new JScrollPane();
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scroll.setViewportView(mainPanel);
    mainPanel.setMinimumSize(new Dimension(200, 400));
    mainPanel.remove(super.pnlButtons);
    pnlParameters.add(super.pnlButtons, BorderLayout.SOUTH);
    pnlParameters.add(scroll, BorderLayout.CENTER);

    newMainPanel = new JPanel(new BorderLayout());
    newMainPanel.add(pnlParameters, BorderLayout.WEST);

    pnlPreview.add(pnlPreviewButtons, BorderLayout.SOUTH);
    pnlPreview.add(pnlChart, BorderLayout.CENTER);

    getContentPane().add(newMainPanel, BorderLayout.CENTER);
    pack();
  }


  @Override
  public void actionPerformed(ActionEvent ae) {
    super.actionPerformed(ae);
    updateParameterSetFromComponents();
    if (ae.getSource() == btnNextPattern) {
      logger.info(ae.getSource().toString());
      int current = Integer.parseInt(txtCurrentPatternIndex.getText());
      if (current < (maxC)) {
        current++;
      }
      txtCurrentPatternIndex.setText(String.valueOf(current));
      if (cmpPreview.isSelected())
        updatePreview();
    }

    else if (ae.getSource() == btnPrevPattern) {
      logger.info(ae.getSource().toString());
      int current = Integer.parseInt(txtCurrentPatternIndex.getText());
      if (current > (minC)) {
        current--;
      }
      txtCurrentPatternIndex.setText(String.valueOf(current));
      if (cmpPreview.isSelected())
        updatePreview();
    }

    else if (ae.getSource() == cmpPreview) { // TODO: this looks like you can do it simpler
      logger.info(ae.getSource().toString());

      if (cmpPreview.isSelected()) {
        newMainPanel.add(pnlPreview, BorderLayout.CENTER);
        pnlPreview.setVisible(true);
        btnUpdatePreview.setEnabled(cmpPreview.isSelected());
        updatePreview();
        updateMinimumSize();
        pack();
      } else {
        newMainPanel.remove(pnlPreview);
        pnlPreview.setVisible(false);
        btnUpdatePreview.setEnabled(cmpPreview.isSelected());
        updateMinimumSize();
        pack();
      }
    }

    else if (ae.getSource() == txtCurrentPatternIndex) {
      logger.info(ae.getSource().toString());
    }

    else if (ae.getSource() == btnUpdatePreview) {
      updatePreview();
    } else if (ae.getSource() == cmpMergeWidth) {
      // stroke = new BasicStroke(Float.parseFloat(String.valueOf(pMergeWidth.getValue())));
    }

    else if (ae.getSource() == cmpAutoCarbonCbx) {
      btnNextPattern.setEnabled(cmpAutoCarbonCbx.isSelected());
      btnPrevPattern.setEnabled(cmpAutoCarbonCbx.isSelected());
      txtCurrentPatternIndex.setEnabled(cmpAutoCarbonCbx.isSelected());
    }
  }


  private void updatePreview() {
    if (!updateParameters()) {
      logger.warning(
          "Could not update parameters or parameters are invalid. Please check the parameters.");
      return;
    }

    ExtendedIsotopePattern pattern = calculateIsotopePattern();
    if (pattern == null) {
      logger.warning("Could not calculate isotope pattern. Please check the parameters.");
      return;
    }

    updateChart(pattern);
  }

  private void updateChart(ExtendedIsotopePattern pattern) {
    dataset = new ExtendedIsotopePatternDataSet(pattern, minIntensity, mergeWidth);
    logger.info("Series count: " + dataset.getSeriesCount());
    chart =
        ChartFactory.createXYBarChart("Isotope pattern preview", "m/z", false, "Abundance", dataset);
    theme.apply(chart);
    XYPlot plot = chart.getXYPlot();
    plot.addRangeMarker(new ValueMarker(minIntensity, belowMin, new BasicStroke(1.0f)));
    XYItemRenderer r = plot.getRenderer();
    r.setSeriesPaint(0, aboveMin);
    r.setSeriesPaint(1, belowMin);
    r.setDefaultToolTipGenerator(ttGen);
    pnlChart.setChart(chart);
  }

  private boolean updateParameters() {
    updateParameterSetFromComponents();
    autoCarbon = pAutoCarbon.getValue();

    if (!checkParameters())
      return false;

    element = pElement.getValue(); //TODO
    minAbundance = pMinAbundance.getValue() / 100;
    mergeWidth = pMergeWidth.getValue();
    minIntensity = pMinIntensity.getValue();
    charge = pCharge.getValue();

    if (autoCarbon)
      updateAutoCarbonParameters();
    return true;
  }

  private void updateAutoCarbonParameters() {
    minC = pMinC.getValue();
    maxC = pMaxC.getValue();
    minSize = pMinSize.getValue();

    if (Integer.parseInt(txtCurrentPatternIndex.getText()) > maxC)
      txtCurrentPatternIndex.setText(String.valueOf(maxC));
    if (Integer.parseInt(txtCurrentPatternIndex.getText()) < minC)
      txtCurrentPatternIndex.setText(String.valueOf(minC));
    form.setMaximum(maxC);
    form.setMinimum(minC);
  }

  private boolean checkParameters() {
    if (/* pElement.getValue().equals("") */ cmpElement.getText().equals("") && !autoCarbon) {
      logger.info("Element == null and no autoCarbon");
      return false;
    }
    if (pMinAbundance.getValue() / 100 > 1.0 || pMinAbundance.getValue() / 100 < 0.0) {
      logger.info("Minimun abundance invalid.");
      return false;
    }
    if (pMinIntensity.getValue() > 1.0 || pMinIntensity.getValue() < 0.0) {
      logger.info("Minimum intensity invalid.");
      return false;
    }
    logger.info("Parameters valid");
    return true;
  }

  private ExtendedIsotopePattern calculateIsotopePattern() {
    ExtendedIsotopePattern pattern = new ExtendedIsotopePattern();

    if (!checkParameters())
      return null;

    String strPattern = "";
    int currentCarbonPattern = Integer.parseInt(txtCurrentPatternIndex.getText());

    if (autoCarbon)
      strPattern = "C" + String.valueOf(currentCarbonPattern) + element;
    else
      strPattern = element;

    if (strPattern.equals(""))
      return null;
    logger.info("Calculating isotope pattern: " + strPattern);

    // *0.2 so the user can see the peaks below the threshold
    pattern.setUpFromFormula(strPattern, minAbundance, mergeWidth, minIntensity * 0.2);
    PolarityType pol = (charge > 0) ? PolarityType.POSITIVE : PolarityType.NEGATIVE;
    pattern.applyCharge(charge, pol);
    return pattern;
  }
}
