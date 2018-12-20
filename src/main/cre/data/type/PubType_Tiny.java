package main.cre.data.type;

import java.util.stream.Stream;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class PubType_Tiny extends PubType {

	private int iD;
	private Integer pY;
	
	@Override
	public boolean equals(Object obj) {

		PubType_Tiny p = (PubType_Tiny) obj;
		return this.iD == p.iD;
	}
	
	@Override
	public int hashCode() {
		return this.iD;
	}
	
	@Override
	public Integer getID() {
		// TODO Auto-generated method stub
		return this.iD;
	}

	@Override
	public SimpleIntegerProperty getIDProp() {
		// TODO Auto-generated method stub
		return new SimpleIntegerProperty(this.iD);
	}

	@Override
	public void setID(int iD) {
		this.iD = iD;
	}

	@Override
	public String getPT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringProperty getPTProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPT(String pT) {
		// TODO Auto-generated method stub

	}

	@Override
	public Stream<String> getAU() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAUSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SimpleStringProperty getAUProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAU(String e) {
		// TODO Auto-generated method stub

	}

	@Override
	public Stream<String> getAF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAFSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SimpleStringProperty getAFProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAF(String e) {
		// TODO Auto-generated method stub

	}

	@Override
	public Stream<String[]> getC1() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getC1Size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SimpleStringProperty getC1Prop() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addC1(String[] e) {
		// TODO Auto-generated method stub

	}

	@Override
	public Stream<String> getEM() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getEMSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SimpleStringProperty getEMProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addEM(String e) {
		// TODO Auto-generated method stub

	}

	@Override
	public Stream<String> getAA() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAASize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SimpleStringProperty getAAProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAA(String e) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getTI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringProperty getTIProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTI(String tI) {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer getPY() {
		return this.pY;
	}

	@Override
	public SimpleObjectProperty<Integer> getPYProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPY(Integer pY) {
		this.pY = pY;
	}

	@Override
	public String getSO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringProperty getSOProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSO(String sO) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getVL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringProperty getVLProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVL(String vL) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getIS() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringProperty getISProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIS(String iS) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringProperty getARProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAR(String aR) {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer getBP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleObjectProperty<Integer> getBPProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBP(Integer bP) {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer getEP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleObjectProperty<Integer> getEPProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEP(Integer eP) {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer getPG() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleObjectProperty<Integer> getPGProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPG(Integer pG) {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer getTC() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleObjectProperty<Integer> getTCProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTC(Integer tC) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringProperty getDIProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDI(String dI) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getLI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringProperty getLIProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLI(String lI) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAB() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringProperty getABProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAB(String aB) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDE() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringProperty getDEProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDE(String dE) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringProperty getDTProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDT(String dT) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getFS() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringProperty getFSProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFS(String fS) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getUT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringProperty getUTProp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUT(String uT) {
		// TODO Auto-generated method stub

	}

}
