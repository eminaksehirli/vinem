package be.uantwerpen.adrem.gui.vinem;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * Runner for VINeM. It first ask for a file and then launches the main VINeM.
 * 
 * @author Detlev Van Looy
 * @author M. Emin Aksehirli
 */

public class Runner {

	public static void main(String[] args) {
		try {
			// Set System L&F
			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
