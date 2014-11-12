package cart.view;

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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import cart.gui2.Cluster;

public class ClusterInfo {

	public final static String ADD = "ClusterInfo.Add";
	public final static String REMOVESEL = "ClusterInfo.RemoveSel";
	public final static String REMOVEFIL = "ClusterInfo.RemoveFil";
	public final static String DELETE = "ClusterInfo.Delete";
	public final static String SELECT = "ClusterInfo.Select";

	private JPanel infoPanel;
	private JTable table;
	private ClusterTable tableModel;

	private JButton add;
	private JButton removeSel;
	private JButton removeFil;
	private JButton delete;
	private JButton select;

	public void init() {
		// the main panel
		infoPanel = CartiView.createVerticalBoxPanel(600, 350);

		// the table containg cluster information
		tableModel = new ClusterTable();

		table = new JTable(tableModel) {
			// make it so column widths automatically fit largest entry
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row,
					int column) {
				Component component = super.prepareRenderer(renderer, row, column);
				int rendererWidth = component.getPreferredSize().width;
				TableColumn tableColumn = getColumnModel().getColumn(column);
				tableColumn.setPreferredWidth(Math.max(rendererWidth
						+ getIntercellSpacing().width, tableColumn.getPreferredWidth()));
				return component;
			}
		};
		table.setAlignmentX(Component.CENTER_ALIGNMENT);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setAutoCreateRowSorter(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// make it so text is centered
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
		table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

		infoPanel.add(new JScrollPane(table));

		// add/remove/delete/select buttons
		JPanel topButtonsPanel = CartiView.createHorizontalBoxPanel(600, 50);
		JPanel bottomButtonsPanel = CartiView.createHorizontalBoxPanel(600, 50);
		add = new JButton("Add selecteds to cluster(s)");
		add.setActionCommand(ADD);

		delete = new JButton("Delete cluster(s)");
		delete.setActionCommand(DELETE);

		select = new JButton("Select cluster(s)");
		select.setActionCommand(SELECT);

		removeSel = new JButton("Remove selecteds from cluster(s)");
		removeSel.setActionCommand(REMOVESEL);

		removeFil = new JButton("Remove filtereds from cluster(s)");
		removeFil.setActionCommand(REMOVEFIL);

		topButtonsPanel.add(add);
		topButtonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		topButtonsPanel.add(delete);
		topButtonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		topButtonsPanel.add(select);

		bottomButtonsPanel.add(removeSel);
		bottomButtonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		bottomButtonsPanel.add(removeFil);

		infoPanel.add(topButtonsPanel);
		infoPanel.add(bottomButtonsPanel);
	}

	public void addButtonsListener(ActionListener buttonsListener) {
		add.addActionListener(buttonsListener);
		removeSel.addActionListener(buttonsListener);
		removeFil.addActionListener(buttonsListener);
		delete.addActionListener(buttonsListener);
		select.addActionListener(buttonsListener);
	}

	public void addTableModelListener(TableModelListener tableModelListener) {
		tableModel.addTableModelListener(tableModelListener);
	}

	public void updateClusterInfo(Map<Integer, Cluster> clustersMap,
			Set<Integer> clustersToShow) {
		// cluster Ids of selected rows before update
		Set<Integer> selectedClusterIds = getSelectedRowsClusterIds();

		// update the table
		tableModel.setRows(clustersMap, clustersToShow);

		// keep selected rows in sync
		setSelectedRows(selectedClusterIds);
	}

	/**
	 * @return The cluster ids on the rows selected by the user (returns empty set
	 *         if no row is selected)
	 */
	public Set<Integer> getSelectedRowsClusterIds() {
		int rows[] = table.getSelectedRows();
		Set<Integer> ids = new HashSet<Integer>();

		for (int i = 0; i < rows.length; i++) {
			ids.add((int) table.getValueAt(rows[i], 1));
		}

		return ids;
	}

	/**
	 * Sets the selected rows to be the rows with given clusterIds.
	 * 
	 * @param clusterIds
	 */
	private void setSelectedRows(Set<Integer> clusterIds) {
		for (int row = 0; row < table.getRowCount(); row++) {
			if (clusterIds.contains(table.getValueAt(row, 1))) {
				table.addRowSelectionInterval(row, row);
			}
		}
	}

	public JPanel getInfoPanel() {
		return infoPanel;
	}

	public class ClusterTable extends AbstractTableModel {
		private String[] columnNames = { "Visible", "Cluster id", "Size", "#Dims",
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
			data = new Object[clustersMap.size()][6];

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
				data[row][2] = new Integer(clustersMap.get(clusterId).getObjects()
						.size());

				// #Dims
				Set<Integer> dims = new HashSet<Integer>(clustersMap.get(clusterId)
						.getDims());
				data[row][3] = dims.size();

				// Dims
				data[row][4] = dims;

				// Objects
				Set<Integer> objs = new HashSet<Integer>(clustersMap.get(clusterId)
						.getObjects());
				data[row][5] = objs;

				row++;
			}
			fireTableDataChanged();
		}

	}
}
