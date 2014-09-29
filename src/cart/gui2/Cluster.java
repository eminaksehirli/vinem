package cart.gui2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cluster {
	
	private Set<Integer> objects;
	private Set<Integer> dims;
	
	public Cluster(Set<Integer> objects, Set<Integer> dims) {
		this.objects = new HashSet<Integer>(objects);
		this.dims = new HashSet<Integer>(dims);
	}
	
	public void addObjects(Set<Integer> toAdd) {
		objects.addAll(toAdd);
	}
	
	public void removeObjects(Set<Integer> toRemove) {
		objects.removeAll(toRemove);
	}
	
	public Set<Integer> getObjects() {
		return objects;
	}
	
	public Set<Integer> getDims() {
		return dims;
	}
}
