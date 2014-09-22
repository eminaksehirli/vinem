package cart.gui2;

import static java.awt.Color.black;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

public class CartiPanel extends JPanel
{
	private static final long serialVersionUID = 6585392843171261529L;
	private int cellHeight;
	private int cellWidth;
	private int[][] matrix;
	private int[][][] image;
	private Set<Integer> selectedLocs;

	public CartiPanel(int[][] matrix) {
		this.matrix = matrix;
		createImage();

		setMinimumSize(new Dimension(matrix.length, matrix[0].length));
		cellWidth = 10;
		cellHeight = 10;
		selectedLocs = new HashSet<Integer>();
	}

	void updateMatrix(int[][] m) {
		this.matrix = m;
		createImage();
		colourSelectedLocs();
		invalidate();
	}
	
	// removes colour from locations no longer selected, adds colour to newly selected locations
	public void updateSelected(List<Integer> orderedObjs, Set<Integer> selecteds) {
		if (orderedObjs.size() == 0) {
			return;
		}
		
		// selected locations
		Set<Integer> newLocs = new HashSet<Integer>();
		
		// loop over all columns to find selected locations
		for (int col = 0; col < image.length; col++) {
			// if this column is selected
			if (selecteds.contains(orderedObjs.get(col))) {
				newLocs.add(col);
			}
		}
		// locsToRemove = deepCopy(selectedLocs) - newLocs
		Set<Integer> locsToRemove = new HashSet<Integer>();
		locsToRemove.addAll(selectedLocs); // deepCopy
		locsToRemove.removeAll(newLocs); // - newLocs
		deColourLocs(locsToRemove);
		
		// newLocs = newLocs - selectedLocs
		newLocs.removeAll(selectedLocs); // - selectedLocs
		colourLocs(newLocs);
		
		// selectedLocs = selectedLocs - locsToRemove + locsToAdd
		selectedLocs.removeAll(locsToRemove);
		selectedLocs.addAll(newLocs);
		
		invalidate();
	}

	@Override
	public Dimension getPreferredSize() {
		if (matrix.length == 0) {
			return new Dimension(600, 600);
		}
		return new Dimension(matrix[0].length, matrix.length);
	}

	@Override
	public void paintComponent(Graphics g_1) {
		Graphics2D g = (Graphics2D) g_1;
		super.paintComponent(g);

		setCellSize();

		int y = 0;
		for (int[][] row : image) {
			int x = 0;
			for (int[] rgb : row) {
				g.setColor(new Color(rgb[0], rgb[1], rgb[2]));
				g.fillRect(x, y, cellWidth, cellHeight);
				x += cellWidth;
			}
			if (cellHeight > 1) {
				g.setColor(Color.BLACK);
				g.drawLine(0, y, x, y);
			}
			y += cellHeight;
		}
	}

	private void createImage() {
		if (matrix.length == 0) {
			image = new int[1][1][3];
			image[0][0][0] = 0;
			image[0][0][1] = 0;
			image[0][0][2] = 0;
			return;
		}
		
		if (image == null)
		{
			image = new int[matrix.length][matrix[0].length][3];
		} else if (image.length != matrix.length
				|| image[0].length != matrix[0].length)
		{
			image = new int[matrix.length][matrix[0].length][3];
		}

		for (int i = 0; i < matrix.length; i++)
		{
			for (int j = 0; j < matrix[i].length; j++)
			{
				image[i][j][0] = matrix[i][j] * 80;
				image[i][j][1] = matrix[i][j] * 80;
				image[i][j][2] = matrix[i][j] * 80;
			}
		}
	}
	
	// colour selected locations
	private void colourSelectedLocs() {
		for (int col : selectedLocs) {
			for (int row = 0; row < image[col].length; row++) {
				image[row][col][1] += 120;
			}
		}		
	}
	
	// colour locations
	private void colourLocs(Set<Integer> locs) {
		for (int col : locs) {
			for (int row = 0; row < image[col].length; row++) {
				image[row][col][1] += 120;
			}
		}	
	}
	
	// decolour locations
	private void deColourLocs(Set<Integer> locs) {
		for (int col : locs) {
			for (int row = 0; row < image[col].length; row++) {
				image[row][col][1] -= 120;
			}
		}			
	}

	private void setCellSize()
	{
		if (matrix.length == 0) {
			cellWidth = 600;
			cellHeight = 600;
			return;
		}
		
		int width = this.getWidth();
		int height = this.getHeight();

		cellWidth = Math.max(1, width / matrix[0].length);
		cellHeight = Math.max(1, height / matrix.length);
	}

	public int[] getCells(int x1, int x2)
	{
		int start = x1 / cellWidth;
		int end = x2 / cellWidth + 1;
		return new int[] { start, end };
	}
	
	public int getCellCount() {
		return matrix.length;
	}
	
	public void clearSelectedLocs() {
		selectedLocs.clear();
	}
}
