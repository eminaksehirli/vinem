package cart.controller;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
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
import cart.model.Obj;
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
	private CartiModel model;
	private CartiView view;

	public CartiController(CartiModel cartiModel, CartiView cartiView) {
		this.model = cartiModel;
		this.view = cartiView;
	}

	public void run() {
		model.init();

		int maxK = model.getNumObjects();
		Set<Integer> dims = model.getDims();
		int[][] matrixToShow = model.getMatrixToShow();
		List<DistMeasure> distMeasures = model.getDistMeasures();
		List<String> distMeasuresStringList = new ArrayList<String>();

		for (DistMeasure distMeasure : distMeasures) {
			distMeasuresStringList.add(distMeasure.toString());
		}

		view.init(model.getOrderedObjList(), dims, maxK, matrixToShow,
				distMeasuresStringList);
		view.addButtonsListener(createButtonsListener());
		view.addSelOptionsListListener(createSelOptionsListListener());
		view.addCartiPanelListener(createCartiPanelListener());
		view.addClusterTableModelListener(createClusterTableModelListener());
		view.addSliderListener(createSliderListener());
		view.addDistOptionsBoxListener(createDistOptionsBoxListener());

		view.getFrame().pack();
		view.getFrame().setVisible(true);
	}

	public void orderSliderChanged() {
		model.setOrderDim(view.getOrderSliderVal());

		if (view.shouldSyncOrderSlider()) {
			// need to update the selected distance measure id
			model.setSelectedDistMeasureId(view.getOrderSliderVal());
			view.updateSelectedDistMeasureId(view.getOrderSliderVal());
		}

		updateAfterOrderChange();
	}

	public void orderByObject(int objIx) {
		model.setOrderByObj(objIx);
		updateAfterOrderChange();
	}

	public void updateAfterOrderChange() {
		int[][] matrixToShow = model.getMatrixToShow();
		Set<Integer> selectedLocs = model.getSelectedLocs();
		Set<Integer> clusteredLocs = model.getClustersToShowLocs();
		Set<Integer> selecteds = model.getSelecteds();

		view.updateFigure(matrixToShow);
		view.updateFigureSelected(selectedLocs);
		view.updateFigureClustered(clusteredLocs);
		view.updateSelOptions(model.getOrderedObjList(), selecteds);
		updateDistribution(true);
	}

	public void kSliderChanged() {
		int k = view.getKSliderVal();
		model.setK(k);

		int[][] matrixToShow = model.getMatrixToShow();
		Set<Integer> selecteds = model.getSelecteds();
		int[] dimSupports = model.getSupports(selecteds);
		double[] standardDevs = model.getStandardDeviations(selecteds);
		int[] medAbsDevs = model.getLocsMedAbsDev(selecteds);

		view.updateFigure(matrixToShow);
		view.updateSelStats(model.getSelectedObjs(), dimSupports, standardDevs,
				medAbsDevs);
		view.getMiningOptions().setMinSupVal((int) (k * 0.75));
		updateDistribution(true);
	}

	public void manSelectedsClear() {
		model.clearSelecteds();

		Set<Integer> selectedLocs = model.getSelectedLocs();
		Set<Integer> selecteds = model.getSelecteds();
		int[] dimSupports = model.getSupports(selecteds);
		double[] standardDevs = model.getStandardDeviations(selecteds);
		int[] medAbsDevs = model.getLocsMedAbsDev(selecteds);

		view.updateFigureSelected(selectedLocs);
		view.updateSelOptions(model.getOrderedObjList(), selecteds);
		view.updateSelStats(model.getSelectedObjs(), dimSupports, standardDevs,
				medAbsDevs);
	}

	public void manSelectedsChange(Set<Integer> toSelect) {
		model.setSelecteds(toSelect);

		Set<Integer> selectedLocs = model.getSelectedLocs();
		Set<Integer> selecteds = model.getSelecteds();
		int[] dimSupports = model.getSupports(selecteds);
		double[] standardDevs = model.getStandardDeviations(selecteds);
		int[] medAbsDevs = model.getLocsMedAbsDev(selecteds);

		view.updateFigureSelected(selectedLocs);
		view.updateSelStats(model.getSelectedObjs(), dimSupports, standardDevs,
				medAbsDevs);
	}

	public void figureSelectedsChange(Set<Integer> locsToSelect) {
		boolean select = view.getSelectionOptions().selModeIsSelect();
		boolean and = view.getSelectionOptions().selModeIsAnd();
		boolean or = view.getSelectionOptions().selModeIsOr();
		model.selectLocs(locsToSelect, select, and, or);

		Set<Integer> selectedLocs = model.getSelectedLocs();
		Set<Integer> selecteds = model.getSelecteds();
		int[] dimSupports = model.getSupports(selecteds);
		double[] standardDevs = model.getStandardDeviations(selecteds);
		int[] medAbsDevs = model.getLocsMedAbsDev(selecteds);

		view.updateFigureSelected(selectedLocs);
		view.updateSelOptions(model.getOrderedObjList(), selecteds);
		view.updateSelStats(model.getSelectedObjs(), dimSupports, standardDevs,
				medAbsDevs);
	}

	public void manFilteredsClear() {
		model.clearFiltereds();

		int[][] matrixToShow = model.getMatrixToShow();
		Set<Integer> selectedLocs = model.getSelectedLocs();
		Set<Integer> clusteredLocs = model.getClustersToShowLocs();
		Set<Integer> selecteds = model.getSelecteds();

		view.clearFigureSavedLocs();
		view.updateFigure(matrixToShow);
		view.updateFigureSelected(selectedLocs);
		view.updateFigureClustered(clusteredLocs);
		view.updateSelOptions(model.getOrderedObjList(), selecteds);
	}

	/**
	 * Filters either every selected object, or every non-selected object.
	 * 
	 * @param filterOutSelected
	 */
	public void filter(boolean filterOutSelected) {
		if (filterOutSelected) {
			model.filterSelecteds();
		} else {
			model.filterNotSelecteds();
		}

		int[][] matrixToShow = model.getMatrixToShow();
		Set<Integer> selectedLocs = model.getSelectedLocs();
		Set<Integer> clusteredLocs = model.getClustersToShowLocs();
		Set<Integer> selecteds = model.getSelecteds();
		int[] dimSupports = model.getSupports(selecteds);
		double[] standardDevs = model.getStandardDeviations(selecteds);
		int[] medAbsDevs = model.getLocsMedAbsDev(selecteds);
		Map<Integer, Cluster> clustersMap = model.getClustersMap();
		Set<Integer> clustersToShow = model.getClustersToShow();

		view.clearFigureSavedLocs();
		view.updateFigure(matrixToShow);
		view.updateFigureSelected(selectedLocs);
		view.updateFigureClustered(clusteredLocs);
		view.updateSelOptions(model.getOrderedObjList(), selecteds);
		view.updateSelStats(model.getSelectedObjs(), dimSupports, standardDevs,
				medAbsDevs);
		view.updateClusterInfo(clustersMap, clustersToShow);
	}

	public void undoFiltering() {
		if (model.canUndoFiltering()) {
			model.undoFiltering();

			int[][] matrixToShow = model.getMatrixToShow();
			Set<Integer> selectedLocs = model.getSelectedLocs();
			Set<Integer> clusteredLocs = model.getClustersToShowLocs();
			Set<Integer> selecteds = model.getSelecteds();
			int[] dimSupports = model.getSupports(selecteds);
			double[] standardDevs = model.getStandardDeviations(selecteds);
			int[] medAbsDevs = model.getLocsMedAbsDev(selecteds);
			Map<Integer, Cluster> clustersMap = model.getClustersMap();
			Set<Integer> clustersToShow = model.getClustersToShow();

			view.clearFigureSavedLocs();
			view.updateFigure(matrixToShow);
			view.updateFigureSelected(selectedLocs);
			view.updateFigureClustered(clusteredLocs);
			view.updateSelOptions(model.getOrderedObjList(), selecteds);
			view.updateSelStats(model.getSelectedObjs(), dimSupports, standardDevs,
					medAbsDevs);
			view.updateClusterInfo(clustersMap, clustersToShow);
		}
	}

	public void clusterSelected() {
		model.clusterSelecteds();

		Map<Integer, Cluster> clustersMap = model.getClustersMap();
		Set<Integer> clustersToShow = model.getClustersToShow();

		view.updateClusterInfo(clustersMap, clustersToShow);
	}

	public void addSelectedToClusters() {
		Set<Integer> clusterIds = view.getClusterInfo().getSelectedRowsClusterIds();

		// if the user has not selected a cluster
		if (clusterIds.isEmpty()) {
			return;
		}

		for (int id : clusterIds) {
			model.addSelectedsToCluster(id);
		}

		Map<Integer, Cluster> clustersMap = model.getClustersMap();
		Set<Integer> clustersToShow = model.getClustersToShow();

		view.updateClusterInfo(clustersMap, clustersToShow);

		// only need to update the figure for clustereds if one of the clusters
		// is visible
		for (Integer id : clusterIds) {
			if (model.clusterIsVisible(id)) {
				Set<Integer> clusteredLocs = model.getClustersToShowLocs();
				view.updateFigureClustered(clusteredLocs);
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
		Set<Integer> clusterIds = view.getClusterInfo().getSelectedRowsClusterIds();

		// if the user has not selected a cluster
		if (clusterIds.isEmpty()) {
			return;
		}

		for (Integer id : clusterIds) {
			if (removeSelecteds) {
				model.removeSelectedsFromCluster(id);
			} else {
				model.removeFilteredsFromCluster(id);
			}
		}

		Map<Integer, Cluster> clustersMap = model.getClustersMap();
		Set<Integer> clustersToShow = model.getClustersToShow();

		view.updateClusterInfo(clustersMap, clustersToShow);

		// only need to update the figure for clustereds if one of the clusters
		// is visible
		for (Integer id : clusterIds) {
			if (model.clusterIsVisible(id)) {
				Set<Integer> clusteredLocs = model.getClustersToShowLocs();
				view.updateFigureClustered(clusteredLocs);
				break;
			}
		}
	}

	public void deleteClusters() {
		Set<Integer> clusterIds = view.getClusterInfo().getSelectedRowsClusterIds();

		// if the user has not selected a cluster
		if (clusterIds.isEmpty()) {
			return;
		}

		boolean wasVisible = false;
		for (Integer id : clusterIds) {
			if (model.clusterIsVisible(id)) {
				wasVisible = true;
			}
			model.deleteCluster(id);
		}

		Map<Integer, Cluster> clustersMap = model.getClustersMap();
		Set<Integer> clustersToShow = model.getClustersToShow();

		view.updateClusterInfo(clustersMap, clustersToShow);

		// only need to update the figure for clustereds if one of the clusters
		// was visible
		if (wasVisible) {
			Set<Integer> clusteredLocs = model.getClustersToShowLocs();
			view.updateFigureClustered(clusteredLocs);
		}
	}

	public void selectClusters() {
		Set<Integer> clusterIds = view.getClusterInfo().getSelectedRowsClusterIds();

		// if the user has not selected a cluster
		if (clusterIds.isEmpty()) {
			return;
		}

		model.selectClusters(clusterIds);

		Set<Integer> selectedLocs = model.getSelectedLocs();
		Set<Integer> selecteds = model.getSelecteds();
		int[] dimSupports = model.getSupports(selecteds);
		double[] standardDevs = model.getStandardDeviations(selecteds);
		int[] medAbsDevs = model.getLocsMedAbsDev(selecteds);

		view.updateFigureSelected(selectedLocs);
		view.updateSelOptions(model.getOrderedObjList(), selecteds);
		view.updateSelStats(model.getSelectedObjs(), dimSupports, standardDevs,
				medAbsDevs);
	}

	public void showCluster(Integer clusterId) {
		model.showCluster(clusterId);

		Set<Integer> clusteredLocs = model.getClustersToShowLocs();

		view.updateFigureClustered(clusteredLocs);
	}

	public void hideCluster(Integer clusterId) {
		model.hideCluster(clusterId);

		Set<Integer> clusteredLocs = model.getClustersToShowLocs();

		view.updateFigureClustered(clusteredLocs);
	}

	public void mineIMM() {
		int minLen = view.getMiningOptions().getMinLenVal();

		if (minLen == -1) {
			return;
		}

		int resultSize = model.mineItemsets(minLen);
		if (resultSize == 0) {
			view.showInfoMessage("0 clusters found, try different k or minLen.",
					"Mining result");
			return;
		}

		// update view
		Map<Integer, Cluster> clustersMap = model.getClustersMap();
		Set<Integer> clustersToShow = model.getClustersToShow();

		view.updateClusterInfo(clustersMap, clustersToShow);
		view.showInfoMessage(resultSize + " cluster(s) found.", "Mining result");
	}

	public void mineRMM(boolean onlySelected) {
		int minSup = view.getMiningOptions().getMinSupVal();
		int numOfItemSets = view.getMiningOptions().getNumOfItemSetsVal();

		if ((minSup == -1) || (numOfItemSets == -1)) {
			return;
		}

		// get the items
		PlainItemDB items;
		if (onlySelected) {
			items = model.getSelectedProjDbOnlySelected();
		} else {
			items = model.getSelectedProjDb();
		}

		// do mining
		List<PlainItemSet> result = RandomMaximalMiner.runParallel(items, minSup,
				numOfItemSets);

		if (result.size() == 0) {
			view.showInfoMessage("0 clusters found, try different k or minSup.",
					"Mining result");
			return;
		}

		// dims for which the cluster was made
		DistMeasure measure = model.getSelectedDistMeasure();
		Set<Integer> dims = measure.getDims();

		// turn result into clusters and add to model
		for (PlainItemSet itemSet : result) {
			List<Obj> objects = new ArrayList<>();
			for (PlainItem item : itemSet) {
				objects.add(model.getObj(item.getId()));
			}

			Cluster cluster = new Cluster(objects, dims);
			model.addCluster(cluster);
		}

		// update view
		Map<Integer, Cluster> clustersMap = model.getClustersMap();
		Set<Integer> clustersToShow = model.getClustersToShow();

		view.updateClusterInfo(clustersMap, clustersToShow);
		view.showInfoMessage(result.size() + " cluster(s) found.", "Mining result");
	}

	public void addDistMeasure() {
		boolean isEucl = view.getDistanceOptions().distModeIsEuclidian();
		boolean isCos = view.getDistanceOptions().distModeIsCosine();
		Set<Integer> dims = view.getDistanceOptions().getSelectedDims();

		DistMeasure distMeasure;
		if (isEucl) {
			distMeasure = new EuclidianDistMeasure(dims);
		} else if (isCos) {
			distMeasure = new CosineDistMeasure(dims);
		} else {
			return;
		}

		model.addDistMeasure(distMeasure);

		view.addDistMeasure(distMeasure.toString());
	}

	public void selectDistMeasure() {
		int selectedDistMeasureId = view.getDistanceOptions()
				.getSelectedMeasureId();
		int numDims = model.getNumDims();

		if (view.shouldSyncOrderSlider() && (selectedDistMeasureId < numDims)) {
			// need to change orderSlider, which will do all the necessary
			// updates
			view.updateOrderSlider(selectedDistMeasureId);
		} else {
			// need to update here
			model.setSelectedDistMeasureId(selectedDistMeasureId);

			int[][] matrixToShow = model.getMatrixToShow();

			view.updateFigure(matrixToShow);
		}
	}

	public void getNoiseInSelDistMeas() {
		int minSup = view.getNoiseOptions().getMinSupVal();

		if (minSup == -1) {
			return;
		}

		// calculate noise objects
		List<Obj> noiseObjs = model.getNoiseObjsInSelDistMeas(minSup);

		processNoiseObjs(noiseObjs);
	}

	public void getNoiseInAllDistMeas() {
		int minSup = view.getNoiseOptions().getMinSupVal();

		if (minSup == -1) {
			return;
		}

		// calculate noise objects
		List<Obj> noiseObjs = model.getNoiseObjsInAllDistMeas(minSup);

		processNoiseObjs(noiseObjs);
	}

	private void processNoiseObjs(Collection<Obj> noiseObjs) {
		if (noiseObjs.size() == 0) {
			view.showInfoMessage("0 noise objects found for given minSup.",
					"Noise result");
			return;
		}

		// turn noise objects into a cluster
		Set<Integer> dims = new HashSet<Integer>();
		Cluster cluster = new Cluster(noiseObjs, dims);
		model.addCluster(cluster);

		// update view
		Map<Integer, Cluster> clustersMap = model.getClustersMap();
		Set<Integer> clustersToShow = model.getClustersToShow();

		view.updateClusterInfo(clustersMap, clustersToShow);
		view.showInfoMessage(noiseObjs.size()
				+ " noise objects found, adding them as a cluster.", "Noise result");
	}

	public void findRelatedDims() {
		int minSup = view.getMiningOptions().getMinSupVal();
		int numOfItemSets = view.getMiningOptions().getNumOfItemSetsVal();

		// get related dims matrix
		int[][] relatedDimsMatrix = model.createRelatedDimsMatrix(minSup,
				numOfItemSets);

		view.showRelatedDims(relatedDimsMatrix);
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
		view.updateDistribution(model.getDistribution(), reset);
	}

	/**
	 * @return Listener for changes in selection in the SelOptions list.
	 */
	private ListSelectionListener createSelOptionsListListener() {
		ListSelectionListener listener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false
						&& (view.selOptionsListenerShouldListen())) {
					manSelectedsChange(view.getSelectionOptions().getSelecteds());
				}
			}
		};

		return listener;
	}

	/**
	 * @return Listener for mouse licks in the cartiPanel figure.
	 */
	private MouseListener createCartiPanelListener() {
		MouseListener listener = new MouseAdapter() {

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
				int cellCount = view.getCartiPanel().getCellCount();
				int[] cells = view.getCartiPanel().getCells(x1, x2);
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

				int[] cells = view.getCartiPanel().getCells(x, x);

				orderByObject(cells[0]);
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
				if (view.clusterInfoListenerShouldListen()) {
					int row = e.getFirstRow();
					ClusterInfo.ClusterTable table = (ClusterInfo.ClusterTable) e
							.getSource();
					boolean isVisible = (boolean) table.getValueAt(row, 0);
					Integer clusterId = (Integer) table.getValueAt(row, 1);

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
					if (slider == view.getKSlider()) {
						kSliderChanged();
					} else if (slider == view.getOrderSlider()) {
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
				if (view.distOptionsListenerShouldListen()) {
					selectDistMeasure();
				}
			}
		};

		return listener;
	}
}
