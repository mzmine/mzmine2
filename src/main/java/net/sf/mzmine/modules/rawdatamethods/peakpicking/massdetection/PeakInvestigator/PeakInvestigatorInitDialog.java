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

package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

import com.veritomyx.actions.InitAction.ResponseTimeCosts;

import net.sf.mzmine.main.MZmineCore;
//import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.util.ExitCode;
import net.sf.mzmine.util.GUIUtils;
import net.sf.mzmine.util.components.GridBagPanel;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * This dialog is presented at the start of a PeakInvestigator run. It shows the
 * user the current selected version of PeakInvestigator, and the associated
 * estimated costs for various MS types and Response Time Objectives (RTOs). It
 * also allows the user to select the desired RTO.
 */
public class PeakInvestigatorInitDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;

    private ExitCode exitCode = ExitCode.UNKNOWN;

    // GUI elements needed later
    protected GridBagPanel mainPanel;
	protected JComboBox<String> responseTimeObjectiveComboBox;

    // Buttons
    private JButton btnOK;
    private JButton btnCancel;
    private JButton btnDetails;

    // Passed-in values needed later
    protected Double availableFunds;
    Map<String, ResponseTimeCosts> estimatedCosts = null;

    // Required in more than one place, so lazily constructed
    protected String[] responseTimeObjectives = null;

	public PeakInvestigatorInitDialog(Window parent, String version,
			double funds, Map<String, ResponseTimeCosts> estimatedCosts) {

		super(parent, Dialog.ModalityType.DOCUMENT_MODAL);
		setTitle("PeakInvestigator " + version);

		this.estimatedCosts = estimatedCosts;
		this.availableFunds = funds;

		addDialogComponents(version, availableFunds, estimatedCosts);
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

	protected void addDialogComponents(String version, double funds,
			Map<String, ResponseTimeCosts> estimatedCosts) {

		// Main panel which holds all the components in a grid
		mainPanel = new GridBagPanel();

		JPanel quotationArea = buildPriceQuotationArea(estimatedCosts);
		mainPanel.addCenter(quotationArea, 0, 1, 3, estimatedCosts.size() + 2);

		JPanel fundsArea = buildFundsArea(funds);
		mainPanel.addCenter(fundsArea, 0, 50, 3, 1);

		JPanel comboBoxArea = buildComboBoxArea(getResponseTimeObjectives(estimatedCosts));
		mainPanel.addCenter(comboBoxArea, 0, 51, 3, 1);

		// Create a separate panel for the buttons
		JPanel pnlButtons = new JPanel();

		btnOK = GUIUtils.addButton(pnlButtons, "OK", null, this);
		btnCancel = GUIUtils.addButton(pnlButtons, "Cancel", null, this);
		btnDetails = GUIUtils.addButton(pnlButtons,
				"Price quotation details...", null, this);

		/*
		 * Last row in the table will be occupied by the buttons. We set the row
		 * number to 100 and width to 3, spanning the 3 component columns
		 * defined above.
		 */
		mainPanel.addCenter(pnlButtons, 0, 100, 3, 1);

		// Add some space around the widgets
		GUIUtils.addMargin(mainPanel, 10);

		// Add the main panel as the only component of this dialog
		add(mainPanel);

		pack();
	}

//	private JPanel buildPiVersionArea(String version) {
//		JPanel panel = new JPanel();
//
//		GridBagPanel grid = new GridBagPanel();
//		grid.add(new JLabel("PeakInvestigator version:"), 0, 1);
//		grid.add(new JLabel(version), 1, 1);
//
//		panel.add(grid);
//		return panel;
//	}

   private JPanel buildPriceQuotationArea(Map<String, ResponseTimeCosts> estimatedCosts) {
    	JPanel panel = new JPanel();
    	String[] RTOs = getResponseTimeObjectives(estimatedCosts);
        String[] machineTypes = getMachineTypes(estimatedCosts);
        
        if (RTOs.length == 0 || machineTypes.length == 0) {
        	return panel;
        }
    	
        panel.setBorder(BorderFactory.createTitledBorder("Price quotation"));
        
        final int columnStart = 1;
        GridBagPanel grid = new GridBagPanel();
        grid.add(new JLabel("Response Time Objective:"), 0, 0);
        for (int i = 0; i < RTOs.length; i++) {
        	grid.add(new JLabel(RTOs[i]), columnStart + i, 0);
        }
        
        grid.add(new JLabel("MS Type"), 0, 1);
        grid.add(Box.createVerticalStrut(1), 0, 2, 3, 0, 0, 1);
        final int rowStart = 3;
        for (int i = 0; i < machineTypes.length; i++) {
        	grid.add(new JLabel(machineTypes[i]), 0, rowStart + i);
        	ResponseTimeCosts costs = estimatedCosts.get(machineTypes[i]);
        	for (int j = 0; j < costs.size(); j++) {
        		String text = String.format("$%.2f", costs.getCost(RTOs[j]));
        		grid.add(new JLabel(text), columnStart + j, rowStart + i);
        	}
        }
        
        panel.add(grid);
        return panel;
    }
    
	private JPanel buildFundsArea(Double funds) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Customer account"));

		GridBagPanel grid = new GridBagPanel();
		grid.add(new JLabel("Current balance:"), 0, 1);
		grid.add(new JLabel(String.format("$%.2f", funds)), 1, 1);

		panel.add(grid);
		return panel;
	}
	
	private JPanel buildComboBoxArea(String[] RTOs) {
		JPanel panel = new JPanel();

		GridBagPanel grid = new GridBagPanel();
		grid.add(new JLabel("Desired RTO:"), 0, 0);
		responseTimeObjectiveComboBox = new JComboBox<String>(RTOs);
		grid.add(responseTimeObjectiveComboBox, 1, 0);

		panel.add(grid);
		return panel;
	}

	public void actionPerformed(ActionEvent ae) {
		Object src = ae.getSource();

		if (src == btnOK) {
			handleOkButton();
		} else if (src == btnCancel) {
			handleCancelButton();
		} else if (src == btnDetails) {
			handleDetailsButton();
		}
	}

	private void handleOkButton() {
		String selectedRTO = responseTimeObjectiveComboBox.getSelectedItem()
				.toString();
		if (determineMaxPotentialCost(estimatedCosts, selectedRTO) > availableFunds) {
			String mesg = "The selected RTO requires more funds than are currently available in your account.";
			MZmineCore.getDesktop().displayErrorMessage(
					MZmineCore.getDesktop().getMainWindow(), "Error", mesg);
		} else {
			closeDialog(ExitCode.OK);
		}

	}

	private void handleCancelButton() {
		closeDialog(ExitCode.CANCEL);
	}

	private void handleDetailsButton() {

	}

	public void closeDialog(ExitCode exitCode) {
		if (exitCode == ExitCode.OK) {
			// commit the changes to the parameter set

		}
		this.exitCode = exitCode;
		dispose();

	}

	public ExitCode getExitCode() {
		return exitCode;
	}

    public String getSelectedRTO() {
        return responseTimeObjectiveComboBox.getSelectedItem().toString();
    }

    private String[] getMachineTypes(Map<String, ResponseTimeCosts> estimatedCosts) {
    	Set<String> keys = estimatedCosts.keySet();
    	return keys.toArray(new String[keys.size()]);
    }
    
	/**
	 * Convenience function to get the Response Time Objectives. Assumes that
	 * each ResponseTimeCosts value have exactly the same number and name of
	 * RTO. This function lazily instantiates the reponseTimeObjective variable.
	 * 
	 * @param estimatedCosts
	 *            As returned from InitAction.getEstimatedCosts()
	 * @return List of strings corresponding to RTOs (e.g. "RTO-24", "RTO-0",
	 *         etc.)
	 */
	private String[] getResponseTimeObjectives(Map<String, ResponseTimeCosts> estimatedCosts) {
		if (responseTimeObjectives != null) {
			return responseTimeObjectives;
		}

		ArrayList<ResponseTimeCosts> costs = new ArrayList<>(
				estimatedCosts.values());
		if (costs.size() == 0) {
			responseTimeObjectives = new String[] {};
			return new String[] {};
		} else {
			Set<String> RTOs = costs.get(0).keySet();
			responseTimeObjectives = RTOs.toArray(new String[RTOs.size()]);
		}

		return responseTimeObjectives;
	}
	
	protected double determineMaxPotentialCost(Map<String, ResponseTimeCosts> estimatedCosts, String RTO) {
		double maxCost = 0;
		for (ResponseTimeCosts costs : estimatedCosts.values()) {
			double cost = costs.getCost(RTO);
			if (maxCost > cost) {
				maxCost = cost;
			}
		}

		return maxCost;
	}

}
