package cart.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import cart.model.Obj;

public class SelOptions {

	public final static String CLEAR = "SelOptions.Clear";

	private JPanel optionsPanel;
	private JList<Obj> list;
	private DefaultListModel<Obj> listModel;
	private JButton clear;
	private JRadioButton modeSelect;
	private JRadioButton modeAnd;
	private JRadioButton modeOr;

	public SelOptions(List<Obj> orderedObjs) {
		// the main panel
		optionsPanel = CartiView.createVerticalBoxPanel(300, 300);
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Selection"));

		// list of selected/unselected objects
		listModel = new DefaultListModel<>();
		for (int i = 0; i < orderedObjs.size(); i++) {
			listModel.addElement(orderedObjs.get(i));
		}
		list = new JList<>(listModel);
		JScrollPane listPane = new JScrollPane(list);
		listPane.setPreferredSize(new Dimension(200, 200));
		listPane.setMaximumSize(new Dimension(200, 200));
		listPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		optionsPanel.add(listPane);

		// add clear button
		clear = new JButton("Clear Selection");
		clear.setActionCommand(CLEAR);
		clear.setAlignmentX(Component.CENTER_ALIGNMENT);
		optionsPanel.add(clear);

		// add radio buttons
		JPanel selModePanel = CartiView.createHorizontalBoxPanel(300, 50);
		ButtonGroup selectButtons = new ButtonGroup();
		modeSelect = new JRadioButton("Select", true);
		modeAnd = new JRadioButton("And");
		modeOr = new JRadioButton("Or");
		selectButtons.add(modeSelect);
		selectButtons.add(modeAnd);
		selectButtons.add(modeOr);
		selModePanel.add(modeSelect);
		selModePanel.add(modeAnd);
		selModePanel.add(modeOr);
		selModePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		optionsPanel.add(selModePanel);
	}

	public void addButtonsListener(ActionListener buttonListener) {
		clear.addActionListener(buttonListener);
	}

	public void addListSelectionListener(ListSelectionListener listListener) {
		list.addListSelectionListener(listListener);
	}

	public JPanel getPanel() {
		return optionsPanel;
	}

	public void updateSelected(List<Obj> orderedObjs, Set<Integer> selecteds) {
		listModel.clear();

		// fill the list
		for (Obj objId : orderedObjs) {
			listModel.addElement(objId);
		}

		// find selected indices
		int[] selectedIndices = new int[selecteds.size()];
		int selIx = 0;

		for (int i = 0; i < orderedObjs.size(); i++) {
			if (selecteds.contains(orderedObjs.get(i).id)) {
				selectedIndices[selIx] = i;
				selIx++;
			}
		}

		list.setSelectedIndices(selectedIndices);
	}

	/**
	 * @return Object ids which were selected by the user (by clicking on them in
	 *         the list)
	 */
	public Set<Integer> getSelecteds() {
		final List<Obj> sels = list.getSelectedValuesList();
		Set<Integer> selecteds = new HashSet<>(sels.size());
		for (Obj i : sels) {
			selecteds.add(i.id);
		}
		return selecteds;
	}

	public boolean selModeIsSelect() {
		return modeSelect.isSelected();
	}

	public boolean selModeIsAnd() {
		return modeAnd.isSelected();
	}

	public boolean selModeIsOr() {
		return modeOr.isSelected();
	}

}
