package main.cre.data;

import java.util.IntSummaryStatistics;

import main.cre.data.type.abs.CRTable;
import main.cre.data.type.mm.CRType_MM;
import main.cre.data.type.mm.clustering.CRMatch2;

public class Statistics {

	/** TODO: Namen der Methoden vereinheitlichen */
	
	
	public static long getNumberOfCRs () {
		return CRTable.get().getCR().count();
	}
	
	public static long getNumberOfPubs () {
		return CRTable.get().getPub().count();
	}

	public static long getNumberOfPubs (boolean includePubsWithoutCRs) {
		return CRTable.get().getPub(includePubsWithoutCRs).count();
	}
	
	public static long getNumberOfMatches (boolean manual) {
		return CRTable.get().getNumberOfMatches(manual);
	}

	public static int[] getMaxRangePY () {
		IntSummaryStatistics stats = CRTable.get().getPub().filter (pub -> pub.getPY() != null).mapToInt(it -> it.getPY()).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	public static int getNumberOfDistinctPY () {
		return (int)CRTable.get().getPub().filter (pub -> pub.getPY() != null).mapToInt(pub -> pub.getPY()).distinct().count();
	}

	
	
	/**
	 * 
	 * @return [min, max]
	 */
	public static int[] getMaxRangeNCR () {
		IntSummaryStatistics stats = CRTable.get().getCR().map(it -> it.getN_CR()).mapToInt(Integer::intValue).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	/**
	 * 
	 * @return [min, max]
	 */
	public static int[] getMaxRangeRPY () {
		return getMaxRangeRPY(false);
	}

	public static int[] getMaxRangeRPY (boolean visibleOnly) {
		IntSummaryStatistics stats = CRTable.get().getCR().filter (cr -> (cr.getRPY()!= null) && (!visibleOnly || cr.getVI())).mapToInt(it -> it.getRPY()).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	public static int getNumberOfDistinctRPY () {
		return (int)CRTable.get().getCR().filter (it -> it.getRPY() != null).mapToInt(it -> it.getRPY()).distinct().count();
	}
	

	
	public static long getNumberOfClusters() {
		return CRTable.get().getNumberOfClusters();
	}

	
	public static int getNumberOfCRsByVisibility (boolean visible) {
		return (int) CRTable.get().getCR().filter(cr -> (cr.getVI() == visible)).count(); 
	}
	
	
	/**
	 * Count / Remove public statications based on NCR range (from <= N_CR <= to)
	 * @param from
	 * @param to
	 */
	
	public static long getNumberOfCRsByNCR (int[] range) {
		return CRTable.get().getCR().filter(cr -> ((range[0] <= cr.getN_CR()) && (cr.getN_CR() <= range[1]))).count();
	}

	/**
	 * Count / Remove publiations based on PERC_YR
	 * @param comp Comparator (<, <=, =, >=, or >)
	 * @param threshold
	 * @return
	 */
	
	public static long getNumberOfCRsByPercentYear (String comp, double threshold) {
		switch (comp) {
			case "<" : return CRTable.get().getCR().filter( cr -> cr.getPERC_YR() <  threshold ).count(); 
			case "<=": return CRTable.get().getCR().filter( cr -> cr.getPERC_YR() <= threshold ).count();
			case "=" : return CRTable.get().getCR().filter( cr -> cr.getPERC_YR() == threshold ).count();
			case ">=": return CRTable.get().getCR().filter( cr -> cr.getPERC_YR() >= threshold ).count();
			case ">" : return CRTable.get().getCR().filter( cr -> cr.getPERC_YR() >  threshold ).count();
		}
		return 0;
	}

	/**
	 * Count / Remove publications based on year range (from <= x <= to)	
	 * Property "removed" is set for cascading deletions (e.g., Citing Publications) 
	 * @param from
	 * @param to
	 */
	
	public static long getNumberOfCRsByRPY (int[] range) {
		return CRTable.get().getCR().filter( cr -> ((cr.getRPY()!=null) && (range[0] <= cr.getRPY()) && (cr.getRPY() <= range[1]))).count();
	}

	

	public static long getNumberOfPubsByCitingYear (int[] range) {
		return CRTable.get().getPub().filter( pub -> ((pub.getPY()!=null) && (range[0] <= pub.getPY()) && (pub.getPY() <= range[1]))).count();
	}

	public static int getNumberOfCRsWithoutRPY () {
		return (int) CRTable.get().getCR().filter(it -> it.getRPY() == null).count();  
	}




	
}
