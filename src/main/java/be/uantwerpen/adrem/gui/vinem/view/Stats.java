package be.uantwerpen.adrem.gui.vinem.view;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.table.AbstractTableModel;

import be.uantwerpen.adrem.gui.vinem.model.Attribute;
import be.uantwerpen.adrem.gui.vinem.model.Obj;

/**
 * Shows basic statistics about the selected data.
 * 
 * @author Detlev Van Looy
 * @author M. Emin Aksehirli
 */
public class Stats {

	private JPanel statsPanel;
	private JLabel sizeLabel;
	private JTable table;
	private StatsTable tableModel;
	private JTextArea objIdsArea;

	public Stats(List<Attribute> dims) {
		// the main panel
		statsPanel = VinemView.createVerticalBoxPanel(520, 300);

		// show size
		JPanel sizePanel = new JPanel(new FlowLayout());
		sizePanel.add(new JLabel("Size:"));
		sizeLabel = new JLabel();
		sizeLabel.setText("0");
		sizePanel.add(sizeLabel);

		// show the table containing stats
		tableModel = new StatsTable(dims);
		table = new JTable(tableModel);
		table.setAutoCreateRowSorter(true);

		// show the obj Ids of the set
		objIdsArea = new JTextArea(4, 2);
		objIdsArea.setText("[]");
		objIdsArea.setLineWrap(true);
		objIdsArea.setEditable(false);
		JScrollPane sPane = new JScrollPane(objIdsArea);
		sPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

		statsPanel.add(sizePanel);
		statsPanel.add(new JScrollPane(table));
		statsPanel.add(sPane);
	}

	public void updateStats(Collection<Obj> objIds, int[] dimSupports,
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

		// keep sorting the same after update
		List keys = table.getRowSorter().getSortKeys();

		tableModel.setCols(supps, devs, mads);

		table.getRowSorter().setSortKeys(keys);

		objIdsArea.setText(objIds.toString());
	}

	public JPanel getStatsPanel() {
		return statsPanel;
	}

	/**
	 * Table containing stats.
	 */
	private class StatsTable extends AbstractTableModel {
		private static final long serialVersionUID = -6492905279113375391L;
		private List<String> columnNames;
		private List<Object[]> data;
		Object[] dims;

		public StatsTable(List<Attribute> dims2) {
			data = new ArrayList<Object[]>();
			columnNames = new ArrayList<String>();

			dims = dims2.toArray();

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

		public void setCols(Object[] supports, Object[] devs, Object[] medAbsDevs) {
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
