package main.cre.data.type.mm;

import java.util.IntSummaryStatistics;

import main.cre.data.type.abs.Statistics;

public class Statistics_MM implements Statistics {

	/** TODO: Namen der Methoden vereinheitlichen */
	
	
	public long getNumberOfCRs () {
		return CRTable_MM.get().getCR().count();
	}
	
	public long getNumberOfPubs () {
		return CRTable_MM.get().getPub().count();
	}

	public long getNumberOfPubs (boolean includePubsWithoutCRs) {
		return CRTable_MM.get().getPub(includePubsWithoutCRs).count();
	}
	


	public int[] getMaxRangePY () {
		IntSummaryStatistics stats = CRTable_MM.get().getPub().filter (pub -> pub.getPY() != null).mapToInt(it -> it.getPY()).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	public int getNumberOfDistinctPY () {
		return (int)CRTable_MM.get().getPub().filter (pub -> pub.getPY() != null).mapToInt(pub -> pub.getPY()).distinct().count();
	}

	
	
	/**
	 * 
	 * @return [min, max]
	 */
	public int[] getMaxRangeNCR () {
		IntSummaryStatistics stats = CRTable_MM.get().getCR().map(it -> it.getN_CR()).mapToInt(Integer::intValue).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	/**
	 * 
	 * @return [min, max]
	 */
	public int[] getMaxRangeRPY () {
		return getMaxRangeRPY(false);
	}

	public int[] getMaxRangeRPY (boolean visibleOnly) {
		IntSummaryStatistics stats = CRTable_MM.get().getCR().filter (cr -> (cr.getRPY()!= null) && (!visibleOnly || cr.getVI())).mapToInt(it -> it.getRPY()).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	public int getNumberOfDistinctRPY () {
		return (int)CRTable_MM.get().getCR().filter (it -> it.getRPY() != null).mapToInt(it -> it.getRPY()).distinct().count();
	}
	

	
	
	public int getNumberOfCRsByVisibility (boolean visible) {
		return (int) CRTable_MM.get().getCR().filter(cr -> (cr.getVI() == visible)).count(); 
	}
	
	
	/**
	 * Count / Remove public statications based on NCR range (from <= N_CR <= to)
	 * @param from
	 * @param to
	 */
	
	public long getNumberOfCRsByNCR (int[] range) {
		return CRTable_MM.get().getCR().filter(cr -> ((range[0] <= cr.getN_CR()) && (cr.getN_CR() <= range[1]))).count();
	}

	/**
	 * Count / Remove publiations based on PERC_YR
	 * @param comp Comparator (<, <=, =, >=, or >)
	 * @param threshold
	 * @return
	 */
	
	public long getNumberOfCRsByPercentYear (String comp, double threshold) {
		switch (comp) {
			case "<" : return CRTable_MM.get().getCR().filter( cr -> cr.getPERC_YR() <  threshold ).count(); 
			case "<=": return CRTable_MM.get().getCR().filter( cr -> cr.getPERC_YR() <= threshold ).count();
			case "=" : return CRTable_MM.get().getCR().filter( cr -> cr.getPERC_YR() == threshold ).count();
			case ">=": return CRTable_MM.get().getCR().filter( cr -> cr.getPERC_YR() >= threshold ).count();
			case ">" : return CRTable_MM.get().getCR().filter( cr -> cr.getPERC_YR() >  threshold ).count();
		}
		return 0;
	}

	/**
	 * Count / Remove publications based on year range (from <= x <= to)	
	 * Property "removed" is set for cascading deletions (e.g., Citing Publications) 
	 * @param from
	 * @param to
	 */
	
	public long getNumberOfCRsByRPY (int[] range) {
		return CRTable_MM.get().getCR().filter( cr -> ((cr.getRPY()!=null) && (range[0] <= cr.getRPY()) && (cr.getRPY() <= range[1]))).count();
	}

	

	public long getNumberOfPubsByCitingYear (int[] range) {
		return CRTable_MM.get().getPub().filter( pub -> ((pub.getPY()!=null) && (range[0] <= pub.getPY()) && (pub.getPY() <= range[1]))).count();
	}

	public int getNumberOfCRsWithoutRPY () {
		return (int) CRTable_MM.get().getCR().filter(it -> it.getRPY() == null).count();  
	}




	
}
