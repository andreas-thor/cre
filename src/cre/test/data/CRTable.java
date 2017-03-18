package cre.test.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import cre.test.data.match.CRCluster;
import cre.test.data.match.CRMatch2;
import cre.test.data.type.CRType;
import cre.test.data.type.PubType;
import cre.test.ui.StatusBar;

public class CRTable {

	private static CRTable crTab = null;
	
	private List<CRType> crData;
	private Map<Character, HashMap<String, CRType>> crDup; // first character -> (crString -> CR )

	private boolean duringUpdate;
	private boolean aborted;
	private boolean showNull;
	
	private Map<Integer, Integer> sumPerYear = new HashMap<Integer, Integer>();	// year -> sum of CRs (also for years without any CR)
	

	
	public static CRTable get() {
		if (crTab == null) {
			crTab = new CRTable();
		}
		return crTab;
	}
	
	
	private CRTable () { 
		init();
	}
	

	/**
	 * Initialize empty CRTable
	 */
	
	public void init() {
		crData = new ArrayList<CRType>();
		crDup = new HashMap<Character,  HashMap<String, CRType>>();
		duringUpdate = false;
		aborted = false;
		showNull = true;
		setAborted(false);
	}
	
	
	public Stream<CRType> getCR() {
		return crData.stream();
	}
	
	public Stream<PubType> getPub() {
		return crData.stream().flatMap(cr -> cr.getPub()).distinct();
	}
	

	public void addCR(CRType cr) {
		String crS = cr.getCR();
		char cr1 = crS.charAt(0);
		crDup.putIfAbsent(cr1, new HashMap<String,CRType>());
		crDup.get(cr1).put(crS, cr);
		crData.add(cr);
	}
	
	
	
	public void addPubs(List<PubType> pubs) {
		
		pubs.stream().flatMap(pub -> pub.getCR()).distinct().collect(Collectors.toList()).stream().forEach(cr -> { // make a copy to avoid concurrent modifcation
			
			String crS = cr.getCR();
			char cr1 = crS.charAt(0);
			crDup.putIfAbsent(cr1, new HashMap<String,CRType>());
			CRType crMain = crDup.get(cr1).get(crS); 
			if (crMain == null) {
				// add current CR to list
				crDup.get(cr1).put(crS, cr);
				crData.add(cr);
				cr.setID(crData.size());
				cr.setCID2(new CRCluster (cr));
			} else {
				// merge current CR with main CR
				cr.getPub().collect(Collectors.toList()).stream().forEach(crPub -> {		// make a copy to avoid concurrent modification
					crPub.addCR(crMain, true);
					crPub.removeCR(cr, true);
				});
			}
		});
	}	
	
	

	public void merge () {
		
		// get all clusters with size > 1
		Set<CRCluster> clusters = getCR().filter(cr -> cr.getCID_S()>1).map(cr -> cr.getCID2()).distinct().collect(Collectors.toSet());
		StatusBar.get().setValue(String.format("Merging of %d clusters...", clusters.size()));

		// merge clusters
		clusters.forEach(cl -> {
			
			StatusBar.get().incProgressbar();
			
			// get mainCR = CR with highest number of citations
			CRType crMain = cl.getMainCR();
			Set<CRType> crMerge = cl.getCR().collect(Collectors.toSet());
			crMerge.remove(crMain);

			// merge CRs with main CR
			for (CRType cr:crMerge) {
				cr.getPub().collect(Collectors.toList()).stream().forEach(crPub -> {		// make a copy to avoid concurrent modification
					crPub.addCR(crMain, true);
					crPub.removeCR(cr, true);
				});
			}
			
			// remove merged CRs
			this.crData.removeAll(crMerge);
		});
		
		// reset clusters and match result
		getCR().forEach(cr -> cr.setCID2(new CRCluster(cr)));
		CRMatch2.get().init();
	}
	
	
	
	/**
	 * Update computation of percentiles for all CRs
	 * Called after loading, deleting or merging of CRs
	 * @param removed Data has been removed --> adjust clustering data structures; adjust CR lists per publication
	 */
	
