package cart.gui2;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import cart.maximizer.Freq;
import cart.maximizer.ItemsetMaximalMinerSupLen;

public class CartiController {
	private CartiModel cartiModel;
	private CartiView cartiView;
	private ItemsetMaximalMinerSupLen maximer;

	public CartiController(CartiModel cartiModel, CartiView cartiView) {
		this.cartiModel = cartiModel;
		this.cartiView = cartiView;
	}

	public void run(String filePath) {
		int initOrderDim = 0;
		int initK = 1;
		cartiModel.init(filePath, initK, initOrderDim);

		int maxK = cartiModel.getNumObjects();
		Set<Integer> dims = cartiModel.getDims();
		int[][] matrixToShow = cartiModel.getMatrixToShow();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();

		cartiView.init(orderedObjs, dims, maxK, matrixToShow);
		cartiView.addButtonsListener(createButtonsListener());
		cartiView.addSelOptionsListListener(createSelOptionsListListener());
		cartiView.addCartiPanelListener(createCartiPanelListener());
		cartiView
				.addClusterTableModelListener(createClusterTableModelListener());
		cartiView.addSliderListener(createSliderListener());

		maximer = new ItemsetMaximalMinerSupLen(filePath);

		cartiView.getFrame().pack();
		cartiView.getFrame().setVisible(true);

	}

	public void orderSliderChanged() {
		cartiModel.setOrderDim(cartiView.getOrderSliderVal());

		int[][] matrixToShow = cartiModel.getMatrixToShow();
		Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
		Set<Integer> clusteredLocs = cartiModel.getClustersToShowLocs();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();

		cartiView.updateFigure(matrixToShow);
		cartiView.updateFigureSelected(selectedLocs);
		cartiView.updateFigureClustered(clusteredLocs);
		cartiView.updateSelOptions(orderedObjs, selecteds);
	}

	public void kSliderChanged() {
		cartiModel.setK(cartiView.getKSliderVal());

		int[][] matrixToShow = cartiModel.getMatrixToShow();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		double[] standardDevs = cartiModel.getStandardDeviations(selecteds);
		int[] measures = cartiModel.getMeasures(selecteds);
		int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);

