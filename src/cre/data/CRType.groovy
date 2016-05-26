package cre.data 

import java.util.List;
import java.util.Map
import java.util.regex.Matcher

import com.orsoncharts.util.json.JSONObject;

import groovy.json.JsonBuilder;
import groovy.json.JsonOutput;
import groovy.transform.CompileStatic

@CompileStatic
public class CRType /* implements Comparable<CRType>*/ {

	
	static Matcher WoS_matchAuthor = "" =~ "([^ ]*)( )?(.*)?"
	static List<Matcher> WoS_matchAuthorVon = ["(von )()", "(van )(der )?", "(van't )()"].collect { "" =~ "${it}([^ ]*)( )([^ ]*)(.*)" }
	static Matcher WoS_matchPageVolumes = "" =~ "([PV])([0-9]+)"
	static Matcher WoS_matchDOI = "" =~ ".*DOI (10\\.[^/]+/ *[^ ,]+).*"

	
	static Matcher Scopus_matchBlock = "" =~ "^([A-Y ]+)(\\: )(.*)"
	static List<Matcher> Scopus_matchPAG  = ["" =~ "p\\. ([0-9]+)\$", "" =~ "p\\. ([0-9]+)[\\.;,]",  "" =~ "pp\\. ([0-9]+)\\-[0-9]+[\\.;,]", "" =~ "pp\\. ([0-9]+)\\-[0-9]+\$"]
	static List<Matcher> Scopus_matchVOL  = ["" =~ "([0-9]+)", "" =~ "([0-9]+) \\([0-9]+\\)", "" =~ "([0-9]+) \\([0-9]+\\-[0-9]+\\)"]
	static Matcher Scopus_matchAuthor = "" =~ "^([^,]+),([ A-Z\\-\\.]+\\.),(.*)"
	static Matcher Scopus_matchYearTitle = "" =~ "(.*?)\\((\\d{4})\\)(.*)"
	static Matcher Scopus_matchDOI = "" =~ ".*((DOI)|(doi))\\:?=?(10\\.[^/]+/ *[^;,]+).*"
	
	
	int ID;
	String CR;
	String AU;
	String AU_F;
	String AU_L;
	String AU_A;	// all Authors
	String TI; 		// title
	String J;
	String J_N;
	String J_S;
	int N_CR = 1;
	Integer RPY;
	String PAG;
	String VOL;
	String DOI;
	CRCluster CID2;
	int CID_S;
	int VI = 1;
	int CO = 0;
	
	static byte TYPE_WOS = 1
	static byte TYPE_SCOPUS = 2
	byte type = 0 ;	
	
	Double PERC_YR;
	Double PERC_ALL;

//	String blockkey;
	
	int mergedTo = -1

	// Mapping of internal attribute names to labels
	public static Map<String, String> attr = [
		'ID'  : 'ID',
		'CR'  : 'Cited Reference',
		'RPY' : 'Cited Reference Year',
		'N_CR': 'Number of Cited References',
		'PERC_YR': 'Percent in Year',
		'PERC_ALL': 'Percent over all Years',
		'AU': 'Author',
		'AU_L': 'Last Name',
		'AU_F': 'First Name Initial',
		'AU_A': 'Authors',
		'TI': 'Title',
		'J': 'Source',
		'J_N': 'Source Title',
		'J_S': 'Title Short',
		'VOL' : 'Volume',
		'PAG' : 'Page',
		'DOI' : 'DOI',
		'CID2': 'ClusterID',
		'CID_S': 'Cluster Size'
	]
	

	
//	public CRType () {
//		N_CR = 1
//		VI = 1
//		CO = 0
//		DOI = ""
//		AU = null
//		AU_F = null
//		AU_L = null
//		AU_A = null
//		TI = null
//		RPY = null
//		PAG = ""
//		VOL = "" 
//		mergedTo = -1
//		RPY = null
//		PERC_ALL = null
//		PERC_YR = null
//		this.type = 0
//	}

	
	
