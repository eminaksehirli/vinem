package cart.gui2;

import static cart.maximizer.MaximalMinerCombiner.getOrd2Id;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import mime.plain.PlainItem;
import mime.plain.PlainItemDB;
import cart.cartifier.CartifyDbInMemory;
import cart.cartifier.Pair;
import cart.maximizer.MaximalMinerCombiner;
import cart.maximizer.OneDCartifier;

public class CartiModel {
	private String filePath;
	private int numObjects;
	private int numDims;
	private int k;
	private int orderDim;
	private int clusterIdCount;
	private Set<Integer> dims;
	private Pair[][] origData;
	private CartifyDbInMemory cartiDb;
	private List<Integer>[][] cartLists;
	private Set<Integer> filtereds;
	private Set<Integer> selecteds;
	private Map<Integer, Cluster> clustersMap;
	private Set<Integer> clustersToShow;
	private Map<Integer, Integer>[] objId2LocMaps;
	private Map<Integer, Integer>[] loc2ObjIdMaps;
	private Stack<Memento> savedStates;

	public void init(final String filePath, int k, int orderDim) {
		this.filePath = filePath;
		this.k = k;
		this.orderDim = orderDim;
		this.clusterIdCount = 0;
		this.filtereds = new HashSet<Integer>();
		this.selecteds = new HashSet<Integer>();
		this.savedStates = new Stack<Memento>();
		this.clustersMap = new HashMap<Integer, Cluster>();
		this.clustersToShow = new HashSet<Integer>();

		ArrayList<double[]> data;
		try {
			data = OneDCartifier.readData(filePath);
			origData = OneDCartifier.toPairs(data);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		numObjects = data.size();
		numDims = OneDCartifier.transpose(data).length;

		dims = new HashSet<Integer>(numDims);
		for (int i = 0; i < numDims; i++) {
			dims.add(i);
		}
		dims = Collections.unmodifiableSet(dims);

		initMaps();
		updateCartLists();
	}

	// cartlists[dimId][objId] is a list containing the objIds in the cart of
	// the given object in the given dimension
	// filtered objects are not added to the carts!
	private void updateCartLists() {
		cartiDb = new CartifyDbInMemory(filePath, k);
		cartiDb.cartify();

		// create new cart lists
		cartLists = new List[numDims][numObjects];
		for (int dimId = 0; dimId < numDims; dimId++) {
			for (int objId = 0; objId < numObjects; objId++) {
				cartLists[dimId][objId] = new ArrayList<Integer>();
			}
		}

		// fill the cart lists
		PlainItemDB[] pDbs = cartiDb.getProjDbs();
		for (int dimId = 0; dimId < pDbs.length; dimId++) {
			PlainItemDB pDb = pDbs[dimId];
			for (PlainItem item : pDb) {
				for (int objId = item.getTIDs().nextSetBit(0); objId >= 0; objId = item
						.getTIDs().nextSetBit(objId + 1)) {
					if (!filtereds.contains(item.getId())) {
						cartLists[dimId][objId].add(item.getId());
					}
				}
			}
		}
	}

	private void initMaps() {
		objId2LocMaps = new Map[numDims];
		loc2ObjIdMaps = new Map[numDims];

		for (int dimId = 0; dimId < numDims; dimId++) {
			int[] objId2LocMap = MaximalMinerCombiner.getId2Ord(getOrd2Id(
					origData, dimId));

			objId2LocMaps[dimId] = new HashMap<Integer, Integer>(
					objId2LocMap.length);
			loc2ObjIdMaps[dimId] = new HashMap<Integer, Integer>(
					objId2LocMap.length);
			for (int i = 0; i < objId2LocMap.length; i++) {
				objId2LocMaps[dimId].put(i, objId2LocMap[i]);
				loc2ObjIdMaps[dimId].put(objId2LocMap[i], i);
			}
		}
	}

	public int[][] getMatrixToShow() {
		Map<Integer, Integer> id2LocMap = getObjId2LocMap(orderDim);
		Map<Integer, Integer> loc2IdMap = getLoc2ObjIdMap(orderDim);

		int[][] matrixToShow = new int[loc2IdMap.size()][loc2IdMap.size()];

		// loop over each row
		for (int loc = 0; loc < loc2IdMap.size(); loc++) {
			// the cart containing ids to show on this row
			List<Integer> cart = cartLists[orderDim][loc2IdMap.get(loc)];

			for (int id : cart) {
				matrixToShow[loc][id2LocMap.get(id)] = 1;
			}
		}

		return matrixToShow;
	}

	public int getNumObjects() {
		return numObjects;
	}

	public Set<Integer> getDims() {
		return dims;
	}

	// returns the objectId 2 location map for a given dimension
	private Map<Integer, Integer> getObjId2LocMap(int dim) {
		if (filtereds.size() == 0) {
			return objId2LocMaps[dim];
		}

		// account for filtereds
		Map<Integer, Integer> loc2IdMap = getLoc2ObjIdMap(dim);
		Map<Integer, Integer> id2LocMap = new HashMap<Integer, Integer>(
				objId2LocMaps[dim].size() - filtereds.size());

		for (int loc : loc2IdMap.keySet()) {
			id2LocMap.put(loc2IdMap.get(loc), loc);
		}

		return id2LocMap;
	}

	// TODO(?) store filteredMaps and only recalculate when filtereds change
	// returns the location 2 objectId map for a given dimension
	private Map<Integer, Integer> getLoc2ObjIdMap(int dim) {
		if (filtereds.size() == 0) {
			return loc2ObjIdMaps[dim];
		}

		// account for filtereds
		int putLoc = 0;
		Map<Integer, Integer> loc2IdMap = new HashMap<Integer, Integer>(
				loc2ObjIdMaps[dim].size() - filtereds.size());

		for (int loc = 0; loc < loc2ObjIdMaps[dim].size(); loc++) {
			int id = loc2ObjIdMaps[dim].get(loc);

			if (!filtereds.contains(id)) {
				loc2IdMap.put(putLoc, id);
				putLoc++;
			}
		}

		return loc2IdMap;
	}

	// returns a list of all object Ids except those which are filtered, ordered
	// for a given dimension
	public List<Integer> getOrderedObjList() {
		List<Integer> orderedObjs = new ArrayList<Integer>();

		Map<Integer, Integer> loc2ObjIdMap = getLoc2ObjIdMap(orderDim);
		for (int i = 0; i < loc2ObjIdMap.size(); i++) {
			orderedObjs.add(loc2ObjIdMap.get(i));
		}

		return orderedObjs;
	}

	// returns the support in each dimension of a given set of object Ids
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

	// calculates the standard deviation of the given objects in every dimension
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

	// TODO name of measure?
	// calculates the measure of the given objects in every dimension
	public int[] getMeasures(Set<Integer> objIds) {
		for (int id : objIds) {
			if (filtereds.contains(id)) {
				System.err
						.println("Getting Measure for object which has been filtered: "
								+ id);
			}
		}

		if (objIds.size() == 0) {
			return new int[0];
		}

		int[] measures = new int[dims.size()];
		double[] means = getMeans(objIds);
		double[] deviations = getStandardDeviations(objIds);

		for (int dimIx = 0; dimIx < dims.size(); dimIx++) {
			measures[dimIx] = 0;
			for (int id : objIds) {
				if (Math.abs(origData[id][dimIx].v - means[dimIx]) > deviations[dimIx]) {
					measures[dimIx]++;
				}
			}
		}

		return measures;
	}

	// calculates the mean of the given objects in every dimension
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

	// Calculates the median absolute deviation based on locations of the given
	// objects in every dimension
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
			Map<Integer, Integer> id2LocMap = getObjId2LocMap(dimIx);

			// calculate median location
			int[] locs = new int[objIds.size()];
			int i = 0;
			for (int id : objIds) {
				locs[i] = id2LocMap.get(id);
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

	public Set<Integer> getSelecteds() {
		return selecteds;
	}

	public Set<Integer> getSelectedLocs() {
		Set<Integer> locs = new HashSet<Integer>();
		Map<Integer, Integer> id2Loc = getObjId2LocMap(orderDim);

		for (int id : selecteds) {
			locs.add(id2Loc.get(id));
		}

		return locs;
	}

	public void selectLocs(Set<Integer> selectedLocs, boolean set,
			boolean intersect, boolean add) {
		Set<Integer> selectedIds = new HashSet<Integer>();

		Map<Integer, Integer> loc2IdMap = getLoc2ObjIdMap(orderDim);
		for (int loc : selectedLocs) {
			selectedIds.add(loc2IdMap.get(loc));
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

	// add selecteds to filtereds
	public void filterSelecteds() {
		this.savedStates.push(new Memento(selecteds, filtereds));
		this.filtereds.addAll(selecteds);
		updateCartLists();

		// remove filtered objects from clusters
		for (int clusterId : clustersMap.keySet()) {
			removeSelectedsFromCluster(clusterId);
		}

		// remove filtered objects from selected
		clearSelecteds();
	}

	public void undoFiltering() {
		Memento state = savedStates.pop();
		this.selecteds = state.getSelecteds();
		this.filtereds = state.getFiltereds();
		updateCartLists();
	}

	public boolean canUndoFiltering() {
		return (savedStates.size() > 0);
	}

	public void addFiltereds(Set<Integer> toFilter) {
		this.savedStates.push(new Memento(selecteds, filtereds));
		this.filtereds.addAll(toFilter);
		updateCartLists();

		// remove filtered objects from clusters
		for (int clusterId : clustersMap.keySet()) {
			removeSelectedsFromCluster(clusterId);
		}

		// remove filtered objects from selected
		clearSelecteds();
	}

	public void removeFiltereds(Set<Integer> toRemove) {
		this.savedStates.push(new Memento(selecteds, filtereds));
		this.filtereds.removeAll(toRemove);
		updateCartLists();
	}

	public void clearFiltereds() {
		this.savedStates.push(new Memento(selecteds, filtereds));
		this.filtereds.clear();
		updateCartLists();
	}

	public Map<Integer, Cluster> getClustersMap() {
		return clustersMap;
	}

	public Set<Integer> getClustersToShowLocs() {
		Set<Integer> locs = new HashSet<Integer>();
		Map<Integer, Integer> id2Loc = getObjId2LocMap(orderDim);

		for (int clusterId : clustersToShow) {
			for (int objId : clustersMap.get(clusterId).getObjects()) {
				locs.add(id2Loc.get(objId));
			}
		}

		return locs;
	}

	public Set<Integer> getClustersToShow() {
		return clustersToShow;
	}

	// selects the objects in the given clusters
	public void selectClusters(Set<Integer> clusterIds) {
		selecteds = new HashSet<Integer>();

		for (int id : clusterIds) {
			selecteds.addAll(clustersMap.get(id).getObjects());
		}
	}

	// create a new cluster from the selecteds
	public void clusterSelecteds() {
		clustersMap.put(clusterIdCount, new Cluster(selecteds, dims));
		clusterIdCount++;
	}

	// add selecteds to the cluster with given id
	public void addSelectedsToCluster(int clusterId) {
		clustersMap.get(clusterId).addObjects(selecteds);
	}

	// removes selecteds from the cluster with given id
	public void removeSelectedsFromCluster(int clusterId) {
		clustersMap.get(clusterId).removeObjects(selecteds);
	}

	// add the clusters to the clustersMap after removing the filtered objects
	public void addCluster(Cluster cluster) {
		cluster.removeObjects(filtereds);

		clustersMap.put(clusterIdCount, cluster);
		clusterIdCount++;
	}

	// deletes the cluster with given id
	public void deleteCluster(int clusterId) {
		clustersMap.remove(clusterId);
		clustersToShow.remove(clusterId);
	}

	// marks the cluster with given id to be shown
	public void showCluster(int clusterId) {
		clustersToShow.add(clusterId);
	}

	// marks the cluster with given id to not be shown
	public void hideCluster(int clusterId) {
		clustersToShow.remove(clusterId);
	}

	// returns whether a cluster is visible
	public boolean clusterIsVisible(int clusterId) {
		return clustersToShow.contains(clusterId);
	}

	public void setK(int k) {
		this.k = k;
		updateCartLists();
	}

	public int getK() {
		return k;
	}

	public void setOrderDim(int orderDim) {
		this.orderDim = orderDim;
	}

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
