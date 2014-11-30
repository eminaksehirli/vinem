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
	final static String NOISECARD = "Find outliers";

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
	NoiseOptions noiseOpts;

	public MineOptions() {
		// the main panel
		minePanel = CartiView.createVerticalBoxPanel(300, 200);
		minePanel.setBorder(BorderFactory.createTitledBorder("Mining"));

		// the combo box for selecting the miner
		String comboBoxItems[] = { IMMCARD, RMMCARD, RELDIMSCARD, NOISECARD };
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
		JPanel cardIMM = createFastMinerCard();
		JPanel cardRMM = createSamplingMinerCard();
		JPanel cardRelDims = createFindRelevantDimsCard();
		JPanel noiseCard = createNoiseDimsCard();

		// Create the panel that contains the cards
		cards = new JPanel(new CardLayout());
		cards.add(cardIMM, IMMCARD);
		cards.add(cardRMM, RMMCARD);
		cards.add(cardRelDims, RELDIMSCARD);
		cards.add(noiseCard, NOISECARD);
		cards.setAlignmentX(Component.CENTER_ALIGNMENT);
		cards.setMaximumSize(new Dimension(300, 150));

		minePanel.add(cards);
	}

	private JPanel createNoiseDimsCard() {
		noiseOpts = new NoiseOptions();
		return noiseOpts.getPanel();
	}

	private JPanel createFindRelevantDimsCard() {
		JPanel cardRelDims = CartiView.createVerticalBoxPanel(300, 150);
		cardRelDims.setAlignmentX(Component.CENTER_ALIGNMENT);

		// add minSup to card
		JPanel minSupPane = CartiView.createPanelWithLabel("minSup");
		minSupFieldRelDims = new JTextField();
		minSupFieldRelDims.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		minSupPane.add(minSupFieldRelDims);
		cardRelDims.add(minSupPane);

		// add numOfItemSets to card
		JPanel numOfItemSetsPanel2 = CartiView
				.createPanelWithLabel("numOfItemSets");
		numOfItemSetsFieldRelDims = new JTextField("2000");
		numOfItemSetsFieldRelDims.setMaximumSize(new Dimension(Integer.MAX_VALUE,
				25));
		numOfItemSetsPanel2.add(numOfItemSetsFieldRelDims);
		cardRelDims.add(numOfItemSetsPanel2);

		// add button for finding rel dims
		findRelDimsButton = new JButton("Find related dims");
		findRelDimsButton.setActionCommand(FINDRELDIMS);
		findRelDimsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		cardRelDims.add(findRelDimsButton);
		cardRelDims.add(Box.createRigidArea(new Dimension(0, 10)));
		return cardRelDims;
	}

	private JPanel createSamplingMinerCard() {
		JPanel cardRMM = CartiView.createVerticalBoxPanel(300, 150);
		cardRMM.setAlignmentX(Component.CENTER_ALIGNMENT);

		// add minSup to card
		JPanel minSupPanel = CartiView.createPanelWithLabel("minSup");
		minSupFieldRMM = new JTextField();
		minSupFieldRMM.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		minSupPanel.add(minSupFieldRMM);
		cardRMM.add(minSupPanel);

		// add numOfItemSets to card
		JPanel numOfItemSetsPanel = CartiView.createPanelWithLabel("numOfItemSets");
		numOfItemSetsFieldRMM = new JTextField();
		numOfItemSetsFieldRMM.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		numOfItemSetsPanel.add(numOfItemSetsFieldRMM);
		cardRMM.add(numOfItemSetsPanel);

		cardRMM.add(Box.createRigidArea(new Dimension(0, 10)));

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
		return cardRMM;
	}

	private JPanel createFastMinerCard() {
		JPanel cardIMM = CartiView.createVerticalBoxPanel(300, 150);
		cardIMM.setAlignmentX(Component.CENTER_ALIGNMENT);

		// add minLen to card
		JPanel minLenPanel = CartiView.createPanelWithLabel("minLen");
		minLenField = new JTextField();
		minLenField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		minLenPanel.add(minLenField);
		cardIMM.add(minLenPanel);

		cardIMM.add(Box.createRigidArea(new Dimension(0, 10)));

		// add button for mining to card
		mineIMMButton = new JButton("Mine");
		mineIMMButton.setActionCommand(MINEIMM);
		mineIMMButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		cardIMM.add(mineIMMButton);
		return cardIMM;
	}

	public void addButtonsListener(ActionListener buttonsListener) {
		mineIMMButton.addActionListener(buttonsListener);
		mineRMMButton.addActionListener(buttonsListener);
		mineRMMSelButton.addActionListener(buttonsListener);
		findRelDimsButton.addActionListener(buttonsListener);
		noiseOpts.addButtonsListener(buttonsListener);
	}

	public JPanel getPanel() {
		return minePanel;
	}

	/**
	 * @return Value of the minLen text field, returns -1 if the text field does
	 *         not contain an integer larger than 1.
	 */
	public int getMinLenVal() {
		return validateInt(minLenField.getText(), 1, "Minimum length");
	}

	/**
	 * @return Value of the minSup text field, returns -1 if the text field does
	 *         not contain an integer larger than 0
	 */
	public int getMinSupVal() {
		String text = "";
		if (cb.getSelectedItem().toString().equals(RMMCARD)) {
			text = minSupFieldRMM.getText();
		} else if (cb.getSelectedItem().toString().equals(RELDIMSCARD)) {
			text = minSupFieldRelDims.getText();
		}

		return validateInt(text, 1, "Minimum support");
	}

	/**
	 * @return Value of the numOfItemSets text field, returns -1 if the text field
	 *         does not contain an integer larger than 0
	 */
	public int getNumOfItemSetsVal() {
		String text = "";
		if (cb.getSelectedItem().toString().equals(RMMCARD)) {
			text = numOfItemSetsFieldRMM.getText();
		} else if (cb.getSelectedItem().toString().equals(RELDIMSCARD)) {
			text = numOfItemSetsFieldRelDims.getText();
		}

		return validateInt(text, 0, "Number of itemsets");
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

	static int validateInt(final String text, final int min, String varName) {
		int val = -1;
		try {
			val = Integer.parseInt(text);
		} catch (NumberFormatException e) {
			val = -1;
		}
		if (val <= min) {
			JOptionPane.showMessageDialog(new JFrame(), varName
					+ " must be an integer larger than " + min + ".", "error",
					JOptionPane.ERROR_MESSAGE);
		}
		return val;
	}
}
