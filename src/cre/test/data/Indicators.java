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
			final int crIdx = x;
			int[] NPCT = new int[3];
			int[] type = new int[11];
			char[] sequence = new char[pySize];
			
			
			double zvalue_old = 0;
			for (int pyIdx=0; pyIdx<pySize; pyIdx++) {
				
				for (int b=0; b<3; b++) {
					if (borders[pyIdx][b]<NCR_CR_PY[crIdx][pyIdx]) {
						NPCT[b]++;
					}
				}
				
				double expected = (1.0d*NCR_CR[crIdx]*NCR_PY[pyIdx]/NCR[0]);
				double zvalue = ((expected == 0) /*|| (NCR_CR_PY[crIdx][pyIdx] == 0)*/) ? 0 : (NCR_CR_PY[crIdx][pyIdx] - expected) / Math.sqrt(expected);

//				System.out.println(String.format("CR=%d; PY=%d; Expected=%10.2f; zValue=%10.2f", crIdx, pyIdx, expected, zvalue));
				
				sequence[pyIdx] = (zvalue>1) ? '+' : ((zvalue<-1) ? '-' : '0');
				type[0]  +=                      (zvalue>-1)?1:0;
				type[1]  += ((pyIdx < 3)?1:0) * ((zvalue< 0)?1:0);
				type[2]  += ((pyIdx >=3)?1:0) * ((zvalue> 0)?1:0);
				type[3]  += ((pyIdx < 3)?1:0) * ((zvalue> 1)?1:0);
				type[4]  += ((pyIdx <=3)?1:0) * ((zvalue< 1)?1:0);
				type[5]  += ((pyIdx > 3)?1:0) * ((zvalue> 1)?1:0);
				type[6]  += ((pyIdx > 6)?1:0) * ((zvalue<-1)?1:0);
				type[7]  += (NCR_CR[crIdx]>0)?1:0;
				type[8]  += ((pyIdx==0) || (zvalue>zvalue_old)) ? 1:0;
				type[9]  +=                      (zvalue>1)?1:0;
				type[10] += 1;
				
				zvalue_old = zvalue;

			}
			
			boolean constant  = ((1.0d*type[0]/type[10])>0.8) && ((1.0d*type[7]/type[10])>0.8);
			boolean sbeauty   = (type[1]>2) && (type[2]>2);
			boolean hotpaper  = (type[3]>1);
			boolean lifecycle = (type[4]>1) && (type[5]>1) && (type[6]>1);
			boolean evergreen = ((1.0d*type[8]/type[10])>0.8) && ((1.0d*type[9]/type[10])>0.2);
		
			CRType cr = crList.get(crIdx);
			
			cr.setPYEAR_PERC (((double)cr.getPYEAR_PERC()) / (pySize-noPYWithoutCR));
			cr.setPERC_YR 	 (((double)cr.getN_CR())       / NCR_RPY[rpy-range_RPY[0]]);
			cr.setPERC_ALL	 (((double)cr.getN_CR())       / NCR_ALL[0]);
			
			cr.setN_PCT50(NPCT[0]);
			cr.setN_PCT75(NPCT[1]);
			cr.setN_PCT90(NPCT[2]);
			
			cr.setSEQUENCE(new String (sequence));
			
			if (hotpaper) 				cr.setTYPE("Hot paper");
			if (sbeauty) 				cr.setTYPE("Sleeping beauty");
			if (lifecycle) 				cr.setTYPE("Life cycle");
			if (constant) 				cr.setTYPE("Constant performer");
			
			if (lifecycle && sbeauty) 	cr.setTYPE("Delayed performer / Life cycle");
			if (hotpaper && sbeauty) 	cr.setTYPE("Delayed performer / Hot paper");
			if (hotpaper && lifecycle) 	cr.setTYPE("Hot Paper / Life Cycle");
			if (evergreen) 				cr.setTYPE("Evergreen performer");

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



