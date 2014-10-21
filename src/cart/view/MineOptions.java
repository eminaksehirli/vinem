package cart.view;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MineOptions {

	public final static String MINEIMM = "MineOptions.MineIMM";
	public final static String MINERMM = "MineOptions.MineRMM";
	public final static String MINERMMSEL = "MineOptions.MineRMMSEL";
	public final static String FINDRELDIMS = "MineOptions.FindRelDims";

	final static String IMMCARD = "ItemSetMaximalMiner";
	final static String RMMCARD = "RandomMaximalMiner";
	final static String RELDIMSCARD = "Find related dims";

	private JPanel minePanel;
	private JPanel cards;
	private JButton mineIMMButton;
	private JButton mineRMMButton;
	private JButton mineRMMSelButton;
	private JButton findRelDimsButton;
	private JTextField minLenField;
	private JTextField minSupFieldRMM;
	private JTextField numOfItemSetsFieldRMM;
	private JTextField minSupFieldRelDims;
	private JTextField numOfItemSetsFieldRelDims;
	private JComboBox<String> cb;

	public void init() {
		// the main panel
		minePanel = CartiView.createVerticalBoxPanel(300, 200);
		minePanel.setBorder(BorderFactory.createTitledBorder("Mining"));

		// the combo box for selecting the miner
		String comboBoxItems[] = { IMMCARD, RMMCARD, RELDIMSCARD };
		cb = new JComboBox<String>(comboBoxItems);
		cb.setEditable(false);
		cb.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent evt) {
				// switch card
				CardLayout cl = (CardLayout) (cards.getLayout());
				cl.show(cards, (String) evt.getItem());
			}

		});
		cb.setAlignmentX(Component.CENTER_ALIGNMENT);
		cb.setPreferredSize(new Dimension(300, 30));
		cb.setMaximumSize(new Dimension(300, 30));
		minePanel.add(cb);

		// Create the cards
		// IMM card
		JPanel cardIMM = CartiView.createVerticalBoxPanel(300, 150);
		cardIMM.setAlignmentX(Component.CENTER_ALIGNMENT);

		// add button for mining to card
		mineIMMButton = new JButton("Mine");
		mineIMMButton.setActionCommand(MINEIMM);
		mineIMMButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		cardIMM.add(mineIMMButton);
		cardIMM.add(Box.createRigidArea(new Dimension(0, 10)));

		// add minLen to card
		JPanel minLenPanel = createPanelWithLabel("minLen");
		minLenField = new JTextField();
		minLenField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		minLenPanel.add(minLenField);
		cardIMM.add(minLenPanel);

		// RMM card
		JPanel cardRMM = CartiView.createVerticalBoxPanel(300, 150);
		cardRMM.setAlignmentX(Component.CENTER_ALIGNMENT);

		// add buttons for mining to card
		mineRMMButton = new JButton("Mine");
		mineRMMButton.setActionCommand(MINERMM);
		mineRMMButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		cardRMM.add(mineRMMButton);
		cardRMM.add(Box.createRigidArea(new Dimension(0, 10)));

		mineRMMSelButton = new JButton("Mine selected");
		mineRMMSelButton.setActionCommand(MINERMMSEL);
		mineRMMSelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		cardRMM.add(mineRMMSelButton);
		cardRMM.add(Box.createRigidArea(new Dimension(0, 10)));

		// add minSup to card
		JPanel minSupPanel = createPanelWithLabel("minSup");
		minSupFieldRMM = new JTextField();
		minSupFieldRMM.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		minSupPanel.add(minSupFieldRMM);
		cardRMM.add(minSupPanel);

		// add numOfItemSets to card
		JPanel numOfItemSetsPanel = createPanelWithLabel("numOfItemSets");
		numOfItemSetsFieldRMM = new JTextField();
		numOfItemSetsFieldRMM.setMaximumSize(new Dimension(Integer.MAX_VALUE,
				25));
		numOfItemSetsPanel.add(numOfItemSetsFieldRMM);
		cardRMM.add(numOfItemSetsPanel);

		// RELDIMS card
		JPanel cardRelDims = CartiView.createVerticalBoxPanel(300, 150);
		cardRelDims.setAlignmentX(Component.CENTER_ALIGNMENT);

		// add button for finding rel dims
		findRelDimsButton = new JButton("Find related dims");
		findRelDimsButton.setActionCommand(FINDRELDIMS);
		findRelDimsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		cardRelDims.add(findRelDimsButton);
		cardRelDims.add(Box.createRigidArea(new Dimension(0, 10)));

		// add minSup to card
		JPanel minSupPanel2 = createPanelWithLabel("minSup");
		minSupFieldRelDims = new JTextField();
		minSupFieldRelDims.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		minSupPanel2.add(minSupFieldRelDims);
		cardRelDims.add(minSupPanel2);

		// add numOfItemSets to card
		JPanel numOfItemSetsPanel2 = createPanelWithLabel("numOfItemSets");
		numOfItemSetsFieldRelDims = new JTextField("2000");
		numOfItemSetsFieldRelDims.setMaximumSize(new Dimension(
				Integer.MAX_VALUE, 25));
		numOfItemSetsPanel2.add(numOfItemSetsFieldRelDims);
		cardRelDims.add(numOfItemSetsPanel2);

		// Create the panel that contains the cards
		cards = new JPanel(new CardLayout());
		cards.add(cardIMM, IMMCARD);
		cards.add(cardRMM, RMMCARD);
		cards.add(cardRelDims, RELDIMSCARD);
		cards.setAlignmentX(Component.CENTER_ALIGNMENT);
		cards.setMaximumSize(new Dimension(300, 150));

		minePanel.add(cards);
	}

	private JPanel createPanelWithLabel(String labelString) {
		JPanel panel = CartiView.createHorizontalBoxPanel(300, 40);
		JLabel label = new JLabel(labelString + ": ");
		panel.add(label);

		panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		return panel;
	}

	public void addButtonsListener(ActionListener buttonsListener) {
		mineIMMButton.addActionListener(buttonsListener);
		mineRMMButton.addActionListener(buttonsListener);
		mineRMMSelButton.addActionListener(buttonsListener);
		findRelDimsButton.addActionListener(buttonsListener);
	}

	public JPanel getPanel() {
		return minePanel;
	}

	/**
	 * @return Value of the minLen text field, returns -1 if the text field does
	 *         not contain an integer larger than 1.
	 */
	public int getMinLenVal() {
		int minLen = -1;
		try {
			minLen = Integer.parseInt(minLenField.getText());
		} catch (NumberFormatException e) {
			minLen = -1;
		}

		if (minLen < 2) {
			JOptionPane.showMessageDialog(new JFrame(),
					"minLen must be an integer larger than 1.", "error",
					JOptionPane.ERROR_MESSAGE);
			return -1;
		}

		return minLen;
	}

	/**
	 * @return Value of the minSup text field, returns -1 if the text field does
	 *         not contain an integer larger than 0
	 */
	public int getMinSupVal() {
		int minSup = -1;
		try {
			if (cb.getSelectedItem().toString().equals(RMMCARD)) {
				minSup = Integer.parseInt(minSupFieldRMM.getText());
			} else if (cb.getSelectedItem().toString().equals(RELDIMSCARD)) {
				minSup = Integer.parseInt(minSupFieldRelDims.getText());
			}
		} catch (NumberFormatException e) {
			minSup = -1;
		}

		if (minSup < 1) {
			JOptionPane.showMessageDialog(new JFrame(),
					"minSup  must be an integer larger than 0.", "error",
					JOptionPane.ERROR_MESSAGE);
			return -1;
		}

		return minSup;
	}

	/**
	 * @return Value of the numOfItemSets text field, returns -1 if the text
	 *         field does not contain an integer larger than 0
	 */
	public int getNumOfItemSetsVal() {
		int numOfItemSets = -1;
		try {
			if (cb.getSelectedItem().toString().equals(RMMCARD)) {
				numOfItemSets = Integer.parseInt(numOfItemSetsFieldRMM
						.getText());
			} else if (cb.getSelectedItem().toString().equals(RELDIMSCARD)) {
				numOfItemSets = Integer.parseInt(numOfItemSetsFieldRelDims
						.getText());
			}
		} catch (NumberFormatException e) {
			numOfItemSets = -1;
		}

		if (numOfItemSets < 1) {
			JOptionPane.showMessageDialog(new JFrame(),
					"numOfItemSets must be an integer larger than 0.", "error",
					JOptionPane.ERROR_MESSAGE);
			return -1;
		}

		return numOfItemSets;
	}

	/**
	 * Sets the value of the minSup textField.
	 * 
	 * @param minSup
	 */
	public void setMinSupVal(int minSup) {
		minSupFieldRMM.setText(Integer.toString(minSup));
		minSupFieldRelDims.setText(Integer.toString(minSup));
	}
}
