package cre.test.data;

import java.util.ArrayList;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class CRType {

	private SimpleIntegerProperty ID;
	private SimpleStringProperty CR;
	private SimpleStringProperty AU;
	private SimpleStringProperty AU_F; 
	private SimpleStringProperty AU_L;
	private SimpleStringProperty AU_A;	// all Authors
	private SimpleStringProperty TI; 		// title
	private SimpleStringProperty J;
	private SimpleStringProperty J_N;
	private SimpleStringProperty J_S;
	
	private SimpleIntegerProperty N_CR;
	private SimpleIntegerProperty RPY;
	private boolean isNullRPY;
	
	private SimpleStringProperty PAG;
	private SimpleStringProperty VOL;
	private SimpleStringProperty DOI;
	private CRCluster CID2;
	private SimpleIntegerProperty CID_S;
	
	private SimpleBooleanProperty VI;	// visible
	private SimpleIntegerProperty CO;	// background color
	
	private SimpleDoubleProperty PERC_YR;
	private SimpleDoubleProperty PERC_ALL;
	
	private SimpleIntegerProperty N_PYEARS;	
	private SimpleDoubleProperty PYEAR_PERC;
	private SimpleIntegerProperty N_PCT50;
	private SimpleIntegerProperty N_PCT75;
	private SimpleIntegerProperty N_PCT90;
	private SimpleIntegerProperty N_PYEARS2;	
	
	
	
	
	public CRType() {
		super();
		
		ID = new SimpleIntegerProperty();
		CR = new SimpleStringProperty();
		AU = new SimpleStringProperty();
		AU_F = new SimpleStringProperty();
		AU_L = new SimpleStringProperty();
		AU_A = new SimpleStringProperty();
		TI = new SimpleStringProperty();
		J = new SimpleStringProperty();
		J_N = new SimpleStringProperty();
		J_S = new SimpleStringProperty();
		N_CR = new SimpleIntegerProperty(1);
		RPY = new SimpleIntegerProperty();
		isNullRPY = true;
		PAG = new SimpleStringProperty();
		VOL = new SimpleStringProperty();
		DOI = new SimpleStringProperty();
		CID2 = null;
		CID_S = new SimpleIntegerProperty();
		PERC_YR = new SimpleDoubleProperty();
		PERC_ALL = new SimpleDoubleProperty();
		
		N_PYEARS = new SimpleIntegerProperty();
		PYEAR_PERC = new SimpleDoubleProperty();
		N_PCT50 = new SimpleIntegerProperty();
		N_PCT75 = new SimpleIntegerProperty();
		N_PCT90 = new SimpleIntegerProperty();
		N_PYEARS2 = new SimpleIntegerProperty();
		
		VI = new SimpleBooleanProperty(true);
		CO = new SimpleIntegerProperty(0);
		
	}
	
	public static byte TYPE_WOS = 1;
	public static byte TYPE_SCOPUS = 2;
	public byte type = 0;	
	

	public ArrayList<PubType> pubList;
	
	public int mergedTo = -1;
	public boolean removed = false;
	

    
    
	public int getID() {
		return ID.get();
	}
	public SimpleIntegerProperty getIDProp() {
		return ID;
	}
	public void setID(int iD) {
		ID.set(iD);
	}
	
	
	public String getCR() {
		return CR.get();
	}
	public SimpleStringProperty getCRProp() {
		return CR;
	}
	public void setCR(String cR) {
		CR.set(cR);
	}
	
	
	public String getAU() {
		return AU.get();
	}
	public SimpleStringProperty getAUProp() {
		return AU;
	}
	public void setAU(String aU) {
		AU.set(aU);
	}
	
	
	public String getAU_F() {
		return AU_F.get();
	}
	public SimpleStringProperty getAU_FProp() {
		return AU_F;
	}
	public void setAU_F(String aU_F) {
		AU_F.set(aU_F);
	}
	
	
	public String getAU_L() {
		return AU_L.get();
	}
	public SimpleStringProperty getAU_LProp() {
		return AU_L;
	}
	public void setAU_L(String aU_L) {
		AU_L.set(aU_L);
	}
	
	
	public String getAU_A() {
		return AU_A.get();
	}
	public SimpleStringProperty getAU_AProp() {
		return AU_A;
	}
	public void setAU_A(String aU_A) {
		AU_A.set(aU_A);
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
	
	
	public String getJ() {
		return J.get();
	}
	public SimpleStringProperty getJProp() {
		return J;
	}
	public void setJ(String j) {
		J.set(j);
	}
	
	
	public String getJ_N() {
		return J_N.get();
	}
	public SimpleStringProperty getJ_NProp() {
		return J_N;
	}
	public void setJ_N(String j_N) {
		J_N.set(j_N);
	}
	
	
	public String getJ_S() {
		return J_S.get();
	}
	public SimpleStringProperty getJ_SProp() {
		return J_S;
	}
	public void setJ_S(String j_S) {
		J_S.set(j_S);
	}
	
	
	public int getN_CR() {
		return N_CR.get();
	}
	public SimpleIntegerProperty getN_CRProp() {
		return N_CR;
	}
	public void setN_CR(int n_CR) {
		N_CR.set(n_CR);;
	}
	
	
	public Integer getRPY() {
		return isNullRPY ? null : RPY.get();
	}
	public SimpleIntegerProperty getRPYProp() {
		return RPY;
	}
	public void setRPY(Integer rPY) {
		if (rPY != null) { 
			isNullRPY = false;
			RPY.set(rPY);
		} else {
			isNullRPY = true;
			RPY.set(0);
		}
	}
	
	
	public String getPAG() {
		return PAG.get();
	}
	public SimpleStringProperty getPAGProp() {
		return PAG;
	}
	public void setPAG(String pAG) {
		PAG.set(pAG);
	}
	
	
	public String getVOL() {
		return VOL.get();
	}
	public SimpleStringProperty getVOLProp() {
		return VOL;
	}
	public void setVOL(String vOL) {
		VOL.set(vOL);
	}
	
	
	public String getDOI() {
		return DOI.get();
	}
	public SimpleStringProperty getDOIProp() {
		return DOI;
	}
	public void setDOI(String dOI) {
		DOI.set(dOI);
	}
	
	
	public CRCluster getCID2() {
		return CID2;
	}
	public void setCID2(CRCluster cID2) {
		CID2 = cID2;
	}
	
	
	public int getCID_S() {
		return CID_S.get();
	}
	public SimpleIntegerProperty getCID_SProp() {
		return CID_S;
	}
	public void setCID_S(int cID_S) {
		CID_S.set(cID_S);
	}
	
	
	public boolean getVI() {
		return VI.get();
	}
	public SimpleBooleanProperty getVIProp() {
		return VI;
	}
	public void setVI(boolean vI) {
		VI.set(vI);
	}
	
	
	public int getCO() {
		return CO.get();
	}
	public SimpleIntegerProperty getCOProp() {
		return CO;
	}
	public void setCO(int cO) {
		CO.set(cO);
	}
	
	
	public Double getPERC_YR() {
		return PERC_YR.getValue();
	}
	public SimpleDoubleProperty getPERC_YRProp() {
		return PERC_YR;
	}
	public void setPERC_YR(Double pERC_YR) {
		PERC_YR.setValue(pERC_YR);
	}
	
	
	public Double getPERC_ALL() {
		return PERC_ALL.getValue();
	}
	public SimpleDoubleProperty getPERC_ALLProp() {
		return PERC_ALL;
	}
	public void setPERC_ALL(Double pERC_ALL) {
		PERC_ALL.setValue(pERC_ALL);
	}
	
		
	public int getN_PYEARS() {
		return N_PYEARS.get();
	}
	public SimpleIntegerProperty getN_PYEARSProp() {
		return N_PYEARS;
	}
	public void setN_PYEARS(int n_PYEARS) {
		N_PYEARS.set(n_PYEARS);
	}
	
	
	
	public Double getPYEAR_PERC() {
		return PYEAR_PERC.getValue();
	}
	public SimpleDoubleProperty getPYEAR_PERCProp() {
		return PYEAR_PERC;
	}
	public void setPYEAR_PERC(Double pYEAR_PERC) {
		PYEAR_PERC.setValue(pYEAR_PERC);
	}
	
	
	
	public int getN_PCT50() {
		return N_PCT50.get();
	}
	public SimpleIntegerProperty getN_PCT50Prop() {
		return N_PCT50;
	}
	public void setN_PCT50(int n_PCT50) {
		N_PCT50.set(n_PCT50);
	}
	
	
	
	public int getN_PCT75() {
		return N_PCT75.get();
	}
	public SimpleIntegerProperty getN_PCT75Prop() {
		return N_PCT75;
	}
	public void setN_PCT75(int n_PCT75) {
		N_PCT75.set(n_PCT75);
	}
	
	
	
	public int getN_PCT90() {
		return N_PCT90.get();
	}
	public SimpleIntegerProperty getN_PCT90Prop() {
		return N_PCT90;
	}
	public void setN_PCT90(int n_PCT90) {
		N_PCT90.set(n_PCT90);
	}
	
	
	
	public int getN_PYEARS2() {
		return N_PYEARS2.get();
	}
	public SimpleIntegerProperty getN_PYEARS2Prop() {
		return N_PYEARS2;
	}
	public void setN_PYEARS2(int n_PYEARS2) {
		N_PYEARS2.set(n_PYEARS2);
	}
			
    
  
	
}



