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

package net.sf.mzmine.modules.peaklistmethods.peakpicking.deconvolution.noiseamplitude;

import java.awt.Window;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.peaklistmethods.peakpicking.deconvolution.PeakResolverSetupDialog;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.ranges.DoubleRangeParameter;
import net.sf.mzmine.util.ExitCode;

import com.google.common.collect.Range;

public class NoiseAmplitudePeakDetectorParameters extends SimpleParameterSet {

  public static final DoubleParameter MIN_PEAK_HEIGHT = new DoubleParameter("Min peak height",
      "Minimum acceptable height (intensity) for a chromatographic peak",
      MZmineCore.getConfiguration().getIntensityFormat());

  public static final DoubleRangeParameter PEAK_DURATION =
      new DoubleRangeParameter("Peak duration range (min)", "Range of acceptable peak lengths",
          MZmineCore.getConfiguration().getRTFormat(), Range.closed(0.0, 10.0));

  public static final DoubleParameter NOISE_AMPLITUDE = new DoubleParameter("Amplitude of noise",
      "This value is the intensity amplitude of the signal in the noise region",
      MZmineCore.getConfiguration().getIntensityFormat());

  public NoiseAmplitudePeakDetectorParameters() {
    super(new Parameter[] {MIN_PEAK_HEIGHT, PEAK_DURATION, NOISE_AMPLITUDE});
  }

  @Override
  public ExitCode showSetupDialog(Window parent, boolean valueCheckRequired) {
    final PeakResolverSetupDialog dialog = new PeakResolverSetupDialog(parent, valueCheckRequired,
        this, NoiseAmplitudePeakDetector.class);
    dialog.setVisible(true);
    return dialog.getExitCode();
  }
}
