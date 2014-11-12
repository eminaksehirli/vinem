package cart.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import cart.gui.CartiCombineGUI.TableModel;

public class CartiCombinerFrame extends JFrame {
	static final class WheelListener implements MouseWheelListener {
		private int size;

		public WheelListener(int i) {
			size = i;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int change = -e.getWheelRotation() * size;
			final JSlider s = JSlider.class.cast(e.getSource());
			s.setValue(s.getValue() + change);
		}
	}

	private static final long serialVersionUID = 3728417676164781628L;

	// JSlider dimSlider;
	private JPanel cpane;
	JButton mineButton;
	JButton mine2Button;
	JSlider kSlider;
	JTextField fileText;
	JButton saveButton;
	JSlider orderSlider_1;
	JSlider orderSlider_2;
	JList<Integer> dimsList;

	JRadioButton selSelect;
	JRadioButton selAnd;
	JRadioButton selOr;
	JButton selClear;
	JTextField selectField;
	JButton manualSel;

	JToggleButton dimButton;
	JButton clusterButton;
	JToggleButton filterButton;
	JButton clearClustersButton;
	JButton selMineButton;
	JTable clusterTable;
	JTextField minlenField;

	private JDialog cframe;
	private JDialog clusterDialog;
	private JDialog infoDialog;
	private JPanel infoPane;
	private JLabel infoSizeLabel;
	private JPanel dimSupPane;
	private JTextArea infoObjects;

	public CartiCombinerFrame(int k, int numOfDims, int maxK) {
		super("Carti Combiner");
		setLayout(new BorderLayout());

		orderSlider_1 = new JSlider(0, numOfDims - 1);
		orderSlider_1.setValue(0);
		orderSlider_1.setPaintLabels(true);
		orderSlider_1.setPaintTicks(true);
		orderSlider_1.setMajorTickSpacing(1);
		orderSlider_1.addMouseWheelListener(new WheelListener(1));

		orderSlider_2 = new JSlider(0, numOfDims - 1);
		orderSlider_2.setValue(0);
		orderSlider_2.setPaintLabels(true);
		orderSlider_2.setPaintTicks(true);
		orderSlider_2.setMajorTickSpacing(1);
		orderSlider_2.addMouseWheelListener(new WheelListener(1));
		orderSlider_2.setEnabled(false);

		JPanel orderPane = new JPanel(new GridLayout(2, 1));
		orderPane.add(orderSlider_1);
		orderPane.add(orderSlider_2);

		kSlider = new JSlider(1, maxK);
		kSlider.setPaintLabels(true);
		kSlider.setPaintTicks(true);
		kSlider.setMajorTickSpacing(((maxK / 10) / 10) * 10);
		kSlider.setValue(k);
		// kSlider.setPreferredSize(new Dimension(270, 50));
		kSlider.addMouseWheelListener(new WheelListener(10));

		// JPanel kPane = new JPanel(new FlowLayout());
		// kPane.add(kSlider);

		cpane = new JPanel();
		cpane.setLayout(new GridLayout(5, 1));
		cframe = new JDialog(this);
		cframe.setTitle("Control Panel");
		cframe.add(cpane);

		cpane.add(orderPane);
		// cpane.add(kPane);
		cpane.add(kSlider);

		JPanel selectionPane = new JPanel(new FlowLayout());
		ButtonGroup selectButtons = new ButtonGroup();
		selSelect = new JRadioButton("Select", true);
		selAnd = new JRadioButton("And");
		selOr = new JRadioButton("Or");
		selectButtons.add(selSelect);
		selectButtons.add(selAnd);
		selectButtons.add(selOr);
		selectionPane.add(selSelect);
		selectionPane.add(selAnd);
		selectionPane.add(selOr);

		selClear = new JButton("Clear");
		selectionPane.add(selClear);

		selectField = new JTextField(30);
		selectField
				.setToolTipText("Put the id's of the items that you want to select, comma separated.");
		manualSel = new JButton("Select");
		selectionPane.add(selectField);
		selectionPane.add(manualSel);

		cpane.add(selectionPane);

		JPanel buttonPane = new JPanel(new FlowLayout());
		dimButton = new JToggleButton("Advanced");
		filterButton = new JToggleButton("Filter");
		clusterButton = new JButton("This is a Cluster!");
		mineButton = new JButton("Mine by Items");
		mine2Button = new JButton("MineBy Dims");
		clearClustersButton = new JButton("Clear Clusters");
		selMineButton = new JButton("Mine Selected");
		minlenField = new JTextField(5);
		buttonPane.add(dimButton);
		buttonPane.add(filterButton);
		buttonPane.add(clusterButton);
		buttonPane.add(mineButton);
		buttonPane.add(mine2Button);
		buttonPane.add(selMineButton);
		buttonPane.add(clearClustersButton);
		buttonPane.add(minlenField);

		cpane.add(buttonPane);

		JPanel savePane = new JPanel(new FlowLayout());
		fileText = new JTextField();
		saveButton = new JButton("Save");
		savePane.add(fileText);
		savePane.add(saveButton);

		cpane.add(savePane);

		Integer[] dims = new Integer[numOfDims];
		for (int i = 0; i < numOfDims; i++) {
			dims[i] = i;
		}
		dimsList = new JList<Integer>(dims);
		dimsList.setSelectedIndex(0);
		dimsList.setEnabled(false);
		add(dimsList, BorderLayout.EAST);
	}

