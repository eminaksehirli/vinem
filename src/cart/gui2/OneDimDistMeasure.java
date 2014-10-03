package cart.gui2;

import java.util.Set;

public class OneDimDistMeasure extends DistMeasure {
	
	public OneDimDistMeasure(Set<Integer> dims) {
		super(dims);
		if (dims.size() != 1) {
			System.err.println("Creating 1 dimensional dist measure with less/more than 1 dim");
		}
	}

	@Override
	public double calculateDistance(double[] object1, double[] object2) {
		double distance = 0;

		for (int d : dims) {
			distance += Math.abs(object1[d] - object2[d]);
		}

		return distance;
	}

	@Override
	public String toString() {
		return "1 dimensional: " + dims;
	}

}
