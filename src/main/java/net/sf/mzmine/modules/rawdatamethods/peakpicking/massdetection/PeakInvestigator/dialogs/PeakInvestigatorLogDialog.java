package net.sf.mzmine.modules.rawdatamethods.peakpicking.massdetection.PeakInvestigator.dialogs;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.mzmine.util.GUIUtils;

public class PeakInvestigatorLogDialog extends JDialog implements
		ActionListener {

	private static final long serialVersionUID = 1L;
	private Logger logger;

	private JButton closeButton;
	private JButton saveButton;
	private final File file;

	public PeakInvestigatorLogDialog(JFrame parent, File file)
			throws IOException {
		super(parent, "PeakInvestigator Job Log",
				Dialog.ModalityType.DOCUMENT_MODAL);

		this.logger = Logger.getLogger(this.getClass().getName());
		this.file = file;

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

	private Container setupButtonBox() {
		Container buttonBox = Box.createHorizontalBox();
		closeButton = GUIUtils.addButton(buttonBox, "Close", null, this);
		saveButton = GUIUtils.addButton(buttonBox, "Save as...", null, this);
		return buttonBox;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object object = e.getSource();

		if (object == closeButton) {
			dispose();
		} else if (object == saveButton) {
			saveLogFile();
		}
	}

	private void saveLogFile() {
		FileDialog dialog = new FileDialog(this, "Save job log...",
				FileDialog.SAVE);

		dialog.setFile(file.getName());
		dialog.setMultipleMode(false);
		dialog.setVisible(true);

		File[] files = dialog.getFiles();
		if (files.length != 1) {
			logger.info("Number of files specified in saveLogFile(): "
					+ files.length);
			return;
		}

		try {
			Files.copy(file.toPath(), files[0].toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			error("Problem saving job log file.");
		}
	}

	private void error(String message) {
		JOptionPane.showMessageDialog(this, message, "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	private static void createAndShowWindow() throws IOException {
		String filename = System.getProperty("filename");
		File file = new File(filename);

		PeakInvestigatorLogDialog dialog = new PeakInvestigatorLogDialog(null,
				file);
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
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
