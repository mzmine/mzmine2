package net.sf.mzmine.modules.peaklistmethods.mymodule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.interfaces.IIsotope;

import com.google.common.base.Predicate;

import dods.util.test_iniFile;

public class Tests {

	public static void main(String[] args) {
		System.out.println("Tests");
		//testIsotope();
		test();
	}
	
	private static void testIsotope() {
		//get isotope information, idk if it works
		Isotopes ifac;
		try {
			ifac = Isotopes.getInstance();
			IIsotope[] el = ifac.getIsotopes("Gd");
			el = (IIsotope[]) Arrays.stream(el).filter(i -> i.getNaturalAbundance()>0.1).toArray(IIsotope[]::new);
			int size = el.length;
			System.out.println(size);
			for(IIsotope i : el)
				System.out.println("mass "+ i.getExactMass() + "   abundance "+i.getNaturalAbundance());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void test()
	{
		ArrayList<Integer> list = new ArrayList<Integer>(3);
		
		for(int i = 0; i < 50; i++)
			list.add(i);
		System.out.println(list.size() + " last entry: " + list.get(list.size()-1));
	}
}
