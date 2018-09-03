package net.sf.mzmine.modules.peaklistmethods.isotopes.isotopepeakscanner;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentListener;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.dialogs.ParameterSetupDialog;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JButton;

public class IsotopePeakScannerSetupDialog2 extends ParameterSetupDialog implements ActionListener, DocumentListener {

  private JPanel contentPane;
  private JTextField textField;
  private JTextField textField_1;
  private JTextField textField_2;
  private JTextField textField_3;
  private JTextField textField_4;
  private JTextField textField_5;
  private JTextField textField_6;
  private JTextField textField_7;
  private JTextField textField_8;
  private JTextField textField_9;
  private JTextField textField_10;

  /**
   * Create the frame.
   */
  
  public IsotopePeakScannerSetupDialog2(Window parent, boolean valueCheckRequired, ParameterSet parameters) {

    super(parent, valueCheckRequired, parameters);
    
  }
  
  
  public IsotopePeakScannerSetupDialog2() {
    /*setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 550, 747);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    GridBagLayout gbl_contentPane = new GridBagLayout();
    gbl_contentPane.columnWidths = new int[]{0, 0, 50, 0, 0};
    gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    gbl_contentPane.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
    gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
    contentPane.setLayout(gbl_contentPane);
    
    JLabel lblPeakList = new JLabel("Peak list");
    GridBagConstraints gbc_lblPeakList = new GridBagConstraints();
    gbc_lblPeakList.anchor = GridBagConstraints.EAST;
    gbc_lblPeakList.insets = new Insets(0, 0, 5, 5);
    gbc_lblPeakList.gridx = 0;
    gbc_lblPeakList.gridy = 0;
    contentPane.add(lblPeakList, gbc_lblPeakList);
    
    JComboBox comboBox_3 = new JComboBox();
    GridBagConstraints gbc_comboBox_3 = new GridBagConstraints();
    gbc_comboBox_3.insets = new Insets(0, 0, 5, 5);
    gbc_comboBox_3.fill = GridBagConstraints.HORIZONTAL;
    gbc_comboBox_3.gridx = 1;
    gbc_comboBox_3.gridy = 0;
    contentPane.add(comboBox_3, gbc_comboBox_3);
    
    JLabel lblNewLabel = new JLabel("m/z tolerance");
    GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
    gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
    gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
    gbc_lblNewLabel.gridx = 0;
    gbc_lblNewLabel.gridy = 1;
    contentPane.add(lblNewLabel, gbc_lblNewLabel);
    
    textField_9 = new JTextField();
    GridBagConstraints gbc_textField_9 = new GridBagConstraints();
    gbc_textField_9.insets = new Insets(0, 0, 5, 5);
    gbc_textField_9.fill = GridBagConstraints.HORIZONTAL;
    gbc_textField_9.gridx = 1;
    gbc_textField_9.gridy = 1;
    contentPane.add(textField_9, gbc_textField_9);
    textField_9.setColumns(10);
    
    textField_10 = new JTextField();
    GridBagConstraints gbc_textField_10 = new GridBagConstraints();
    gbc_textField_10.insets = new Insets(0, 0, 5, 5);
    gbc_textField_10.fill = GridBagConstraints.HORIZONTAL;
    gbc_textField_10.gridx = 2;
    gbc_textField_10.gridy = 1;
    contentPane.add(textField_10, gbc_textField_10);
    textField_10.setColumns(10);
    
    JLabel lblCheckRt = new JLabel("Check RT");
    GridBagConstraints gbc_lblCheckRt = new GridBagConstraints();
    gbc_lblCheckRt.insets = new Insets(0, 0, 5, 5);
    gbc_lblCheckRt.gridx = 0;
    gbc_lblCheckRt.gridy = 2;
    contentPane.add(lblCheckRt, gbc_lblCheckRt);
    
    JCheckBox chckbxTest = new JCheckBox("");
    GridBagConstraints gbc_chckbxTest = new GridBagConstraints();
    gbc_chckbxTest.insets = new Insets(0, 0, 5, 5);
    gbc_chckbxTest.gridx = 1;
    gbc_chckbxTest.gridy = 2;
    contentPane.add(chckbxTest, gbc_chckbxTest);
    
    textField = new JTextField();
    GridBagConstraints gbc_textField = new GridBagConstraints();
    gbc_textField.anchor = GridBagConstraints.WEST;
    gbc_textField.insets = new Insets(0, 0, 5, 5);
    gbc_textField.gridx = 2;
    gbc_textField.gridy = 2;
    contentPane.add(textField, gbc_textField);
    textField.setColumns(10);
    
    JComboBox comboBox = new JComboBox();
    GridBagConstraints gbc_comboBox = new GridBagConstraints();
    gbc_comboBox.insets = new Insets(0, 0, 5, 0);
    gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
    gbc_comboBox.gridx = 3;
    gbc_comboBox.gridy = 2;
    contentPane.add(comboBox, gbc_comboBox);
    
    JLabel lblElementPattern = new JLabel("Element pattern");
    GridBagConstraints gbc_lblElementPattern = new GridBagConstraints();
    gbc_lblElementPattern.anchor = GridBagConstraints.EAST;
    gbc_lblElementPattern.insets = new Insets(0, 0, 5, 5);
    gbc_lblElementPattern.gridx = 0;
    gbc_lblElementPattern.gridy = 3;
    contentPane.add(lblElementPattern, gbc_lblElementPattern);
    
    textField_1 = new JTextField();
    GridBagConstraints gbc_textField_1 = new GridBagConstraints();
    gbc_textField_1.insets = new Insets(0, 0, 5, 5);
    gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
    gbc_textField_1.gridx = 1;
    gbc_textField_1.gridy = 3;
    contentPane.add(textField_1, gbc_textField_1);
    textField_1.setColumns(10);
    
    JLabel lblAutoCarbon = new JLabel("Auto carbon");
    GridBagConstraints gbc_lblAutoCarbon = new GridBagConstraints();
    gbc_lblAutoCarbon.insets = new Insets(0, 0, 5, 5);
    gbc_lblAutoCarbon.gridx = 0;
    gbc_lblAutoCarbon.gridy = 4;
    contentPane.add(lblAutoCarbon, gbc_lblAutoCarbon);
    
    JButton btnSetUp = new JButton("Set up");
    GridBagConstraints gbc_btnSetUp = new GridBagConstraints();
    gbc_btnSetUp.insets = new Insets(0, 0, 5, 5);
    gbc_btnSetUp.gridx = 1;
    gbc_btnSetUp.gridy = 4;
    contentPane.add(btnSetUp, gbc_btnSetUp);
    
    JCheckBox checkBox_2 = new JCheckBox("");
    GridBagConstraints gbc_checkBox_2 = new GridBagConstraints();
    gbc_checkBox_2.insets = new Insets(0, 0, 5, 5);
    gbc_checkBox_2.gridx = 2;
    gbc_checkBox_2.gridy = 4;
    contentPane.add(checkBox_2, gbc_checkBox_2);
    
    JLabel lblCharge = new JLabel("Charge");
    GridBagConstraints gbc_lblCharge = new GridBagConstraints();
    gbc_lblCharge.anchor = GridBagConstraints.EAST;
    gbc_lblCharge.insets = new Insets(0, 0, 5, 5);
    gbc_lblCharge.gridx = 0;
    gbc_lblCharge.gridy = 5;
    contentPane.add(lblCharge, gbc_lblCharge);
    
    textField_2 = new JTextField();
    GridBagConstraints gbc_textField_2 = new GridBagConstraints();
    gbc_textField_2.insets = new Insets(0, 0, 5, 5);
    gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
    gbc_textField_2.gridx = 1;
    gbc_textField_2.gridy = 5;
    contentPane.add(textField_2, gbc_textField_2);
    textField_2.setColumns(10);
    
    JLabel lblMinimumAbundance = new JLabel("Minimum abundance");
    GridBagConstraints gbc_lblMinimumAbundance = new GridBagConstraints();
    gbc_lblMinimumAbundance.anchor = GridBagConstraints.EAST;
    gbc_lblMinimumAbundance.insets = new Insets(0, 0, 5, 5);
    gbc_lblMinimumAbundance.gridx = 0;
    gbc_lblMinimumAbundance.gridy = 6;
    contentPane.add(lblMinimumAbundance, gbc_lblMinimumAbundance);
    
    textField_3 = new JTextField();
    GridBagConstraints gbc_textField_3 = new GridBagConstraints();
    gbc_textField_3.insets = new Insets(0, 0, 5, 5);
    gbc_textField_3.fill = GridBagConstraints.HORIZONTAL;
    gbc_textField_3.gridx = 1;
    gbc_textField_3.gridy = 6;
    contentPane.add(textField_3, gbc_textField_3);
    textField_3.setColumns(10);
    
    JLabel lblMinPatternIntensity = new JLabel("Min. pattern intensity");
    GridBagConstraints gbc_lblMinPatternIntensity = new GridBagConstraints();
    gbc_lblMinPatternIntensity.anchor = GridBagConstraints.EAST;
    gbc_lblMinPatternIntensity.insets = new Insets(0, 0, 5, 5);
    gbc_lblMinPatternIntensity.gridx = 0;
    gbc_lblMinPatternIntensity.gridy = 7;
    contentPane.add(lblMinPatternIntensity, gbc_lblMinPatternIntensity);
    
    textField_4 = new JTextField();
    GridBagConstraints gbc_textField_4 = new GridBagConstraints();
    gbc_textField_4.insets = new Insets(0, 0, 5, 5);
    gbc_textField_4.fill = GridBagConstraints.HORIZONTAL;
    gbc_textField_4.gridx = 1;
    gbc_textField_4.gridy = 7;
    contentPane.add(textField_4, gbc_textField_4);
    textField_4.setColumns(10);
    
    JLabel lblMergeWidthmz = new JLabel("Merge width (m/z)");
    GridBagConstraints gbc_lblMergeWidthmz = new GridBagConstraints();
    gbc_lblMergeWidthmz.anchor = GridBagConstraints.EAST;
    gbc_lblMergeWidthmz.insets = new Insets(0, 0, 5, 5);
    gbc_lblMergeWidthmz.gridx = 0;
    gbc_lblMergeWidthmz.gridy = 8;
    contentPane.add(lblMergeWidthmz, gbc_lblMergeWidthmz);
    
    textField_5 = new JTextField();
    GridBagConstraints gbc_textField_5 = new GridBagConstraints();
    gbc_textField_5.insets = new Insets(0, 0, 5, 5);
    gbc_textField_5.fill = GridBagConstraints.HORIZONTAL;
    gbc_textField_5.gridx = 1;
    gbc_textField_5.gridy = 8;
    contentPane.add(textField_5, gbc_textField_5);
    textField_5.setColumns(10);
    
    JLabel lblMinimumHeight = new JLabel("Minimum height");
    GridBagConstraints gbc_lblMinimumHeight = new GridBagConstraints();
    gbc_lblMinimumHeight.anchor = GridBagConstraints.EAST;
    gbc_lblMinimumHeight.insets = new Insets(0, 0, 5, 5);
    gbc_lblMinimumHeight.gridx = 0;
    gbc_lblMinimumHeight.gridy = 9;
    contentPane.add(lblMinimumHeight, gbc_lblMinimumHeight);
    
    textField_6 = new JTextField();
    GridBagConstraints gbc_textField_6 = new GridBagConstraints();
    gbc_textField_6.insets = new Insets(0, 0, 5, 5);
    gbc_textField_6.fill = GridBagConstraints.HORIZONTAL;
    gbc_textField_6.gridx = 1;
    gbc_textField_6.gridy = 9;
    contentPane.add(textField_6, gbc_textField_6);
    textField_6.setColumns(10);
    
    JLabel lblCheckIntensity = new JLabel("Check intensity");
    GridBagConstraints gbc_lblCheckIntensity = new GridBagConstraints();
    gbc_lblCheckIntensity.insets = new Insets(0, 0, 5, 5);
    gbc_lblCheckIntensity.gridx = 0;
    gbc_lblCheckIntensity.gridy = 10;
    contentPane.add(lblCheckIntensity, gbc_lblCheckIntensity);
    
    JCheckBox checkBox = new JCheckBox("");
    GridBagConstraints gbc_checkBox = new GridBagConstraints();
    gbc_checkBox.insets = new Insets(0, 0, 5, 5);
    gbc_checkBox.gridx = 1;
    gbc_checkBox.gridy = 10;
    contentPane.add(checkBox, gbc_checkBox);
    
    JLabel lblMinimumRating = new JLabel("Minimum rating");
    GridBagConstraints gbc_lblMinimumRating = new GridBagConstraints();
    gbc_lblMinimumRating.anchor = GridBagConstraints.EAST;
    gbc_lblMinimumRating.insets = new Insets(0, 0, 5, 5);
    gbc_lblMinimumRating.gridx = 0;
    gbc_lblMinimumRating.gridy = 11;
    contentPane.add(lblMinimumRating, gbc_lblMinimumRating);
    
    textField_7 = new JTextField();
    GridBagConstraints gbc_textField_7 = new GridBagConstraints();
    gbc_textField_7.insets = new Insets(0, 0, 5, 5);
    gbc_textField_7.fill = GridBagConstraints.HORIZONTAL;
    gbc_textField_7.gridx = 1;
    gbc_textField_7.gridy = 11;
    contentPane.add(textField_7, gbc_textField_7);
    textField_7.setColumns(10);
    
    JLabel lblRatingType = new JLabel("Rating type");
    GridBagConstraints gbc_lblRatingType = new GridBagConstraints();
    gbc_lblRatingType.anchor = GridBagConstraints.EAST;
    gbc_lblRatingType.insets = new Insets(0, 0, 5, 5);
    gbc_lblRatingType.gridx = 0;
    gbc_lblRatingType.gridy = 12;
    contentPane.add(lblRatingType, gbc_lblRatingType);
    
    JComboBox comboBox_1 = new JComboBox();
    GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
    gbc_comboBox_1.insets = new Insets(0, 0, 5, 5);
    gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
    gbc_comboBox_1.gridx = 1;
    gbc_comboBox_1.gridy = 12;
    contentPane.add(comboBox_1, gbc_comboBox_1);
    
    JLabel lblCalculateAccurateAverage = new JLabel("Calculate accurate average");
    GridBagConstraints gbc_lblCalculateAccurateAverage = new GridBagConstraints();
    gbc_lblCalculateAccurateAverage.insets = new Insets(0, 0, 5, 5);
    gbc_lblCalculateAccurateAverage.gridx = 0;
    gbc_lblCalculateAccurateAverage.gridy = 13;
    contentPane.add(lblCalculateAccurateAverage, gbc_lblCalculateAccurateAverage);
    
    JCheckBox checkBox_1 = new JCheckBox("");
    GridBagConstraints gbc_checkBox_1 = new GridBagConstraints();
    gbc_checkBox_1.insets = new Insets(0, 0, 5, 5);
    gbc_checkBox_1.gridx = 1;
    gbc_checkBox_1.gridy = 13;
    contentPane.add(checkBox_1, gbc_checkBox_1);
    
    JComboBox comboBox_2 = new JComboBox();
    GridBagConstraints gbc_comboBox_2 = new GridBagConstraints();
    gbc_comboBox_2.insets = new Insets(0, 0, 5, 5);
    gbc_comboBox_2.fill = GridBagConstraints.HORIZONTAL;
    gbc_comboBox_2.gridx = 2;
    gbc_comboBox_2.gridy = 13;
    contentPane.add(comboBox_2, gbc_comboBox_2);
    
    JLabel lblSuffix = new JLabel("Suffix");
    GridBagConstraints gbc_lblSuffix = new GridBagConstraints();
    gbc_lblSuffix.anchor = GridBagConstraints.EAST;
    gbc_lblSuffix.insets = new Insets(0, 0, 5, 5);
    gbc_lblSuffix.gridx = 0;
    gbc_lblSuffix.gridy = 14;
    contentPane.add(lblSuffix, gbc_lblSuffix);
    
    textField_8 = new JTextField();
    GridBagConstraints gbc_textField_8 = new GridBagConstraints();
    gbc_textField_8.insets = new Insets(0, 0, 5, 5);
    gbc_textField_8.fill = GridBagConstraints.HORIZONTAL;
    gbc_textField_8.gridx = 1;
    gbc_textField_8.gridy = 14;
    contentPane.add(textField_8, gbc_textField_8);
    textField_8.setColumns(10);
    
    JButton btnOk = new JButton("OK");
    GridBagConstraints gbc_btnOk = new GridBagConstraints();
    gbc_btnOk.insets = new Insets(0, 0, 0, 5);
    gbc_btnOk.gridx = 0;
    gbc_btnOk.gridy = 16;
    contentPane.add(btnOk, gbc_btnOk);
    
    JButton btnCancel = new JButton("Cancel");
    GridBagConstraints gbc_btnCancel = new GridBagConstraints();
    gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
    gbc_btnCancel.gridx = 1;
    gbc_btnCancel.gridy = 16;
    contentPane.add(btnCancel, gbc_btnCancel);
    
    JButton btnHelp = new JButton("Help");
    GridBagConstraints gbc_btnHelp = new GridBagConstraints();
    gbc_btnHelp.insets = new Insets(0, 0, 0, 5);
    gbc_btnHelp.gridx = 2;
    gbc_btnHelp.gridy = 16;
    contentPane.add(btnHelp, gbc_btnHelp);*/
  }

}
