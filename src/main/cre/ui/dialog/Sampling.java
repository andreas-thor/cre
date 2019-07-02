package main.cre.ui.dialog;

public enum Sampling {
	NONE("Import all (no sampling)"),
	RANDOM("Random Sampling (set Number of Cited References ==>)"),
	SYSTEMATIC("Systematic Sampling (set Number of Cited References ==>)"),
	CLUSTER("Cluster Sampling (all References of a random Year, PY)");
	
	public String label;
	public int offset;
	
	Sampling(String label) {
		this.label = label;
		this.offset = 0;
	}
}