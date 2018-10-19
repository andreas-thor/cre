package main.cre.data.type;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import main.cre.data.match.CRCluster;

public abstract class CRType implements Comparable<CRType>  {

	
	public static enum FORMATTYPE { 
		WOS, 	
		SCOPUS 
	};
	
	public static enum PERCENTAGE {
		
		P50(0.5d), P75(0.75d), P90(0.9d);
		
		public final double threshold;
		
		PERCENTAGE (double v) {
			this.threshold = v;
		}
		
	};
	
	private FORMATTYPE type = null;	
	private boolean flag;
	
	private Set<PubType> pubList;
	
	public CRType() {
		pubList = new HashSet<PubType>();
	}
	
	public static CRType create() {
		return new CRType_Member();
	}
	
	
	
	
	public static int mein() {
		return 3;
	}
	
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	public FORMATTYPE getType() {
		return type;
	}

	public void setType(FORMATTYPE type) {
		this.type = type;
	}

	public Stream<PubType> getPub() {
		return pubList.stream();
	}

	public int getNumberOfPubs() {
		return pubList.size();
	}

	
	public Stream<PubType> getPub(int py) {
		return pubList.stream().filter(pub -> (pub.getPY()!=null) && (pub.getPY().equals(py)));
	}
	
	
	/**
	 * Adds a PUB to the CR
	 * @param pub to be added
	 * @param inverse true, if this CR should also be added to the PUB
	 */
	public void addPub(PubType pub, boolean inverse) {
		if (inverse) {
			pub.addCR(this, false);
		}
		this.resetN_CR();	// invalidate N_CR --> updated on next get access
		this.pubList.add(pub);
	}


	/**
	 * Removes a PUB from the CR
	 * @param pub to be removed
	 * @param inverse true, if this CR should also be remove from the PUB
	 * @return if the PUB was in the publist
	 */
	public boolean removePub(PubType pub, boolean inverse) {
		if (inverse) {
			pub.removeCR(this, false);
		}
		this.resetN_CR();	// invalidate N_CR --> updated on next get access
		return this.pubList.remove(pub);
	}
	
	
	public void removeAllPubs(boolean inverse) {
		if (inverse) {
			pubList.forEach(pub -> pub.removeCR(this, false));
		}
		this.resetN_CR();
		pubList.clear();
		
	}
	
	
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




	
}



