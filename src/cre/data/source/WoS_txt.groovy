package cre.data.source

import groovy.transform.CompileStatic

import java.io.BufferedReader;
import java.io.File;
import java.util.regex.Matcher

import cre.data.CRTable;
import cre.data.CRType
import cre.data.PubType;

@CompileStatic
public class WoS_txt extends FileImportExport {

	// TODO: von Autoren paasen nicht; warum uppercase (hab ich mal rausgenommen)
	static Matcher matchAuthor = "" =~ "([^ ]*)( )?(.*)?"
	static List<Matcher> matchAuthorVon = ["(von )()", "(van )(der )?", "(van't )()"].collect { "" =~ "${it}([^ ]*)( )([^ ]*)(.*)" }
	static Matcher matchPageVolumes = "" =~ "([PV])([0-9]+)"
	static Matcher matchDOI = "" =~ ".*DOI (10\\.[^/]+/ *[^ ,]+).*"
	
	
	static String[] tags = ["PT" ,"AU" ,"AF" ,"CA" ,"TI" ,"ED" ,"SO" ,"SE" ,"BS" ,"LA" ,"DT" ,"CT" ,"CY" ,"HO" ,"CL" ,"SP" ,"DE" ,"ID" ,"AB" ,"C1" ,"RP" ,"EM" ,"RI", "OI", "FU" ,"FX" ,"CR" ,"NR" ,"TC" ,"Z9" ,"PU" ,"PI" ,"PA" ,"SN" ,"BN" ,"J9" ,"JI" ,"PD" ,"PY" ,"VL" ,"IS" ,"PN" ,"SU" ,"SI" ,"BP" ,"EP" ,"AR" ,"PG" ,"DI" ,"WC", "SC" ,"GA" ,"UT"]
	
	
	

	public WoS_txt(int[] yearRange, BufferedReader br) {
		super(yearRange, br);
	}


	/**
	 * @return next publication entry; null if there is no more data
	 */
		
	@Override
	public PubType getNextPub() {

		String currentTag = ""
		String tagBlock = ""
		
		PubType pub = new PubType()
		HashMap<String, String> entries = [:]	
			
		String line
		while ((line = br.readLine()) != null) {
			
			pub.length += line.length()+1
			if (line.length()<2) continue
			currentTag = line.substring(0, 2)

			if (currentTag.equals("ER")) break;	// ER = End Of Record
			if (currentTag.equals("EF")) return null;	// EF = End Of File
						
			tagBlock = currentTag.equals("  ") ? tagBlock : new String(currentTag)
			entries.put(tagBlock, (entries.get(tagBlock) == null) ? line.substring(3) : entries.get(tagBlock) + "\n" + line.substring(3))
		}
		
		pub.with {
			AU = entries.get("AU");
			TI = entries.get("TI");
			PY = entries.get("PY")?.isInteger() ? entries.get("PY").toInteger() : null
			SO = entries.get("SO");
			VL = entries.get("VL");
			IS = entries.get("IS");
			BP = entries.get("BP")?.isInteger() ? entries.get("BP").toInteger() : null
			EP = entries.get("EP")?.isInteger() ? entries.get("EP").toInteger() : null
			PG = entries.get("PG")?.isInteger() ? entries.get("PG").toInteger() : null
			NR = entries.get("NR")?.isInteger() ? entries.get("NR").toInteger() : null
			DOI = entries.get("DOI");
			AB = entries.get("AB");
			DT = entries.get("DT");
		}
		
		if (pub.TI == null) return null;
		pub.crList = entries.get("CR")?.split("\n").collect { String it -> parseCR (it) }.findAll { CRType it -> it != null } as List
		return pub
	}
	
	
	/**
	 * @param line line of WoS file in the CR section
	 * @return parsed Cited References 
	 */
	private CRType parseCR (String line) {

		CRType result = new CRType()
		result.CR = line // [3..-1] // .toUpperCase()
		String[] crsplit = result.CR.split (",", 3)
		
		// abort if year is not a number
		String yearS = crsplit.length > 1 ? crsplit[1].trim() : ""
		if (!yearS.isInteger()) return null
			
		// abort if year is out of range
		int year = yearS.toInteger().intValue()
		
		if (((year < yearRange[0]) && (yearRange[0]!=0)) || ((year > yearRange[1]) && (yearRange[1]!=0))) return null
		
		result.RPY = year
		result.AU = crsplit[0].trim()
		
		// process "difficult" last names starting with "von" etc.
		if ((result.AU.length()>0) && (result.AU[0]=='v')) {
			matchAuthorVon.each { Matcher matchVon ->
				matchVon.reset(result.AU)
				if (matchVon.matches()) {
					String[] m = (String[]) matchVon[0]
					result.AU_L = (m[1] + (m[2]?:"") + m[ ((m[3] == "") ? 5 : 3) ]).replaceAll(" ","").replaceAll("\\-","")
					result.AU_F = ((((m[3] == "") ? "" : m[5]) + m[6]).trim() as List)[0]?:""	// cast as List to avoid Index out of Bounds exception
				}
			}
		}
		
		// process all other authors
		if (result.AU_L == null) {
			matchAuthor.reset(result.AU)
			if (matchAuthor.matches()) {
				String[] m = (String[]) matchAuthor[0]
				result.AU_L = m[1].replaceAll("\\-","")
				result.AU_F = (m[3]?:" ")[0]
			}
		}
			
		// process all journals
		result.J = crsplit.length > 2 ? crsplit[2].trim() : ""
		result.J_N = result.J.split(",")[0]
		String[] split = result.J_N.split(" ")
		result.J_S = (split.size()==1) ? split[0] : split.inject("") { x, y -> x + ((y.length()>0) ? y[0] : "") }
		
		// find Volume, Pages and DOI
		result.J.split(",").each { String it ->
			matchPageVolumes.reset(it.trim())
			if (matchPageVolumes.matches()) {
				String[] m = (String[]) matchPageVolumes[0]
				if (m[1].equals("P")) result.PAG = m[2]
				if (m[1].equals("V")) result.VOL = m[2]
			}
			
			matchDOI.reset(it.trim())
			if (matchDOI.matches()) {
				String[] m = (String[]) matchDOI[0]
				result.DOI = m[1].replaceAll("  ","").toUpperCase()
			}
		}
		
		return result
		
	}
	

