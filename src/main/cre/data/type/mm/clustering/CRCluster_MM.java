package main.cre.data.type.mm.clustering;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import main.cre.data.type.mm.CRType_MM;



public class CRCluster_MM implements Serializable, Comparable<CRCluster_MM> {  
	
	private static final long serialVersionUID = 1L;

	private Set<CRType_MM> crSet;

	private int c1;
	private int c2;
	
	public CRCluster_MM(CRType_MM cr) {
		this.crSet = new HashSet<CRType_MM>();
		this.crSet.add(cr);
		this.c1 = cr.getID();
		this.c2 = this.c1;
	}

	public CRCluster_MM (CRType_MM cr, int c1) {
		this (cr);
		this.c1 = c1;
	}
	
	


	/*
	public CRCluster_MM (CRType cr) {
		this.crSet = new HashSet<CRType>();
		this.crSet.add(cr);
		this.c1 = cr.getID();
		this.c2 = this.c1;
	}

	public CRCluster_MM (CRType cr, int c1) {
		this (cr);
		this.c1 = c1;
	}
	
	
	public CRCluster_MM(String s) {
		String[] split = s.split ("/");
		this.c1 = Integer.valueOf(split[0]).intValue();
		this.c2 = Integer.valueOf(split[1]).intValue();
	}
*/	

	public void merge (CRCluster_MM clus) {
		this.crSet.addAll(clus.crSet);
		this.c1 = Math.min (this.c1, clus.c1);
		this.c2 = this.crSet.stream().mapToInt(cr -> cr.getID()).min().getAsInt();
		clus.crSet.forEach(cr -> cr.setCluster(this));
	}
	
	
	/**
	 * Retrieves the CR with the highest number of citations (picks one at random if there are several highest cited CRs)
	 * @return
	 */
	public CRType_MM getMainCR () {
		
		int maxN_CR = -1;
		CRType_MM result = null;
		for (CRType_MM cr: this.crSet) {
			if (cr.getN_CR()>maxN_CR) {
				maxN_CR = cr.getN_CR();
				result = cr;
			}
		};
		return result;
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
	public int compareTo(CRCluster_MM o) {
		if (this.c1<o.c1) return -1;
		if (this.c1>o.c1) return +1;
		if (this.c2<o.c2) return -1;
		if (this.c2>o.c2) return +1;
		return 0;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		CRCluster_MM o = (CRCluster_MM)obj;
		return ((this.c1==o.c1) && (this.c2==o.c2));
	}
	
	@Override
	public int hashCode() {
		return c1*10111+c2;
	}
	
	
}
