package cre.test.data.match;

class CRPair {
	Integer id1;
	Integer id2;
	Double s;

	public CRPair(Integer id1, Integer id2, Double s) {
		super();
		this.id1 = id1;
		this.id2 = id2;
		this.s = s;
	}
	
	public CRPair (String s) {
		String[] split = s.split ("/");
		this.id1 = Integer.valueOf(split[0]);
		this.id2 = Integer.valueOf(split[1]);
		this.s = Double.valueOf(split[2]);
	}
	
	public String toString () {
		return (this.id1 + "/" + this.id2 + "/" + this.s);
	}
	
}