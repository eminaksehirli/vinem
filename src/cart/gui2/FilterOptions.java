package cart.gui2;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

public class FilterOptions {

	final static String UNDO = "FilterOptions.Undo";
	final static String FILTER = "FilterOptions.Filter";
	final static String CLEAR = "FilterOptions.Clear";

	private JPanel optionsPanel;
	private JButton undo;
	private JButton filter;
	private JButton clear;
	
	public void init(ActionListener buttonsListener) {
		// the main panel
		optionsPanel = CartiView.createHorizontalBoxPanel(300,50);
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Filtering"));
		
		// add filter/undo buttons
		filter = new JButton("Filter");
		filter.setActionCommand(FILTER);
		filter.addActionListener(buttonsListener);
		filter.setAlignmentX(Component.CENTER_ALIGNMENT);
		clear = new JButton("Clear");
		clear.setActionCommand(CLEAR);
		clear.addActionListener(buttonsListener);
		clear.setAlignmentX(Component.CENTER_ALIGNMENT);
		undo = new JButton("Undo");
		undo.setActionCommand(UNDO);
		undo.addActionListener(buttonsListener);
		undo.setAlignmentX(Component.CENTER_ALIGNMENT);
		optionsPanel.add(filter);
		optionsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		optionsPanel.add(clear);
		optionsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		optionsPanel.add(undo);
	}
	
	public JPanel getPanel() {
		return optionsPanel;
	}
}
