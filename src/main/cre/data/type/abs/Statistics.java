package main.cre.data.type.abs;

public interface Statistics {

	public long getNumberOfCRs ();
	
	public long getNumberOfPubs ();

	public long getNumberOfPubs (boolean includePubsWithoutCRs);
	
	public int[] getMaxRangePY ();

	public int getNumberOfDistinctPY ();

	public int[] getMaxRangeNCR ();

	public int[] getMaxRangeRPY ();

	public int[] getMaxRangeRPY (boolean visibleOnly);

	public int getNumberOfDistinctRPY ();
	
	public int getNumberOfCRsByVisibility (boolean visible);
	
	
	/**
	 * Count / Remove public abstractations based on NCR range (from <= N_CR <= to)
	 * @param from
	 * @param to
	 */
	
	public long getNumberOfCRsByNCR (int[] range);

	/**
	 * Count / Remove publiations based on PERC_YR
	 * @param comp Comparator (<, <=, =, >=, or >)
	 * @param threshold
	 * @return
	 */
	
	public long getNumberOfCRsByPercentYear (String comp, double threshold);

	/**
	 * Count / Remove publications based on year range (from <= x <= to)	
	 * Property "removed" is set for cascading deletions (e.g., Citing Publications) 
	 * @param from
	 * @param to
	 */
	
	public long getNumberOfCRsByRPY (int[] range);

	

	public long getNumberOfPubsByCitingYear (int[] range);

	public int getNumberOfCRsWithoutRPY ();




	
}
