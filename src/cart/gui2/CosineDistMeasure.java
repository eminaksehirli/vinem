package cart.gui2;

import java.util.Set;

public class CosineDistMeasure extends DistMeasure {

	public CosineDistMeasure(Set<Integer> dims) {
		super(dims);
	}

	@Override
	public double calculateDistance(double[] object1, double[] object2) {
		// TODO correct implementation?
		double distance;
		double similarity;

		double sumAxA = 0;
		double sumBxB = 0;
		double sumAxB = 0;

		for (int d : dims) {
			sumAxA += object1[d] * object1[d];
			sumBxB += object2[d] * object2[d];
			sumAxB += object1[d] * object2[d];
		}

		// TODO divide by 0?
		similarity = sumAxB / (Math.sqrt(sumAxA) * Math.sqrt(sumBxB));

		distance = (2 * (Math.acos(similarity) / Math.PI));

		return distance;
	}

	@Override
	public String toString() {
		return "Cosine: " + dims;
	}
}
