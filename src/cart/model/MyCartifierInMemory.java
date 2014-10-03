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

			for (int neighbor = 0; neighbor < Math.min(cart.length, k); neighbor++) {
				itemDb.get(cart[neighbor].ix).getTIDs().set(itemIx);
			}
		}
	}
}
