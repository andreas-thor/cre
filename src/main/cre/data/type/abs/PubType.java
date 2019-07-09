package main.cre.data.type.abs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.cre.store.db.PubType_DB;
import main.cre.store.mm.PubType_MM;

public abstract class PubType<C extends CRType<?>> implements Comparable<PubType<C>> {


	private Integer ID;
	
	private String PT; // Publication Type (WoS only)

	// Authors; each author has format: "<lastname>, <initials_without_dots>"
	private List<String> AU; 
	// Authors Full Name; format: "<lastname>, <firstnames>
	private List<String> AF; 
	// Authors with Affiliations / Adresses; format: "array ("<lastname>, <firstnames>]", "<affiliation>")
	private List<String[]> C1; 
	// E-Mail Adressess
	private List<String> EM; 
	// All affiliations (Scopus only)
	private List<String> AA; 

	private String TI; // Title
	private Integer PY; // Year

	private String SO; // Source title
	private String VL; // Volume
	private String IS; // Issue
	private String AR; // Article Number

	private Integer BP; // Beginning Page / Page Start
	private Integer EP; // Ending Page / Page End
	private Integer PG; // Page Count

	private Integer TC; // Times Cited

	private String DI; // Digital Object Identifier (DOI)
	private String LI; // Link (Scopus only)
	private String AB; // Abstract
	private String DE; // Author Keywords

	private String DT; // Document Typs
	private String FS; // File Source (Scopus only)
	private String UT; // Unique Article Identifier
	
	private boolean flag;
	
	private int length; // approx. size for import status bar

	
	public static PubType create() {
		
		switch (CRTable.type) {
		case MM: return new PubType_MM();	
		case DB: return new PubType_DB();
		default: return null;
		}
		
	}
	
	
	public PubType() {
		AU = new ArrayList<String>();
		AF = new ArrayList<String>();
		C1 = new ArrayList<String[]>();
		EM = new ArrayList<String>();
		AA = new ArrayList<String>();
	}
	
	public Stream<C> getCR() {
		return getCR (false);
	}

	public abstract Stream<C> getCR(boolean sortById);
	
	public abstract int getSizeCR();
	


	public String toLineString() {
		return String.format("[%d] %s: %s (%d)", getID(), getAU().collect(Collectors.joining("; ")), getTI(), getPY());
	}	
	
	@Override
	public int compareTo(PubType<C> o) {
		return this.getID().intValue() - o.getID().intValue();
	}

/*
	@Override
	public boolean equals(Object obj) {

		PubType p = (PubType) obj;
		

		
		if (!this.getTIProp().getValueSafe().equals(p.getTIProp().getValueSafe())) return false;
		if (!this.getPTProp().getValueSafe().equals(p.getPTProp().getValueSafe())) return false;
		
		if (!this.getPYProp().isEqualTo(p.getPYProp()).get()) return false;

		if (!this.getAU().equals(p.getAU())) return false;
		
		if (!this.getSOProp().getValueSafe().equals(p.getSOProp().getValueSafe())) return false;
		if (!this.getVLProp().getValueSafe().equals(p.getVLProp().getValueSafe())) return false;
		if (!this.getISProp().getValueSafe().equals(p.getISProp().getValueSafe())) return false;
		if (!this.getARProp().getValueSafe().equals(p.getARProp().getValueSafe())) return false;

		if (!this.getBPProp().isEqualTo(p.getBPProp()).get()) return false;
		if (!this.getEPProp().isEqualTo(p.getEPProp()).get()) return false;
		if (!this.getPGProp().isEqualTo(p.getPGProp()).get()) return false;
		if (!this.getTCProp().isEqualTo(p.getTCProp()).get()) return false;

		if (!this.getDIProp().getValueSafe().equals(p.getDIProp().getValueSafe())) return false;
		if (!this.getLIProp().getValueSafe().equals(p.getLIProp().getValueSafe())) return false;
		if (!this.getABProp().getValueSafe().equals(p.getABProp().getValueSafe())) return false;
		if (!this.getDEProp().getValueSafe().equals(p.getDEProp().getValueSafe())) return false;

		if (!this.getDTProp().getValueSafe().equals(p.getDTProp().getValueSafe())) return false;
		if (!this.getFSProp().getValueSafe().equals(p.getFSProp().getValueSafe())) return false;
		if (!this.getUTProp().getValueSafe().equals(p.getUTProp().getValueSafe())) return false;
		
		return true;
	}
	
	
	
	@Override
	public int hashCode() {
		return (this.getTI() == null) ? 0 : this.getTI().hashCode();
	}
	
	*/

	public Integer getID() {
		return ID;
	}
	public void setID(Integer iD) {
		ID = iD;
	}
	public String getPT() {
		return PT;
	}
	public void setPT(String pT) {
		PT = pT;
	}
	public Stream<String> getAU() {
		return AU.stream();
	}
	public void addAU(String aU) {
		AU.add(aU);
	}
	public Stream<String> getAF() {
		return AF.stream();
	}
	public void addAF(String aF) {
		AF.add(aF);
	}
	public Stream<String[]> getC1() {
		return C1.stream();
	}
	public void addC1(String[] c1) {
		if (c1.length==2) C1.add(c1);
	}
	public Stream<String> getEM() {
		return EM.stream();
	}
	public void addEM(String eM) {
		EM.add(eM);
	}
	public Stream<String> getAA() {
		return AA.stream();
	}
	public void addAA(String aA) {
		AA.add(aA);
	}
	public String getTI() {
		return TI;
	}
	public void setTI(String tI) {
		TI = tI;
	}
	public Integer getPY() {
		return PY;
	}
	public void setPY(Integer pY) {
		PY = pY;
	}
	public String getSO() {
		return SO;
	}
	public void setSO(String sO) {
		SO = sO;
	}
	public String getVL() {
		return VL;
	}
	public void setVL(String vL) {
		VL = vL;
	}
	public String getIS() {
		return IS;
	}
	public void setIS(String iS) {
		IS = iS;
	}
	public String getAR() {
		return AR;
	}
	public void setAR(String aR) {
		AR = aR;
	}
	public Integer getBP() {
		return BP;
	}
	public void setBP(Integer bP) {
		BP = bP;
	}
	public Integer getEP() {
		return EP;
	}
	public void setEP(Integer eP) {
		EP = eP;
	}
	public Integer getPG() {
		return PG;
	}
	public void setPG(Integer pG) {
		PG = pG;
	}
	public Integer getTC() {
		return TC;
	}
	public void setTC(Integer tC) {
		TC = tC;
	}
	public String getDI() {
		return DI;
	}
	public void setDI(String dI) {
		DI = dI;
	}
	public String getLI() {
		return LI;
	}
	public void setLI(String lI) {
		LI = lI;
	}
	public String getAB() {
		return AB;
	}
	public void setAB(String aB) {
		AB = aB;
	}
	public String getDE() {
		return DE;
	}
	public void setDE(String dE) {
		DE = dE;
	}
	public String getDT() {
		return DT;
	}
	public void setDT(String dT) {
		DT = dT;
	}
	public String getFS() {
		return FS;
	}
	public void setFS(String fS) {
		FS = fS;
	}
	public String getUT() {
		return UT;
	}
	public void setUT(String uT) {
		UT = uT;
	}
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	
	public int getLength() {
		return length;
	}


	public void setLength(int length) {
		this.length = length;
	}



	
		
}
