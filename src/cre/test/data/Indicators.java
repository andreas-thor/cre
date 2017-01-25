package cre.test.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.Frequency;

import javafx.util.Pair;

public class Indicators {

	
	public class CR_PY_Cit implements Comparable<CR_PY_Cit> {

		private int cr;
		private int py;
		private int cit;
		
		public CR_PY_Cit(int cr, int py, int cit) {
			super();
			this.cr = cr;
			this.py = py;
			this.cit = cit;
		}
		
		
		@Override
		public int compareTo(CR_PY_Cit o) {
			if (this.cr < o.cr) return -1;
			if (this.cr > o.cr) return +1;
			return this.cr - o.cr;
		}
		
		@Override
		public boolean equals(Object obj) {
			CR_PY_Cit o = (CR_PY_Cit) obj;
			return (this.cr == o.cr) && (this.py == o.py); 
		}
		
		@Override
		public int hashCode() {
			return cr;
		}
	}
	
	
	public static void computeNPCT (ArrayList<CRType> crData, int maxPY) {
		
		
		// Group CRs by RPY
		Map<Integer, Set<CRType>> mapRPY_CRs = crData.stream().filter(cr -> cr.getRPY()!=null).collect(
				Collectors.groupingBy(CRType::getRPY, Collectors.mapping(Function.identity(), Collectors.toSet()) ));
		for (Entry<Integer, Set<CRType>> group: mapRPY_CRs.entrySet()) {
			int rpy = group.getKey();
			
			int yearSize = maxPY-rpy+1;
			int crSize = group.getValue().size();
			
			int[][] cit = new int[yearSize][crSize];	// matrix [year] x [cr] -> #citations 

			int idx = 0;
			for (CRType cr: group.getValue()) {
				
				// Count #citations per PY
				Map<Integer, Long> mapPY_Count = cr.pubList.stream().filter(pub -> pub.PY != null).collect(
						Collectors.groupingBy(PubType::getPY, Collectors.counting()));
				for (Entry<Integer, Long> pyCount: mapPY_Count.entrySet()) { 
					int py = pyCount.getKey();
					if (py < rpy) continue;
					cit[py-rpy][idx] += pyCount.getValue();
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
