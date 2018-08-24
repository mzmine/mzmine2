/*
 * Copyright 2006-2018 The MZmine 2 Development Team
 *
 * This file is part of MZmine 2.
 *
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package net.sf.mzmine.modules.peaklistmethods.identification.sirius.table.db;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.peaklistmethods.identification.sirius.table.SiriusCompound;
import net.sf.mzmine.util.GUIUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class DBFrame Creates a new window with Database links (e.g.: Pubchem: 1520) New window is
 * created on a position of `Display DB links` button (it is parent JFrame) Opens new window in a
 * browser if entry point is known, otherwise shows dialogue window.
 */
public class DBFrame extends JFrame implements ActionListener {
  private static final Logger logger = LoggerFactory.getLogger(DBFrame.class);

  private final JTable dbTable;
  private final DBTableModel model;

  public DBFrame(SiriusCompound compound, JButton button) {
    super("Database links");

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setBackground(Color.white);

    JPanel pnlLabelsAndList = new JPanel(new BorderLayout());
    pnlLabelsAndList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    pnlLabelsAndList.add(new JLabel("List of databases with IDs"), BorderLayout.NORTH);

    // Configure table
    dbTable = new JTable();
    model = new DBTableModel();
    dbTable.setModel(model);
    dbTable.setRowSorter(new TableRowSorter<>(dbTable.getModel()));
    dbTable.getTableHeader().setReorderingAllowed(false);
    dbTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JScrollPane listScroller = new JScrollPane(dbTable);
    listScroller.setPreferredSize(new Dimension(400, 250));
    listScroller.setAlignmentX(LEFT_ALIGNMENT);
    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
    listPanel.add(listScroller);
    listPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    pnlLabelsAndList.add(listPanel, BorderLayout.CENTER);

    // Buttons panel
    JPanel pnlButtons = new JPanel();
    pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.X_AXIS));
    pnlButtons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    GUIUtils.addButton(pnlButtons, "Open browser", null, this, "OPEN_WEB");
    setLayout(new BorderLayout());
    add(pnlLabelsAndList, BorderLayout.CENTER);
    add(pnlButtons, BorderLayout.SOUTH);

    // Set size and position
    setSize(400, 250);
    setLocation(button.getLocationOnScreen());
    pack();

    // Initiate loading of DB links into new table
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        model.addElement(compound);
      }
    });
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();

    if (action.equals("OPEN_WEB")) {
      int index = dbTable.getSelectedRow();

      if (index < 0) {
        MZmineCore.getDesktop().displayMessage(this,
            "Select one result to add as compound identity");
        return;
      }
      int realIndex = dbTable.convertRowIndexToModel(index);
      SiriusDBCompound compound = model.getCompoundAt(realIndex);

      // Generate url
      try {
        URL url = compound.generateURL();
        if (url == null)
          throw new RuntimeException("Unsupported DB");
        if (Desktop.isDesktopSupported()) {
          // Open uri in default browser
          Desktop.getDesktop().browse(url.toURI());
        }
      } catch (RuntimeException f) {
        MZmineCore.getDesktop().displayMessage(this, "Not supported Database");
      } catch (URISyntaxException | IOException d) {
        logger.error("Error happened on opening db link for {} : {}", compound.getDB(),
            compound.getID());
      }
    }
  }
}
