package cre.test.data;

import java.util.IntSummaryStatistics;

import cre.test.data.match.CRMatch2;
import cre.test.data.type.CRType;

public class CRStats {

	/** TODO: Namen der Methoden vereinheitlichen */
	
	
	public static long getSize () {
		return CRTable.get().getCR().count();
	}
	
	public static long getSizePub () {
		return CRTable.get().getPub().count();
	}
	
	public static long getSizeMatch (boolean manual) {
		return CRMatch2.get().getSize(manual);
	}

	public static int[] getMaxRangeCitingYear () {
		IntSummaryStatistics stats = CRTable.get().getPub().filter (pub -> pub.PY != null).mapToInt(it -> it.PY).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	public static int getNumberOfDistinctPY () {
		return (int)CRTable.get().getPub().filter (pub -> pub.PY != null).mapToInt(pub -> pub.PY).distinct().count();
	}

	
	
	/**
	 * 
	 * @return [min, max]
	 */
	public static int[] getMaxRangeNCR () {
		IntSummaryStatistics stats = CRTable.get().getCR().map((CRType it) -> it.getN_CR()).mapToInt(Integer::intValue).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	/**
	 * 
	 * @return [min, max]
	 */
	public static int[] getMaxRangeYear () {
		return getMaxRangeYear(false);
	}

	public static int[] getMaxRangeYear (boolean visibleOnly) {
		IntSummaryStatistics stats = CRTable.get().getCR().filter (cr -> (cr.getRPY()!= null) && (!visibleOnly || cr.getVI())).mapToInt(it -> it.getRPY()).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	public static int getNumberOfDistinctRPY () {
		return (int)CRTable.get().getCR().filter (it -> it.getRPY() != null).mapToInt(it -> it.getRPY()).distinct().count();
	}
	

	
	public static long getNoOfClusters() {
		return CRTable.get().getCR().map(cr -> cr.getCID2()).distinct().count();
	}

	
	public static int getNumberByVisibility (boolean visible) {
		return (int) CRTable.get().getCR().filter(cr -> (cr.getVI() == visible)).count(); 
	}
	
	
	/**
	 * Count / Remove public statications based on NCR range (from <= N_CR <= to)
	 * @param from
	 * @param to
	 */
	
	public static long getNumberByNCR (int[] range) {
		return CRTable.get().getCR().filter(cr -> ((range[0] <= cr.getN_CR()) && (cr.getN_CR() <= range[1]))).count();
	}

	/**
	 * Count / Remove publiations based on PERC_YR
	 * @param comp Comparator (<, <=, =, >=, or >)
	 * @param threshold
	 * @return
	 */
	
	public static long getNumberByPercentYear (String comp, double threshold) {
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
	
	public static long getNumberByYear (int[] range) {
		return CRTable.get().getCR().filter( cr -> ((cr.getRPY()!=null) && (range[0] <= cr.getRPY()) && (cr.getRPY() <= range[1]))).count();
	}

	public static long getNumberOfPubs () {
		return CRTable.get().getPub().count();
	}

	public static long getNumberOfPubsByCitingYear (int[] range) {
		return CRTable.get().getPub().filter( pub -> ((pub.PY!=null) && (range[0] <= pub.PY) && (pub.PY <= range[1]))).count();
	}

	public static int getNumberWithoutYear () {
		return (int) CRTable.get().getCR().filter( (CRType it) -> it.getRPY() == null).count();  
	}




	
}