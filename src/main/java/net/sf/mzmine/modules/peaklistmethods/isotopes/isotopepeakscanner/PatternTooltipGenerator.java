package net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;
import net.sf.mzmine.modules.visualization.spectra.datasets.IsotopePatternDataSet;

public class PatternTooltipGenerator implements XYToolTipGenerator {

    IsotopePatternDataSet dataset;
    PatternTooltipGenerator(){
      this.dataset = null;
    }
    PatternTooltipGenerator(IsotopePatternDataSet ds){
      this.setDataset(ds);
    }
    public void setDataset(IsotopePatternDataSet dataset) {
      this.dataset = dataset;
    }
    @Override
    public String generateToolTip(XYDataset data, int series, int item) {
//      if(dataset != null)
//        return dataset.getItemDescription(series, item) + ", Intensity: " + IsotopePeakScannerTask.round(dataset.getY(series, item).doubleValue(), 2);
      if(data instanceof IsotopePatternDataSet) {
        return ((IsotopePatternDataSet)data).getItemDescription(series, item) + ", Intensity: " + IsotopePeakScannerTask.round(dataset.getY(series, item).doubleValue(), 2);
      }
      return "Invalid";
    }
}
