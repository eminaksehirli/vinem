package cart.controller;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
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

import mime.plain.PlainItem;
import mime.plain.PlainItemDB;
import mime.plain.PlainItemSet;
import cart.gui2.Cluster;
import cart.gui2.CosineDistMeasure;
import cart.gui2.DistMeasure;
import cart.gui2.EuclidianDistMeasure;
import cart.model.CartiModel;
import cart.model.RandomMaximalMiner;
import cart.view.CartiView;
import cart.view.ClusterInfo;
import cart.view.DistOptions;
import cart.view.FilterOptions;
import cart.view.MineOptions;
import cart.view.NoiseOptions;
import cart.view.SelOptions;

/**
 * @author Detlev, Aksehirli
 * 
 */
public class CartiController {
	private CartiModel cartiModel;
	private CartiView cartiView;

	public CartiController(CartiModel cartiModel, CartiView cartiView) {
		this.cartiModel = cartiModel;
		this.cartiView = cartiView;
	}

	public void run() {
		cartiModel.init();

		int maxK = cartiModel.getNumObjects();
		Set<Integer> dims = cartiModel.getDims();
		int[][] matrixToShow = cartiModel.getMatrixToShow();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		List<DistMeasure> distMeasures = cartiModel.getDistMeasures();
		List<String> distMeasuresStringList = new ArrayList<String>();

		for (DistMeasure distMeasure : distMeasures) {
			distMeasuresStringList.add(distMeasure.toString());
		}

		cartiView.init(orderedObjs, dims, maxK, matrixToShow,
				distMeasuresStringList);
		cartiView.addButtonsListener(createButtonsListener());
		cartiView.addSelOptionsListListener(createSelOptionsListListener());
		cartiView.addCartiPanelListener(createCartiPanelListener());
		cartiView.addClusterTableModelListener(createClusterTableModelListener());
		cartiView.addSliderListener(createSliderListener());
		cartiView.addDistOptionsBoxListener(createDistOptionsBoxListener());

		cartiView.getFrame().pack();
		cartiView.getFrame().setVisible(true);
	}

	public void orderSliderChanged() {
		cartiModel.setOrderDim(cartiView.getOrderSliderVal());

		if (cartiView.shouldSyncOrderSlider()) {
			// need to update the selected distance measure id
			cartiModel.setSelectedDistMeasureId(cartiView.getOrderSliderVal());
			cartiView.updateSelectedDistMeasureId(cartiView.getOrderSliderVal());
		}

		updateAfterOrderChange();
	}

	public void orderByObject(int objIx) {
		cartiModel.setOrderByObj(objIx);
		updateAfterOrderChange();
	}

	public void updateAfterOrderChange() {
		int[][] matrixToShow = cartiModel.getMatrixToShow();
		Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
		Set<Integer> clusteredLocs = cartiModel.getClustersToShowLocs();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();

		cartiView.updateFigure(matrixToShow);
		cartiView.updateFigureSelected(selectedLocs);
		cartiView.updateFigureClustered(clusteredLocs);
		cartiView.updateSelOptions(orderedObjs, selecteds);
		updateDistribution(true);
	}

	public void kSliderChanged() {
		int k = cartiView.getKSliderVal();
		cartiModel.setK(k);

		int[][] matrixToShow = cartiModel.getMatrixToShow();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		double[] standardDevs = cartiModel.getStandardDeviations(selecteds);
		int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);

