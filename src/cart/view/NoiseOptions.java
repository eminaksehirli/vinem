package cart.view;

import static java.awt.Component.CENTER_ALIGNMENT;

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
	public final static String EACHMEAS = "NoiseOptions.eachMeas";

	private JPanel noisePanel;
	private JButton allMeas;
	private JButton selMeas;
	private JButton eachMeas;
	private JTextField minSupField;

	public NoiseOptions() {
		// the main panel
		noisePanel = CartiView.createVerticalBoxPanel(350, 150);
		// add minSup textfield
		JPanel minSupPanel = CartiView.createHorizontalBoxPanel(300, 40);
		minSupPanel.add(new JLabel("minSup: "));
		minSupPanel.setAlignmentX(CENTER_ALIGNMENT);

		minSupField = new JTextField();
		minSupField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		minSupPanel.add(minSupField);
		minSupPanel.setMaximumSize(new Dimension(300, 40));

		noisePanel.add(minSupPanel);

		noisePanel.add(Box.createRigidArea(new Dimension(0, 5)));

		// add allMeas/selMeas buttons
		JPanel btPane = CartiView.createVerticalBoxPanel(300, 100);
		btPane.add(new JLabel("Find the outliers..."));
		selMeas = new JButton("in the selected measure");
		selMeas.setActionCommand(SELMEAS);
		eachMeas = new JButton("in each measure");
		eachMeas.setActionCommand(EACHMEAS);
		allMeas = new JButton("globally");
		allMeas.setActionCommand(ALLMEAS);
		btPane.add(selMeas);
		btPane.add(eachMeas);
		btPane.add(allMeas);

		noisePanel.add(btPane);
	}

	public void addButtonsListener(ActionListener buttonsListener) {
		allMeas.addActionListener(buttonsListener);
		selMeas.addActionListener(buttonsListener);
		eachMeas.addActionListener(buttonsListener);
	}

	public JPanel getPanel() {
		return noisePanel;
	}

	/**
	 * @return Value of the minSup text field, returns -1 if the text field does
	 *         not contain an integer larger than 0
	 */
	public int getMinSupVal() {
		return MineOptions.validateInt(minSupField.getText(), 0, "Minimum support");
	}

	public void setMinsupVal(String minSup) {
		minSupField.setText(minSup);
	}
}