	/**
	 * Writes a tagged field
	 */
	private static writeTag (BufferedWriter bw, String tag, String value) {
		if (value==null) return;
		value.split("\n").eachWithIndex { String line, int pos ->
			bw.write ((pos==0) ? tag+" " : "   ")
			bw.writeLine (line)
		}
	}
	
	public static void save (File file, CRTable crTab) {
		
		String d = "${new Date()}: "
		crTab.stat.setValue(d + "Saving TXT file in WoS format ...", 0)
		
		// add txt extension if necessary
		// TODO: Do I really need to adjust the file extension??
		String file_name = file.toString();
		if (!file_name.endsWith(".txt")) file_name += ".txt";
				
						
		BufferedWriter bw = new BufferedWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"))
		bw.writeLine("FN Thomson Reuters Web of Science\u0153 modified by CRExplorer")
		bw.writeLine("VR 1.0")
		
		crTab.pubData.eachWithIndex { PubType pub, int idx ->
			crTab.stat.setValue (d + "Saving TXT file in WoS format ...", ((idx+1)*100.0/crTab.pubData.size()).intValue())
			pub.with {
				writeTag(bw, "AU", AU)
				writeTag(bw, "TI", TI)
				writeTag(bw, "PY", PY?.toString())
				writeTag(bw, "SO", SO)
				writeTag(bw, "VL", VL)
				writeTag(bw, "IS", IS?.toString())
				writeTag(bw, "BP", BP?.toString())
				writeTag(bw, "EP", EP?.toString())
				writeTag(bw, "PG", PG?.toString())
				writeTag(bw, "CR", crList.collect { CRType cr -> cr.CR }.join("\n")) 
				writeTag(bw, "NR", NR?.toString())
				writeTag(bw, "DOI", DOI)
				writeTag(bw, "AB", AB)
				writeTag(bw, "DT", DT)
			}
			bw.writeLine("ER")
			bw.writeLine("")
		}
		bw.writeLine("EF")
		bw.close()
		crTab.stat.setValue("${new Date()}: Saving TXT file in WoS format done", 0, crTab.getInfoString())
			
	}
	
}

