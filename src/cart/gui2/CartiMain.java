package cart.gui2;

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
