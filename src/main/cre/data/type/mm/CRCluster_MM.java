package main.cre.data.type.mm;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import main.cre.data.type.abs.CRCluster;
import main.cre.data.type.abs.CRType;



public class CRCluster_MM extends CRCluster  {
	
	private static final long serialVersionUID = 1L;

	private Set<CRType> crSet;

	
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
	

	@Override
	public void merge (CRCluster crc) {
		CRCluster_MM clus = (CRCluster_MM) crc;
		this.crSet.addAll(clus.crSet);
		this.c1 = Math.min (this.c1, clus.c1);
		this.c2 = this.crSet.stream().mapToInt(cr -> cr.getID()).min().getAsInt();
		clus.crSet.forEach(cr -> cr.setCID2(this));
	}
	
	
	/**
	 * Retrieves the CR with the highest number of citations (picks one at random if there are several highest cited CRs)
	 * @return
	 */
	public CRType getMainCR () {
		
		int maxN_CR = -1;
		CRType result = null;
		for (CRType cr: this.crSet) {
			if (cr.getN_CR()>maxN_CR) {
				maxN_CR = cr.getN_CR();
				result = cr;
			}
		};
		return result;
	}
	
	@Override
	public int getSize() {
		return crSet.size();
	}
	
	
	@Override
	public Stream<CRType> getCR() {
		return this.crSet.stream();
	}
	
	
}
