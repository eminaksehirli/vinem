package cart.gui2;

import static cart.maximizer.MaximalMinerCombiner.getOrd2Id;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
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

public class CartiModel 
{
	private String filePath;
	private int numObjects;
	private int numDims;
	private int k;
	private int orderDim;
	private List<String> dimList;
	private Pair[][] origData;
	private CartifyDbInMemory cartiDb;
	private List<Integer>[][] cartLists;
	private Set<Integer> filtereds;
	private Set<Integer> selecteds;
	private Map<Integer,Integer>[] objId2LocMaps;
	private Map<Integer,Integer>[] loc2ObjIdMaps;
	private Stack<Memento> savedStates;

	public void init(final String filePath, int k, int orderDim) {
		this.filePath = filePath;
		this.k = k;
		this.orderDim = orderDim;
		this.filtereds = new HashSet<Integer>();
		this.selecteds = new HashSet<Integer>();
		this.savedStates = new Stack<Memento>();
		
		ArrayList<double[]> data;
		try
		{
			data = OneDCartifier.readData(filePath);
			origData = OneDCartifier.toPairs(data);
		} catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		
		numObjects = data.size();
		numDims = OneDCartifier.transpose(data).length;

		// dimensions might have names, e.g. "petal length"
		// for now we just add string versions of the dimension ids
		dimList = new ArrayList<String>();
		for (int i = 0; i < numDims; i++)
		{
			dimList.add(Integer.toString(i));
		}
		dimList = Collections.unmodifiableList(dimList);
		
		initMaps();
		updateCartLists();
	}
	
	// cartlists[dimId][objId] is a list containing the objIds in the cart of the given object in the given dimension
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
		for (int dimId = 0; dimId < pDbs.length; dimId++)
		{
			PlainItemDB pDb = pDbs[dimId];
			for (PlainItem item : pDb)
			{
				for (int objId = item.getTIDs().nextSetBit(0); objId >= 0; objId = item.getTIDs()
						.nextSetBit(objId + 1))
				{
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
			int[] objId2LocMap = MaximalMinerCombiner.getId2Ord(getOrd2Id(origData, dimId));
			
			objId2LocMaps[dimId] = new HashMap<Integer,Integer>(objId2LocMap.length);
			loc2ObjIdMaps[dimId] = new HashMap<Integer,Integer>(objId2LocMap.length);
			for (int i = 0; i < objId2LocMap.length; i++)
			{
				objId2LocMaps[dimId].put(i, objId2LocMap[i]);
				loc2ObjIdMaps[dimId].put(objId2LocMap[i], i);
			}
		}
	}
	
	public int[][] getMatrixToShow() {
		Map<Integer,Integer> id2LocMap = getObjId2LocMap();
		Map<Integer,Integer> loc2IdMap = getLoc2ObjIdMap();
		
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
	
	public List<String> getDimList() {
		return dimList;
	}
	
	// returns the objectId 2 location map for a given dimension
	private Map<Integer,Integer> getObjId2LocMap() {
		if (filtereds.size() == 0) {
			return objId2LocMaps[orderDim];
		} 
		
		// account for filtereds
		Map<Integer,Integer> loc2IdMap = getLoc2ObjIdMap();
		Map<Integer,Integer> id2LocMap = new HashMap<Integer,Integer>(objId2LocMaps[orderDim].size() - filtereds.size());
		
		for (int loc : loc2IdMap.keySet()) {
			id2LocMap.put(loc2IdMap.get(loc), loc);
		}
		
		return id2LocMap;
	}
	
	// returns the location 2 objectId map for a given dimension
	private Map<Integer,Integer> getLoc2ObjIdMap() {
		if (filtereds.size() == 0) {
			return loc2ObjIdMaps[orderDim];
		}
		
		// account for filtereds
		int putLoc = 0;
		Map<Integer,Integer> loc2IdMap = new HashMap<Integer,Integer>(loc2ObjIdMaps[orderDim].size() - filtereds.size());
		
		for (int loc = 0; loc < loc2ObjIdMaps[orderDim].size(); loc++) {
			int id = loc2ObjIdMaps[orderDim].get(loc);
			
			if (!filtereds.contains(id)) {
				loc2IdMap.put(putLoc, id);
				putLoc++;
			}
		}
		
		return loc2IdMap;
	}
	
	// returns a list of all object Ids except those which are filtered, ordered for a given dimension
	public List<Integer> getOrderedObjList() {
		List<Integer> orderedObjs = new ArrayList<Integer>();
		
		Map<Integer,Integer> loc2ObjIdMap = getLoc2ObjIdMap();
		for (int i = 0; i < loc2ObjIdMap.size(); i++) {
			orderedObjs.add(loc2ObjIdMap.get(i));
		}
			
		return orderedObjs;
	}
	
	// returns the support in each dimension of a given set of object Ids 
	// TODO(?) what to do with filtereds, for now assume this will never be called for filtered Ids
	public int[] getSupports(Set<Integer> objIds) {
		// 
		for (int id : objIds) {
			if (filtereds.contains(id)) {
				System.out.println("Getting support for object which has been filtered: " + id);
			}
		}
		
		if (objIds.size() == 0) {
			return new int[0];
		}
		
		PlainItemDB[] dbs = cartiDb.getProjDbs();
		int[] dimSupports = new int[dbs.length];
		
		for (int dimIx = 0; dimIx < dbs.length; dimIx++)
		{
			Iterator<Integer> it = objIds.iterator();
			Integer obj = it.next();
			BitSet tids = (BitSet) dbs[dimIx].get(obj).getTIDs().clone();
			while (it.hasNext())
			{
				obj = it.next();
				tids.and(dbs[dimIx].get(obj).getTIDs());
			}

			dimSupports[dimIx] = tids.cardinality();
		}
		
		return dimSupports;
	}
	
	public Set<Integer> getSelecteds() {
		return selecteds;
	}
	
	public void selectLocs(Set<Integer> selectedLocs, boolean set, boolean intersect, boolean add) {
		Set<Integer> selectedIds = new HashSet<Integer>();
		
		Map<Integer,Integer> loc2IdMap = getLoc2ObjIdMap();
		for (int loc: selectedLocs) {
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
		this.selecteds = toSelect;
	}
	
	private void addSelecteds(Set<Integer> toAdd) {
		this.selecteds.addAll(toAdd);
	}
	
	private void intersectSelecteds(Set<Integer> toIntersect) {
		this.selecteds.retainAll(toIntersect);
	}
	
	private void removeSelecteds(Set<Integer> toRemove) {
		this.selecteds.removeAll(toRemove);
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
		clearSelecteds(); // can't select filtered objects
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
	
	public void addFiltereds(Set<Integer> toAdd) {
		this.savedStates.push(new Memento(selecteds, filtereds));
		this.filtereds.addAll(toAdd);
		updateCartLists();
		removeSelecteds(toAdd); // can't select filtered objects
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
	
	public void setK(int k) {
		this.k = k;
		updateCartLists();
	}
	
	public void setOrderDim(int orderDim) {
		this.orderDim = orderDim;
	}
	
	public static class Memento {
		private final Set<Integer> selecteds;
		private final Set<Integer> filtereds;
		
		public Memento(Set<Integer> selectedsToSave, Set<Integer> filteredsToSave) {
			selecteds = new HashSet<Integer>();
			selecteds.addAll(selectedsToSave);
			filtereds = new HashSet<Integer>();
			filtereds.addAll(filteredsToSave);
		}
		
		public Set<Integer> getSelecteds() {
			return selecteds;
		}
		
		public Set<Integer> getFiltereds() {
			return filtereds;
		}
	}
}
