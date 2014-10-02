package cart.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;

public class FilterOptions {

	public final static String UNDO = "FilterOptions.Undo";
	public final static String FILTER = "FilterOptions.Filter";
	public final static String CLEAR = "FilterOptions.Clear";

	private JPanel optionsPanel;
	private JButton undo;
	private JButton filter;
	private JButton clear;

	public void init() {
		// the main panel
		optionsPanel = CartiView.createHorizontalBoxPanel(300, 50);
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Filtering"));

		// add filter/undo buttons
		filter = new JButton("Filter");
		filter.setActionCommand(FILTER);
		filter.setAlignmentX(Component.CENTER_ALIGNMENT);

		clear = new JButton("Clear");
		clear.setActionCommand(CLEAR);
		clear.setAlignmentX(Component.CENTER_ALIGNMENT);

		undo = new JButton("Undo");
		undo.setActionCommand(UNDO);
		undo.setAlignmentX(Component.CENTER_ALIGNMENT);

		optionsPanel.add(filter);
		optionsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		optionsPanel.add(clear);
		optionsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		optionsPanel.add(undo);
	}

	public void addButtonsListener(ActionListener buttonsListener) {
		filter.addActionListener(buttonsListener);
		clear.addActionListener(buttonsListener);
		undo.addActionListener(buttonsListener);
	}

	public JPanel getPanel() {
		return optionsPanel;
	}
}
