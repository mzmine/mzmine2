package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
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

	JButton closeButton;

	public PeakInvestigatorTextDialog(JFrame parent, String title,
			String filename) throws IOException {

		this(parent, title, new File(filename));
	}

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
