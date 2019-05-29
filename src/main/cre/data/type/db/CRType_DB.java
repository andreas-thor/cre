package main.cre.data.type.db;

import java.util.stream.Stream;

import main.cre.data.type.abs.CRType;

public class CRType_DB extends CRType<PubType_DB> {

	private int N_CR;
	private int c1;
	private int c2;
	private int clusterSize;
	
	public CRType_DB() {
		super();
		this.N_CR = 0;
	}
	

	public void setCluster(int c1, int c2, int size) {
		this.c1 = c1;
		this.c2 = c2;
		this.clusterSize = size;
	}

	@Override
	public int getClusterC1() {
		return c1;
	}

	@Override
	public int getClusterC2() {
		return c2;
	}
	
	@Override
	public int getClusterSize() {
		return clusterSize;
	}
	

	


	@Override
	public Stream<PubType_DB> getPub() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addPub(PubType_DB pubType, boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean removePub(PubType_DB pub, boolean inverse) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeAllPubs(boolean inverse) {
		// TODO Auto-generated method stub
		
	}

	
	public void setN_CR(int n_CR) {
		this.N_CR = n_CR;
	}
	
	@Override
	public int getN_CR() {
		return this.N_CR;
	}




	
	
	
}
