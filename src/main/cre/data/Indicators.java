package main.cre.data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import main.cre.data.CRChartData.SERIESTYPE;
import main.cre.data.type.CRType;
import main.cre.data.type.CRType.PERCENTAGE;


public class Indicators {

	private int[] range_RPY;
	private int[] range_PY;
	private int[] NCR_ALL;		// NCR overall (array length=1; array to make it effectively final)
	private int[] NCR_RPY;		// (sum of) NCR by RPY
	private int[] CNT_RPY;		// number of CRs by RPY
	
	private static Indicators indicators;
	
	private Indicators () {
		this.update();
	}
	
	
	public static Indicators get() {
		if (indicators == null) {
			indicators = new Indicators();
		}
		return indicators;
	}
	

	public void update() {

		System.out.println("Compute Ranges");
		this.range_RPY = Statistics.getMaxRangeRPY();
		this.range_PY  = Statistics.getMaxRangePY();
		this.NCR_ALL = new int[1];
		this.NCR_RPY = new int[this.range_RPY[1]-this.range_RPY[0]+1];
		this.CNT_RPY = new int[this.range_RPY[1]-this.range_RPY[0]+1];
		
		// Group CRs by RPY, compute NCR_ALL and NCR_RPY
		System.out.println("mapRPY_CRs");
		Map<Integer, List<CRType>> map_RPY_CRs = CRTable.get().getCR().filter(cr -> {
			
			this.NCR_ALL[0] += cr.getN_CR();
			if (cr.getRPY()!=null) {
				this.NCR_RPY[cr.getRPY()-this.range_RPY[0]] += cr.getN_CR();
				this.CNT_RPY[cr.getRPY()-this.range_RPY[0]] += 1;
				return true;
			} else {
				return false;
			}
		}).collect(Collectors.groupingBy(CRType::getRPY, Collectors.mapping(Function.identity(), Collectors.toList())));
		
		
		map_RPY_CRs.entrySet().stream().parallel().forEach(rpyGroup -> {	
			this.computeForAllCRsOfTheSameRPY (rpyGroup.getKey(), rpyGroup.getValue());
		});
		
		return;
	}
	
	
	
