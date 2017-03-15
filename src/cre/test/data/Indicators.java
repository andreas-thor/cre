package cre.test.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import cre.test.data.type.CRType;
import cre.test.data.type.PubType;
import javafx.util.Pair;


public class Indicators {


	private static int[] getPercBorders (Set<CRType> crSet, int firstPY, int lastPY) {
		
		int yearSize = lastPY-firstPY+1;
		int crSize = crSet.size();
		
		int[][] cit = new int[yearSize][crSize];	// matrix [year] x [cr] -> #citations 

		int idx = 0;
		for (CRType cr: crSet) {
			
			// Count #citations per PY
			Map<Integer, Long> mapPY_Count = cr.getPub().filter(pub -> pub.PY != null).collect(
					Collectors.groupingBy(PubType::getPY, Collectors.counting()));
			
			for (Entry<Integer, Long> pyCount: mapPY_Count.entrySet()) { 
				int current = pyCount.getKey();
				if ((current < firstPY) || (current > lastPY)) continue;
				cit[current-firstPY][idx] += pyCount.getValue();
			}				
			idx++;
		}
		
		
//		System.out.println("\ncit[]=" + cit);
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
		
		return new int[] { 
				citSort[Math.max(0, (int) Math.floor(0.50d * yearSize * crSize)-1)], 
				citSort[Math.max(0, (int) Math.floor(0.75d * yearSize * crSize)-1)],
				citSort[Math.max(0, (int) Math.floor(0.90d * yearSize * crSize)-1)]};
		
//		System.out.println("\nborders=" + percBorder);			
	}
	
	
	public static void computeNPCT (Set<CRType> crData, int minPY, int maxPY, int range) {
		
		
		// Group CRs by RPY
		Map<Integer, Set<CRType>> mapRPY_CRs = crData.stream().filter(cr -> cr.getRPY()!=null).collect(
				Collectors.groupingBy(CRType::getRPY, Collectors.mapping(Function.identity(), Collectors.toSet()) ));
		
		for (Entry<Integer, Set<CRType>> group: mapRPY_CRs.entrySet()) {
			
			// inititalize
			for (CRType cr: group.getValue()) {
				cr.setN_PCT50(0);
				cr.setN_PCT75(0);
				cr.setN_PCT90(0);
			}
			
			int rpy = group.getKey();
//			System.out.println("\n\nRPY=" + rpy);
//			System.out.println("\n==============");
			
			Map<Pair<Integer, Integer>, int[]> cachePercBorder = new HashMap<Pair<Integer, Integer>, int[]>();
			
			for (int i=Math.max(rpy, minPY); i<=maxPY; i++) {

				final int py = i;
				
				
				int firstPY = Math.max(rpy,  Math.max(py-range, minPY)); 
				int lastPY  = Math.min(py+range, maxPY);
				if (lastPY < firstPY) continue;	// in the error case that RPY is higher than PY

//				System.out.println("\nPY=" + py);
//				System.out.println("\nrange=["+ firstPY + "," + lastPY + "]");
//				System.out.println("\n-----------");

				int[] percBorder = cachePercBorder.get(new Pair<Integer, Integer> (firstPY, lastPY)); 
				if (percBorder == null) {
					percBorder = getPercBorders(group.getValue(), firstPY, lastPY);
					cachePercBorder.put(new Pair<Integer, Integer> (firstPY, lastPY), percBorder);
				}

//				System.out.println("\n" + percBorder[0] + "," + percBorder[1] + "," + percBorder[2]);
				
				
				for (CRType cr: group.getValue()) {
					long count = cr.getPub().filter(pub -> (pub.PY != null) && (pub.PY.intValue() == py)).count();
					if (percBorder[0]<count) cr.setN_PCT50(cr.getN_PCT50()+1);
					if (percBorder[1]<count) cr.setN_PCT75(cr.getN_PCT75()+1);
					if (percBorder[2]<count) cr.setN_PCT90(cr.getN_PCT90()+1);
//					System.out.println("\nCR-ID=" + cr.getID() + "; PCT50=" + cr.getN_PCT50());
				}
			}
			
			
			
		}
		
		
		
		
		
		
	
		
		
	}
	
	
	
}
