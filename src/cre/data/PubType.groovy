package cre.data


import java.util.List;

import groovy.transform.CompileStatic

@CompileStatic
public class PubType {

	public List<CRType> crList

	String AU;	// Authors
	String TI; 	// Title
	Integer PY; 	// Year
	String SO;	// Source title
	String VL;	// Volume
	String IS; 	// Issue
	Integer BP;		// Beginning Page / Page Start
	Integer EP;		// Ending Page / Page End
	Integer PG;		// Page Count
	Integer NR;		// Number of References
	String DOI;
	String AB;	// Abstract
	String DT;	// Document Typs
	
	public int length	// approx. size for import status bar

	
	public PubType() {
		
		super();
		this.crList = new ArrayList<CRType>();
		AU = null;
		TI = null;
		PY = null;
		SO = null;
		VL = null;
		IS = null;
		BP = null;
		EP = null;
		PG = null;
		NR = null;
		DOI = null;
		AB = null;
		DT = null;
	}

	
	public PubType(List<CRType> crList, String aU, String tI, Integer pY, String sO, String vL, String iS, Integer bP, Integer eP,
			Integer pG, Integer nR, String dOI, String aB, String dT) {
			
		super();
		this.crList = crList;
		AU = aU;
		TI = tI;
		PY = pY;
		SO = sO;
		VL = vL;
		IS = iS;
		BP = bP;
		EP = eP;
		PG = pG;
		NR = nR;
		DOI = dOI;
		AB = aB;
		DT = dT;
	}

	
	
	
	
//	public StringBuffer export
//	public int year
//	public int length
//	public HashMap<String, String> entries

	
		
//	public PubType() {
//		super()
//		this.crList = new ArrayList<CRType>()
//		this.year = 0
//		this.length = 0
//		this.export = new StringBuffer()
//		this.entries = [:]
//
//	}
}