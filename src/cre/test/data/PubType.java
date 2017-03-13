package cre.test.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

public class PubType {

	
	public String PT;	// Publication Type (WoS only)
	
	public List<String> AU	= new ArrayList<String>(); // Authors; each author has format: "<lastname>, <initials_without_dots>"
	public List<String> AF	= new ArrayList<String>(); // Authors Full Name; format: "<lastname>, <firstnames>
	public List<String[]> C1 	= new ArrayList<String[]>(); // Authors with Affiliations / Adresses; format: "array ("<lastname>, <firstnames>]", "<affiliation>")
	public List<String>	EM = new ArrayList<String>(); // E-Mail Adressess
	public List<String>	AA = new ArrayList<String>();	// All affiliations	(Scopus only)
	
	public String TI; 	// Title
	public Integer PY; 	// Year
	
	public String SO;	// Source title
	public String VL;	// Volume
	public String IS; 	// Issue
	public String AR; 	// Article Number
	
	public Integer BP;	// Beginning Page / Page Start
	public Integer EP;	// Ending Page / Page End
	public Integer PG;	// Page Count
	
	public Integer TC;	// Times Cited
	
	protected HashSet<CRType> crList = new HashSet<CRType>();
	
	public String DI;	// Digital Object Identifier (DOI)
	public String LI;// Link	(Scopus only)
	public String AB;	// Abstract
	public String DE;	// Author Keywords
	
	public String DT;	// Document Typs
	public String FS;	// File Source	(Scopus only)
	public String UT;	// Unique Article Identifier

	
	public int length;	// approx. size for import status bar


	
	public Integer getPY () {
		return this.PY;
	}
	
	
	public Stream<CRType> getCR() {
		return crList.stream();
	}


	public int getSizeCR() {
		return crList.size();
	}


	public void addCR(CRType cr) {
		if (cr != null) {
			this.crList.add(cr);
			cr.addPub(this);
		}
	}
	
	public boolean removeCR (CRType cr) {
		return this.crList.remove(cr);
	}
	
}
