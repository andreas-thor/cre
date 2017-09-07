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
	
	private static Map<Integer, Integer> mapRPY_SumNCR;

	private static Map<Integer, Map<Integer, Integer>> mapRPY_PY_SumNCR;

	
	public static int[][] update() {

		System.out.println("1");
		
		// Group CRs by RPY
		mapRPY_CRs = CRTable.get().getCR().filter(cr -> cr.getRPY()!=null).collect(
				Collectors.groupingBy(CRType::getRPY, Collectors.mapping(Function.identity(), Collectors.toSet()) ));
		
		System.out.println("2");
		
		// Determine sum citations per RPY
		int[] rangeRPY = CRStats.getMaxRangeYear();
		int[] rangePY = CRStats.getMaxRangeCitingYear();
		
		mapRPY_SumNCR = 
			IntStream.rangeClosed(rangeRPY[0], rangeRPY[1]).mapToObj(RPY -> new Integer (RPY)).collect(Collectors.toMap(	
				RPY -> RPY, 
				RPY -> (mapRPY_CRs.get(RPY) == null) ? 0 : mapRPY_CRs.get(RPY).stream().mapToInt(cr -> cr.getN_CR()).sum()
			));
		

		System.out.println("3");
	
		/* !!!! LANGE */
		
		mapRPY_PY_SumNCR = 
			IntStream.rangeClosed(rangeRPY[0], rangeRPY[1]).mapToObj(RPY -> new Integer (RPY)).collect(Collectors.toMap(	
					RPY -> RPY, 
					RPY -> (IntStream.rangeClosed(rangePY[0], rangePY[1]).mapToObj(PY -> new Integer (PY)).collect(Collectors.toMap(
							PY -> PY,
							PY -> (mapRPY_CRs.get(RPY) == null) ? 0 : mapRPY_CRs.get(RPY).stream().mapToInt(cr -> (int)cr.getPub(PY).count()).sum()
							))
				)));
		
		
		System.out.println("4");
		
		computePERC();
		
		System.out.println("5");
		
	/* !!!! LANGE */	computeNPCT(CRStats.getMaxRangeCitingYear(), UserSettings.get().getNPCTRange());
		
		System.out.println("6");
		
		return getChartData(UserSettings.get().getMedianRange());
	}
	
	
	private static int[][] getChartData (int medianRange) {
		
		
		// compute difference to median
		final Map<Integer, Integer> mapRPY_MedianDiff = new HashMap<Integer, Integer>();	// RPY -> SumNCR - (median of sumPerYear[year-range] ... sumPerYear[year+range])   
		mapRPY_SumNCR.forEach ((rpy, crs) -> {
			int median =  IntStream.rangeClosed(-medianRange, +medianRange)
				.map( it -> { return (mapRPY_SumNCR.get(rpy+it)==null) ? 0 : mapRPY_SumNCR.get(rpy+it);})
				.sorted()
				.toArray()[medianRange];
			mapRPY_MedianDiff.put(rpy, crs - median);
		});

		
		int[] rangeYear = CRStats.getMaxRangeYear();
		
		// generate data rows for chart
		return new int[][] {
			IntStream.rangeClosed(rangeYear[0], rangeYear[1]).toArray(),
			IntStream.rangeClosed(rangeYear[0], rangeYear[1]).map(rpy -> mapRPY_SumNCR.get(rpy)).toArray(),
			IntStream.rangeClosed(rangeYear[0], rangeYear[1]).map(rpy -> mapRPY_MedianDiff.get(rpy)).toArray()
		};		
	}
	
	
	private static void computePERC () {
		

		// compute PERC_YR and PERC_ALL
		int sum = mapRPY_SumNCR.values().stream().mapToInt(Integer::intValue).sum(); 
		CRTable.get().getCR().forEach ( cr -> {
			cr.setPERC_YR( (cr.getRPY() == null) ? null : ((double)cr.getN_CR())/mapRPY_SumNCR.get(cr.getRPY()));
			cr.setPERC_ALL(((double)cr.getN_CR())/sum);
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
		
		

	}
	
	
	private static void computeNPCT (int[] rangePY, int range) {
		
		mapRPY_CRs.entrySet().parallelStream().forEach(group -> {
			
		
//		for (Entry<Integer, Set<CRType>> group: mapRPY_CRs.entrySet()) {
			
			
			int rpy = group.getKey();
			
			HashMap<Integer, char[]> mapCRID_Sequence = new HashMap<Integer, char[]>();
			HashMap<Integer, int[]> mapCRID_Type = new HashMap<Integer, int[]>();
			
			int seqStart = Math.max(rpy, rangePY[0]);
			int seqEnd = rangePY[1];
			
			
			// inititalize
			for (CRType cr: group.getValue()) {
				cr.setN_PCT50(0);
				cr.setN_PCT75(0);
				cr.setN_PCT90(0);
				mapCRID_Sequence.put(cr.getID(), new char[seqEnd-seqStart+1]);
				mapCRID_Type.put(cr.getID(), new int[9]);
				
			}
			
			
//			System.out.println("\n\nRPY=" + rpy);
//			System.out.println("\n==============");
			
			Map<Pair<Integer, Integer>, int[]> cachePercBorder = new HashMap<Pair<Integer, Integer>, int[]>();
			
//			for (int i=Math.max(rpy, rangePY[0]); i<=rangePY[1]; i++) {

			IntStream.rangeClosed(seqStart, seqEnd).forEach(py -> {				
				
				int firstPY = Math.max(rpy,  Math.max(py-range, rangePY[0])); 
				int lastPY  = Math.min(py+range, rangePY[1]);
				if (lastPY < firstPY) return;	// in the error case that RPY is higher than PY

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
//					long count = cr.getPub().filter(pub -> (pub.getPY() != null) && (pub.getPY().intValue() == py)).count();
					long count = cr.getPub(py).count(); // number of citations to CR in year PY
					if (percBorder[0]<count) cr.setN_PCT50(cr.getN_PCT50()+1);
					if (percBorder[1]<count) cr.setN_PCT75(cr.getN_PCT75()+1);
					if (percBorder[2]<count) cr.setN_PCT90(cr.getN_PCT90()+1);
//					System.out.println("\nCR-ID=" + cr.getID() + "; PCT50=" + cr.getN_PCT50());
//				}
				
				
				
				/*
			 	#433: TOTSUM ist die Anzahl der Zitierungen pro RPY?
				#440: SUM_COUNT1 ist die Anzahl der Zitierungen pro RPY und CR?
				#449: SUM_COUNT2 ist die Anzahl der Zitierungen pro RPY UND PY (mit PY >= RPY)?
				#499: COUNT ist die Anzahl der Zitierungen einer CR?
				#508: Das Array CONFIG hat am Ende die Länge (PY_Max – RPY + 1), als den maximalen Zeitraum, in dem eine CR Zitierungen bekommen kann
				#545: Was ist das t??? Woher kommen die Vergleichswerte 4 und 7?
				 */
				
//				for (CRType cr: group.getValue()) {
					
					double expect=0, z_res=0; 
					if ((mapRPY_SumNCR.get(rpy) != null) && (mapRPY_SumNCR.get(rpy) > 0)) {
						// TODO: statt cr.getN_CR() wahrscheinlich hier die N_CR für das aktuelle py
						expect = 1.0d * cr.getN_CR() * mapRPY_PY_SumNCR.get(rpy).getOrDefault(py, 0) / mapRPY_SumNCR.get(rpy);
					}
					
					if (expect>0) {
						z_res = (count /*cr.getPub(py).count()*/ - expect) / Math.sqrt(expect);
					}
					
					mapCRID_Sequence.get(cr.getID())[py-seqStart] = (z_res>1) ? '+' : ((z_res<-1) ? '-' : '0');
					
					int[] type = mapCRID_Type.get(cr.getID());
					int t = py-seqStart+1;
					type[0] +=                 (z_res>-1)?1:0;
					type[1] += ((t< 4)?1:0) * ((z_res< 0)?1:0);
					type[2] += ((t>=4)?1:0) * ((z_res> 0)?1:0);
					type[3] += ((t< 4)?1:0) * ((z_res> 1)?1:0);
					type[4] += ((t<=4)?1:0) * ((z_res< 1)?1:0);
					type[5] += ((t> 4)?1:0) * ((z_res> 1)?1:0);
					type[6] += ((t> 7)?1:0) * ((z_res<-1)?1:0);
					type[7] += (count /*cr.getPub(py).count()*/ >0)?1:0;
					type[8] += 1;
				}
				
				
				
			});
			
			for (CRType cr: group.getValue()) {
				cr.setSEQUENCE(new String (mapCRID_Sequence.get(cr.getID())));
				
				int[] type = mapCRID_Type.get(cr.getID());
				
				boolean constant  = ((1.0d*type[0]/type[8])>0.8) && ((1.0d*type[7]/type[8])>0.8);
				boolean sbeauty   = (type[1]>2) && (type[2]>2);
				boolean hotpaper  = (type[3]>1);
				boolean lifecycle = (type[4]>1) && (type[5]>1) && (type[6]>1);
				
				if (hotpaper) 	cr.setTYPE("Hot paper");
				if (sbeauty) 	cr.setTYPE("Delayed performer");
				if (lifecycle) 	cr.setTYPE("Life cycle");
				if (constant) 	cr.setTYPE("Constant performer");
				if (lifecycle && sbeauty) 	cr.setTYPE("Delayed performer / Life cycle");
				if (hotpaper && sbeauty) 	cr.setTYPE("Delayed performer / Hot paper");
				if (hotpaper && lifecycle) 	cr.setTYPE("Hot Paper / Life Cycle");
				
				
				
			}
			
		});
//		}
		
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