	private void computeForAllCRsOfTheSameRPY (int rpy, List<CRType> crList) {
		
		int firstPY = (rpy<=this.range_PY[0]) ? this.range_PY[0] : rpy;	// usually: rpy<=range_PY[0] 
		int lastPY = this.range_PY[1];
		if (lastPY < firstPY) return;
		
		int pySize = lastPY-firstPY+1;
		int crSize = crList.size();
		
		int[][] NCR_CR_PY = new int[crSize][pySize];	
		int[] NCR_CR = new int[crSize];	
		int[] NCR_PY = new int[pySize];	
		int[] NCR = new int[1];
		
		
		for (int x=0; x<crSize; x++) {

			final int crIdx = x;
			CRType cr = crList.get(crIdx);
			int[] NPYEARS = new int[1];
			cr.getPub().filter(pub -> pub.getPY() != null).forEach(pub -> {
				
				if ((pub.getPY()>=firstPY) && (pub.getPY()<=lastPY)) {	// PY is out of range
				
					int pyIdx = pub.getPY()-firstPY;
					
					if (NCR_CR_PY[crIdx][pyIdx]==0) {	// we found a citation from a new PY
						NPYEARS[0]++;
					}
					NCR_CR_PY[crIdx][pyIdx]++;
					NCR_CR[crIdx]++;
					NCR_PY[pyIdx]++;
					NCR[0]++;
				}
			});
			
			cr.setN_PYEARS   (NPYEARS[0]);
		}
		
		
		
		int noPYWithoutCR = 0;	// number of PY where we do not have any Publication citing a CR in RPY
		for (int pyIdx=0; pyIdx<pySize; pyIdx++) {
			if (NCR_PY[pyIdx]==0) noPYWithoutCR++;
		}
		
//		if (noPYWithoutCR>0) {
//			System.out.println(String.format("RPY=%d, PYSize=%d, w/o=%d", rpy, pySize, noPYWithoutCR));
//		}
		
		
 
		int[][] borders = this.getPercentileBorders(NCR_CR_PY, crSize, pySize);

		
		for (int crIdx=0; crIdx<crSize; crIdx++) {
			
//			System.out.println("CR x=" + x);
//			final int crIdx = x;
			
			int[] NPCT = new int[PERCENTAGE.values().length];
			int[] NPCT_AboveAverage = new int[PERCENTAGE.values().length];
			
			int[] type = new int[11];
			char[] sequence = new char[pySize];
			
			/* just for debugging */
//			double[] expectedArray = new double[pySize];
//			double[] zvalueArray  = new double[pySize];
			
			
			for (int pyIdx=0; pyIdx<pySize; pyIdx++) {
				
				double expected = (1.0d*NCR_CR[crIdx]*NCR_PY[pyIdx]/NCR[0]);
				double zvalue = (expected == 0) ? 0 : (NCR_CR_PY[crIdx][pyIdx] - expected) / Math.sqrt(expected);

				
				/* just for debugging */
//				expectedArray[pyIdx] = expected;
//				zvalueArray[pyIdx] = zvalue;
//				System.out.println(String.format("CR=%d\tPY=%d\tExpected=%10.2f\tzValue=%10.2f", crIdx, pyIdx, expected, zvalue));
				
				sequence[pyIdx] = (zvalue>1) ? '+' : ((zvalue<-1) ? '-' : '0');
				
				type[0]  +=                      (zvalue<-1)?0:1;	// # at least average
				type[1]  += ((pyIdx< 3) 		&& (zvalue<-1)) ? 1 : 0;	// # below average in the first 3 py
				type[2]  += ((pyIdx>=3) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the 4th+ py 
				type[3]  += ((pyIdx< 3) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the first 3 py 
				type[4]  += ((pyIdx< 4) 		&& (zvalue<=1)) ? 1 : 0;	// # average or lower in the first 4 py
				type[5]  += ((pyIdx>=4) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the 5th+ py 
				type[6]  += ((pySize-pyIdx<=3) 	&& (zvalue<=1)) ? 1: 0;		// # average or lower in the last 3 py
				type[7]  += (NCR_CR_PY[crIdx][pyIdx]>0) ? 1 : 0;			// # no of citing years with at least 1 citation
				type[8]  += ((pyIdx==0) || (sequence[pyIdx-1]=='-') ||  (sequence[pyIdx]=='+') || ((sequence[pyIdx-1]=='0') && (sequence[pyIdx]=='0'))) ? 1:0;
				type[9]  +=                      (zvalue>1)?1:0;			// above average
				type[10] += 1;	// # citing years
				
				
				for (int b=0; b<PERCENTAGE.values().length; b++) {
					if (borders[pyIdx][b]<NCR_CR_PY[crIdx][pyIdx]) {
						NPCT[b]++;
						if (zvalue>1) {
							NPCT_AboveAverage[b]++;
						}
					}
				}

			}
			
			// Sleeping Beauty = Publication which has been cited below average in the first three citing years ("-"; z<-1) at least twice and above average ("+"; z>1) in the following citing years at least once
			boolean sbeauty   = (type[1]>=2) && (type[2]>=1);
			
			// Constant Performer = Publication which has been cited in more than 80% of the citing years at least once. In more than 80% of the citing years it has been cited at least on the average level 
			boolean constant  = ((1.0d*type[0]/type[10])>0.8) && ((1.0d*type[7]/type[10])>0.8);
			
			// Hot Paper = Publication which has been cited above average ("+"; z>1) in the first three years after publication at least twice
			boolean hotpaper  = (type[3]>=2);
			
			// Life cycle = Publication which has been cited in the first four years in at least two years on the average level ("0"; -1<=z<=1) or lower ("-"; z<-1), in at least two years of the following years above average ("+"; z>1), and in the last three years on the average level ("0"; -1<=z<=1) or lower ("-"; z<-1)
			boolean lifecycle = (type[4]>=2) && (type[5]>=2) && (type[6]>1);
			
		
			
			
			
			
			CRType cr = crList.get(crIdx);
			
			cr.setPYEAR_PERC (((double)cr.getN_PYEARS()) / (pySize-noPYWithoutCR));
			cr.setPERC_YR 	 (((double)cr.getN_CR())       / NCR_RPY[rpy-range_RPY[0]]);
			cr.setPERC_ALL	 (((double)cr.getN_CR())       / NCR_ALL[0]);
			
			cr.setN_PCT50(NPCT[PERCENTAGE.P50.ordinal()]);
			cr.setN_PCT75(NPCT[PERCENTAGE.P75.ordinal()]);
			cr.setN_PCT90(NPCT[PERCENTAGE.P90.ordinal()]);
			cr.setN_PCT99(NPCT[PERCENTAGE.P99.ordinal()]);
			cr.setN_PCT999(NPCT[PERCENTAGE.P999.ordinal()]);

			cr.setN_PCT_AboveAverage50(NPCT_AboveAverage[PERCENTAGE.P50.ordinal()]);
			cr.setN_PCT_AboveAverage75(NPCT_AboveAverage[PERCENTAGE.P75.ordinal()]);
			cr.setN_PCT_AboveAverage90(NPCT_AboveAverage[PERCENTAGE.P90.ordinal()]);
			cr.setN_PCT_AboveAverage99(NPCT_AboveAverage[PERCENTAGE.P99.ordinal()]);
			cr.setN_PCT_AboveAverage999(NPCT_AboveAverage[PERCENTAGE.P999.ordinal()]);
			
			cr.setSEQUENCE(new String (sequence));

			StringBuffer typeLabel = new StringBuffer();
			if (sbeauty) 	typeLabel.append (typeLabel.length()>0?" + ":"").append("Sleeping beauty");
			if (constant) 	typeLabel.append (typeLabel.length()>0?" + ":"").append("Constant performer");
			if (hotpaper) 	typeLabel.append (typeLabel.length()>0?" + ":"").append("Hot paper");
			if (lifecycle) 	typeLabel.append (typeLabel.length()>0?" + ":"").append("Life cycle");
			cr.setTYPE(typeLabel.toString());
			
			/*
			if (lifecycle && sbeauty) 	cr.setTYPE("Sleeping beauty / Life cycle");	// Delayed performer
			if (hotpaper && sbeauty) 	cr.setTYPE("Sleeping beauty / Hot paper");	// Delayed performer
			if (hotpaper && lifecycle) 	cr.setTYPE("Hot Paper / Life Cycle");
			if (evergreen) 				cr.setTYPE("Evergreen performer");
			 */
			
			/*
			if ((cr.getID()==2797) || (cr.getID()==51687)) {
				System.out.println("\n-----------------\n" + cr.getID());
				System.out.println("expectedArray");
				System.out.println(Arrays.toString(expectedArray));
				System.out.println("zvalueArray");
				System.out.println(Arrays.toString(zvalueArray));
				System.out.println("sequence");
				System.out.println(Arrays.toString(sequence));
				System.out.println("NCR_PY");
				System.out.println(Arrays.toString(NCR_PY));
				System.out.println("NCR_CR[crIdx]");
				System.out.println(NCR_CR[crIdx]);
				System.out.println("NCR_CR_PY[crIdx]");
				System.out.println(Arrays.toString(NCR_CR_PY[crIdx]));
				System.out.println("cr.getN_CR()");
				System.out.println(cr.getN_CR());
				System.out.println("NCR[0]");
				System.out.println(NCR[0]);
			}
			*/
			
		}
		
		

		
	}
	
	
	
	private int[][] getPercentileBorders (int[][] NCR_CR_PY, int crSize, int pySize) {

		int rangeSize_NPCT = CRTable.get().getNpctRange();
		
		
		int[][] borders = new int[pySize][];	// borders (50%, 75%, 90%, 99%, 99.9%) for each PY
		for (int pyIdx=0; pyIdx<pySize; pyIdx++) {
			
			int rangeStart = (pyIdx-rangeSize_NPCT>=0) ? pyIdx-rangeSize_NPCT : 0;
			int rangeEnd = (pyIdx+rangeSize_NPCT<pySize) ? pyIdx+rangeSize_NPCT : pySize-1;
			
			int[] temp = new int[(rangeEnd-rangeStart+1)*crSize];
			
			for (int rIdx=0; rIdx<rangeEnd-rangeStart+1; rIdx++) {
				for (int crIdx=0; crIdx<crSize; crIdx++) {
					temp[rIdx*crSize + crIdx] = NCR_CR_PY[crIdx][rIdx+rangeStart];
				}
			}
				
			Arrays.sort(temp);
			borders[pyIdx] = new int[PERCENTAGE.values().length];
			for (PERCENTAGE perc: PERCENTAGE.values()) {
				borders[pyIdx][perc.ordinal()] = temp[Math.max(0, (int) Math.floor(perc.threshold * temp.length)-1)];
			}
		}
		
		return borders;
		
	}
	
	
	public void updateChartData () {
		
		int medianRange = CRChartData.get().getMedianRange();
		
		// compute difference to median
		int[] RPY_MedianDiff = new int[this.NCR_RPY.length];	// RPY_idx -> SumNCR - (median of sumPerYear[year-range] ... sumPerYear[year+range])   
		for (int rpyIdx=0; rpyIdx<this.NCR_RPY.length; rpyIdx++) {
			int[] temp = new int[2*medianRange+1];
			for (int m=-medianRange; m<=medianRange; m++) {
				temp[m+medianRange] = (rpyIdx+m<0) || (rpyIdx+m>this.NCR_RPY.length-1) ? 0 : this.NCR_RPY[rpyIdx+m];
			}
			Arrays.sort(temp);
			RPY_MedianDiff[rpyIdx] = this.NCR_RPY[rpyIdx] - temp[medianRange];
		}
		
		
		CRChartData.get().init(this.range_RPY[0], this.range_RPY[1]);
		CRChartData.get().addSeries(SERIESTYPE.NCR, this.NCR_RPY);
		CRChartData.get().addSeries(SERIESTYPE.MEDIANDIFF, RPY_MedianDiff);
		CRChartData.get().addSeries(SERIESTYPE.CNT, this.CNT_RPY);
	}
	
	
	
}



