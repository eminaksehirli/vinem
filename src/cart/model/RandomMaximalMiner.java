/* Random Maximal Miner
 *  
 * Copyright (C) 2011 - 2013  Emin Aksehirli, Sandy Moens
 *
 * This file is part of MIME Framework - http://adrem.ua.ac.be/mime . 
 * 
 * MIME is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cart.model;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.Math.ceil;
import static mime.tool.Utils.partition;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import mime.plain.PlainItem;
import mime.plain.PlainItemSet;
import mime.plain.PlainTransactionDB;
import mime.plain.measure.itemset.ItemSetMeasure;
import mime.plain.measure.itemset.SupportMeasure;
import mime.tool.Utils;

import com.google.common.collect.Lists;

/**
 * Implements a random maximal frequent itemset (mfis) miner that can use
 * different itemset measures to compute a distribution on the possible mfis the
 * can still be generated.
 * 
 * @author Sandy Moens
 * @author Emin Aksehirli
 * 
 */
public class RandomMaximalMiner {
	private static final int DefaultSeedSize = 100;

	// private static final int N_THREADS = 4;

	public static class EqualMeasure implements ItemSetMeasure {
		@Override
		public String getName() {
			return "Equal";
		}

		@Override
		public double evaluate(PlainItemSet itemSet) {
			return 1.0;
		}

		@Override
		public double evaluate(PlainItemSet itemSet, PlainItem extension) {
			return 1.0;
		}
	}

	private final List<PlainItem> allItems;
	private int minSize;
	private ItemSetMeasure approximationMeasure;
	private ItemSetMeasure pruningMeasure;

	public static Random random = new Random();
	private PlainItemSet theItemset;
	private int minSup;
	private List<PlainItem> items;
	private LinkedList<PlainItem> seedSet;
	private static List<PlainItem> AllItems;

	public static List<PlainItemSet> runParallel(
			Iterable<? extends PlainItem> items, final int minSup,
			int numOfItemSets) {
		return runParallel(items, minSup, numOfItemSets, DefaultSeedSize);
	}

