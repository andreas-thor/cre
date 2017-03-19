package cre.test.data.type;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class PubType {

//		private static String[] csvColumnsPub = new String[] {"PUBID", "PT", "AU", "AF", "C1", "EM", "AA", "TI", "PY", "SO", "VL", "IS", "AR", "BP", "EP", "PG", "TC", "DI", "LI", "AB", "DE", "DT", "FS", "UT"}; 

	
	
	
	private SimpleStringProperty PT;	// Publication Type (WoS only)
	
	private List<String> AU	= new ArrayList<String>(); // Authors; each author has format: "<lastname>, <initials_without_dots>"
	private List<String> AF	= new ArrayList<String>(); // Authors Full Name; format: "<lastname>, <firstnames>
	private List<String[]> C1 = new ArrayList<String[]>(); // Authors with Affiliations / Adresses; format: "array ("<lastname>, <firstnames>]", "<affiliation>")
	private List<String>	EM = new ArrayList<String>(); // E-Mail Adressess
	private List<String>	AA = new ArrayList<String>();	// All affiliations	(Scopus only)
	
	private SimpleStringProperty TI; 	// Title
	private SimpleIntegerProperty PY; 	// Year
	
	private SimpleStringProperty SO;	// Source title
	private SimpleStringProperty VL;	// Volume
	private SimpleStringProperty IS; 	// Issue
	private SimpleStringProperty AR; 	// Article Number
	
	private SimpleIntegerProperty BP;	// Beginning Page / Page Start
	private SimpleIntegerProperty EP;	// Ending Page / Page End
	private SimpleIntegerProperty PG;	// Page Count
	
	private SimpleIntegerProperty TC;	// Times Cited
	
	private SimpleStringProperty DI;	// Digital Object Identifier (DOI)
	private SimpleStringProperty LI;	// Link	(Scopus only)
	private SimpleStringProperty AB;	// Abstract
	private SimpleStringProperty DE;	// Author Keywords
	
	private SimpleStringProperty DT;	// Document Typs
	private SimpleStringProperty FS;	// File Source	(Scopus only)
	private SimpleStringProperty UT;	// Unique Article Identifier

	
	public int length;	// approx. size for import status bar

	private Set<CRType> crList = new LinkedHashSet<CRType>();

	
	public String toLineString () {
		return String.format("%s: %s (%d)", String.join(", ", getAU()), getTI(), getPY());
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


	public String getPT() {
		return PT.get();
	}

	public SimpleStringProperty getPTProp() {
		return PT;
	}
	public void setPT(String pT) {
		PT.set(pT);
	}


	public List<String> getAU() {
		return AU;
	}
	public void setAU(List<String> aU) {
		AU = aU;
	}


	public List<String> getAF() {
		return AF;
	}
	public void setAF(List<String> aF) {
		AF = aF;
	}


	public List<String[]> getC1() {
		return C1;
	}
	public void setC1(List<String[]> c1) {
		C1 = c1;
	}


	public List<String> getEM() {
		return EM;
	}
	public void setEM(List<String> eM) {
		EM = eM;
	}


	public List<String> getAA() {
		return AA;
	}
	public void setAA(List<String> aA) {
		AA = aA;
	}


	public String getTI() {
		return TI.get();
	}
	public SimpleStringProperty getTIProp() {
		return TI;
	}
	public void setTI(String tI) {
		TI.set(tI);
	}

	
	public Integer getPY () {
		return this.PY.get();
	}
	public SimpleIntegerProperty getPYProp() {
		return PY;
	}
	public void setPY(Integer pY) {
		PY.set(pY);
	}


	public String getSO() {
		return SO.get();
	}
	public SimpleStringProperty getSOProp() {
		return SO;
	}
	public void setSO(String sO) {
		SO.set(sO);
	}


	public String getVL() {
		return VL.get();
	}
	public SimpleStringProperty getVLProp() {
		return VL;
	}
	public void setVL(String vL) {
		VL.set(vL);
	}


	public String getIS() {
		return IS.get();
	}
	public SimpleStringProperty getISProp() {
		return IS;
	}
	public void setIS(String iS) {
		IS.set(iS);
	}


	public String getAR() {
		return AR.get();
	}
	public SimpleStringProperty getARProp() {
		return AR;
	}
	public void setAR(String aR) {
		AR.set(aR);
	}


	public Integer getBP() {
		return BP.get();
	}
	public SimpleIntegerProperty getBPProp() {
		return BP;
	}
	public void setBP(Integer bP) {
		BP.set(bP);
	}


	public Integer getEP() {
		return EP.get();
	}
	public SimpleIntegerProperty getEPProp() {
		return EP;
	}
	public void setEP(Integer eP) {
		EP.set(eP);
	}


	public Integer getPG() {
		return PG.get();
	}
	public SimpleIntegerProperty getPGProp() {
		return PG;
	}
	public void setPG(Integer pG) {
		PG.set(pG);
	}


	public Integer getTC() {
		return TC.get();
	}
	public SimpleIntegerProperty getTCProp() {
		return TC;
	}
	public void setTC(Integer tC) {
		TC.set(tC);
	}


	public String getDI() {
		return DI.get();
	}
	public SimpleStringProperty getDIProp() {
		return DI;
	}
	public void setDI(String dI) {
		DI.set(dI);
	}


	public String getLI() {
		return LI.get();
	}
	public SimpleStringProperty getLIProp() {
		return LI;
	}
	public void setLI(String lI) {
		LI.set(lI);
	}


	public String getAB() {
		return AB.get();
	}
	public SimpleStringProperty getABProp() {
		return AB;
	}
	public void setAB(String aB) {
		AB.set(aB);
	}


	public String getDE() {
		return DE.get();
	}
	public SimpleStringProperty getDEProp() {
		return DE;
	}
	public void setDE(String dE) {
		DE.set(dE);
	}

	
	public String getDT() {
		return DT.get();
	}
	public SimpleStringProperty getDTProp() {
		return DT;
	}
	public void setDT(String dT) {
		DT.set(dT);
	}


	public String getFS() {
		return FS.get();
	}
	public SimpleStringProperty getFSProp() {
		return FS;
	}
	public void setFS(String fS) {
		FS.set(fS);
	}


	public String getUT() {
		return UT.get();
	}
	public SimpleStringProperty getUTProp() {
		return UT;
	}
	public void setUT(String uT) {
		UT.set(uT);
	}


























































































	











	






















	public int getLength() {
		return length;
	}





	public Set<CRType> getCrList() {
		return crList;
	}
	
	
	
	
}
