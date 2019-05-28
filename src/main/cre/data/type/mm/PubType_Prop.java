package main.cre.data.type.mm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class PubType_Prop extends PubType_MM {


	
	// private static String[] csvColumnsPub = new String[] {"PUBID", "PT",
	// "AU", "AF", "C1", "EM", "AA", "TI", "PY", "SO", "VL", "IS", "AR", "BP",
	// "EP", "PG", "TC", "DI", "LI", "AB", "DE", "DT", "FS", "UT"};

	private SimpleIntegerProperty ID;
	
	private SimpleStringProperty PT; // Publication Type (WoS only)

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

	private SimpleStringProperty TI; // Title
	private SimpleObjectProperty<Integer> PY; // Year

	private SimpleStringProperty SO; // Source title
	private SimpleStringProperty VL; // Volume
	private SimpleStringProperty IS; // Issue
	private SimpleStringProperty AR; // Article Number

	private SimpleObjectProperty<Integer> BP; // Beginning Page / Page Start
	private SimpleObjectProperty<Integer> EP; // Ending Page / Page End
	private SimpleObjectProperty<Integer> PG; // Page Count

	private SimpleObjectProperty<Integer> TC; // Times Cited

	private SimpleStringProperty DI; // Digital Object Identifier (DOI)
	private SimpleStringProperty LI; // Link (Scopus only)
	private SimpleStringProperty AB; // Abstract
	private SimpleStringProperty DE; // Author Keywords

	private SimpleStringProperty DT; // Document Typs
	private SimpleStringProperty FS; // File Source (Scopus only)
	private SimpleStringProperty UT; // Unique Article Identifier

	
	public PubType_Prop() {
		super();
		
		ID = new SimpleIntegerProperty();
		PT = new SimpleStringProperty();
		
		AU = new ArrayList<String>();
		AF = new ArrayList<String>();
		C1 = new ArrayList<String[]>();
		EM = new ArrayList<String>();
		AA = new ArrayList<String>();
		
		TI = new SimpleStringProperty();
		PY = new SimpleObjectProperty<Integer>();

		SO = new SimpleStringProperty();
		VL = new SimpleStringProperty();
		IS = new SimpleStringProperty();
		AR = new SimpleStringProperty();

		BP = new SimpleObjectProperty<Integer>();
		EP = new SimpleObjectProperty<Integer>();
		PG = new SimpleObjectProperty<Integer>();

		TC = new SimpleObjectProperty<Integer>();

		DI = new SimpleStringProperty();
		LI = new SimpleStringProperty();
		AB = new SimpleStringProperty();
		DE = new SimpleStringProperty();

		DT = new SimpleStringProperty();
		FS = new SimpleStringProperty();
		UT = new SimpleStringProperty();
	}
	
	
	

	
	public Integer getID() {
		return ID.get();
	}
	public SimpleIntegerProperty getIDProp() {
		return ID;
	}
	public void setID (int iD) {
		ID.set(iD);
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

	public Stream<String> getAU() {
		return AU.stream();
	}
	public int getAUSize() {
		return AU.size();
	}
	public SimpleStringProperty getAUProp() {
		return new SimpleStringProperty(String.join("; ", AU));
	}
	public void addAU(String e) {
		AU.add(e);
	}


	public Stream<String> getAF() {
		return AF.stream();
	}
	public int getAFSize() {
		return AF.size();
	}
	public SimpleStringProperty getAFProp() {
		return new SimpleStringProperty(String.join("; ", AF));
	}
	public void addAF(String e) {
		AF.add(e);
	}
	

	public Stream<String[]> getC1() {
		return C1.stream();
	}
	public int getC1Size() {
		return C1.size();
	}
	public SimpleStringProperty getC1Prop() {
		return new SimpleStringProperty(String.join("; ", C1.stream().map(it -> "["+String.join("; ", it)+"]").collect(Collectors.toList())));
	}
	public void addC1(String[] e) {
		C1.add(e);
	}

	
	public Stream<String> getEM() {
		return EM.stream();
	}
	public int getEMSize() {
		return EM.size();
	}
	public SimpleStringProperty getEMProp() {
		return new SimpleStringProperty(String.join("; ", EM));
	}
	public void addEM(String e) {
		EM.add(e);
	}

	public Stream<String> getAA() {
		return AA.stream();
	}
	public int getAASize() {
		return AA.size();
	}
	public SimpleStringProperty getAAProp() {
		return new SimpleStringProperty(String.join("; ", AA));
	}
	public void addAA(String e) {
		AA.add(e);
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

	public Integer getPY() {
		return this.PY.get();
	}

	public SimpleObjectProperty<Integer> getPYProp() {
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

	public SimpleObjectProperty<Integer> getBPProp() {
		return BP;
	}

	public void setBP(Integer bP) {
		BP.set(bP);
	}

	public Integer getEP() {
		return EP.get();
	}

	public SimpleObjectProperty<Integer> getEPProp() {
		return EP;
	}

	public void setEP(Integer eP) {
		EP.set(eP);
	}

	public Integer getPG() {
		return PG.get();
	}

	public SimpleObjectProperty<Integer> getPGProp() {
		return PG;
	}

	public void setPG(Integer pG) {
		PG.set(pG);
	}

	public Integer getTC() {
		return TC.get();
	}

	public SimpleObjectProperty<Integer> getTCProp() {
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




	



}