	public static List<PlainItemSet> runParallel(
			Iterable<? extends PlainItem> items, final int minSup,
			int numOfItemSets, final int seedSize) {

		AllItems = Lists.newArrayList(items);

		int numOfProcessors = (int) ceil(0.75 * Runtime.getRuntime()
				.availableProcessors());

		ExecutorService pool = Executors.newFixedThreadPool(numOfProcessors);

		final int[] shareSizes = partition(numOfItemSets, numOfProcessors);

		List<Callable<List<PlainItemSet>>> tasks = newArrayList();
		for (int i = 0; i < numOfProcessors; i++) {
			final int numberOfItemSets = shareSizes[i];
			tasks.add(new Callable<List<PlainItemSet>>() {
				@Override
				public List<PlainItemSet> call() throws Exception {
					RandomMaximalMiner miner = new RandomMaximalMiner();
					SupportMeasure measure = new SupportMeasure();
					miner.setApproximationMeasure(measure);
					miner.setPruningMeasure(measure);
					miner.setMinSize(seedSize);
					return miner.run(minSup, numberOfItemSets);
				}
			});
		}

		try {
			List<PlainItemSet> fises = newArrayListWithCapacity(numOfItemSets);
			for (Future<List<PlainItemSet>> future : pool.invokeAll(tasks)) {
				fises.addAll(future.get());
			}

			pool.shutdown();
			return fises;

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		pool.shutdown();

		return Collections.emptyList();
	}

	private RandomMaximalMiner() {
		allItems = AllItems;
	}

	public RandomMaximalMiner(PlainTransactionDB db) {
		this(db.getItemDB());
	}

	public RandomMaximalMiner(Iterable<? extends PlainItem> items) {
		minSize = 0;
		allItems = Lists.newArrayList(items);
	}

	/**
	 * Sets the measure that is used to create a distribution on the different
	 * branches that need to be traversed
	 * 
	 * @param measure
	 *            itemset measure that is used to score different branches
	 */
	public void setApproximationMeasure(ItemSetMeasure measure) {
		this.approximationMeasure = measure;
	}

	/**
	 * Sets the measure that is used to create a distribution on the different
	 * branches that need to be traversed
	 * 
	 * @param measure
	 *            itemset measure that is used to score different branches
	 */
	public void setPruningMeasure(ItemSetMeasure measure) {
		this.pruningMeasure = measure;
	}

	/**
	 * Sets the minimum size of an itemset that is to be found
	 * 
	 * @param minSize
	 *            the mimimum size of an itemset
	 */
	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	/**
	 * Prunes a list of items based on the minimal support
	 * 
	 * @param items
	 *            the list of items that must be pruned
	 * @param minSup
	 *            the minimal support threshold that must be met
	 * @return a new list of items that is pruned based on minimal support
	 */
	private List<PlainItem> pruneItemsOnMinsup() {

		List<PlainItem> items = newLinkedList(allItems);

		Iterator<PlainItem> it = items.iterator();
		while (it.hasNext()) {
			PlainItem item = it.next();
			if (pruningMeasure.evaluate(new PlainItemSet(item)) < minSup) {
				it.remove();
			}
		}

		return items;
	}

	/**
	 * Prunes a list of items based on the minimal support
	 * 
	 * @param items
	 *            the list of items that must be pruned
	 * @param itemset
	 *            the itemset to which each of the individual is added prior to
	 *            computing support
	 * 
	 * @return a new list of items that is pruned based on minimal support
	 */
	private void pruneItemsForTheItemset() {
		Iterator<PlainItem> it = items.iterator();
		while (it.hasNext()) {
			PlainItem item = it.next();
			if (pruningMeasure.evaluate(theItemset, item) < minSup) {
				it.remove();
			}
		}
	}

	/**
	 * Runs the miner with specified minimal support threshold and generates the
	 * specified number of itemsets.
	 * 
	 * @param minSup
	 *            minimal support of the maximal frequent itemsets
	 * @param numberOfItemsets
	 *            number of itemsets that need to be mined
	 * @return a list of maximal frequent itemsets
	 */
	public List<PlainItemSet> run(int minSup, int numberOfItemsets) {
		this.minSup = minSup;

		List<PlainItem> prunedItems = pruneItemsOnMinsup();

		if (prunedItems.isEmpty() || prunedItems.size() < minSize) {
			return new LinkedList<PlainItemSet>();
		}

		seedSet = newLinkedList();

		int numberOfTries = 0;
		List<PlainItemSet> itemsets = newLinkedList();
		do {
			items = newArrayList(prunedItems);
			theItemset = getSeed();
			extendItemset();

			if (theItemset.size() != 0 && theItemset.size() >= minSize) {
				itemsets.add(theItemset);
				// printADot();
			}
			// System.out.println(theItemset.size() + " : " + theItemset);
			seedSet.removeAll(theItemset);
			numberOfTries++;
		} while (itemsets.size() < numberOfItemsets
				&& numberOfTries <= numberOfItemsets * 10);

		return itemsets;
	}

	private PlainItemSet getSeed() {
		if (seedSet.isEmpty()) {
			seedSet = newLinkedList(items);
		}
		// return new PlainItemSet(items.get(random.nextInt(5)));
		return new PlainItemSet(seedSet.get(random.nextInt(seedSet.size())));
	}

	public PlainItemSet buildMaximalFor(PlainItemSet itemset, int minSup) {
		theItemset = new PlainItemSet(itemset);
		this.minSup = minSup;
		items = newArrayList(allItems);

		extendItemset();

		return theItemset;
	}

	private void extendItemset() {
		pruneItemsForTheItemset();
		while (items.size() > 0) {
			PlainItem nextItem = getNextItem();
			theItemset.add(nextItem);
			items.remove(nextItem);
			pruneItemsForTheItemset();
		}
	}

	/**
	 * Computes the normalized distribution map of the itemsets based on the
	 * itemset measure
	 * 
	 * @param items
	 *            the list of items
	 * 
	 * @return map containing the different measure values for the items
	 */
	private Map<PlainItem, Double> generateDistributionMap() {
		Map<PlainItem, Double> values = new HashMap<PlainItem, Double>();

		double normalizationFactor = 0;
		for (PlainItem item : items) {
			double value = approximationMeasure.evaluate(theItemset, item);
			normalizationFactor += value;
			values.put(item, value);
		}

		if (normalizationFactor == 0) {
			return values;
		}

		for (Entry<PlainItem, Double> entry : values.entrySet()) {
			entry.setValue(entry.getValue() / normalizationFactor);
		}

		return values;
	}

	/**
	 * Gets an array of cumulated values sorted according to the ordering of
	 * items in the list
	 * 
	 * @param values
	 *            map containing the values of the probabilities of the
	 *            individual items
	 * @param items
	 *            sorted list of items
	 * @return array of double values containing the values of the indivual
	 *         items sorted according to the list of items
	 */
	private double[] getCumulatedValuesAsArray(Map<PlainItem, Double> values) {
		double[] arrayValues = new double[items.size()];

		int ix = 0;
		double cum = 0;
		for (PlainItem item : items) {
			arrayValues[ix++] = (cum += values.get(item));
		}
		return arrayValues;
	}

	/**
	 * Gives the next item based on the distribution specified by the itemset
	 * measure
	 * 
	 * @param items
	 *            the list of items that have to be evaluated and from which the
	 *            next item is picked
	 * 
	 * @return a new item from the list of items
	 */
	private PlainItem getNextItem() {
		if (items.size() == 1) {
			return items.get(0);
		}

		final Map<PlainItem, Double> values = generateDistributionMap();

		Collections.sort(items, new Comparator<PlainItem>() {
			@Override
			public int compare(PlainItem i1, PlainItem i2) {
				return values.get(i2).compareTo(values.get(i1));
			}
		});

		// all values are zero - caused by using different measures
		if (values.get(items.get(items.size() - 1)) == 0) {
			return items.get(random.nextInt(items.size()));
		}

		double[] arrayValues = getCumulatedValuesAsArray(values);

		int itemIx = Utils.logIndexSearch(arrayValues, random.nextDouble());

		if (itemIx == items.size()) {
			System.out.println(itemIx);
		}

		return items.get(itemIx);
	}

	private static int dotCount = 0;

	private synchronized static void printADot() {
		System.out.print(".");
		dotCount++;
		if (dotCount % 50 == 0) {
			System.out.println();
		}
	}
}