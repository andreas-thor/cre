package main.cre.data.type.mm;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;


public class CRCluster implements Serializable, Comparable<CRCluster> {  
	
	private static final long serialVersionUID = 1L;

	private Set<CRType_MM> crSet;

	private int c1;
	private int c2;
	
	public CRCluster(CRType_MM cr) {
		this.crSet = new HashSet<CRType_MM>();
		this.crSet.add(cr);
		this.c1 = cr.getID();
		this.c2 = this.c1;
	}

	public CRCluster (CRType_MM cr, int c1) {
		this (cr);
		this.c1 = c1;
	}
	
	
//	public CRCluster (CRType_MM cr, int c1, int c2) {
//		this (cr);
//		this.c1 = c1;
//		this.c2 = c2;
//	}

	public void merge (CRCluster clus) {
		this.crSet.addAll(clus.crSet);
		this.c1 = Math.min (this.c1, clus.c1);
		this.c2 = this.crSet.stream().mapToInt(cr -> cr.getID()).min().getAsInt();
		clus.crSet.forEach(cr -> cr.setCluster(this));
	}
	
	
	/**
	 * Retrieves the CR with the highest number of citations (picks the one with the smaller id if there are several highest cited CRs)
	 * @return
	 */
	public CRType_MM getMainCR () {
		return getCR().max((c1, c2) -> (c1.getN_CR()>c2.getN_CR()) || ((c1.getN_CR()==c2.getN_CR()) && (c1.getID()<c2.getID())) ? +1 : -1).orElse(null);
	}
	

	public Stream<CRType_MM> getCR() {
		return this.crSet.stream();
	}

	public int getC1 () {
		return this.c1;
	}

	public int getC2 () {
		return this.c2;
	}
	
	public int getSize() {
		return crSet.size();
	}
	
	@Override
	public String toString() {
		return c1 + "/" + c2;
	}
	
	@Override
	public int compareTo(CRCluster o) {
		if (this.c1<o.c1) return -1;
		if (this.c1>o.c1) return +1;
		if (this.c2<o.c2) return -1;
		if (this.c2>o.c2) return +1;
		return 0;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		CRCluster o = (CRCluster)obj;
		return ((this.c1==o.c1) && (this.c2==o.c2));
	}
	
	@Override
	public int hashCode() {
		return c1*10111+c2;
	}
	
	
}

