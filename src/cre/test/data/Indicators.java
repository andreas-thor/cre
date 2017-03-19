package cre.test.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import cre.test.data.type.CRType;
import cre.test.data.type.PubType;
import javafx.util.Pair;


public class Indicators {


	private static Map<Integer, Set<CRType>> mapRPY_CRs;
	

	
	public static int[][] update() {

		// Group CRs by RPY
		mapRPY_CRs = CRTable.get().getCR().filter(cr -> cr.getRPY()!=null).collect(
				Collectors.groupingBy(CRType::getRPY, Collectors.mapping(Function.identity(), Collectors.toSet()) ));
		
		computeNPCT(CRStats.getMaxRangeCitingYear(), UserSettings.get().getNPCTRange());
		return computePERC(UserSettings.get().getMedianRange());
	}
	
	private static int[][] computePERC (int medianRange) {
		
		// Determine sum citations per year
		int[] rangeYear = CRStats.getMaxRangeYear();
		Map<Integer, Integer> sumPerYear = 
			IntStream.rangeClosed(rangeYear[0], rangeYear[1]).mapToObj(RPY -> new Integer (RPY)).collect(Collectors.toMap(	
				RPY -> RPY, 
				RPY -> (mapRPY_CRs.get(RPY) == null) ? 0 : mapRPY_CRs.get(RPY).stream().mapToInt(cr -> cr.getN_CR()).sum()
			));
		
		// compute PERC_YR and PERC_ALL
		int sum = sumPerYear.values().stream().mapToInt(Integer::intValue).sum(); 
		CRTable.get().getCR().forEach ( it -> {
			it.setPERC_YR( (it.getRPY() == null) ? null : ((double)it.getN_CR())/sumPerYear.get(it.getRPY()));
			it.setPERC_ALL(((double)it.getN_CR())/sum);
		});

		/*
		 * - PYEARS_PERC= Anzahl Jahre, in denen eine Referenzpublikation
		 * zitiert wurde/maximal mögliche Anzahl von Jahren.-> NICHT OK!
		 * Problem: Ich vermute, dass immer das Intervall
		 * "2016-Referenzpublikationsjahr" als Nenner verwendet wurde. Es gibt
		 * aber ein vorgegebenes Zeitintervall von Publikationen, hier von
		 * 2007-2016, die Referenzpublikationen zitieren. Referenzpublikationen
		 * vor 2007 (z.B. Hirsch-Index, 2005) können maximal 10 Jahre
		 * (=2016-2007+1) zitiert werden, Publikationen nach 2007 (z.B.
		 * Radicchi, 2008) können nur x Jahre=(2016-x+1) zitiert werden, z.b.
		 * Radicchi 9 Jahre (2016-2008+1). D.h. Für Referenzpublikationen vor
		 * 2007 ist immer das maximal mögliche Intervall "2016-2007"=10 Jahre
		 * einzusetzen, für die jüngeren Publikationen
		 * "2016-Referenzpublikationsjahr".
		 */
				
		// N_PYEARS = Number of DISTINCT PY (for a CR)
		int[] rangePub = CRStats.getMaxRangeCitingYear();
		CRTable.get().getCR().forEach( cr -> {
			cr.setN_PYEARS((int) cr.getPub().filter(pub -> pub.getPY()!=null).mapToInt(pub -> pub.getPY()).distinct().count());
			cr.setPYEAR_PERC( (cr.getRPY()==null) ? null : ((double)cr.getN_PYEARS()) /  (rangePub[1]-Math.max(rangePub[0], cr.getRPY())+1));
		});		
		
		
		// compute chart data
		final Map<Integer, Integer> NCRperYearMedian = new HashMap<Integer, Integer>();	// year -> median of sumPerYear[year-range] ... sumPerYear[year+range]   
		
		// generate data rows for chart
		sumPerYear.forEach ((y, crs) -> {
			int median =  IntStream.rangeClosed(-medianRange, +medianRange)
				.map( it -> { return (sumPerYear.get(y+it)==null) ? 0 : sumPerYear.get(y+it);})
				.sorted()
				.toArray()[medianRange];
			NCRperYearMedian.put(y, crs - median);
		});
		
		return new int[][] {
			IntStream.rangeClosed(rangeYear[0], rangeYear[1]).toArray(),
			IntStream.rangeClosed(rangeYear[0], rangeYear[1]).map(RPY -> sumPerYear.get(RPY)).toArray(),
			IntStream.rangeClosed(rangeYear[0], rangeYear[1]).map(RPY -> NCRperYearMedian.get(RPY)).toArray()
		};
	}
	
	
	private static void computeNPCT (int[] rangePY, int range) {
		
		
		
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
			
			for (int i=Math.max(rpy, rangePY[0]); i<=rangePY[1]; i++) {

				final int py = i;
				
				
				int firstPY = Math.max(rpy,  Math.max(py-range, rangePY[0])); 
				int lastPY  = Math.min(py+range, rangePY[1]);
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
					long count = cr.getPub().filter(pub -> (pub.getPY() != null) && (pub.getPY().intValue() == py)).count();
					if (percBorder[0]<count) cr.setN_PCT50(cr.getN_PCT50()+1);
					if (percBorder[1]<count) cr.setN_PCT75(cr.getN_PCT75()+1);
					if (percBorder[2]<count) cr.setN_PCT90(cr.getN_PCT90()+1);
//					System.out.println("\nCR-ID=" + cr.getID() + "; PCT50=" + cr.getN_PCT50());
				}
			}
		}
		
	}
	
	
	
	private static int[] getPercBorders (Set<CRType> crSet, int firstPY, int lastPY) {
		
		int yearSize = lastPY-firstPY+1;
		int crSize = crSet.size();
		
		int[][] cit = new int[yearSize][crSize];	// matrix [year] x [cr] -> #citations 

		int idx = 0;
		for (CRType cr: crSet) {
			
			// Count #citations per PY
			Map<Integer, Long> mapPY_Count = cr.getPub().filter(pub -> pub.getPY() != null).collect(
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
		
	
}
