package cre.data


import java.util.List

import com.orsoncharts.util.json.JSONObject;

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.transform.CompileStatic


/*
Var		WoS		Scopus 							Conversion		Description
---------------------------------------------------------------------------------------------------------
PT		PT 		--												Publication Type (conference, book, journal, book in series, or patent)
AU		AU 		Authors							+				Authors
AF		AF 		--								+				Author Full Name
C1		C1 		Authors with Affiliations		+				Author Address		
AA		--		Affiliations									Affiliations
TI		TI 		Title											Document Title
PY		PY 		Year											Year Published
SO		SO 		Source Title									Publication Name
VL		VL 		Volume											Volume
IS		IS 		Issue											Issue
AR		AR		Art. No.										Article Number
BP		BP 		Page start										Beginning Page
EP		EP 		Page end										Ending Page
PG		PG 		Page count										Page Count
TC		TC 		Cited By										Times Cited
CR		CR 		References						+				Cited References

DI		DI 		DOI												Digital Object Identifier (DOI)
LI		--		Link											Link (URL) 
AB		AB 		Abstract										Abstract
DE 		DE		Author Keywords									Author Keywords
DT		DT 		Document Type									Document Type
FS		--		Source											File Source 
UT		UT		EID												Unique Article Identifier
 */


@CompileStatic
public class PubType {

	String PT	// Publication Type (WoS only)
	
	String[] AU	= [] // Authors; each author has format: "<lastname>, <initials_without_dots>"
	String[] AF	= [] // Authors Full Name; format: "<lastname>, <firstnames>
	List<String[]> C1 = [] // Authors with Affiliations / Adresses; format: "array ("<lastname>, <firstnames>]", "<affiliation>")
	List<String> AA	// All affiliations	(Scopus only)
	
	String TI 	// Title
	Integer PY 	// Year
	
	String SO	// Source title
	String VL	// Volume
	String IS 	// Issue
	String AR 	// Article Number
	
	Integer BP	// Beginning Page / Page Start
	Integer EP	// Ending Page / Page End
	Integer PG	// Page Count
	
	Integer TC	// Times Cited
	public List<CRType> crList = new ArrayList<CRType>()
	
	String DI		// Digital Object Identifier (DOI)
	String LI	// Link	(Scopus only)
	String AB	// Abstract
	String DE	// Author Keywords
	
	String DT	// Document Typs
	String FS	// File Source	(Scopus only)
	String UT	// Unique Article Identifier

	
	public int length	// approx. size for import status bar



	public PubType parseWoS (HashMap<String, ArrayList<String>> entries, int length, int[] yearRange) {
		
		this.length = length
		
		PT = entries.get("PT")
		
		AU = entries.get("AU")
		AF = entries.get("AF")
		C1 = []
		AA = []
		// pattern: [author1; author2; ...] affiliation
		entries.get("C1")?.each {
			int pos = it.indexOf(']')
			if (pos>0) {
				String names = it.substring(1, pos-1)
				String affiliation = it.substring (pos+2)
				names.split("; ").collect { String name -> 
					C1 << ([name, affiliation] as String[])
					AA << affiliation
				}
			}
		}
		
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
		if (entries.get("CR") != null) {
			crList = entries.get("CR")?.collect { String it -> new CRType().parseWoS (it, yearRange) }?.findAll { CRType it -> it != null } as List
		}

		DI = entries.get("DOI")?.join(" ")
		LI = entries.get("LI")?.join(" ")
		AB = entries.get("AB")?.join(" ")
		DE = entries.get("DE")?.join(" ")
		
		DT = entries.get("DT")?.join(" ")
		FS = "WoS"
		UT = entries.get("UT")?.join(" ")

		this
	}
		
	public Map<String, ArrayList<String>> getWoS() {
		
		/* Title field: max length = 70 chars per line */
		ArrayList<String> linesTI = []
		String title = new String(TI?:"")
		while (true) {
			if (title.length()<=70) { 
				linesTI.add(title)
				break
			}
			
			int maxLength = 70
			int pos = title.lastIndexOf(' ', maxLength)
			if (pos > 0) {
				linesTI.add (title.substring(0,  pos))
				title = title.substring(pos+1)
			} else {
				linesTI.add (title.substring(0,  maxLength))
				title = title.substring(maxLength)
			}
		} 
		
		
		[
			"PT": [PT],	
			"AU": AU,
			"AF": AF,
			"C1": C1.collect { String[] it -> "[" + it[0] + "] " + it[1]},	// TODO: Group together authors with same affiliation
			"TI": linesTI,
			"PY": [PY?.toString()],
			"SO": [SO],
			"VL": [VL],
			"IS": [IS?.toString()],
			"BP": [BP?.toString()],
			"EP": [EP?.toString()],
			"PG": [PG?.toString()],
			"TC": [TC?.toString()],
			"CR": crList.collect { CRType cr -> cr.getWoS() },
			"NR": [crList.size().toString()],
			"DI": [DI],
			"AB": [AB],
			"DE": [DE],
			"DT": [DT],
			"UT": [UT]
		]
	}
	
	
	
	public PubType parseScopus (HashMap<String, String> entries, int length, int[] yearRange) {
		
		this.length = length
		
		PT = "J" // TODO: what is the default Publication Type? (No value in scopus!)
		
		// Scopus Authors: Lastname1, I1., Lastname2, I2.I2., ...
		AU = entries.get('Authors').split("\\., ").collect { String name -> name.replaceAll("\\.", "") }
		AF = AU		// there are no full names in Scopus 

		// Authors with affiliations: "<lastname>, <initials with dots>, affiliation"
		C1 = []
		entries.get('Authors with affiliations')?.each {String it ->
			String[] split = it.split(", ", 3)
			if (split.length == 3) {
				C1 << ([(split[0]+", "+split[1].replaceAll("\\.", "")), split[2]] as String[])
			}
		}
		AA = entries.get('Affiliations').split("; ") as List
		
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
		crList = entries.get('References')?.split(";").collect { String it -> new CRType().parseScopus (it, yearRange) }.findAll { CRType it -> it != null } as List

		DI = entries.get('DOI')
		LI = entries.get('Link')
		AB = entries.get('Abstract')
		DE = entries.get('Author Keywords')  
		
		DT = entries.get('Document Type')
		FS = entries.get('Source')
		UT = entries.get('EID')
		
		this
	}
	

	public String[] getScopus () {
		[
			// add dots to each initial
			(AU != null) ? AU.collect { String a ->
				String[] split = a.split(", ", 2)
				if (split.size()==2) {
					split[0] + ", " + split[1].replaceAll("([A-Z])", "\$1.") 
				} else {
				 a
				}
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
			(AA==null)?"":AA.join ("; "),
			(C1==null)?"":C1.collect { String[] it -> it[0] + ", " + it[1]}.join ("; "),
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
			PT: this.PT,
			AU: this.AU,
			AF: this.AF,
			C1: this.C1,
			AA: this.AA,
			TI: this.TI,
			PY: this.PY,
			SO: this.SO,
			VL: this.VL,
			IS: this.IS,
			AR: this.AR,
			BP: this.BP,
			EP: this.EP,
			PG: this.PG,
			TC: this.TC,
			CRLIST: crList.collect { it.ID },
			DI: this.DI,
			LI: this.LI,
			AB: this.AB,
			DE: this.DE,
			DT: this.DT,
			FS: this.FS,
			UT: this.UT
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