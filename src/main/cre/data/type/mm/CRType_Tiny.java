package main.cre.data.type.mm;

import java.util.EnumMap;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import main.cre.data.match.CRCluster;

public class CRType_Tiny extends CRType_MM {

	private Integer ID;
	
	private Integer N_CR;
	private Integer RPY;

	private CRCluster CID2;
	
	private Boolean VI;	// visible
	private Integer CO;	// background color
	private Integer SEARCH_SCORE;
	
	
	private Double PERC_YR;
	private Double PERC_ALL;
	
	private Integer N_PYEARS;	
	private Double PYEAR_PERC;
	private EnumMap<PERCENTAGE, Integer> N_PCT;
	private EnumMap<PERCENTAGE, Integer> N_PCT_AboveAverage;

	private Integer N_PYEARS2;	
	
	private String SEQUENCE;
	private String TYPE;
	


	@Override
	public boolean equals(Object obj) {
		return this.getID() == ((CRType_Tiny)obj).getID();
	}
	

	@Override
	public int hashCode() {
		return this.getID();
	}



	public CRType_Tiny() {
		super();
		
		ID = null;
//		CR = new String();
//		AU = new String();
//		AU_F = new String();
//		AU_L = new String();
//		AU_A = new String();
//		TI = new String();
//		J = new String();
//		J_N = new String();
//		J_S = new String();
		N_CR = new Integer(0);
		RPY = null;
//x		isNullRPY = true;
//		PAG = new String();
//		VOL = new String();
//		DOI = new String();
//		CID2 = null;
//x		CID_S = new Integer();
		PERC_YR = 0d;
		PERC_ALL = 0d;
		
		N_PYEARS = 0;
		PYEAR_PERC = 0d;
		
		N_PCT = new EnumMap<>(PERCENTAGE.class);
		for (PERCENTAGE perc: PERCENTAGE.values()) {
			N_PCT.put(perc, new Integer(0));
		}
		
		N_PCT_AboveAverage = new EnumMap<>(PERCENTAGE.class);
		for (PERCENTAGE perc: PERCENTAGE.values()) {
			N_PCT_AboveAverage.put(perc, new Integer(0));
		}
		
		N_PYEARS2 = 0;
//		
		VI = new Boolean(true);
		CO = new Integer(0);
		SEARCH_SCORE = new Integer(0);
		
		SEQUENCE = new String();
		TYPE = new String();
	}
	
	



	
	public int getID() {
		return ID;
	}
	public SimpleIntegerProperty getIDProp() {
		return new SimpleIntegerProperty(ID);
	}
	public void setID(int iD) {
		ID=iD;
	}
	
	
	
	public int getN_CR() {
		if (N_CR == null) {
			N_CR = getNumberOfPubs();
		}
		return N_CR;
	}
	
	public SimpleIntegerProperty getN_CRProp() {
		return new SimpleIntegerProperty (getN_CR());
	}


	@Override
	public void resetN_CR() {
		N_CR = null;
	}
	@Override
	public void setN_CR(int n_CR) {
		N_CR = n_CR;
	}	

	
	public Integer getRPY() {
		return RPY;
//		return isNullRPY ? null : RPY.get();
	}
	public SimpleObjectProperty<Integer> getRPYProp() {
		return new SimpleObjectProperty<Integer>(RPY);
	}
	public void setRPY(Integer rPY) {
		RPY = rPY;
//		if (rPY != null) { 
//			isNullRPY = false;
//			RPY.set(rPY);
//		} else {
//			isNullRPY = true;
//			RPY.set(0);
//		}
	}
	
	
	
	public CRCluster getCID2() {
		return CID2;
	}
	public void setCID2(CRCluster cID2) {
		CID2 = cID2;
	}
	
	
	public int getCID_S() {
		return CID2.getCID_SProp().get();
	}
	public SimpleIntegerProperty getCID_SProp() {
		return CID2.getCID_SProp();
	}
//	public void setCID_S(int cID_S) {
//		CID_S.set(cID_S);
//	}
	
	
	public boolean getVI() {
		return VI;
	}
	public SimpleBooleanProperty getVIProp() {
		return new SimpleBooleanProperty(VI);
	}
	public void setVI(boolean vI) {
		VI = vI;
	}
	
	
	public int getCO() {
		return CO;
	}
	public SimpleIntegerProperty getCOProp() {
		return new SimpleIntegerProperty(CO);
	}
	public void setCO(int cO) {
		CO = cO;
	}
	
	
	public Double getPERC_YR() {
		return PERC_YR;
	}
	public SimpleDoubleProperty getPERC_YRProp() {
		return new SimpleDoubleProperty(PERC_YR);
	}
	public void setPERC_YR(Double pERC_YR) {
		PERC_YR = pERC_YR;
	}
	
	
	public Double getPERC_ALL() {
		return PERC_ALL;
	}
	public SimpleDoubleProperty getPERC_ALLProp() {
		return new SimpleDoubleProperty(PERC_ALL);
	}
	public void setPERC_ALL(Double pERC_ALL) {
		PERC_ALL = pERC_ALL;
	}
	
		
	public int getN_PYEARS() {
		return N_PYEARS;
	}
	public SimpleIntegerProperty getN_PYEARSProp() {
		return new SimpleIntegerProperty(N_PYEARS);
	}
	public void setN_PYEARS(int n_PYEARS) {
		N_PYEARS = n_PYEARS;
	}
	
	
	
