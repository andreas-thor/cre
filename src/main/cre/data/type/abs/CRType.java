package main.cre.data.type.abs;

import java.util.stream.Stream;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import main.cre.data.type.db.CRType_DB;
import main.cre.data.type.mm.CRType_Member;

public abstract class CRType implements Comparable<CRType> {

	public static enum FORMATTYPE { 
		WOS, 	
		SCOPUS 
	};
	
	public static enum PERCENTAGE {
		
		P50(0.5d), P75(0.75d), P90(0.9d), P99(0.99d), P999(0.999d);
		
		public final double threshold;
		
		PERCENTAGE (double v) {
			this.threshold = v;
		}
		
	};
	
	
	public static CRType create() {
		
		switch (CRTable.type) {
		case MM: return new CRType_Member();	//		return new CRType_Tiny();
		case DB: return new CRType_DB();
		default: return null;
		}
		
	}
	
	public abstract int getNumberOfPubs();
	public abstract Stream<? extends PubType> getPub();
	public abstract void addPub(PubType pubType, boolean b);
	
	public abstract boolean removePub(PubType pub, boolean inverse);
	public abstract void removeAllPubs(boolean inverse);
	
	public abstract int getID();
	public abstract SimpleIntegerProperty getIDProp();
	public abstract void setID(int iD);

	public abstract String getCR();
	public abstract SimpleStringProperty getCRProp();
	public abstract void setCR(String cR);

	public abstract String getAU();
	public abstract SimpleStringProperty getAUProp();
	public abstract void setAU(String aU);

	public abstract String getAU_F();
	public abstract SimpleStringProperty getAU_FProp();
	public abstract void setAU_F(String aU_F);

	public abstract String getAU_L();
	public abstract SimpleStringProperty getAU_LProp();
	public abstract void setAU_L(String aU_L);

	public abstract String getAU_A();
	public abstract SimpleStringProperty getAU_AProp();
	public abstract void setAU_A(String aU_A);

	public abstract String getTI();
	public abstract SimpleStringProperty getTIProp();
	public abstract void setTI(String tI);

	public abstract String getJ();
	public abstract SimpleStringProperty getJProp();
	public abstract void setJ(String j);

	public abstract String getJ_N();
	public abstract SimpleStringProperty getJ_NProp();
	public abstract void setJ_N(String j_N);

	public abstract String getJ_S();
	public abstract SimpleStringProperty getJ_SProp();
	public abstract void setJ_S(String j_S);

	public abstract int getN_CR();
	public abstract SimpleIntegerProperty getN_CRProp();
	public abstract void resetN_CR();
	public abstract void setN_CR(int n_CR);
	
	public abstract Integer getRPY();
	public abstract SimpleObjectProperty<Integer> getRPYProp();
	public abstract void setRPY(Integer rPY);

	public abstract String getPAG();
	public abstract SimpleStringProperty getPAGProp();
	public abstract void setPAG(String pAG);

	public abstract String getVOL();
	public abstract SimpleStringProperty getVOLProp();
	public abstract void setVOL(String vOL);

	public abstract String getDOI();
	public abstract SimpleStringProperty getDOIProp();
	public abstract void setDOI(String dOI);

	public abstract CRCluster getCID2();
	public abstract void setCID2(CRCluster cID2);
	public abstract void setCID2(String s);
	public abstract void setCID2(CRType cr);
	public abstract void setCID2(CRType cr, int c1);

	public abstract int getCID_S();
	public abstract SimpleIntegerProperty getCID_SProp();

	public abstract boolean getVI();
	public abstract SimpleBooleanProperty getVIProp();
	public abstract void setVI(boolean vI);

	public abstract int getCO();
	public abstract SimpleIntegerProperty getCOProp();
	public abstract void setCO(int cO);

	public abstract Double getPERC_YR();
	public abstract SimpleDoubleProperty getPERC_YRProp();
	public abstract void setPERC_YR(Double pERC_YR);

	public abstract Double getPERC_ALL();
	public abstract SimpleDoubleProperty getPERC_ALLProp();
	public abstract void setPERC_ALL(Double pERC_ALL);

	public abstract int getN_PYEARS();
	public abstract SimpleIntegerProperty getN_PYEARSProp();
	public abstract void setN_PYEARS(int n_PYEARS);

	public abstract Double getPYEAR_PERC();
	public abstract SimpleDoubleProperty getPYEAR_PERCProp();
	public abstract void setPYEAR_PERC(Double pYEAR_PERC);

