/*
 * Copyright 2013-2016 Veritomyx, Inc.
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

package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.mzmine.util.GUIUtils;

public class PeakInvestigatorTextDialog extends JDialog implements
		ActionListener {

	private static final long serialVersionUID = 1L;

	private static final Font DEFAULT_FONT = new Font(Font.MONOSPACED,
			Font.PLAIN, 12);

	JButton closeButton;

	public PeakInvestigatorTextDialog(JFrame parent, String title, File file)
			throws IOException {

		super(parent, title, Dialog.ModalityType.DOCUMENT_MODAL);

		Container verticalBox = Box.createVerticalBox();

		JTextArea textArea = setupTextArea(file);
		JScrollPane scrollPane = new JScrollPane(textArea);
		verticalBox.add(scrollPane);

		Container buttonBox = setupButtonBox();
		verticalBox.add(buttonBox);

		add(verticalBox);

		setMinimumSize(new Dimension(600, 400));
		setLocationRelativeTo(getParent());
	}

	private JTextArea setupTextArea(File file) throws IOException {
		byte[] data = Files.readAllBytes(file.toPath());
		JTextArea textArea = new JTextArea(new String(data));
		textArea.setEditable(false);
		textArea.setFont(DEFAULT_FONT);
		return textArea;
	}

	Container setupButtonBox() {
		Container buttonBox = Box.createHorizontalBox();
		closeButton = GUIUtils.addButton(buttonBox, "Close", null, this);
		return buttonBox;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object object = e.getSource();

		if (object == closeButton) {
			dispose();
		}
	}
}
