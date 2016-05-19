package cre.data

import java.io.Serializable

import groovy.transform.CompileStatic


@CompileStatic
public class CRCluster implements Serializable, Comparable<CRCluster>  {
	
	int c1
	int c2
	
	public CRCluster (int c1, int c2) {
		this.c1 = c1
		this.c2 = c2
	}

	@Override
	public String toString() {
		return "${c1}/${c2}"
	}
	
	@Override
	public int compareTo(CRCluster o) {
		if (this.c1<o.c1) return -1
		if (this.c1>o.c1) return +1
		if (this.c2<o.c2) return -1
		if (this.c2>o.c2) return +1
		return 0
	}
	
	@Override
	public boolean equals(Object obj) {
		CRCluster o = (CRCluster)obj
		return ((this.c1==o.c1) && (this.c2=o.c2))
	}
	
	@Override
	public int hashCode() {
		return c1*10111+c2
	}
	
}
