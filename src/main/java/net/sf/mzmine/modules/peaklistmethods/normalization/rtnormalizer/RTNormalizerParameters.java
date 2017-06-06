/*
 * Copyright 2006-2015 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.peaklistmethods.normalization.rtnormalizer;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.BooleanParameter;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.StringParameter;
import net.sf.mzmine.parameters.parametertypes.selectors.PeakListsParameter;
import net.sf.mzmine.parameters.parametertypes.tolerances.MZToleranceParameter;
import net.sf.mzmine.parameters.parametertypes.tolerances.RTToleranceParameter;

public class RTNormalizerParameters extends SimpleParameterSet {

    public static final PeakListsParameter peakLists = new PeakListsParameter(2);

    public static final StringParameter suffix = new StringParameter(
	    "Name suffix", "Suffix to be added to peak list name", "normalized");

    public static final MZToleranceParameter MZTolerance = new MZToleranceParameter();

    public static final RTToleranceParameter RTTolerance = new RTToleranceParameter();

    public static final DoubleParameter minHeight = new DoubleParameter(
	    "Minimum standard intensity",
	    "Minimum height of a peak to be selected as normalization standard",
	    MZmineCore.getConfiguration().getIntensityFormat());

    public static final BooleanParameter autoRemove = new BooleanParameter(
	    "Remove original peak list",
	    "If checked, original peak list will be removed and only normalized version remains");

    public RTNormalizerParameters() {
	super(new Parameter[] { peakLists, suffix, MZTolerance, RTTolerance,
		minHeight, autoRemove });
    }

}
