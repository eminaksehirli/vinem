package cart.model;

import java.util.Arrays;
import java.util.List;

import mime.plain.PlainItemDB;
import cart.cartifier.Pair;
import cart.gui2.DistMeasure;

public class MyCartifierInMemory {

	protected List<double[]> db;
	public PlainItemDB itemDb;

	public MyCartifierInMemory(List<double[]> db) {
		this.db = db;
	}

	public void cartifyNumeric(DistMeasure distMeasure, int k) {

		int numOfItems = db.size();
		itemDb = new PlainItemDB();

		for (int itemIx = 0; itemIx < numOfItems; itemIx++) {
			double[] object_i = db.get(itemIx);
			Pair[] cart = new Pair[numOfItems];
			for (int j = 0; j < numOfItems; j++) {
				double[] object_j = db.get(j);
				double distance = distMeasure.calculateDistance(object_i,
						object_j);

				cart[j] = new Pair(distance, j);
			}

			Arrays.sort(cart);

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
}
