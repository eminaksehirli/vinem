package cart.gui2;

import java.util.Collections;
import java.util.List;

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
		cartiModel.init(filePath);
		
		List<String> dimList = cartiModel.getAllDimsStringList();
		int maxK = cartiModel.getNumObjects();
		int[][] matrixToShow = cartiModel.getMatrixToShow(Collections.singletonList(0),0,0);
		cartiView.init(dimList, maxK, matrixToShow);

		cartiView.getMainFrame().pack();
		cartiView.getMainFrame().setVisible(true);
	}
	
	public void orderSliderChanged() {
		int order_1 = cartiView.getOrderSlider_1();
		int order_2 = cartiView.getOrderSlider_2();
		List<Integer> dimsToShow = cartiView.getDimsToShow();
		
		System.out.println("order_1: " + order_1);
		System.out.println("order_2: " + order_2);
		cartiView.updateCartiPanel(cartiModel.getMatrixToShow(dimsToShow, order_1, order_2));
	}
	
	public void kSliderChanged() {
		cartiModel.updateCarts(cartiView.getK());
		int order_1 = cartiView.getOrderSlider_1();
		int order_2 = cartiView.getOrderSlider_2();
		List<Integer> dimsToShow = cartiView.getDimsToShow();
		
		System.out.println("k: " + cartiView.getK());
		cartiView.updateCartiPanel(cartiModel.getMatrixToShow(dimsToShow, order_1, order_2));
	}
	
	// TODO once dimstoShow in cartiView is ready, set this up
	public void dimsToShowChanged() {
		List<Integer> dimsToShow = cartiView.getDimsToShow();
		
		System.out.println("dimsToShowChanged");

	}
}
