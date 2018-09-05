package net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Checkbox;
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
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.awt.Component;
import net.sf.mzmine.chartbasics.gui.swing.EChartPanel;
import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.PolarityType;
import net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner.autocarbon.AutoCarbonParameters;
import net.sf.mzmine.parameters.Parameter;
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

  private JButton btnPrevPattern, btnNextPattern, btnUpdatePerview;
  private JCheckBox cbxPreview;
  private JFormattedTextField txtCurrentPatternIndex;
  
  NumberFormatter form;

  PercentComponent cmpMinAbundance;
  DoubleComponent cmpMinIntensity, cmpMergeWidth;
  IntegerComponent cmpCharge, cmpMinC, cmpMaxC, cmpMinSize;
  StringComponent cmpElement;
  OptionalModuleComponent cmpAutoCarbon;
  JCheckBox cmpAutoCarbonCbx;
  
  IntegerParameter pMinC, pMaxC, pMinSize, pCharge;
  StringParameter pElement;
  DoubleParameter pMinIntensity, pMergeWidth;
  PercentParameter pMinAbundance;
  OptionalModuleParameter pAutoCarbon;
 
  Color aboveMin, belowMin;
//  BasicStroke stroke;
  
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
//    stroke = new BasicStroke((float)mergeWidth);
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
    
    btnUpdatePerview = new JButton("Update");
    btnUpdatePerview.addActionListener(this);
    btnUpdatePerview.setPreferredSize(btnUpdatePerview.getMinimumSize());
    btnUpdatePerview.setEnabled(true);

    chart = ChartFactory.createXYLineChart("Isotope pattern preview", "m/z", "Abundance",
        new XYSeriesCollection(new XYSeries("")));
    pnlChart = new EChartPanel(chart);
    pnlChart.setEnabled(cbxPreview.isSelected());


    pnlButtons.add(btnPrevPattern);
    pnlButtons.add(txtCurrentPatternIndex);
    pnlButtons.add(btnNextPattern);
    pnlButtons.add(btnUpdatePerview);


    pnlPreview.add(pnlCheckbox, BorderLayout.NORTH);
    pnlPreview.add(pnlButtons, BorderLayout.SOUTH);
    pnlPreview.add(pnlChart, BorderLayout.CENTER);

    mainPanel.add(pnlPreview, 3, 0, 0, 0, 1, 1);
    
    
    cmpMinAbundance = (PercentComponent) this.getComponentForParameter(IsotopePeakScannerParameters.minAbundance);
    cmpMinIntensity = (DoubleComponent) this.getComponentForParameter(IsotopePeakScannerParameters.minPatternIntensity);
    cmpCharge = (IntegerComponent) this.getComponentForParameter(IsotopePeakScannerParameters.charge);
    cmpMergeWidth = (DoubleComponent) this.getComponentForParameter(IsotopePeakScannerParameters.mergeWidth);
    cmpElement = (StringComponent) this.getComponentForParameter(IsotopePeakScannerParameters.element);
    cmpAutoCarbon = (OptionalModuleComponent) this.getComponentForParameter(IsotopePeakScannerParameters.autoCarbonOpt);
    cmpAutoCarbonCbx = (JCheckBox) cmpAutoCarbon.getComponent(0);
    
    
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
  }


  @Override
  public void actionPerformed(ActionEvent ae) {
    super.actionPerformed(ae);
    setParameterValuesFromComponents();
    if (ae.getSource() == btnNextPattern) {
      logger.info(ae.getSource().toString());
      int current = Integer.parseInt(txtCurrentPatternIndex.getText());
      if (current < (maxC)) {
        current++;
      }
      txtCurrentPatternIndex.setText(String.valueOf(current));
      if (cbxPreview.isSelected())
        updatePreview();
    } 
    
    else if (ae.getSource() == btnPrevPattern) {
      logger.info(ae.getSource().toString());
      int current = Integer.parseInt(txtCurrentPatternIndex.getText());
      if (current > (minC)) {
        current--;
      }
      txtCurrentPatternIndex.setText(String.valueOf(current));
      if (cbxPreview.isSelected())
        updatePreview();
    } 
    
    else if (ae.getSource() == cbxPreview) { // TODO: this looks like you can do it simpler
      logger.info(ae.getSource().toString());
      if(cmpAutoCarbonCbx.isSelected()) {
        btnNextPattern.setEnabled(cbxPreview.isSelected());
        btnPrevPattern.setEnabled(cbxPreview.isSelected());
        txtCurrentPatternIndex.setEnabled(cbxPreview.isSelected());
      }
      else { // false
        btnNextPattern.setEnabled(cmpAutoCarbonCbx.isSelected());
        btnPrevPattern.setEnabled(cmpAutoCarbonCbx.isSelected());
        txtCurrentPatternIndex.setEnabled(cmpAutoCarbonCbx.isSelected());
      }
      if (cbxPreview.isSelected()) {
        btnUpdatePerview.setEnabled(cbxPreview.isSelected());
        updatePreview();
      }
      else
        btnUpdatePerview.setEnabled(cbxPreview.isSelected());
    }
    
    else if (ae.getSource() == txtCurrentPatternIndex) {
      logger.info(ae.getSource().toString());
    }
    
    else if(ae.getSource() == btnUpdatePerview) {
      updatePreview();
    }
    else if(ae.getSource() == cmpMergeWidth) {
//      stroke = new BasicStroke(Float.parseFloat(String.valueOf(pMergeWidth.getValue())));
    }
    
    else if(ae.getSource() == cmpAutoCarbonCbx) { // checkbox is added first therefore index 0
        if(!cmpAutoCarbonCbx.isSelected() && !cbxPreview.isSelected()) // TODO: this looks like you can do it simpler
          btnUpdatePerview.setEnabled(false);
        else if(!cmpAutoCarbonCbx.isSelected() && cbxPreview.isSelected())
          btnUpdatePerview.setEnabled(true);
        else if(cmpAutoCarbonCbx.isSelected() && cbxPreview.isSelected())
          btnUpdatePerview.setEnabled(true);
        else if(cmpAutoCarbonCbx.isSelected() && !cbxPreview.isSelected())
          btnUpdatePerview.setEnabled(false);
    }
    
    if(pAutoCarbon.getValue() == false || (pAutoCarbon.getValue() == true && cbxPreview.isSelected())) {
      
    }
  }


  private void updatePreview() {
    if(!updateParameters()) {
      logger.warning("Could not update parameters or parameters are invalid. Please check the parameters.");
      return;
    }
    
    ExtendedIsotopePattern pattern = calculateIsotopePattern();
    if(pattern == null) {
      logger.warning("Could not calculate isotope pattern. Please check the parameters.");
      return;
    }
    
    XYSeries[] dataset = convertPatternToXYSeries(pattern);
    updateChart(dataset);
  }

  private void updateChart(XYSeries[] xypattern) {
    XYSeriesCollection dataset = new XYSeriesCollection();
    for (XYSeries xy : xypattern)
      dataset.addSeries(xy);
    chart = ChartFactory.createXYLineChart("Isotope pattern preview", "m/z", "Abundance", dataset);
    Plot plot = chart.getPlot();
    
    if(plot instanceof XYPlot) {
      XYItemRenderer r = ((XYPlot)plot).getRenderer();
      
      for(int p = 1; p < xypattern.length; p++) { // using p because its a pattern. starting at 1 because 0 is the minimum intensity line
        if(xypattern[p].getMaxY() < minIntensity)
          r.setSeriesPaint(p, belowMin);
        else
          r.setSeriesPaint(p, aboveMin);
//        r.setSeriesStroke(p, stroke, false);
      }
    }
    pnlChart.setChart(chart);
  }
  
  private void setParameterValuesFromComponents() {
    pMinAbundance.setValueFromComponent(cmpMinAbundance);
    pElement.setValueFromComponent(cmpElement);
    pMinIntensity.setValueFromComponent(cmpMinIntensity);
    pCharge.setValueFromComponent(cmpCharge);
    pMergeWidth.setValueFromComponent(cmpMergeWidth);
    pAutoCarbon.setValueFromComponent(cmpAutoCarbon);
    
    /*autoCarbonParameters = pAutoCarbon.getEmbeddedParameters();
    pMinC = autoCarbonParameters.getParameter(AutoCarbonParameters.minCarbon);
    pMaxC = autoCarbonParameters.getParameter(AutoCarbonParameters.maxCarbon);
    pMinSize = autoCarbonParameters.getParameter(AutoCarbonParameters.minPatternSize);*/    
  }
  
  private boolean updateParameters()
  {
    setParameterValuesFromComponents();
    autoCarbon = pAutoCarbon.getValue();
    
    if(!checkParameters())
      return false;
    
    /*element = cmpElement.getText();
    minAbundance = cmpMinAbundance.getValue() / 100;
    mergeWidth = Double.parseDouble(cmpMergeWidth.getText());
    minIntensity = Double.parseDouble(cmpMinIntensity.getText());
    charge = Integer.parseInt(cmpCharge.getText());*/
    
    //element = pElement.getValue(); //TODO: Why does this return an empty string all the time?
    element = cmpElement.getText();
    minAbundance = pMinAbundance.getValue() / 100;
    mergeWidth = pMergeWidth.getValue();
    minIntensity = pMinIntensity.getValue();
    charge = pCharge.getValue();
    
    if(autoCarbon)
      updateAutoCarbonParameters();
    
    logger.info("new parameter values:\n"
        + "element: " + element + "\tMinimum abundance: " + minAbundance
        + "\nMerge width: " + mergeWidth + "\tMinimum intensity: " + minIntensity
        + "\nCharge: " + charge + "\tAuto carbon: " + autoCarbon
        + "\nMinimum C: " + minC + "\tMaximum C: " + maxC);
    return true;
  }
  
  private void updateAutoCarbonParameters() {
    minC = pMinC.getValue();
    maxC = pMaxC.getValue();
    minSize = pMinSize.getValue();
    
    if(Integer.parseInt(txtCurrentPatternIndex.getText()) > maxC)
      txtCurrentPatternIndex.setText(String.valueOf(maxC));
    if(Integer.parseInt(txtCurrentPatternIndex.getText()) < minC)
      txtCurrentPatternIndex.setText(String.valueOf(minC));
    form.setMaximum(maxC);
    form.setMinimum(minC);
  }
  
  private boolean checkParameters() {
    if(/*pElement.getValue().equals("")*/ cmpElement.getText().equals("") && !autoCarbon) {
      logger.info("Element == null and no autoCarbon");
      return false;
    }
    if(pMinAbundance.getValue() / 100 > 1.0 || pMinAbundance.getValue() / 100 < 0.0) {
      logger.info("Minimun abundance invalid.");
      return false;
    }
    if(pMinIntensity.getValue() > 1.0 || pMinIntensity.getValue() < 0.0) {
      logger.info("Minimum intensity invalid.");
      return false;
    }
    logger.info("Parameters valid");
    return true;
  }
  
  private ExtendedIsotopePattern calculateIsotopePattern() {
    ExtendedIsotopePattern pattern = new ExtendedIsotopePattern();
    
    if(!checkParameters())
      return null;
    
    String strPattern = "";
    int currentCarbonPattern = Integer.parseInt(txtCurrentPatternIndex.getText());
    
    if(autoCarbon)
      strPattern = "C" + String.valueOf(currentCarbonPattern) + element;
    else
      strPattern = element;
    
    if(strPattern.equals(""))
      return null;
    logger.info("Calculating isotope pattern: " + strPattern);
    
    pattern.setUpFromFormula(strPattern, minAbundance, 
        mergeWidth, minIntensity*0.2); // *0.2 so the user can see the peaks below the threshold
    pattern.applyCharge(charge, PolarityType.POSITIVE);
    return pattern;
  }

  private XYSeries[] convertPatternToXYSeries(ExtendedIsotopePattern pattern) {

    DataPoint[] dp = pattern.getDataPoints();
    XYSeries[] xypattern = new XYSeries[dp.length+1];

    xypattern[0] = new XYSeries("Minimum Intensity");
    xypattern[0].add(dp[0].getMZ()-0.5, minIntensity);
    xypattern[0].add(dp[dp.length-1].getMZ()+0.5, minIntensity);
    xypattern[0].setDescription("this is a test");
    
    for (int i = 1; i < xypattern.length; i++) {
      xypattern[i] = new XYSeries(pattern.getDetailedPeakDescription(i-1));

      xypattern[i].add(dp[i-1].getMZ()/* - dp[0].getMZ()*/, 0);
      xypattern[i].add(dp[i-1].getMZ()/* - dp[0].getMZ()*/, dp[i-1].getIntensity());
    }
    return xypattern;
  }
}
