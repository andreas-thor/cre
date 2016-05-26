package cre.data


import java.util.List

import com.orsoncharts.util.json.JSONObject;

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.transform.CompileStatic

@CompileStatic
public class PubType {

	public List<CRType> crList = new ArrayList<CRType>()

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



	public PubType parseWoS (HashMap<String, ArrayList<String>> entries, int length, int[] yearRange) {
		
		this.length = length
		
		AU = entries.get("AU")
		TI = entries.get("TI")?.join(" ")
		PY = entries.get("PY")?.get(0)?.isInteger() ? entries.get("PY").get(0).toInteger() : null
		SO = entries.get("SO")?.join(" ")
		VL = entries.get("VL")?.join(" ")
		IS = entries.get("IS")?.join(" ")
		AR = entries.get("AR")?.join(" ")
		BP = entries.get("BP")?.get(0)?.isInteger() ? entries.get("BP").get(0).toInteger() : null
		EP = entries.get("EP")?.get(0)?.isInteger() ? entries.get("EP").get(0).toInteger() : null
		PG = entries.get("PG")?.get(0)?.isInteger() ? entries.get("PG").get(0).toInteger() : null
		TC = entries.get("TC")?.get(0)?.isInteger() ? entries.get("TC").get(0).toInteger() : null
		DI = entries.get("DOI")?.join(" ")
		LI = entries.get("LI")?.join(" ")
		AF = entries.get("AF")?.join(" ")
		AA = null // Scopus only
		AB = entries.get("AB")?.join(" ")
		DE = entries.get("DE")?.join(" ")
		DT = entries.get("DT")?.join(" ")
		FS = "WoS"
		UT = entries.get("UT")?.join(" ")
			
		if (TI == null) return null 
	
		if (entries.get("CR") != null) {
			crList = entries.get("CR")?.collect { String it -> new CRType().parseWoS (it, yearRange) }?.findAll { CRType it -> it != null } as List
		}
		
		this
	}
		
	public HashMap<String, ArrayList<String>> getWoS() {
		
		[
			"AU": AU,
			"TI": [TI],
			"PY": [PY?.toString()],
			"SO": [SO],
			"VL": [VL],
			"IS": [IS?.toString()],
			"BP": [BP?.toString()],
			"EP": [EP?.toString()],
			"PG": [PG?.toString()],
			"CR": crList.collect { CRType cr -> cr.getWoS() },
			"NR": [crList.size().toString()],
			"DI": [DI],
			"AB": [AB],
			"DT": [DT]
		]
	}
	
	
	
	public PubType parseScopus (HashMap<String, String> entries, int length, int[] yearRange) {
		
		this.length = length
		
		// Scopus Authors: Lastname1, I1., Lastname2, I2.I2., ...
		AU = entries.get('Authors').split("\\., ").collect { String name -> name.replaceAll("\\.", "") }
		
		TI = entries.get('Title')
		PY = entries.get('Year')?.isInteger() ? entries.get('Year').toInteger() : null
		SO = entries.get('Source title')
		VL = entries.get('Volume')
		IS = entries.get('Issue')
		AR = entries.get('Art. No.')
		BP = entries.get('Page start')?.isInteger() ? entries.get('Page start').toInteger() : null
		EP = entries.get('Page end')?.isInteger() ? entries.get('Page end').toInteger() : null
		PG = entries.get('Page count')?.isInteger() ? entries.get('Page count').toInteger() : null
		TC = entries.get('Cited By')?.isInteger() ? entries.get('Cited By').toInteger() : null
		DI = entries.get('DOI')
		LI = entries.get('Link')
		AF = entries.get('Affiliations')	// TODO: Is this correct???
		AA = entries.get('Authors with affiliations')
		AB = entries.get('Abstract')
		DE = entries.get('Author Keywords')  
		DT = entries.get('Document Type')
		FS = entries.get('Source')
		UT = entries.get('EID')
		
		crList = entries.get('References')?.split(";").collect { String it -> new CRType().parseScopus (it, yearRange) }.findAll { CRType it -> it != null } as List
		
		this
	}
	

	public String[] getScopus () {
		[
			// add dots to each initial
			(AU != null) ? AU.collect { String a ->
				String[] split = a.split(", ", 2)
				(split.size()==2) ? split[0] + ", " + split[1].replaceAll("([A-Z])", "\$1.") : a
			}.join(', ') : "",
			TI?:"",
			PY?:"",
			SO?:"",
			VL?:"",
			IS?:"",
			AR?:"",
			BP?:"",
			EP?:"",
			PG?:"",
			TC?:"",
			DI?:"",
			LI?:"",
			AF?:"",
			AA?:"",
			AB?:"",
			DE?:"",
			crList.collect { CRType cr -> cr.getScopus() }.join("; "),
			DT?:"",
			FS?:"",
			UT?:""
		] as String[]
	}
	
	
	
	public JSONObject getJSON () {
		
		JsonBuilder jb = new JsonBuilder()
		
		jb (
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
			AU: this.AU,
			CRLIST: crList.collect { it.ID }
		) as JSONObject
		
	}
	
	
//	public PubType() {
//		
//		super()
//		this.crList = new ArrayList<CRType>()
//		AU = null
//		TI = null
//		PY = null
//		SO = null
//		VL = null
//		IS = null
//		BP = null
//		EP = null
//		PG = null
//		NR = null
//		DI = null
//		AB = null
//		DT = null
//		TC = null
//		AR = null
//		LI = null
//		AF = null
//		AA = null
//		DE = null
//		UT = null
//		FS = null
//	}
//
//	
//
//	public PubType(List<CRType> crList, String[] aU, String tI, Integer pY, String sO, String vL, String iS, Integer bP,
//			Integer eP, Integer pG, Integer nR, String dO, String aB, String dT, Integer tC, String aR, String lI, String aF,
//			String aA, String dE, String uT, String fS) {
//			
//		super()
//		this.crList = crList
//		this.AU = aU
//		this.TI = tI
//		this.PY = pY
//		this.SO = sO
//		this.VL = vL
//		this.IS = iS
//		this.BP = bP
//		this.EP = eP
//		this.PG = pG
//		this.NR = nR
//		this.DI = dO
//		this.AB = aB
//		this.DT = dT
//		this.TC = tC
//		this.AR = aR
//		this.LI = lI
//		this.AF = aF
//		this.AA = aA
//		this.DE = dE
//		this.UT = uT
//		this.FS = fS
//	}

	


	
	
	
	
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