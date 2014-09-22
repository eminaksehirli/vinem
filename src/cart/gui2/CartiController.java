package cart.gui2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;





public class CartiController 
{
	private CartiModel cartiModel;
	private CartiView cartiView;
	
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
		List<String> dimList = cartiModel.getDimList();
		int[][] matrixToShow = cartiModel.getMatrixToShow();			
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		
		cartiView.init(orderedObjs, dimList, maxK, matrixToShow);
		
		cartiView.getFrame().pack();
		cartiView.getFrame().setVisible(true);
	}
	
	public void orderSliderChanged() {
		cartiModel.setOrderDim(cartiView.getOrderSlider_1());
		
		int[][] matrixToShow = cartiModel.getMatrixToShow();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		
		cartiView.updateFigure(matrixToShow);
		cartiView.updateFigureSelected(orderedObjs, selecteds);
		cartiView.updateSelOptions(orderedObjs, selecteds);
	}
	
	public void kSliderChanged() {
		cartiModel.setK(cartiView.getK());
		
		int[][] matrixToShow = cartiModel.getMatrixToShow();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		
		cartiView.updateFigure(matrixToShow);
		cartiView.updateSelStats(selecteds, dimSupports);
	}
	
	public void manSelectedsClear() {
		cartiModel.clearSelecteds();
		
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		
		cartiView.updateFigureSelected(orderedObjs, selecteds);
		cartiView.updateSelOptions(orderedObjs, selecteds);
		cartiView.updateSelStats(selecteds, dimSupports);
	}
	
	public void manSelectedsChange(Set<Integer> toSelect) {
		cartiModel.setSelecteds(toSelect);
		
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		
		cartiView.updateFigureSelected(orderedObjs, selecteds);
		cartiView.updateSelStats(selecteds, dimSupports);
	}
	
	public void figureSelectedsChange(Set<Integer> selectedLocs) {
		boolean select = cartiView.selModeIsSelect();
		boolean and = cartiView.selModeIsAnd();
		boolean or = cartiView.selModeIsOr();
		cartiModel.selectLocs(selectedLocs, select, and, or);
		
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		
		cartiView.updateFigureSelected(orderedObjs, selecteds);
		cartiView.updateSelOptions(orderedObjs, selecteds);
		cartiView.updateSelStats(selecteds, dimSupports);
	}
	
	public void manFilteredsClear() {
		cartiModel.clearFiltereds();
		
		int[][] matrixToShow = cartiModel.getMatrixToShow();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		
		cartiView.clearFigureSelectedLocs();
		cartiView.updateFigure(matrixToShow);
		cartiView.updateFigureSelected(orderedObjs, selecteds);
		cartiView.updateSelOptions(orderedObjs, selecteds);
	}
	
	public void filterSelecteds() {
		cartiModel.filterSelecteds();
		
		int[][] matrixToShow = cartiModel.getMatrixToShow();
		List<Integer> orderedObjs = cartiModel.getOrderedObjList();
		Set<Integer> selecteds = cartiModel.getSelecteds();
		int[] dimSupports = cartiModel.getSupports(selecteds);
		
		cartiView.clearFigureSelectedLocs();
		cartiView.updateFigure(matrixToShow);
		cartiView.updateFigureSelected(orderedObjs, selecteds);
		cartiView.updateSelOptions(orderedObjs, selecteds);
		cartiView.updateSelStats(selecteds, dimSupports);
	}
	
	public void undoFiltering() {
		if (cartiModel.canUndoFiltering()) {
			cartiModel.undoFiltering();
			
			int[][] matrixToShow = cartiModel.getMatrixToShow();
			List<Integer> orderedObjs = cartiModel.getOrderedObjList();
			Set<Integer> selecteds = cartiModel.getSelecteds();
			int[] dimSupports = cartiModel.getSupports(selecteds);
			
			cartiView.clearFigureSelectedLocs();
			cartiView.updateFigure(matrixToShow);
			cartiView.updateFigureSelected(orderedObjs, selecteds);
			cartiView.updateSelOptions(orderedObjs, selecteds);
			cartiView.updateSelStats(selecteds, dimSupports);
		}
	}
	
	/*
	// TODO once dimstoShow in cartiView is ready, set this up
	public void dimsToShowChanged() {
		int order_1 = cartiView.getOrderSlider_1();
		// int order_2 = cartiView.getOrderSlider_2();
		List<Integer> dimsToShow = cartiView.getDimsToShow();
		
		// System.out.println("dimsToShowChanged");
		// cartiView.updateCartiPanel(cartiModel.getMatrixToShow(dimsToShow, order_1, order_1));
	} */
}
