package net.sf.mzmine.modules.peaklistmethods.identification.glycerophospholipidsearch.GPLipidIdentities;

import javax.annotation.Nonnull;

import net.sf.mzmine.datamodel.impl.SimplePeakIdentity;
import net.sf.mzmine.modules.peaklistmethods.identification.glycerophospholipidsearch.GPLipidType;
import net.sf.mzmine.util.FormulaUtils;

public class GPLipidIdentity3Chains  extends SimplePeakIdentity {

    private final double mass;

    public GPLipidIdentity3Chains(final GPLipidType lipidType,
	    final int fattyAcid1Length, final int fattyAcid1DoubleBonds,
	    final int fattyAcid2Length, final int fattyAcid2DoubleBonds,
	    final int fattyAcid3Length, final int fattyAcid3DoubleBonds) {

	this(lipidType.getAbbr() + '(' + fattyAcid1Length + ':'
		+ fattyAcid1DoubleBonds + '/' + fattyAcid2Length + ':'
		+ fattyAcid2DoubleBonds + '/' + fattyAcid3Length + ':'
		+ fattyAcid3DoubleBonds + ')', lipidType.getFormula()
		+ calculateFattyAcidFormula(fattyAcid1Length,
			fattyAcid1DoubleBonds)
		+ calculateFattyAcidFormula(fattyAcid2Length,
			fattyAcid2DoubleBonds)
		+ calculateFattyAcidFormula(fattyAcid3Length,
				fattyAcid3DoubleBonds));
    }

    private GPLipidIdentity3Chains(final String name, final String formula) {

	super(name);
	mass = FormulaUtils.calculateExactMass(formula);
	setPropertyValue(PROPERTY_FORMULA, formula);
	setPropertyValue(PROPERTY_METHOD, "Glycerophospholipid search");
    }

    /**
     * Calculate fatty acid formula.
     *
     * @param fattyAcidLength
     *            acid length.
     * @param fattyAcidDoubleBonds
     *            double bond count.
     * @return fatty acid formula.
     */
    private static String calculateFattyAcidFormula(final int fattyAcidLength,
	    final int fattyAcidDoubleBonds) {

	String fattyAcid1Formula = "H";
	if (fattyAcidLength > 0) {

	    final int numberOfHydrogens = fattyAcidLength * 2
		    - fattyAcidDoubleBonds * 2 - 1;
	    fattyAcid1Formula = "C" + fattyAcidLength + 'H' + numberOfHydrogens
		    + 'O';
	}
	return fattyAcid1Formula;
    }

    /**
     * Get the mass.
     *
     * @return the mass.
     */
    public double getMass() {

	return mass;
    }

    @Override
    public @Nonnull Object clone() {

	return new GPLipidIdentity3Chains(getName(),
		getPropertyValue(PROPERTY_FORMULA));
    }
}
