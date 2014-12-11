package vinem.controller;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import vinem.model.CartiModel;
import vinem.view.CartiPanel;
import vinem.view.CartiView;
import vinem.view.ClusterInfo;
import vinem.view.DistOptions;
import vinem.view.FilterOptions;
import vinem.view.MineOptions;
import vinem.view.NoiseOptions;
import vinem.view.SelOptions;
import cart.cartifier.CosineDistance;
import cart.cartifier.Dissimilarity;
import cart.cartifier.EuclidianDistance;

/**
 * @author Detlev
 * @author Aksehirli
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
		List<Dissimilarity> dissimilarities = model.getDistMeasures();

		view.init(model.getOrderedObjList(), dims, maxK, matrixToShow,
				dissimilarities, model.getMaxEps());
		view.addButtonsListener(createButtonsListener());
		view.addSelOptionsListListener(createSelOptionsListListener());
		view.addCartiPanelListener(createCartiPanelListener());
		view.addClusterTableModelListener(createClusterTableModelListener());
		addSliderListeners();
		view.addDistOptionsBoxListener(createDistOptionsBoxListener());

		view.getFrame().pack();
		view.getFrame().setVisible(true);
	}

	private void orderSliderChanged() {
		model.setOrderDim(view.getOrderSliderVal());

		if (view.shouldSyncOrderSlider()) {
			// need to update the selected distance measure id
			model.setSelectedDistMeasureId(view.getOrderSliderVal());
			view.updateSelectedDistMeasureId(view.getOrderSliderVal());
		}
		updateAfterOrderChange();
	}

	private void orderByObject(int objIx) {
		model.setOrderByObj(objIx);
		updateAfterOrderChange();
	}

	private void updateAfterOrderChange() {
		view.updateFigure(model.getMatrixToShow());
		view.updateFigureSelected(model.getSelectedLocs());
		view.updateFigureClustered(model.getLocsOfClustersToShow());
		view.updateSelOptions(model.getOrderedObjList(), model.getSelecteds());
		updateDistribution(true);
	}

	private void kSliderChanged() {
		int k = view.getKSliderVal();
		model.setK(k);
		afterCartDbChange();
	}

	private void epsSliderChanged() {
		double eps = view.getEpsSliderVal();
		model.setEps(eps);
		afterCartDbChange();
	}

	private void afterCartDbChange() {
		view.updateFigure(model.getMatrixToShow());
		view.updateSelStats(model.getSelectedObjs(), model.getSupportOfSel(),
				model.getStDevOfSel(), model.getMedAbsDevOfSel());
		setDefaultParameters();
		updateDistribution(true);
	}

	private void setDefaultParameters() {
		view.getMiningOptions().setMinLenVal(model.getDefaultMinLen());
		view.getMiningOptions().setMinSupVal(model.getDefaultMinSup());
		view.getMiningOptions().setNoiseThreshold(model.getDefaultNoiseSup());
	}

	private void manSelectedsClear() {
		model.clearSelecteds();
		updateViewSelection();
	}

	private void manSelectedsChange(Set<Integer> toSelect) {
		model.setSelecteds(toSelect);

		view.updateFigureSelected(model.getSelectedLocs());
		view.updateSelStats(model.getSelectedObjs(), model.getSupportOfSel(),
				model.getStDevOfSel(), model.getMedAbsDevOfSel());
	}

	private void figureSelectedsChange(Set<Integer> locsToSelect) {
		boolean select = view.getSelectionOptions().selModeIsSelect();
		boolean and = view.getSelectionOptions().selModeIsAnd();
		boolean or = view.getSelectionOptions().selModeIsOr();
		model.selectLocs(locsToSelect, select, and, or);

		updateViewSelection();
	}

	private void manFilteredsClear() {
		model.clearFiltereds();

		view.clearFigureSavedLocs();
		view.updateFigure(model.getMatrixToShow());
		view.updateFigureClustered(model.getLocsOfClustersToShow());
		view.updateFigureSelected(model.getSelectedLocs());
		view.updateSelOptions(model.getOrderedObjList(), model.getSelecteds());
	}

	/**
	 * Filters either every selected object, or every non-selected object.
	 * 
	 * @param filterOutSelected
	 */
	private void filter(boolean filterOutSelected) {
		if (filterOutSelected) {
			model.filterSelecteds();
		} else {
			model.filterNotSelecteds();
		}
		afterFilterChange();
	}

	private void undoFiltering() {
		if (!model.canUndoFiltering()) {
			return;
		}
		model.undoFiltering();
		afterFilterChange();
	}

	private void afterFilterChange() {
		view.clearFigureSavedLocs();
		view.updateFigure(model.getMatrixToShow());
		view.updateFigureClustered(model.getLocsOfClustersToShow());
		view.updateClusterInfo(model.getClustersMap(), model.getClustersToShow());
		updateViewSelection();
	}

	private void clusterSelected() {
		model.clusterSelecteds();
		view.updateClusterInfo(model.getClustersMap(), model.getClustersToShow());
	}

	private void addSelectedToClusters() {
		Set<Integer> clusterIds = view.getClusterInfo().getSelectedRowsClusterIds();

		// if the user has not selected a cluster
		if (clusterIds.isEmpty()) {
			return;
		}

		for (int id : clusterIds) {
			model.addSelectedsToCluster(id);
		}

		view.updateClusterInfo(model.getClustersMap(), model.getClustersToShow());

		// only need to update the figure for clustereds if one of the clusters
		// is visible
		for (Integer id : clusterIds) {
			if (model.clusterIsVisible(id)) {
				view.updateFigureClustered(model.getLocsOfClustersToShow());
				break;
			}
		}
	}

	/**
	 * Removes either the selecteds or the filtereds from the clusters.
	 * 
	 * @param removeSelecteds
	 */
	private void removeFromClusters(boolean removeSelecteds) {
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

		view.updateClusterInfo(model.getClustersMap(), model.getClustersToShow());

		// only need to update the figure for clustereds if one of the clusters
		// is visible
		for (Integer id : clusterIds) {
			if (model.clusterIsVisible(id)) {
				view.updateFigureClustered(model.getLocsOfClustersToShow());
				break;
			}
		}
	}

	private void deleteClusters() {
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

		view.updateClusterInfo(model.getClustersMap(), model.getClustersToShow());

		// only need to update the figure for clustereds if one of the clusters
		// was visible
		if (wasVisible) {
			view.updateFigureClustered(model.getLocsOfClustersToShow());
		}
	}

	private void selectClusters() {
		Set<Integer> clusterIds = view.getClusterInfo().getSelectedRowsClusterIds();

		// if the user has not selected a cluster
		if (clusterIds.isEmpty()) {
			return;
		}

		model.selectClusters(clusterIds);

		updateViewSelection();
	}

	private void updateViewSelection() {
		view.updateFigureSelected(model.getSelectedLocs());
		view.updateSelOptions(model.getOrderedObjList(), model.getSelecteds());
		view.updateSelStats(model.getSelectedObjs(), model.getSupportOfSel(),
				model.getStDevOfSel(), model.getMedAbsDevOfSel());
	}

	private void saveClusters() {
		Set<Integer> clusterIds = view.getClusterInfo().getSelectedRowsClusterIds();
		if (clusterIds.isEmpty()) {
			JOptionPane.showMessageDialog(null,
					"Please select cluster to save by hightlighting them on the table.",
					"No clusters selected", WARNING_MESSAGE);
			return;
		}
		final boolean saveDim = view.getClusterInfo().saveDimCB.isSelected();
		final boolean saveSize = view.getClusterInfo().saveSizeCB.isSelected();
		File clusterFile;
		try {
			clusterFile = model.saveClusters(clusterIds, saveDim, saveSize);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Could not save the clusters: " + e.toString(),
					"Problem while saving!", ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}
		final String msg = "Selected clusters are saved to the file:"
				+ clusterFile.getAbsolutePath();
		showConfirmSaveFile(clusterFile, msg, "Clusters are saved");
		System.out.println(msg);
	}

	private void saveMatrix() {
		System.out.println("Will save!");
		int w = view.getCartiPanel().getWidth() - CartiPanel.TickSize;
		int h = view.getCartiPanel().getHeight() - CartiPanel.TickSize;
		BufferedImage img = new BufferedImage(w, h, TYPE_INT_RGB);
		view.getCartiPanel().paintComponent(img.createGraphics());
		File file;
		try {
			final String measure = model.getSelectedDistMeasure().toString()
					.replace(" ", "_");
			file = File.createTempFile("NeighMat-" + model.getInputFile().baseName()
					+ "-" + measure + "-", ".png");
			ImageIO.write(img, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Could not save the file:" + e.getMessage()
							+ " Check console for details.", "Cannot save", ERROR_MESSAGE);
			return;
		}
		showConfirmSaveFile(file, "Neighborhood matrix is saved to the File:"
				+ file.getAbsolutePath(), "Matrix saved");
	}

	private static void showConfirmSaveFile(File savedFile, final String msg,
			final String dialogTitle) {
		int openFile = JOptionPane.showConfirmDialog(null, "<html>" + msg
				+ "<br/>Do you want to open the file with the default editor?</html>",
				dialogTitle, JOptionPane.YES_NO_OPTION, INFORMATION_MESSAGE);
		if (openFile == 0) {
			try {
				Desktop.getDesktop().open(savedFile);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,
						"Could not open the file: " + e.toString(),
						"Problem while opening the file!", ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	private void showCluster(Integer clusterId) {
		model.showCluster(clusterId);

		Set<Integer> clusteredLocs = model.getLocsOfClustersToShow();

		view.updateFigureClustered(clusteredLocs);
	}

	private void hideCluster(Integer clusterId) {
		model.hideCluster(clusterId);

		Set<Integer> clusteredLocs = model.getLocsOfClustersToShow();

		view.updateFigureClustered(clusteredLocs);
	}

	private void mineIMM(boolean onlySelected) {
		int minLen = view.getMiningOptions().getMinLenVal();

		if (minLen == -1) {
			return;
		}

		int resultSize = model.mineItemsets(onlySelected, minLen);
		if (resultSize == 0) {
			view.showInfoMessage("0 clusters found, try different k or minLen.",
					"Mining result");
			return;
		}

		// update view
		view.updateClusterInfo(model.getClustersMap(), model.getClustersToShow());
		view.showInfoMessage(resultSize + " cluster(s) found.", "Mining result");
	}

	private void mineRMM(boolean onlySelected) {
		int minSup = view.getMiningOptions().getMinSupVal();
		int numOfItemSets = view.getMiningOptions().getNumOfItemSetsVal();

		if ((minSup == -1) || (numOfItemSets == -1)) {
			return;
		}

		int resultSize = model.mineRandomFreqs(onlySelected, minSup, numOfItemSets);
		if (resultSize == 0) {
			view.showInfoMessage("0 clusters found, try different k or minLen.",
					"Mining result");
			return;
		}

		// update view
		view.updateClusterInfo(model.getClustersMap(), model.getClustersToShow());
		view.showInfoMessage(resultSize + " cluster(s) found.", "Mining result");
	}

	private void addDistMeasure() {
		boolean isEucl = view.getDistanceOptions().distModeIsEuclidian();
		boolean isCos = view.getDistanceOptions().distModeIsCosine();
		List<Integer> dims = view.getDistanceOptions().getSelectedDims();

		Dissimilarity dissimilarity;
		if (isEucl) {
			dissimilarity = new EuclidianDistance(dims);
		} else if (isCos) {
			dissimilarity = new CosineDistance(dims);
		} else {
			return;
		}

		model.addDistMeasure(dissimilarity);
		view.addDistMeasure(dissimilarity.toString());
	}

	private void selectDistMeasure() {
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

	private void findNoiseInSelectedMeasure() {
		int minSup = view.getNoiseOptions().getMinSupVal();

		if (minSup == -1) {
			return;
		}
		int noiseObjs = model.findNoiseObjsInSelDistMeas(minSup);

		afterFindingNoise(noiseObjs);
	}

	private void findNoiseInEachMeasure() {
		int minSup = view.getNoiseOptions().getMinSupVal();

		if (minSup == -1) {
			return;
		}
		int noiseObjs = model.findNoiseObjsInEachProj(minSup);

		afterFindingNoise(noiseObjs);
	}

	private void findNoiseGlobally() {
		int minSup = view.getNoiseOptions().getMinSupVal();

		if (minSup == -1) {
			return;
		}
		int noiseObjs = model.findNoiseGlobally(minSup);

		afterFindingNoise(noiseObjs);
	}

	private void afterFindingNoise(int numOfNoiseObjs) {
		if (numOfNoiseObjs == 0) {
			view.showInfoMessage("Could not find any outliers for the given minSup.",
					"No outliers found");
			return;
		}

		// update view
		view.updateClusterInfo(model.getClustersMap(), model.getClustersToShow());
		view.showInfoMessage(numOfNoiseObjs
				+ " outliers found, adding them as cluster(s).", "Outliers found");
	}

	private void findRelatedDims() {
		int minSup = view.getMiningOptions().getMinSupVal();
		int numOfItemSets = view.getMiningOptions().getNumOfItemSetsVal();

		// get related dims matrix
		int[][] relatedDimsMatrix = model.createRelatedDimsMatrix(minSup,
				numOfItemSets);

		view.showRelatedDims(relatedDimsMatrix);
	}

	private void addSliderListeners() {
		view.getKSlider().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				kSliderChanged();
			}
		});
		view.getEpsSlider().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				epsSliderChanged();
			}
		});
		view.getOrderSlider().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				orderSliderChanged();
			}
		});
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
					mineIMM(false);
				} else if (e.getActionCommand() == MineOptions.MINEIMMSEL) {
					mineIMM(true);
				} else if (e.getActionCommand() == MineOptions.MINERMM) {
					mineRMM(false);
				} else if (e.getActionCommand() == MineOptions.MINERMMSEL) {
					mineRMM(true);
				} else if (e.getActionCommand() == MineOptions.FINDRELDIMS) {
					findRelatedDims();
				} else if (e.getActionCommand() == NoiseOptions.SELMEAS) {
					findNoiseInSelectedMeasure();
				} else if (e.getActionCommand() == NoiseOptions.EACHMEAS) {
					findNoiseInEachMeasure();
				} else if (e.getActionCommand() == NoiseOptions.ALLMEAS) {
					findNoiseGlobally();
				} else if (e.getActionCommand() == SelOptions.CLUSTER) {
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
				} else if (e.getActionCommand() == ClusterInfo.SAVECLUSTERS) {
					saveClusters();
				} else if (e.getActionCommand() == CartiView.CARTIFIER_CHANGE) {
					changeCartifier();
				} else if (e.getActionCommand() == CartiView.SAVE_MATRIX) {
					saveMatrix();
				}
			}
		};

		return listener;
	}

	private void changeCartifier() {
		Neighborhood neigh = view.selectedNeighborhood();
		model.switchCartifier(neigh);

		afterCartDbChange();
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
	 * @return Listener for mouse clicks in the cartiPanel figure.
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