	public CRType parseWoS (String line, int[] yearRange) {
		
		CR = line // [3..-1] // .toUpperCase()
		type = TYPE_WOS
		
		String[] crsplit = CR.split (",", 3)
		
		String yearS = crsplit.length > 1 ? crsplit[1].trim() : ""
		if (yearS.isInteger()) {
			// abort if year is out of range
			int year = yearS.toInteger().intValue()
			if (((year < yearRange[0]) && (yearRange[0]!=0)) || ((year > yearRange[1]) && (yearRange[1]!=0))) return null
			RPY = year
		} else {
			return null
		}
		
		AU = crsplit[0].trim()
		
		// process "difficult" last names starting with "von" etc.
		if ((AU.length()>0) && (AU[0]=='v')) {
			WoS_matchAuthorVon.each { Matcher matchVon ->
				matchVon.reset(AU)
				if (matchVon.matches()) {
					String[] m = (String[]) matchVon[0]
					AU_L = (m[1] + (m[2]?:"") + m[ ((m[3] == "") ? 5 : 3) ]).replaceAll(" ","").replaceAll("\\-","")
					AU_F = ((((m[3] == "") ? "" : m[5]) + m[6]).trim() as List)[0]?:""	// cast as List to avoid Index out of Bounds exception
				}
			}
		}
		
		// process all other authors
		if (AU_L == null) {
			WoS_matchAuthor.reset(AU)
			if (WoS_matchAuthor.matches()) {
				String[] m = (String[]) WoS_matchAuthor[0]
				AU_L = m[1].replaceAll("\\-","")
				AU_F = (m[3]?:" ")[0]
			}
		}
			
		// process all journals
		J = crsplit.length > 2 ? crsplit[2].trim() : ""
		J_N = J.split(",")[0]
		String[] split = J_N.split(" ")
		J_S = (split.size()==1) ? split[0] : split.inject("") { x, y -> x + ((y.length()>0) ? y[0] : "") }
		
		// find Volume, Pages and DOI
		J.split(",").each { String it ->
			WoS_matchPageVolumes.reset(it.trim())
			if (WoS_matchPageVolumes.matches()) {
				String[] m = (String[]) WoS_matchPageVolumes[0]
				if (m[1].equals("P")) PAG = m[2]
				if (m[1].equals("V")) VOL = m[2]
			}
			
			WoS_matchDOI.reset(it.trim())
			if (WoS_matchDOI.matches()) {
				String[] m = (String[]) WoS_matchDOI[0]
				DOI = m[1].replaceAll("  ","").toUpperCase()
			}
		}
		
		return this
		
	}
	
	
	public String getWoS () {
		if (type == TYPE_WOS) return this.CR
		return AU_L + " " + AU_F + ", " + RPY + ", " + J
	}

	

	
	public CRType parseScopus (String line, int[] yearRange) {
		
		line = line.trim()
		if (line.length() == 0) return null
		
		type = CRType.TYPE_SCOPUS
		CR = line
		
		// parse all authors (but save first author only)
		boolean firstAuthor = true
		Scopus_matchAuthor.reset(line)
		while (Scopus_matchAuthor.matches()) {
			String[] m = (String[]) Scopus_matchAuthor[0]
			if (firstAuthor) {
				AU_L = m[1]
				AU_F = (m[2].trim())[0]
				AU = m[1] + "," + m[2]
				AU_A = m[1] + "," + m[2]
			} else {
				AU_A += "; " + m[1] + "," + m[2]
			}
			firstAuthor = false
			line = m[3].trim()
			Scopus_matchAuthor.reset(line)
		}
		
		// find publication year and title
		J_N = ""
		J = ""
		TI = ""
		Scopus_matchYearTitle.reset (line)
		if (Scopus_matchYearTitle.matches()) {
			String[] m = (String[]) Scopus_matchYearTitle[0]
			if (m[1].length() == 0) {
//					J = "XXX"
				if (m[2]?.isInteger()) RPY = m[2].toInteger()
				int pos = m[3].indexOf(", ,")
				if (pos>=0) {
//						TITLE = m[3][0..pos]
					TI = m[3][0..pos]
					J_N = ""
					J = m[3][pos+3..-1].trim()
				} else {
					String[] crsplit = m[3].split (",", 2)
					J_N = crsplit[0].trim()
					J = m[3].trim()
				}
				
			} else {
//					TITLE = m[1]
				TI = m[1]
				if (m[2]?.isInteger()) RPY = m[2].toInteger()
				String[] crsplit = m[3].split (",", 2)
				J_N = crsplit[0].trim()
				J = m[3].trim()
			}
		}

		
		if (RPY == null) return null
		if (((RPY < yearRange[0]) && (yearRange[0]!=0)) || ((RPY > yearRange[1]) && (yearRange[1]!=0))) return null
		
		
		
//			J = line
		
		// process Journal names
		String[] split = J_N.split(" ")
		J_S = (split.size()==1) ? split[0] : split.inject("") { x, y -> x + ((y.length()>0) ? y[0] : "") }

		
		Scopus_matchDOI.reset(J.replaceAll(" ",""))
		if (Scopus_matchDOI.matches()) {
			String[] m = (String[]) Scopus_matchDOI[0]
			DOI = m[4]
		}
		
		if ((J.toLowerCase().indexOf("doi")>=0) && (DOI == null)) {
			println J
		}
		
		
		J.split (",").each { String it ->
			String s = it.trim()
			Scopus_matchPAG.each { Matcher matchP ->
				matchP.reset(s.trim())
				if (matchP.matches()) {
					String[] m = (String[]) matchP[0]
					PAG = m[1]
				}
			}
			
			Scopus_matchVOL.each { Matcher matchV ->
				matchV.reset(s.trim())
				if (matchV.matches()) {
					String[] m = (String[]) matchV[0]
					VOL = m[1]
				}
			}
			
			
		}
		
		this

		
	}
		
