package net.sf.mzmine.modules.visualization.spectra.datasets;

import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.sun.xml.xsom.impl.scd.Iterators.Map;
import io.github.msdk.MSDKRuntimeException;
import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.IsotopePattern;
/**
 * 
 * @author Steffen
 *
 */
//public class IsotopePatternDataSet extends XYSeriesCollection {
public class IsotopePatternDataSet extends XYSeriesCollection {
  /**
   *
   */
  private static final long serialVersionUID = 1L;
  
  private IsotopePattern pattern;
  private double minAbundance;
  private DataPoint[] dp;
  private XYSeries above;
  private XYSeries below;
  private XYBarDataset barData;
  private double width;
 
  private enum AB {ABOVE, BELOW};
  Assignment assignment[];
  private class Assignment{
    AB ab;
    private int id;
  }
  
  public IsotopePatternDataSet(IsotopePattern pattern, double minAbundance, double width)
  {
    this.pattern = pattern;
    this.minAbundance = minAbundance;
    above = new XYSeries("Above minimum intensity");
    below = new XYSeries("Below minimum intensity");
    
    dp = pattern.getDataPoints();
    assignment = new Assignment[dp.length];
    for(int i = 0; i < assignment.length; i++)
      assignment[i] = new Assignment();
    
    for(int i = 0; i < dp.length ; i++) {
      if(dp[i].getIntensity() < minAbundance) {
        assignment[i].ab = AB.BELOW;
        assignment[i].id = i;
        below.add(dp[i].getMZ(), dp[i].getIntensity());
      }
      else {
        assignment[i].ab = AB.ABOVE;
        assignment[i].id = i;
        above.add(dp[i].getMZ(), dp[i].getIntensity());
      }
    }
    
    super.addSeries(above);
    super.addSeries(below);
    
    
    barData = new XYBarDataset(this, width);
  }
  
  public int getSeriesDpIndex(int index) throws MSDKRuntimeException {
    if(index > dp.length)
      throw new MSDKRuntimeException("Index out of bounds.");
    return assignment[index].id;
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
  }

  public XYBarDataset getBarData() {
    return barData;
  }
}
