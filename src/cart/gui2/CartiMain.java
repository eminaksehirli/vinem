package cart.gui2;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class CartiMain {

	public static void main(String[] args) {
		try {
			// Set System L&F
//			 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			System.err.println("Cannot change look and feed, using Metal!"
					+ e.getMessage());
		}
		FileSelector.run();
	}
}
