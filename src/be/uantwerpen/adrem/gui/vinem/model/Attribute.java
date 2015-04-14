package be.uantwerpen.adrem.gui.vinem.model;

public class Attribute implements Comparable<Attribute> {
	public int ix;
	public String name;

	public Attribute(int ix) {
		this(ix, "Dim " + ix);
	}

	public Attribute(int ix, String name) {
		this.ix = ix;
		this.name = name;
	}

	@Override
	public int compareTo(Attribute o) {
		return Integer.compare(this.ix, o.ix);
	}

	@Override
	public String toString() {
		return "(" + ix + ") " + name;
	}
}
