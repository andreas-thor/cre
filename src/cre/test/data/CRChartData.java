package cre.test.data;

import java.util.Arrays;
import java.util.stream.IntStream;

public class CRChartData {

	
	public static enum SERIESTYPE { 
		NCR, 	// Sum of Cited References
		MEDIANDIFF, 	// Difference to Median
		CNT		// Count the number of CRs 
	};

	
	private int[] rangeRPY;
	private int[][] series;	// #years x 4; 4 elements = RPY, NCR, MedianDiff, number of CR
	
	public CRChartData(int fromRPY, int toRPY) {
		
		this.rangeRPY = IntStream.rangeClosed(fromRPY, toRPY).toArray();

		this.series = new int[SERIESTYPE.values().length][];
		for (SERIESTYPE type: SERIESTYPE.values()) {
			this.series[type.ordinal()] = new int[this.rangeRPY.length];
		}
	}

	public void addSeries (SERIESTYPE type, int[] data) {
		// we add a copy of the series data and make sure it has the same length as the RPY range
		this.series[type.ordinal()] = Arrays.copyOf(data, this.rangeRPY.length);
	}
	
	
	public int[] getRPY () {
		return this.rangeRPY;
	}

	public int getRPYValue (int index) {
		return this.rangeRPY[index];
	}
	
	public int[] getSeries (SERIESTYPE type) {
		return this.series[type.ordinal()];
	}

	public int getSeriesValue (SERIESTYPE type, int index) {
		return this.series[type.ordinal()][index];
	}
	
	
	
}
