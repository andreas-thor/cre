package main.cre.data.type.abs;

import java.util.Arrays;
import java.util.stream.IntStream;


public class CRChartData  {
	
	
	public static enum SERIESTYPE { 
		NCR, 	// Sum of Cited References
		MEDIANDIFF, 	// Difference to Median
		CNT		// Count the number of CRs 
	};

	
	private int[] rangeRPY;
	private int[][] series;	// #years x 4; 4 elements = RPY, NCR, MedianDiff, number of CR
	private int medianRange;
	
	
	public CRChartData() {
		this.medianRange = 2;
		this.init(0, 0);
	}

	
	public void updateChartData (int fromRPY, int toRPY, int[] seriesNCR, int[] seriesCNT) {
		
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
		
		
		init(fromRPY, toRPY);
		addSeries(SERIESTYPE.NCR, seriesNCR);
		addSeries(SERIESTYPE.MEDIANDIFF, seriesMEDIANDIFF);
		addSeries(SERIESTYPE.CNT, seriesCNT);
	}	
	
	
	private void init (int fromRPY, int toRPY) {
		this.rangeRPY = IntStream.rangeClosed(fromRPY, toRPY).toArray();
	
		// all series initialized with 0
		this.series = new int[SERIESTYPE.values().length][];
		for (SERIESTYPE type: SERIESTYPE.values()) {
			this.series[type.ordinal()] = new int[getRPYLength()];
		}
	}
	
	private void addSeries (SERIESTYPE type, int[] data) {
		// we add a copy of the series data and make sure it has the same length as the RPY range
		this.series[type.ordinal()] = Arrays.copyOf(data, getRPYLength());
	}
	
	
	public int getRPYLength () {
		return this.rangeRPY.length;
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

	
	public int getMedianRange() {
		return medianRange;
	}

	public void setMedianRange(int medianRange) {
		this.medianRange = medianRange;
		this.updateChartData(this.rangeRPY[0], this.rangeRPY[this.rangeRPY.length-1], getSeries(SERIESTYPE.NCR), getSeries(SERIESTYPE.CNT));
	}
}
