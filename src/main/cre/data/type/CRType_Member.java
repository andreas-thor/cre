package main.cre.data.type;

import java.util.EnumMap;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import main.cre.data.match.CRCluster;
import main.cre.data.type.CRType.PERCENTAGE;

public class CRType_Member extends CRType {

	/**
	 * !!! Probleme durch unterschiedliche Init-Werte, z.B. bei AU_L
	 * Parser (z.B. bei WoS) pr�ft auf "==NULL", hier aber Init-Wert = ""
	 * Daher erstmal weiter CRTYpe_Prop verwenden! 
	 * --> jetzt passt es, oder?
	 */
	
	
	private Integer ID;
	private String CR;
	private String AU;
	private String AU_F; 
	private String AU_L;
	private String AU_A;	// all Authors
	private String TI; 		// title
	private String J;
	private String J_N;
	private String J_S;
	
	private Integer N_CR;
	private Integer RPY;
//	private boolean isNullRPY;
	
	private String PAG;
	private String VOL;
	private String DOI;
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
	






	public CRType_Member() {
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
	
	
	public String getCR() {
		return CR;
	}
	public SimpleStringProperty getCRProp() {
		return new SimpleStringProperty(CR);
	}
	public void setCR(String cR) {
		CR = new String(cR);
	}
	
	
	public String getAU() {
		return AU;
	}
	public SimpleStringProperty getAUProp() {
		return new SimpleStringProperty(AU);
	}
	public void setAU(String aU) {
		AU = new String(aU);
	}
	
	
	public String getAU_F() {
		return AU_F;
	}
	public SimpleStringProperty getAU_FProp() {
		return new SimpleStringProperty(AU_F);
	}
	public void setAU_F(String aU_F) {
		AU_F = new String(aU_F);
	}
	
	
	public String getAU_L() {
		return AU_L;
	}
	public SimpleStringProperty getAU_LProp() {
		return new SimpleStringProperty(AU_L);
	}
	public void setAU_L(String aU_L) {
		AU_L = new String(aU_L);
	}
	
	
	public String getAU_A() {
		return AU_A;
	}
	public SimpleStringProperty getAU_AProp() {
		return new SimpleStringProperty(AU_A);
	}
	public void setAU_A(String aU_A) {
		AU_A = new String(aU_A);
	}
	
	
	public String getTI() {
		return TI;
	}
	public SimpleStringProperty getTIProp() {
		return new SimpleStringProperty(TI);
	}
	public void setTI(String tI) {
		TI = new String(tI);
	}
	
	
	public String getJ() {
		return J;
	}
	public SimpleStringProperty getJProp() {
		return new SimpleStringProperty(J);
	}
	public void setJ(String j) {
		J = new String(j);
	}
	
	
	public String getJ_N() {
		return J_N;
	}
	public SimpleStringProperty getJ_NProp() {
		return new SimpleStringProperty(J_N);
	}
	public void setJ_N(String j_N) {
		J_N = new String(j_N);
	}
	
	
	public String getJ_S() {
		return J_S;
	}
	public SimpleStringProperty getJ_SProp() {
		return new SimpleStringProperty(J_S);
	}
	public void setJ_S(String j_S) {
		J_S = new String(j_S);
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
	
	
	public String getPAG() {
		return PAG;
	}
	public SimpleStringProperty getPAGProp() {
		return new SimpleStringProperty(PAG);
	}
	public void setPAG(String pAG) {
		PAG = new String(pAG);
	}
	
	
	public String getVOL() {
		return VOL;
	}
	public SimpleStringProperty getVOLProp() {
		return new SimpleStringProperty (VOL);
	}
	public void setVOL(String vOL) {
		VOL = new String(vOL);
	}
	
	
	public String getDOI() {
		return DOI;
	}
	public SimpleStringProperty getDOIProp() {
		return new SimpleStringProperty(DOI);
	}
	public void setDOI(String dOI) {
		DOI = new String(dOI);
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
    





	
}



