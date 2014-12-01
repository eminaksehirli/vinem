package cart.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import cart.gui2.Dissimilarity;

public class DistOptions {

	public final static String ADD = "DistOptions.Add";

	private JPanel distPanel;
	private JButton add;
	private JRadioButton distEucl;
	private JRadioButton distCos;
	private JList<Integer> list;
	private JComboBox<String> cBox;
	private DefaultComboBoxModel<String> cBoxModel;

	public DistOptions(Set<Integer> dimList, List<Dissimilarity> distMeasures) {
		// the main panel
		distPanel = CartiView.createVerticalBoxPanel(300, 240);
		distPanel.setBorder(BorderFactory.createTitledBorder("Distance measures"));

		// panel containing radio buttons, dim list, add button
		JPanel topPanel = CartiView.createHorizontalBoxPanel(300, 200);

		// distance mode radio buttons
		JPanel distModePanel = CartiView.createVerticalBoxPanel(100, 200);
		ButtonGroup distButtons = new ButtonGroup();
		distEucl = new JRadioButton("Euclidian", true);
		distCos = new JRadioButton("Cosine");
		distButtons.add(distEucl);
		distButtons.add(distCos);
		distModePanel.add(distEucl);
		distModePanel.add(distCos);
		distModePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		topPanel.add(distModePanel);

		// list of dimensions
		DefaultListModel<Integer> listModel = new DefaultListModel<Integer>();
		for (Integer dim : dimList) {
			listModel.addElement(dim);
		}
		list = new JList<Integer>(listModel);
		JScrollPane listPane = new JScrollPane(list);
		listPane.setPreferredSize(new Dimension(125, 200));
		listPane.setMaximumSize(new Dimension(125, 200));
		topPanel.add(listPane);
		topPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		// add button
		add = new JButton("Add");
		add.setActionCommand(ADD);
		add.setAlignmentY(Component.CENTER_ALIGNMENT);
		topPanel.add(add);

		distPanel.add(topPanel);

		// add measure combo box
		cBoxModel = new DefaultComboBoxModel<String>();
		for (Dissimilarity distMeasure : distMeasures) {
			cBoxModel.addElement(distMeasure.toString());
		}
		cBox = new JComboBox<String>(cBoxModel);
		cBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		cBox.setPreferredSize(new Dimension(300, 30));
		cBox.setMaximumSize(new Dimension(300, 30));

		distPanel.add(cBox);
	}

	public void addButtonsListener(ActionListener buttonListener) {
		add.addActionListener(buttonListener);
	}

	public void addBoxListener(ActionListener boxListener) {
		cBox.addActionListener(boxListener);
	}

	public JPanel getPanel() {
		return distPanel;
	}

	public void addDistMeasure(String distMeasure) {
		cBoxModel.addElement(distMeasure);
	}

	/**
	 * @return Set containing the dimensions which were selected by the user (by
	 *         clicking on them in the list)
	 */
	public List<Integer> getSelectedDims() {
		return list.getSelectedValuesList();
	}

	public boolean distModeIsEuclidian() {
		return distEucl.isSelected();
	}

	public boolean distModeIsCosine() {
		return distCos.isSelected();
	}

	public int getSelectedMeasureId() {
		return cBox.getSelectedIndex();
	}

	public void setSelectedMeasureId(int id) {
		cBox.setSelectedIndex(id);
	}
}
