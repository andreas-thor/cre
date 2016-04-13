package cre.data.source

import java.io.BufferedReader
import java.io.File;
import java.util.regex.Matcher;

import groovy.transform.CompileStatic
import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import cre.data.CRTable;
import cre.data.CRType
import cre.data.PubType

@CompileStatic
public class Scopus_csv extends FileImportExport {

	CSVReader csv
	String[] attributes = null
	
	
	static HashMap<String, String> mapScopus = [
		'AU' : 'Authors' ,
		'TI' : 'Title' ,
		'PY' : 'Year' ,
		'SO' : 'Source title' ,
		'VL' : 'Volume' ,
		'IS' : 'Issue' ,
		'BP' : 'Page start' ,
		'EP' : 'Page end' ,
		'PG' : 'Page count' ,
		'NR' : 'Cited by' ,
		'DI' : 'DOI' ,
		'AB' : 'Abstract' ,
		'CR' : 'References' ,
		'DT' : 'Document Type' 
	]
	
	
	static Matcher matchBlock = "" =~ "^([A-Y ]+)(\\: )(.*)"
	static List<Matcher> matchPAG  = ["" =~ "p\\. ([0-9]+)[\\.;]", "" =~ "pp\\. ([0-9]+)\\-[0-9]+[\\.;]"]
	static List<Matcher> matchVOL  = ["" =~ "([0-9]+)", "" =~ "([0-9]+) \\([0-9]+\\)", "" =~ "([0-9]+) \\([0-9]+\\-[0-9]+\\)"]
	static Matcher matchAuthor = "" =~ "^([^,]+),([ A-Z\\-\\.]+\\.),(.*)"
	static Matcher matchYearTitle = "" =~ "(.*?)\\((\\d{4})\\)(.*)"
	static Matcher matchDOI = "" =~ ".*((DOI)|(doi))\\:?=?(10\\.[^/]+/ *[^;,]+).*"
	
	public Scopus_csv(int[] yearRange, BufferedReader br) {
		super (yearRange, br)

		csv = new CSVReader(br)
		attributes = csv.readNext().collect { String field -> field.trim() }
	}
	
	
	@Override
	public PubType getNextPub() {
		
		if (attributes == null) return null;
		String[] line = csv.readNext()
		if (line == null) return null

		PubType pub = new PubType()
		HashMap<String, String> entries = [:]
		
		line.eachWithIndex { String val, int idx ->
			pub.length += val.length()+1
			entries.put(attributes[idx], val)
		}
		
		pub.with {
			AU = entries.get(mapScopus.get("AU"));
			TI = entries.get(mapScopus.get("TI"));
			PY = entries.get(mapScopus.get("PY"))?.isInteger() ? entries.get(mapScopus.get("PY")).toInteger() : null
			SO = entries.get(mapScopus.get("SO"));
			VL = entries.get(mapScopus.get("VL"));
			IS = entries.get(mapScopus.get("IS"));
			BP = entries.get(mapScopus.get("BP"))?.isInteger() ? entries.get(mapScopus.get("BP")).toInteger() : null
			EP = entries.get(mapScopus.get("EP"))?.isInteger() ? entries.get(mapScopus.get("EP")).toInteger() : null
			PG = entries.get(mapScopus.get("PG"))?.isInteger() ? entries.get(mapScopus.get("PG")).toInteger() : null
			NR = entries.get(mapScopus.get("NR"))?.isInteger() ? entries.get(mapScopus.get("NR")).toInteger() : null
			DOI = entries.get(mapScopus.get("DOI"));
			AB = entries.get(mapScopus.get("AB"));
			DT = entries.get(mapScopus.get("DT"));
		}
		
		pub.crList = entries.get(mapScopus.get("CR"))?.split(";").collect { String it -> parseLine (it) }.findAll { CRType it -> it != null } as List
		
		return pub
		
	}

	
	
