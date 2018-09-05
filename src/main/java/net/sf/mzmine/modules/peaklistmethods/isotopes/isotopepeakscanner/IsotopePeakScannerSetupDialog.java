package net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.experimental.theories.DataPoints;
import net.sf.mzmine.chartbasics.gui.swing.EChartPanel;
import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.PolarityType;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.dialogs.ParameterSetupDialog;
import net.sf.mzmine.parameters.parametertypes.DoubleComponent;
import net.sf.mzmine.parameters.parametertypes.IntegerComponent;
import net.sf.mzmine.parameters.parametertypes.PercentComponent;
import net.sf.mzmine.parameters.parametertypes.StringComponent;

/**
 * 
 * @author Steffen Heuckeroth s_heuc03@uni-muenster.de
 *
 */
public class IsotopePeakScannerSetupDialog extends ParameterSetupDialog {

  private Logger logger = Logger.getLogger(this.getClass().getName());

  private JPanel pnlPreview; // this will contain the preview and navigation panels
  private JPanel pnlButtons; // this will contain the navigation
  private JPanel pnlCheckbox; // this will contain the checkbox to enable the preview

  private EChartPanel pnlChart;
  private JFreeChart chart;
  private XYPlot plot;

  private JButton btnPrevPattern, btnNextPattern;
  private JCheckBox cbxPreview;
  private JFormattedTextField txtCurrentPatternIndex;
  
  NumberFormatter form;

  PercentComponent txtMinAbundance;
  StringComponent txtElement;
  DoubleComponent txtMinIntensity, txtMergeWidth;
  IntegerComponent txtCharge, txtMinC, txtMaxC, txtMinSize;
  
  private double minAbundance, minIntensity, mergeWidth;
  private int charge, minSize, minC, maxC;
  private String element;

  public IsotopePeakScannerSetupDialog(Window parent, boolean valueCheckRequired,
      ParameterSet parameters) {
    super(parent, valueCheckRequired, parameters);

  }

  @Override
  protected void addDialogComponents() {
    super.addDialogComponents();

    minC = 10;// TODO: dont hardcode this
    maxC = 100;
    form = new NumberFormatter(NumberFormat.getInstance());
    form.setValueClass(Integer.class);
    form.setAllowsInvalid(true);
    form.setMinimum(minC);
    form.setMaximum(maxC);

    pnlPreview = new JPanel(new BorderLayout());
    pnlButtons = new JPanel(new FlowLayout());
    pnlCheckbox = new JPanel(new FlowLayout());


    cbxPreview = new JCheckBox("Preview");
    cbxPreview.addActionListener(this);
    cbxPreview.setSelected(false);;
    pnlCheckbox.add(cbxPreview);

    btnPrevPattern = new JButton("Previous");
    btnPrevPattern.addActionListener(this);
    btnPrevPattern.setMinimumSize(btnPrevPattern.getPreferredSize());
    btnPrevPattern.setEnabled(cbxPreview.isSelected());

    txtCurrentPatternIndex = new JFormattedTextField(form);
    txtCurrentPatternIndex.addActionListener(this);
    txtCurrentPatternIndex.setText(String.valueOf((maxC + minC) / 2));
    txtCurrentPatternIndex.setMinimumSize(txtCurrentPatternIndex.getPreferredSize());
    txtCurrentPatternIndex.setEditable(true);
    txtCurrentPatternIndex.setEnabled(cbxPreview.isSelected());

    btnNextPattern = new JButton("Next");
    btnNextPattern.addActionListener(this);
    btnNextPattern.setPreferredSize(btnNextPattern.getMaximumSize());
    btnNextPattern.setEnabled(cbxPreview.isSelected());

    chart = ChartFactory.createXYLineChart("Isotope pattern preview", "m/z", "Abundance",
        new XYSeriesCollection(new XYSeries("")));
    pnlChart = new EChartPanel(chart);
    pnlChart.setBackground(new Color(255, 255, 255));
    pnlChart.setEnabled(cbxPreview.isSelected());


    pnlButtons.add(btnPrevPattern);
    pnlButtons.add(txtCurrentPatternIndex);
    pnlButtons.add(btnNextPattern);


    pnlPreview.add(pnlCheckbox, BorderLayout.NORTH);
    pnlPreview.add(pnlButtons, BorderLayout.SOUTH);
    pnlPreview.add(pnlChart, BorderLayout.CENTER);

    mainPanel.add(pnlPreview, 3, 0, 0, 0, 1, 1);


    txtMinAbundance = (PercentComponent) parametersAndComponents.get("Minimum abundance");
    txtElement = (StringComponent) parametersAndComponents.get("Element pattern");
    txtMinIntensity = (DoubleComponent) parametersAndComponents.get("Min. pattern intensity");
    txtCharge = (IntegerComponent) parametersAndComponents.get("Charge");
    txtMergeWidth = (DoubleComponent) parametersAndComponents.get("Merge width(m/z)");
  }