	public abstract int getN_PCT(PERCENTAGE perc);
	public abstract SimpleIntegerProperty getN_PCTProp(PERCENTAGE perc);
	public abstract void setN_PCT(PERCENTAGE perc, int n);
	
	public int getN_PCT50() {
		return getN_PCT(PERCENTAGE.P50);
	}
	
	public SimpleIntegerProperty getN_PCT50Prop() {
		return getN_PCTProp(PERCENTAGE.P50);
	}
	
	public void setN_PCT50(int n) {
		setN_PCT(PERCENTAGE.P50, n);
	}

	
	public int getN_PCT75() {
		return getN_PCT(PERCENTAGE.P75);
	}
	
	public SimpleIntegerProperty getN_PCT75Prop() {
		return getN_PCTProp(PERCENTAGE.P75);
	}
	
	public void setN_PCT75(int n) {
		setN_PCT(PERCENTAGE.P75, n);
	}
	
	public int getN_PCT90() {
		return getN_PCT(PERCENTAGE.P90);
	}
	
	public SimpleIntegerProperty getN_PCT90Prop() {
		return getN_PCTProp(PERCENTAGE.P90);
	}
	
	public void setN_PCT90(int n) {
		setN_PCT(PERCENTAGE.P90, n);
	}

	public int getN_PCT99() {
		return getN_PCT(PERCENTAGE.P99);
	}
	
	public SimpleIntegerProperty getN_PCT99Prop() {
		return getN_PCTProp(PERCENTAGE.P99);
	}
	
	public void setN_PCT99(int n) {
		setN_PCT(PERCENTAGE.P99, n);
	}

	public int getN_PCT999() {
		return getN_PCT(PERCENTAGE.P999);
	}
	
	public SimpleIntegerProperty getN_PCT999Prop() {
		return getN_PCTProp(PERCENTAGE.P999);
	}
	
	public void setN_PCT999(int n) {
		setN_PCT(PERCENTAGE.P999, n);
	}
	
	
	
	public abstract int getN_PCT_AboveAverage(PERCENTAGE perc);
	public abstract SimpleIntegerProperty getN_PCT_AboveAverageProp(PERCENTAGE perc);
	public abstract void setN_PCT_AboveAverage(PERCENTAGE perc, int n);
	
	public int getN_PCT_AboveAverage50() {
		return getN_PCT_AboveAverage(PERCENTAGE.P50);
	}
	
	public SimpleIntegerProperty getN_PCT_AboveAverage50Prop() {
		return getN_PCT_AboveAverageProp(PERCENTAGE.P50);
	}
	
	public void setN_PCT_AboveAverage50(int n) {
		setN_PCT_AboveAverage(PERCENTAGE.P50, n);
	}

	
	public int getN_PCT_AboveAverage75() {
		return getN_PCT_AboveAverage(PERCENTAGE.P75);
	}
	
	public SimpleIntegerProperty getN_PCT_AboveAverage75Prop() {
		return getN_PCT_AboveAverageProp(PERCENTAGE.P75);
	}
	
	public void setN_PCT_AboveAverage75(int n) {
		setN_PCT_AboveAverage(PERCENTAGE.P75, n);
	}
	
	public int getN_PCT_AboveAverage90() {
		return getN_PCT_AboveAverage(PERCENTAGE.P90);
	}
	
	public SimpleIntegerProperty getN_PCT_AboveAverage90Prop() {
		return getN_PCT_AboveAverageProp(PERCENTAGE.P90);
	}
	
	public void setN_PCT_AboveAverage90(int n) {
		setN_PCT_AboveAverage(PERCENTAGE.P90, n);
	}	

	
	public int getN_PCT_AboveAverage99() {
		return getN_PCT_AboveAverage(PERCENTAGE.P99);
	}
	
	public SimpleIntegerProperty getN_PCT_AboveAverage99Prop() {
		return getN_PCT_AboveAverageProp(PERCENTAGE.P99);
	}
	
	public void setN_PCT_AboveAverage99(int n) {
		setN_PCT_AboveAverage(PERCENTAGE.P99, n);
	}	
	

	public int getN_PCT_AboveAverage999() {
		return getN_PCT_AboveAverage(PERCENTAGE.P999);
	}
	
	public SimpleIntegerProperty getN_PCT_AboveAverage999Prop() {
		return getN_PCT_AboveAverageProp(PERCENTAGE.P999);
	}
	
