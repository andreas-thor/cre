package cre.obsolete


import groovy.json.JsonBuilder
import groovy.transform.CompileStatic

import java.util.regex.Matcher

import com.orsoncharts.util.json.JSONObject


/*
Var		WoS		Scopus 							Conversion		Description
---------------------------------------------------------------------------------------------------------
PT		PT 		--												Publication Type (conference, book, journal, book in series, or patent)
AU		AU 		Authors							+				Authors
AF		AF 		--								+				Author Full Name
C1		C1 		Authors with Affiliations		+				Author Address		
EM		EM		Authors with Affiliations       + 				E-mail
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
public class PubTypeG {

	public String PT	// Publication Type (WoS only)
	
	public List<String> AU	= [] // Authors; each author has format: "<lastname>, <initials_without_dots>"
	public List<String> AF	= [] // Authors Full Name; format: "<lastname>, <firstnames>
	public List<String[]> C1 = [] // Authors with Affiliations / Adresses; format: "array ("<lastname>, <firstnames>]", "<affiliation>")
	public List<String> EM = [] // E-Mail Adressess
	public List<String> AA	// All affiliations	(Scopus only)
	
	public String TI 	// Title
	public Integer PY 	// Year
	
	public String SO	// Source title
	public String VL	// Volume
	public String IS 	// Issue
	public String AR 	// Article Number
	
	public Integer BP	// Beginning Page / Page Start
	public Integer EP	// Ending Page / Page End
	public Integer PG	// Page Count
	
	public Integer TC	// Times Cited
	public ArrayList<CRType> crList = new ArrayList<CRType>()
	
	public String DI	// Digital Object Identifier (DOI)
	public String LI	// Link	(Scopus only)
	public String AB	// Abstract
	public String DE	// Author Keywords
	
	public String DT	// Document Typs
	public String FS	// File Source	(Scopus only)
	public String UT	// Unique Article Identifier

	
	public int length	// approx. size for import status bar


	
	
	public static PubType createWoS (List<String> it, int[] yearRange) {
		
		String currentTag = "";
		String tagBlock = "";
		String value = "";
			
		PubType pub = new PubType();
		pub.FS = "WoS";
		pub.length = 0;
		List<String> C1 = new ArrayList<String>();
		
		for (String l: it) {
			pub.length += 1 + l.length();
			if (l.length()<2) continue;
			currentTag = l.substring(0, 2);
			if (currentTag.equals("ER")) continue;
			if (currentTag.equals("EF")) continue;
			tagBlock = currentTag.equals("  ") ? tagBlock : new String(currentTag);
			value = l.substring(3);
			
			
			switch (tagBlock) {
			
			case "PT": pub.PT = value; break;
			
			/* Concatenated Strings */
			case "TI": pub.TI = (pub.TI==null) ? value : pub.TI+" "+value; break;
			case "SO": pub.SO = (pub.SO==null) ? value : pub.SO+" "+value; break;
			case "VL": pub.VL = (pub.VL==null) ? value : pub.VL+" "+value; break;
			case "IS": pub.IS = (pub.IS==null) ? value : pub.IS+" "+value; break;
			case "AR": pub.AR = (pub.AR==null) ? value : pub.AR+" "+value; break;
			case "DI": pub.DI = (pub.DI==null) ? value : pub.DI+" "+value; break;
			case "LI": pub.LI = (pub.LI==null) ? value : pub.LI+" "+value; break;
			case "AB": pub.AB = (pub.AB==null) ? value : pub.AB+" "+value; break;
			case "DE": pub.DE = (pub.DE==null) ? value : pub.DE+" "+value; break;
			case "DT": pub.DT = (pub.DT==null) ? value : pub.DT+" "+value; break;
			case "UT": pub.UT = (pub.UT==null) ? value : pub.UT+" "+value; break;
			
			/* Integer values */
			case "PY": pub.PY = Integer.valueOf(value); break;
			case "BP": pub.BP = Integer.valueOf(value); break;
			case "EP": pub.EP = Integer.valueOf(value); break;
			case "PG": pub.PG = Integer.valueOf(value); break;
			case "TC": pub.TC = Integer.valueOf(value); break;
			
			/* Parse Cited References */
			case "CR": CRType cr = new CRType().parseWoS(value, yearRange); if (cr!=null) pub.crList.add(cr); break;
			
			/* Authors */
			case "AU": if (pub.AU==null) pub.AU=new ArrayList<String>(); pub.AU.add(value); break;
			case "AF": if (pub.AF==null) pub.AF=new ArrayList<String>(); pub.AF.add(value); break;
			case "EM": pub.EM = new ArrayList<String>(Arrays.asList(value.split("; "))); break;
			
			/* store C1 values in a separate list for further processing */
			case "C1": C1.add(value); break;
			}
		}
		
		if (pub.PT==null) return null;
		
		pub.C1 = new ArrayList<String[]>();
		pub.AA = new ArrayList<String>();
		for (String corr: C1) {
			int pos = corr.indexOf(']');
			if (pos>0) {
				String names = corr.substring(1, pos);
				String affiliation = corr.substring (pos+2);
				for (String name: names.split("; ")) {
					String[] tmp = new String[2]; tmp[0] = name; tmp[1] = affiliation;
					pub.C1.add (tmp);
					pub.AA.add (affiliation);
				}
			}
		}
		
		return pub;
	}
	
	

	public PubType parseWoS (HashMap<String, ArrayList<String>> entries, int length, int[] yearRange) {
		
		this.length = length
		
		PT = entries.get("PT")
		
		AU = entries.get("AU")
		AF = entries.get("AF")
		C1 = []
		EM = (entries.get("EM")?.get(0)?.split("; ") as List)
		AA = []
		// pattern: [author1; author2; ...] affiliation
		entries.get("C1")?.each {
			int pos = it.indexOf(']')
			if (pos>0) {
				String names = it.substring(1, pos)
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
			crList = entries.get("CR")?.collect { String it -> new CRType().parseWoS (it, yearRange) }?.findAll { CRType it -> it != null } as ArrayList
		}

		DI = entries.get("DI")?.join(" ")
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
			"EM": [EM?.unique()?.join ("; ")], 
			"TI": linesTI,
			"PY": [PY?.toString()],
			"SO": [SO],
			"VL": [VL],
			"IS": [IS?.toString()],
			"AR": [AR?.toString()],
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
	
	
	public PubType parseScopus (String[] line, String[] attributes, int[] yearRange) {
		HashMap<String, String> entries = new HashMap<String, String>();
		
		int length = 0;
		for (int i=0; i<line.length; i++) {
			entries.put(attributes[i], line[i]);
			length += 1 + line[i].length();
		}
		
		return parseScopus (entries, length, yearRange);
	}
		
	public PubType parseScopus (HashMap<String, String> entries, int length, int[] yearRange) {
		
		Matcher Scopus_matchEMail = "" =~ '\\s(\\S+@\\w+(\\.\\w+)+)\\W*'
		
		this.length = length
		
		PT = "J" // TODO: what is the default Publication Type? (No value in scopus!)
		
		// Scopus Authors: Lastname1 I1., Lastname2 I2.I2. ...
		AU = entries.get('Authors').split("\\., ").collect { String name -> 
			name = name.replaceAll("\\.", "") 
			int pos = name.lastIndexOf(" ");
			(pos>0) ? name.substring(0, pos) + "," + name.substring (pos) : name
		}
		AF = AU		// there are no full names in Scopus 

		// Authors with affiliations: "<lastname>, <initials with dots>, affiliation"
		C1 = []
		EM = []
		entries.get('Authors with affiliations')?.split("; ").each {String it ->
			String[] split = it.split(", ", 3)
			if (split.length == 3) {
				C1 << ([(split[0]+", "+split[1].replaceAll("\\.", "")), split[2]] as String[])
			}
			
			if (it.contains("@")) {
				println "@@@"
			}
			
			Scopus_matchEMail.reset(it)
			if (Scopus_matchEMail.find()) {
				String[] m = (String[]) Scopus_matchEMail[0]
				println m
				EM << (m[1] as String)
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
		
		TC = entries.get('Cited by')?.isInteger() ? entries.get('Cited by').toInteger() : null
		crList = entries.get('References')?.split(";").collect { String it -> new CRType().parseScopus (it, yearRange) }.findAll { CRType it -> it != null } as ArrayList

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
			(C1==null)?"":C1.collect { String[] it -> 
				String[] split = it[0].split(", ", 2)
				if (split.size()==2) {
					split[0] + ", " + split[1].replaceAll("([A-Z])", "\$1.") + ", " + it[1]
				} else {
					it[0] + ", " + it[1]
				}
			}.join ("; "),
			AB?:"",
			DE?:"",
			crList.collect { CRType cr -> cr.getScopus() }.join("; "),
			DT?:"",
			FS?:"",
			UT?:""
		] as String[]
	}
	
	public PubType parseJSON (JSONObject j, List<CRType> crData, HashMap<Integer, Integer> crId2Index) {
		
		PT = j.PT as String
		AU = j.AU as List<String>
		AF = j.AF as List<String>
		C1 = j.C1.collect { it as String[] }
		EM = j.EM as List<String>
		AA = j.AA as List<String>
		TI = j.TI as String
		PY = j.PY as Integer
		SO = j.SO as String
		VL = j.VL as String
		IS = j.IS as String
		AR = j.AR as String
		BP = j.BP as Integer
		EP = j.EP as Integer
		PG = j.PG as Integer
		TC = j.TC as Integer
		crList = (j.CRLISTID as String[]).collect { crData[crId2Index[it as int]] } as ArrayList
		DI = j.DI as String
		LI = j.LI as String
		AB = j.AB as String
		DE = j.DE as String
		DT = j.DT as String
		FS = j.FS as String
		UT = j.UT as String
		
		this
	}
	
	public JSONObject getJSON () {
		
		JsonBuilder jb = new JsonBuilder()
		
		jb (
			PT: this.PT,
			AU: this.AU.collect { it },
			AF: this.AF.collect { it },
			C1: this.C1.collect { it.collect { x -> x } },
			EM: this.EM,
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
			CRLISTID: crList.collect { it.ID },
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