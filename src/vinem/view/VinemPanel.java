package vinem.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

public class CartiPanel extends JPanel {
	public static final int TickSize = 15;
	private static final long serialVersionUID = 6585392843171261529L;
	private int cellHeight;
	private int cellWidth;
	private int[][] matrix;
	private int[][][] image;
	private Set<Integer> selectedLocs;
	private Set<Integer> clusteredLocs;

	/**
	 * @param matrix
	 *          A 2d matrix containing 0s and 1s, where a 1 means we need to
	 *          colour that spot.
	 */
	public CartiPanel(int[][] matrix) {
		this.matrix = matrix;
		createImage();

		setMinimumSize(new Dimension(matrix.length + TickSize, matrix[0].length
				+ TickSize));
		cellWidth = 10;
		cellHeight = 10;
		selectedLocs = new HashSet<Integer>();
		clusteredLocs = new HashSet<Integer>();
	}

	/**
	 * Updates the visualisation
	 * 
	 * @param m
	 *          A 2d matrix containing 0s and 1s, where a 1 means we need to
	 *          colour that spot.
	 */
	void updateMatrix(int[][] m) {
		this.matrix = m;
		createImage();
		colourLocs(selectedLocs, 0, 120, 0);
		colourLocs(clusteredLocs, 120, 0, 0);
		invalidate();
	}

	/**
	 * Removes colour from locations no longer selected, adds colour to newly
	 * selected locations
	 * 
	 * @param newLocs
	 */
	public void updateSelected(Set<Integer> newLocs) {
		// locations to decolour = copy(selectedLocs) - newLocs
		Set<Integer> locsToDeColour = new HashSet<Integer>(selectedLocs); // copy
		locsToDeColour.removeAll(newLocs); // - newLocs
		deColourLocs(locsToDeColour, 0, 120, 0);

		// locations to colour = newLocs - selectedLocs
		newLocs.removeAll(selectedLocs); // - selectedLocs
		colourLocs(newLocs, 0, 120, 0);

		// selectedLocs = selectedLocs - locsToDecolour + newLocs
		selectedLocs.removeAll(locsToDeColour);
		selectedLocs.addAll(newLocs);

		invalidate();
	}

	/**
	 * Removes colour from locations no longer clustered, adds colour to newly
	 * clustered locations
	 * 
	 * @param newLocs
	 */
	public void updateClustered(Set<Integer> newLocs) {
		// locations to decolour = copy(clusteredLocs) - newLocs
		Set<Integer> locsToDeColour = new HashSet<Integer>(clusteredLocs); // copy
		locsToDeColour.removeAll(newLocs); // - newLocs
		deColourLocs(locsToDeColour, 120, 0, 0);

		// locations to colour = newLocs - clusteredLocs
		newLocs.removeAll(clusteredLocs); // - clusteredLocs
		colourLocs(newLocs, 120, 0, 0);

		// clusteredLocs = clusteredLocs - locsToDecolour + newLocs
		clusteredLocs.removeAll(locsToDeColour);
		clusteredLocs.addAll(newLocs);

		invalidate();
	}

	public void clearSavedLocs() {
		selectedLocs.clear();
		clusteredLocs.clear();
	}

	@Override
	public Dimension getPreferredSize() {
		if (matrix.length == 0) {
			return new Dimension(600, 600);
		}
		return new Dimension(matrix[0].length + TickSize, matrix.length + TickSize);
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

		// add horizontal indication lines
		for (int i = 0; i < matrix[0].length; i += 100) {
			g.setColor(Color.BLACK);
			int startX = i * cellWidth;
			int startY = matrix.length * cellHeight;
			g.drawLine(startX, startY, startX, startY + TickSize);
		}

		// add vertical indication lines
		for (int i = 0; i < matrix.length; i += 100) {
			g.setColor(Color.BLACK);
			int startX = matrix[0].length * cellWidth;
			int startY = i * cellHeight;
			g.drawLine(startX, startY, startX + TickSize, startY);
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

		if (image == null) {
			image = new int[matrix.length][matrix[0].length][3];
		} else if (image.length != matrix.length
				|| image[0].length != matrix[0].length) {
			image = new int[matrix.length][matrix[0].length][3];
		}

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				image[i][j][0] = matrix[i][j] * 80;
				image[i][j][1] = matrix[i][j] * 80;
				image[i][j][2] = matrix[i][j] * 80;
			}
		}
	}

	// colour locations
	private void colourLocs(Set<Integer> locs, int red, int green, int blue) {
		for (int col : locs) {
			for (int row = 0; row < image[col].length; row++) {
				image[row][col][0] += red;
				image[row][col][1] += green;
				image[row][col][2] += blue;
			}
		}
	}

	// decolour locations
	private void deColourLocs(Set<Integer> locs, int red, int green, int blue) {
		for (int col : locs) {
			for (int row = 0; row < image[col].length; row++) {
				image[row][col][0] -= red;
				image[row][col][1] -= green;
				image[row][col][2] -= blue;
			}
		}
	}

	// colour rows
	private void colourRows(int[] starts, int red, int green, int blue) {
		for (int col = 0; col < image[0].length; col++) {
			for (int row = starts[col]; row < image[col].length; row++) {
				image[row][col][0] += red;
				image[row][col][1] += green;
				image[row][col][2] += blue;
			}
		}
	}

	// de-colour rows
	private void deColourRows(int[] starts, int red, int green, int blue) {
		for (int col = 0; col < image[0].length; col++) {
			for (int row = starts[col]; row < image[col].length; row++) {
				image[row][col][0] -= red;
				image[row][col][1] -= green;
				image[row][col][2] -= blue;
			}
		}
	}

	private void setCellSize() {
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

	public int[] getCells(int x1, int x2) {
		int start = x1 / cellWidth;
		int end = x2 / cellWidth + 1;
		return new int[] { start, end };
	}

	public int getCellCount() {
		return matrix.length;
	}

	public void showDistribution(int[] starts) {
		colourRows(starts, 0, 0, 120);
	}

	public void hideDistribution(int[] starts) {
		deColourRows(starts, 0, 0, 120);
	}
}
