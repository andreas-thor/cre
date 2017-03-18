package cre.test.data.type;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class PubType {

	public String PT;	// Publication Type (WoS only)
	
	public List<String> AU	= new ArrayList<String>(); // Authors; each author has format: "<lastname>, <initials_without_dots>"
	public List<String> AF	= new ArrayList<String>(); // Authors Full Name; format: "<lastname>, <firstnames>
	public List<String[]> C1 = new ArrayList<String[]>(); // Authors with Affiliations / Adresses; format: "array ("<lastname>, <firstnames>]", "<affiliation>")
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
	
	public String DI;	// Digital Object Identifier (DOI)
	public String LI;	// Link	(Scopus only)
	public String AB;	// Abstract
	public String DE;	// Author Keywords
	
	public String DT;	// Document Typs
	public String FS;	// File Source	(Scopus only)
	public String UT;	// Unique Article Identifier

	
	public int length;	// approx. size for import status bar

	private Set<CRType> crList = new LinkedHashSet<CRType>();

	
	public String toLineString () {
		return String.format("%s: %s (%d)", String.join(", ", AU), TI, PY);
	}
	
	
	public Integer getPY () {
		return this.PY;
	}
	
	
	public Stream<CRType> getCR() {
		return crList.stream();
	}


	public int getSizeCR() {
		return crList.size();
	}


	/**
	 * Adds a CR to a PUB
	 * @param cr to be added
	 * @param inverse true, if this PUB should also be added to the publist of the CR
	 */
	
	public void addCR(CRType cr, boolean inverse) {
		if (cr==null) return;
		if (inverse) {
			cr.addPub(this, false);
		}
		this.crList.add(cr);
	}
	
	
	/**
	 * Removes a CR from a PUB
	 * @param cr to be removed
	 * @param inverse true, if this PUB should also be removed from the publist of the CR
	 */
	public boolean removeCR (CRType cr, boolean inverse) {
		if (cr==null) return false;
		if (inverse) {
			cr.removePub(this, false);
		}
		return this.crList.remove(cr);
	}
	
	
	
	public void removeAllCRs (boolean inverse) {
		if (inverse) {
			crList.forEach(cr -> cr.removePub(this, false));
		}
		this.crList.clear();
	}
	
}
