package cart.gui2;

import cart.controller.CartiController;
import cart.model.CartiModel;
import cart.view.CartiView;

public class CartiMain {

	public static void main(String[] args) {
		CartiModel cartiModel = new CartiModel();
		CartiView cartiView = new CartiView();
		CartiController cartiController = new CartiController(cartiModel,
				cartiView);

		String filePath = "C:\\Users\\Detlev\\Desktop\\detlev\\2eMa\\Internship 2 stuff\\example files\\6c10d.mime";
		cartiController.run(filePath);
	}
}
