package cre.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class CRType {

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
	
	public int VI = 1;	// visible
	public int CO = 0;	// background color
	
	public Double PERC_YR;
	public Double PERC_ALL;
	
	public int N_PYEARS = 0;	
	public Double PYEAR_PERC;
	public int N_PCT50 = 0;
	public int N_PCT75 = 0;
	public int N_PCT90 = 0;
	public int N_PYEARS2 = 0;	
	
	
	public static byte TYPE_WOS = 1;
	public static byte TYPE_SCOPUS = 2;
	public byte type = 0;	
	

	public ArrayList<PubType> pubList;
	
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
		aMap.put("N_PYEARS", "N_PYEARS");
		aMap.put("PYEAR_PERC", "PYEAR_PERC");
		aMap.put("N_PCT50","N_PCT50");
		aMap.put("N_PCT75","N_PCT75");
		aMap.put("N_PCT90","N_PCT90");
		aMap.put("N_PYEARS2","N_PYEARS2");
        attr = Collections.unmodifiableMap(aMap);
    }
			
    
  
	
}


