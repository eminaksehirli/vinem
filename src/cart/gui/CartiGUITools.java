package cart.gui;

import mime.tool.Utils;
import cart.maximizer.OneDCartifier;

public class CartiGUITools {
	public static int[][][] getAllCarts(double[][] dims, int k, boolean extendDim) {
		int[][][] carts = new int[dims.length][dims[0].length][k];
		for (int dimIx = 0; dimIx < dims.length; dimIx++) {
			int[] cartStarts = OneDCartifier
					.findCartStarts(dims[dimIx], k, extendDim);
			for (int cartIx = 0; cartIx < cartStarts.length; cartIx++) {
				int cartStart = cartStarts[cartIx];
				carts[dimIx][cartIx] = Utils.range(cartStart, cartStart + k);
			}
		}
		return carts;
	}

	public static int[][][] createConfMat(final int[][][] carts) {
		int[][][] cumMat = new int[carts.length][][];

		for (int dimIx = 0; dimIx < carts.length; dimIx++) {
			int[][] dimCarts = carts[dimIx];
			int[][] mat = new int[dimCarts.length][dimCarts.length];
			for (int i = 0; i < dimCarts.length; i++) {
				for (int j = 0; j < dimCarts[i].length; j++) {
					mat[i][dimCarts[i][j]] = 1;
				}
			}
			cumMat[dimIx] = mat;
		}
		return cumMat;
	}
}
