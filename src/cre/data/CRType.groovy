package cre.data 

import java.util.Map

import com.orsoncharts.util.json.JSONObject;

import groovy.json.JsonBuilder;
import groovy.json.JsonOutput;
import groovy.transform.CompileStatic

@CompileStatic
public class CRType /* implements Comparable<CRType>*/ {

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
	int N_CR;
	Integer RPY;
	String PAG;
	String VOL;
	String DOI;
	CRCluster CID2;
	int CID_S;
	int VI;
	int CO;
	
	static byte TYPE_WOS = 1
	static byte TYPE_SCOPUS = 2
	byte type;	
	
	Double PERC_YR;
	Double PERC_ALL;

//	String blockkey;
	
	int mergedTo

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
	

	
	public CRType (byte type) {
		N_CR = 1
		VI = 1
		CO = 0
		DOI = ""
		AU = null
		AU_F = null
		AU_L = null
		AU_A = null
		TI = null
		RPY = null
		PAG = ""
		VOL = "" 
		mergedTo = -1
		RPY = null
		PERC_ALL = null
		PERC_YR = null
		this.type = type
	}

	public CRType(int iD, String cR, String aU, String aU_F, String aU_L,
			String j, String j_N, String j_S, int n_CR, Integer rPY,
			String pAG, String vOL, String dOI, CRCluster cID2, int cID_S,
			int vI, int cO) {
			
		super()
		ID = iD
		CR = cR
		AU = aU
		AU_F = aU_F
		AU_L = aU_L
		J = j
		J_N = j_N
		J_S = j_S
		N_CR = n_CR
		RPY = rPY
		PAG = pAG
		VOL = vOL
		DOI = dOI
		CID2 = cID2
		CID_S = cID_S
		VI = vI
		CO = cO  
	}
	

	public String getCRString (byte targetType) {
		
		if (targetType==this.type) return this.CR
		
		if (targetType==TYPE_WOS) {
			return AU_L + " " + AU_F + ", " + RPY + ", " + J
		}
		
		if (targetType==TYPE_SCOPUS) {
			String a = (AU_A == null) ? AU_L + ", " + AU_F.replaceAll("([A-Z])", "\$1.") : AU_A.replaceAll(";", ",")
			return a + ", " + TI?:"" + " (" + RPY + ") " + J
		}
		
	}
	
	
	public JSONObject getJSON () {
		
		JsonBuilder jb = new JsonBuilder()
		
//		def a = jb {
//			ID: this.ID
//		}
//		
////		JsonOutput.toJson([
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
		
//		jb
	}
			
}
