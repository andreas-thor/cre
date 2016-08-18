package cre.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.orsoncharts.util.json.JSONObject;

public class CRType {

	
	static Pattern sWoS_matchAuthor = Pattern.compile("([^ ]*)( )?(.*)?");
	
	static Pattern[] sWoS_matchAuthorVon = new Pattern[] { 
			Pattern.compile("(von )()([^ ]*)( )([^ ]*)(.*)"), 
			Pattern.compile("(van )(der )?([^ ]*)( )([^ ]*)(.*)"), 
			Pattern.compile("(van't )()([^ ]*)( )([^ ]*)(.*)") 
		};
	
	static Pattern sWoS_matchPageVolumes = Pattern.compile ("([PV])([0-9]+)");
	static Pattern sWoS_matchDOI = Pattern.compile(".*DOI (10\\.[^/]+/ *[^ ,]+).*");


	

	
	
	public int ID;
	public String CR;
	public String AU;
	public String AU_F; 
	public String AU_L;
	public String AU_A;	// all Authors
	public String TI; 		// title
	public String J;
	public String J_N;
	public String J_S;
	public int N_CR = 1;
	public Integer RPY;
	public String PAG;
	public String VOL;
	public String DOI;
	public CRCluster CID2;
	public int CID_S;
	public int VI = 1;
	public int CO = 0;
	
	public static byte TYPE_WOS = 1;
	public static byte TYPE_SCOPUS = 2;
	public byte type = 0 ;	
	
	public Double PERC_YR;
	public Double PERC_ALL;

//	String blockkey;
	
	public int mergedTo = -1;
	public boolean removed = false;
	
	// Mapping of internal attribute names to labels
	public static final Map<String, String> attr;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
		aMap.put("ID", "ID");
		aMap.put("CR", "Cited Reference");
		aMap.put("RPY", "Cited Reference Year");
		aMap.put("N_CR", "Number of Cited References");
		aMap.put("PERC_YR", "Percent in Year");
		aMap.put("PERC_ALL", "Percent over all Years");
		aMap.put("AU", "Author");
		aMap.put("AU_L", "Last Name");
		aMap.put("AU_F", "First Name Initial");
		aMap.put("AU_A", "Authors");
		aMap.put("TI", "Title");
		aMap.put("J", "Source");
		aMap.put("J_N", "Source Title");
		aMap.put("J_S", "Title Short");
		aMap.put("VOL", "Volume");
		aMap.put("PAG", "Page");
		aMap.put("DOI", "DOI");
		aMap.put("CID2", "ClusterID");
		aMap.put("CID_S", "Cluster Size");
        attr = Collections.unmodifiableMap(aMap);
    }
			
	
	
	public CRType parseWoS (String line, int[] yearRange) {

		CR = line; // [3..-1] // .toUpperCase()
		type = TYPE_WOS;
		String[] crsplit = CR.split (",", 3);
		
		String yearS = crsplit.length > 1 ? crsplit[1].trim() : "";
		try {
			
			int year = Integer.parseInt(yearS);
			if (((year < yearRange[0]) && (yearRange[0]!=0)) || ((year > yearRange[1]) && (yearRange[1]!=0))) return null;
			RPY = year;
		} catch (NumberFormatException e) {
			return null;
		}

		
		AU = crsplit[0].trim();
		
		// process "difficult" last names starting with "von" etc.
		if ((AU.length()>0) && (AU.charAt(0)=='v')) {
			
			for (Pattern p: sWoS_matchAuthorVon) {
				Matcher matchVon = p.matcher(AU);
				if (matchVon.matches()) {
					AU_L = (matchVon.group(1) + (matchVon.group(2)==null?"":matchVon.group(2)) + matchVon.group( ((matchVon.group(3).equals("")) ? 5 : 3) )).replaceAll(" ","").replaceAll("\\-","");
					String tmp = ((((matchVon.group(3).equals("")) ? "" : matchVon.group(5)) + matchVon.group(6)).trim());
					AU_F = tmp.equals("") ? "" : tmp.substring(0,1);	// cast as List to avoid Index out of Bounds exception
					break;
				}
			}
		}
		
		// process all other authors
		if (AU_L == null) {
			Matcher WoS_matchAuthor = sWoS_matchAuthor.matcher(AU);
			if (WoS_matchAuthor.matches()) {
				AU_L = WoS_matchAuthor.group(1).replaceAll("\\-","");
				AU_F = (WoS_matchAuthor.group(3) == null ? " " : WoS_matchAuthor.group(3) + " ").substring(0, 1);
			}
		}
			
		// process all journals
		J = crsplit.length > 2 ? crsplit[2].trim() : "";
		J_N = J.split(",")[0];
		String[] split = J_N.split(" ");
		if (split.length==1) {
			J_S = split[0]; 
		} else {
			J_S = "";
			for (String s: split) {
				if (s.length()>0) J_S += s.charAt(0);
			}
		}
		
		
		// find Volume, Pages and DOI
		for (String it: J.split(",")) {
			Matcher WoS_matchPageVolumes = sWoS_matchPageVolumes.matcher(it.trim());
			if (WoS_matchPageVolumes.matches()) {
				if (WoS_matchPageVolumes.group(1).equals("P")) PAG = WoS_matchPageVolumes.group(2);
				if (WoS_matchPageVolumes.group(1).equals("V")) VOL = WoS_matchPageVolumes.group(2);
			}
			
			Matcher WoS_matchDOI = sWoS_matchDOI.matcher(it.trim());
			if (WoS_matchDOI.matches()) {
				DOI = WoS_matchDOI.group(1).replaceAll("  ","").toUpperCase();
			}
		}
		
		return this;
				
		
	}
	
	
	public String getWoS () {
		
		if (type == TYPE_WOS) return this.CR;
		
		String res = (AU_L != null) ? AU_L + " " : "";
		if (AU_F != null) res += AU_F;
		if (RPY != null) res += ", " + RPY;
		if ((VOL!=null) || (PAG!=null)) {
			if (J_N!=null) res += ", " + J_N; 
			if (VOL!=null) res += ", V" + VOL;
			if (PAG!=null) res += ", P" + PAG;
		} else {
			res += ", " + J;
		}
		if (DOI!=null) res += ", DOI " + DOI;
		return res;
	}

	

	
	public CRType parseScopus (String line, int[] yearRange) {
		

		// TODO: J8 to implement
		return null;

		
	}
		
	public String getScopus () {
		
		if (type == TYPE_SCOPUS) return this.CR;
		
		String res = "";
		if (AU_A == null) {
			if (AU_L != null) res += AU_L + ", " + AU_F.replaceAll("([A-Z])", "$1."); 
		} else {
			res += AU_A.replaceAll(";", ",");
		}
		res += ",";
		if (TI != null)	res += TI;
		if (RPY != null) res += " (" + RPY + ") ";
		if (J_N != null) res += J_N;
		if (VOL != null) res += ", " + VOL;
		if (PAG != null) res += ", pp." + VOL;
		if (DOI != null) res += ", DOI " + DOI;

		return res;		
	}

	
	
}

//public CRType parseJSON (JSONObject j) {
//	// TODO: J8 to implement
//	return null;
//	
//}
//
//public JSONObject getJSON () {
//	// TODO: J8 to implement
//	return null;
//
//	
//}

