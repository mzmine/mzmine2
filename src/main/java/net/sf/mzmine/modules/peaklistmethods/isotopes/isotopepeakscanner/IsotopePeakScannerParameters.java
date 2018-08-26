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

package net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner;

import java.text.DecimalFormat;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.BooleanParameter;
import net.sf.mzmine.parameters.parametertypes.ComboParameter;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.IntegerParameter;
import net.sf.mzmine.parameters.parametertypes.MassListParameter;
import net.sf.mzmine.parameters.parametertypes.OptionalParameter;
import net.sf.mzmine.parameters.parametertypes.PercentParameter;
import net.sf.mzmine.parameters.parametertypes.StringParameter;
import net.sf.mzmine.parameters.parametertypes.selectors.PeakListsParameter;
import net.sf.mzmine.parameters.parametertypes.tolerances.MZToleranceParameter;
import net.sf.mzmine.parameters.parametertypes.tolerances.RTToleranceParameter;

public class IsotopePeakScannerParameters extends SimpleParameterSet {

  public static final PeakListsParameter PEAK_LISTS = new PeakListsParameter();

  public static final MZToleranceParameter mzTolerance = new MZToleranceParameter();

  public static final BooleanParameter checkRT =
      new BooleanParameter("Check RT", "Compare rt of peaks to parent.");

  public static final RTToleranceParameter rtTolerance = new RTToleranceParameter();

  public static final StringParameter element = new StringParameter("Element pattern",
      "Element (combination) whose isotope pattern to be searched for. Please enter the two letter Symbol. (e.g. \"Gd\", \"Cl2Br\"S)", "", false);

  public static final PercentParameter minAbundance = new PercentParameter("Minimum abundance",
      "The minimum abundance (%) of Isotopes. Small values "
          + "might increase accuracy but will decrease sensitivity.", 0.01);

  public static final DoubleParameter minHeight = new DoubleParameter("Minimum height",
      "Minimum peak height to be considered as an isotope peak.",
      MZmineCore.getConfiguration().getIntensityFormat(), 1E4);

  public static final DoubleParameter mergeWidth = new DoubleParameter("Merge width(m/z)",
      "This will be used to merge peaks in the calculated isotope pattern if they overlap in the spectrum. Specify in m/z, this depends on the resolution of your mass spectrometer.",
      MZmineCore.getConfiguration().getMZFormat(), 0.0005, 1E-7, 10.0);

  public static final DoubleParameter minPatternIntensity = new DoubleParameter(
      "Min. pattern intensity",
      "The minimum normalized intensity of a peak in the final calculated isotope pattern. Depends on the sensitivity of your MS.\nMin = 0.0, Max = 0.99...",
      new DecimalFormat("0.####"), 0.01, 0.0, 0.99999);

  public static final BooleanParameter checkIntensity = new BooleanParameter("Check intensity",
      "Compare intensity of peaks to the calculated abundance.", true);

  public static final DoubleParameter minRating = new DoubleParameter("Minimun rating",
      "Minimum rating to be considered as an isotope peak. min = 0.0, max = 1.0",
      new DecimalFormat("0.####"), 0.90, 0.0, 1.0);

  public static final String[] ratingTypeChoices = {"Highest intensity", "Temporary average"};

  public static final ComboParameter<String> ratingChoices = new ComboParameter<String>(
      "Rating type",
      "Method to calculate the rating with.\nHighest Intensity is the standard method and faster.\nAverage is slower but could be more accurate for some peaks. Select a masslist.",
      ratingTypeChoices);

  public static final StringParameter suffix = new StringParameter("Name suffix",
      "Suffix to be added to peak list name. If \"auto\" then this module will create a suffix.",
      "auto");

  public static final IntegerParameter charge =
      new IntegerParameter("Charge", "Amount and polarity (e.g.: [M]+=+1 / [M]-=-1", 1, true);
  
  public static final IntegerParameter minCarbon = new IntegerParameter("Min. carbon", "Minumum amount of carbon to search for.", 1, true);
  public static final IntegerParameter maxCarbon = new IntegerParameter("Max. carbon", "Maximum amount of carbon to search for.", 1, true);

  public static final OptionalParameter<MassListParameter> massList =
      new OptionalParameter<MassListParameter>(new MassListParameter("Calculate accurate average",
          "Please select a mass list.\nThis method will use averaged intensitys over all mass lists in which ALL relevant masses were detected in.\nThis will only be done for peaks that match the defined rating-calculation with the given rating.\nMake sure the mass list is contained in the peak list.\nIf there are no Scans that match all criteria avg rating will be -1.0."));

  public IsotopePeakScannerParameters() {
    super(new Parameter[] {PEAK_LISTS, mzTolerance, checkRT, rtTolerance, minCarbon, maxCarbon, element, charge,
        minAbundance, minPatternIntensity, mergeWidth, minHeight, checkIntensity, minRating,
        ratingChoices, massList, suffix});
  }
}
