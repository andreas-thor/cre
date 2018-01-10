package cre.test.data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import cre.test.data.type.CRType;


public class Indicators {

	private static int[] range_RPY;
	private static int[] range_PY;
	private static int[] NCR_ALL;			// NCR overall (array length=1; array to make it effectively final)
	private static int[] NCR_RPY;		// NCR by RPY
	
	

	public static int[][] update() {

		System.out.println("Compute Ranges");
		range_RPY = CRStats.getMaxRangeYear();
		range_PY  = CRStats.getMaxRangeCitingYear();
		NCR_ALL = new int[1];
		NCR_RPY = new int[range_RPY[1]-range_RPY[0]+1];
		
		// Group CRs by RPY, compute NCR_ALL and NCR_RPY
		System.out.println("mapRPY_CRs");
		Map<Integer, List<CRType>> map_RPY_CRs = CRTable.get().getCR().filter(cr -> {
			
			NCR_ALL[0] += cr.getN_CR();
			if (cr.getRPY()!=null) {
				NCR_RPY[cr.getRPY()-range_RPY[0]] += cr.getN_CR();
				return true;
			} else {
				return false;
			}
		}).collect(Collectors.groupingBy(CRType::getRPY, Collectors.mapping(Function.identity(), Collectors.toList())));
		
		
		map_RPY_CRs.entrySet().stream().parallel().forEach(rpyGroup -> {	
			computeForAllCRsOfTheSameRPY (rpyGroup.getKey(), rpyGroup.getValue());
		});
		
		return getChartData(UserSettings.get().getMedianRange());
	}
	
	
	
	private static void computeForAllCRsOfTheSameRPY (int rpy, List<CRType> crList) {
		
		int firstPY = (rpy<=range_PY[0]) ? range_PY[0] : rpy;	// usually: rpy<=range_PY[0] 
		int lastPY = range_PY[1];
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
		if (noPYWithoutCR>0) {
			System.out.println(String.format("RPY=%d, PYSize=%d, w/o=%d", rpy, pySize, noPYWithoutCR));
		}
		
		
 
		int rangeSize_NPCT = UserSettings.get().getNPCTRange();
		int[][] borders = new int[pySize][];	// borders (50%, 75%, 90%) for each PY
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
			borders[pyIdx] = new int[] { 
					temp[Math.max(0, (int) Math.floor(0.50d * temp.length)-1)], 
					temp[Math.max(0, (int) Math.floor(0.75d * temp.length)-1)],
					temp[Math.max(0, (int) Math.floor(0.90d * temp.length)-1)]};
		}
		
		
		for (int x=0; x<crSize; x++) {
			
//			System.out.println("CR x=" + x);
			
			final int crIdx = x;
			int[] NPCT = new int[3];
			int[] type = new int[11];
			char[] sequence = new char[pySize];
			
			
			for (int pyIdx=0; pyIdx<pySize; pyIdx++) {
				
				for (int b=0; b<3; b++) {
					if (borders[pyIdx][b]<NCR_CR_PY[crIdx][pyIdx]) {
						NPCT[b]++;
					}
				}
				
				double expected = (1.0d*NCR_CR[crIdx]*NCR_PY[pyIdx]/NCR[0]);
				double zvalue = (expected == 0) ? 0 : (NCR_CR_PY[crIdx][pyIdx] - expected) / Math.sqrt(expected);

//				System.out.println(String.format("CR=%d\tPY=%d\tExpected=%10.2f\tzValue=%10.2f", crIdx, pyIdx, expected, zvalue));
				
				sequence[pyIdx] = (zvalue>1) ? '+' : ((zvalue<-1) ? '-' : '0');
				
				type[0]  +=                      (zvalue<-1)?0:1;	// # at least average
				type[1]  += ((pyIdx< 3) 		&& (zvalue<-1)) ? 1 : 0;	// # below average in the first 3 py
				type[2]  += ((pyIdx>=3) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the 4th+ py 
				type[3]  += ((pyIdx< 3) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the first 3 py 
				type[4]  += ((pyIdx< 4) 		&& (zvalue<=1)) ? 1 : 0;	// # average or lower in the first 4 py
				type[5]  += ((pyIdx>=4) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the 5th+ py 
				type[6]  += ((pySize-pyIdx<=3) 	&& (zvalue<=1)) ? 1: 0;		// # average or lower in the last 3 py
				
				
				type[7]  += (NCR_CR[crIdx]>0) ? 1 : 0;					// # no of citing years with at least 1 citation
				type[8]  += ((pyIdx==0) || (sequence[pyIdx-1]=='-') ||  (sequence[pyIdx]=='+') || ((sequence[pyIdx-1]=='0') && (sequence[pyIdx]=='0'))) ? 1:0;
				type[9]  +=                      (zvalue>1)?1:0;
				type[10] += 1;	// # citing years
				

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
			
			cr.setN_PCT50(NPCT[0]);
			cr.setN_PCT75(NPCT[1]);
			cr.setN_PCT90(NPCT[2]);
			
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
		}
		
		

		
	}
	
	
	
	private static int[][] getChartData (int medianRange) {
		
		
		// compute difference to median
		int[] RPY_MedianDiff = new int[NCR_RPY.length];	// RPY_idx -> SumNCR - (median of sumPerYear[year-range] ... sumPerYear[year+range])   
		for (int rpyIdx=0; rpyIdx<NCR_RPY.length; rpyIdx++) {
			int[] temp = new int[2*medianRange+1];
			for (int m=-medianRange; m<=medianRange; m++) {
				temp[m+medianRange] = (rpyIdx+m<0) || (rpyIdx+m>NCR_RPY.length-1) ? 0 : NCR_RPY[rpyIdx+m];
			}
			Arrays.sort(temp);
			RPY_MedianDiff[rpyIdx] = NCR_RPY[rpyIdx] - temp[medianRange];
		}
		
		// generate data rows for chart
		return new int[][] {
			IntStream.rangeClosed(range_RPY[0], range_RPY[1]).toArray(),
			NCR_RPY, 
			RPY_MedianDiff 
		};		
	}
	
	
	
}