	public static CRType parseLine(String line) {

		line = line.trim()
		if (line.length() == 0) return null
		
		CRType result = new CRType()
		result.CR = line
		
		// parse all authors (but save first author only)
		boolean firstAuthor = true
		matchAuthor.reset(line)
		while (matchAuthor.matches()) {
			String[] m = (String[]) matchAuthor[0]
			if (firstAuthor) {
				result.AU_L = m[1]
				result.AU_F = (m[2].trim())[0]
				result.AU = m[1] + "," + m[2]
			}
			firstAuthor = false
			line = m[3].trim()
			matchAuthor.reset(line)
		}
		
		// find publication year and title
		result.J_N = ""
		result.J = ""
		matchYearTitle.reset (line)
		if (matchYearTitle.matches()) {
			String[] m = (String[]) matchYearTitle[0]
			if (m[1].length() == 0) {
//					result.J = "XXX"
				result.RPY = Integer.valueOf (m[2]).intValue()
				int pos = m[3].indexOf(", ,") 
				if (pos>=0) {
//						TITLE = m[3][0..pos]
					result.J_N = ""
					result.J = m[3][pos+3..-1].trim()
				} else {
					String[] crsplit = m[3].split (",", 2)
					result.J_N = crsplit[0].trim()
					result.J = m[3].trim()
				}
				
			} else {
//					TITLE = m[1] 
				result.RPY = Integer.valueOf (m[2]).intValue()
				String[] crsplit = m[3].split (",", 2)
				result.J_N = crsplit[0].trim()
				result.J = m[3].trim()
			}
		} 
		
//			result.J = line
		
		// process Journal names
		String[] split = result.J_N.split(" ")
		result.J_S = (split.size()==1) ? split[0] : split.inject("") { x, y -> x + ((y.length()>0) ? y[0] : "") }

		
		matchDOI.reset(result.J.replaceAll(" ",""))
		if (matchDOI.matches()) {
			String[] m = (String[]) matchDOI[0]
			result.DOI = m[4]
		}
		
		if ((result.J.toLowerCase().indexOf("doi")>=0) && (result.DOI == null)) {
			println result.J
		}
		
		
		result.J.split (",").each { String it ->
			String s = it.trim() 
			matchPAG.each { Matcher matchP ->
				matchP.reset(s.trim())
				if (matchP.matches()) { 
					String[] m = (String[]) matchP[0]
					result.PAG = m[1]
				}
			}
			
			matchVOL.each { Matcher matchV ->
				matchV.reset(s.trim())
				if (matchV.matches()) {
					String[] m = (String[]) matchV[0]
					result.VOL = m[1]
				}
			}
			
			
		}
		
		
		
		return result
	}



	public static void save (File file, CRTable crTab) {
		
		String d = "${new Date()}: "
		crTab.stat.setValue(d + "Saving CSV file in Scopus format ...", 0)
		
		// add csv extension if necessary
		String file_name = file.toString();
		if (!file_name.endsWith(".csv")) file_name += ".csv";
				
		CSVWriter csv = new CSVWriter (new OutputStreamWriter(new FileOutputStream(file_name), "UTF-8"))
		
		String[] attributes = ["AU","TI","PY","SO","VL", "IS", "BP", "EP", "PG", "CR", "NR", "DOI", "AB", "DT"] 
		String[] fields = attributes.collect { mapScopus.get(it) } 
		csv.writeNext(fields)
		
		crTab.pubData.eachWithIndex  { PubType pub, int idx ->
			crTab.stat.setValue (d + "Saving CSV file in Scopus format ...", ((idx+1)*100.0/crTab.pubData.size()).intValue())
			pub.with {
				csv.writeNext ([
					AU?:"", TI?:"", PY?:"", SO?:"", VL?:"", IS?:"", BP?:"", EP?:"", PG?:"",
					crList.collect { CRType cr -> cr.CR }.join("; "),
					NR?:"", DOI?:"", AB?:"", DT?:""
				] as String[])
			} 
		}
		csv.close()

	
		crTab.stat.setValue("${new Date()}: Saving CSV file in Scopus format done", 0, crTab.getInfoString())
			
	}
	
	
	
	
} 