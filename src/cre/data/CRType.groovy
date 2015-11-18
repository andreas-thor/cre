package cre.data 

import groovy.transform.CompileStatic

@CompileStatic
public class CRType /* implements Comparable<CRType>*/ {

	int ID;
	String CR;
	String AU;
	String AU_F;
	String AU_L;
	String J;
	String J_N;
	String J_S;
	int N_CR;
	int RPY;
	String PAG;
	String VOL;
	String DOI;
	CRCluster CID2;
	int CID_S;
	int VI;
	int CO;
	
	double PERC_YR;
	double PERC_ALL;

	String blockkey;

	// Mapping of internal attribute names to labels
	static Map<String, String> attr = [
		'ID'  : 'ID',
		'CR'  : 'Cited Reference',
		'RPY' : 'Cited Reference Year',
		'N_CR': 'Number of Cited References',
		'PERC_YR': 'Percent in Year',
		'PERC_ALL': 'Percent over all Years',
		'AU': 'Author',
		'AU_L': 'Last Name',
		'AU_F': 'First Name Initial',
		'J': 'Source',
		'J_N': 'Source Title',
		'J_S': 'Title Short',
		'VOL' : 'Volume',
		'PAG' : 'Page',
		'DOI' : 'DOI',
		'CID2': 'ClusterID',
		'CID_S': 'Cluster Size'
	]
	
	public CRType () {
		
	}


	public CRType(int iD, String cR, String aU, String aU_F, String aU_L,
			String j, String j_N, String j_S, int n_CR, int rPY,
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
	

}