	public void updateData (boolean removed) throws OutOfMemoryError {

		System.out.println("update Data");
		System.out.println(System.currentTimeMillis());
		
		duringUpdate = true;		// mutex to avoid chart updates during computation
		
		// update match
		
//		crMatch.updateData (removed);
		
		
		System.out.println("match done:" + System.currentTimeMillis());

		// compute list of citing publications for each CR
		
		// not anymore ?
//		crData.stream().forEach ( it -> it.pubList = new ArrayList<PubType>() );
//		pubData.stream().forEach ( (PubType pub) -> 
//			pub.crList.stream().distinct().forEach ( (CRType cr) -> cr.pubList.add(pub) )
//		);
		
		
//		crData.stream().forEach( it -> it.setN_CR(it.pubList.size()) );
		


		
		// Determine sum citations per year
		int[] rangeYear = CRStats.getMaxRangeYear();
		sumPerYear = IntStream.rangeClosed(rangeYear[0], rangeYear[1]).mapToObj (it -> new Integer(it)).collect(Collectors.toMap(it->it, it->0));
		crData.stream().filter (it -> it.getRPY() != null).forEach( it -> { sumPerYear.computeIfPresent(it.getRPY(), (year, sum) -> sum + it.getN_CR()); });
		

		System.out.println("b" + System.currentTimeMillis());
		
		// compute PERC_YR and PERC_ALL
		int sum = sumPerYear.values().stream().mapToInt(Integer::intValue).sum(); 
		crData.stream().forEach ( it -> {
			if (it.getRPY() != null) {
				it.setPERC_YR(((double)it.getN_CR())/sumPerYear.get(it.getRPY()));
			}
			it.setPERC_ALL(((double)it.getN_CR())/sum);
		});

		System.out.println("c" + System.currentTimeMillis());
		

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
		crData.stream().forEach( cr -> {
			cr.setN_PYEARS((int) cr.getPub().filter(pub -> pub.PY!=null).mapToInt(pub -> pub.PY).distinct().count());
			cr.setPYEAR_PERC( (cr.getRPY()==null) ? null : ((double)cr.getN_PYEARS()) /  (rangePub[1]-Math.max(rangePub[0], cr.getRPY())+1));
		});
		
		System.out.println("c1: " + System.currentTimeMillis());
		
		
		Indicators.computeNPCT(crData, rangePub[0], rangePub[1], UserSettings.get().getNPCTRange());
		
/*		

		// SELECT RPY, PY, Frequency FROM CR-Pub GROUP BY RPY, PY
		Map<Pair<Integer, Integer>, Frequency> mapRPY_PY_Count = new HashMap<Pair<Integer, Integer>, Frequency>();
		crData.stream().filter(cr -> cr.getRPY()!=null).forEach(cr -> {
			cr.pubList.stream().filter(pub -> pub.PY != null).collect(Collectors.groupingBy(PubType::getPY, Collectors.counting())).forEach((py, count) -> {
				Pair<Integer, Integer> pair = new Pair<Integer, Integer>(cr.getRPY(), py);
				mapRPY_PY_Count.putIfAbsent(pair, new Frequency());
				
				// consider #citations only ocnce
//				if (mapRPY_PY_Count.get(pair).getCount(count)==0) {
					mapRPY_PY_Count.get(pair).addValue(count);
//				}
			});
		});
		
		System.out.println("c2: " + System.currentTimeMillis());

		crData.stream().filter(cr -> cr.getRPY()!=null).forEach(cr -> {
			cr.setN_PCT50(0);
			cr.setN_PCT75(0);
			cr.setN_PCT90(0);
			cr.pubList.stream().filter(pub -> pub.PY != null).collect(Collectors.groupingBy(PubType::getPY, Collectors.counting())).forEach((py, count) -> {
				Pair<Integer, Integer> pair = new Pair<Integer, Integer>(cr.getRPY(), py);
				Frequency f = mapRPY_PY_Count.get(pair);
				
				double perc = f.getCumPct(count);
				// adjustment if there are multiple CRs with the same N_CR
				if (f.getCount(count)>1) {
					perc -= f.getPct(count)/2;
				}
		
				if (cr.getID()==56) {
					System.out.println("RPY=" + pair.getKey() + ", PY=" + pair.getValue() + ", Count=" + count + " ==> perc=" + perc);
					System.out.println(f);
				}
				
				
				if (perc>0.5)  cr.setN_PCT50(cr.getN_PCT50()+1);
				if (perc>0.75) cr.setN_PCT75(cr.getN_PCT75()+1);
				if (perc>0.9)  cr.setN_PCT90(cr.getN_PCT90()+1);
			});
			
			if (cr.getID()==56) {
				System.out.println("N_PCT50=" + cr.getN_PCT50());
				System.out.println("N_PCT75=" + cr.getN_PCT75());
				System.out.println("N_PCT90=" + cr.getN_PCT90());
			}
		});
			
		*/
		
		System.out.println("c3: " + System.currentTimeMillis());
		
/*		
		int maxPubYear = pubData.stream().mapToInt(pub -> (pub.PY==null)?0:pub.PY).max().getAsInt();
		
		Map<Integer, Frequency> mapPY2CRFreq = new HashMap<Integer, Frequency>();
		pubData.stream().forEach(pub -> {
			if (mapPY2CRFreq.get(pub.PY) == null) mapPY2CRFreq.put(pub.PY, new Frequency());
			Frequency f = mapPY2CRFreq.get(pub.PY);
			pub.crList.stream().forEach(cr -> { f.addValue(cr.ID); } );
		});
		
		crData.parallelStream().forEach( (CRType cr) -> {
			cr.N_PYEARS = (int) cr.pubList.stream().map(pub -> pub.PY).distinct().count();
			cr.PYEAR_PERC = (cr.RPY==null) ? null : ((double)cr.N_PYEARS) / (maxPubYear-cr.RPY+1);
			cr.N_PCT50 = 0;
			cr.N_PCT75 = 0;
			cr.N_PCT90 = 0;
			cr.N_PYEARS2 = 0;
			
			if (cr.RPY!=null) {
				for (int y=cr.RPY; y<=maxPubYear; y++) {
					Frequency f = mapPY2CRFreq.get(y);
					if (f==null) continue;
					if (f.getCount(cr.ID)==0) continue; 
					cr.N_PYEARS2++;
					double perc = f.getCumPct(cr.ID);
					if (perc>0.5) cr.N_PCT50++;
					if (perc>0.75) cr.N_PCT75++;
					if (perc>0.9) cr.N_PCT90++;
					
					
				}
				
			}
		});
		*/
		
		System.out.println("d" + System.currentTimeMillis());
		

		
//		generateChart();
		duringUpdate = false;
		
		System.out.println(System.currentTimeMillis());
		
		
		
		
		
/*  LUCENE TEST		
		 System.out.println("Free memory (bytes): " + 
				  Runtime.getRuntime().freeMemory());
		 
		RAMDirectory lu_dir = new RAMDirectory();
		try {
			IndexWriter lu_idx = new IndexWriter(lu_dir, new IndexWriterConfig(new StandardAnalyzer()));
			
			for (CRType cr: crData) {
				
				Document d = new Document();
				org.apache.lucene.document.TextField t = new org.apache.lucene.document.TextField("CR", cr.getCR(), Store.YES);
				d.add(t);
				
				lu_idx.addDocument(d);
			}
			
			lu_idx.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 System.out.println("Free memory (bytes): " + 
				  Runtime.getRuntime().freeMemory());
		 
			System.out.println(System.currentTimeMillis());
*/
			
	}

	
	
