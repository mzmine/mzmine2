/* 
 * Copyright (C) 2016 Du-Lab Team <dulab.binf@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.mzmine.modules.peaklistmethods.peakpicking.adap3decompositionV2;

import java.awt.Window;
import java.text.NumberFormat;

import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.BooleanParameter;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.IntegerParameter;
import net.sf.mzmine.parameters.parametertypes.StringParameter;
import net.sf.mzmine.parameters.parametertypes.selectors.PeakListsParameter;
import net.sf.mzmine.parameters.parametertypes.selectors.PeakListsSelectionType;
import net.sf.mzmine.util.ExitCode;

/**
 *
 * @author aleksandrsmirnov
 */
public class ADAP3DecompositionV2Parameters extends SimpleParameterSet {

    public static final PeakListsParameter CHROMATOGRAM_LISTS =
            new PeakListsParameter("Chromatograms", 1, Integer.MAX_VALUE);

    public static final PeakListsParameter PEAK_LISTS =
            new PeakListsParameter("Peaks", 1, Integer.MAX_VALUE);
    
    // ------------------------------------------------------------------------
    // ----- First-phase parameters -------------------------------------------
    // ------------------------------------------------------------------------

    public static final DoubleParameter PREF_WINDOW_WIDTH =
            new DoubleParameter("Preferred window width (min)",
                    "Preferred width of a deconvolution window (in minutes). The algorithm will try to " +
                            "select windows of the given width, but it may not be able to do so.",
                    NumberFormat.getNumberInstance(), 0.05);
    
    public static final IntegerParameter MIN_NUM_PEAK =
            new IntegerParameter("Min number peaks",
                    "Minimum number of detected peaks in a window",
                    5);
    
    // ------------------------------------------------------------------------
    // ----- End of First-phase parameters ------------------------------------
    // ------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------
    // ----- Second-phase parameters ------------------------------------------
    // ------------------------------------------------------------------------

    public static final DoubleParameter RET_TIME_TOLERANCE = new DoubleParameter("Retention time tolerance (min)",
            "Retention time tolerance value (between 0 and 1) is used for determine the number of components" +
                    " in a window. The larger tolerance, the smaller components are determined.",
            NumberFormat.getNumberInstance(), 0.05, 0.0, Double.MAX_VALUE);

    public static final BooleanParameter SMOOTHING = new BooleanParameter("Smoothed elution profiles",
            "If this option is checked, the elution profiles of components are smoothed", false);

    public static final BooleanParameter UNIMODALITY = new BooleanParameter("Unimodal elution profiles",
            "If this option is checked, the elution profiles have a single local maximum", false);

    // ------------------------------------------------------------------------
    // ----- End of Second-phase parameters -----------------------------------
    // ------------------------------------------------------------------------
    
    public static final StringParameter SUFFIX = new StringParameter("Suffix",
	    "This string is added to peak list name as suffix", "Spectral Deconvolution");
    
    public static final BooleanParameter AUTO_REMOVE = new BooleanParameter(
	    "Remove original peak lists",
	    "If checked, original chromomatogram and peak lists will be removed");
    
    public ADAP3DecompositionV2Parameters() {
	    super(new Parameter[] {CHROMATOGRAM_LISTS, PEAK_LISTS, PREF_WINDOW_WIDTH, MIN_NUM_PEAK,
                RET_TIME_TOLERANCE, SMOOTHING, UNIMODALITY, SUFFIX, AUTO_REMOVE});
    }
    
    @Override
    public ExitCode showSetupDialog(Window parent, boolean valueCheckRequired)
    {
        CHROMATOGRAM_LISTS.setValue(PeakListsSelectionType.SPECIFIC_PEAKLISTS);
        PEAK_LISTS.setValue(PeakListsSelectionType.SPECIFIC_PEAKLISTS);

        final ADAP3DecompositionV2SetupDialog dialog =
                new ADAP3DecompositionV2SetupDialog(
                        parent, valueCheckRequired, this);
        
        dialog.setVisible(true);
        return dialog.getExitCode();
    }
}
