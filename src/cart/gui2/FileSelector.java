package cart.gui2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import cart.controller.CartiController;
import cart.model.CartiModel;
import cart.view.CartiView;

import com.google.common.base.Joiner;

public class FileSelector extends JFrame {
	private static final long serialVersionUID = 3599962945189307089L;

	private List<String> filesList = new ArrayList<>();
	private JPanel recentsPanel;

	private static class PrevFileListener implements ActionListener {
		String path;

		PrevFileListener(String path) {
			this.path = path;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			runCarti(path);
		}
	}

	public FileSelector() {
		setLayout(new BorderLayout(10, 10));

		// contains browse and clear button
		JPanel optionsPanel = CartiView.createHorizontalBoxPanel(550, 60);

		// browse button
		JButton browseButton = new JButton("Run With a New File");
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				int returnVal = fc.showOpenDialog(FileSelector.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					final String path = file.getAbsolutePath();
					runWithANewFile(path);
				} else {
					System.out.println("Open command cancelled by user.");
				}
			}
		});
		optionsPanel.add(browseButton);
		optionsPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		// clear button
		JButton clearButton = new JButton("Clear Recent Files");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearRecents();
			}
		});
		optionsPanel.add(clearButton);

		add(optionsPanel, BorderLayout.PAGE_START);

		// contains buttons for recent files
		recentsPanel = new JPanel();
		recentsPanel.setLayout(new BoxLayout(recentsPanel, BoxLayout.Y_AXIS));
		recentsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		getRecents();

		JScrollPane recentsScrollPane = new JScrollPane(recentsPanel);
		add(recentsScrollPane, BorderLayout.CENTER);
	}

	private void getRecents() {
		Preferences pref = Preferences.userRoot().node("be.adrem.cartiliner");

		String recentFilesStr = pref.get("recent-files", "||");
		System.out.println("RecentFiles:" + recentFilesStr);

		String[] files = recentFilesStr.split("\\|\\|");
		if (files.length > 0) {
			for (String file : files) {
				filesList.add(file);
				addRecentFileButton(file);
			}
		}
	}

	private void runWithANewFile(String path) {
		boolean alreadyInFilesList = false;

		// check for duplicate
		for (String file : filesList) {
			if (file.equals(path)) {
				alreadyInFilesList = true;
			}
		}

		if (!alreadyInFilesList) {
			filesList.add(path);
			saveRecents();
			addRecentFileButton(path);
		}

		runCarti(path);
	}

	private void addRecentFileButton(String filePath) {
		JButton bt = new JButton(filePath);
		bt.addActionListener(new PrevFileListener(filePath));
		recentsPanel.add(bt);
		recentsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		recentsPanel.revalidate();
	}

	public void showTime() {
		this.setMinimumSize(new Dimension(550, 500));
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	private void saveRecents() {
		Preferences pref = Preferences.userRoot().node("be.adrem.cartiliner");

		Joiner j = Joiner.on("||");
		String str = j.join(filesList);
		pref.put("recent-files", str);
	}

	private void clearRecents() {
		Preferences pref = Preferences.userRoot().node("be.adrem.cartiliner");
		try {
			pref.clear();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		filesList.clear();
		recentsPanel.removeAll();
		recentsPanel.updateUI();
	}

	private static void runCarti(String filePath) {
		// make sure the file exists
		if (!(new File(filePath).isFile())) {
			System.out.println("File not found: " + filePath);
			return;
		}

		CartiModel cartiModel = new CartiModel();
		CartiView cartiView = new CartiView();
		CartiController cartiController = new CartiController(cartiModel, cartiView);

		cartiController.run(filePath);
	}
}
