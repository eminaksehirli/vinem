package cart.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.common.base.Joiner;

public class FileSelector extends JFrame {
	private static final long serialVersionUID = 3599962945189307089L;

	private List<String> filesList = new ArrayList<>();

	private static class Listener implements ActionListener {
		String path;

		Listener(String path) {
			this.path = path;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			CartiCombineGUI gui = new CartiCombineGUI();
			gui.run(path);
		}
	}

	public FileSelector() {
		setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

		JPanel manualFilePane = new JPanel();
		JButton bt = new JButton("Browse For a New File");
		bt.addActionListener(new ActionListener() {
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
		// manualFilePane.add(bt);
		// add(manualFilePane);
		add(bt);
		getRecents();
	}

	protected void runWithANewFile(String path) {
		filesList.add(path);
		addButton(path);
		saveRecents();
		CartiCombineGUI gui = new CartiCombineGUI();
		gui.run(path);
	}

	public void addButton(String file) {
		JButton bt = new JButton(file);
		bt.addActionListener(new Listener(file));
		add(bt);
	}

	public void showTime() {
		this.setMinimumSize(new Dimension(500, 500));
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public void getRecents() {
		Preferences pref = Preferences.userRoot().node("be.adrem.cartiliner");
		String recentFilesStr = pref.get("recent-files",
				"/home/memin/research/data/synth/6c10d/6c10d.mime");
		System.out.println("RecentFiles:" + recentFilesStr);
		String[] files = recentFilesStr.split("\\|\\|");

		if (files.length > 0) {
			for (String file : files) {
				addButton(file);
				filesList.add(file);
			}
		}
	}

	public void saveRecents() {
		Preferences pref = Preferences.userRoot().node("be.adrem.cartiliner");

		Joiner j = Joiner.on("||");
		String str = j.join(filesList);
		pref.put("recent-files", str);
	}
}
