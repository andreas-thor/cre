package main.cre.data.type.abs;

import java.io.Serializable;
import java.util.stream.Stream;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public abstract class CRCluster implements Serializable, Comparable<CRCluster>, ObservableValue<CRCluster> {

	
	private static final long serialVersionUID = 1L;

	protected int c1;
	protected int c2;
	
	
	public abstract Stream<CRType> getCR();
	
	public abstract int getSize();
	
	public abstract CRType getMainCR();
	
	public abstract void merge (CRCluster crc);
	
	
	public int getC1 () {
		return this.c1;
	}

	public int getC2 () {
		return this.c2;
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
	public CRCluster getValue() {
		return this;
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
	public void removeListener(ChangeListener<? super CRCluster> listener) {
		// TODO Auto-generated method stub
	}




}
