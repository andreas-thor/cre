package cre.data.source
 
import java.io.BufferedReader
import java.io.File
import java.util.HashMap
import java.util.List
import java.util.regex.Matcher

import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import cre.data.*
import cre.ui.StatusBar
import groovy.transform.CompileStatic

@CompileStatic
public class ScopusG_csv extends FileImportExport {

	CSVReader csv
	String[] attributes = null
	
	public ScopusG_csv(int[] yearRange, BufferedReader br) {
		super (yearRange, br)

		csv = new CSVReader(br)
		attributes = csv.readNext().collect { String field -> field.trim() }
		
		/*
			http://stackoverflow.com/questions/21891578/removing-bom-characters-using-java
			Java does not handle BOM properly. In fact Java handles a BOM like every other char.
			Found this:	http://www.rgagnon.com/javadetails/java-handle-utf8-file-with-bom.html
		*/
		if (attributes[0].startsWith("\uFEFF")) attributes[0] = attributes[0].substring(1)
	}
	
	
	@Override
	public PubType getNextPub() {
		
		if (attributes == null) return null
		String[] line = csv.readNext()
		
		if (line == null) return null

		HashMap<String, String> entries = [:]
		int length = 0
		line.eachWithIndex { String val, int idx ->
			length += val.length()+1
			entries.put(attributes[idx], val)
		}
		
		return new PubType().parseScopus(entries, length, yearRange);
	}




	public static void save (File file, CRTable crTab, StatusBar stat) {
		
		String d = "${new Date()}: "
		stat.setValue(d + "Saving CSV file in Scopus format ...", 0)
		
		// add csv extension if necessary
		String file_name = file.toString()
		if (!file_name.endsWith(".csv")) file_name += ".csv"
				
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"))
		
		csv.writeNext(["Authors","Title","Year","Source title","Volume","Issue","Art. No.","Page start","Page end","Page count","Cited by","DOI","Link","Affiliations","Authors with affiliations","Abstract","Author Keywords","References","Document Type","Source","EID"] as String[])
		
		crTab.pubData.eachWithIndex  { PubType pub, int idx ->
			stat.setValue (d + "Saving CSV file in Scopus format ...", ((idx+1)*100.0/crTab.pubData.size()).intValue())
			csv.writeNext (pub.getScopus())
		}
		csv.close()

	
		stat.setValue("${new Date()}: Saving CSV file in Scopus format done", 0, crTab.getInfoString())
			
	}
	
	
	
	
} 


//	static Matcher matchBlock = "" =~ "^([A-Y ]+)(\\: )(.*)"
//	static List<Matcher> matchPAG  = ["" =~ "p\\. ([0-9]+)\$", "" =~ "p\\. ([0-9]+)[\\.;,]",  "" =~ "pp\\. ([0-9]+)\\-[0-9]+[\\.;,]", "" =~ "pp\\. ([0-9]+)\\-[0-9]+\$"]
//	static List<Matcher> matchVOL  = ["" =~ "([0-9]+)", "" =~ "([0-9]+) \\([0-9]+\\)", "" =~ "([0-9]+) \\([0-9]+\\-[0-9]+\\)"]
//	static Matcher matchAuthor = "" =~ "^([^,]+),([ A-Z\\-\\.]+\\.),(.*)"
//	static Matcher matchYearTitle = "" =~ "(.*?)\\((\\d{4})\\)(.*)"
//	static Matcher matchDOI = "" =~ ".*((DOI)|(doi))\\:?=?(10\\.[^/]+/ *[^;,]+).*"
	


/**
 *
 * @param line
 * @param yearRange
 * @param first = true if called from Scopus Reader (false, if called from WoS-Reader in case of import / export scenarios)
 * @return
 */

//	public static CRType parseCR(String line, int[] yearRange, boolean first) {
//
//		line = line.trim()
//		if (line.length() == 0) return null
//
//		CRType result = new CRType(CRType.TYPE_SCOPUS)
//		result.CR = line
//
//		// parse all authors (but save first author only)
//		boolean firstAuthor = true
//		matchAuthor.reset(line)
//		while (matchAuthor.matches()) {
//			String[] m = (String[]) matchAuthor[0]
//			if (firstAuthor) {
//				result.AU_L = m[1]
//				result.AU_F = (m[2].trim())[0]
//				result.AU = m[1] + "," + m[2]
//				result.AU_A = m[1] + "," + m[2]
//			} else {
//				result.AU_A += "; " + m[1] + "," + m[2]
//			}
//			firstAuthor = false
//			line = m[3].trim()
//			matchAuthor.reset(line)
//		}
//
//		// find publication year and title
//		result.J_N = ""
//		result.J = ""
//		result.TI = ""
//		matchYearTitle.reset (line)
//		if (matchYearTitle.matches()) {
//			String[] m = (String[]) matchYearTitle[0]
//			if (m[1].length() == 0) {
////					result.J = "XXX"
//				if (m[2]?.isInteger()) result.RPY = m[2].toInteger()
//				int pos = m[3].indexOf(", ,")
//				if (pos>=0) {
////						TITLE = m[3][0..pos]
//					result.TI = m[3][0..pos]
//					result.J_N = ""
//					result.J = m[3][pos+3..-1].trim()
//				} else {
//					String[] crsplit = m[3].split (",", 2)
//					result.J_N = crsplit[0].trim()
//					result.J = m[3].trim()
//				}
//
//			} else {
////					TITLE = m[1]
//				result.TI = m[1]
//				if (m[2]?.isInteger()) result.RPY = m[2].toInteger()
//				String[] crsplit = m[3].split (",", 2)
//				result.J_N = crsplit[0].trim()
//				result.J = m[3].trim()
//			}
//		}
//
//
//		if (result.RPY != null) {
//			if (((result.RPY < yearRange[0]) && (yearRange[0]!=0)) || ((result.RPY > yearRange[1]) && (yearRange[1]!=0))) return null
//		} else {
//			if (first) return WoS_txt.parseCR(line, yearRange, false)
//			return null
//		}
//
//
//
////			result.J = line
//
//		// process Journal names
//		String[] split = result.J_N.split(" ")
//		result.J_S = (split.size()==1) ? split[0] : split.inject("") { x, y -> x + ((y.length()>0) ? y[0] : "") }
//
//
//		matchDOI.reset(result.J.replaceAll(" ",""))
//		if (matchDOI.matches()) {
//			String[] m = (String[]) matchDOI[0]
//			result.DOI = m[4]
//		}
//
//		if ((result.J.toLowerCase().indexOf("doi")>=0) && (result.DOI == null)) {
//			println result.J
//		}
//
//
//		result.J.split (",").each { String it ->
//			String s = it.trim()
//			matchPAG.each { Matcher matchP ->
//				matchP.reset(s.trim())
//				if (matchP.matches()) {
//					String[] m = (String[]) matchP[0]
//					result.PAG = m[1]
//				}
//			}
//
//			matchVOL.each { Matcher matchV ->
//				matchV.reset(s.trim())
//				if (matchV.matches()) {
//					String[] m = (String[]) matchV[0]
//					result.VOL = m[1]
//				}
//			}
//
//
//		}
//
//		if (result.RPY == null) return null
//		return result
//	}
