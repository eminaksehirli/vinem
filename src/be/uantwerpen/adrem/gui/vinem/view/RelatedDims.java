package be.uantwerpen.adrem.gui.vinem.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import javax.swing.table.JTableHeader;

import be.uantwerpen.adrem.gui.vinem.model.Attribute;

public class RelatedDims {

	public static final String SAVE_RELATED = "Related.Save";

	private JPanel relatedDimsPanel;
	private JTable table;
	private JSlider slider;
	private int[][] matrix;
	private List<Attribute> dims;
	JButton saveBt;

	public RelatedDims(List<Attribute> dims, int[][] matrix) {
		this.matrix = matrix;
		this.dims = dims;
		// the main panel
		relatedDimsPanel = new JPanel(new BorderLayout());
		relatedDimsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// the table containing the related dims matrix
		createTable();
		relatedDimsPanel.add(new JScrollPane(table), BorderLayout.CENTER);

		// the slider controlling which cells to highlight
		int max = (int) (0.25 * matrix[0][0]);
		slider = new JSlider(0, max) {
			private static final long serialVersionUID = -5323561605574322770L;

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
					((DefaultTableModel) table.getModel()).fireTableDataChanged();
				}
			}
		});

		JPanel bottomPane = new JPanel(new BorderLayout());
		bottomPane.add(slider, BorderLayout.CENTER);

		saveBt = new JButton("Save");
		saveBt.setActionCommand(SAVE_RELATED);

		bottomPane.add(saveBt, BorderLayout.EAST);

		relatedDimsPanel.add(bottomPane, BorderLayout.SOUTH);
	}

	public JPanel getRelatedDimsPanel() {
		return relatedDimsPanel;
	}

	/**
	 * Turns relatedDimsMatrix into JTable
	 */
	private void createTable() {
		Object[][] rowData = new Object[matrix.length][1 + matrix[0].length];
		final String[] columnNames = new String[1 + matrix.length];

		columnNames[0] = "";
		for (int i = 0; i < matrix.length; i++) {
			columnNames[i + 1] = dims.get(i).toString();

			// row header
			rowData[i][0] = dims.get(i).toString();

			for (int j = 0; j < matrix[0].length; j++) {
				rowData[i][j + 1] = matrix[i][j];
			}
		}

		DefaultTableModel tableModel = new DefaultTableModel(rowData, columnNames) {
			private static final long serialVersionUID = 1774014217061930195L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table = new JTable(tableModel) {
			@Override
			protected JTableHeader createDefaultTableHeader() {
				return new JTableHeader(columnModel) {
					public String getToolTipText(MouseEvent e) {
						String tip = null;
						java.awt.Point p = e.getPoint();
						int index = columnModel.getColumnIndexAtX(p.x);
						int realIndex = columnModel.getColumn(index).getModelIndex();
						return columnNames[realIndex];
					}
				};
			}
		};

		MyRenderer renderer = new MyRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER);
		table.setDefaultRenderer(Object.class, renderer);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		for (int cIx = 0; cIx < columnNames.length; cIx++) {
			table.getColumnModel().getColumn(cIx).setPreferredWidth(15);
		}
	}

	/**
	 * Renders cells of the table, colours them green if they are above the slider
	 * value.
	 */
	public class MyRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 8255460337844895789L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);

			if ((column > 0) && ((int) value > slider.getValue())) {
				c.setBackground(Color.GREEN);
			} else {
				c.setBackground(Color.WHITE);
			}

			if (c instanceof JComponent) {
				JComponent jc = (JComponent) c;
				jc.setToolTipText(value.toString());
			}
			return c;
		}
	}
}
