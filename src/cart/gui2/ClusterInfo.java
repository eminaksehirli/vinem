package cart.gui2;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class ClusterInfo {

	final static String ADD = "ClusterInfo.Add";
	final static String REMOVE = "ClusterInfo.Remove";
	final static String DELETE = "ClusterInfo.Delete";
	final static String SELECT = "ClusterInfo.Select";

	private JPanel infoPanel;
	private JTable table;
	private ClusterTable tableModel;

	private JButton add;
	private JButton remove;
	private JButton delete;
	private JButton select;

	public void init() {
		// the main panel
		infoPanel = CartiView.createVerticalBoxPanel(600, 300);

		// the table containg cluster information
		tableModel = new ClusterTable();

		table = new JTable(tableModel);
		table.setAlignmentX(Component.CENTER_ALIGNMENT);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.getColumnModel().getColumn(0).setMaxWidth(50);
		table.getColumnModel().getColumn(1).setMaxWidth(70);
		table.getColumnModel().getColumn(2).setMaxWidth(40);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		// make it so text in cluster id and size columns is centered
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

		infoPanel.add(new JScrollPane(table));

		// add/remove/delete/select buttons
		JPanel buttonsPanel = CartiView.createHorizontalBoxPanel(600, 50);
		add = new JButton("Add to cluster(s)");
		add.setActionCommand(ADD);

		remove = new JButton("Remove from cluster(s)");
		remove.setActionCommand(REMOVE);

		delete = new JButton("Delete cluster(s)");
		delete.setActionCommand(DELETE);

		select = new JButton("Select cluster(s)");
		select.setActionCommand(SELECT);

		buttonsPanel.add(add);
		buttonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonsPanel.add(remove);
		buttonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonsPanel.add(delete);
		buttonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonsPanel.add(select);
		buttonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));

		infoPanel.add(buttonsPanel);
	}

	public void addButtonsListener(ActionListener buttonsListener) {
		add.addActionListener(buttonsListener);
		remove.addActionListener(buttonsListener);
		delete.addActionListener(buttonsListener);
		select.addActionListener(buttonsListener);
	}

	public void addTableModelListener(TableModelListener tableModelListener) {
		tableModel.addTableModelListener(tableModelListener);
	}

	public void updateClusterInfo(Map<Integer, Cluster> clustersMap,
			Set<Integer> clustersToShow) {
		boolean clustersHaveBeenDeleted = false;
		int[] selectedRows = table.getSelectedRows();

		// if clusters have been deleted, we don't need to keep selected rows in
		// sync
		if (table.getRowCount() > clustersMap.size()) {
			clustersHaveBeenDeleted = true;
		}

		// update the table
		tableModel.setRows(clustersMap, clustersToShow);

		// keep selected rows in sync
		if (!clustersHaveBeenDeleted) {
			for (int i = 0; i < selectedRows.length; i++) {
				table.addRowSelectionInterval(selectedRows[i], selectedRows[i]);
			}
		}
	}

	// returns the cluster ids on the rows selected by the user (returns null if
	// no selection)
	public Set<Integer> getSelectedRowsClusterIds() {
		int rows[] = table.getSelectedRows();
		Set<Integer> ids = new HashSet<Integer>();

		for (int i = 0; i < rows.length; i++) {
			ids.add((int) table.getValueAt(rows[i], 1));
		}

		return ids;
	}

	public JPanel getInfoPanel() {
		return infoPanel;
	}

	public class ClusterTable extends AbstractTableModel {
		private String[] columnNames = { "Visible", "Cluster id", "Size",
				"Dims", "Objects" };

		private Object[][] data;

		public ClusterTable() {
			data = new Object[0][0];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		@Override
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		// can only edit the "Visible" column
		@Override
		public boolean isCellEditable(int row, int col) {
			if (col == 0) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}

		public void setRows(Map<Integer, Cluster> clustersMap,
				Set<Integer> clustersToShow) {
			data = new Object[clustersMap.size()][5];

			int row = 0;
			for (int clusterId : clustersMap.keySet()) {
				// Visible
				if (clustersToShow.contains(clusterId)) {
					data[row][0] = new Boolean(true);
				} else {
					data[row][0] = new Boolean(false);
				}

				// Cluster id
				data[row][1] = new Integer(clusterId);

				// Size
				data[row][2] = new Integer(clustersMap.get(clusterId)
						.getObjects().size());

				// Dims
				Set<Integer> dims = new HashSet<Integer>(clustersMap.get(
						clusterId).getDims());
				data[row][3] = dims;

				// Objects
				Set<Integer> objs = new HashSet<Integer>(clustersMap.get(
						clusterId).getObjects());
				data[row][4] = objs;

				row++;
			}
			fireTableDataChanged();
		}

	}
}
