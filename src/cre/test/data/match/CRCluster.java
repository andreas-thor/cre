package cre.test.data.match;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.stream.Stream;

import cre.test.data.type.CRType;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;



public class CRCluster extends Observable implements Serializable, Comparable<CRCluster>, ObservableValue<CRCluster> {
	
	private static final long serialVersionUID = 1L;

	public int c1;
	public int c2;
	
	private Set<CRType> crSet;
	private SimpleIntegerProperty CID_S;

	
	public CRCluster (CRType cr) {
		this.crSet = new HashSet<CRType>();
		this.crSet.add(cr);
		this.CID_S = new SimpleIntegerProperty(1);
		this.c1 = cr.getID();
		this.c2 = this.c1;
	}

	public CRCluster (CRType cr, int c1) {
		this (cr);
		this.c1 = c1;
	}
	

	public void merge (CRCluster clus) {
		this.crSet.addAll(clus.crSet);
		this.c1 = Math.min (this.c1, clus.c1);
		this.c2 = this.crSet.stream().mapToInt(cr -> cr.getID()).min().getAsInt();
		this.CID_S = new SimpleIntegerProperty(this.crSet.size());
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
	
	public SimpleIntegerProperty getCID_SProp() {
		return CID_S;
	}
	
	
	public Stream<CRType> getCR() {
		return this.crSet.stream();
	}
	

//	public CRCluster(int c1, int c2) {
//		this.c1 = c1;
//		this.c2 = c2;
//	}

	public CRCluster(String s) {
		String[] split = s.split ("/");
		this.c1 = Integer.valueOf(split[0]).intValue();
		this.c2 = Integer.valueOf(split[1]).intValue();
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
	
//	@Override
//	public boolean equals(Object obj) {
//		CRCluster o = (CRCluster)obj;
//		return ((this.c1==o.c1) && (this.c2==o.c2));
//	}
//	
//	@Override
//	public int hashCode() {
//		return c1*10111+c2;
//	}

	@Override
	public void addListener(InvalidationListener arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeListener(InvalidationListener arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListener(ChangeListener<? super CRCluster> listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CRCluster getValue() {
		return this;
	}

	@Override
	public void removeListener(ChangeListener<? super CRCluster> listener) {
		// TODO Auto-generated method stub
		
	}
	
}
