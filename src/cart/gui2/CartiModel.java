package cart.gui2;

import static cart.maximizer.MaximalMinerCombiner.getOrd2Id;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import mime.plain.PlainItem;
import mime.plain.PlainItemDB;

import cart.cartifier.CartifyDbInMemory;
import cart.cartifier.Pair;
import cart.maximizer.MaximalMinerCombiner;
import cart.maximizer.OneDCartifier;

public class CartiModel 
{
	private String filePath;
	private List<Integer> allDims;
	private double[][] dims;
	private Pair[][] origData;
	private int numObjects;
	private int[][] id2LocMaps;
	private int[][][] mat;
	private CartifyDbInMemory cartiDb;
	
	public void init(final String filePath) {
		this.filePath = filePath;
		
		ArrayList<double[]> data;
		try
		{
			data = OneDCartifier.readData(filePath);
			origData = OneDCartifier.toPairs(data);
		} catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		dims = OneDCartifier.transpose(data);
		allDims = new ArrayList<>();
		for (int i = 0; i < dims.length; i++)
		{
			allDims.add(i);
		}
		allDims = Collections.unmodifiableList(allDims);
		
		numObjects = data.size();
		
		updateCarts(1);
	}
	

	public int getNumObjects() {
		return numObjects;
	}
	
	public List<String> getAllDimsStringList() {
		List<String> stringList = new ArrayList<String>();
		for (int i = 0; i < allDims.size(); i++) {
			stringList.add(Integer.toString(allDims.get(i)));
		}
		
		return stringList;
	}
	
	public int[][] getMatrixToShow(List<Integer> dimsToShow, int order_1, int order_2) {
		int[][] matrixToShow;	

		int[] id2LocMap_1 = getId2LocMap(order_1);
		int[] id2LocMap_2 = getId2LocMap(order_2);

		matrixToShow = translate(mat, dimsToShow, id2LocMap_1, id2LocMap_2);
		
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
	
	private int[][] translate(int[][][] mat, List<Integer> dimsToInclude,
			int[] id2LocMap_1, int[] id2LocMap_2) {
		int[][] matrixToShow = new int[numObjects][numObjects];
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

		return matrixToShow;
	}
	
	public void updateCarts(int k) {
		cartiDb = new CartifyDbInMemory(filePath, k);
		cartiDb.cartify();

		mat = new int[dims.length][][];

		PlainItemDB[] pDbs = cartiDb.getProjDbs();
		for (int dimIx = 0; dimIx < pDbs.length; dimIx++)
		{
			PlainItemDB pDb = pDbs[dimIx];
			int[][] newMatrix = new int[numObjects][numObjects];
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
}
