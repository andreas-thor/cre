package cre.test.data.match;

import cre.test.data.type.CRType;

public class CRPair2 {
	CRType cr1;
	CRType cr2;
	Double s;

	public CRPair2(CRType cr1, CRType cr2, Double s) {
		super();
		
		if (cr1.getID()<cr2.getID()) {
			this.cr1 = cr1;
			this.cr2 = cr2;
		} else {
			this.cr1 = cr2;
			this.cr2 = cr1;
		}
		this.s = s;
	}
	

	
	public String toString () {
		return (this.cr1.getID() + "/" + this.cr2.getID() + "/" + this.s);
	}
	
}