	public Double getPYEAR_PERC() {
		return PYEAR_PERC;
	}
	public SimpleDoubleProperty getPYEAR_PERCProp() {
		return new SimpleDoubleProperty(PYEAR_PERC);
	}
	public void setPYEAR_PERC(Double pYEAR_PERC) {
		PYEAR_PERC = pYEAR_PERC;
	}
	
	
	public int getN_PCT(PERCENTAGE perc) {
		return N_PCT.get(perc);
	}
	
	public SimpleIntegerProperty getN_PCTProp(PERCENTAGE perc) {
		 return new SimpleIntegerProperty(getN_PCT(perc));
	}
	
	public void setN_PCT(PERCENTAGE perc, int n) {
		N_PCT.put(perc, n);
	}
	
	
	
	public int getN_PCT_AboveAverage(PERCENTAGE perc) {
		return N_PCT_AboveAverage.get(perc);
	}
	
	public SimpleIntegerProperty getN_PCT_AboveAverageProp(PERCENTAGE perc) {
		 return new SimpleIntegerProperty(getN_PCT_AboveAverage(perc));
	}
	
	public void setN_PCT_AboveAverage(PERCENTAGE perc, int n) {
		N_PCT_AboveAverage.put(perc, n);
	}	
	
	
	public int getN_PYEARS2() {
		return N_PYEARS2;
	}
	public SimpleIntegerProperty getN_PYEARS2Prop() {
		return new SimpleIntegerProperty(N_PYEARS2);
	}
	public void setN_PYEARS2(int n_PYEARS2) {
		N_PYEARS2 = n_PYEARS2;
	}
			

	public int getSEARCH_SCORE() {
		return SEARCH_SCORE;
	}
	public SimpleIntegerProperty getSEARCH_SCOREProp() {
		return new SimpleIntegerProperty(SEARCH_SCORE);
	}
	public void setSEARCH_SCORE(Double sEARCH_SCORE) {
		SEARCH_SCORE = sEARCH_SCORE > 0 ? 1 : 0;
	}
	
	
	
	public String getSEQUENCE() {
		return SEQUENCE;
	}
	public SimpleStringProperty getSEQUENCEProp() {
		return new SimpleStringProperty(SEQUENCE);
	}
	public void setSEQUENCE(String sEQUENCE) {
		SEQUENCE = new String(sEQUENCE);
	}
	
	public String getTYPE() {
		return TYPE;
	}
	public SimpleStringProperty getTYPEProp() {
		return new SimpleStringProperty(TYPE);
	}
	public void setTYPE(String tYPE) {
		TYPE = new String(tYPE);
	}






	@Override
	public String getCR() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public SimpleStringProperty getCRProp() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public void setCR(String cR) {
		// TODO Auto-generated method stub
		
	}






	@Override
	public String getAU() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public SimpleStringProperty getAUProp() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public void setAU(String aU) {
		// TODO Auto-generated method stub
		
	}






	@Override
	public String getAU_F() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public SimpleStringProperty getAU_FProp() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public void setAU_F(String aU_F) {
		// TODO Auto-generated method stub
		
	}






	@Override
	public String getAU_L() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public SimpleStringProperty getAU_LProp() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public void setAU_L(String aU_L) {
		// TODO Auto-generated method stub
		
	}






	@Override
	public String getAU_A() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public SimpleStringProperty getAU_AProp() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public void setAU_A(String aU_A) {
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
	public String getJ() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public SimpleStringProperty getJProp() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public void setJ(String j) {
		// TODO Auto-generated method stub
		
	}






	@Override
	public String getJ_N() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public SimpleStringProperty getJ_NProp() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public void setJ_N(String j_N) {
		// TODO Auto-generated method stub
		
	}






	@Override
	public String getJ_S() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public SimpleStringProperty getJ_SProp() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public void setJ_S(String j_S) {
		// TODO Auto-generated method stub
		
	}






	@Override
	public String getPAG() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public SimpleStringProperty getPAGProp() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public void setPAG(String pAG) {
		// TODO Auto-generated method stub
		
	}






	@Override
	public String getVOL() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public SimpleStringProperty getVOLProp() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public void setVOL(String vOL) {
		// TODO Auto-generated method stub
		
	}






	@Override
	public String getDOI() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public SimpleStringProperty getDOIProp() {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public void setDOI(String dOI) {
		// TODO Auto-generated method stub
		
	}	
    





	
}



