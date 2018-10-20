package main.cre.data.type;

/**
 * Export data structure for Cited References
 * @author Andreas
 *
 */
public class CitedReference {

	public int ID;
	public String CR;
	public int RPY;
	public int N_CR;
	
	public String AU;
	public String AU_L;
	public String AU_F;
	public String AU_A;
	
	public String TI;
	public String J;
	public String J_N;
	public String J_S;
	
	public String VOL;
	public String PAG;
	public String DOI;
	
	public double PERC_YR;
	public double PERC_ALL;
	public int N_PYEARS;
	public double PERC_PYEARS;
	
	public int N_TOP50;
	public int N_TOP25;
	public int N_TOP10;

	public int N_TOP50_Plus;
	public int N_TOP25_Plus;
	public int N_TOP10_Plus;
	
	public String SEQUENCE;
	public String TYPE;
	
	public String CID2;
	public int CID_S;
	
	private CitedReference () {
	}
	
	public static CitedReference createFromCRType (CRType cr) {
		
		CitedReference result = new CitedReference();
		
		result.ID = cr.getID();
		result.CR = cr.getCR();
		result.RPY = (cr.getRPY() == null) ? 0 : cr.getRPY().intValue();
		result.N_CR = cr.getN_CR();
		
		result.AU = cr.getAU();
		result.AU_L = cr.getAU_L();
		result.AU_F = cr.getAU_F();
		result.AU_A = cr.getAU_A();
		
		result.TI = cr.getTI();
		result.J = cr.getJ();
		result.J_N = cr.getJ_N();
		result.J_S = cr.getJ_S();
		
		result.VOL = cr.getVOL();
		result.PAG = cr.getPAG();
		result.DOI = cr.getDOI();
		
		result.PERC_YR = 100d*cr.getPERC_YR().doubleValue();
		result.PERC_ALL = 100d*cr.getPERC_ALL().doubleValue();
		result.N_PYEARS = cr.getN_PYEARS();
		result.PERC_PYEARS = 100d*cr.getPYEAR_PERC();
		
		result.N_TOP50 = cr.getN_PCT50();
		result.N_TOP25 = cr.getN_PCT75();
		result.N_TOP10 = cr.getN_PCT90();

		result.N_TOP50_Plus = cr.getN_PCT_AboveAverage50();
		result.N_TOP25_Plus = cr.getN_PCT_AboveAverage75();
		result.N_TOP10_Plus = cr.getN_PCT_AboveAverage90();
		
		result.SEQUENCE = cr.getSEQUENCE();
		result.TYPE = cr.getTYPE();
		
		result.CID2 = cr.getCID2().toString();
		result.CID_S = cr.getCID_S();
		
		return result;
	}
	
}
