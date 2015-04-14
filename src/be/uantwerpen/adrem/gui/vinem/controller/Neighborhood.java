package vinem.controller;

public enum Neighborhood {
	KNN("k-NN"), Radius("eps-N");

	public final String name;

	Neighborhood(String name) {
		this.name = name;
	}
}
