package main.cre.data.type.abs;

import java.util.Arrays;
import java.util.stream.IntStream;

import main.cre.data.type.abs.Statistics.IntRange;


public class CRChartData  {
	
	
	public static enum SERIESTYPE { 
		NCR, 	// Sum of Cited References
		MEDIANDIFF, 	// Difference to Median
		CNT		// Count the number of CRs 
	};

	
	private IntRange rangeRPY;
	private int[][] series;	// #years x 4; 4 elements = RPY, NCR, MedianDiff, number of CR
	private int medianRange;
	
	
	public CRChartData() {
		this.medianRange = 2;
		this.updateChartData(new IntRange(0), new int[] {0}, new int[] {0});
	}

	
	public void updateChartData (IntRange rangeRPY, int[] seriesNCR, int[] seriesCNT) {
		
		this.rangeRPY = rangeRPY;
		
		// compute difference to median
		int[] seriesMEDIANDIFF = new int[seriesNCR.length];	// RPY_idx -> SumNCR - (median of sumPerYear[year-range] ... sumPerYear[year+range])   
		for (int rpyIdx=0; rpyIdx<seriesNCR.length; rpyIdx++) {
			int[] temp = new int[2*medianRange+1];
			for (int m=-medianRange; m<=medianRange; m++) {
				temp[m+medianRange] = (rpyIdx+m<0) || (rpyIdx+m>seriesNCR.length-1) ? 0 : seriesNCR[rpyIdx+m];
			}
			Arrays.sort(temp);
			seriesMEDIANDIFF[rpyIdx] = seriesNCR[rpyIdx] - temp[medianRange];
		}
		
		// we add a copy of the series data and make sure it has the same length as the RPY range
		this.series = new int[SERIESTYPE.values().length][];
		this.series[SERIESTYPE.NCR.ordinal()] = Arrays.copyOf(seriesNCR, getRPYLength());
		this.series[SERIESTYPE.MEDIANDIFF.ordinal()] = Arrays.copyOf(seriesMEDIANDIFF, getRPYLength());
		this.series[SERIESTYPE.CNT.ordinal()] = Arrays.copyOf(seriesCNT, getRPYLength());
	}	
	
	
	
	public int getRPYLength () {
		return this.rangeRPY.getSize();
	}
	
	public int[] getRPY () {
		return IntStream.rangeClosed(this.rangeRPY.getMin(), this.rangeRPY.getMax()).toArray();
	}

	public int getRPYValue (int index) {
		return this.rangeRPY.getMin() + index;
	}
	
	public int[] getSeries (SERIESTYPE type) {
		return this.series[type.ordinal()];
	}

	public int getSeriesValue (SERIESTYPE type, int index) {
		return this.series[type.ordinal()][index];
	}

	public int getMedianRange() {
		return medianRange;
	}

	public void setMedianRange(int medianRange) {
		this.medianRange = medianRange;
		this.updateChartData(this.rangeRPY, getSeries(SERIESTYPE.NCR), getSeries(SERIESTYPE.CNT));
	}
}
