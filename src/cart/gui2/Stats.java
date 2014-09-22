package cart.gui2;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;

public class Stats {

	private JPanel statsPanel;
	private JLabel sizeLabel;
	private JPanel dimSupPanel;
	private JTextArea objIdsArea;
	
	public void init(Set<Integer> objIds, int[] dimSupports) {
		// the main panel
		statsPanel = CartiView.createVerticalBoxPanel(300,300);

		// show size
		JPanel sizePanel = new JPanel(new FlowLayout());
		sizePanel.add(new JLabel("Size:"));
		sizeLabel = new JLabel();
		sizeLabel.setText(String.valueOf(objIds.size()));
		sizePanel.add(sizeLabel);

		// show the supports
		dimSupPanel = new JPanel(new GridLayout(dimSupports.length, 2));
		for (int i = 0; i < dimSupports.length; i++) {
			dimSupPanel.add(new JLabel("D_" + i + ":"));
			dimSupPanel.add(new JLabel(String.valueOf(dimSupports[i])));
		}
		
		// show the obj Ids of the set
		objIdsArea = new JTextArea(4, 2);
		objIdsArea.setText(objIds.toString());
		objIdsArea.setEditable(false);
		JScrollPane sPane = new JScrollPane(objIdsArea);
		sPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		
		statsPanel.add(sizePanel);
		statsPanel.add(dimSupPanel);
		statsPanel.add(sPane);
	}
	
	public void updateStats(Set<Integer> objIds, int[] dimSupports) {
		sizeLabel.setText(String.valueOf(objIds.size()));
		
		dimSupPanel.removeAll();
		for (int i = 0; i < dimSupports.length; i++) {
			dimSupPanel.add(new JLabel("D_" + i + ":"));
			dimSupPanel.add(new JLabel(String.valueOf(dimSupports[i])));
		}
		
		objIdsArea.setText(objIds.toString());
	}
	
	public JPanel getStatsPanel() {
		return statsPanel;
	}
}
