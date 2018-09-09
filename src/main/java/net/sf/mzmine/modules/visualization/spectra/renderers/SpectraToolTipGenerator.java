/*
 * Copyright 2006-2018 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package net.sf.mzmine.modules.visualization.spectra.renderers;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import net.sf.mzmine.datamodel.Feature;
import net.sf.mzmine.datamodel.IsotopePattern;
import net.sf.mzmine.datamodel.PeakList;
import net.sf.mzmine.datamodel.PeakListRow;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner.IsotopePeakScannerTask;
import net.sf.mzmine.modules.visualization.spectra.datasets.ExtendedIsotopePatternDataSet;
import net.sf.mzmine.modules.visualization.spectra.datasets.IsotopesDataSet;
import net.sf.mzmine.modules.visualization.spectra.datasets.PeakListDataSet;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

/**
 * Tooltip generator for raw data points
 */
public class SpectraToolTipGenerator implements XYToolTipGenerator {

  private NumberFormat mzFormat = MZmineCore.getConfiguration().getMZFormat();
  private NumberFormat intensityFormat = MZmineCore.getConfiguration().getIntensityFormat();
  private NumberFormat percentFormat = new DecimalFormat("0.00");

  /**
   * @see org.jfree.chart.labels.XYToolTipGenerator#generateToolTip(org.jfree.data.xy.XYDataset,
   *      int, int)
   */
  public String generateToolTip(XYDataset dataset, int series, int item) {

    double intValue = dataset.getYValue(series, item);
    double mzValue = dataset.getXValue(series, item);

    if (dataset instanceof PeakListDataSet) {

      PeakListDataSet peakListDataSet = (PeakListDataSet) dataset;

      Feature peak = peakListDataSet.getPeak(series, item);

      PeakList peakList = peakListDataSet.getPeakList();
      PeakListRow row = peakList.getPeakRow(peak);

      String tooltip = "Peak: " + peak + "\nStatus: " + peak.getFeatureStatus()
          + "\nPeak list row: " + row + "\nData point m/z: " + mzFormat.format(mzValue)
          + "\nData point intensity: " + intensityFormat.format(intValue);

      return tooltip;

    }

    if (dataset instanceof IsotopesDataSet) {

      IsotopesDataSet isotopeDataSet = (IsotopesDataSet) dataset;

      IsotopePattern pattern = isotopeDataSet.getIsotopePattern();
      double relativeIntensity = intValue / pattern.getHighestDataPoint().getIntensity() * 100;

      String tooltip = "Isotope pattern: " + pattern.getDescription() + "\nStatus: "
          + pattern.getStatus() + "\nData point m/z: " + mzFormat.format(mzValue)
          + "\nData point intensity: " + intensityFormat.format(intValue) + "\nRelative intensity: "
          + percentFormat.format(relativeIntensity) + "%";

      return tooltip;

    }
    
    if(dataset instanceof ExtendedIsotopePatternDataSet) {
      return "Isotope pattern: " + ((ExtendedIsotopePatternDataSet)dataset).getIsotopePattern().getDescription()
          + "\nm/z: " + mzFormat.format(mzValue)
          + "\nIdentity: " + ((ExtendedIsotopePatternDataSet)dataset).getItemDescription(series, item) 
          + "\nRelative intensity: " + percentFormat.format((dataset.getY(series, item).doubleValue() * 100)) + "%";
    }

    String tooltip =
        "m/z: " + mzFormat.format(mzValue) + "\nIntensity: " + intensityFormat.format(intValue);

    return tooltip;

  }
}
