package main.cre.store.mm;

import java.util.IntSummaryStatistics;

import main.cre.data.type.abs.Statistics;

public class Statistics_MM implements Statistics {

	/** TODO: Namen der Methoden vereinheitlichen */
	
	@Override
	public long getNumberOfCRs () {
		return CRTable_MM.get().getCR().count();
	}
	
	@Override
	public long getNumberOfPubs () {
		return CRTable_MM.get().getPub().count();
	}

	@Override
	public long getNumberOfPubs (boolean includePubsWithoutCRs) {
		return CRTable_MM.get().getPub(includePubsWithoutCRs).count();
	}
	

	@Override
	public IntRange getMaxRangePY () {
		IntSummaryStatistics stats = CRTable_MM.get().getPub().filter (pub -> pub.getPY() != null).mapToInt(it -> it.getPY()).summaryStatistics();
		return (stats.getCount()==0) ? new IntRange() : new IntRange(stats.getMin(), stats.getMax());
	}

	@Override
	public int getNumberOfDistinctPY () {
		return (int)CRTable_MM.get().getPub().filter (pub -> pub.getPY() != null).mapToInt(pub -> pub.getPY()).distinct().count();
	}

	
	
	/**
	 * 
	 * @return [min, max]
	 */
	@Override
	public IntRange getMaxRangeNCR () {
		IntSummaryStatistics stats = CRTable_MM.get().getCR().map(it -> it.getN_CR()).mapToInt(Integer::intValue).summaryStatistics();
		return (stats.getCount()==0) ? new IntRange() : new IntRange(stats.getMin(), stats.getMax());
	}

	/**
	 * 
	 * @return [min, max]
	 */
	@Override
	public IntRange getMaxRangeRPY () {
		return getMaxRangeRPY(false);
	}

	@Override
	public IntRange getMaxRangeRPY (boolean visibleOnly) {
		IntSummaryStatistics stats = CRTable_MM.get().getCR().filter (cr -> (cr.getRPY()!= null) && (!visibleOnly || cr.getVI())).mapToInt(it -> it.getRPY()).summaryStatistics();
		return (stats.getCount()==0) ? new IntRange() : new IntRange(stats.getMin(), stats.getMax());
	}

	@Override
	public int getNumberOfDistinctRPY () {
		return (int)CRTable_MM.get().getCR().filter (it -> it.getRPY() != null).mapToInt(it -> it.getRPY()).distinct().count();
	}
	

	
	@Override
	public int getNumberOfCRsByVisibility (boolean visible) {
		return (int) CRTable_MM.get().getCR().filter(cr -> (cr.getVI() == visible)).count(); 
	}
	
	
	/**
	 * Count / Remove public statications based on NCR range (from <= N_CR <= to)
	 * @param from
	 * @param to
	 */
	@Override
	public long getNumberOfCRsByNCR (IntRange range) {
		return CRTable_MM.get().getCR().filter(cr -> ((range.getMin() <= cr.getN_CR()) && (cr.getN_CR() <= range.getMax()))).count();
	}

	/**
	 * Count / Remove publiations based on PERC_YR
	 * @param comp Comparator (<, <=, =, >=, or >)
	 * @param threshold
	 * @return
	 */
	
	@Override
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
	@Override
	public long getNumberOfCRsByRPY (IntRange range) {
		return CRTable_MM.get().getCR().filter( cr -> ((cr.getRPY()!=null) && (range.getMin() <= cr.getRPY()) && (cr.getRPY() <= range.getMax()))).count();
	}

	
	@Override
	public long getNumberOfPubsByCitingYear (IntRange range) {
		return CRTable_MM.get().getPub().filter( pub -> ((pub.getPY()!=null) && (range.getMin() <= pub.getPY()) && (pub.getPY() <= range.getMax()))).count();
	}

	@Override
	public int getNumberOfCRsWithoutRPY () {
		return (int) CRTable_MM.get().getCR().filter(it -> it.getRPY() == null).count();  
	}




	
}
