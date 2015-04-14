package be.uantwerpen.adrem.gui.vinem.model;

public class Obj {
	public int id;
	public String name;

	public Obj(int id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