	public String getScopus () {
		if (type == TYPE_SCOPUS) return this.CR
		String a = (AU_A == null) ? AU_L + ", " + AU_F.replaceAll("([A-Z])", "\$1.") : AU_A.replaceAll(";", ",")
		return a + ", " + (TI?:"") + " (" + RPY + ") " + J
	}

	
	
	
//	public CRType(int iD, String cR, String aU, String aU_F, String aU_L,
//			String j, String j_N, String j_S, int n_CR, Integer rPY,
//			String pAG, String vOL, String dOI, CRCluster cID2, int cID_S,
//			int vI, int cO) {
//			
//		super()
//		ID = iD
//		CR = cR
//		AU = aU
//		AU_F = aU_F
//		AU_L = aU_L
//		J = j
//		J_N = j_N
//		J_S = j_S
//		N_CR = n_CR
//		RPY = rPY
//		PAG = pAG
//		VOL = vOL
//		DOI = dOI
//		CID2 = cID2
//		CID_S = cID_S
//		VI = vI
//		CO = cO  
//	}
	


	
	
	public JSONObject getJSON () {
		
		JsonBuilder jb = new JsonBuilder()
		
		jb (
				ID: this.ID,
				CR: this.CR,
				AU: this.AU,
				AU_F: this.AU_F,
				AU_L: this.AU_L,
				AU_A: this.AU_A,
				TI: this.TI,
				J: this.J,
				J_N: this.J_N,
				J_S: this.J_S,
				N_CR: this.N_CR,
				RPY: this.RPY,
				PAG: this.PAG,
				VOL: this.VOL,
				DOI: this.DOI,
				CID2: this.CID2.toString(),
				CID_S: this.CID_S,
				VI: this.VI,
				CO: this.CO,
				type: this.type
		) as JSONObject
		
	}
			
}
