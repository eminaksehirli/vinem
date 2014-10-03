package cart.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import mime.plain.PlainItem;
import mime.plain.PlainItemDB;
import cart.gui2.DistMeasure;

public class MyCartifyDbInMemory {

	private final class RunnableImplementation implements Runnable {
		private DistMeasure distMeasure;
		private int distMeasureIx;

		private RunnableImplementation(int distMeasureIx) {
			this.distMeasureIx = distMeasureIx;
			this.distMeasure = distMeasures.get(distMeasureIx);
		}

		@Override
		public void run() {
			log("Creating cart for distMeasure #" + distMeasureIx);
			MyCartifierInMemory cartifier = new MyCartifierInMemory(
					originalDatabase);
			cartifier.cartifyNumeric(distMeasure, k);
			projectedDbs[distMeasureIx] = cartifier.itemDb;
		}
	}

	private int k;

	private List<DistMeasure> distMeasures;
	private List<double[]> originalDatabase;
	private String originalDatabaseFilename;

	private PlainItemDB[] projectedDbs;
	public PlainItemDB completeDb;

	private PrintWriter log;

	public MyCartifyDbInMemory(String databaseFileName, int k,
			List<DistMeasure> distMeasures) {
		originalDatabaseFilename = databaseFileName;
		this.k = k;
		this.distMeasures = new ArrayList<DistMeasure>(distMeasures);
		try {
			log = new PrintWriter(File.createTempFile("cartify-log-", ".txt"));
		} catch (IOException e) {
			e.printStackTrace();
			log = new PrintWriter(System.out);
		}
		try {
			readOriginalDatabase();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public PlainItemDB cartify() {
		try {
			createCarts();

			return mergeToAFinalTDb(k);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private PlainItemDB mergeToAFinalTDb(int k) {
		log("Merging to final db....");
		completeDb = new PlainItemDB();

		Set<PlainItem> allTheItems = new HashSet<PlainItem>();
		int[] bitSetSizes = new int[originalDatabase.size()];
		for (PlainItemDB projectedDb : projectedDbs) {
			for (PlainItem item : projectedDb) {
				allTheItems.add(item);
				bitSetSizes[item.getId()] += item.getTIDs().size() >> 6;
			}
		}

		BitSet allTransactions = new BitSet();
		for (PlainItem item : allTheItems) {
			long[] tidsArr = new long[bitSetSizes[item.getId()]];
			int ofset = 0;
			for (PlainItemDB projectedDb : projectedDbs) {
				long[] arr = projectedDb.get(item.getId()).getTIDs()
						.toLongArray();
				for (int i = 0; i < arr.length; i++) {
					tidsArr[ofset + i] = arr[i];
				}
				ofset += arr.length;
			}
			PlainItem bigItem = completeDb.get(item.getId(),
					BitSet.valueOf(tidsArr));
			allTransactions.or(bigItem.getTIDs());
		}

		// completeDb.transactionCounter = allTransactions.cardinality();
		log("Final db is created.");
		return completeDb;
	}

	private void log(final String msg) {
		log.println(msg);
		log.flush();
	}

	private void readOriginalDatabase() throws FileNotFoundException {
		originalDatabase = new ArrayList<double[]>();

		log("Reading the file: " + originalDatabaseFilename);
		Scanner sc = new Scanner(new File(originalDatabaseFilename));
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String delimiter = " ";
			String[] lineArr = line.split(delimiter);

			double[] thisRow = new double[lineArr.length];
			for (int i = 0; i < lineArr.length; i++) {
				thisRow[i] = Double.parseDouble(lineArr[i]);
			}
			originalDatabase.add(thisRow);
		}
		sc.close();

	}

	private void createCarts() {
		projectedDbs = new PlainItemDB[distMeasures.size()];
		ExecutorService executor = Executors.newFixedThreadPool(4);

		for (int i = 0; i < distMeasures.size(); i++) {
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

	public PlainItemDB[] getProjDbs() {
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
