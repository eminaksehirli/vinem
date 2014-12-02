package cart.model;

import static cart.maximizer.MaximalMinerCombiner.getOrd2Id;
import static java.util.Collections.singleton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import mime.plain.PlainItem;
import mime.plain.PlainItemDB;
import mime.plain.PlainItemSet;
import cart.cartifier.CartifyDb;
import cart.cartifier.CartifyKNNDb;
import cart.cartifier.CartifyRadiusDb;
import cart.cartifier.Dissimilarity;
import cart.cartifier.OneDimDissimilarity;
import cart.cartifier.Pair;
import cart.controller.Neighborhood;
import cart.gui2.Cluster;
import cart.io.InputFile;
import cart.maximizer.Freq;
import cart.maximizer.ItemsetMaximalMiner;
import cart.maximizer.MaximalMinerCombiner;
import cart.maximizer.OneDCartifier;

/**
 * The main model class.
 * 
 * @author Detlev
 * @author Aksehirli
 * 
 */

public class CartiModel {
	private int numObjects;
	private int numDims;
	private int k;
	private int orderDim;
	private int clusterIdCount;
	private Set<Integer> dims;
	private Pair[][] origData;
	private CartifyDb cartiDb;
	private Set<Integer> filtereds;
	private Set<Integer> selecteds;
	private Map<Integer, Cluster> clustersMap;
	private Set<Integer> clustersToShow;
	private int[][] objId2LocMaps;
	private int[][] loc2ObjIdMaps;
	private Stack<Memento> savedStates;
	private List<Dissimilarity> dissimilarities;
	private int selectedDistMeasureId;
	private ArrayList<double[]> data;
	private int[] byObjId2LocMap;
	private int[] byObjLoc2IdMap;
	private ItemsetMaximalMiner maximer;
	private InputFile inputFile;
	public String[] columnNames;
	public String[] rowNames;
	private Obj[] objects;
	private Neighborhood neighborhood = Neighborhood.KNN;
	private double eps;
	private double maxEps = -1;

	public CartiModel(InputFile inputFile) {
		this.inputFile = inputFile;
		this.k = 1;
		this.orderDim = 0;
		this.clusterIdCount = 0;
		this.filtereds = new HashSet<Integer>();
		this.selecteds = new HashSet<Integer>();
		this.savedStates = new Stack<Memento>();
		this.clustersMap = new TreeMap<Integer, Cluster>();
		this.clustersToShow = new HashSet<Integer>();
		this.dissimilarities = new ArrayList<Dissimilarity>();
		this.selectedDistMeasureId = 0;

		maximer = new ItemsetMaximalMiner(inputFile);
	}