	public int[][] getChartData () {
		
		final Map<Integer, Integer> NCRperYearMedian = new HashMap<Integer, Integer>();	// year -> median of sumPerYear[year-range] ... sumPerYear[year+range]   
		final int medianRange = UserSettings.get().getMedianRange();
		
		// generate data rows for chart
		sumPerYear.forEach ((y, crs) -> {
			int median =  IntStream.rangeClosed(-medianRange, +medianRange)
				.map( it -> { return (sumPerYear.get(y+it)==null) ? 0 : sumPerYear.get(y+it);})
				.sorted()
				.toArray()[medianRange];
			NCRperYearMedian.put(y, crs - median);
		});
		
		return new int[][] {
			
			// x-axis = sorted years 
			sumPerYear.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(it -> { return it.getKey(); })
			.mapToInt(Integer::valueOf)
			.toArray(),
			
			// y-axis[0] = number of CRs per Year
			sumPerYear.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(it -> { return it.getValue(); })
			.mapToInt(Integer::valueOf)
			.toArray() ,
			
			// y-axis[1] = Difference to median
			sumPerYear.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(it -> { return NCRperYearMedian.get(it.getKey()); })
			.mapToInt(Integer::valueOf)
			.toArray()
		};
	}
		
	
		


	private void removeCR (Predicate<CRType> cond) {
		crData.removeIf( cr ->  { 
			if (cond.test(cr)) {
				cr.removeAllPubs(true);
				return true;
			} else {
				return false;
			}
		});
		updateData(true);
	}

