package net.sf.mzmine.modules.visualization.spectra.datasets;

import java.util.ArrayList;
import java.util.List;
import org.jfree.data.xy.IntervalXYDelegate;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.sun.xml.xsom.impl.scd.Iterators.Map;
import io.github.msdk.MSDKRuntimeException;
import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.IsotopePattern;
import net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner.ExtendedIsotopePattern;
/**
 * 
 * @author Steffen
 *
 */
//public class IsotopePatternDataSet extends XYBarDataset {
public class IsotopePatternDataSet extends XYSeriesCollection {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private ExtendedIsotopePattern pattern;
  private double minAbundance;
  private DataPoint[] dp;
  private XYSeries above;
  private XYSeries below;
  private XYBarDataset barData;
  private double width;
  private List <String> descrBelow, descrAbove;
  private IntervalXYDelegate intervalDelegate;
  
  private enum AB {ABOVE, BELOW};
  Assignment assignment[];
  private class Assignment{
    AB ab;
    private int id;
  }
  
  public IsotopePatternDataSet(ExtendedIsotopePattern pattern, double minAbundance, double width)
  {
//    super(pattern.getDescription(), pattern.getDataPoints());    
    this.pattern = pattern;
    this.minAbundance = minAbundance;
    above = new XYSeries("Above minimum intensity");
    below = new XYSeries("Below minimum intensity");
    descrBelow = new ArrayList <String>();
    descrAbove = new ArrayList <String>();
    
    dp = pattern.getDataPoints();
    assignment = new Assignment[dp.length];
    for(int i = 0; i < assignment.length; i++)
      assignment[i] = new Assignment();
    
    for(int i = 0; i < dp.length ; i++) {
      if(dp[i].getIntensity() < minAbundance) {
        assignment[i].ab = AB.BELOW;
        assignment[i].id = i;
        below.add(dp[i].getMZ(), dp[i].getIntensity());
        descrBelow.add(pattern.getDetailedPeakDescription(i));
      }
      else {
        assignment[i].ab = AB.ABOVE;
        assignment[i].id = i;
        above.add(dp[i].getMZ(), dp[i].getIntensity());
        descrAbove.add(pattern.getDetailedPeakDescription(i));
      }
    }
    
    this.intervalDelegate = new IntervalXYDelegate(this);
    this.intervalDelegate.setFixedIntervalWidth(width);
    super.addSeries(above);
    super.addSeries(below);
    
    
    barData = new XYBarDataset(this, width);
  }
  
  public String getItemDescription(int series, int item) {
    if(series == 0 && item < descrAbove.size())
      return descrAbove.get(item);
    if(series == 1 && item < descrBelow.size())
      return descrBelow.get(item);
    
    return "Invalid series/index";
    
  }
  
  public int getSeriesDpIndex(int item) throws MSDKRuntimeException {
    if(item > dp.length)
      throw new MSDKRuntimeException("Index out of bounds.");
    return assignment[item].id;
  }
  
  public AB getSeriesAB(int index) throws MSDKRuntimeException {
    if(index > dp.length)
      throw new MSDKRuntimeException("Index out of bounds.");
    return assignment[index].ab;
  }
  
  public IsotopePattern getIsotopePattern() {
    return pattern;
  }
  
  public XYBarDataset getBarDataset(double mergeWidth) {

    return new XYBarDataset((XYSeriesCollection)this, mergeWidth);
  }

  public double getWidth() {
    return width;
  }

  public void setWidth(double width) {
    this.width = width;
    barData.setBarWidth(width);
    this.intervalDelegate.setFixedIntervalWidth(width);
  }

  public XYBarDataset getBarData() {
    return barData;
  }
  
//  @Override
//  public Number getStartX(int series, int item) {
//    System.out.println("getStartX called.");
//    return (this.getX(series, item).doubleValue() - width/2);
//  }
//  
//  @Override
//  public Number getEndX(int series, int item) {
//    System.out.println("getEndX called.");
//    return (this.getX(series, item).doubleValue() + width/2);
//  }
  @Override
  public double getStartXValue(int series, int item) {
    return intervalDelegate.getStartXValue(series, item);
  }
  
  @Override
  public double getEndXValue(int series, int item) {
    return intervalDelegate.getEndXValue(series, item);
  }
}
