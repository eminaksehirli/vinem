package cart.view;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.table.AbstractTableModel;

public class Stats {

	private JPanel statsPanel;
	private JLabel sizeLabel;
	private JTable table;
	private StatsTable tableModel;
	private JTextArea objIdsArea;

	public void init(Set<Integer> dims) {
		// the main panel
		statsPanel = CartiView.createVerticalBoxPanel(520, 300);

		// show size
		JPanel sizePanel = new JPanel(new FlowLayout());
		sizePanel.add(new JLabel("Size:"));
		sizeLabel = new JLabel();
		sizeLabel.setText("0");
		sizePanel.add(sizeLabel);

		// show the table containing stats
		tableModel = new StatsTable(dims);
		table = new JTable(tableModel);

		// show the obj Ids of the set
		objIdsArea = new JTextArea(4, 2);
		objIdsArea.setText("[]");
		objIdsArea.setEditable(false);
		JScrollPane sPane = new JScrollPane(objIdsArea);
		sPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

		statsPanel.add(sizePanel);
		statsPanel.add(new JScrollPane(table));
		statsPanel.add(sPane);
	}

	public void updateStats(Set<Integer> objIds, int[] dimSupports,
			double[] standardDevs, int[] medAbsDevs) {
		sizeLabel.setText(String.valueOf(objIds.size()));

		Object[] supps = new Object[dimSupports.length];
		for (int i = 0; i < dimSupports.length; i++) {
			supps[i] = dimSupports[i];
		}

		Object[] devs = new Object[standardDevs.length];
		for (int i = 0; i < standardDevs.length; i++) {
			devs[i] = standardDevs[i];
		}

		Object[] mads = new Object[medAbsDevs.length];
		for (int i = 0; i < medAbsDevs.length; i++) {
			mads[i] = medAbsDevs[i];
		}

		tableModel.setCols(supps, devs, mads);

		objIdsArea.setText(objIds.toString());
	}

	public JPanel getStatsPanel() {
		return statsPanel;
	}

	private class StatsTable extends AbstractTableModel {

		private List<String> columnNames;
		private List<Object[]> data;
		Object[] dims;

		public StatsTable(Set<Integer> dimsSet) {
			data = new ArrayList<Object[]>();
			columnNames = new ArrayList<String>();

			dims = new Object[dimsSet.size()];

			int i = 0;
			for (int dim : dimsSet) {
				dims[i] = dim;
				i++;
			}

			data.add(dims);
			columnNames.add("Dimension");
		}

		@Override
		public int getColumnCount() {
			return data.size();
		}

		@Override
		public int getRowCount() {
			return data.get(0).length;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames.get(col);
		}

		@Override
		public Object getValueAt(int row, int col) {
			Object[] objects = data.get(col);
			return objects[row];
		}

		@Override
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			Object[] objects = data.get(col);
			objects[row] = value;
			fireTableCellUpdated(row, col);
		}

		public void setCols(Object[] supports, Object[] devs,
				Object[] medAbsDevs) {
			data.clear();
			columnNames.clear();
			data.add(dims);
			columnNames.add("Dimension");
			data.add(supports);
			columnNames.add("Support");
			data.add(devs);
			columnNames.add("Standard Deviation");
			data.add(medAbsDevs);
			columnNames.add("Med. Abs. Dev.");

			fireTableStructureChanged();

			table.getColumnModel().getColumn(0).setPreferredWidth(80);
			table.getColumnModel().getColumn(1).setPreferredWidth(80);
			table.getColumnModel().getColumn(2).setPreferredWidth(130);
			table.getColumnModel().getColumn(3).setPreferredWidth(130);
		}

	}
}
