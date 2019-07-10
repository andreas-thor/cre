package main.cre.data.type.extern;

import java.util.Comparator;

import main.cre.data.type.abs.CRType;
import main.cre.data.type.abs.CRType.PERCENTAGE;

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
	public int N_TOP1;
	public int N_TOP0_1;

	public int N_TOP50_Plus;
	public int N_TOP25_Plus;
	public int N_TOP10_Plus;
	public int N_TOP1_Plus;
	public int N_TOP0_1_Plus;
	
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
		
		result.N_TOP50 = cr.getN_PCT(PERCENTAGE.P50);
		result.N_TOP25 = cr.getN_PCT(PERCENTAGE.P75);
		result.N_TOP10 = cr.getN_PCT(PERCENTAGE.P90);
		result.N_TOP1 = cr.getN_PCT(PERCENTAGE.P99);
		result.N_TOP0_1 = cr.getN_PCT(PERCENTAGE.P999);

		result.N_TOP50_Plus = cr.getN_PCT_AboveAverage(PERCENTAGE.P50);
		result.N_TOP25_Plus = cr.getN_PCT_AboveAverage(PERCENTAGE.P75);
		result.N_TOP10_Plus = cr.getN_PCT_AboveAverage(PERCENTAGE.P90);
		result.N_TOP1_Plus = cr.getN_PCT_AboveAverage(PERCENTAGE.P99);
		result.N_TOP0_1_Plus = cr.getN_PCT_AboveAverage(PERCENTAGE.P999);
		
		result.SEQUENCE = cr.getSEQUENCE();
		result.TYPE = cr.getTYPE();
		
		result.CID2 = cr.getClusterId();
		result.CID_S = cr.getClusterSize();
		
		return result;
	}
	
	
	public static Comparator<CitedReference> getComparatorByProperty (String prop) {
		
		switch (prop) {

		case "ID":
			return Comparator.comparing((CitedReference cr) -> cr.ID);
		case "CR":
			return Comparator.comparing((CitedReference cr) -> cr.CR);
		case "RPY":
			return Comparator.comparing((CitedReference cr) -> cr.RPY);
		case "N_CR":
			return Comparator.comparing((CitedReference cr) -> cr.N_CR);
		case "AU":
			return Comparator.comparing((CitedReference cr) -> cr.AU);
		case "AU_L":
			return Comparator.comparing((CitedReference cr) -> cr.AU_L);
		case "AU_F":
			return Comparator.comparing((CitedReference cr) -> cr.AU_F);
		case "AU_A":
			return Comparator.comparing((CitedReference cr) -> cr.AU_A);
		case "TI":
			return Comparator.comparing((CitedReference cr) -> cr.TI);
		case "J":
			return Comparator.comparing((CitedReference cr) -> cr.J);
		case "J_N":
			return Comparator.comparing((CitedReference cr) -> cr.J_N);
		case "J_S":
			return Comparator.comparing((CitedReference cr) -> cr.J_S);
		case "VOL":
			return Comparator.comparing((CitedReference cr) -> cr.VOL);
		case "PAG":
			return Comparator.comparing((CitedReference cr) -> cr.PAG);
		case "DOI":
			return Comparator.comparing((CitedReference cr) -> cr.DOI);
		case "PERC_YR":
			return Comparator.comparing((CitedReference cr) -> cr.PERC_YR);
		case "PERC_ALL":
			return Comparator.comparing((CitedReference cr) -> cr.PERC_ALL);
		case "N_PYEARS":
			return Comparator.comparing((CitedReference cr) -> cr.N_PYEARS);
		case "PERC_PYEARS":
			return Comparator.comparing((CitedReference cr) -> cr.PERC_PYEARS);
		case "N_TOP50":
			return Comparator.comparing((CitedReference cr) -> cr.N_TOP50);
		case "N_TOP25":
			return Comparator.comparing((CitedReference cr) -> cr.N_TOP25);
		case "N_TOP10":
			return Comparator.comparing((CitedReference cr) -> cr.N_TOP10);
		case "N_TOP1":
			return Comparator.comparing((CitedReference cr) -> cr.N_TOP1);
		case "N_TOP0_1":
			return Comparator.comparing((CitedReference cr) -> cr.N_TOP0_1);
		case "N_TOP50_Plus":
			return Comparator.comparing((CitedReference cr) -> cr.N_TOP50_Plus);
		case "N_TOP25_Plus":
			return Comparator.comparing((CitedReference cr) -> cr.N_TOP25_Plus);
		case "N_TOP10_Plus":
			return Comparator.comparing((CitedReference cr) -> cr.N_TOP10_Plus);
		case "N_TOP1_Plus":
			return Comparator.comparing((CitedReference cr) -> cr.N_TOP1_Plus);
		case "N_TOP0_1_Plus":
			return Comparator.comparing((CitedReference cr) -> cr.N_TOP0_1_Plus);
		case "SEQUENCE":
			return Comparator.comparing((CitedReference cr) -> cr.SEQUENCE);
		case "TYPE":
			return Comparator.comparing((CitedReference cr) -> cr.TYPE);
		case "CID2":
			return Comparator.comparing((CitedReference cr) -> cr.CID2);
		case "CID_S":
			return Comparator.comparing((CitedReference cr) -> cr.CID_S);
		default:
			return null;
		}
	}
	
}
