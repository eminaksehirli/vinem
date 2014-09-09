package cart.gui;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mime.tool.Utils;
import cart.gui.CartPane.Layer;
import cart.maximizer.CartiFiner;
import cart.maximizer.OneDCartifier;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class CartiManuGUI
{
	private static double[][] dims;
	private static CartiMinerFrame frame;
	private static int[][][] mat;
	private static int k;
	private static Map<Integer, Map<Integer, Integer>> freqs;
	private static CartiFiner liner;
	private static CartiFiner maxer;
	private static CartiFiner tighter;
	private static int currentDim;
	private static CartPane cartPane;
	private static Multimap<Integer, Layer> layers;

	public static void main(String[] args) throws Exception
	{
		final String dir = "/home/memin/research/data/synth";

		// String pathname = dir + "/6c10d/6c10d.mime";
		// String pathname = dir + "/10c21d/10c21d.mime";
		String pathname = dir + "//10c24d/10c24d.mime";
		// String pathname = dir + "/xor4c3d/xor4c3d.mime";
		// String pathname = dir + "/xor12c10d/xor12c10d.mime";
		// String pathname = "/home/memin/research/data/wout/metrics.mime";

		// String pathname = "/a/data.mime";
		// String pathname = "/home/memin/research/cartiplus/dat.mime";
		ArrayList<double[]> data = OneDCartifier.readData(pathname);
		dims = OneDCartifier.transpose(data);

		// miner = new CartiMiner();
		// liner = new CartiLinerLight();
		// maxer = new CartiMaximizer();
		// tighter = new CartiTighter();
		int numOfObjects = data.size();

		k = 150;
		frame = new CartiMinerFrame(k, dims.length, numOfObjects);
		frame.dimSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				JSlider s = (JSlider) e.getSource();
				if (s.getValueIsAdjusting())
				{
					return;
				}
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						updateFigure();
						updateFileName();
					}
				});
			}
		});
		frame.kSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				JSlider s = (JSlider) e.getSource();
				if (s.getValueIsAdjusting())
				{
					return;
				}
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						updateCarts();
						updateFigure();
						updateFileName();
					}
				});
			}
		});

		frame.mineButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				EventQueue.invokeLater(new Runnable() {

					@Override
					public void run()
					{
						runMiner();
						updateCarts();
						updateFigure();
					}
				});
			}
		});

		frame.lineButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				EventQueue.invokeLater(new Runnable() {

					@Override
					public void run()
					{
						runLiner();
						updateCarts();
						updateFigure();
					}
				});
			}
		});

		frame.tightButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e)
			{
				EventQueue.invokeLater(new Runnable() {

					@Override
					public void run()
					{
						runTighter();
						updateCarts();
						updateFigure();
					}
				});
			}
		});

		frame.saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int w = cartPane.getWidth();
				int h = cartPane.getHeight();
				BufferedImage img = new BufferedImage(w, h, TYPE_INT_RGB);
				cartPane.paintComponent(img.createGraphics());
				CartiCombineGUI.writeImage(img, frame.fileText.getText());
			}
		});

		updateCarts();
		updateFigure();
		updateFileName();

		frame.showTime();
	}

	private static void updateCarts()
	{
		k = frame.kSlider.getValue();
		// int[][][] carts = CartiKSelectGUI.findCarts(dims, k, true);
		int[][][] carts = CartiGUITools.getAllCarts(dims, k, false);
		System.out.println("Cart size: " + carts[0][0].length);
		mat = CartiGUITools.createConfMat(carts);

		// for (int[][] dMat : mat)
		// {
		// for (int i = k / 2; i < dMat.length - (k - k / 2); i++)
		// {
		// for (int j = 0; j < k / 2; j++)
		// {
		// if (dMat[i][j] > 0)
		// {
		// dMat[i][j] += 1;
		// }
		// }
		// for (int j = k / 2; j < dMat[i].length - (k - k / 2); j++)
		// {
		// if (dMat[i][j] > 0)
		// {
		// dMat[i][j] += 2;
		// }
		// }
		// for (int j = dMat[i].length - (k - k / 2); j < dMat[i].length; j++)
		// {
		// if (dMat[i][j] > 0)
		// {
		// dMat[i][j] += 1;
		// }
		// }
		// }
		// }
	}

	private static void createLayers()
	{
		layers = HashMultimap.create();

		// int ofset = k / 2;
		int ofset = 0;
		for (Entry<Integer, Map<Integer, Integer>> freq : freqs.entrySet())
		{
			final int dim = freq.getKey();
			for (Entry<Integer, Integer> dimFreq : freq.getValue().entrySet())
			{
				int end = dimFreq.getKey() + ofset;
				int start = dimFreq.getValue() + ofset;

				int ystart = 0;
				int yend = mat[dim][0].length;
				// this code is to shrink the squares of Liner. Couldn't find a nice
				// solution yet.
				// while (mat[dim][ystart][start] != 0)
				// {
				// ystart++;
				// }
				// while (mat[dim][yend][end] != 0)
				// {
				// yend--;
				// }

				int[][] lm = new int[yend - ystart][end - start];
				for (int i : Utils.range(0, lm.length))
				{
					for (int j : Utils.range(0, lm[0].length))
					{
						lm[i][j] = 1;
					}
				}

				layers.put(dim, new Layer(lm, Color.red, start, ystart));
			}
		}
	}

	private static void runMiner()
	{
		freqs = maxer.mineCarts(dims, k);
		createLayers();
	}

	private static void runLiner()
	{
		freqs = liner.mineCarts(dims, k);
		createLayers();
	}

	private static void runTighter()
	{
		freqs = tighter.mineCarts(dims, k);
		createLayers();
	}

	private static void updateFigure()
	{
		currentDim = frame.dimSlider.getValue();
		int[][] processedMatrix = mat[currentDim];
		if (cartPane == null)
		{
			cartPane = new CartPane(processedMatrix);
			final JScrollPane sPane = new JScrollPane(cartPane);
			sPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
			frame.add(sPane, BorderLayout.CENTER);
		} else
		{
			cartPane.updateMatrix(processedMatrix);
		}
		if (layers != null)
		{
			cartPane.setLayers(layers.get(currentDim));
		}
		frame.validate();
		frame.repaint();
	}

	private static void updateFileName()
	{
		frame.fileText.setText("/tmp/carti-k-" + frame.dimSlider.getValue() + "-"
				+ frame.kSlider.getValue() + ".png");
	}
}
