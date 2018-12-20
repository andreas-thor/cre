package main.cre.data.type;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import main.cre.data.CRStatsInfo;
import main.cre.ui.CRTableView.ColDataType;

public abstract class PubType implements Comparable<PubType> {

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
	


	public int length; // approx. size for import status bar

	
	private Set<CRType> crList;

	private boolean flag;
	
	
	public static PubType create() {
		return new PubType_Prop();
//		return new PubType_Tiny();
	}
	
	public PubType() {
		super();
		
		crList = new LinkedHashSet<CRType>();
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
	
	
	public void removeCRByProbability (float probability, int offset, AtomicLong noToImportCRs, AtomicLong noAvailableCRs, AtomicInteger currentOffset) {
		
		this.crList.removeIf(cr -> {
			
			boolean remove = true;
			
			if ((noToImportCRs.get()>0) && (probability*noAvailableCRs.get() <= 1.0f*noToImportCRs.get())) {
			
				if (currentOffset.get()==offset) {
					noToImportCRs.decrementAndGet();
					currentOffset.set(0);
					remove = false;
				} else {
					currentOffset.incrementAndGet();
				}
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

	
	public abstract Integer getID();
	public abstract SimpleIntegerProperty getIDProp();
	public abstract void setID (int iD);
	
	public abstract String getPT();
	public abstract SimpleStringProperty getPTProp();
	public abstract void setPT(String pT);
	
	public abstract Stream<String> getAU();
	public abstract int getAUSize();
	public abstract SimpleStringProperty getAUProp();
	public abstract void addAU(String e);

	public abstract Stream<String> getAF();
	public abstract int getAFSize(); 
	public abstract SimpleStringProperty getAFProp();
	public abstract void addAF(String e);

	public abstract Stream<String[]> getC1();
	public abstract int getC1Size();
	public abstract SimpleStringProperty getC1Prop();
	public abstract void addC1(String[] e);
	
	public abstract Stream<String> getEM();
	public abstract int getEMSize();
	public abstract SimpleStringProperty getEMProp();
	public abstract void addEM(String e);
	
	public abstract Stream<String> getAA();
	public abstract int getAASize();
	public abstract SimpleStringProperty getAAProp();
	public abstract void addAA(String e);

	public abstract String getTI();
	public abstract SimpleStringProperty getTIProp();
	public abstract void setTI(String tI);

	public abstract Integer getPY();
	public abstract SimpleObjectProperty<Integer> getPYProp();
	public abstract void setPY(Integer pY);

	public abstract String getSO();
	public abstract SimpleStringProperty getSOProp();
	public abstract void setSO(String sO);

	public abstract String getVL();
	public abstract SimpleStringProperty getVLProp();
	public abstract void setVL(String vL);
	
	public abstract String getIS();
	public abstract SimpleStringProperty getISProp();
	public abstract void setIS(String iS);
	
	public abstract String getAR();
	public abstract SimpleStringProperty getARProp();
	public abstract void setAR(String aR);
	
	public abstract Integer getBP();
	public abstract SimpleObjectProperty<Integer> getBPProp();
	public abstract void setBP(Integer bP);
	
	public abstract Integer getEP();
	public abstract SimpleObjectProperty<Integer> getEPProp();
	public abstract void setEP(Integer eP);

	public abstract Integer getPG();
	public abstract SimpleObjectProperty<Integer> getPGProp();
	public abstract void setPG(Integer pG);

	public abstract Integer getTC();
	public abstract SimpleObjectProperty<Integer> getTCProp();
	public abstract void setTC(Integer tC);

	public abstract String getDI();
	public abstract SimpleStringProperty getDIProp();
	public abstract void setDI(String dI);

	public abstract String getLI();
	public abstract SimpleStringProperty getLIProp();
	public abstract void setLI(String lI);

	public abstract String getAB();
	public abstract SimpleStringProperty getABProp();
	public abstract void setAB(String aB);

	public abstract String getDE();
	public abstract SimpleStringProperty getDEProp();
	public abstract void setDE(String dE);

	public abstract String getDT();
	public abstract SimpleStringProperty getDTProp();
	public abstract void setDT(String dT);

	public abstract String getFS();
	public abstract SimpleStringProperty getFSProp();
	public abstract void setFS(String fS);

	public abstract String getUT();
	public abstract SimpleStringProperty getUTProp();
	public abstract void setUT(String uT);

//	public int getLength() {
//		return length;
//	}

//	public Set<CRType> getCrList() {
//		return crList;
//	}



	@Override
	public int compareTo(PubType o) {
		return this.getID().intValue() - o.getID().intValue();
	}


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

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

}
