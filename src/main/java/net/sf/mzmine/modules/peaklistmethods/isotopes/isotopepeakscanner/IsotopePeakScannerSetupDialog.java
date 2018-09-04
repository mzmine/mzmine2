package net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
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
/**
 * 
 * @author Steffen Heuckeroth s_heuc03@uni-muenster.de
 *
 */
public class IsotopePeakScannerSetupDialog extends ParameterSetupDialog {
  
  private Logger logger = Logger.getLogger(this.getClass().getName());
  
  private JPanel pnlPreview;  // this will contain the preview and navigation panels
  private JPanel pnlButtons;  // this will contain the navigation
  private JPanel pnlCheckbox; // this will contain the checkbox to enable the preview
  
  private EChartPanel pnlChart;
  private JFreeChart chart;
  private XYPlot plot;
  
  
  
  private JButton btnPrevPattern, btnNextPattern;
  private JCheckBox cbxPreview;
  private JFormattedTextField txtCurrentPatternIndex;
  
  private int minC, maxC;
  
  NumberFormatter form;
  
  public IsotopePeakScannerSetupDialog(Window parent, boolean valueCheckRequired,
      ParameterSet parameters) {
    super(parent, valueCheckRequired, parameters);
  }
  
  @Override
  protected void addDialogComponents() {
    super.addDialogComponents();
    
    minC = 10;//TODO: dont hardcode this
    maxC = 100;
    form = new NumberFormatter(NumberFormat.getInstance());
    form.setValueClass(Integer.class);
    form.setAllowsInvalid(false);
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
    txtCurrentPatternIndex.setText("" + maxC);
    txtCurrentPatternIndex.setMinimumSize(txtCurrentPatternIndex.getPreferredSize());
    txtCurrentPatternIndex.setText("");
    txtCurrentPatternIndex.setEditable(true);
    txtCurrentPatternIndex.setEnabled(cbxPreview.isSelected());
    
    btnNextPattern = new JButton("Next");
    btnNextPattern.addActionListener(this);
    btnNextPattern.setPreferredSize(btnNextPattern.getMaximumSize());
    btnNextPattern.setEnabled(cbxPreview.isSelected());
    
    chart = ChartFactory.createXYLineChart("Isotope pattern preview", "m/z", "Abundance", new XYSeriesCollection(new XYSeries("penis")));
    pnlChart= new EChartPanel(chart);
    pnlChart.setEnabled(cbxPreview.isSelected());
    
    
    
    pnlButtons.add(btnPrevPattern);
    pnlButtons.add(txtCurrentPatternIndex);
    pnlButtons.add(btnNextPattern);

    
    pnlPreview.add(pnlCheckbox, BorderLayout.NORTH);
    pnlPreview.add(pnlButtons, BorderLayout.SOUTH);
    pnlPreview.add(pnlChart, BorderLayout.CENTER);
    
    mainPanel.add(pnlPreview, 3, 0, 0, 0, 1, 1); 
  }
  

  @Override
  public void actionPerformed(ActionEvent ae) {
    super.actionPerformed(ae);
    
    if(ae.getSource() == btnNextPattern) {
      logger.info(ae.getSource().toString());
      
      int current = Integer.parseInt(txtCurrentPatternIndex.getText());
      
      if(current < (maxC)) {
        current++;
      }
      txtCurrentPatternIndex.setText(String.valueOf(current));
    }
    else if(ae.getSource() == btnPrevPattern) {
      logger.info(ae.getSource().toString());
      
      int current = Integer.parseInt(txtCurrentPatternIndex.getText());
      
      if(current > (minC)) {
        current--;
      }
      txtCurrentPatternIndex.setText(String.valueOf(current));
    }
    else if(ae.getSource() == cbxPreview) {
      logger.info(ae.getSource().toString());
      btnNextPattern.setEnabled(cbxPreview.isSelected());
      btnPrevPattern.setEnabled(cbxPreview.isSelected());
      txtCurrentPatternIndex.setEnabled(cbxPreview.isSelected());
      
      if(cbxPreview.isSelected()) {
        ExtendedIsotopePattern pattern = calculateIsotopePattern();
        XYSeries[] dataset = convertPatternToXYSeries(pattern);
        updateChart(dataset);
      }
    }  
    else if(ae.getSource() == txtCurrentPatternIndex) {
      logger.info(ae.getSource().toString());
    }
  }
  
  private ExtendedIsotopePattern calculateIsotopePattern() {
    ExtendedIsotopePattern pattern = new ExtendedIsotopePattern();
    pattern.setUpFromFormula("C16Cl2Br5H20", 0.01, 0.05, 0.02);
    pattern.applyCharge(1, PolarityType.POSITIVE);
    return pattern;
  }
  
  private XYSeries[] convertPatternToXYSeries(ExtendedIsotopePattern pattern) {
    //TODO: how do you read out the parameters from the window?
    
    DataPoint[] dp = pattern.getDataPoints();
    
    XYSeries[] xypattern = new XYSeries[dp.length];
    
    for(int i = 0; i < dp.length; i++) {
      xypattern[i] = new XYSeries(pattern.getSimplePeakDescription(i));
      
      xypattern[i].add(dp[i].getMZ(), 0);
      xypattern[i].add(dp[i].getMZ(), dp[i].getIntensity());
    }
    
    return xypattern;
  }
  
  private void updateChart(XYSeries[] xypattern) {
    XYSeriesCollection dataset = new XYSeriesCollection();
    for(XYSeries xy : xypattern)
      dataset.addSeries(xy);
    
    chart = ChartFactory.createXYLineChart("Isotope pattern preview", "m/z", "Abundance", dataset);
    pnlChart.setChart(chart);
  }
}