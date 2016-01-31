package be.uantwerpen.adrem.gui.vinem.controller;

/**
 * Neighborhood type.
 * 
 * @author M. Emin Aksehirli
 */

public enum Neighborhood {
	KNN("k-NN"), Radius("eps-N");

	public final String name;

	Neighborhood(String name) {
		this.name = name;
	}
}
