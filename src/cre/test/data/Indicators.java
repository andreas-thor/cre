package cre.test.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Indicators {


	
	public static void computeNPCT (ArrayList<CRType> crData, int minPY, int maxPY) {
		
		
		// Group CRs by RPY
		Map<Integer, Set<CRType>> mapRPY_CRs = crData.stream().filter(cr -> cr.getRPY()!=null).collect(
				Collectors.groupingBy(CRType::getRPY, Collectors.mapping(Function.identity(), Collectors.toSet()) ));
		
		for (Entry<Integer, Set<CRType>> group: mapRPY_CRs.entrySet()) {
			int rpy = group.getKey();
			int firstPY = Math.max(rpy, minPY); 
			int yearSize = maxPY-firstPY+1;
			int crSize = group.getValue().size();
			
			if (yearSize<=0) continue;	// in the error case that RPY is higher than PY
			
			int[][] cit = new int[yearSize][crSize];	// matrix [year] x [cr] -> #citations 

			int idx = 0;
			for (CRType cr: group.getValue()) {
				
				// Count #citations per PY
				Map<Integer, Long> mapPY_Count = cr.pubList.stream().filter(pub -> pub.PY != null).collect(
						Collectors.groupingBy(PubType::getPY, Collectors.counting()));
				for (Entry<Integer, Long> pyCount: mapPY_Count.entrySet()) { 
					int py = pyCount.getKey();
					if (py < rpy) continue;
					cit[py-firstPY][idx] += pyCount.getValue();
				}				
				idx++;
			}
			
			// flatten 2dimensional array to 1dimensional
			int[] citSort = new int[ yearSize * crSize ];
			idx = 0;
			for (int x=0; x<yearSize; x++) {
				for (int y=0; y<crSize; y++) {
					citSort[idx] = cit[x][y];
					idx++;
				}
			}
			Arrays.sort(citSort);
			
			int[] percBorder = new int[] { 
					citSort[Math.max(0, (int) Math.floor(0.50d * yearSize * crSize)-1)], 
					citSort[Math.max(0, (int) Math.floor(0.75d * yearSize * crSize)-1)],
					citSort[Math.max(0, (int) Math.floor(0.90d * yearSize * crSize)-1)]};
			
			for (CRType cr: group.getValue()) {
				
				cr.setN_PCT50(0);
				cr.setN_PCT75(0);
				cr.setN_PCT90(0);
				
				// Count #citations per PY
				Map<Integer, Long> mapPY_Count = cr.pubList.stream().filter(pub -> pub.PY != null).collect(
						Collectors.groupingBy(PubType::getPY, Collectors.counting()));

				for (Entry<Integer, Long> pyCount: mapPY_Count.entrySet()) { 
					int py = pyCount.getKey();
					if (py < rpy) continue;
					int count = pyCount.getValue().intValue();
					if (percBorder[0]<count) cr.setN_PCT50(cr.getN_PCT50()+1);
					if (percBorder[1]<count) cr.setN_PCT75(cr.getN_PCT75()+1);
					if (percBorder[2]<count) cr.setN_PCT90(cr.getN_PCT90()+1);
				}	
			}
			
		}
		
		
		
		
		
		
	
		
		
	}
	
	
	
}
