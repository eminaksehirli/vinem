package be.uantwerpen.adrem.gui.vinem.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

public class FilterOptions {

	public final static String UNDO = "FilterOptions.Undo";
	public final static String FILTERSEL = "FilterOptions.FilterSel";
	public final static String FILTERNOTSEL = "FilterOptions.FilterNotSel";
	public final static String CLEAR = "FilterOptions.Clear";

	private JPanel optionsPanel;
	private JButton undo;
	private JButton filterSel;
	private JButton filterNotSel;
	private JButton clear;

	public FilterOptions() {
		// the main panel
		optionsPanel = VinemView.createVerticalBoxPanel(300, 130);
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Filtering"));

		// top two buttons
		JPanel topPanel = VinemView.createHorizontalBoxPanel(300, 50);

		// bottom two buttons
		JPanel bottomPanel = VinemView.createHorizontalBoxPanel(300, 50);

		// add filter/clear/undo buttons
		filterSel = new JButton("Filter out selected");
		filterSel.setActionCommand(FILTERSEL);
		filterSel.setAlignmentX(Component.CENTER_ALIGNMENT);

		clear = new JButton("Clear filtereds");
		clear.setActionCommand(CLEAR);
		clear.setAlignmentX(Component.CENTER_ALIGNMENT);

		filterNotSel = new JButton("Filter out non-selected");
		filterNotSel.setActionCommand(FILTERNOTSEL);
		filterNotSel.setAlignmentX(Component.CENTER_ALIGNMENT);

		undo = new JButton("Undo");
		undo.setActionCommand(UNDO);
		undo.setAlignmentX(Component.CENTER_ALIGNMENT);

		topPanel.add(filterSel);
		topPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		topPanel.add(clear);

		bottomPanel.add(filterNotSel);
		bottomPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		bottomPanel.add(undo);

		optionsPanel.add(topPanel);
		optionsPanel.add(bottomPanel);
	}

	public void addButtonsListener(ActionListener buttonsListener) {
		filterSel.addActionListener(buttonsListener);
		filterNotSel.addActionListener(buttonsListener);
		clear.addActionListener(buttonsListener);
		undo.addActionListener(buttonsListener);
	}

	public JPanel getPanel() {
		return optionsPanel;
	}
}