	/**
	 * Initializes the model.
	 */
	public void init() {
		try {
			data = inputFile.getData();
			origData = OneDCartifier.toPairs(data);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		numObjects = data.size();
		numDims = OneDCartifier.transpose(data).length;

		if (inputFile.colsHasNames) {
			columnNames = inputFile.columnNames;
		} else {
			columnNames = rangeAsArr(numDims);
		}

		if (inputFile.rowsHasNames) {
			rowNames = inputFile.rowNames;
		} else {
			rowNames = rangeAsArr(numObjects);
		}

		objects = new Obj[numObjects];
		for (int i = 0; i < rowNames.length; i++) {
			objects[i] = new Obj(i, rowNames[i]);
		}

		dims = new TreeSet<Integer>();
		for (int i = 0; i < numDims; i++) {
			dims.add(i);
			dissimilarities.add(new OneDimDissimilarity(i));
		}
		dims = Collections.unmodifiableSet(dims);

		initMaps();
		updateCartiDb();
	}

	/**
	 * Updates the id2loc and loc2id maps, taking filtereds into account.
	 */
	private void updateMaps() {
		initMaps();

		for (int dimId = 0; dimId < numDims; dimId++) {
			int[] loc2id = new int[loc2ObjIdMaps[dimId].length - filtereds.size()];

			int putLoc = 0;
			for (int loc = 0; loc < loc2ObjIdMaps[dimId].length; loc++) {
				if (!filtereds.contains(loc2ObjIdMaps[dimId][loc])) {
					loc2id[putLoc] = loc2ObjIdMaps[dimId][loc];
					objId2Loc(dimId)[loc2ObjIdMaps[dimId][loc]] = putLoc;
					putLoc++;
				} else {
					objId2Loc(dimId)[loc2ObjIdMaps[dimId][loc]] = -1;
				}
			}

			loc2ObjIdMaps[dimId] = loc2id;
		}
	}

	private void updateCartiDb() {
		cartiDb = newCartiDb();
		cartiDb.cartify();
	}

	private CartifyDb newCartiDb() {
		switch (neighborhood) {
		case KNN:
			return new CartifyKNNDb(inputFile, dissimilarities, k);
		case Radius:
			return new CartifyRadiusDb(inputFile, dissimilarities, eps);
		default:
			return cartiDb;
		}
	}

	private void addDistMeasureToCartiDb(Dissimilarity measure) {
		cartiDb.addMeasure(measure);
	}

	/**
	 * Initializes the id2loc and loc2id maps.
	 */
	private void initMaps() {
		objId2LocMaps = new int[numDims][];
		loc2ObjIdMaps = new int[numDims][];

		for (int dimId = 0; dimId < numDims; dimId++) {
			objId2LocMaps[dimId] = MaximalMinerCombiner.getId2Ord(getOrd2Id(origData,
					dimId));

			loc2ObjIdMaps[dimId] = new int[objId2Loc(dimId).length];
			for (int i = 0; i < objId2Loc(dimId).length; i++) {
				loc2ObjIdMaps[dimId][objId2Loc(dimId)[i]] = i;
			}
		}
	}

	/**
	 * @return A 2d matrix containing 0s and 1s, where a 1 means we need to colour
	 *         that spot.
	 */
	public int[][] getMatrixToShow() {
		int[][] matrixToShow = new int[numObjects - filtereds.size()][numObjects
				- filtereds.size()];

		PlainItemDB[] pDbs = cartiDb.getProjDbs();
		PlainItemDB pDb = pDbs[selectedDistMeasureId];

		// loop over each item
		for (PlainItem item : pDb) {
			if (!filtereds.contains(item.getId())) {

				// loop over each cart in which this object occurs
				for (int objId = item.getTIDs().nextSetBit(0); objId >= 0; objId = item
						.getTIDs().nextSetBit(objId + 1)) {
					if (!filtereds.contains(objId)) {
						matrixToShow[objId2Loc(orderDim)[objId]][objId2Loc(orderDim)[item
								.getId()]] = 1;
					}
				}
			}
		}

		return matrixToShow;
	}

	public int getNumObjects() {
		return numObjects;
	}

	public int getNumDims() {
		return numDims;
	}

	public Set<Integer> getDims() {
		return dims;
	}

	/**
	 * @return A list of all object Ids except those which are filtered, ordered
	 *         for a given dimension.
	 */
	public List<Obj> getOrderedObjList() {
		List<Obj> orderedObjs = new ArrayList<>();

		for (int i = 0; i < loc2ObjId().length; i++) {
			orderedObjs.add(getObj(loc2ObjId()[i]));
		}

		return orderedObjs;
	}

	public List<Dissimilarity> getDistMeasures() {
		return dissimilarities;
	}

	public Dissimilarity getSelectedDistMeasure() {
		return dissimilarities.get(selectedDistMeasureId);
	}

	/**
	 * @return The projection db for the selected dist measure.
	 */
	public PlainItemDB getSelectedProjDb() {
		return cartiDb.getProjDbs()[selectedDistMeasureId];
	}

	/**
	 * @return The projection db, containing only the carts of selected objects,
	 *         for the selected dist measure.
	 */
	public PlainItemDB getSelectedProjDbOnlySelected() {
		PlainItemDB db = getSelectedProjDb();

		PlainItemDB onlySelected = new PlainItemDB();

		for (PlainItem item : db) {
			if (selecteds.contains(item.getId())) {
				onlySelected.get(item.getId(), item.getTIDs());
			}
		}

		return onlySelected;
	}

	/**
	 * @param minSup
	 *          the minSup threshold
	 * @return The object ids of each object with a support < minSup in the
	 *         selected distMeasure.
	 */
	public int findNoiseObjsInSelDistMeas(int minSup) {
		return findNoiseIn(selectedDistMeasureId, minSup);
	}

	/**
	 * @param minSup
	 *          the minSup threshold
	 * @return
	 * @return The object ids of each object with a support < minSup in the
	 *         selected distMeasure.
	 */
	public int findNoiseObjsInEachProj(int minSup) {
		int total = 0;
		for (int measureId = 0; measureId < dissimilarities.size(); measureId++) {
			total += findNoiseIn(measureId, minSup);
		}
		return total;
	}

	private int findNoiseIn(final int measureId, int minSup) {
		List<Obj> noiseObjects = new ArrayList<>();

		PlainItemDB pDb = getSelectedProjDb();

		// loop over each item
		for (PlainItem item : pDb) {
			if ((!filtereds.contains(item.getId()))
					&& (item.getTIDs().cardinality() < minSup)) {
				noiseObjects.add(getObj(item.getId()));
			}
		}

		if (!noiseObjects.isEmpty()) {
			addCluster(new Cluster(noiseObjects, singleton(measureId)));
		}
		return noiseObjects.size();
	}

	/**
	 * @param minSup
	 *          the minSup threshold
	 * @return The object ids of each object with a support < minSup in every
	 *         distMeasure.
	 */
	public int findNoiseGlobally(int minSup) {
		Set<Integer> noiseObjects = new HashSet<>();

		// add each object to noiseObjects to remove them later
		PlainItemDB[] pDbs = cartiDb.getProjDbs();
		for (PlainItem item : pDbs[0]) {
			if (!filtereds.contains(item.getId())) {
				noiseObjects.add(item.getId());
			}
		}

		// remove objects where support >= minSup
		for (PlainItemDB pDb : pDbs) {
			for (PlainItem item : pDb) {
				if (item.getTIDs().cardinality() >= minSup) {
					noiseObjects.remove(item.getId());
				}
			}
		}

		addCluster(new Cluster(ids2Objs(noiseObjects), new HashSet<Integer>()));

		return noiseObjects.size();
	}

	/**
	 * @param objIds
	 * @return The support in each 1d dimension of a given set of object Ids.
	 */
	public int[] getSupports(Set<Integer> objIds) {
		for (int id : objIds) {
			if (filtereds.contains(id)) {
				System.err
						.println("Getting support for object which has been filtered: "
								+ id);
			}
		}

		if (objIds.size() == 0) {
			return new int[0];
		}

		PlainItemDB[] dbs = cartiDb.getProjDbs();
		int[] dimSupports = new int[dbs.length];

		for (int dimIx = 0; dimIx < dbs.length; dimIx++) {
			Iterator<Integer> it = objIds.iterator();
			Integer obj = it.next();
			BitSet tids = (BitSet) dbs[dimIx].get(obj).getTIDs().clone();
			while (it.hasNext()) {
				obj = it.next();
				tids.and(dbs[dimIx].get(obj).getTIDs());
			}

			dimSupports[dimIx] = tids.cardinality();
		}

		return dimSupports;
	}

	/**
	 * @param objIds
	 * @return The standard deviation in each 1d dimension of a given set of
	 *         object Ids.
	 */
	public double[] getStandardDeviations(Set<Integer> objIds) {
		for (int id : objIds) {
			if (filtereds.contains(id)) {
				System.err
						.println("Getting Standard Deviation for object which has been filtered: "
								+ id);
			}
		}

		if (objIds.size() == 0) {
			return new double[0];
		}

		double[] standardDeviations = new double[dims.size()];
		double[] means = getMeans(objIds);

		for (int dimIx = 0; dimIx < dims.size(); dimIx++) {
			standardDeviations[dimIx] = 0;
			for (int id : objIds) {
				standardDeviations[dimIx] += Math.pow(
						(means[dimIx] - origData[id][dimIx].v), 2);
			}
			standardDeviations[dimIx] = Math.sqrt(standardDeviations[dimIx]
					/ objIds.size());
		}

		return standardDeviations;
	}

	/**
	 * @param objIds
	 * @return The mean in each 1d dimension of a given set of object Ids.
	 */
	private double[] getMeans(Set<Integer> objIds) {
		double[] means = new double[dims.size()];

		for (int dimIx = 0; dimIx < dims.size(); dimIx++) {
			means[dimIx] = 0;
			for (int id : objIds) {
				means[dimIx] += origData[id][dimIx].v;
			}
			means[dimIx] /= objIds.size();
		}

		return means;
	}

	/**
	 * @param objIds
	 * @return The median absoluate deviation in each 1d dimension of a given set
	 *         of object Ids.
	 */
	public int[] getLocsMedAbsDev(Set<Integer> objIds) {
		for (int id : objIds) {
			if (filtereds.contains(id)) {
				System.err
						.println("Getting median absolute deviation for object which has been filtered: "
								+ id);
			}
		}

		if (objIds.size() == 0) {
			return new int[0];
		}

		int[] medianDists = new int[dims.size()];

		for (int dimIx = 0; dimIx < dims.size(); dimIx++) {
			// calculate median location
			int[] locs = new int[objIds.size()];
			int i = 0;
			for (int id : objIds) {
				locs[i] = objId2Loc(dimIx)[id];
				i++;
			}
			Arrays.sort(locs);

			int medianLoc;
			int middle = locs.length / 2;
			if (locs.length % 2 == 0) {
				// even
				medianLoc = (locs[middle - 1] + locs[middle]) / 2;
			} else {
				// uneven
				medianLoc = locs[middle];
			}

			// calculate median distance from median location
			int[] dists = new int[locs.length];
			for (int j = 0; j < dists.length; j++) {
				dists[j] = Math.abs(medianLoc - locs[j]);
			}
			Arrays.sort(dists);
			if (dists.length % 2 == 0) {
				// even
				medianDists[dimIx] = (dists[middle - 1] + dists[middle]) / 2;
			} else {
				// uneven
				medianDists[dimIx] = dists[middle];
			}
		}

		return medianDists;
	}

	/**
	 * Calculates a similarity matrix for the 1d dimensions. This is done by
	 * finding numItemSets FIS (based on minSup) of size 5 in each dimension and
	 * seeing how many of those are frequent in the other dimensions.
	 * 
	 * @param minSup
	 * @param numItemSets
	 * @return Symmetric 2d matrix showing dimension similarity.
	 */
	public int[][] createRelatedDimsMatrix(int minSup, int numItemSets) {
		int itemSetSize = 5;
		int[][] relatedDimsMatrix = new int[numDims][numDims];

		for (int i = 0; i < numDims; i++) {
			PlainItemDB pDb = cartiDb.getProjDbs()[i];
			List<PlainItemSet> result = RandomMiner.runParallel(pDb, minSup,
					numItemSets, itemSetSize);

			for (int j = 0; j < numDims; j++) {
				int freqCount = 0;
				for (PlainItemSet set : result) {
					if (getSupportInDim(set, j) >= minSup) {
						freqCount++;
					}
				}
				relatedDimsMatrix[i][j] = freqCount;
			}
		}

		// turn into symmetric matrix
		for (int i = 0; i < numDims; i++) {
			for (int j = 0; j < numDims; j++) {
				if (i < j) {
					int temp = relatedDimsMatrix[i][j];
					relatedDimsMatrix[i][j] += relatedDimsMatrix[j][i];
					relatedDimsMatrix[j][i] += temp;
				}
			}
		}
		return relatedDimsMatrix;
	}

	/**
	 * @param set
	 * @param dim
	 * @return The support of a given PlainItemSet in a given dimension.
	 */
	private int getSupportInDim(PlainItemSet set, int dim) {
		PlainItemDB pDb = cartiDb.getProjDbs()[dim];

		Iterator<PlainItem> it = set.iterator();
		PlainItem item = it.next();
		BitSet tids = (BitSet) pDb.get(item.getId()).getTIDs().clone();
		while (it.hasNext()) {
			item = it.next();
			tids.and(pDb.get(item.getId()).getTIDs());
		}

		return tids.cardinality();
	}

	public Set<Integer> getSelecteds() {
		return selecteds;
	}

	public List<Obj> getSelectedObjs() {
		return ids2Objs(selecteds);
	}

	public Set<Integer> getSelectedLocs() {
		Set<Integer> locs = new HashSet<Integer>();

		for (int id : selecteds) {
			locs.add(objId2Loc(orderDim)[id]);
		}

		return locs;
	}

	/**
	 * Sets/intersects/adds the selecteds to/with selectedLocs.
	 * 
	 * @param selectedLocs
	 * @param set
	 * @param intersect
	 * @param add
	 */
	public void selectLocs(Set<Integer> selectedLocs, boolean set,
			boolean intersect, boolean add) {
		Set<Integer> selectedIds = new HashSet<Integer>();

		for (int loc : selectedLocs) {
			selectedIds.add(loc2ObjId()[loc]);
		}

		// depending on selection mode, set/intersect/add to the selecteds
		if (set) {
			setSelecteds(selectedIds);
		} else if (intersect) {
			intersectSelecteds(selectedIds);
		} else if (add) {
			addSelecteds(selectedIds);
		}
	}

	public void setSelecteds(Set<Integer> toSelect) {
		this.selecteds = new HashSet<Integer>(toSelect);
	}

	private void addSelecteds(Set<Integer> toAdd) {
		this.selecteds.addAll(toAdd);
	}

	private void intersectSelecteds(Set<Integer> toIntersect) {
		this.selecteds.retainAll(toIntersect);
	}

	public void clearSelecteds() {
		this.selecteds.clear();
	}

	public Set<Integer> getFiltereds() {
		return filtereds;
	}

	/**
	 * Add selecteds to filtereds.
	 */
	public void filterSelecteds() {
		this.savedStates.push(new Memento(selecteds, filtereds));
		this.filtereds.addAll(selecteds);
		updateMaps();

		// remove filtered objects from selected
		clearSelecteds();
	}

	/**
	 * Add everything but the selecteds to filtereds.
	 */
	public void filterNotSelecteds() {
		this.savedStates.push(new Memento(selecteds, filtereds));

		// loop over every non-filtered object id
		for (int objId : loc2ObjId()) {
			if (!selecteds.contains(objId)) {
				this.filtereds.add(objId);
			}
		}

		updateMaps();
	}

	public void undoFiltering() {
		Memento state = savedStates.pop();
		this.selecteds = state.getSelecteds();
		this.filtereds = state.getFiltereds();
		updateMaps();
	}

	public boolean canUndoFiltering() {
		return (savedStates.size() > 0);
	}

	public void clearFiltereds() {
		this.savedStates.push(new Memento(selecteds, filtereds));
		this.filtereds.clear();
		updateMaps();
	}

	public Map<Integer, Cluster> getClustersMap() {
		return clustersMap;
	}

	public Set<Integer> getClustersToShowLocs() {
		Set<Integer> locs = new HashSet<Integer>();

		for (int clusterId : clustersToShow) {
			for (Obj obj : clustersMap.get(clusterId).getObjects()) {
				// cluster might contain filtered ids
				if (!filtereds.contains(obj.id)) {
					locs.add(objId2Loc(orderDim)[obj.id]);
				}
			}
		}

		return locs;
	}

	public Set<Integer> getClustersToShow() {
		return clustersToShow;
	}

	/**
	 * Selects the objects in the given clusters.
	 * 
	 * @param clusterIds
	 */
	public void selectClusters(Set<Integer> clusterIds) {
		selecteds = new HashSet<Integer>();

		for (Integer cid : clusterIds) {
			for (Obj obj : clustersMap.get(cid).getObjects()) {
				if (!filtereds.contains(obj.id)) {
					// cluster might contain filtered ids
					selecteds.add(obj.id);
				}
			}
		}
	}

	/**
	 * Create a new cluster from the selecteds.
	 */
	public void clusterSelecteds() {
		clustersMap.put(clusterIdCount, new Cluster(getSelectedObjs(), dims));
		clusterIdCount++;
	}

	/**
	 * Add selecteds to the cluster with given id
	 * 
	 * @param clusterId
	 */
	public void addSelectedsToCluster(int clusterId) {
		clustersMap.get(clusterId).addObjects(getSelectedObjs());
	}

	/**
	 * Removes selecteds from the cluster with given id
	 * 
	 * @param clusterId
	 */
	public void removeSelectedsFromCluster(Integer clusterId) {
		clustersMap.get(clusterId).removeObjects(getSelectedObjs());
	}

	/**
	 * Removes filtereds from the cluster with given id
	 * 
	 * @param clusterId
	 */
	public void removeFilteredsFromCluster(Integer clusterId) {
		clustersMap.get(clusterId).removeObjects(ids2Objs(filtereds));
	}

	/**
	 * Add the clusters to the clustersMap.
	 * 
	 * @param cluster
	 */
	public void addCluster(Cluster cluster) {
		clustersMap.put(clusterIdCount, cluster);
		clusterIdCount++;
	}

	/**
	 * Deletes the cluster with given id.
	 * 
	 * @param clusterId
	 */
	public void deleteCluster(Integer clusterId) {
		clustersMap.remove(clusterId);
		clustersToShow.remove(clusterId);
	}

	/**
	 * Marks the cluster with given id to be shown.
	 * 
	 * @param clusterId
	 */
	public void showCluster(Integer clusterId) {
		clustersToShow.add(clusterId);
	}

	/**
	 * Marks the cluster with given id to not be shown.
	 * 
	 * @param clusterId
	 */
	public void hideCluster(Integer clusterId) {
		clustersToShow.remove(clusterId);
	}

	/**
	 * @param clusterId
	 * @return Whether a cluster is visible.
	 */
	public boolean clusterIsVisible(Integer clusterId) {
		return clustersToShow.contains(clusterId);
	}

	public void setK(int k) {
		this.k = k;
		updateCartiDb();
	}

	public void setEps(double eps) {
		this.eps = eps;
		updateCartiDb();
	}

	public int getK() {
		return k;
	}

	public void setOrderDim(int orderDim) {
		this.orderDim = orderDim;
	}

	public void setOrderByObj(int objIx) {
		int objId = loc2ObjId()[objIx];
		System.out.println("Order by object " + objId);
		Dissimilarity dm = getSelectedDistMeasure();
		// MyCartifierInMemory cartifier = new MyCartifierInMemory(data);
		Pair[] order = orderBy(objId, dm);

		this.orderDim = dims.size() * 2;

		this.byObjId2LocMap = MaximalMinerCombiner.getId2Ord(order);
		this.byObjLoc2IdMap = new int[byObjId2LocMap.length];

		for (int i = 0; i < byObjId2LocMap.length; i++) {
			byObjLoc2IdMap[byObjId2LocMap[i]] = i;
		}
	}

	public void setSelectedDistMeasureId(int selectedDistMeasureId) {
		this.selectedDistMeasureId = selectedDistMeasureId;
	}

	public void addDistMeasure(Dissimilarity dissimilarity) {
		dissimilarities.add(dissimilarity);
		addDistMeasureToCartiDb(dissimilarity);
	}

	/**
	 * Keeps track of selecteds/filtereds to allow undo of filtering.
	 */
	private static class Memento {
		private final Set<Integer> selecteds;
		private final Set<Integer> filtereds;

		public Memento(Set<Integer> selectedsToSave, Set<Integer> filteredsToSave) {

			selecteds = new HashSet<Integer>(selectedsToSave);
			filtereds = new HashSet<Integer>(filteredsToSave);
		}

		public Set<Integer> getSelecteds() {
			return selecteds;
		}

		public Set<Integer> getFiltereds() {
			return filtereds;
		}
	}

	public int[] getDistribution() {
		PlainItemDB pDb = getSelectedProjDb();
		int size = pDb.size() - filtereds.size();
		int[] starts = new int[size];

		for (PlainItem item : pDb) {
			if (!filtereds.contains(item.getId())) {
				starts[objId2Loc(orderDim)[item.getId()]] = size
						- item.getTIDs().cardinality();
			}
		}
		return starts;
	}

	public int mineItemsets(boolean onlySelected, int minLen) {
		// do mining
		List<Freq> result;
		if (onlySelected) {
			result = maximer.mineFor(asArr(getSelectedObjs()), k, minLen,
					selectedDistMeasureId);
		} else {
			result = maximer.mineFor(getK(), minLen);
		}

		// turn result into clusters and add to model
		for (Freq freq : result) {
			Cluster cluster = new Cluster(arr2ObjSet(freq.freqSet), freq.freqDims);

			addCluster(cluster);
		}

		return result.size();
	}

	public int mineRandomFreqs(boolean onlySelected, int minSup, int numOfItemSets) {
		// get the items
		PlainItemDB items;
		if (onlySelected) {
			items = getSelectedProjDbOnlySelected();
		} else {
			items = getSelectedProjDb();
		}

		// do mining
		List<PlainItemSet> rawResult = RandomMaximalMiner.runParallel(items,
				minSup, numOfItemSets);
		// Remove the duplicates
		Set<PlainItemSet> result = new HashSet<>(rawResult);

		// dims for which the cluster was made
		Dissimilarity measure = getSelectedDistMeasure();
		int[] measureDims = measure.getDims();

		// turn result into clusters and add to model
		for (PlainItemSet itemSet : result) {
			List<Obj> objs = new ArrayList<>();
			for (PlainItem item : itemSet) {
				objs.add(getObj(item.getId()));
			}

			addCluster(new Cluster(objs, measureDims));
		}

		return result.size();
	}

	public Obj getObj(final int id) {
		return objects[id];
	}

	private Pair[] orderBy(int objId, Dissimilarity dm) {
		Pair[] cart = new Pair[data.size()];
		for (int j = 0; j < data.size(); j++) {
			double distance = dm.between(data.get(objId), data.get(j));
			cart[j] = new Pair(distance, j);
		}
		Arrays.sort(cart);
		return cart;
	}

	private int[] objId2Loc(int dimId) {
		if (dimId > dims.size()) {
			return byObjId2LocMap;
		}
		return objId2LocMaps[dimId];
	}

	private int[] loc2ObjId() {
		if (orderDim > dims.size()) {
			return byObjLoc2IdMap;
		}
		return loc2ObjIdMaps[orderDim];
	}

	private static String[] rangeAsArr(int r) {
		String[] names = new String[r];
		for (int i = 0; i < names.length; i++) {
			names[i] = String.valueOf(i);
		}
		return names;
	}

	private List<Obj> ids2Objs(final Collection<Integer> ids) {
		List<Obj> selectedItems = new ArrayList<>(ids.size());
		for (Integer id : ids) {
			selectedItems.add(getObj(id));
		}
		return selectedItems;
	}

	private Set<Obj> arr2ObjSet(final int[] arr) {
		Set<Obj> s = new HashSet<>(arr.length);
		for (int i : arr) {
			s.add(getObj(i));
		}
		return s;
	}

	private static int[] asArr(List<Obj> objs) {
		int[] arr = new int[objs.size()];
		int ix = 0;
		for (Obj obj : objs) {
			arr[ix++] = obj.id;
		}
		return arr;
	}

	public File saveClusters(Set<Integer> clusterIds, boolean saveDim,
			boolean saveSize) throws IOException {
		String filePrefix = "Clusters";
		if (saveSize) {
			filePrefix += "-size";
		}
		if (saveDim) {
			filePrefix += "-dims";
		}
		File file = File.createTempFile(filePrefix + "-", ".txt");

		PrintWriter pw = new PrintWriter(file);
		for (Integer id : clusterIds) {
			Cluster cl = clustersMap.get(id);
			if (saveSize) {
				pw.print(cl.getObjects().size() + ";");
			}
			if (saveDim) {
				pw.print(toSpaceSeparated(cl.getDims()) + ";");
			}
			pw.println(toSpaceSeparated(cl.getObjects()));
		}
		pw.flush();
		pw.close();
		return file;
	}

	private static <E> String toSpaceSeparated(Iterable<E> col) {
		Iterator<E> it = col.iterator();
		if (!it.hasNext())
			return "";

		StringBuilder sb = new StringBuilder();
		for (;;) {
			sb.append(it.next());
			if (!it.hasNext())
				return sb.toString();
			sb.append(' ');
		}
	}

	public void switchCartifier(Neighborhood neigh) {
		this.neighborhood = neigh;
		updateCartiDb();
	}

	public int getDefaultMinSup() {
		return (int) (k * 0.75);
	}

	public int getMaxEps() {
		if (maxEps < 0) {
			double max = -1;
			for (double[] row : data) {
				for (double cell : row) {
					if (cell > max) {
						max = cell;
					}
				}
			}
			maxEps = max;
		}
		return (int) maxEps;
	}
}