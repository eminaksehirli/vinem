package be.uantwerpen.adrem.gui.vinem;

import static java.lang.Integer.parseInt;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import be.uantwerpen.adrem.gui.vinem.controller.VinemController;
import be.uantwerpen.adrem.gui.vinem.model.VinemModel;
import be.uantwerpen.adrem.gui.vinem.view.VinemView;

import com.google.common.base.Joiner;

/**
 * Ask user for the working file. Manages a list of recent files.
 * 
 * @author Detlev Van Looy
 * @author M. Emin Aksehirli
 */

public class FileSelector {
	private static final String Input_File_Sep = "||";
	private static final String Recent_Key = "recent-files";
	private static final String Pref_Key = "be.adrem.cartiliner";

	private FileSelectorFrame frame;
	private List<InputFile> filesList = new ArrayList<>();

	public static void run() {
		FileSelector fs = new FileSelector();
		fs.frame.showTime();
	}

	public FileSelector() {
		frame = new FileSelectorFrame();

		frame.browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					final String fileName = file.getAbsolutePath();
					frame.fileNameField.setText(fileName);
					if (fileName.endsWith(".csv")) {
						frame.separatorTF.setText(",");
					}
				} else {
					System.out.println("Open command cancelled by user.");
				}
			}
		});

		frame.runBt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runWithANewFile();
			}
		});

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				saveRecents();
				frame.dispose();
			}
		});

		getRecents();
	}

	private void runWithANewFile() {
		InputFile inf = new InputFile();
		inf.fileName = frame.fileNameField.getText();
		inf.colsHasNames = frame.columnNamesCB.isSelected();
		inf.rowsHasNames = frame.rowNamesCB.isSelected();
		inf.separator = frame.separatorTF.getText();

		// There can be only one configuration for a file
		for (Iterator<InputFile> it = filesList.iterator(); it.hasNext();) {
			InputFile lf = it.next();
			if (lf.fileName.equals(inf.fileName)) {
				it.remove();
			}
		}

		filesList.add(0, inf);
		updateRecentsPane();

		runCarti(inf);
	}

	private void getRecents() {
		Preferences pref = Preferences.userRoot().node(Pref_Key);

		String recentFilesStr = pref.get(Recent_Key, Input_File_Sep);
		System.out.println("RecentFiles:" + recentFilesStr);

		String[] encodeds = recentFilesStr.split("\\|\\|");
		List<InputFile> newFilesList = new ArrayList<>(encodeds.length);
		if (encodeds.length > 0) {
			for (String encoded : encodeds) {
				InputFile inputFile = InputFile.fromString(encoded);
				if (inputFile == null) {
					problemWithTheRecents();
					break;
				}
				newFilesList.add(inputFile);
			}
		}
		filesList = newFilesList;
		updateRecentsPane();
	}

	private void updateRecentsPane() {
		frame.recentFilesPane.removeAll();
		for (InputFile inputFile : filesList) {
			addRecentFileButton(inputFile);
		}
		frame.recentFilesPane.updateUI();
	}

	private void problemWithTheRecents() {
		System.out.println("There is a problem with the recent files.");

		final JDialog dl = new JDialog(frame,
				"There is a problem with the recent files!");
		dl.setLayout(new BorderLayout(10, 10));

		JPanel infoPane = new JPanel(new FlowLayout());
		infoPane.add(new JLabel("There is a problem with the recent files!"));
		infoPane.add(new JLabel("This may be caused by a version change. "
				+ "Please select what you want to do."));

		dl.add(infoPane, BorderLayout.CENTER);

		JPanel fixBts = VinemView.createHorizontalBoxPanel(600, 30);
		JButton repairBt = new JButton("Try to repair");
		JButton showBt = new JButton("Just show the broken info");
		JButton clearBt = new JButton("Clear the recent files");

		fixBts.add(repairBt);
		fixBts.add(showBt);
		fixBts.add(clearBt);

		dl.add(fixBts, BorderLayout.SOUTH);

		repairBt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				upgradeRecents();
				dl.dispose();
			}
		});

		showBt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				listTheRecents();
			}
		});

		clearBt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int confPane = JOptionPane.showConfirmDialog(frame,
						"Do you want to remove all the recent files?");
				if (confPane == 0) {
					clearRecents();
					dl.dispose();
				}
			}
		});

		dl.setSize(600, 200);
		dl.setVisible(true);
	}

	private static void listTheRecents() {
		Preferences pref = Preferences.userRoot().node(Pref_Key);
		String recentFilesStr = pref.get(Recent_Key, Input_File_Sep);

		System.out.println("RecentFiles:" + recentFilesStr);

		String[] encodeds = recentFilesStr.split("\\|\\|");

		for (String encoded : encodeds) {
			System.out.println(encoded);
		}

		JOptionPane.showMessageDialog(null, "Printed to the console");
	}

	private void addRecentFileButton(final InputFile inputFile) {
		JButton closeBt = new JButton("X");
		closeBt.setToolTipText("Remove this file");
		JButton bt = new JButton(inputFile.fileName);
		bt.setToolTipText(String.format(
				"Col header:%b, Row header:%b, Separator:'%s'", inputFile.colsHasNames,
				inputFile.rowsHasNames, inputFile.separator));
		final JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
		buttonPane.add(closeBt);
		buttonPane.add(bt);
		frame.recentFilesPane.add(buttonPane);
		frame.recentFilesPane.revalidate();

		bt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				filesList.remove(inputFile);
				filesList.add(0, inputFile);
				updateRecentsPane();
				runCarti(inputFile);
			}
		});
		closeBt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				filesList.remove(inputFile);
				updateRecentsPane();
			}
		});
	}

	private void saveRecents() {
		Preferences pref = Preferences.userRoot().node(Pref_Key);
		if (filesList.isEmpty()) {
			try {
				pref.removeNode();
			} catch (BackingStoreException e) {
				System.err.println("Cannot remove the recent file history!");
				e.printStackTrace();
			}
			return;
		}

		Joiner j = Joiner.on(Input_File_Sep);
		String str = j.join(filesList);
		pref.put(Recent_Key, str);
	}

	private void clearRecents() {
		Preferences pref = Preferences.userRoot().node(Pref_Key);
		String oldValue = pref.get(Recent_Key, "");
		try {
			File tf = File.createTempFile(Pref_Key, ".txt");
			FileWriter fw = new FileWriter(tf);
			fw.write(oldValue);
			fw.close();
			final String msg = "Old value for the key is written to the file: "
					+ tf.getAbsolutePath();
			System.err.println(msg);
			// JOptionPane.showMessageDialog(null, msg);
		} catch (IOException e) {
			System.err.println("Could not save the old value: ");
			e.printStackTrace();
		}
		try {
			pref.clear();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		filesList.clear();
		updateRecentsPane();
	}

	private static void runCarti(InputFile inputFile) {
		// make sure the file exists
		if (!(new File(inputFile.fileName).isFile())) {
			System.out.println("File not found: " + inputFile);
			return;
		}

		VinemModel vinemModel = new VinemModel(inputFile);
		VinemView vinemView = new VinemView();
		VinemController vinemController = new VinemController(vinemModel, vinemView);

		vinemController.run();
	}

	private void upgradeRecents() {
		Preferences pref = Preferences.userRoot().node(Pref_Key);
		String recentFilesStr = pref.get(Recent_Key, Input_File_Sep);

		List<InputFile> inputFiles = null;
		try {
			inputFiles = recentsToNewFormat(recentFilesStr);
		} catch (Exception e) {
			System.err.println("Couln't convert!");
			JOptionPane.showMessageDialog(null, "Could not convert!", "Problem",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}

		filesList = inputFiles;
		System.out.println("Successfully converted the recent files.");
		saveRecents();
		getRecents();

		System.out.println("Old info: " + recentFilesStr);
	}

	private static List<InputFile> recentsToNewFormat(String recentFilesStr) {
		String[] paths = recentFilesStr.split("\\|\\|");
		List<InputFile> inputs = new ArrayList<>(paths.length);
		if (paths.length > 0) {
			for (String path : paths) {
				InputFile inf = new InputFile();

				inf.fileName = path;
				inf.rowsHasNames = false;
				inf.colsHasNames = false;
				inf.separator = " ";

				inputs.add(inf);
			}
		}

		Joiner j = Joiner.on(Input_File_Sep);
		String str = j.join(inputs);

		List<InputFile> filesList = new ArrayList<>();
		String[] encodeds = str.split("\\|\\|");
		if (encodeds.length > 0) {
			for (String encoded : encodeds) {
				InputFile inputFile = InputFile.fromString(encoded);
				filesList.add(inputFile);
			}
		}
		return filesList;
	}

	static private class InputFile extends be.uantwerpen.adrem.cart.io.InputFile {
		static final String Sep = "|";

		static InputFile fromString(String str) {
			InputFile inf = new InputFile();
			int slp = str.indexOf(Sep);
			if (slp < 0) {
				return null;
			}
			int sl = parseInt(str.substring(0, slp));
			inf.separator = str.substring(slp + 1, slp + 1 + sl);
			inf.colsHasNames = str.charAt(slp + 1 + sl) == 'Y';
			inf.rowsHasNames = str.charAt(slp + 1 + sl + 1) == 'Y';
			inf.fileName = str.substring(slp + 1 + sl + 1 + 1);

			return inf;
		}

		@Override
		public String toString() {
			int sl = separator.length();

			String col = colsHasNames ? "Y" : "N";
			String row = rowsHasNames ? "Y" : "N";

			return sl + Sep + separator + col + row + fileName;
		}
	}
}
