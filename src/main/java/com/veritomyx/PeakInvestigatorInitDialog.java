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
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package com.veritomyx;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

import net.sf.mzmine.main.MZmineCore;
//import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.util.ExitCode;
import net.sf.mzmine.util.GUIUtils;
import net.sf.mzmine.util.components.GridBagPanel;
import net.sf.mzmine.util.components.HelpButton;

import java.util.Map;

/**
 * This class represents the user selected SLA and PI Version dialog. 
 * This dialog presents 2 lists, one for SLA and one for PI.
 * The first SLA and the highest version of PI are automatically selected to start. 
 * 
 */
public class PeakInvestigatorInitDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;

    private ExitCode exitCode = ExitCode.UNKNOWN;

    private String helpID;

    // Buttons
    private JButton btnOK, btnCancel, btnHelp;

    /**
     * This single panel contains a grid of all the components of this dialog
     * (see GridBagPanel). First three columns of the grid are title (JLabel),
     * INIT component (JFormattedTextField or other) and value (JLabel). 
     */
    protected GridBagPanel mainPanel, fundsPanel;
    protected JPanel fundsGroupbox;
    
    protected JComboBox<String>	   	responseTimeObjectiveComboBox;
    protected JComboBox<String> 	versionsComboBox;
    protected JLabel                    estimatedCostLabel;
    
    protected Double fundsAvailable;
    
    protected Map<String, Double> responseTimeObjectives;

    /**
     * Constructor
     */
    public PeakInvestigatorInitDialog(Window parent, Double funds, Map<String, Double> responseTimeObjectives, String[] PIversions) {

	// Make dialog modal
	super(parent, "Please set the parameters",
		Dialog.ModalityType.DOCUMENT_MODAL);

	this.responseTimeObjectives = responseTimeObjectives;

	addDialogComponents(fundsAvailable = funds, responseTimeObjectives, PIversions);

	updateMinimumSize();
	pack();

	setLocationRelativeTo(parent);

    }

    /**
     * This method must be called each time when a component is added to
     * mainPanel. It will ensure the minimal size of the dialog is set to the
     * minimum size of the mainPanel plus a little extra, so user cannot resize
     * the dialog window smaller.
     */
    protected void updateMinimumSize() {
	Dimension panelSize = mainPanel.getMinimumSize();
	Dimension minimumSize = new Dimension(panelSize.width + 50,
		panelSize.height + 50);
	setMinimumSize(minimumSize);
    }

    /**
     * Constructs all components of the dialog
     */
    protected void addDialogComponents(Double funds, Map<String, Double> SLAs, String[] PIversions) {

	// Main panel which holds all the components in a grid
	mainPanel = new GridBagPanel();
	
	JLabel PIV_label = new JLabel("Use Peak Investigator Version:");
    mainPanel.add(PIV_label, 0, 0);
    
    versionsComboBox = new JComboBox<String>(PIversions);
    versionsComboBox.setEditable(false);
    versionsComboBox.setSelectedIndex(0);
    mainPanel.add(versionsComboBox, 1, 0);

    // Create a new panel for the funds items.
    fundsGroupbox = new JPanel();
    fundsGroupbox.setBorder(BorderFactory.createTitledBorder("Funds"));
    fundsPanel = new GridBagPanel();
	JLabel funds_label = new JLabel("The available funds are (in USD): $");
	fundsPanel.add(funds_label, 0, 1);
    JLabel funds_disp = new JLabel(String.format( "%.2f", funds ));
    fundsPanel.add(funds_disp, 1, 1);
    
    JLabel SLA_label = new JLabel("Use Response Time Objectives:");
    fundsPanel.add(SLA_label, 0, 2);
    
    
        // Create the 2 combo boxes, filled with the available selections.
  
    	responseTimeObjectiveComboBox = new JComboBox<String>(SLAs.keySet().toArray(new String[SLAs.size()]));
        responseTimeObjectiveComboBox.setEditable(false);
        responseTimeObjectiveComboBox.setSelectedIndex(0);
        responseTimeObjectiveComboBox.addActionListener(this);
        fundsPanel.add(responseTimeObjectiveComboBox, 1, 2);

        JLabel costLabel = new JLabel("Estimated cost:");
        fundsPanel.add(costLabel, 0, 3);
        estimatedCostLabel = new JLabel(formatCost());
        fundsPanel.add(estimatedCostLabel, 1, 3);
        
        fundsGroupbox.add(fundsPanel);

	// Add a single empty cell to the 4th row. This cell is expandable
	// (weightY is 1), therefore the other components will be
	// aligned to the top, which is what we want
	// JComponent emptySpace = (JComponent) Box.createVerticalStrut(1);
	// mainPanel.add(emptySpace, 0, 99, 3, 1, 0, 1);

	// Create a separate panel for the buttons
	JPanel pnlButtons = new JPanel();

	btnOK = GUIUtils.addButton(pnlButtons, "OK", null, this);
	btnCancel = GUIUtils.addButton(pnlButtons, "Cancel", null, this);

	if (helpID != null) {
	    btnHelp = new HelpButton(helpID);
	    pnlButtons.add(btnHelp);
	}

	/*
	 * Last row in the table will be occupied by the buttons. We set the row
	 * number to 100 and width to 3, spanning the 3 component columns
	 * defined above.
	 */
	mainPanel.addCenter(fundsGroupbox, 0, 2, 3, 5);
	mainPanel.addCenter(pnlButtons, 0, 100, 3, 1);

	// Add some space around the widgets
	GUIUtils.addMargin(mainPanel, 10);

	// Add the main panel as the only component of this dialog
	add(mainPanel);

	pack();

    }

    /**
     * Implementation for ActionListener interface
     */
    public void actionPerformed(ActionEvent ae) {

	Object src = ae.getSource();

	if (src == btnOK) {
		String currentSelection = responseTimeObjectiveComboBox.getSelectedItem().toString();
		if(responseTimeObjectives.get(currentSelection) >= fundsAvailable) {
			MZmineCore.getDesktop().displayErrorMessage(MZmineCore.getDesktop().getMainWindow(), "Error", "The selected RTO requires more funds than are currently available in your account.");
		} else {
			closeDialog(ExitCode.OK);
		}
	}

	if (src == btnCancel) {
	    closeDialog(ExitCode.CANCEL);
	}

	if (src instanceof JComboBox) {
	    estimatedCostLabel.setText(formatCost());
	}

    }

    /**
     * Method for reading exit code
     */
    public ExitCode getExitCode() {
	return exitCode;
    }

    /**
     * This method may be called by some of the dialog components, for example
     * as a result of double-click by user
     */
    public void closeDialog(ExitCode exitCode) {
	if (exitCode == ExitCode.OK) {
	    // commit the changes to the parameter set
	    
	}
	this.exitCode = exitCode;
	dispose();

    }

    public String getSLA() {
        return responseTimeObjectiveComboBox.getSelectedItem().toString();
    }

    public String getPIversion() {
        return versionsComboBox.getSelectedItem().toString();
    }
    
    // convenience method to use the selected SLA to return a cost
    private String formatCost() {
        String currentSelection = responseTimeObjectiveComboBox.getSelectedItem().toString();
        return String.format("$%.2f", responseTimeObjectives.get(currentSelection));
    }
}
