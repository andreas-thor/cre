package cre.test.data;

import java.util.IntSummaryStatistics;

public class CRStats {

	/** TODO: Namen der Methoden vereinheitlichen */
	
	
	public static int getSize () {
		return CRTable.get().crData.size();
	}
	
	public static int getSizePub () {
		return CRTable.get().pubData.size();
	}
	
	public static int getSizeMatch (boolean manual) {
		return CRTable.get().crMatch.match.get(manual).size();
	}

	public static int[] getMaxRangeCitingYear () {
		IntSummaryStatistics stats = CRTable.get().pubData.stream().filter (pub -> pub.PY != null).mapToInt(it -> it.PY).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	public static int getNumberOfDistinctPY () {
//		return (int)CRTable.get().pubData.stream().filter (pub -> pub.PY != null).mapToInt(pub -> pub.PY).distinct().count();
		
		return (int) CRTable.get().crData.stream().map(cr -> cr.pubList).flatMap(pub -> pub.stream()).filter (pub -> pub.PY != null).mapToInt(pub -> pub.PY).distinct().count();
		
	}

	
	
	/**
	 * 
	 * @return [min, max]
	 */
	public static int[] getMaxRangeNCR () {
		IntSummaryStatistics stats = CRTable.get().crData.stream().map((CRType it) -> it.getN_CR()).mapToInt(Integer::intValue).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	/**
	 * 
	 * @return [min, max]
	 */
	public static int[] getMaxRangeYear () {
		IntSummaryStatistics stats = CRTable.get().crData.stream().filter (cr -> cr.getRPY() != null).mapToInt(it -> it.getRPY()).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	public static int getNumberOfDistinctRPY () {
		return (int)CRTable.get().crData.stream().filter (it -> it.getRPY() != null).mapToInt(it -> it.getRPY()).distinct().count();
	}
	

	
	public static int getNoOfClusters() {
		return CRTable.get().crMatch.getNoOfClusters();
	}

	
	public static int getNumberByVisibility (boolean visible) {
		return (int) CRTable.get().crData.stream().filter(cr -> (cr.getVI() == visible)).count(); 
	}
	
	
	/**
	 * Count / Remove public statications based on NCR range (from <= N_CR <= to)
	 * @param from
	 * @param to
	 */
	
	public static long getNumberByNCR (int[] range) {
		return CRTable.get().crData.stream().filter(cr -> ((range[0] <= cr.getN_CR()) && (cr.getN_CR() <= range[1]))).count();
	}

	/**
	 * Count / Remove publiations based on PERC_YR
	 * @param comp Comparator (<, <=, =, >=, or >)
	 * @param threshold
	 * @return
	 */
	
	public static long getNumberByPercentYear (String comp, double threshold) {
		switch (comp) {
			case "<" : return CRTable.get().crData.stream().filter( cr -> cr.getPERC_YR() <  threshold ).count(); 
			case "<=": return CRTable.get().crData.stream().filter( cr -> cr.getPERC_YR() <= threshold ).count();
			case "=" : return CRTable.get().crData.stream().filter( cr -> cr.getPERC_YR() == threshold ).count();
			case ">=": return CRTable.get().crData.stream().filter( cr -> cr.getPERC_YR() >= threshold ).count();
			case ">" : return CRTable.get().crData.stream().filter( cr -> cr.getPERC_YR() >  threshold ).count();
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
		return CRTable.get().crData.stream().filter( cr -> ((cr.getRPY()!=null) && (range[0] <= cr.getRPY()) && (cr.getRPY() <= range[1]))).count();
	}

	public static long getNumberOfPubs () {
		return CRTable.get().pubData.size();
	}

	public static long getNumberOfPubsByCitingYear (int[] range) {
		return CRTable.get().pubData.stream().filter( pub -> ((pub.PY!=null) && (range[0] <= pub.PY) && (pub.PY <= range[1]))).count();
	}

	public static int getNumberWithoutYear () {
		return (int) CRTable.get().crData.stream().filter( (CRType it) -> it.getRPY() == null).count();  
	}




	
}
