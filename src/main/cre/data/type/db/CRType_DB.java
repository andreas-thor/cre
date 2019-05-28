package main.cre.data.type.db;

import java.util.stream.Stream;

import main.cre.data.type.abs.CRType;

public class CRType_DB extends CRType<PubType_DB> {

	private CRCluster_DB CID2;
	private int N_CR;
	
	public CRType_DB() {
		super();
		this.N_CR = 0;
	}
	
//	@Override
//	public CRCluster getCID2() {
//		return CID2;
//	}
//	
//	@Override
//	public void setCID2(CRCluster cID2) {
//		CID2 = (CRCluster_DB) cID2;
//	}
	
	@Override
	public void setCID2(String s) {
		CID2 = new CRCluster_DB (s);
	}
	
	@Override
	public  void setCID2(CRType cr) {
		CID2 = new CRCluster_DB (cr.getID());
	}

//	public void setCID2(int c1, int c2, int size) {
//		CID2 = new CRCluster_DB(c1, c2, size);
//	}
//	
//	
//	@Override
//	public void setCID2(CRType cr, int c1) {
//		CID2 = new CRCluster_DB(c1, cr.getCID2().getC2(), 1);
//	}
	
	
	@Override
	public String getCID_String() {
		return CID2.toString();
	}
	
	@Override
	public int getCID_S() {
		return CID2.getSize();
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
