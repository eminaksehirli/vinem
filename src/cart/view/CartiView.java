package cart.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;

import cart.gui2.Cluster;

public class CartiView {

	public final static String CLUSTER = "CartiView.Cluster";
	public final static String MINE = "CartiView.Mine";

	private JFrame theFrame;

	private JSlider orderSlider;
	private JSlider kSlider;
	private JButton clusterButton;
	private JButton mineButton;
	private JTextField minLenField;
	private CartiPanel cartiPanel;
	private SelOptions selectionOptions;
	private FilterOptions filteringOptions;
	private DistOptions distanceOptions;
	private Stats selectedsStats;
	private JDialog selectedsStatsDialog;
	private ClusterInfo clusterInfo;
	private JDialog clusterInfoDialog;

	private boolean selOptionsListenerShouldListen; // prevents the selection
													// options list listener
													// from listening while
													// updating
	private boolean clusterInfoListenerShouldListen; // prevents cluster info
														// table listener from
														// listening while
														// updating

	public CartiView() {
		theFrame = new JFrame("Carti");
		theFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		theFrame.setLayout(new BorderLayout(10, 10));
	}

	public void init(List<Integer> orderedObjs, Set<Integer> dims, int maxK,
			int[][] matrixToShow, List<String> distMeasures) {
		// visualPanel contains the visual representation
		JPanel visualPanel = createVerticalBoxPanel(700, 700);
		// controlsPanel contains the buttons/sliders/...
		JPanel controlsPanel = createHorizontalBoxPanel(600, 700);
		JPanel controlsPanelLeft = createVerticalBoxPanel(300, 700);
		JPanel controlsPanelRight = createVerticalBoxPanel(300, 700);

		// VISUAL GOES HERE
		// add cartiPanel
		cartiPanel = new CartiPanel(matrixToShow);
		JScrollPane sPane = new JScrollPane(cartiPanel);
		sPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		visualPanel.add(sPane);

		// CONTROLS GO HERE
		// CONTROLS PANEL LEFT
		// add the selection options panel
		selectionOptions = new SelOptions();
		selectionOptions.init(orderedObjs);

		controlsPanelLeft.add(selectionOptions.getPanel());
		controlsPanelLeft.add(Box.createRigidArea(new Dimension(0, 10)));

		// add the filtering options panel
		filteringOptions = new FilterOptions();
		filteringOptions.init();

		controlsPanelLeft.add(filteringOptions.getPanel());
		controlsPanelLeft.add(Box.createRigidArea(new Dimension(0, 10)));

		// add button for clustering
		clusterButton = new JButton("Cluster selected");
		clusterButton.setActionCommand(CLUSTER);
		clusterButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		controlsPanelLeft.add(clusterButton);
		controlsPanelLeft.add(Box.createRigidArea(new Dimension(0, 10)));

		// mining panel
		JPanel minePanel = createHorizontalBoxPanel(150, 50);
		minePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		minePanel.setBorder(BorderFactory.createTitledBorder("Mining"));

		// add button for mining
		mineButton = new JButton("Mine");
		mineButton.setActionCommand(MINE);

		minePanel.add(mineButton);
		minePanel.add(Box.createRigidArea(new Dimension(10, 0)));

		// add minLen textfield
		JLabel minLenLabel = new JLabel("minLen: ");
		minePanel.add(minLenLabel);
		minLenField = new JTextField(20);
		minLenField.setMaximumSize(new Dimension(50, 25));
		minePanel.add(minLenField);

		controlsPanelLeft.add(minePanel);
		controlsPanelLeft.add(Box.createRigidArea(new Dimension(0, 10)));

		// add slider for k
		JLabel kSliderLabel = new JLabel("k");
		kSliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		controlsPanelLeft.add(kSliderLabel);
		kSlider = createSlider(1, maxK);
		controlsPanelLeft.add(kSlider);
		controlsPanelLeft.add(Box.createRigidArea(new Dimension(0, 20)));

		// add slider for order_1
		JLabel order_1SliderLabel = new JLabel("order_1");
		order_1SliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		controlsPanelLeft.add(order_1SliderLabel);
		orderSlider = createSlider(0, dims.size() - 1);
		controlsPanelLeft.add(orderSlider);
		controlsPanelLeft.add(Box.createRigidArea(new Dimension(0, 20)));

		// CONTROLS PANEL RIGHT
		// add the distance measure options panel
		distanceOptions = new DistOptions();
		distanceOptions.init(dims, distMeasures);

		controlsPanelRight.add(distanceOptions.getPanel());
		controlsPanelRight.add(Box.createRigidArea(new Dimension(0, 20)));

		// add left and right controls panels to main controls panel
		controlsPanel.add(controlsPanelLeft);
		controlsPanelLeft.setAlignmentY(Component.TOP_ALIGNMENT);

		controlsPanel.add(controlsPanelRight);
		controlsPanelRight.setAlignmentY(Component.TOP_ALIGNMENT);

		// add visual and controls panel to the frame
		theFrame.add(visualPanel, BorderLayout.CENTER);
		theFrame.add(controlsPanel, BorderLayout.LINE_END);

		// DIALOGS
		// initialise selecteds stats dialog
		selectedsStats = new Stats();
		selectedsStats.init(dims);
		selectedsStatsDialog = new JDialog(theFrame, "Selecteds stats");
		selectedsStatsDialog.add(selectedsStats.getStatsPanel());

		// initialise cluster info dialog
		clusterInfo = new ClusterInfo();
		clusterInfo.init();
		clusterInfoDialog = new JDialog(theFrame, "Clusters info");
		clusterInfoDialog.add(clusterInfo.getInfoPanel());

		// make sure the listSelectionListener is listening
		selOptionsListenerShouldListen = true;
		clusterInfoListenerShouldListen = true;
	}

