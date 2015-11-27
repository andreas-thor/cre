package cre.data.source

import groovy.transform.CompileStatic

import java.util.regex.Matcher

import cre.data.CRType

@CompileStatic
public class WoS extends FileImport {

	boolean crBlock
	
	
	static Matcher matchAuthor = "" =~ "([^ ]*)( )?(.*)?"
	static List<Matcher> matchAuthorVon = ["(von)()", "(van)( der)?", "(van't)()"].collect { "" =~ "${it}([^ ]*)( )([^ ]*)(.*)" }
	static Matcher matchPageVolumes = "" =~ "([PV])([0-9]+)"
	static Matcher matchDOI = "" =~ ".*DOI (10\\.[^/]+/ *[^ ,]+).*"
	
	public WoS(int[] yearRange) {
		super (yearRange)
		crBlock = false
	}
	
	@Override
	public CRType parseLine(String line) {

		if (line.startsWith("TI ")) {
			noOfPubs++ // publication that has cited the other pubs (CR)
			return null
		}

		if (!line.startsWith("   ")) crBlock = false
		
		if (line.startsWith("CR ") || (line.startsWith("   ") && crBlock)) {
			
			crBlock = true
			CRType result = new CRType()
			result.CR = line[3..-1].toUpperCase()
			String[] crsplit = result.CR.split (",", 3)
			
			// abort if year is not a number
			String yearS = crsplit.length > 1 ? crsplit[1].trim() : ""
			if (!yearS.isNumber()) return null
				
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
		
		return null
	}
	
	
	
}