		cartiView.updateFigure(matrixToShow);
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs, medAbsDevs);
		cartiView.getMiningOptions().setMinSupVal((int) (k * 0.75));
		updateDistribution(true);
	}

	public void manSelectedsClear() {
		cartiModel.clearSelecteds();

		Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		double[] standardDevs = cartiModel.getStandardDeviations(selecteds);
		int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);

		cartiView.updateFigureSelected(selectedLocs);
		cartiView.updateSelOptions(orderedObjs, selecteds);
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs, medAbsDevs);
	}

	public void manSelectedsChange(Set<Integer> toSelect) {
		cartiModel.setSelecteds(toSelect);

		Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		double[] standardDevs = cartiModel.getStandardDeviations(selecteds);
		int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);

		cartiView.updateFigureSelected(selectedLocs);
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs, medAbsDevs);
	}

	public void figureSelectedsChange(Set<Integer> locsToSelect) {
		boolean select = cartiView.getSelectionOptions().selModeIsSelect();
		boolean and = cartiView.getSelectionOptions().selModeIsAnd();
		boolean or = cartiView.getSelectionOptions().selModeIsOr();
		cartiModel.selectLocs(locsToSelect, select, and, or);

		Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		double[] standardDevs = cartiModel.getStandardDeviations(selecteds);
		int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);

		cartiView.updateFigureSelected(selectedLocs);
		cartiView.updateSelOptions(orderedObjs, selecteds);
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs, medAbsDevs);
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

	/**
	 * Filters either every selected object, or every non-selected object.
	 * 
	 * @param filterOutSelected
	 */
	public void filter(boolean filterOutSelected) {
		if (filterOutSelected) {
			cartiModel.filterSelecteds();
		} else {
			cartiModel.filterNotSelecteds();
		}

		int[][] matrixToShow = cartiModel.getMatrixToShow();
		Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
		Set<Integer> clusteredLocs = cartiModel.getClustersToShowLocs();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		double[] standardDevs = cartiModel.getStandardDeviations(selecteds);
		int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);
		Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
		Set<Integer> clustersToShow = cartiModel.getClustersToShow();

		cartiView.clearFigureSavedLocs();
		cartiView.updateFigure(matrixToShow);
		cartiView.updateFigureSelected(selectedLocs);
		cartiView.updateFigureClustered(clusteredLocs);
		cartiView.updateSelOptions(orderedObjs, selecteds);
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs, medAbsDevs);
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
			int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);
			Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
			Set<Integer> clustersToShow = cartiModel.getClustersToShow();

			cartiView.clearFigureSavedLocs();
			cartiView.updateFigure(matrixToShow);
			cartiView.updateFigureSelected(selectedLocs);
			cartiView.updateFigureClustered(clusteredLocs);
			cartiView.updateSelOptions(orderedObjs, selecteds);
			cartiView
					.updateSelStats(selecteds, dimSupports, standardDevs, medAbsDevs);
			cartiView.updateClusterInfo(clustersMap, clustersToShow);
		}
	}

	public void clusterSelected() {
		cartiModel.clusterSelecteds();

		Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
		Set<Integer> clustersToShow = cartiModel.getClustersToShow();

		cartiView.updateClusterInfo(clustersMap, clustersToShow);
	}

	public void addSelectedToClusters() {
		Set<Integer> clusterIds = cartiView.getClusterInfo()
				.getSelectedRowsClusterIds();

		// if the user has not selected a cluster
		if (clusterIds.isEmpty()) {
			return;
		}

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

	/**
	 * Removes either the selecteds or the filtereds from the clusters.
	 * 
	 * @param removeSelecteds
	 */
	public void removeFromClusters(boolean removeSelecteds) {
		Set<Integer> clusterIds = cartiView.getClusterInfo()
				.getSelectedRowsClusterIds();

		// if the user has not selected a cluster
		if (clusterIds.isEmpty()) {
			return;
		}

		for (int id : clusterIds) {
			if (removeSelecteds) {
				cartiModel.removeSelectedsFromCluster(id);
			} else {
				cartiModel.removeFilteredsFromCluster(id);
			}
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

	public void deleteClusters() {
		Set<Integer> clusterIds = cartiView.getClusterInfo()
				.getSelectedRowsClusterIds();

		// if the user has not selected a cluster
		if (clusterIds.isEmpty()) {
			return;
		}

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

	public void selectClusters() {
		Set<Integer> clusterIds = cartiView.getClusterInfo()
				.getSelectedRowsClusterIds();

		// if the user has not selected a cluster
		if (clusterIds.isEmpty()) {
			return;
		}

		cartiModel.selectClusters(clusterIds);

		Set<Integer> selectedLocs = cartiModel.getSelectedLocs();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		double[] standardDevs = cartiModel.getStandardDeviations(selecteds);
		int[] medAbsDevs = cartiModel.getLocsMedAbsDev(selecteds);

		cartiView.updateFigureSelected(selectedLocs);
		cartiView.updateSelOptions(orderedObjs, selecteds);
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs, medAbsDevs);
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

	public void mineIMM() {
		int minLen = cartiView.getMiningOptions().getMinLenVal();

		if (minLen == -1) {
			return;
		}

		int resultSize = cartiModel.mineItemsets(minLen);
		if (resultSize == 0) {
			cartiView.showInfoMessage("0 clusters found, try different k or minLen.",
					"Mining result");
			return;
		}

		// update view
		Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
		Set<Integer> clustersToShow = cartiModel.getClustersToShow();

		cartiView.updateClusterInfo(clustersMap, clustersToShow);
		cartiView.showInfoMessage(resultSize + " cluster(s) found.",
				"Mining result");
	}

	public void mineRMM(boolean onlySelected) {
		int minSup = cartiView.getMiningOptions().getMinSupVal();
		int numOfItemSets = cartiView.getMiningOptions().getNumOfItemSetsVal();

		if ((minSup == -1) || (numOfItemSets == -1)) {
			return;
		}

		// get the items
		PlainItemDB items;
		if (onlySelected) {
			items = cartiModel.getSelectedProjDbOnlySelected();
		} else {
			items = cartiModel.getSelectedProjDb();
		}

		// do mining
		List<PlainItemSet> result = RandomMaximalMiner.runParallel(items, minSup,
				numOfItemSets);

		if (result.size() == 0) {
			cartiView.showInfoMessage("0 clusters found, try different k or minSup.",
					"Mining result");
			return;
		}

		// dims for which the cluster was made
		DistMeasure measure = cartiModel.getSelectedDistMeasure();
		Set<Integer> dims = measure.getDims();

		// turn result into clusters and add to model
		for (PlainItemSet itemSet : result) {
			Set<Integer> objects = new HashSet<Integer>();
			for (PlainItem item : itemSet) {
				objects.add(item.getId());
			}

			Cluster cluster = new Cluster(objects, dims);
			cartiModel.addCluster(cluster);
		}

		// update view
		Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
		Set<Integer> clustersToShow = cartiModel.getClustersToShow();

		cartiView.updateClusterInfo(clustersMap, clustersToShow);
		cartiView.showInfoMessage(result.size() + " cluster(s) found.",
				"Mining result");
	}

	public void addDistMeasure() {
		boolean isEucl = cartiView.getDistanceOptions().distModeIsEuclidian();
		boolean isCos = cartiView.getDistanceOptions().distModeIsCosine();
		Set<Integer> dims = cartiView.getDistanceOptions().getSelectedDims();

		DistMeasure distMeasure;
		if (isEucl) {
			distMeasure = new EuclidianDistMeasure(dims);
		} else if (isCos) {
			distMeasure = new CosineDistMeasure(dims);
		} else {
			distMeasure = null;
		}

		cartiModel.addDistMeasure(distMeasure);

		cartiView.addDistMeasure(distMeasure.toString());
	}

	public void selectDistMeasure() {
		int selectedDistMeasureId = cartiView.getDistanceOptions()
				.getSelectedMeasureId();
		int numDims = cartiModel.getNumDims();

		if (cartiView.shouldSyncOrderSlider() && (selectedDistMeasureId < numDims)) {
			// need to change orderSlider, which will do all the necessary
			// updates
			cartiView.updateOrderSlider(selectedDistMeasureId);
		} else {
			// need to update here
			cartiModel.setSelectedDistMeasureId(selectedDistMeasureId);

			int[][] matrixToShow = cartiModel.getMatrixToShow();

			cartiView.updateFigure(matrixToShow);
		}
	}

	public void getNoiseInSelDistMeas() {
		int minSup = cartiView.getNoiseOptions().getMinSupVal();

		if (minSup == -1) {
			return;
		}

		// calculate noise objects
		Set<Integer> noiseObjs = cartiModel.getNoiseObjsInSelDistMeas(minSup);

		if (noiseObjs.size() == 0) {
			cartiView.showInfoMessage("0 noise objects found for given minSup.",
					"Noise result");
			return;
		}

		// turn noise objects into a cluster
		Set<Integer> dims = new HashSet<Integer>();
		Cluster cluster = new Cluster(noiseObjs, dims);
		cartiModel.addCluster(cluster);

		// update view
		Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
		Set<Integer> clustersToShow = cartiModel.getClustersToShow();

		cartiView.updateClusterInfo(clustersMap, clustersToShow);
		cartiView.showInfoMessage(noiseObjs.size()
				+ " noise objects found, adding them as a cluster.", "Noise result");
	}

	public void getNoiseInAllDistMeas() {
		int minSup = cartiView.getNoiseOptions().getMinSupVal();

		if (minSup == -1) {
			return;
		}

		// calculate noise objects
		Set<Integer> noiseObjs = cartiModel.getNoiseObjsInAllDistMeas(minSup);

		if (noiseObjs.size() == 0) {
			cartiView.showInfoMessage("0 noise objects found for given minSup.",
					"Noise result");
			return;
		}

		// turn noise objects into a cluster
		Set<Integer> dims = new HashSet<Integer>();
		Cluster cluster = new Cluster(noiseObjs, dims);
		cartiModel.addCluster(cluster);

		// update view
		Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
		Set<Integer> clustersToShow = cartiModel.getClustersToShow();

		cartiView.updateClusterInfo(clustersMap, clustersToShow);
		cartiView.showInfoMessage(noiseObjs.size()
				+ " noise objects found, adding them as a cluster.", "Noise result");
	}

	public void findRelatedDims() {
		int minSup = cartiView.getMiningOptions().getMinSupVal();
		int numOfItemSets = cartiView.getMiningOptions().getNumOfItemSetsVal();

		// get related dims matrix
		int[][] relatedDimsMatrix = cartiModel.createRelatedDimsMatrix(minSup,
				numOfItemSets);

		cartiView.showRelatedDims(relatedDimsMatrix);
	}

	// LISTENERS
	/**
	 * @return Listener to listen to all buttons in the view.
	 */
	private ActionListener createButtonsListener() {
		ActionListener listener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand() == SelOptions.CLEAR) {
					manSelectedsClear();
				} else if (e.getActionCommand() == FilterOptions.CLEAR) {
					manFilteredsClear();
				} else if (e.getActionCommand() == FilterOptions.UNDO) {
					undoFiltering();
				} else if (e.getActionCommand() == FilterOptions.FILTERSEL) {
					filter(true);
				} else if (e.getActionCommand() == FilterOptions.FILTERNOTSEL) {
					filter(false);
				} else if (e.getActionCommand() == DistOptions.ADD) {
					addDistMeasure();
				} else if (e.getActionCommand() == MineOptions.MINEIMM) {
					mineIMM();
				} else if (e.getActionCommand() == MineOptions.MINERMM) {
					mineRMM(false);
				} else if (e.getActionCommand() == MineOptions.MINERMMSEL) {
					mineRMM(true);
				} else if (e.getActionCommand() == MineOptions.FINDRELDIMS) {
					findRelatedDims();
				} else if (e.getActionCommand() == NoiseOptions.SELMEAS) {
					getNoiseInSelDistMeas();
				} else if (e.getActionCommand() == NoiseOptions.ALLMEAS) {
					getNoiseInAllDistMeas();
				} else if (e.getActionCommand() == CartiView.CLUSTER) {
					clusterSelected();
				} else if (e.getActionCommand() == CartiView.SHOWDIST) {
					updateDistribution(false);
				} else if (e.getActionCommand() == ClusterInfo.ADD) {
					addSelectedToClusters();
				} else if (e.getActionCommand() == ClusterInfo.REMOVESEL) {
					removeFromClusters(true);
				} else if (e.getActionCommand() == ClusterInfo.REMOVEFIL) {
					removeFromClusters(false);
				} else if (e.getActionCommand() == ClusterInfo.DELETE) {
					deleteClusters();
				} else if (e.getActionCommand() == ClusterInfo.SELECT) {
					selectClusters();
				}
			}
		};

		return listener;
	}

	protected void updateDistribution(boolean reset) {
		cartiView.updateDistribution(cartiModel.getDistribution(), reset);
	}

	/**
	 * @return Listener for changes in selection in the SelOptions list.
	 */
	private ListSelectionListener createSelOptionsListListener() {
		ListSelectionListener listener = new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false
						&& (cartiView.selOptionsListenerShouldListen())) {
					manSelectedsChange(cartiView.getSelectionOptions().getSelecteds());
				}
			}
		};

		return listener;
	}

	/**
	 * @return Listener for mouse licks in the cartiPanel figure.
	 */
	private MouseListener createCartiPanelListener() {
		MouseListener listener = new MouseListener() {

			private int startX;

			@Override
			public void mousePressed(MouseEvent e) {
				// only left click
				if (e.getButton() != MouseEvent.BUTTON1) {
					return;
				}

				startX = e.getX();
			}

			@Override
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

			@Override
			public void mouseClicked(MouseEvent e) {
				// Only right click
				if (e.getButton() != MouseEvent.BUTTON3) {
					return;
				}

				int x = e.getX();

				int[] cells = cartiView.getCartiPanel().getCells(x, x);

				orderByObject(cells[0]);
			}

			// do nothing for these
			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

		};

		return listener;
	}

	/**
	 * @return Listener for changes in the ClusterInfo table (whether a cluster is
	 *         visible/not visible)
	 */
	private TableModelListener createClusterTableModelListener() {
		TableModelListener listener = new TableModelListener() {

			@Override
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

	/**
	 * @return Listener for changes in the sliders.
	 */
	private ChangeListener createSliderListener() {
		ChangeListener listener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();

				// slider has stopped moving
				if (!slider.getValueIsAdjusting()) {
					slider.setToolTipText(Integer.toString(slider.getValue()));
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

	/**
	 * @return Listener for DistOptions combo box selection.
	 */
	private ActionListener createDistOptionsBoxListener() {
		ActionListener listener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (cartiView.distOptionsListenerShouldListen()) {
					selectDistMeasure();
				}
			}
		};

		return listener;
	}
}
