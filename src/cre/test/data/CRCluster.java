package cre.test.data;

import java.io.Serializable;
import java.util.Observable;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;



public class CRCluster extends Observable implements Serializable, Comparable<CRCluster>, ObservableValue<CRCluster> {
	
	private static final long serialVersionUID = 1L;

	public int c1;
	public int c2;
	
	public CRCluster(int c1, int c2) {
		this.c1 = c1;
		this.c2 = c2;
	}

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
	
	@Override
	public boolean equals(Object obj) {
		CRCluster o = (CRCluster)obj;
		return ((this.c1==o.c1) && (this.c2==o.c2));
	}
	
	@Override
	public int hashCode() {
		return c1*10111+c2;
	}

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
