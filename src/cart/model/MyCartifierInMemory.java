package cart.model;

import java.util.Arrays;
import java.util.List;

import mime.plain.PlainItemDB;
import cart.cartifier.Pair;
import cart.gui2.Dissimilarity;

public class MyCartifierInMemory {

	protected double[][] db;
	public PlainItemDB itemDb;

	public MyCartifierInMemory(List<double[]> db) {
		this.db = db.toArray(new double[0][]);
	}

	public void cartifyNumeric(Dissimilarity dissimilarity, int k) {

		itemDb = new PlainItemDB();

		for (int itemIx = 0; itemIx < db.length; itemIx++) {
			Pair[] cart = cartOf(itemIx, dissimilarity);

			int neighbor = 0;
			while (neighbor < Math.min(cart.length, k)) {
				itemDb.get(cart[neighbor].ix).getTIDs().set(itemIx);
				neighbor++;
			}

			// don't cut off cart when (distance to neighbor == distance to
			// neighbor-1)
			while ((neighbor < cart.length)
					&& (cart[neighbor].v == cart[neighbor - 1].v)) {
				itemDb.get(cart[neighbor].ix).getTIDs().set(itemIx);
				neighbor++;
			}
		}
	}

	public Pair[] cartOf(int itemIx, Dissimilarity dissimilarity) {
		double[] obj_i = db[itemIx];
		Pair[] cart = new Pair[db.length];
		for (int j = 0; j < db.length; j++) {
			double[] obj_j = db[j];
			double distance = dissimilarity.between(obj_i, obj_j);

			cart[j] = new Pair(distance, j);
		}

		Arrays.sort(cart);
		return cart;
	}
}
