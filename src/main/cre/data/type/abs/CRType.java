package main.cre.data.type.abs;

import java.util.EnumMap;
import java.util.stream.Stream;

public abstract class CRType<P extends PubType<?>> implements Comparable<CRType<P>> {

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
	

	private Integer RPY;
//	private boolean isNullRPY;
	
	private String PAG;
	private String VOL;
	private String DOI;
	
	
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
	
	private FORMATTYPE type = null;	
	private boolean flag;
	

	
	
	public CRType () {
		
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

		VI = new Boolean(true);
		CO = new Integer(0);
		SEARCH_SCORE = new Integer(0);
		
		SEQUENCE = new String();
		TYPE = new String();
	}
	
	
	public abstract Stream<P> getPub();
	
	
	public abstract int getN_CR();
	
	
	
//	public abstract CRCluster getCID2();
	
	
	
//	public abstract void setCID2(String s);
//	public abstract void setCID2(CRType cr);
//	public abstract void setCID2(CRType cr, int c1);

//	public abstract void setCID2(CRType<P> cr);
//	public abstract void setCID2(String cID2);

	public String getClusterId() {
		return getClusterC1() + "/" + getClusterC2();
	}

	public abstract int getClusterC1();
	
	public abstract int getClusterC2();
	
	public abstract int getClusterSize();
	
	
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID=iD;
	}
	
	
	public String getCR() {
		return CR;
	}
	public void setCR(String cR) {
		CR = cR==null? null : new String(cR);
	}
	
	
	public String getAU() {
		return AU;
	}
	public void setAU(String aU) {
		AU = aU==null? null : new String(aU);
	}
	
	
	public String getAU_F() {
		return AU_F;
	}
	public void setAU_F(String aU_F) {
		AU_F = aU_F==null? null : new String(aU_F);
	}
	
	
	public String getAU_L() {
		return AU_L;
	}
	public void setAU_L(String aU_L) {
		AU_L = aU_L==null? null : new String(aU_L);
	}
	
	
	public String getAU_A() {
		return AU_A;
	}
	public void setAU_A(String aU_A) {
		AU_A = aU_A==null? null : new String(aU_A);
	}
	
	
	public String getTI() {
		return TI;
	}
	public void setTI(String tI) {
		TI = tI==null? null : new String(tI);
	}
	
	
	public String getJ() {
		return J;
	}
	public void setJ(String j) {
		J = j==null? null : new String(j);
	}
	
	
	public String getJ_N() {
		return J_N;
	}
	public void setJ_N(String j_N) {
		J_N = j_N==null? null : new String(j_N);
	}
	
	
	public String getJ_S() {
		return J_S;
	}
	public void setJ_S(String j_S) {
		J_S = j_S==null? null : new String(j_S);
	}

	
	public Integer getRPY() {
		return RPY;
	}
	public void setRPY(Integer rPY) {
		RPY = rPY;
	}
	
	
	public String getPAG() {
		return PAG;
	}
	public void setPAG(String pAG) {
		PAG = pAG==null? null : new String(pAG);
	}
	
	
	public String getVOL() {
		return VOL;
	}
	public void setVOL(String vOL) {
		VOL = vOL==null? null : new String(vOL);
	}
	
	
	public String getDOI() {
		return DOI;
	}
	public void setDOI(String dOI) {
		DOI = dOI==null? null : new String(dOI);
	}
	

	public boolean getVI() {
		return VI;
	}
	public void setVI(boolean vI) {
		VI = vI;
	}
	
	
	public int getCO() {
		return CO;
	}
	public void setCO(int cO) {
		CO = cO;
	}
	
	
	public Double getPERC_YR() {
		return PERC_YR;
	}
	public void setPERC_YR(Double pERC_YR) {
		PERC_YR = pERC_YR;
	}
	
	
	public Double getPERC_ALL() {
		return PERC_ALL;
	}
	public void setPERC_ALL(Double pERC_ALL) {
		PERC_ALL = pERC_ALL;
	}
	
		
	public int getN_PYEARS() {
		return N_PYEARS;
	}
	public void setN_PYEARS(int n_PYEARS) {
		N_PYEARS = n_PYEARS;
	}
	
	
	public Double getPYEAR_PERC() {
		return PYEAR_PERC;
	}
	public void setPYEAR_PERC(Double pYEAR_PERC) {
		PYEAR_PERC = pYEAR_PERC;
	}
	
	
	public int getN_PYEARS2() {
		return N_PYEARS2;
	}
	public void setN_PYEARS2(int n_PYEARS2) {
		N_PYEARS2 = n_PYEARS2;
	}
		
	
	public int getSEARCH_SCORE() {
		return SEARCH_SCORE;
	}
	public void setSEARCH_SCORE(Double sEARCH_SCORE) {
		SEARCH_SCORE = sEARCH_SCORE > 0 ? 1 : 0;
	}
	
		
	public String getSEQUENCE() {
		return SEQUENCE;
	}
	public void setSEQUENCE(String sEQUENCE) {
		SEQUENCE = new String(sEQUENCE);
	}
		
	
	public String getTYPE() {
		return TYPE;
	}
	public void setTYPE(String tYPE) {
		TYPE = new String(tYPE);
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
	
	
	public int getN_PCT(PERCENTAGE perc) {
		return N_PCT.get(perc);
	}
	public void setN_PCT(PERCENTAGE perc, int n) {
		N_PCT.put(perc, n);
	}
	
	
	public int getN_PCT_AboveAverage(PERCENTAGE perc) {
		return N_PCT_AboveAverage.get(perc);
	}
	public void setN_PCT_AboveAverage(PERCENTAGE perc, int n) {
		N_PCT_AboveAverage.put(perc, n);
	}	
	

	
	
	public void copyNotNULLValues (CRType<?> cr) {
		
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
	public int compareTo(CRType<P> o) {
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
