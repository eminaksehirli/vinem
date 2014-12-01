package cart.gui2;

import java.util.Arrays;
import java.util.Collection;

public abstract class Dissimilarity {
	protected int[] dims;

	public Dissimilarity(int[] dims) {
		this.dims = dims;
	}

	public Dissimilarity(Collection<Integer> dims) {
		this.dims = new int[dims.size()];
		int ix = 0;
		for (int dim : dims) {
			this.dims[ix++] = dim;
		}
	}

	public abstract double between(double[] object1, double[] object2);

	protected abstract String getName();

	@Override
	public String toString() {
		return getName() + " :" + Arrays.toString(dims);
	}

	public int[] getDims() {
		return dims;
	}
}