	public void addButtonsListener(ActionListener buttonsListener) {
		clusterButton.addActionListener(buttonsListener);
		mineButton.addActionListener(buttonsListener);
		selectionOptions.addButtonsListener(buttonsListener);
		filteringOptions.addButtonsListener(buttonsListener);
		clusterInfo.addButtonsListener(buttonsListener);
		distanceOptions.addButtonsListener(buttonsListener);
	}

	public void addSelOptionsListListener(
			ListSelectionListener selOptionsListListener) {
		selectionOptions.addListSelectionListener(selOptionsListListener);
	}

	public void addCartiPanelListener(MouseListener cartiPanelListener) {
		cartiPanel.addMouseListener(cartiPanelListener);
	}

	public void addClusterTableModelListener(
			TableModelListener clusterTableModelListener) {
		clusterInfo.addTableModelListener(clusterTableModelListener);
	}

	public void addSliderListener(ChangeListener sliderListener) {
		kSlider.addChangeListener(sliderListener);
		orderSlider.addChangeListener(sliderListener);
	}

	public void addDistOptionsBoxListener(ActionListener distOptionsBoxListener) {
		distanceOptions.addBoxListener(distOptionsBoxListener);
	}

	// creates a slider with given minimum/maximum values
	private JSlider createSlider(int min, int max) {
		JSlider slider = new JSlider(min, max);
		slider.setValue(min);
		// slider.setMajorTickSpacing(((max / 10) / 10) * 10);
		slider.setMajorTickSpacing(max / 5);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		return slider;
	}

	public static JPanel createVerticalBoxPanel(int prefWidth, int prefHeight) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		p.setPreferredSize(new Dimension(prefWidth, prefHeight));
		return p;
	}

	public static JPanel createHorizontalBoxPanel(int prefWidth, int prefHeight) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		p.setPreferredSize(new Dimension(prefWidth, prefHeight));
		return p;
	}

	public void updateFigure(int[][] matrixToShow) {
		cartiPanel.updateMatrix(matrixToShow);
		theFrame.validate();
		theFrame.repaint();
	}

	// this just clears the saved locations, it does not actually decolour them
	// in the figure
	// this is used to counter a bug when calling updateFigure after a filtering
	public void clearFigureSavedLocs() {
		cartiPanel.clearSavedLocs();
	}

	public void updateFigureSelected(Set<Integer> selectedLocs) {
		cartiPanel.updateSelected(selectedLocs);
		theFrame.validate();
		theFrame.repaint();
	}

	public void updateFigureClustered(Set<Integer> clusteredLocs) {
		cartiPanel.updateClustered(clusteredLocs);
		theFrame.validate();
		theFrame.repaint();
	}

	public void updateSelOptions(List<Integer> orderedObjs,
			Set<Integer> selecteds) {
		selOptionsListenerShouldListen = false;
		selectionOptions.updateSelected(orderedObjs, selecteds);
		selOptionsListenerShouldListen = true;
	}

	public void updateSelStats(Set<Integer> selecteds, int[] dimSupports,
			double[] standardDevs, int[] measures, int[] medAbsDevs) {
		if (selecteds.size() == 0) {
			selectedsStatsDialog.setVisible(false);
		} else {
			selectedsStats.updateStats(selecteds, dimSupports, standardDevs,
					measures, medAbsDevs);
			selectedsStatsDialog.pack();
			selectedsStatsDialog.setVisible(true);
		}
	}

	public void updateClusterInfo(Map<Integer, Cluster> clustersMap,
			Set<Integer> clustersToShow) {
		clusterInfoListenerShouldListen = false;
		if (clustersMap.size() == 0) {
			clusterInfoDialog.setVisible(false);
		} else {
			clusterInfo.updateClusterInfo(clustersMap, clustersToShow);
			clusterInfoDialog.pack();
			clusterInfoDialog.setVisible(true);
		}
		clusterInfoListenerShouldListen = true;
	}

	public void addDistMeasure(String distMeasure) {
		distanceOptions.addDistMeasure(distMeasure);
	}

	public JFrame getFrame() {
		return theFrame;
	}

	public int getOrderSliderVal() {
		return orderSlider.getValue();
	}

	public int getKSliderVal() {
		return kSlider.getValue();
	}

	public boolean selModeIsSelect() {
		return selectionOptions.selModeIsSelect();
	}

	public boolean selModeIsAnd() {
		return selectionOptions.selModeIsAnd();
	}

	public boolean selModeIsOr() {
		return selectionOptions.selModeIsOr();
	}

	// returns value of the minLen text field, returns -1 if the text field does
	// not contain a valid integer
	public int getMinLenVal() {
		try {
			return Integer.parseInt(minLenField.getText());
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public ClusterInfo getClusterInfo() {
		return clusterInfo;
	}

	public boolean selOptionsListenerShouldListen() {
		return selOptionsListenerShouldListen;
	}

	public SelOptions getSelectionOptions() {
		return selectionOptions;
	}

	public CartiPanel getCartiPanel() {
		return cartiPanel;
	}

	public boolean clusterInfoListenerShouldListen() {
		return clusterInfoListenerShouldListen;
	}

	public JSlider getKSlider() {
		return kSlider;
	}

	public JSlider getOrderSlider() {
		return orderSlider;
	}

	public DistOptions getDistanceOptions() {
		return distanceOptions;
	}
}
