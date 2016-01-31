package be.uantwerpen.adrem.gui.vinem.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data cluster.
 * 
 * @author Detlev Van Looy
 * @author M. Emin Aksehirli
 */
public class Cluster {

	private Set<Obj> objects;
	private List<Attribute> dims;

	public Cluster(Collection<Obj> objects, Collection<Attribute> dims) {
		this.objects = new HashSet<>(objects);
		this.dims = new ArrayList<>(dims);
	}

	public Cluster(List<Obj> objects, int[] dims2) {
		this.objects = new HashSet<>(objects);
		dims = new ArrayList<>(dims2.length);
		for (int i = 0; i < dims2.length; i++) {
			dims.add(new Attribute(i));
		}
	}

	public Cluster(Cluster cluster) {
		this.objects = new HashSet<>(cluster.getObjects());
		this.dims = new ArrayList<>(cluster.getDims());
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

	public List<Attribute> getDims() {
		return dims;
	}
}
