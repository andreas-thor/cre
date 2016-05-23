package cre.data


import java.util.List

import groovy.json.JsonOutput
import groovy.transform.CompileStatic

@CompileStatic
public class PubType {

	public List<CRType> crList

	String[] AU	// Authors; each author has format: "<lastname>, <initials_without_dots>"
	String TI 	// Title
	Integer PY 	// Year
	String SO	// Source title
	String VL	// Volume
	String IS 	// Issue
	Integer BP		// Beginning Page / Page Start
	Integer EP		// Ending Page / Page End
	Integer PG		// Page Count
	Integer NR		// Number of References
	String DI
	String AB	// Abstract
	String DT	// Document Typs
	
	// TODO: adjust import/export!!!
	Integer TC	// Times Cited
	String AR 	// Article Number
	String LI	// Link
	String AF	// Authors Full Name
	String AA	// Authors with affiliations
	String DE	// Author Keywords
	String UT	// Unique Article Identifier
	String FS	// File Source
	
	public int length	// approx. size for import status bar

	
	public String getJSON () {
		
		JsonOutput.toJson([
			TI: this.TI,
			PY: this.PY,
			SO: this.SO,
			VL: this.VL,
			IS: this.IS,
			BP: this.BP,
			EP: this.EP,
			PG: this.PG,
			NR: this.NR,
			DI: this.DI,
			AP: this.AB,
			DT: this.DT,
			AU: this.AU
			]);
		
	}
	
	
	
	public PubType() {
		
		super()
		this.crList = new ArrayList<CRType>()
		AU = null
		TI = null
		PY = null
		SO = null
		VL = null
		IS = null
		BP = null
		EP = null
		PG = null
		NR = null
		DI = null
		AB = null
		DT = null
		TC = null
		AR = null
		LI = null
		AF = null
		AA = null
		DE = null
		UT = null
		FS = null
	}

	

	public PubType(List<CRType> crList, String[] aU, String tI, Integer pY, String sO, String vL, String iS, Integer bP,
			Integer eP, Integer pG, Integer nR, String dO, String aB, String dT, Integer tC, String aR, String lI, String aF,
			String aA, String dE, String uT, String fS) {
			
		super()
		this.crList = crList
		this.AU = aU
		this.TI = tI
		this.PY = pY
		this.SO = sO
		this.VL = vL
		this.IS = iS
		this.BP = bP
		this.EP = eP
		this.PG = pG
		this.NR = nR
		this.DI = dO
		this.AB = aB
		this.DT = dT
		this.TC = tC
		this.AR = aR
		this.LI = lI
		this.AF = aF
		this.AA = aA
		this.DE = dE
		this.UT = uT
		this.FS = fS
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