  @Override
  public void actionPerformed(ActionEvent ae) {
    super.actionPerformed(ae);


    if (ae.getSource() == btnNextPattern) {
      logger.info(ae.getSource().toString());

      int current = Integer.parseInt(txtCurrentPatternIndex.getText());

      if (current < (maxC)) {
        current++;
      }
      txtCurrentPatternIndex.setText(String.valueOf(current));
    } else if (ae.getSource() == btnPrevPattern) {
      logger.info(ae.getSource().toString());

      int current = Integer.parseInt(txtCurrentPatternIndex.getText());

      if (current > (minC)) {
        current--;
      }
      txtCurrentPatternIndex.setText(String.valueOf(current));
    } else if (ae.getSource() == cbxPreview) {
      logger.info(ae.getSource().toString());
      btnNextPattern.setEnabled(cbxPreview.isSelected());
      btnPrevPattern.setEnabled(cbxPreview.isSelected());
      txtCurrentPatternIndex.setEnabled(cbxPreview.isSelected());

      if (cbxPreview.isSelected()) {
        ExtendedIsotopePattern pattern = calculateIsotopePattern();
        XYSeries[] dataset = convertPatternToXYSeries(pattern);
        updateChart(dataset);
      }
    } else if (ae.getSource() == txtCurrentPatternIndex) {
      logger.info(ae.getSource().toString());
    }
  }


  private ExtendedIsotopePattern calculateIsotopePattern() {
    // TODO: how do you read out the parameters from the parent window?
    ExtendedIsotopePattern pattern = new ExtendedIsotopePattern();
    /*
     * pattern.setUpFromFormula(parameterSet.getParameter(IsotopePeakScannerParameters.element).
     * getValue(), parameterSet.getParameter(IsotopePeakScannerParameters.minAbundance).getValue(),
     * parameterSet.getParameter(IsotopePeakScannerParameters.mergeWidth).getValue(),
     * parameterSet.getParameter(IsotopePeakScannerParameters.minPatternIntensity).getValue());
     * pattern.applyCharge(parameterSet.getParameter(IsotopePeakScannerParameters.charge).getValue()
     * , PolarityType.POSITIVE);
     */
    
    //TODO: createUpdateParameters() and checkParameters() then implement auto carbon
    if(txtElement.getText().equals(""))
      return new ExtendedIsotopePattern();
    
    pattern.setUpFromFormula(element, minAbundance, 
        mergeWidth, minIntensity);

    pattern.applyCharge(charge, PolarityType.POSITIVE);
    return pattern;
  }

  private XYSeries[] convertPatternToXYSeries(ExtendedIsotopePattern pattern) {

    DataPoint[] dp = pattern.getDataPoints();
    XYSeries[] xypattern = new XYSeries[dp.length];

    for (int i = 0; i < dp.length; i++) {
      xypattern[i] = new XYSeries(pattern.getDetailedPeakDescription(i));

      xypattern[i].add(dp[i].getMZ()/* - dp[0].getMZ()*/, 0);
      xypattern[i].add(dp[i].getMZ()/* - dp[0].getMZ()*/, dp[i].getIntensity());
    }

    return xypattern;
  }

  private void updateChart(XYSeries[] xypattern) {
    XYSeriesCollection dataset = new XYSeriesCollection();

    for (XYSeries xy : xypattern)
      dataset.addSeries(xy);

    chart = ChartFactory.createXYLineChart("Isotope pattern preview", "m/z", "Abundance", dataset);
    pnlChart.setChart(chart);
  }
  
  private boolean updateParameters()
  {
    if(!checkParameters())
      return false;
    element = txtElement.getText();
    minAbundance = txtMinAbundance.getValue() / 100;
    mergeWidth = Double.parseDouble(txtMergeWidth.getText());
    minIntensity = Double.parseDouble(txtMinIntensity.getText());
    charge = Integer.parseInt(txtCharge.getText());
    return true;
  }
  private boolean checkParameters() {
    if(txtElement.getText().equals(""))
      return false;
    if(txtMinAbundance.getValue() / 100 > 100)
      return false;
    if(Double.parseDouble(txtMinIntensity.getText()) > 1.0)
        return false;
    
    return true;
  }
}
