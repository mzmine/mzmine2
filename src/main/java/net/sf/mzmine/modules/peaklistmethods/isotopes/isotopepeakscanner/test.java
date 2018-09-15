package net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner;

public class test {

  public static void main(String[] args) {
    String pattern = "";
    
    ExtendedIsotopePattern p = new ExtendedIsotopePattern();
    
    p.setUpFromFormula(pattern, 0.01, 0.001, 0.05);
    
    p.print();
    
    System.out.println("---------------------");
    ExtendedIsotopePattern p2 = new ExtendedIsotopePattern();
    
    p2.setUpFromFormula("C31Cl", 0.01, 0.001, 0.05);
    
    p2.print();
  }
}
