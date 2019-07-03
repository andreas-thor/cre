package main.cre.data.type.abs;

public interface Statistics {

	public class IntRange {
		
		public static final int MISSING = -1;
		public static final int NONE = 0;
		
		final private int min;
		final private int max;
		
		public IntRange() {
			this (MISSING, MISSING);
		}

		public IntRange(int value) {
			this (value, value);
		}

		public IntRange(long min, long max) {
			this ((int)min, (int)max);
		}
		
		public IntRange(int min, int max) {
			this.min = min;
			this.max = max;
		}
		
		
		public int getMin() {
			return min;
		}

		public int getMax() {
			return max;
		}
		
		public int getSize() {
			return max-min+1;
		}
	}
	

	
	
	
	
	public long getNumberOfCRs ();
	
	public long getNumberOfPubs ();

	public long getNumberOfPubs (boolean includePubsWithoutCRs);
	
	public IntRange getMaxRangePY ();

	public int getNumberOfDistinctPY ();

	public IntRange getMaxRangeNCR ();

	public IntRange getMaxRangeRPY ();

	public IntRange getMaxRangeRPY (boolean visibleOnly);

	public int getNumberOfDistinctRPY ();
	
	public int getNumberOfCRsByVisibility (boolean visible);
	
	
	/**
	 * Count / Remove public abstractations based on NCR range (from <= N_CR <= to)
	 * @param from
	 * @param to
	 */
	
	public long getNumberOfCRsByNCR (IntRange range);

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
	
	public long getNumberOfCRsByRPY (IntRange range);

	

	public long getNumberOfPubsByCitingYear (IntRange range);

	public int getNumberOfCRsWithoutRPY ();




	
}
