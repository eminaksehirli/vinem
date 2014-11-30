package cart.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NoiseOptions {

	public final static String ALLMEAS = "NoiseOptions.allMeas";
	public final static String SELMEAS = "NoiseOptions.selMeas";

	private JPanel noisePanel;
	private JButton allMeas;
	private JButton selMeas;
	private JTextField minSupField;

	public NoiseOptions() {
		// the main panel
		noisePanel = CartiView.createVerticalBoxPanel(300, 150);

		// add allMeas/selMeas buttons
		allMeas = new JButton("Get noise over all measures");
		allMeas.setActionCommand(ALLMEAS);
		allMeas.setAlignmentX(Component.CENTER_ALIGNMENT);

		selMeas = new JButton("Get noise for selected measure");
		selMeas.setActionCommand(SELMEAS);
		selMeas.setAlignmentX(Component.CENTER_ALIGNMENT);

		noisePanel.add(allMeas);
		noisePanel.add(Box.createRigidArea(new Dimension(0, 5)));
		noisePanel.add(selMeas);
		noisePanel.add(Box.createRigidArea(new Dimension(0, 5)));

		// add minSup textfield
		JPanel minSupPanel = CartiView.createHorizontalBoxPanel(300, 40);
		minSupPanel.add(new JLabel("minSup: "));
		minSupPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		minSupField = new JTextField();
		minSupField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		minSupPanel.add(minSupField);
		minSupPanel.setMaximumSize(new Dimension(300, 40));

		noisePanel.add(minSupPanel);
	}

	public void addButtonsListener(ActionListener buttonsListener) {
		allMeas.addActionListener(buttonsListener);
		selMeas.addActionListener(buttonsListener);
	}

	public JPanel getPanel() {
		return noisePanel;
	}

	/**
	 * @return Value of the minSup text field, returns -1 if the text field does
	 *         not contain an integer larger than 0
	 */
	public int getMinSupVal() {
		int minSup = -1;
		try {
			minSup = Integer.parseInt(minSupField.getText());
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
}
