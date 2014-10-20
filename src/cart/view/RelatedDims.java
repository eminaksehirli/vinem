package cart.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class RelatedDims {

	private JPanel relatedDimsPanel;
	private JTable table;
	private JSlider slider;

	public void init(int[][] relatedDimsMatrix) {
		// the main panel
		relatedDimsPanel = new JPanel();
		relatedDimsPanel.setLayout(new BoxLayout(relatedDimsPanel,
				BoxLayout.Y_AXIS));
		relatedDimsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// the table containing the related dims matrix
		createTable(relatedDimsMatrix);
		relatedDimsPanel.add(new JScrollPane(table));

		// the slider controlling which cells to highlight
		int max = (int) (0.25 * relatedDimsMatrix[0][0]);
		slider = new JSlider(0, max) {
			@Override
			public Point getToolTipLocation(MouseEvent event) {
				return new Point(event.getX() + 15, event.getY());
			}
		};
		slider.setValue(0);
		slider.setMajorTickSpacing(max / 5);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setToolTipText(Integer.toString(slider.getValue()));
		slider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// slider has stopped moving
				if (!slider.getValueIsAdjusting()) {
					slider.setToolTipText(Integer.toString(slider.getValue()));
					((DefaultTableModel) table.getModel())
							.fireTableDataChanged();
				}
			}
		});

		relatedDimsPanel.add(slider);
	}

	public JPanel getRelatedDimsPanel() {
		return relatedDimsPanel;
	}

	private void createTable(int[][] relatedDimsMatrix) {
		Object[][] rowData = new Object[relatedDimsMatrix.length][1 + relatedDimsMatrix[0].length];
		String[] columnNames = new String[1 + relatedDimsMatrix.length];

		columnNames[0] = "";
		for (int i = 0; i < relatedDimsMatrix.length; i++) {
			columnNames[i + 1] = Integer.toString(i);

			// header row
			rowData[i][0] = i;

			for (int j = 0; j < relatedDimsMatrix[0].length; j++) {
				rowData[i][j + 1] = relatedDimsMatrix[i][j];
			}
		}

		DefaultTableModel tableModel = new DefaultTableModel(rowData,
				columnNames) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table = new JTable(tableModel);

		MyRenderer renderer = new MyRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER);
		table.setDefaultRenderer(Object.class, renderer);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public class MyRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component c = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);

			int val = (int) value;
			if ((column > 0) && (val > slider.getValue())) {
				c.setBackground(Color.GREEN);
			} else {
				c.setBackground(Color.WHITE);
			}

			return c;
		}

	}
}
