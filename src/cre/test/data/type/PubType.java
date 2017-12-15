package cre.test.data.type;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cre.test.data.CRStatsInfo;
import cre.test.ui.CRTableView.ColDataType;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

public class PubType implements Comparable<PubType> {

	public static enum PubColumn {
		
		ID 	("ID", "ID", ColDataType.INT, PubType::getIDProp),
		PT 	("PT", "PT", ColDataType.STRING, PubType::getPTProp),
		
		AU 	("AU", "AU", ColDataType.STRING, PubType::getAUProp),
		AF 	("AF", "AF", ColDataType.STRING, PubType::getAFProp),
		C1 	("C1", "C1", ColDataType.STRING, PubType::getC1Prop),
		EM 	("EM", "EM", ColDataType.STRING, PubType::getEMProp),
		AA 	("AA", "AA", ColDataType.STRING, PubType::getAAProp),
		
		TI 	("TI", "TI", ColDataType.STRING, PubType::getTIProp),
		PY 	("PY", "PY", ColDataType.INT, PubType::getPYProp),
		SO 	("SO", "SO", ColDataType.STRING, PubType::getSOProp),
		VL 	("VL", "VL", ColDataType.STRING, PubType::getVLProp),
		IS 	("IS", "IS", ColDataType.STRING, PubType::getISProp),
		AR 	("AR", "AR", ColDataType.STRING, PubType::getARProp),
		BP 	("BP", "BP", ColDataType.INT, PubType::getBPProp),
		EP 	("EP", "EP", ColDataType.INT, PubType::getEPProp),
		PG 	("PG", "PG", ColDataType.INT, PubType::getPGProp),
		TC 	("TC", "TC", ColDataType.INT, PubType::getTCProp),
		
		
		DI 	("DI", "DI", ColDataType.STRING, PubType::getDIProp),
		LI 	("LI", "LI", ColDataType.STRING, PubType::getLIProp),
		AB 	("AB", "AB", ColDataType.STRING, PubType::getABProp),
		DE 	("DE", "DE", ColDataType.STRING, PubType::getDEProp),
		DT 	("DT", "DT", ColDataType.STRING, PubType::getDTProp),
		FS 	("FS", "FS", ColDataType.STRING, PubType::getFSProp),
		UT 	("UT", "UT", ColDataType.STRING, PubType::getUTProp);

		
		public String id;
		public String title;
		public ColDataType type;
		public Function<PubType, ObservableValue<?>> prop;
		
		PubColumn(String id, String title, ColDataType type, Function<PubType, ObservableValue<?>> prop) {
			this.id = id;
			this.title = title;
			this.type = type;
			this.prop = prop;
		}
		
		public String getSQLCreateTable() {
			StringBuffer sb = new StringBuffer();
			sb.append("pub_");
			sb.append (this.id);
			sb.append(" ");
			switch (this.type) {
				case INT: sb.append ("int"); break;
				case STRING: sb.append ("varchar"); break;
				default:
			}
			return sb.toString();
		}
		
	}
	

	
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

	public int length; // approx. size for import status bar

	private Set<CRType> crList;

	private boolean flag;
	
	public PubType() {
		super();
		
		crList = new LinkedHashSet<CRType>();
		
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
		
		setFlag(false);
	}
	
	
	
	public String toLineString() {
		return String.format("[%d] %s: %s (%d)", getID(), getAUProp().getValue(), getTI(), getPY());
	}

	public Stream<CRType> getCR() {
		return crList.stream();
	}

	public int getSizeCR() {
		return crList.size();
	}

	/**
	 * Adds a CR to a PUB
	 * 
	 * @param cr
	 *            to be added
	 * @param inverse
	 *            true, if this PUB should also be added to the publist of the
	 *            CR
	 */

	public void addCR(CRType cr, boolean inverse) {
		if (cr == null)
			return;
		if (inverse) {
			cr.addPub(this, false);
		}
		this.crList.add(cr);
	}

	/**
	 * Removes a CR from a PUB
	 * 
	 * @param cr
	 *            to be removed
	 * @param inverse
	 *            true, if this PUB should also be removed from the publist of
	 *            the CR
	 */
	public boolean removeCR(CRType cr, boolean inverse) {
		if (cr == null)
			return false;
		if (inverse) {
			cr.removePub(this, false);
		}
		return this.crList.remove(cr);
	}

	
	public void removeCRByYear (int[] range, boolean keepCRsWithoutYear, boolean inverse) {
		
		
		this.crList.removeIf(cr -> {
			
			boolean toBeRemoved = false;
			if (cr.getRPY()==null) {
				toBeRemoved = !keepCRsWithoutYear;
			} else {
				int rpy = cr.getRPY().intValue();
				if ((range[0]!=CRStatsInfo.NONE) && (range[0]>rpy)) toBeRemoved = true;
				if ((range[1]!=CRStatsInfo.NONE) && (range[1]<rpy)) toBeRemoved = true;
			}
			
			if (toBeRemoved && inverse) {
				cr.removePub(this, false);
			}
			return toBeRemoved;	
		});
		
	}
	
	
	public void removeCRByProbability (float probability, AtomicLong noToImportCRs, AtomicLong noAvailableCRs) {
		
		this.crList.removeIf(cr -> {
			
			boolean remove = true;
			
			if ((noToImportCRs.get()>0) && (probability*noAvailableCRs.get() <= 1.0f*noToImportCRs.get())) {
				noToImportCRs.decrementAndGet();
				remove = false;
			}
			noAvailableCRs.decrementAndGet();
			return remove;
			
		});
	}

	
	public void removeAllCRs(boolean inverse) {
		if (inverse) {
			crList.forEach(cr -> cr.removePub(this, false));
		}
		this.crList.clear();
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

//	public int getLength() {
//		return length;
//	}

//	public Set<CRType> getCrList() {
//		return crList;
//	}



	@Override
	public int compareTo(PubType o) {
		return this.ID.get() - o.ID.get();
	}



	public boolean isFlag() {
		return flag;
	}



	public void setFlag(boolean flag) {
		this.flag = flag;
	}

}