/*
*Web of Science*

Field Tags (Articles and Conference Proceedings)

These two-character field tags identify fields in records that you e-mail or save to file.

FN 	File Name
VR 	Version Number
PT 	Publication Type (conference, book, journal, book in series, or patent)
AU 	Authors
AF 	Author Full Name
CA 	Group Authors
TI 	Document Title
ED 	Editors
SO 	Publication Name
SE 	Book Series Title
BS 	Book Series Subtitle
LA 	Language
DT 	Document Type
CT 	Conference Title
CY 	Conference Date
HO 	Conference Host
CL 	Conference Location
SP 	Conference Sponsors
DE 	Author Keywords
ID 	Keywords Plus®
AB 	Abstract
C1 	Author Address
RP 	Reprint Address
EM 	E-mail Address
FU 	Funding Agency and Grant Number
FX 	Funding Text
CR 	Cited References
NR 	Cited Reference Count
TC 	Times Cited
PU 	Publisher
PI 	Publisher City
PA 	Publisher Address
SN 	ISSN
BN 	ISBN
J9 	29-Character Source Abbreviation
JI 	ISO Source Abbreviation
PD 	Publication Date
PY 	Year Published
VL 	Volume
IS 	Issue
PN 	Part Number
SU 	Supplement
SI 	Special Issue
BP 	Beginning Page
EP 	Ending Page
AR 	Article Number
PG 	Page Count
DI 	Digital Object Identifier (DOI)
SC 	Subject Category
GA 	Document Delivery Number
UT 	Unique Article Identifier
ER 	End of Record
EF 	End of File

*/


//	public CRType parseLine(String line) {
//
//		if (line.startsWith("TI ")) {
//			noOfPubs++ // publication that has cited the other pubs (CR)
//			return null
//		}
//
//		if (!line.startsWith("   ")) crBlock = false
//		
//		if (line.startsWith("CR ") || (line.startsWith("   ") && crBlock)) {
//			
//			crBlock = true
//			CRType result = new CRType()
//			result.CR = line[3..-1].toUpperCase()
//			String[] crsplit = result.CR.split (",", 3)
//			
//			// abort if year is not a number
//			String yearS = crsplit.length > 1 ? crsplit[1].trim() : ""
//			if (!yearS.isInteger()) return null
//				
//			// abort if year is out of range
//			int year = yearS.toInteger().intValue()
//			
//			if (((year < yearRange[0]) && (yearRange[0]!=0)) || ((year > yearRange[1]) && (yearRange[1]!=0))) return null
//			
//			result.RPY = year
//			result.AU = crsplit[0].trim()
//			
//			// process "difficult" last names starting with "von" etc.
//			if ((result.AU.length()>0) && (result.AU[0]=='v')) {
//				matchAuthorVon.each { Matcher matchVon ->
//					matchVon.reset(result.AU)
//					if (matchVon.matches()) {
//						String[] m = (String[]) matchVon[0]
//						result.AU_L = (m[1] + (m[2]?:"") + m[ ((m[3] == "") ? 5 : 3) ]).replaceAll(" ","").replaceAll("\\-","")
//						result.AU_F = ((((m[3] == "") ? "" : m[5]) + m[6]).trim() as List)[0]?:""	// cast as List to avoid Index out of Bounds exception
//					}
//				}
//			}
//			
//			// process all other authors
//			if (result.AU_L == null) {
//				matchAuthor.reset(result.AU)
//				if (matchAuthor.matches()) {
//					String[] m = (String[]) matchAuthor[0]
//					result.AU_L = m[1].replaceAll("\\-","")
//					result.AU_F = (m[3]?:" ")[0]
//				}
//			}
//				
//			// process all journals
//			result.J = crsplit.length > 2 ? crsplit[2].trim() : ""
//			result.J_N = result.J.split(",")[0]
//			String[] split = result.J_N.split(" ")
//			result.J_S = (split.size()==1) ? split[0] : split.inject("") { x, y -> x + ((y.length()>0) ? y[0] : "") }
//			
//			// find Volume, Pages and DOI
//			result.J.split(",").each { String it ->
//				matchPageVolumes.reset(it.trim())
//				if (matchPageVolumes.matches()) {
//					String[] m = (String[]) matchPageVolumes[0]
//					if (m[1].equals("P")) result.PAG = m[2]
//					if (m[1].equals("V")) result.VOL = m[2]
//				}
//				
//				matchDOI.reset(it.trim())
//				if (matchDOI.matches()) {
//					String[] m = (String[]) matchDOI[0]
//					result.DOI = m[1].replaceAll("  ","").toUpperCase()
//				}
//			}
//			
//			return result
//				
//		}
//		
//		return null
//	}
//
//
//	