	/**
	 * Remove list of CRs
	 * @param toDelete list of CRs to be deleted
	 */
	public void removeCR (List<CRType> toDelete) {
		removeCR(cr -> toDelete.contains(cr));
	}
	

	/**
	 * Remove all but the given list of CRs
	 * @param toRetain list of CRs to be retained
	 */
	public void retainCR (List<CRType> toRetain) {
		removeCR(cr -> !toRetain.contains(cr));
	}
	
	
	/**
	 * Remove all CRs without year (RPY)
	 */
	public void removeCRWithoutYear () {
		removeCR (cr -> cr.getRPY() == null);
	}

	
	/**
	 * Remove all CRS within a given RPY range
	 * @param range
	 */
	public void removeCRByYear (int[] range) {
		removeCR (cr -> ((cr.getRPY()!=null) && (range[0] <= cr.getRPY()) && (cr.getRPY() <= range[1])));
	}

	
	/**
	 * Remove all CRs within a given N_CR range
	 * @param range
	 */
	public void removeCRByN_CR(int[] range) {
		removeCR (cr -> (range[0] <= cr.getN_CR()) && (cr.getN_CR() <= range[1]));
	}
	
	
	/**
	 * Remove all CRs < / <= / = / >= / > PERC_YR
	 * @param comp comparator (as string); TODO: ENUMERATION
	 * @param threshold
	 */
	
	public void removeCRByPERC_YR (String comp, double threshold) {
		switch (comp) {
			case "<" : removeCR (cr -> cr.getPERC_YR() <  threshold); break;
			case "<=": removeCR (cr -> cr.getPERC_YR() <= threshold); break;
			case "=" : removeCR (cr -> cr.getPERC_YR() == threshold); break;
			case ">=": removeCR (cr -> cr.getPERC_YR() >= threshold); break;
			case ">" : removeCR (cr -> cr.getPERC_YR() >  threshold); break;
		}
	}
	
	
	/**
	 * Remove all citing publications, that do *not* reference any of the given CRs 
	 * @param selCR list of CRs
	 */
	public void removePubByCR (List<CRType> selCR) {
		removePub (pub -> !selCR.stream().flatMap (cr -> cr.getPub()).distinct().collect(Collectors.toList()).contains(pub));
	}
	
	
	
	private void removePub (Predicate<PubType> cond) {
		getPub().filter(cond).collect(Collectors.toList()).forEach(pub -> pub.removeAllCRs(true));
		removeCR(cr -> cr.getN_CR()==0);
	}
	
	/**
	 * Remove all citing publications OUTSIDE the given citing year (PY) range
	 * @param range
	 */
	public void removePubByCitingYear (int[] range) {
		removePub (pub -> (pub.PY==null) || (range[0] > pub.PY) || (pub.PY > range[1]));
	}
	
	

	
	
	/**
	 * Filter publications by year range
	 * Filtering = set VI property to 1 or 0
	 * @param from
	 * @param to
	 */
	public void filterByYear (int[] range) {
		if (!duringUpdate) {
			crData.stream().forEach ( it -> { it.setVI(((it.getRPY()!=null) && (range[0]<=it.getRPY()) && (range[1]>=it.getRPY())) || ((it.getRPY()==null) && (this.showNull))); });
		}
	}
	

	
	public void filterByCluster (List<CRType> sel) {
		Set<CRCluster> clusters = sel.stream().map(cr -> cr.getCID2()).distinct().collect(Collectors.toSet());
		crData.stream().forEach ( it -> it.setVI( clusters.contains(it.getCID2()) ));
	}
	
	
	

	
	
	public void setShowNull (boolean showNull) {
		this.showNull = showNull;
		crData.stream().forEach ( it -> { if (it.getRPY() == null) it.setVI(showNull);  });
	}
	
	public void showAll() {
		this.showNull = true;
		crData.stream().forEach ( it -> it.setVI(true) );
	}
	
	

	
	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}






	
}
