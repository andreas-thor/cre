package cre.test.data;

import java.util.IntSummaryStatistics;

public class CRStats {

	/** TODO: Namen der Methoden vereinheitlichen */
	
	private CRTable crTab;
	
	public CRStats(CRTable crTab) {
		this.crTab = crTab; 
	}
	
	public int getSize () {
		return crTab.crData.size();
	}
	
	public int getSizePub () {
		return crTab.pubData.size();
	}
	
	public int getSizeMatch (boolean manual) {
		return crTab.crMatch.match.get(manual).size();
	}

	public int[] getMaxRangeCitingYear () {
		IntSummaryStatistics stats = crTab.pubData.stream().filter (pub -> pub.PY != null).mapToInt(it -> it.PY).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	public int getNumberOfDistinctPY () {
		return (int)crTab.pubData.stream().filter (pub -> pub.PY != null).mapToInt(pub -> pub.PY).distinct().count();
	}

	
	
	/**
	 * 
	 * @return [min, max]
	 */
	public int[] getMaxRangeNCR () {
		IntSummaryStatistics stats = crTab.crData.stream().map((CRType it) -> it.getN_CR()).mapToInt(Integer::intValue).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	/**
	 * 
	 * @return [min, max]
	 */
	public int[] getMaxRangeYear () {
		IntSummaryStatistics stats = crTab.crData.stream().filter (cr -> cr.getRPY() != null).mapToInt(it -> it.getRPY()).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	public int getNumberOfDistinctRPY () {
		return (int)crTab.crData.stream().filter (it -> it.getRPY() != null).mapToInt(it -> it.getRPY()).distinct().count();
	}
	

	
	public int getNoOfClusters() {
		return this.crTab.crMatch.getNoOfClusters();
	}

	
	public int getNumberByVisibility (boolean visible) {
		return (int) crTab.crData.stream().filter(cr -> (cr.getVI() == visible)).count(); 
	}
	
	
	/**
	 * Count / Remove publications based on NCR range (from <= N_CR <= to)
	 * @param from
	 * @param to
	 */
	
	public long getNumberByNCR (int[] range) {
		return crTab.crData.stream().filter(cr -> ((range[0] <= cr.getN_CR()) && (cr.getN_CR() <= range[1]))).count();
	}

	/**
	 * Count / Remove publiations based on PERC_YR
	 * @param comp Comparator (<, <=, =, >=, or >)
	 * @param threshold
	 * @return
	 */
	
	public long getNumberByPercentYear (String comp, double threshold) {
		switch (comp) {
			case "<" : return crTab.crData.stream().filter( cr -> cr.getPERC_YR() <  threshold ).count(); 
			case "<=": return crTab.crData.stream().filter( cr -> cr.getPERC_YR() <= threshold ).count();
			case "=" : return crTab.crData.stream().filter( cr -> cr.getPERC_YR() == threshold ).count();
			case ">=": return crTab.crData.stream().filter( cr -> cr.getPERC_YR() >= threshold ).count();
			case ">" : return crTab.crData.stream().filter( cr -> cr.getPERC_YR() >  threshold ).count();
		}
		return 0;
	}

	/**
	 * Count / Remove publications based on year range (from <= x <= to)	
	 * Property "removed" is set for cascading deletions (e.g., Citing Publications) 
	 * @param from
	 * @param to
	 */
	
	public long getNumberByYear (int[] range) {
		return crTab.crData.stream().filter( cr -> ((cr.getRPY()!=null) && (range[0] <= cr.getRPY()) && (cr.getRPY() <= range[1]))).count();
	}

	public long getNumberOfPubs () {
		return crTab.pubData.size();
	}

	public long getNumberOfPubsByCitingYear (int[] range) {
		return crTab.pubData.stream().filter( pub -> ((pub.PY!=null) && (range[0] <= pub.PY) && (pub.PY <= range[1]))).count();
	}

	public int getNumberWithoutYear () {
		return (int) crTab.crData.stream().filter( (CRType it) -> it.getRPY() == null).count();  
	}




	
}
