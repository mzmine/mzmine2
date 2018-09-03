package net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.DocumentListener;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.dialogs.ParameterSetupDialog;
import net.sf.mzmine.util.ExitCode;
import net.sf.mzmine.util.GUIUtils;
import net.sf.mzmine.util.components.GridBagPanel;

public class IsotopePeakScannerSetupDialog extends ParameterSetupDialog implements ActionListener, DocumentListener {
  
  private Logger logger = Logger.getLogger(this.getClass().getName());
  private JButton autoCSetup;
  
  public IsotopePeakScannerSetupDialog(Window parent, boolean valueCheckRequired,
      ParameterSet parameters) {
    super(parent, valueCheckRequired, parameters);
  }
  
  @Override
  protected void addDialogComponents() {
    logger.info("should add");
    super.addDialogComponents();
    
    JPanel pnlAutoCButton = new JPanel();
    pnlAutoCButton.setLayout(new BoxLayout(pnlAutoCButton, BoxLayout.X_AXIS));
    
    autoCSetup = new JButton();
    autoCSetup.addActionListener(this);
    autoCSetup.setText("Auto carbon");
    autoCSetup.setToolTipText("Set up automatic carbon detection.\nThis will calculate isotope patterns with a given range of carbon atoms\n"
        + "and add the best fitting one as an isotope pattern. Set up minimum and maximum carbon atoms, minimum number of data points in\n"
        + "the isotope pattern and view predicted peaks.");
    autoCSetup.setMinimumSize(autoCSetup.getPreferredSize());
    GUIUtils.addButton(pnlAutoCButton, "Auto carbon", null, this);
    
    mainPanel.add(pnlAutoCButton);
    
    logger.info("should have added");
  }
  
  /**
   * Implementation for ActionListener interface
   */
  @Override
  public void actionPerformed(ActionEvent ae) {
    super.actionPerformed(ae);
    Object src = ae.getSource();
    if(src == autoCSetup)
    {
      logger.info("auto c pressed");
    }
  }
    
}