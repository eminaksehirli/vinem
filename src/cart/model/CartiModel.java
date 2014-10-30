package cart.model;

import static cart.maximizer.MaximalMinerCombiner.getOrd2Id;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
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
import cart.cartifier.Pair;
import cart.gui2.Cluster;
import cart.gui2.DistMeasure;
import cart.gui2.OneDimDistMeasure;
import cart.maximizer.MaximalMinerCombiner;
import cart.maximizer.OneDCartifier;

/**
 * The main model class.
 * 
 * @author Detlev
 * 
 */
public class CartiModel {
	private String filePath;
	private int numObjects;
	private int numDims;
	private int k;
	private int orderDim;
	private int clusterIdCount;
	private Set<Integer> dims;
	private Pair[][] origData;
	private MyCartifyDbInMemory cartiDb;
	private Set<Integer> filtereds;
	private Set<Integer> selecteds;
	private Map<Integer, Cluster> clustersMap;
	private Set<Integer> clustersToShow;
	private int[][] objId2LocMaps;
	private int[][] loc2ObjIdMaps;
	private Stack<Memento> savedStates;
	private List<DistMeasure> distMeasures;
	private int selectedDistMeasureId;

	/**
	 * Initializes the model.
	 * 
	 * @param filePath
	 *            path for the data
	 * @param k
	 *            the initial k value
	 * @param orderDim
	 *            the initial order value
	 */
	public void init(final String filePath, int k, int orderDim) {
		this.filePath = filePath;
		this.k = k;
		this.orderDim = orderDim;
		this.clusterIdCount = 0;
		this.filtereds = new HashSet<Integer>();
		this.selecteds = new HashSet<Integer>();
		this.savedStates = new Stack<Memento>();
		this.clustersMap = new TreeMap<Integer, Cluster>();
		this.clustersToShow = new HashSet<Integer>();
		this.distMeasures = new ArrayList<DistMeasure>();
		this.selectedDistMeasureId = 0;

		ArrayList<double[]> data;
		try {
			data = OneDCartifier.readData(filePath);
			origData = OneDCartifier.toPairs(data);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		numObjects = data.size();
		numDims = OneDCartifier.transpose(data).length;

		dims = new TreeSet<Integer>();
		for (int i = 0; i < numDims; i++) {
			dims.add(i);
			Set<Integer> dimToAdd = new HashSet<Integer>();
			dimToAdd.add(i);
			distMeasures.add(new OneDimDistMeasure(dimToAdd));
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
			int[] loc2id = new int[loc2ObjIdMaps[dimId].length
					- filtereds.size()];

			int putLoc = 0;
			for (int loc = 0; loc < loc2ObjIdMaps[dimId].length; loc++) {
				if (!filtereds.contains(loc2ObjIdMaps[dimId][loc])) {
					loc2id[putLoc] = loc2ObjIdMaps[dimId][loc];
					objId2LocMaps[dimId][loc2ObjIdMaps[dimId][loc]] = putLoc;
					putLoc++;
				} else {
					objId2LocMaps[dimId][loc2ObjIdMaps[dimId][loc]] = -1;
				}
			}

			loc2ObjIdMaps[dimId] = loc2id;
		}
	}

	private void updateCartiDb() {
		cartiDb = new MyCartifyDbInMemory(filePath, k, distMeasures);
		cartiDb.cartify();
	}

	private void addDistMeasureToCartiDb(DistMeasure measure) {
		cartiDb.addDistMeasure(measure);
	}

	/**
	 * Initializes the id2loc and loc2id maps.
	 */
	private void initMaps() {
		objId2LocMaps = new int[numDims][];
		loc2ObjIdMaps = new int[numDims][];

		for (int dimId = 0; dimId < numDims; dimId++) {
			objId2LocMaps[dimId] = MaximalMinerCombiner.getId2Ord(getOrd2Id(
					origData, dimId));

			loc2ObjIdMaps[dimId] = new int[objId2LocMaps[dimId].length];
			for (int i = 0; i < objId2LocMaps[dimId].length; i++) {
				loc2ObjIdMaps[dimId][objId2LocMaps[dimId][i]] = i;
			}
		}
	}

	/**
	 * @return A 2d matrix containing 0s and 1s, where a 1 means we need to
	 *         colour that spot.
	 */
	public int[][] getMatrixToShow() {
		int[][] matrixToShow = new int[numObjects - filtereds.size()][numObjects
				- filtereds.size()];

		List<PlainItemDB> pDbs = cartiDb.getProjDbs();
		PlainItemDB pDb = pDbs.get(selectedDistMeasureId);

		// loop over each item
		for (PlainItem item : pDb) {
			if (!filtereds.contains(item.getId())) {

				// loop over each cart in which this object occurs
				for (int objId = item.getTIDs().nextSetBit(0); objId >= 0; objId = item
						.getTIDs().nextSetBit(objId + 1)) {
					if (!filtereds.contains(objId)) {
						// matrixToShow[row][col] = 1;
						matrixToShow[objId2LocMaps[orderDim][objId]][objId2LocMaps[orderDim][item
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
	public List<Integer> getOrderedObjList() {
		List<Integer> orderedObjs = new ArrayList<Integer>();

		for (int i = 0; i < loc2ObjIdMaps[orderDim].length; i++) {
			orderedObjs.add(loc2ObjIdMaps[orderDim][i]);
		}

		return orderedObjs;
	}

	public List<DistMeasure> getDistMeasures() {
		return distMeasures;
	}

	public DistMeasure getSelectedDistMeasure() {
		return distMeasures.get(selectedDistMeasureId);
	}

	/**
	 * @return The projection db for the selected dist measure.
	 */
	public PlainItemDB getSelectedProjDb() {
		return cartiDb.getProjDbs().get(selectedDistMeasureId);
	}

	/**
	 * @return The projection db, containing only the carts of selected objects,
	 *         for the selected dist measure.
	 */
	public PlainItemDB getSelectedProjDbOnlySelected() {
		PlainItemDB db = cartiDb.getProjDbs().get(selectedDistMeasureId);

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
	 *            the minSup threshold
	 * @return The object ids of each object with a support < minSup in the
	 *         selected distMeasure.
	 */
	public Set<Integer> getNoiseObjsInSelDistMeas(int minSup) {
		Set<Integer> noiseObjects = new HashSet<Integer>();

		List<PlainItemDB> pDbs = cartiDb.getProjDbs();
		PlainItemDB pDb = pDbs.get(selectedDistMeasureId);

		// loop over each item
		for (PlainItem item : pDb) {
			if ((!filtereds.contains(item.getId()))
					&& (item.getTIDs().cardinality() < minSup)) {
				noiseObjects.add(item.getId());
			}
		}

		return noiseObjects;
	}

	/**
	 * @param minSup
	 *            the minSup threshold
	 * @return The object ids of each object with a support < minSup in every
	 *         distMeasure.
	 */
	public Set<Integer> getNoiseObjsInAllDistMeas(int minSup) {
		Set<Integer> noiseObjects = new HashSet<Integer>();

		// add each object to noiseObjects to remove them later
		List<PlainItemDB> pDbs = cartiDb.getProjDbs();
		for (PlainItem item : pDbs.get(0)) {
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

		return noiseObjects;
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

		List<PlainItemDB> dbs = cartiDb.getProjDbs();
		int[] dimSupports = new int[dbs.size()];

		for (int dimIx = 0; dimIx < dbs.size(); dimIx++) {
			Iterator<Integer> it = objIds.iterator();
			Integer obj = it.next();
			BitSet tids = (BitSet) dbs.get(dimIx).get(obj).getTIDs().clone();
			while (it.hasNext()) {
				obj = it.next();
				tids.and(dbs.get(dimIx).get(obj).getTIDs());
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
	 * @return The median absoluate deviation in each 1d dimension of a given
	 *         set of object Ids.
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
				locs[i] = objId2LocMaps[dimIx][id];
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
			PlainItemDB pDb = cartiDb.getProjDbs().get(i);
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
		PlainItemDB pDb = cartiDb.getProjDbs().get(dim);

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

	public Set<Integer> getSelectedLocs() {
		Set<Integer> locs = new HashSet<Integer>();

		for (int id : selecteds) {
			locs.add(objId2LocMaps[orderDim][id]);
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
			selectedIds.add(loc2ObjIdMaps[orderDim][loc]);
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
		for (int objId : loc2ObjIdMaps[orderDim]) {
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
			for (int objId : clustersMap.get(clusterId).getObjects()) {
				// cluster might contain filtered ids
				if (!filtereds.contains(objId)) {
					locs.add(objId2LocMaps[orderDim][objId]);
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

		for (int cid : clusterIds) {
			for (int objId : clustersMap.get(cid).getObjects()) {
				if (!filtereds.contains(objId)) {
					// cluster might contain filtered ids
					selecteds.add(objId);
				}
			}
		}
	}

	/**
	 * Create a new cluster from the selecteds.
	 */
	public void clusterSelecteds() {
		clustersMap.put(clusterIdCount, new Cluster(selecteds, dims));
		clusterIdCount++;
	}

	/**
	 * Add selecteds to the cluster with given id
	 * 
	 * @param clusterId
	 */
	public void addSelectedsToCluster(int clusterId) {
		clustersMap.get(clusterId).addObjects(selecteds);
	}

	/**
	 * Removes selecteds from the cluster with given id
	 * 
	 * @param clusterId
	 */
	public void removeSelectedsFromCluster(int clusterId) {
		clustersMap.get(clusterId).removeObjects(selecteds);
	}

	/**
	 * Removes filtereds from the cluster with given id
	 * 
	 * @param clusterId
	 */
	public void removeFilteredsFromCluster(int clusterId) {
		clustersMap.get(clusterId).removeObjects(filtereds);
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
	public void deleteCluster(int clusterId) {
		clustersMap.remove(clusterId);
		clustersToShow.remove(clusterId);
	}

	/**
	 * Marks the cluster with given id to be shown.
	 * 
	 * @param clusterId
	 */
	public void showCluster(int clusterId) {
		clustersToShow.add(clusterId);
	}

	/**
	 * Marks the cluster with given id to not be shown.
	 * 
	 * @param clusterId
	 */
	public void hideCluster(int clusterId) {
		clustersToShow.remove(clusterId);
	}

	/**
	 * @param clusterId
	 * @return Whether a cluster is visible.
	 */
	public boolean clusterIsVisible(int clusterId) {
		return clustersToShow.contains(clusterId);
	}

	public void setK(int k) {
		this.k = k;
		updateCartiDb();
	}

	public int getK() {
		return k;
	}

	public void setOrderDim(int orderDim) {
		this.orderDim = orderDim;
	}

	public void setSelectedDistMeasureId(int selectedDistMeasureId) {
		this.selectedDistMeasureId = selectedDistMeasureId;
	}

	public void addDistMeasure(DistMeasure distMeasure) {
		distMeasures.add(distMeasure);
		addDistMeasureToCartiDb(distMeasure);
	}

	/**
	 * Keeps track of selecteds/filtereds to allow undo of filtering.
	 */
	private static class Memento {
		private final Set<Integer> selecteds;
		private final Set<Integer> filtereds;

		public Memento(Set<Integer> selectedsToSave,
				Set<Integer> filteredsToSave) {

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
}
