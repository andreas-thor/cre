package cre.data.source

import java.io.BufferedReader;
import java.util.regex.Matcher;

import groovy.transform.CompileStatic
import cre.data.CRType
import cre.data.PubType

@CompileStatic
public class Scopus extends FileImport {

	boolean crBlock 
	
	static Matcher matchBlock = "" =~ "^([A-Y ]+)(\\: )(.*)"
	static List<Matcher> matchPAG  = ["" =~ "p\\. ([0-9]+)[\\.;]", "" =~ "pp\\. ([0-9]+)\\-[0-9]+[\\.;]"]
	static List<Matcher> matchVOL  = ["" =~ "([0-9]+)", "" =~ "([0-9]+) \\([0-9]+\\)", "" =~ "([0-9]+) \\([0-9]+\\-[0-9]+\\)"]
	static Matcher matchAuthor = "" =~ "^([^,]+),([ A-Z\\-\\.]+\\.),(.*)"
	static Matcher matchYearTitle = "" =~ "(.*?)\\((\\d{4})\\)(.*)"
	static Matcher matchDOI = "" =~ ".*((DOI)|(doi))\\:?=?(10\\.[^/]+/ *[^;,]+).*"
	
	public Scopus(int[] yearRange, BufferedReader br) {
		super (yearRange, br)
		crBlock = false
	}
	
	
	@Override
	public PubType getNextPub() {

		

		
		
		return null
	}

	
	
	public CRType parseLine(String line) {


		matchBlock.reset(line)
		if (matchBlock.matches()) {
			crBlock = false
		}
		
		if (line.startsWith("REFERENCES: ")) {
			crBlock = true
			line = line.substring("REFERENCES: ".length())
		}
		
		if (crBlock) {
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
		
		
		
		return null
	}



	
	
	
}