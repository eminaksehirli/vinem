package vinem.gui;

import static vinem.view.VinemView.createHorizontalBoxPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import vinem.view.VinemView;

public class FileSelectorFrame extends JFrame {
	private static final long serialVersionUID = 3599962945189307089L;

	JPanel recentFilesPane;
	JCheckBox rowNamesCB;
	JCheckBox columnNamesCB;
	JTextField separatorTF;
	JTextField fileNameField;
	JButton browseButton;
	JButton runBt;

	public FileSelectorFrame() {
		setLayout(new BorderLayout(10, 0));

		JPanel newFilePane = new JPanel(new BorderLayout(0, 0));
		newFilePane.setBorder(BorderFactory.createEtchedBorder());
		newFilePane.add(new JLabel("Select a new file"), BorderLayout.NORTH);

		JPanel filePane = VinemView.createHorizontalBoxPanel(500, 40);
		fileNameField = new JTextField(50);
		filePane.add(fileNameField);

		browseButton = new JButton("Browse File");

		filePane.add(browseButton);

		rowNamesCB = new JCheckBox("Rows Has Names");
		columnNamesCB = new JCheckBox("Column Has Names");
		separatorTF = new JTextField(" ", 2);
		runBt = new JButton("Run with a new file");

		JPanel detailsPane = createHorizontalBoxPanel(600, 40);
		detailsPane.add(rowNamesCB);
		detailsPane.add(columnNamesCB);
		detailsPane.add(new JLabel("Column separator:"));
		detailsPane.add(separatorTF);
		detailsPane.add(runBt);

		JPanel fileInfoPane = VinemView.createVerticalBoxPanel(500, 90);
		fileInfoPane.add(filePane);
		fileInfoPane.add(detailsPane);

		newFilePane.add(fileInfoPane, BorderLayout.CENTER);

		add(newFilePane, BorderLayout.PAGE_START);

		JPanel recentsPane = new JPanel(new BorderLayout());
		recentsPane.add(new JLabel("Work with an old file"), BorderLayout.NORTH);
		recentsPane.setBorder(BorderFactory.createEtchedBorder());

		// contains buttons for recent files
		recentFilesPane = new JPanel();
		recentFilesPane.setLayout(new BoxLayout(recentFilesPane, BoxLayout.Y_AXIS));
		// recentsFilesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JScrollPane recentsScrollPane = new JScrollPane(recentFilesPane);
		recentsScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		recentsPane.add(recentsScrollPane, BorderLayout.CENTER);

		add(recentsPane, BorderLayout.CENTER);
	}

	public void showTime() {
		this.setMinimumSize(new Dimension(700, 800));
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
}
