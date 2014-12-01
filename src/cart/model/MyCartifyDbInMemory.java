package cart.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import mime.plain.PlainItem;
import mime.plain.PlainItemDB;
import cart.cartifier.CartifierInMemory;
import cart.gui2.Dissimilarity;
import cart.gui2.OneDimDistMeasure;

public class MyCartifyDbInMemory {

	private int k;

	private List<Dissimilarity> dissimilarities;
	private List<double[]> originalDatabase;

	private List<PlainItemDB> projectedDbs;
	public PlainItemDB completeDb;

	private PrintWriter log;

	public MyCartifyDbInMemory(List<double[]> data, int k,
			List<Dissimilarity> dissimilarities) {
		originalDatabase = data;
		this.k = k;
		this.dissimilarities = new ArrayList<Dissimilarity>(dissimilarities);
		try {
			log = new PrintWriter(File.createTempFile("cartify-log-", ".txt"));
		} catch (IOException e) {
			e.printStackTrace();
			log = new PrintWriter(System.out);
		}
	}

	public void cartify() {
		try {
			createCarts();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void addDistMeasure(Dissimilarity measure) {
		dissimilarities.add(measure);
		projectedDbs.add(null); // space to be set

		ExecutorService executor = Executors.newFixedThreadPool(4);
		executor.execute(new RunnableImplementation(dissimilarities.size() - 1));
		try {
			executor.shutdown();
			executor.awaitTermination(1000, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
			executor.shutdownNow();
		}
	}

	private final class RunnableImplementation implements Runnable {
		private Dissimilarity dissimilarity;
		private int distMeasureIx;

		private RunnableImplementation(int distMeasureIx) {
			this.distMeasureIx = distMeasureIx;
			this.dissimilarity = dissimilarities.get(distMeasureIx);
		}

		@Override
		public void run() {
			log("Creating cart for distMeasure #" + distMeasureIx);

			if (dissimilarity.getClass().equals(OneDimDistMeasure.class)) {
				// use carti-bander Cartifier for 1-dimensional dist measures
				CartifierInMemory cartifier = new CartifierInMemory(originalDatabase);
				int[] dimension = { distMeasureIx };
				cartifier.cartifyNumeric(dimension, k);
				projectedDbs.set(distMeasureIx, cartifier.itemDb);
			} else {
				// use MyCartifier for other dist measures
				MyCartifierInMemory cartifier = new MyCartifierInMemory(
						originalDatabase);
				cartifier.cartifyNumeric(dissimilarity, k);
				projectedDbs.set(distMeasureIx, cartifier.itemDb);
			}
		}
	}

	private void log(final String msg) {
		log.println(msg);
		log.flush();
	}

	private void createCarts() {
		projectedDbs = new ArrayList<PlainItemDB>(dissimilarities.size());
		// make space for the projectedDbs to be set
		while (projectedDbs.size() < dissimilarities.size()) {
			projectedDbs.add(null);
		}

		ExecutorService executor = Executors.newFixedThreadPool(4);

		for (int i = 0; i < dissimilarities.size(); i++) {
			executor.execute(new RunnableImplementation(i));
		}

		try {
			executor.shutdown();
			executor.awaitTermination(1000, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
			executor.shutdownNow();
		}
	}

	public List<PlainItemDB> getProjDbs() {
		return projectedDbs;
	}

	public void print(String fileName) {
		PrintWriter wr;
		try {
			wr = new PrintWriter(new File(fileName));
			for (PlainItem item : completeDb) {
				wr.println(item.getId() + ":" + item.getTIDs());
			}
			wr.flush();
			wr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