	void showClusterTable(TableModel model) {
		clusterDialog = new JDialog(this);
		clusterTable = new JTable(model);
		clusterTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		clusterTable.getColumnModel().getColumn(1).setPreferredWidth(20);
		clusterTable.getColumnModel().getColumn(2).setPreferredWidth(80);
		clusterTable.getColumnModel().getColumn(3).setPreferredWidth(300);
		clusterDialog.add(new JScrollPane(clusterTable));
		clusterDialog.setSize(400, 400);
		clusterDialog.setVisible(true);
		cframe.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	void updateInfoPane(int size, int[] dimSupports, String objects) {
		if (infoDialog == null) {
			infoDialog = new JDialog(this);
			infoPane = new JPanel();
			BoxLayout l = new BoxLayout(infoPane, BoxLayout.PAGE_AXIS);
			infoPane.setLayout(l);

			JPanel sizePane = new JPanel(new FlowLayout());
			infoSizeLabel = new JLabel();
			sizePane.add(new JLabel("Size:"));
			sizePane.add(infoSizeLabel);

			dimSupPane = new JPanel(new GridLayout(dimSupports.length, 2));

			infoObjects = new JTextArea(4, 2);
			infoObjects.setLineWrap(true);
			infoObjects.setWrapStyleWord(true);

			infoPane.add(sizePane);
			infoPane.add(dimSupPane);
			infoPane.add(infoObjects);

			infoDialog.add(infoPane);

			infoDialog.setSize(100, 400);
			infoDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		}

		infoSizeLabel.setText(size + "");
		setDimSupports(dimSupports);
		infoObjects.setText(objects);
		infoDialog.setVisible(true);
	}

	private void setDimSupports(int[] dimSupports) {
		dimSupPane.removeAll();
		for (int i = 0; i < dimSupports.length; i++) {
			// JPanel aPane = new JPanel(new FlowLayout());
			// aPane.add(new JLabel("D" + i + ":"));
			dimSupPane.add(new JLabel("D_" + i + ":"));
			dimSupPane.add(new JLabel(dimSupports[i] + ""));
		}
	}

	void showTime() {
		setSize(1000, 1000);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		cframe.setSize(600, 400);
		cframe.setVisible(true);
		cframe.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	public void hideInfoPane() {
		if (infoDialog != null) {
			infoDialog.setVisible(false);
		}
	}
}
