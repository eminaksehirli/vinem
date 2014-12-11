package vinem.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;

import vinem.controller.Neighborhood;
import vinem.gui.Cluster;
import vinem.model.Obj;
import cart.cartifier.Dissimilarity;

public class CartiView {

	public final static String SHOWDIST = "CartiView.ShowDist";
	public static final String CARTIFIER_CHANGE = "CartiView.CartifierChange";
	public static final String SAVE_MATRIX = "CartiView.SaveMatrix";

	private JFrame theFrame;
	private JDialog controlsDialog;

	private JSlider orderSlider;
	private JCheckBox syncOrderSlider;
	private JSlider kSlider;
	private JSlider epsSlider;
	private CartiPanel cartiPanel;
	private SelOptions selectionOptions;
	private FilterOptions filteringOptions;
	private MineOptions miningOptions;
	private DistOptions distanceOptions;
	private Stats selectedsStats;
	private JDialog selectedsStatsDialog;
	private ClusterInfo clusterInfo;
	private JDialog clusterInfoDialog;
	private JToggleButton showDistButton;
	private JToggleButton knnButton;
	private JToggleButton radiusButton;
	private JPanel sliderCards;

	// prevents the selection options list listener from listening while updating
	private boolean selOptionsListenerShouldListen;
	// prevents cluster infotable listener from listening while updating
	private boolean clusterInfoListenerShouldListen;
	// prevents the distance options box listener from listening while updating
	private boolean distOptionsListenerShouldListen;
	private boolean showingDist;
	private JPanel controlsPanel;
	private JPanel visualPanel;
	private JButton saveMatrixButton;

	public CartiView() {
		theFrame = new JFrame("Visual Interactive Neighborhood Miner");
		theFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		theFrame.setLayout(new BorderLayout(10, 10));
	}

	public void init(List<Obj> orderedObjs, Set<Integer> dims, int maxK,
			int[][] matrixToShow, List<Dissimilarity> distMeasures, int maxEps) {
		visualPanel = createVerticalBoxPanel(700, 700);
		controlsPanel = createHorizontalBoxPanel(600, 650);
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
		selectionOptions = new SelOptions(orderedObjs);

		controlsPanelLeft.add(selectionOptions.getPanel());
		controlsPanelLeft.add(Box.createRigidArea(new Dimension(0, 10)));

		// add the filtering options panel
		filteringOptions = new FilterOptions();

		controlsPanelLeft.add(filteringOptions.getPanel());
		controlsPanelLeft.add(Box.createRigidArea(new Dimension(0, 10)));

		JPanel neighborhoodPane = createNeighborhoodPane(maxK, maxEps);

		neighborhoodPane.add(sliderCards);
		// controlsPanelLeft.add(Box.createRigidArea(new Dimension(0, 20)));
		controlsPanelLeft.add(neighborhoodPane);
		controlsPanelLeft.add(Box.createRigidArea(new Dimension(0, 10)));

		JPanel orderPane = createVerticalBoxPanel(300, 120);
		orderPane.setBorder(BorderFactory.createTitledBorder("Dimension Order"));
		// add slider for order_1
		// JLabel orderSliderLabel = new JLabel("order");
		// orderSliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		// orderPane.add(orderSliderLabel);
		orderSlider = createSlider(0, dims.size() - 1);
		orderSlider.setMinorTickSpacing(1);
		orderPane.add(orderSlider);

		// add checkbox for syncing order_1 with distanceOptions
		syncOrderSlider = new JCheckBox("Sync with distance measure", true);
		syncOrderSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
		orderPane.add(syncOrderSlider);
		orderPane.add(Box.createRigidArea(new Dimension(0, 10)));
		controlsPanelLeft.add(orderPane);

		// CONTROLS PANEL RIGHT
		// add the distance measure options panel
		distanceOptions = new DistOptions(dims, distMeasures);

		controlsPanelRight.add(distanceOptions.getPanel());
		controlsPanelRight.add(Box.createRigidArea(new Dimension(0, 10)));

		// add the mining options panel
		miningOptions = new MineOptions();

		controlsPanelRight.add(miningOptions.getPanel());
		controlsPanelRight.add(Box.createRigidArea(new Dimension(0, 10)));

		showDistButton = new JToggleButton("Show Distribution");
		showDistButton.setActionCommand(SHOWDIST);

		saveMatrixButton = new JButton("Save Matrix");
		saveMatrixButton.setActionCommand(SAVE_MATRIX);

		JPanel miscButtons = createVerticalBoxPanel(150, 100);
		miscButtons.setBorder(BorderFactory.createTitledBorder("Tools"));

		miscButtons.add(showDistButton);
		miscButtons.add(saveMatrixButton);
		miscButtons.setAlignmentX(Component.CENTER_ALIGNMENT);

		controlsPanelRight.add(miscButtons);
		controlsPanelRight.add(Box.createRigidArea(new Dimension(0, 10)));

		// add left and right controls panels to main controls panel
		controlsPanelLeft.setAlignmentY(Component.TOP_ALIGNMENT);
		controlsPanel.add(controlsPanelLeft);

		controlsPanelRight.setAlignmentY(Component.TOP_ALIGNMENT);
		controlsPanel.add(controlsPanelRight);

		// add visual and controls panel to the frame
		theFrame.add(visualPanel, BorderLayout.CENTER);

		controlsDialog = new JDialog(theFrame, "Controls");
		controlsDialog.add(controlsPanel);
		controlsDialog.pack();
		controlsDialog.setVisible(true);

		// DIALOGS
		// initialise selecteds stats dialog
		selectedsStats = new Stats(dims);
		selectedsStatsDialog = new JDialog(theFrame, "Selecteds stats");
		selectedsStatsDialog.add(selectedsStats.getStatsPanel());

		// initialise cluster info dialog
		clusterInfo = new ClusterInfo();
		clusterInfoDialog = new JDialog(theFrame, "Clusters info");
		clusterInfoDialog.add(clusterInfo.getInfoPanel());

		// make sure all listeners are listening
		selOptionsListenerShouldListen = true;
		clusterInfoListenerShouldListen = true;
		distOptionsListenerShouldListen = true;
	}

