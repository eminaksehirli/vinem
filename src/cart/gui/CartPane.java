package cart.gui;

import static java.awt.Color.black;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JPanel;

public class CartPane extends JPanel {
	public static class Layer {

		int[][] mat;
		Color color;
		int ofsetX = 0;
		int ofsetY = 0;

		public Layer(int[][] mat, Color color) {
			this.mat = mat;
			this.color = color;
		}

		public Layer(int[][] mat, Color color, int ofset_x, int ofset_y) {
			this(mat, color);
			this.ofsetX = ofset_x;
			this.ofsetY = ofset_y;
		}
	}

	private static final long serialVersionUID = 6585392843171261529L;
	private static final int Y_Ofset = 10;
	private static final int X_Ofset = 10;
	private int cellHeight;
	private int cellWidth;
	private int[][] matrix;
	private int[][][] image;
	private Collection<Layer> layers;

	public CartPane(int[][] matrix) {
		layers = new ArrayList<>();
		this.matrix = matrix;
		createImage();

		setMinimumSize(new Dimension(matrix.length, matrix[0].length));
		cellWidth = 10;
		cellHeight = 10;
		layers = new ArrayList<>();
	}

	void updateMatrix(int[][] m) {
		this.matrix = m;
		createImage();
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(matrix[0].length, matrix.length);
	}

	@Override
	public void paintComponent(Graphics g_1) {
		Graphics2D g = (Graphics2D) g_1;
		super.paintComponent(g);

		setCellSize();

		for (int i = 0; i < matrix[0].length; i += 100) {
			g.setColor(black);
			g.drawLine(i * cellWidth + X_Ofset, 0, i * cellWidth + X_Ofset, Y_Ofset);
		}

		for (int i = 0; i < matrix.length; i += 100) {
			g.setColor(black);
			g.drawLine(0, i * cellHeight + Y_Ofset, X_Ofset, i * cellHeight + Y_Ofset);
		}

		int y = Y_Ofset;
		for (int[][] row : image) {
			int x = X_Ofset;
			for (int[] cell : row) {
				g.setColor(new Color(cell[0], cell[1], cell[2]));
				g.fillRect(x, y, cellWidth, cellHeight);
				x += cellWidth;
			}
			if (cellHeight > 1) {
				g.setColor(Color.BLACK);
				g.drawLine(X_Ofset, y, x, y);
			}
			y += cellHeight;
		}
	}

	private void createImage() {
		if (image == null) {
			image = new int[matrix.length][matrix[0].length][3];
		} else if (image.length != matrix.length
				|| image[0].length != matrix[0].length) {
			image = new int[matrix.length][matrix[0].length][3];
		}

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				image[i][j][0] = matrix[i][j] * 40;
				image[i][j][1] = matrix[i][j] * 40;
				image[i][j][2] = matrix[i][j] * 40;
			}
		}

		int[][] redLayer = new int[matrix.length][matrix[0].length];
		int[][] greenLayer = new int[matrix.length][matrix[0].length];
		int[][] blueLayer = new int[matrix.length][matrix[0].length];
		int redMax = createLayer(Color.red, redLayer);
		int greenMax = createLayer(Color.green, greenLayer);
		int blueMax = createLayer(Color.blue, blueLayer);

		redMax = Math.min(10, redMax);
		blueMax = Math.min(10, blueMax);
		greenMax = Math.min(10, greenMax);

		int colMax = 255 - 160;
		if (redMax > 0) {
			mergeLayer(redLayer, 0, colMax / redMax);
		}
		if (greenMax > 0) {
			mergeLayer(greenLayer, 1, colMax / greenMax);
		}
		if (blueMax > 0) {
			mergeLayer(blueLayer, 2, colMax / blueMax);
		}
		invalidate();
	}

	private void mergeLayer(int[][] rlayer, int colLayer, int col) {
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[i].length; j++) {
				image[i][j][colLayer] += Math.min(255 - image[i][j][colLayer],
						rlayer[i][j] * col);
			}
		}
	}

	private int createLayer(final Color col, int[][] layer) {
		for (Layer l : layers) {
			if (l.color == col) {
				for (int i = 0; i < l.mat.length; i++) {
					for (int j = 0; j < l.mat[0].length; j++) {
						if (l.mat[i][j] != 0) {
							layer[l.ofsetY + i][l.ofsetX + j] += 1;
						}
					}
				}
			}
		}
		int max = 0;
		for (int i = 0; i < layer.length; i++) {
			for (int j = 0; j < layer[i].length; j++) {
				if (layer[i][j] > max) {
					max = layer[i][j];
				}
			}
		}
		return max;
	}

	private void setCellSize() {
		int width = this.getWidth() - X_Ofset;
		int height = this.getHeight() - Y_Ofset;

		cellHeight = Math.max(1, height / matrix.length);
		cellWidth = Math.max(1, width / matrix[0].length);
	}

	public int[] getCells(int x1, int x2) {
		int start = (x1 - X_Ofset) / cellWidth;
		int end = (x2 - X_Ofset) / cellWidth + 1;
		return new int[] { start, end };
	}

	public void setLayer(int[][] mat, Color color) {
		layers = new ArrayList<>();
		layers.add(new Layer(mat, color));
		createImage();
	}

	public void setLayers(Collection<Layer> layers) {
		this.layers = layers;
		createImage();
	}
}
