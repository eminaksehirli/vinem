package cart.gui2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import cart.maximizer.Freq;
import cart.maximizer.ItemsetMaximalMinerSupLen;

public class CartiController 
{
	private CartiModel cartiModel;
	private CartiView cartiView;
	private ItemsetMaximalMinerSupLen maximer;
	
	public CartiController(CartiModel cartiModel, CartiView cartiView) 
	{
		this.cartiModel = cartiModel;
		this.cartiView = cartiView;
	}
	
	public void run(String filePath) 
	{
		int initOrderDim = 0;
		int initK = 1;
		cartiModel.init(filePath, initK, initOrderDim);
		
		int maxK = cartiModel.getNumObjects();
		Set<Integer> dims = cartiModel.getDims();
		int[][] matrixToShow = cartiModel.getMatrixToShow();			
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		
		cartiView.init(orderedObjs, dims, maxK, matrixToShow);
		
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
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs, measures, medAbsDevs);
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
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs, measures, medAbsDevs);
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
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs, measures, medAbsDevs);
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
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs, measures, medAbsDevs);
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
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs, measures, medAbsDevs);
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
			cartiView.updateSelStats(selecteds, dimSupports, standardDevs, measures, medAbsDevs);
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
		
		// only need to update the figure for clustereds if one of the clusters is visible
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
		
		// only need to update the figure for clustereds if one of the clusters is visible
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
		
		// only need to update the figure for clustereds if one of the clusters was visible
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
		cartiView.updateSelStats(selecteds, dimSupports, standardDevs, measures, medAbsDevs);
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
	
	// TODO minLen?
	public void mine() {
		int minLen = 70;
		
		List<Freq> result = maximer.mineFor(cartiModel.getK(), minLen);
		for (Freq freq : result) {
			Cluster cluster = new Cluster(new HashSet<Integer>(freq.freqSet), new HashSet<Integer>(freq.freqDims));
			
			cartiModel.addCluster(cluster);
		}
		
		Map<Integer, Cluster> clustersMap = cartiModel.getClustersMap();
		Set<Integer> clustersToShow = cartiModel.getClustersToShow();
		
		cartiView.updateClusterInfo(clustersMap, clustersToShow);
	}
}
