package cart.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

public class CartiEpsFrame extends JFrame {
	private static final long serialVersionUID = 3728417676164781628L;

	JSlider dimSlider;
	private JPanel cpane;
	JButton mineButton;
	JButton lineButton;
	JButton tightButton;
	JSlider kSlider;
	JTextField fileText;
	JButton saveButton;

	public CartiEpsFrame(int eps, double[][] dims) {
		// public CartiEpsFrame(double eps, int numOfDims, int maxK) {
		setLayout(new BorderLayout());

		int numOfDims = dims.length;
		int numOfObjs = dims[0].length;
//		int maxV = 0;
//		for (double[] dim : dims) {
//			for (double v : dim) {
//				if (v > maxV) {
//					maxV = (int) v;
//				}
//			}
//		}

		dimSlider = new JSlider(0, numOfDims - 1);
		dimSlider.setPaintLabels(true);
		dimSlider.setPaintTicks(true);
		dimSlider.setMajorTickSpacing(1);
		dimSlider.addMouseWheelListener(new CartiCombinerFrame.WheelListener(1));
		kSlider = new JSlider(1, numOfObjs);
		kSlider.setPaintLabels(true);
		kSlider.setPaintTicks(true);
		kSlider.setMajorTickSpacing(50);
		kSlider.setValue(eps);
		kSlider.addMouseWheelListener(new CartiCombinerFrame.WheelListener(10));

		mineButton = new JButton("Mine");
		lineButton = new JButton("Line");
		tightButton = new JButton("Tighten");

		cpane = new JPanel();
		cpane.setLayout(new GridLayout(1, 3));
		add(cpane, BorderLayout.NORTH);

		cpane.add(dimSlider);
		cpane.add(kSlider);

//		JPanel buttonPane = new JPanel(new FlowLayout());
//		buttonPane.add(mineButton);
//		buttonPane.add(lineButton);
//		buttonPane.add(tightButton);
//
//		cpane.add(buttonPane);

		JPanel savePane = new JPanel(new FlowLayout());
		fileText = new JTextField();
		saveButton = new JButton("Save");
		savePane.add(fileText);
		savePane.add(saveButton);
		cpane.add(savePane);
	}

	void showTime() {
		setSize(1000, 1000);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
