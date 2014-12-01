package cart.gui2;

import java.util.Collection;

public class CosineDistance extends Dissimilarity {

	public CosineDistance(int[] dims) {
		super(dims);
	}

	public CosineDistance(Collection<Integer> dims) {
		super(dims);
	}

	@Override
	public double between(double[] object1, double[] object2) {
		// avoid division by 0
		double sumAxA = 0.000001;
		double sumBxB = 0.000001;
		double sumAxB = 0;

		for (int d : dims) {
			sumAxA += object1[d] * object1[d];
			sumBxB += object2[d] * object2[d];
			sumAxB += object1[d] * object2[d];
		}

		double similarity = sumAxB / (Math.sqrt(sumAxA) * Math.sqrt(sumBxB));
		double distance = (2 * (Math.acos(similarity) / Math.PI));

		return distance;
	}

	@Override
	public String getName() {
		return "Cosine";
	}
}
