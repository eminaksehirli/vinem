package cart.gui2;

import java.util.Set;

public class EuclidianDistMeasure extends DistMeasure {

	public EuclidianDistMeasure(Set<Integer> dims) {
		super(dims);
	}

	@Override
	public double calculateDistance(double[] object1, double[] object2) {
		double distance;
		double sum = 0;

		for (int d : dims) {
			sum += Math.pow(object1[d] - object2[d], 2);
		}
		distance = Math.sqrt(sum);

		return distance;
	}

	@Override
	public String toString() {
		return "Euclidian: " + dims;
	}
}
