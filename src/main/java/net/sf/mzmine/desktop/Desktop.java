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
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.desktop;

import java.awt.Color;
import java.awt.Window;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.event.TreeModelListener;

import net.sf.mzmine.datamodel.PeakList;
import net.sf.mzmine.datamodel.RawDataFile;
import net.sf.mzmine.modules.MZmineModule;
import net.sf.mzmine.util.ExitCode;

/**
 * This interface represents the application GUI
 * 
 */
public interface Desktop extends MZmineModule {

    /**
     * Returns a reference to main application window. May return null if MZmine
     * is running in headless (batch) mode.
     * 
     * @return Main window frame
     */
    public JFrame getMainWindow();

    /**
     * Adds a new internal frame (JInternalFrame) to the desktop pane
     * 
     * @param frame Internal frame to add
     */
    public void addInternalFrame(JInternalFrame frame);

    /**
     * Displays a given text on the application status bar in black color
     * 
     * @param text
     *            Text to show
     */
    public void setStatusBarText(String text);

    /**
     * Displays a given text on the application status bar in a given color
     * 
     * @param text
     *            Text to show
     * @param textColor
     *            Text color
     */
    public void setStatusBarText(String text, Color textColor);

    /**
     * Displays a message box with a given text
     * 
     * @param msg
     *            Text to show
     */
    public void displayMessage(Window window, String msg);

    /**
     * Displays a message box with a given text
     * 
     * @param title
     *            Message box title
     * @param msg
     *            Text to show
     * @param log 
     * 			  if not null logger
     */
    public void displayMessage(Window window, String title, String msg);
    
    public void displayMessage(Window window, String title, String msg, Logger log);

    /**
     * Displays an error message box with a given text
     * 
     * @param msg
     *            Text to show
     * @param log 
     * 			  if not null logger
     */
    public void displayErrorMessage(Window window, String msg);

    /**
     * Displays an error message box with a given text
     * 
     * @param title
     *            Message box title
     * @param msg
     *            Text to show
     */
    public void displayErrorMessage(Window window, String title, String msg);

    public void displayErrorMessage(Window window, String title, String msg, Logger log);
    
    /**
     * Displays an error message
     *
     */
    public void displayException(Window window, Exception e);

    /**
     * Returns array of currently selected raw data files in GUI
     * 
     * @return Array of selected raw data files
     */
    public RawDataFile[] getSelectedDataFiles();

    /**
     * Returns array of currently selected peak lists in GUI
     * 
     * @return Array of selected peak lists
     */
    public PeakList[] getSelectedPeakLists();

    public void addRawDataTreeListener(TreeModelListener listener);

    public void addPeakListTreeListener(TreeModelListener listener);

    public void removeRawDataTreeListener(TreeModelListener listener);

    public void removePeakListTreeListener(TreeModelListener listener);

    @Nonnull
    public ExitCode exitMZmine();

}
