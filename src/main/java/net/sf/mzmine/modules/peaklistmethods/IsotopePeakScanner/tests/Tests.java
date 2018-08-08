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

package net.sf.mzmine.modules.peaklistmethods.IsotopePeakScanner.tests;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.formula.MolecularFormulaGenerator;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IElement;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import net.sf.mzmine.datamodel.DataPoint;
import net.sf.mzmine.datamodel.IsotopePattern;
import net.sf.mzmine.datamodel.PolarityType;
import net.sf.mzmine.datamodel.impl.SimpleIsotopePattern;
import net.sf.mzmine.modules.peaklistmethods.isotopes.isotopeprediction.IsotopePatternCalculator;

public class Tests {

	public static void main(String[] args) {
		//isotopePatternTest();
		exIPT("Cl");
		//isotopePatternTest();
	}
		
	private static void isotopePatternTest(String formula)
	{
		IsotopePattern pattern = IsotopePatternCalculator.calculateIsotopePattern(formula, 0.05, 1, PolarityType.NEGATIVE);
		pattern = IsotopePatternCalculator.mergeIsotopes(pattern, 0.0005);
		int size = pattern.getNumberOfDataPoints();
		System.out.println("size: " + size);
		
		pattern = IsotopePatternCalculator.normalizeIsotopePattern(pattern, 0, 1);
		DataPoint[] points2 = pattern.getDataPoints();
		//System.out.println(pattern.getDescription());

		for(int i = 0; i<points2.length; i++)
		{
			System.out.println(formula + "Peak " + i + ": m/z: " + points2[i].getMZ() + "\tI: " + points2[i].getIntensity());
		}
		System.out.println(getIntensityRatios(pattern));
	}
	public static void exIPT(String formula)
	{
		ExtendedIsotopePattern p = new ExtendedIsotopePattern();
		//p.addElement("C5");
		p.setUpFromFormula(formula, 0.001, 0.0005, 0.001);
		//p.applyCharge(1, PolarityType.NEGATIVE);
		//p.addElement("Cl");
		
		p.print();
	}
	
	private static String getIntensityRatios(IsotopePattern pattern)
	{
		DataPoint[] dp = pattern.getDataPoints();
		String ratios = "";
		for(int i = 0; i < dp.length; i++)
			ratios += round((dp[i].getIntensity()/dp[0].getIntensity()),2) + ":";
		ratios = (ratios.length() > 0) ? ratios.substring(0, ratios.length()-1) : ratios;
		return 	ratios;
	}
	public static double round(double value, int places) { // https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
}