	private JPanel createNeighborhoodPane(int maxK, int maxEps) {
		JPanel neighborhoodPane = createVerticalBoxPanel(300, 150);
		neighborhoodPane
				.setBorder(BorderFactory.createTitledBorder("Neighborhood"));
		JPanel sliderSelectors = new JPanel();
		ButtonGroup bg = new ButtonGroup();
		knnButton = new JToggleButton("kNN", null, true);
		radiusButton = new JToggleButton("radius", null, false);
		knnButton.setActionCommand(CARTIFIER_CHANGE);
		radiusButton.setActionCommand(CARTIFIER_CHANGE);
		bg.add(knnButton);
		bg.add(radiusButton);
		sliderSelectors.add(knnButton);
		sliderSelectors.add(radiusButton);
		neighborhoodPane.add(sliderSelectors);
		final ActionListener l = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeCartifier();
			}
		};
		knnButton.addActionListener(l);
		radiusButton.addActionListener(l);

		sliderCards = new JPanel(new CardLayout());
		JPanel knnCard = createKNNCard(maxK);
		JPanel radiusCard = createRadiusCard(maxEps);

		sliderCards.add(knnCard, Neighborhood.KNN.name);
		sliderCards.add(radiusCard, Neighborhood.Radius.name);
		return neighborhoodPane;
	}

	protected void changeCartifier() {
		Neighborhood cardToShow = selectedNeighborhood();
		CardLayout cl = (CardLayout) (sliderCards.getLayout());
		cl.show(sliderCards, cardToShow.name);
	}

	public Neighborhood selectedNeighborhood() {
		if (knnButton.isSelected()) {
			return Neighborhood.KNN;
		}
		return Neighborhood.Radius;
	}

	protected JPanel createKNNCard(int maxK) {
		JPanel knnCard = createVerticalBoxPanel(300, 100);
		JLabel kSliderLabel = new JLabel("k");
		kSliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		knnCard.add(kSliderLabel);
		kSlider = createSlider(1, maxK);
		knnCard.add(kSlider);
		return knnCard;
	}

	protected JPanel createRadiusCard(int maxEps) {
		JPanel knnCard = createVerticalBoxPanel(300, 100);
		JLabel label = new JLabel("eps");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		knnCard.add(label);
		epsSlider = createSlider(1, maxEps);
		knnCard.add(epsSlider);
		return knnCard;
	}

	public void addButtonsListener(ActionListener buttonsListener) {
		showDistButton.addActionListener(buttonsListener);
		saveMatrixButton.addActionListener(buttonsListener);
		selectionOptions.addButtonsListener(buttonsListener);
		filteringOptions.addButtonsListener(buttonsListener);
		clusterInfo.addButtonsListener(buttonsListener);
		distanceOptions.addButtonsListener(buttonsListener);
		miningOptions.addButtonsListener(buttonsListener);
		knnButton.addActionListener(buttonsListener);
		radiusButton.addActionListener(buttonsListener);
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

	public void addDistOptionsBoxListener(ActionListener distOptionsBoxListener) {
		distanceOptions.addBoxListener(distOptionsBoxListener);
	}

	/**
	 * @param prefWidth
	 * @param prefHeight
	 * @return A panel with vertical box layout and given pref width/height.
	 */
	public static JPanel createVerticalBoxPanel(int prefWidth, int prefHeight) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		p.setPreferredSize(new Dimension(prefWidth, prefHeight));
		return p;
	}

	/**
	 * @param prefWidth
	 * @param prefHeight
	 * @return A panel with horizontal box layout and given pref width/height.
	 */
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

	public void updateSelOptions(List<Obj> orderedObjs, Set<Integer> selecteds) {
		selOptionsListenerShouldListen = false;
		selectionOptions.updateSelected(orderedObjs, selecteds);
		selOptionsListenerShouldListen = true;
	}

	public void updateSelStats(Collection<Obj> selecteds, int[] dimSupports,
			double[] standardDevs, int[] medAbsDevs) {
		if (selecteds.size() == 0) {
			selectedsStatsDialog.setVisible(false);
		} else {
			selectedsStats.updateStats(selecteds, dimSupports, standardDevs,
					medAbsDevs);
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

	/**
	 * Updates the orderSlider to be equal to a given order. A changeEvent is
	 * fired even when the order does not actually change. This is necessary to
	 * allow the syncing of orderSlider to the Selected Dist Measure
	 * 
	 * @param order
	 */
	public void updateOrderSlider(int order) {
		if (orderSlider.getValue() != order) {
			orderSlider.setValue(order);
		} else {
			for (ChangeListener cl : orderSlider.getChangeListeners()) {
				cl.stateChanged(new ChangeEvent(orderSlider));
			}
		}
	}

	public void updateSelectedDistMeasureId(int id) {
		distOptionsListenerShouldListen = false;
		distanceOptions.setSelectedMeasureId(id);
		distOptionsListenerShouldListen = true;
	}

	public void addDistMeasure(String distMeasure) {
		distanceOptions.addDistMeasure(distMeasure);
	}

	public void showInfoMessage(String message, String title) {
		JOptionPane.showMessageDialog(theFrame, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	public void showRelatedDims(int[][] relatedDimsMatrix) {
		RelatedDims relatedDims = new RelatedDims(relatedDimsMatrix);

		JDialog relatedDimsDialog = new JDialog(theFrame, "Related dims");
		relatedDimsDialog.add(relatedDims.getRelatedDimsPanel());
		relatedDimsDialog.pack();
		relatedDimsDialog.setVisible(true);
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

	public int getEpsSliderVal() {
		return epsSlider.getValue();
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

	public JPanel getControlsPanel() {
		return controlsPanel;
	}

	public boolean clusterInfoListenerShouldListen() {
		return clusterInfoListenerShouldListen;
	}

	public JSlider getKSlider() {
		return kSlider;
	}

	public JSlider getEpsSlider() {
		return epsSlider;
	}

	public JSlider getOrderSlider() {
		return orderSlider;
	}

	public boolean shouldSyncOrderSlider() {
		return syncOrderSlider.isSelected();
	}

	public DistOptions getDistanceOptions() {
		return distanceOptions;
	}

	public MineOptions getMiningOptions() {
		return miningOptions;
	}

	public NoiseOptions getNoiseOptions() {
		return getMiningOptions().noiseOpts;
	}

	public boolean distOptionsListenerShouldListen() {
		return distOptionsListenerShouldListen;
	}

	public void updateDistribution(int[] starts, boolean reset) {
		showingDist = !reset && showingDist;
		if (!showingDist && showDistButton.isSelected()) {
			cartiPanel.showDistribution(starts);
			showingDist = true;
		} else if (showingDist) {
			cartiPanel.hideDistribution(starts);
			showingDist = false;
		}
		theFrame.validate();
		theFrame.repaint();
	}

	static JPanel createPanelWithLabel(String labelString) {
		JPanel panel = createHorizontalBoxPanel(300, 40);
		JLabel label = new JLabel(labelString + ": ");
		panel.add(label);

		panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		return panel;
	}

	private final static class SliderWheelListener implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int change = -e.getWheelRotation();
			final JSlider s = JSlider.class.cast(e.getSource());
			s.setValue(s.getValue() + change);
		}
	}

	// creates a slider with given minimum/maximum values
	private static JSlider createSlider(int min, int max) {
		JSlider slider = new JSlider(min, max) {
			private static final long serialVersionUID = -6440732335999151699L;

			@Override
			public Point getToolTipLocation(MouseEvent event) {
				return new Point(event.getX() + 15, event.getY());
			}
		};
		slider.setValue(min);
		// slider.setMajorTickSpacing(((max / 10) / 10) * 10);
		slider.setMajorTickSpacing(max / 5);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setToolTipText(Integer.toString(slider.getValue()));
		slider.addMouseWheelListener(new SliderWheelListener());
		final ChangeListener toolTipSetter = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();
				if (!slider.getValueIsAdjusting()) {
					slider.setToolTipText(Integer.toString(slider.getValue()));
				}
			}
		};
		slider.addChangeListener(toolTipSetter);

		return slider;
	}
}
