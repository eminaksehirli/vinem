package cart.gui2;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cart.model.Obj;

public class Cluster {

	private Set<Obj> objects;
	private Set<Integer> dims;

	public Cluster(Collection<Obj> objects, Set<Integer> dims) {
		this.objects = new HashSet<>(objects);
		this.dims = new HashSet<>(dims);
	}

	public Cluster(Cluster cluster) {
		this.objects = new HashSet<>(cluster.getObjects());
		this.dims = new HashSet<>(cluster.getDims());
	}

	public void addObjects(Collection<Obj> toAdd) {
		objects.addAll(toAdd);
	}

	public void removeObjects(Collection<Obj> toRemove) {
		objects.removeAll(toRemove);
	}

	public Collection<Obj> getObjects() {
		return objects;
	}

	public Set<Integer> getDims() {
		return dims;
	}
}
