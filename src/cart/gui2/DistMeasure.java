package cart.gui2;

import java.util.HashSet;
import java.util.Set;

public abstract class DistMeasure {
	protected Set<Integer> dims;

	public DistMeasure(Set<Integer> dims) {
		this.dims = new HashSet<Integer>(dims);
	}

	public abstract double calculateDistance(double[] object1, double[] object2);

	@Override
	public abstract String toString();
}
