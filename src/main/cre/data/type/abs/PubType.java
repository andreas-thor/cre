package main.cre.data.type.abs;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public abstract class PubType implements Comparable<PubType> {

	
	public abstract Stream<? extends CRType> getCR();
	public abstract int getSizeCR();
	public abstract void addCR(CRType cr, boolean inverse);	

	
	public abstract boolean removeCR(CRType cr, boolean inverse);
	public abstract void removeCRByYear (int[] range, boolean keepCRsWithoutYear, boolean inverse);	
	public abstract void removeCRByProbability (float probability, int offset, AtomicLong noToImportCRs, AtomicLong noAvailableCRs, AtomicInteger currentOffset);
	public abstract void removeAllCRs(boolean inverse);
	
	
	public abstract boolean isFlag();
	public abstract void setFlag(boolean flag);
	
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
	
	
	
	public abstract String toLineString();
	
	
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
	


}
