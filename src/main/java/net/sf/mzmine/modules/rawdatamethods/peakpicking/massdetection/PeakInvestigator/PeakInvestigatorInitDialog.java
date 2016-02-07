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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

import org.apache.batik.ext.swing.GridBagConstants;
import org.json.simple.parser.ParseException;

import com.veritomyx.actions.InitAction;
import com.veritomyx.actions.InitAction.ResponseTimeCosts;

import net.sf.mzmine.main.MZmineCore;
//import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.util.ExitCode;
import net.sf.mzmine.util.GUIUtils;
import net.sf.mzmine.util.components.GridBagPanel;

import java.util.Map;

/**
 * This dialog is presented at the start of a PeakInvestigator run so the user
 * can selected the desired Response Time Objective (RTO). It shows the the
 * associated estimated costs for various MS types and RTOs.
 */
public class PeakInvestigatorInitDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final Dimension SMALL_SIZE_XY = new Dimension(5, 5);
    private static final Dimension SMALL_SIZE_X = new Dimension(5, 0);

    private ExitCode exitCode = ExitCode.UNKNOWN;

    // GUI elements needed later
    protected GridBagPanel mainPanel;
	protected JComboBox<String> responseTimeObjectiveComboBox;

    // Buttons
    private JButton btnOK;
    private JButton btnCancel;
    private JButton btnDetails;

    // Passed-in values needed later
	private InitAction action;

	public PeakInvestigatorInitDialog(Window parent, String version, InitAction action) {
		super(parent, Dialog.ModalityType.DOCUMENT_MODAL);
		setTitle("PeakInvestigator " + version);

		this.action = action;

		addDialogComponents(version, action.getFunds(), action.getEstimatedCosts());
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
		mainPanel.add(quotationArea, 0, 0, 1, 1, 1, 1, GridBagConstants.BOTH);

		JPanel fundsArea = buildFundsArea(funds);
		mainPanel.add(fundsArea, 0, 1, 1, 1, 1, 1, GridBagConstants.BOTH);

		JPanel comboBoxArea = buildComboBoxArea(action.getResponseTimeObjectives());
		mainPanel.add(comboBoxArea, 0, 2, 1, 1, 1, 1, GridBagConstants.BOTH);

		// Create a separate panel for the buttons
		JPanel pnlButtons = new JPanel();

		btnDetails = GUIUtils.addButton(pnlButtons,
				"Price Quotation Details...", null, this);
		btnCancel = GUIUtils.addButton(pnlButtons, "Cancel", null, this);
		btnOK = GUIUtils.addButton(pnlButtons, "Purchase", null, this);

		/*
		 * Last row in the table will be occupied by the buttons. We set the row
		 * number to 100 and width to 3, spanning the 3 component columns
		 * defined above.
		 */
		mainPanel.addCenter(pnlButtons, 0, 100, 1, 1);

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
		String[] RTOs = action.getResponseTimeObjectives();
		String[] machineTypes = action.getMSTypes();
        
        if (RTOs.length == 0 || machineTypes.length == 0) {
        	return panel;
        }
    	
        panel.setBorder(BorderFactory.createTitledBorder("Levels of Service"));
        
        final int columnStart = 1;
        GridBagPanel grid = new GridBagPanel();
        grid.add(new JLabel("Response Time Objective (<= x hrs):"), 0, 0);
        for (int i = 0; i < RTOs.length; i++) {
			grid.add(Box.createRigidArea(SMALL_SIZE_X), columnStart + 2 * i, 0);
			grid.add(new JLabel(RTOs[i]), columnStart + 2 * i + 1, 0);
        }

        grid.add(Box.createRigidArea(SMALL_SIZE_XY), 0, 1);
        grid.add(new JLabel("Price Quotation:"), 0, 2);
        final int rowStart = 3;
        for (int i = 0; i < machineTypes.length; i++) {
        	grid.add(new JLabel(machineTypes[i]), 0, rowStart + i);
        	ResponseTimeCosts costs = estimatedCosts.get(machineTypes[i]);
        	for (int j = 0; j < costs.size(); j++) {
				String text = String.format("$%.2f", costs.getCost(RTOs[j]));
				grid.add(Box.createRigidArea(SMALL_SIZE_X),
						columnStart + 2 * j, rowStart + 1);
				grid.add(new JLabel(text), columnStart + 2 * j + 1, rowStart
						+ i);
        	}
        }
        
        panel.add(grid);
        return panel;
    }
    
	private JPanel buildFundsArea(Double funds) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Customer Account"));

		GridBagPanel grid = new GridBagPanel();
		grid.add(new JLabel(String.format("Available Balance: $%.2f", funds)), 0, 0);

		panel.add(grid);
		return panel;
	}
	
	private JPanel buildComboBoxArea(String[] RTOs) {
		JPanel panel = new JPanel();

		GridBagPanel grid = new GridBagPanel();
		grid.add(new JLabel("Select Level of Service:"), 0, 0);
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
		if (action.getMaxPotentialCost(selectedRTO) > action.getFunds()) {
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

	private static void createAndShowWindow()
			throws UnsupportedOperationException, ParseException {
		InitAction action = InitAction.create("3.0", "adam", "password");
		action.processResponse("{\"Action\":\"INIT\",\"Job\":\"V-504.1461\",\"SubProjectID\":504,\"Funds\":115.01,\"EstimatedCost\":{\"TOF\":{\"RTO-24\":0.6,\"RTO-0\":12.00},\"Orbitrap\":{\"RTO-24\":0.85, \"RTO-0\":24.00},\"Iontrap\":{\"RTO-24\":1.02,\"RTO-0\":26.00}}}");
		PeakInvestigatorInitDialog dialog = new PeakInvestigatorInitDialog(
				null, "test", action);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public static void main(String args[]) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					createAndShowWindow();
				} catch (UnsupportedOperationException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
