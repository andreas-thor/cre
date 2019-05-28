package main.cre.data.type.db;

import javafx.beans.property.SimpleIntegerProperty;
import main.cre.data.type.abs.CRCluster;
import main.cre.data.type.abs.CRType;
import main.cre.data.type.mm.CRType_Member;

public class CRType_DB extends CRType_Member {

	private CRCluster_DB CID2;
	
	@Override
	public CRCluster getCID2() {
		return CID2;
	}
	
	@Override
	public void setCID2(CRCluster cID2) {
		CID2 = (CRCluster_DB) cID2;
	}
	
	@Override
	public void setCID2(String s) {
		CID2 = new CRCluster_DB (s);
	}
	
	@Override
	public  void setCID2(CRType cr) {
		CID2 = new CRCluster_DB (cr.getID());

	}

	public void setCID2(int c1, int c2, int size) {
		CID2 = new CRCluster_DB(c1, c2, size);
	}
	
	
	@Override
	public void setCID2(CRType cr, int c1) {
		CID2 = new CRCluster_DB(c1, cr.getCID2().getC2(), 1);
	}
	
	@Override
	public int getCID_S() {
		return CID2.getSize();
	}
	
	@Override
	public SimpleIntegerProperty getCID_SProp() {
		return new SimpleIntegerProperty(CID2.getSize());
	}
	
}
