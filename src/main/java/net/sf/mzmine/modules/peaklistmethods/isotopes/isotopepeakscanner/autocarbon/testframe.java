package net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner.autocarbon;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.JSpinner;
import javax.swing.JCheckBox;

public class testframe extends JFrame {

  private JPanel contentPane;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          testframe frame = new testframe();
          frame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the frame.
   */
  public testframe() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 450, 300);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    GridBagLayout gbl_contentPane = new GridBagLayout();
    gbl_contentPane.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
    gbl_contentPane.rowHeights = new int[]{0, 0, 0};
    gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
    gbl_contentPane.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
    contentPane.setLayout(gbl_contentPane);
    
    JPanel panel = new JPanel();
    GridBagConstraints gbc_panel = new GridBagConstraints();
    gbc_panel.fill = GridBagConstraints.BOTH;
    gbc_panel.gridx = 5;
    gbc_panel.gridy = 1;
    contentPane.add(panel, gbc_panel);
    panel.setLayout(new BorderLayout(0, 0));
    
    JPanel panel_1 = new JPanel();
    panel.add(panel_1, BorderLayout.SOUTH);
    
    JSpinner spinner = new JSpinner();
    panel_1.add(spinner);
    
    JPanel panel_2 = new JPanel();
    panel.add(panel_2, BorderLayout.NORTH);
    
    JCheckBox checkBox = new JCheckBox("New check box");
    panel_2.add(checkBox);
  }

}
