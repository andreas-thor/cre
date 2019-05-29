package main.cre.data.type.mm.clustering;

import main.cre.data.type.mm.CRType_MM;

public class CRPair2 {
	
	CRType_MM cr1;
	CRType_MM cr2;
	Double s;

	public CRPair2(CRType_MM cr1, CRType_MM cr2, Double s) {
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
	

	@Override
	public String toString () {
		return (this.cr1.getID() + "/" + this.cr2.getID() + "/" + this.s);
	}
	
}