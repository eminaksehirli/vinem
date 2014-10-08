package cart.model;

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
import java.util.TreeSet;

import mime.plain.PlainItem;
import mime.plain.PlainItemDB;
import cart.cartifier.Pair;
import cart.gui2.Cluster;
import cart.gui2.DistMeasure;
import cart.gui2.OneDimDistMeasure;
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
	private MyCartifyDbInMemory cartiDb;
	private List<List<Integer>[]> cartLists;
	private Set<Integer> filtereds;
	private Set<Integer> selecteds;
	private Map<Integer, Cluster> clustersMap;
	private Set<Integer> clustersToShow;
	private int[][] objId2LocMaps;
	private int[][] loc2ObjIdMaps;
	private Stack<Memento> savedStates;
	private List<DistMeasure> distMeasures;
	private int selectedDistMeasureId;

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
		updateCartLists();
	}

	// updates the id2loc and loc2id maps when filtereds has changed
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

	// cartlists.get(distMeasureId)[objId] is a list containing the objIds in
	// the cart of the given objId for the given distMeasureId
	// filtered objects are not added to the carts
	private void updateCartLists() {
		cartiDb = new MyCartifyDbInMemory(filePath, k, distMeasures);
		cartiDb.cartify();

		// create new cart lists
		cartLists = new ArrayList<List<Integer>[]>();
		while (cartLists.size() < distMeasures.size()) {
			List<Integer>[] carts = new List[numObjects];
			for (int objId = 0; objId < numObjects; objId++) {
				carts[objId] = new ArrayList<Integer>();
			}
			cartLists.add(carts);
		}

		// fill the cart lists
		PlainItemDB[] pDbs = cartiDb.getProjDbs();
		for (int distMeasureId = 0; distMeasureId < pDbs.length; distMeasureId++) {
			PlainItemDB pDb = pDbs[distMeasureId];
			for (PlainItem item : pDb) {
				for (int objId = item.getTIDs().nextSetBit(0); objId >= 0; objId = item
						.getTIDs().nextSetBit(objId + 1)) {
					if (!filtereds.contains(item.getId())) {
						cartLists.get(distMeasureId)[objId].add(item.getId());
					}
				}
			}
		}
	}

	// adds a distance measure to the cart lists
	private void addMeasureToCartLists() {
		// create the cart lists for the new measure
		List<Integer>[] carts = new List[numObjects];
		for (int objId = 0; objId < numObjects; objId++) {
			carts[objId] = new ArrayList<Integer>();
		}
		cartLists.add(carts);

		// create a temporary MyCartifyDbInMemory to find the carts for the new
		// measure
		int newMeasureId = distMeasures.size() - 1;
		List<DistMeasure> tempDistMeasures = new ArrayList<DistMeasure>();
		tempDistMeasures.add(distMeasures.get(newMeasureId));

		MyCartifyDbInMemory tempCartiDb = new MyCartifyDbInMemory(filePath, k,
				tempDistMeasures);
		tempCartiDb.cartify();

		// fill the cart lists for the new measure
		PlainItemDB[] pDbs = tempCartiDb.getProjDbs();
		PlainItemDB pDb = pDbs[0];
		for (PlainItem item : pDb) {
			for (int objId = item.getTIDs().nextSetBit(0); objId >= 0; objId = item
					.getTIDs().nextSetBit(objId + 1)) {
				if (!filtereds.contains(item.getId())) {
					cartLists.get(newMeasureId)[objId].add(item.getId());
				}
			}
		}
	}

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

	public int[][] getMatrixToShow() {
		int[][] matrixToShow = new int[numObjects - filtereds.size()][numObjects
				- filtereds.size()];

		// loop over each row
		for (int loc = 0; loc < matrixToShow.length; loc++) {
			// the cart containing ids to show on this row
			List<Integer> cart = cartLists.get(selectedDistMeasureId)[loc2ObjIdMaps[orderDim][loc]];

			for (int id : cart) {
				matrixToShow[loc][objId2LocMaps[orderDim][id]] = 1;
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

	// returns a list of all object Ids except those which are filtered, ordered
	// for a given dimension
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

	// add selecteds to filtereds
	public void filterSelecteds() {
		this.savedStates.push(new Memento(selecteds, filtereds));
		this.filtereds.addAll(selecteds);
		updateMaps();
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
		updateMaps();
		updateCartLists();
	}

	public boolean canUndoFiltering() {
		return (savedStates.size() > 0);
	}

	public void addFiltereds(Set<Integer> toFilter) {
		this.savedStates.push(new Memento(selecteds, filtereds));
		this.filtereds.addAll(toFilter);
		updateMaps();
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
		updateMaps();
		updateCartLists();
	}

	public void clearFiltereds() {
		this.savedStates.push(new Memento(selecteds, filtereds));
		this.filtereds.clear();
		updateMaps();
		updateCartLists();
	}

	public Map<Integer, Cluster> getClustersMap() {
		return clustersMap;
	}

	public Set<Integer> getClustersToShowLocs() {
		Set<Integer> locs = new HashSet<Integer>();

		for (int clusterId : clustersToShow) {
			for (int objId : clustersMap.get(clusterId).getObjects()) {
				locs.add(objId2LocMaps[orderDim][objId]);
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

	public void setSelectedDistMeasureId(int selectedDistMeasureId) {
		this.selectedDistMeasureId = selectedDistMeasureId;
	}

	public void addDistMeasure(DistMeasure distMeasure) {
		distMeasures.add(distMeasure);
		addMeasureToCartLists();
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