		cartiView.updateFigure(matrixToShow);
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs,
				measures, medAbsDevs);
	}

	public void manSelectedsClear() {
		cartiModel.clearSelecteds();

		Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		double[] standardDevs = cartiModel.getStandardDeviations(selecteds);
		int[] measures = cartiModel.getMeasures(selecteds);
		int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);

		cartiView.updateFigureSelected(selectedLocs);
		cartiView.updateSelOptions(orderedObjs, selecteds);
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs,
				measures, medAbsDevs);
	}

	public void manSelectedsChange(Set<Integer> toSelect) {
		cartiModel.setSelecteds(toSelect);

		Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		double[] standardDevs = cartiModel.getStandardDeviations(selecteds);
		int[] measures = cartiModel.getMeasures(selecteds);
		int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);

		cartiView.updateFigureSelected(selectedLocs);
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs,
				measures, medAbsDevs);
	}

	public void figureSelectedsChange(Set<Integer> locsToSelect) {
		boolean select = cartiView.selModeIsSelect();
		boolean and = cartiView.selModeIsAnd();
		boolean or = cartiView.selModeIsOr();
		cartiModel.selectLocs(locsToSelect, select, and, or);

		Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		double[] standardDevs = cartiModel.getStandardDeviations(selecteds);
		int[] measures = cartiModel.getMeasures(selecteds);
		int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);

		cartiView.updateFigureSelected(selectedLocs);
		cartiView.updateSelOptions(orderedObjs, selecteds);
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs,
				measures, medAbsDevs);
	}

	public void manFilteredsClear() {
		cartiModel.clearFiltereds();

		int[][] matrixToShow = cartiModel.getMatrixToShow();
		Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
		Set<Integer> clusteredLocs = cartiModel.getClustersToShowLocs();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();

		cartiView.clearFigureSavedLocs();
		cartiView.updateFigure(matrixToShow);
		cartiView.updateFigureSelected(selectedLocs);
		cartiView.updateFigureClustered(clusteredLocs);
		cartiView.updateSelOptions(orderedObjs, selecteds);
	}

	public void filterSelecteds() {
		cartiModel.filterSelecteds();

		int[][] matrixToShow = cartiModel.getMatrixToShow();
		Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
		Set<Integer> clusteredLocs = cartiModel.getClustersToShowLocs();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		double[] standardDevs = cartiModel.getStandardDeviations(selecteds);
		int[] measures = cartiModel.getMeasures(selecteds);
		int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);
		Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
		Set<Integer> clustersToShow = cartiModel.getClustersToShow();

		cartiView.clearFigureSavedLocs();
		cartiView.updateFigure(matrixToShow);
		cartiView.updateFigureSelected(selectedLocs);
		cartiView.updateFigureClustered(clusteredLocs);
		cartiView.updateSelOptions(orderedObjs, selecteds);
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs,
				measures, medAbsDevs);
		cartiView.updateClusterInfo(clustersMap, clustersToShow);
	}

	public void undoFiltering() {
		if (cartiModel.canUndoFiltering()) {
			cartiModel.undoFiltering();

			int[][] matrixToShow = cartiModel.getMatrixToShow();
			Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
			Set<Integer> clusteredLocs = cartiModel.getClustersToShowLocs();
			List<Integer> orderedObjs = cartiModel.getOrderedObjList();
			Set<Integer> selecteds = cartiModel.getSelecteds();
			int[] dimSupports = cartiModel.getSupports(selecteds);
			double[] standardDevs = cartiModel.getStandardDeviations(selecteds);
			int[] measures = cartiModel.getMeasures(selecteds);
			int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);

			cartiView.clearFigureSavedLocs();
			cartiView.updateFigure(matrixToShow);
			cartiView.updateFigureSelected(selectedLocs);
			cartiView.updateFigureClustered(clusteredLocs);
			cartiView.updateSelOptions(orderedObjs, selecteds);
			cartiView.updateSelStats(selecteds, dimSupports, standardDevs,
					measures, medAbsDevs);
		}
	}

	public void clusterSelected() {
		cartiModel.clusterSelecteds();

		Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
		Set<Integer> clustersToShow = cartiModel.getClustersToShow();

		cartiView.updateClusterInfo(clustersMap, clustersToShow);
	}

	public void addSelectedToClusters(Set<Integer> clusterIds) {
		for (int id : clusterIds) {
			cartiModel.addSelectedsToCluster(id);
		}

		Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
		Set<Integer> clustersToShow = cartiModel.getClustersToShow();

		cartiView.updateClusterInfo(clustersMap, clustersToShow);

		// only need to update the figure for clustereds if one of the clusters
		// is visible
		for (int id : clusterIds) {
			if (cartiModel.clusterIsVisible(id)) {
				Set<Integer> clusteredLocs = cartiModel.getClustersToShowLocs();
				cartiView.updateFigureClustered(clusteredLocs);
				break;
			}
		}
	}

	public void removeSelectedFromClusters(Set<Integer> clusterIds) {
		for (int id : clusterIds) {
			cartiModel.removeSelectedsFromCluster(id);
		}

		Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
		Set<Integer> clustersToShow = cartiModel.getClustersToShow();

		cartiView.updateClusterInfo(clustersMap, clustersToShow);

		// only need to update the figure for clustereds if one of the clusters
		// is visible
		for (int id : clusterIds) {
			if (cartiModel.clusterIsVisible(id)) {
				Set<Integer> clusteredLocs = cartiModel.getClustersToShowLocs();
				cartiView.updateFigureClustered(clusteredLocs);
				break;
			}
		}
	}

	public void deleteClusters(Set<Integer> clusterIds) {
		boolean wasVisible = false;
		for (int id : clusterIds) {
			if (cartiModel.clusterIsVisible(id)) {
				wasVisible = true;
			}
			cartiModel.deleteCluster(id);
		}

		Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
		Set<Integer> clustersToShow = cartiModel.getClustersToShow();

		cartiView.updateClusterInfo(clustersMap, clustersToShow);

		// only need to update the figure for clustereds if one of the clusters
		// was visible
		if (wasVisible) {
			Set<Integer> clusteredLocs = cartiModel.getClustersToShowLocs();
			cartiView.updateFigureClustered(clusteredLocs);
		}
	}

	public void selectClusters(Set<Integer> clusterIds) {
		cartiModel.selectClusters(clusterIds);

		Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		double[] standardDevs = cartiModel.getStandardDeviations(selecteds);
		int[] measures = cartiModel.getMeasures(selecteds);
		int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);

		cartiView.updateFigureSelected(selectedLocs);
		cartiView.updateSelOptions(orderedObjs, selecteds);
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs,
				measures, medAbsDevs);
	}

	public void showCluster(int clusterId) {
		cartiModel.showCluster(clusterId);

		Set<Integer> clusteredLocs = cartiModel.getClustersToShowLocs();

		cartiView.updateFigureClustered(clusteredLocs);
	}

	public void hideCluster(int clusterId) {
		cartiModel.hideCluster(clusterId);

		Set<Integer> clusteredLocs = cartiModel.getClustersToShowLocs();

		cartiView.updateFigureClustered(clusteredLocs);
	}

	public void mine() {
		int minLen = cartiView.getMinLenVal();

		if (minLen < 0) {
			System.err.println("invalid minLen value");
			return;
		}

		List<Freq> result = maximer.mineFor(cartiModel.getK(), minLen);

		if (result.size() == 0) {
			System.out
					.println("No results for mining, try different k or minLen");
		}

		for (Freq freq : result) {
			Cluster cluster = new Cluster(new HashSet<Integer>(freq.freqSet),
					new HashSet<Integer>(freq.freqDims));

			cartiModel.addCluster(cluster);
		}

		Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
		Set<Integer> clustersToShow = cartiModel.getClustersToShow();

		cartiView.updateClusterInfo(clustersMap, clustersToShow);
	}

	// LISTENERS

	// listens to all the buttons
	private ActionListener createButtonsListener() {
		ActionListener listener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand() == SelOptions.CLEAR) {
					manSelectedsClear();
				} else if (e.getActionCommand() == FilterOptions.CLEAR) {
					manFilteredsClear();
				} else if (e.getActionCommand() == FilterOptions.UNDO) {
					undoFiltering();
				} else if (e.getActionCommand() == FilterOptions.FILTER) {
					filterSelecteds();
				} else if (e.getActionCommand() == CartiView.MINE) {
					mine();
				} else if (e.getActionCommand() == CartiView.CLUSTER) {
					clusterSelected();
				}

				// only get here if one of the ClusterInfo buttons was pressed
				Set<Integer> clusterIds = cartiView.getClusterInfo()
						.getSelectedRowsClusterIds();

				// if the user has not selected a cluster
				if (clusterIds.isEmpty()) {
					return;
				}

				if (e.getActionCommand() == ClusterInfo.ADD) {
					addSelectedToClusters(clusterIds);
				} else if (e.getActionCommand() == ClusterInfo.REMOVE) {
					removeSelectedFromClusters(clusterIds);
				} else if (e.getActionCommand() == ClusterInfo.DELETE) {
					deleteClusters(clusterIds);
				} else if (e.getActionCommand() == ClusterInfo.SELECT) {
					selectClusters(clusterIds);
				}
			}
		};

		return listener;
	}

	// listens for changes in selection in the SelOptions list
	private ListSelectionListener createSelOptionsListListener() {
		ListSelectionListener listener = new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false
						&& (cartiView.selOptionsListenerShouldListen())) {
					manSelectedsChange(cartiView.getSelectionOptions()
							.getSelecteds());
				}
			}
		};

		return listener;
	}

	// listens for mouse clicks in the cartiPanel
	private MouseListener createCartiPanelListener() {
		MouseListener listener = new MouseListener() {

			private int startX;

			public void mousePressed(MouseEvent e) {
				// only left click
				if (e.getButton() != MouseEvent.BUTTON1) {
					return;
				}

				startX = e.getX();
			}

			public void mouseReleased(MouseEvent e) {
				// only left click
				if (e.getButton() != MouseEvent.BUTTON1) {
					return;
				}

				int endX = e.getX();
				int x1 = min(startX, endX);
				int x2 = max(startX, endX);

				// get which cell the click started in and ended at, make sure
				// it is between 0 and cellCount
				int cellCount = cartiView.getCartiPanel().getCellCount();
				int[] cells = cartiView.getCartiPanel().getCells(x1, x2);
				cells[0] = max(0, cells[0]);
				cells[0] = min(cellCount, cells[0]);
				cells[1] = max(0, cells[1]);
				cells[1] = min(cellCount, cells[1]);

				Set<Integer> selectedLocs = new HashSet<>(cells[1] - cells[0]);
				for (int i = cells[0]; i < cells[1]; i++) {
					selectedLocs.add(i);
				}

				figureSelectedsChange(selectedLocs);
			}

			// do nothing for these
			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
			}
		};

		return listener;
	}

	// listens for changes in the ClusterInfo table (whether a cluster is
	// visible/not visible)
	private TableModelListener createClusterTableModelListener() {
		TableModelListener listener = new TableModelListener() {

			public void tableChanged(TableModelEvent e) {
				if (cartiView.clusterInfoListenerShouldListen()) {
					int row = e.getFirstRow();
					ClusterInfo.ClusterTable table = (ClusterInfo.ClusterTable) e
							.getSource();
					boolean isVisible = (boolean) table.getValueAt(row, 0);
					int clusterId = (int) table.getValueAt(row, 1);

					if (isVisible) {
						showCluster(clusterId);
					} else {
						hideCluster(clusterId);
					}
				}
			}
		};

		return listener;
	}

	// listens for changes in the sliders
	private ChangeListener createSliderListener() {
		ChangeListener listener = new ChangeListener() {

			private int previousVal = -1;

			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();

				// slider has stopped moving on a different value than before
				if ((!slider.getValueIsAdjusting())
						&& (slider.getValue() != previousVal)) {
					previousVal = slider.getValue();

					if (slider == cartiView.getKSlider()) {
						kSliderChanged();
					} else if (slider == cartiView.getOrderSlider()) {
						orderSliderChanged();
					}
				}
			}
		};

		return listener;
	}
}
