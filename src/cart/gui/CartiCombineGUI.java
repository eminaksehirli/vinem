package cart.gui;

import static cart.maximizer.MaximalMinerCombiner.getOrd2Id;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.table.AbstractTableModel;

import mime.plain.PlainItem;
import mime.plain.PlainItemDB;
import mime.tool.Utils;
import cart.cartifier.CartifyDbInMemory;
import cart.cartifier.Pair;
import cart.gui.CartPane.Layer;
import cart.maximizer.DimBasedMaximalMiner;
import cart.maximizer.Freq;
import cart.maximizer.ItemsetMaximalMinerSupLen;
import cart.maximizer.MaximalMinerCombiner;
import cart.maximizer.MaximalMinerCombiner.FreqCollector;
import cart.maximizer.OneDCartifier;

public class CartiCombineGUI
{
	private List<Integer> AllDims;
	private double[][] dims;
	private CartiCombinerFrame frame;
	private int[][][] mat;
	private int k;
	private CartPane cartPane;
	private String pathname;
	private Pair[][] origData;
	private int numOfObjects;
	private Point selectionStart;
	private int[][] id2LocMaps;
	private Set<Integer> selecteds = new HashSet<>();
	private ChangeListener dimOrderSlideListener;
	private ChangeListener notAdvancedListener;
	private ListSelectionListener listListener;
	private List<Cluster> clusters;
	private HashSet<Cluster> clustersToMark;
	private boolean filterByCluster;
	private JScrollPane sPane;
	private DimBasedMaximalMiner dimer;
	private ItemsetMaximalMinerSupLen maximer;
	private TableModel clusterTableModel;
	private int[] selectedClusterRows;
	private CartifyDbInMemory cartiDb;

	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		} catch (UnsupportedLookAndFeelException e1)
		{
			e1.printStackTrace();
		}

		FileSelector fileSelector = new FileSelector();

		fileSelector.showTime();
		// final String dir = "/home/memin/research/data/synth";
		// //
		// // pathname = dir + "/6c10d/6c10d.mime";
		// // pathname = dir + "/10c21d/10c21d.mime";
		// // pathname = dir + "/10c24d/10c24d.mime";
		// final String path = dir + "/8c12d/8c12d.mime";
		// run(path);

		// pathname = "/home/memin/research/data/wout/metrics.mime";

		// pathname = "/home/memin/research/cartiplus/dat.mime";
		// pathname =
		// "/home/memin/research/data/Databases/synth_dbsizescale/S1500.mime";
		// pathname = "/a/data.mime";
	}

	void run(final String path)
	{
		pathname = path;
		ArrayList<double[]> data;
		try
		{
			data = OneDCartifier.readData(pathname);
			origData = OneDCartifier.toPairs(data);
		} catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		dims = OneDCartifier.transpose(data);
		AllDims = new ArrayList<>();
		for (int i = 0; i < dims.length; i++)
		{
			AllDims.add(i);
		}
		AllDims = Collections.unmodifiableList(AllDims);

		clusters = new ArrayList<>();
		clustersToMark = new HashSet<>();

		// miner = new CartiMiner();
		// liner = new CartiLinerLight();
		maximer = new ItemsetMaximalMinerSupLen(pathname);
		dimer = new DimBasedMaximalMiner(pathname);
		numOfObjects = data.size();

		k = 1;
		frame = new CartiCombinerFrame(k, dims.length, numOfObjects);

		dimOrderSlideListener = new ChangeListener() {
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
		};
		notAdvancedListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				final int val = frame.orderSlider_1.getValue();
				frame.orderSlider_2.setValue(val);
			}
		};

		frame.orderSlider_1.addChangeListener(dimOrderSlideListener);
		frame.orderSlider_1.addChangeListener(notAdvancedListener);
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
						System.out.println("k: " + frame.kSlider.getValue());

						updateCarts();
						updateSelectedStats();
						updateFigure();
						updateFileName();
					}
				});
			}
		});

		frame.selClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						selecteds = new HashSet<>();
						updateSelectedStats();
						updateFigure();
					}
				});
			}
		});

		frame.dimButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						if (e.getStateChange() == ItemEvent.SELECTED)
						{
							enableAdvanced();
						} else
						{
							disableAdvanced();
						}
					}
				});
			}
		});

		frame.filterButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						if (e.getStateChange() == ItemEvent.SELECTED)
						{
							filterByCluster = true;
							updateFigure();
						} else
						{
							filterByCluster = false;
							cartPane = null; // Reset the cart pane to prevent size
																// discrepancy
							frame.remove(sPane);
							updateFigure();
						}
					}
				});
			}
		});

		frame.clusterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						clusterTheSelected();
					}
				});
			}
		});

		frame.clearClustersButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						clearClusters();
					}
				});
			}
		});

		final ActionListener manuallySelected = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						manuallySelected();
					}
				});
			}
		};
		frame.manualSel.addActionListener(manuallySelected);
		frame.selectField.addActionListener(manuallySelected);

		frame.mineButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						runMineByItems();
					}
				});
			}
		});

		frame.mine2Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						runMineByDims();
					}
				});
			}
		});

		frame.selMineButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						runMineSelected();
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
				writeImage(img, frame.fileText.getText());
			}
		});

		updateCarts();
		updateFigure();
		updateFileName();

		frame.showTime();
	}

	private void runMineByItems()
	{
		final FreqCollector fc = new MaximalMinerCombiner.FreqCollector() {
			@Override
			public void foundFreq(Freq freq)
			{
				addAsCluster(freq.freqDims, new HashSet<>(freq.freqSet));
			}
		};
		Thread t = new Thread(new Runnable() {
			@Override
			public void run()
			{
				maximer.mineFor(k, Integer.valueOf(frame.minlenField.getText()), fc);
			}
		});
		t.start();
		// List<Freq> freqs = maximer.mineFor(k, a);

		// for (Freq freq : freqs)
		// {
		// addAsCluster(freq.freqDims, new HashSet<>(freq.freqSet));
		// }
	}

	private void runMineByDims()
	{
		List<Freq> freqs = dimer.mineFor(k, 100);

		for (Freq freq : freqs)
		{
			addAsCluster(freq.freqDims, new HashSet<>(freq.freqSet));
		}
	}

	private void runMineSelected()
	{
		Set<Integer> aMined = new HashSet<>(selecteds);
		Integer startDimIx = frame.orderSlider_1.getValue();
		List<Freq> freqs = maximer.mineFor(aMined, k, startDimIx);

		for (Freq freq : freqs)
		{
			addAsCluster(freq.freqDims, new HashSet<>(freq.freqSet));
		}
	}

	private void runFindKnees()
	{
		// CartiPointUnderTheLineFinder kneer = new CartiPointUnderTheLineFinder();
		// List<Freq> knees = kneer.mineFor(pathname, k);
		//
		// for (Freq knee : knees)
		// {
		// addAsCluster(knee.freqDims, new HashSet<>(knee.freqSet));
		// }
		// updateFigure();
	}

	private void clusterTheSelected()
	{
		addAsCluster(AllDims, selecteds);
		System.err.println("CLUSTER: " + selecteds.size() + ":" + selecteds);
		updateFigure();
	}

	private void clearClusters()
	{
		clustersToMark.clear();
		clusters.clear();
		clusterTableModel.fireTableDataChanged();
		updateFigure();
	}

	private void disableAdvanced()
	{
		frame.orderSlider_2.setEnabled(false);
		frame.dimsList.setEnabled(false);
		frame.orderSlider_2.removeChangeListener(dimOrderSlideListener);
		frame.dimsList.removeListSelectionListener(listListener);
		frame.orderSlider_1.addChangeListener(notAdvancedListener);
		notAdvancedListener.stateChanged(null);
		updateFigure();
		updateFileName();

	}

	private void enableAdvanced()
	{
		if (listListener == null)
		{
			listListener = new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e)
				{
					updateFigure();
					updateFileName();
				}
			};
		}
		frame.orderSlider_1.removeChangeListener(notAdvancedListener);
		frame.dimsList.addListSelectionListener(listListener);
		frame.orderSlider_2.addChangeListener(dimOrderSlideListener);

		frame.orderSlider_2.setEnabled(true);
		frame.dimsList.setEnabled(true);
	}

	private void updateCarts()
	{
		k = frame.kSlider.getValue();

		cartiDb = new CartifyDbInMemory(pathname, k);
		cartiDb.cartify();

		mat = new int[dims.length][][];

		PlainItemDB[] pDbs = cartiDb.getProjDbs();
		for (int dimIx = 0; dimIx < pDbs.length; dimIx++)
		{
			PlainItemDB pDb = pDbs[dimIx];
			int[][] newMatrix = new int[numOfObjects][numOfObjects];
			for (PlainItem item : pDb)
			{
				for (int tid = item.getTIDs().nextSetBit(0); tid >= 0; tid = item.getTIDs()
						.nextSetBit(tid + 1))
				{
					newMatrix[tid][item.getId()] += 2;
				}
			}

			mat[dimIx] = newMatrix;
		}
	}

	private void updateFigure()
	{
		List<Integer> dimsToShow = getDimsToShow();

		int[] id2LocMap_1 = getId2LocMap(frame.orderSlider_1.getValue());
		int[] id2LocMap_2 = getId2LocMap(frame.orderSlider_2.getValue());

		int[][] matrixToShow = translate(mat, dimsToShow, id2LocMap_1, id2LocMap_2);
		List<Layer> layers = new ArrayList<>();
		if (selecteds.size() != 0)
		{
			int[][] selectedsToShow = translate(cluster2Mat(selecteds), dimsToShow,
					id2LocMap_1, id2LocMap_2);
			layers.add(new Layer(selectedsToShow, Color.green));
		}

		Collection<Cluster> dimClusters = clustersToMark;
		if (dimClusters != null && dimClusters.size() > 0)
		{
			Iterator<Cluster> it = dimClusters.iterator();
			int[][] selectedsToShow = null;
			if (it.hasNext())
			{
				selectedsToShow = translate(cluster2Mat(it.next().objects), dimsToShow,
						id2LocMap_1, id2LocMap_2);
			}
			while (it.hasNext())
			{
				Set<Integer> cluster = it.next().objects;
				selectedsToShow = translateInto(selectedsToShow, cluster2Mat(cluster),
						dimsToShow, id2LocMap_1, id2LocMap_2);
			}
			layers.add(new Layer(selectedsToShow, Color.red));
		}

		if (cartPane == null)
		{
			cartPane = new CartPane(matrixToShow);
			cartPane.setLayers(layers);
			cartPane.addMouseListener(new SelectorMouseAdapter());
			sPane = new JScrollPane(cartPane);
			sPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
			frame.add(sPane, BorderLayout.CENTER);
		} else
		{
			cartPane.setLayers(layers);
			cartPane.updateMatrix(matrixToShow);
		}
		frame.validate();
		frame.repaint();
	}

	private List<Integer> getDimsToShow()
	{
		if (frame.dimButton.isSelected())
		{
			return frame.dimsList.getSelectedValuesList();
		}
		return Collections.singletonList(frame.orderSlider_1.getValue());
	}

	private int[][][] cluster2Mat(Set<Integer> toShow)
	{
		int[][][] selectedsLayer = new int[dims.length][numOfObjects][numOfObjects];
		for (int dimIx = 0; dimIx < dims.length; dimIx++)
		{
			for (int i : Utils.range(numOfObjects))
			// for (int i : toShow)
			{
				for (int j : toShow)
				{
					selectedsLayer[dimIx][i][j] += 1;
				}
			}
		}
		return selectedsLayer;
	}

	private int[][] translate(int[][][] mat, List<Integer> dimsToInclude,
			int[] id2LocMap_1, int[] id2LocMap_2)
	{
		int[][] matrixToShow = new int[numOfObjects][numOfObjects];
		matrixToShow = translateInto(matrixToShow, mat, dimsToInclude, id2LocMap_1,
				id2LocMap_2);
		return matrixToShow;
	}

	private int[][] translateInto(int[][] matrixToShow, int[][][] mat,
			List<Integer> dimsToInclude, int[] id2LocMap_1, int[] id2LocMap_2)
	{
		for (int dim : dimsToInclude)
		{
			for (int i = 0; i < mat[dim].length; i++)
			{
				for (int j = 0; j < mat[dim][i].length; j++)
				{
					matrixToShow[id2LocMap_2[i]][id2LocMap_1[j]] += mat[dim][i][j];
				}
			}
		}

		Collection<Cluster> dimClusters = clustersToMark;
		if (filterByCluster && dimClusters.size() == 1)
		{
			Set<Integer> firstCl = dimClusters.iterator().next().objects;
			int[][] newMat = new int[matrixToShow.length][firstCl.size()];

			for (int rowIx = 0; rowIx < matrixToShow.length; rowIx++)
			{
				int[] colsToShow = new int[firstCl.size()];
				int i = 0;
				for (int col : firstCl)
				{
					colsToShow[i++] = id2LocMap_2[col];
				}
				Arrays.sort(colsToShow);
				for (int j = 0; j < colsToShow.length; j++)
				{
					newMat[rowIx][j] = matrixToShow[rowIx][colsToShow[j]];
				}
			}

			matrixToShow = newMat;
		}
		return matrixToShow;
	}

	private int[] getId2LocMap(int dim)
	{
		if (id2LocMaps == null)
		{
			id2LocMaps = new int[dims.length][];
		}

		if (id2LocMaps[dim] == null)
		{
			id2LocMaps[dim] = MaximalMinerCombiner.getId2Ord(getOrd2Id(origData, dim));
		}
		return id2LocMaps[dim];
	}

	private void updateFileName()
	{
		frame.fileText.setText("/tmp/carti-k-" + frame.orderSlider_1.getValue()
				+ "_" + frame.orderSlider_2.getValue() + "-" + frame.kSlider.getValue()
				+ ".png");
		frame.fileText.invalidate();
	}

	protected void selectUntil(Point point)
	{
		int[] id2LocM = getId2LocMap(frame.orderSlider_2.getValue());

		int[] loc2IdM = new int[id2LocM.length];
		for (int i = 0; i < id2LocM.length; i++)
		{
			loc2IdM[id2LocM[i]] = i;
		}

		int x1 = min(selectionStart.x, point.x);
		int x2 = max(selectionStart.x, point.x);

		int[] cells = cartPane.getCells(x1, x2);
		cells[0] = max(0, cells[0]);
		cells[0] = min(loc2IdM.length, cells[0]);
		cells[1] = max(0, cells[1]);
		cells[1] = min(loc2IdM.length, cells[1]);

		Set<Integer> selectedObjects = new HashSet<>(cells[1] - cells[0]);
		for (int i = cells[0]; i < cells[1]; i++)
		{
			selectedObjects.add(loc2IdM[i]);
		}

		if (frame.selSelect.isSelected())
		{
			selecteds = selectedObjects;
		} else if (frame.selAnd.isSelected())
		{
			selecteds.retainAll(selectedObjects);
		} else if (frame.selOr.isSelected())
		{
			selecteds.addAll(selectedObjects);
		}

		System.out.println("Selected objects: " + selecteds);

		updateSelectedStats();
		updateFigure();
		updateFileName();
	}

	private void updateSelectedStats()
	{
		if (selecteds.size() == 0)
		{
			frame.hideInfoPane();
			return;
		}
		PlainItemDB[] dbs = cartiDb.getProjDbs();
		int[] dimSupports = new int[dbs.length];
		for (int dimIx = 0; dimIx < dbs.length; dimIx++)
		{
			Iterator<Integer> it = selecteds.iterator();
			Integer obj = it.next();
			BitSet tids = (BitSet) dbs[dimIx].get(obj).getTIDs().clone();
			while (it.hasNext())
			{
				obj = it.next();
				tids.and(dbs[dimIx].get(obj).getTIDs());
			}

			dimSupports[dimIx] = tids.cardinality();
		}
		frame.updateInfoPane(selecteds.size(), dimSupports, selecteds.toString());
	}

	private class SelectorMouseAdapter extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			selectionStart = e.getPoint();
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			selectUntil(e.getPoint());
		}
	}

	private void manuallySelected()
	{
		String t = frame.selectField.getText();
		String[] idArr = t.split(",");

		Set<Integer> manSel = new HashSet<>(idArr.length);
		for (String idStr : idArr)
		{
			manSel.add(Integer.valueOf(idStr.trim()));
		}
		selecteds = manSel;

		updateSelectedStats();
		updateFigure();
		updateFileName();
	}

	private void addAsCluster(Collection<Integer> dims, Set<Integer> objects)
	{
		final Cluster cl = new Cluster(new HashSet<>(dims), objects);
		clusters.add(cl);

		if (clusterTableModel == null)
		{
			clusterTableModel = new TableModel(clusters);
			frame.showClusterTable(clusterTableModel);
			frame.clusterTable.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent e)
						{
							if (e.getValueIsAdjusting())
							{
								return;
							}
							clusterSelected();
						}
					});
		} else
		{
			clusterTableModel.fireTableDataChanged();
		}

	}

	private void clusterSelected()
	{
		int[] rows = frame.clusterTable.getSelectedRows();
		if (Arrays.equals(rows, selectedClusterRows))
		{
			return;
		}
		clustersToMark.clear();
		selectedClusterRows = rows;
		for (int rowIx : rows)
		{
			Cluster cl = clusters.get(rowIx);

			clustersToMark.add(cl);
		}
		updateFigure();
	}

	public static void writeImage(BufferedImage img, final String file)
	{
		try
		{
			if (ImageIO.write(img, "png", new File(file)))
			{
				System.out.println("-- saved as " + file);
			}
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	static class TableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 3337994276952657633L;
		private List<Cluster> clusters;
		private String[] names;

		public TableModel(List<Cluster> clusters)
		{
			this.clusters = clusters;
			this.names = new String[]
			{ "id", "Size", "Dims", "Objects" };
		}

		@Override
		public int getRowCount()
		{
			return clusters.size();
		}

		@Override
		public int getColumnCount()
		{
			return 4;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			switch (columnIndex)
			{
			case 0:
				return rowIndex;
			case 1:
				return clusters.get(rowIndex).objects.size();
			case 2:
				return clusters.get(rowIndex).dims.toString();
			case 3:
				return clusters.get(rowIndex).objects.toString();
			default:
				return "";
			}
		}

		@Override
		public String getColumnName(int column)
		{
			return names[column];
		}
	}

	static class Cluster
	{
		Set<Integer> dims;
		Set<Integer> objects;

		public Cluster(Set<Integer> dims, Set<Integer> objects)
		{
			this.dims = dims;
			this.objects = objects;
		}
	}
}