	public void setN_PCT_AboveAverage999(int n) {
		setN_PCT_AboveAverage(PERCENTAGE.P999, n);
	}	
	
	
	
	public abstract int getN_PYEARS2();
	public abstract SimpleIntegerProperty getN_PYEARS2Prop();
	public abstract void setN_PYEARS2(int n_PYEARS2);

	public abstract int getSEARCH_SCORE();
	public abstract SimpleIntegerProperty getSEARCH_SCOREProp();
	public abstract void setSEARCH_SCORE(Double sEARCH_SCORE);

	public abstract String getSEQUENCE();
	public abstract SimpleStringProperty getSEQUENCEProp();
	public abstract void setSEQUENCE(String sEQUENCE);

	public abstract String getTYPE();
	public abstract SimpleStringProperty getTYPEProp();
	public abstract void setTYPE(String tYPE);
	
	
	public abstract boolean isFlag();
	public abstract void setFlag(boolean flag);

	public abstract FORMATTYPE getType();
	public abstract void setType(FORMATTYPE type);
	
	
	public void copyNotNULLValues (CRType cr) {
		
		if (cr.getCR()!=null) 		this.setCR(cr.getCR());
		if (cr.getAU()!=null) 		this.setAU(cr.getAU());
		if (cr.getAU_F()!=null) 	this.setAU_F(cr.getAU_F());
		if (cr.getAU_L()!=null) 	this.setAU_L(cr.getAU_L());
		if (cr.getAU_A()!=null) 	this.setAU_A(cr.getAU_A());
		if (cr.getTI()!=null) 		this.setTI(cr.getTI());
		if (cr.getJ()!=null) 		this.setJ(cr.getJ());
		if (cr.getJ_N()!=null) 		this.setJ_N(cr.getJ_N());
		if (cr.getJ_S()!=null) 		this.setJ_S(cr.getJ_S());
		if (cr.getRPY()!=null) 		this.setRPY(cr.getRPY());
		if (cr.getPAG()!=null) 		this.setPAG(cr.getPAG());
		if (cr.getVOL()!=null) 		this.setVOL(cr.getVOL());
		if (cr.getDOI()!=null) 		this.setDOI(cr.getDOI());
	}
	

	@Override
	public boolean equals(Object obj) {
		return this.getCR().equals(((CRType)obj).getCR());
	}
	

	@Override
	public int hashCode() {
		return this.getCR().hashCode();
	}


	@Override
	public int compareTo(CRType o) {
		return this.getID() - o.getID();
	}
	
	
	@Override
	public String toString() {
		
		StringBuffer result = new StringBuffer();
		result.append (String.valueOf(this.getID()));
		result.append("\t");
		result.append ((this.getCR()==null)?""		:this.getCR());
		result.append("\t");
		result.append (this.getVI());
//		result.append("\t");
//		result.append ((this.getAU()==null)?""		:this.getAU());
//		result.append("\t");
//		result.append ((this.getAU_F()==null)?""	:this.getAU_F());
//		result.append("\t");
//		result.append ((this.getAU_L()==null)?""	:this.getAU_L());
//		result.append("\t");
//		result.append ((this.getAU_A()==null)?""	:this.getAU_A());
//		result.append("\t");
//		result.append ((this.getTI()==null)?""		:this.getTI());
//		result.append("\t");
//		result.append ((this.getJ()==null)?""		:this.getJ());
//		result.append("\t");
//		result.append ((this.getJ_N()==null)?""		:this.getJ_N());
//		result.append("\t");
//		result.append ((this.getJ_S()==null)?""		:this.getJ_S());
//		result.append("\t");
//		result.append (String.valueOf(this.getN_CR()));
//		result.append("\t");
//		result.append ((this.getRPY()==null)?""		:this.getRPY().toString());
//		result.append("\t");
//		result.append ((this.getPAG()==null)?""		:this.getPAG());
//		result.append("\t");
//		result.append ((this.getVOL()==null)?""		:this.getVOL());
//		result.append("\t");
//		result.append ((this.getDOI()==null)?""		:this.getDOI());
//		result.append("\t");
//		result.append ((this.getPERC_YR()==null)?""	:this.getPERC_YR().toString());
//		result.append("\t");
//		result.append ((this.getPERC_ALL()==null)?"":this.getPERC_ALL().toString());
		result.append("\n");
		
		// TODO Auto-generated method stub
		return result.toString();
	}
	

	
	
}
