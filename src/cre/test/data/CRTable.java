package cre.test.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import cre.test.data.CRMatch.ManualMatchType;
import cre.test.data.type.CRType;
import cre.test.data.type.PubType;

public class CRTable {

	private static CRTable crTab = null;
	

//	protected ArrayList<CRType> crData = new ArrayList<CRType>();
	
	protected Set<CRType> crData = new HashSet<CRType>();
	
//	protected List<PubType> pubData = new ArrayList<PubType>();	// all Publication data
	protected CRMatch crMatch;
	
	
	private boolean duringUpdate = false;
	private boolean aborted;
	private boolean showNull = false;
	
	
	private Map<Character, HashMap<String,CRType>> crDup; // first character -> (crString -> CR )

	
	private Map<Integer, Integer> sumPerYear = new HashMap<Integer, Integer>();	// year -> sum of CRs (also for years without any CR)
	

	
	public static CRTable get() {
		if (crTab == null) {
			crTab = new CRTable();
		}
		return crTab;
	}
	
	
	
	/**
	 * @param stat status panel
	 */
	private CRTable () {
		this.crMatch = new CRMatch(this);
//		this.crStats = new CRStats(this);
	}
	



	/**
	 * Initialize empty CRTable
	 */
	
	public void init() {
//		crData.clear ();
		crData = new HashSet<CRType>();
		crDup = new HashMap<Character,  HashMap<String, CRType>>();
				
				
		showNull = true;
//		crMatch.clear();
		crMatch = new CRMatch(this);
//		pubData.clear();
//		pubData = new ArrayList<PubType>();
		setAborted(false);
	}
	
	
	public Stream<CRType> getCR() {
		return crData.stream();
	}
	
	public Stream<PubType> getPub() {
		return crData.stream().flatMap(cr -> cr.getPub()).distinct();
	}
	
//	public Stream<PubType> getPub () {
//		return this.pubData.stream();
//	}
	

	
	
	
	
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
				cr.setCID2(new CRCluster (cr.getID(), 1));
				cr.setCID_S(1);
			} else {
				// merge current CR with main CR
				cr.getPub().collect(Collectors.toList()).stream().forEach(crPub -> {		// make a copy to avoid concurrent modifcation
					crPub.addCR(crMain, true);
					crPub.removeCR(cr, true);
				});
			}
		});
	}	
	
	

	
	/*
	
	public void createCRList () {
		
		// TODO: initialize crDup  when "no init" mode  
		HashMap<Character,  HashMap<String,Integer>> crDup = new HashMap<Character,  HashMap<String,Integer>>(); // first character -> (crString -> id )
		int indexCount = 0;
	
		for (PubType pub: pubData) {
				
			int crPos = 0;
			
			HashSet<CRType> crList = new HashSet<CRType>(pub.crList);
			pub.crList = new HashSet<CRType>();
			
			for (CRType cr: crList) {
					
				// if CR already in database: increase N_CR by 1; else add new record
				crDup.putIfAbsent(cr.getCR().charAt(0), new HashMap<String,Integer>());
				Integer id = crDup.get(cr.getCR().charAt(0)).get(cr.getCR());
				if (id != null) {
//					crData.get(id).setN_CR(crData.get(id).getN_CR() + 1);
//					pub.crList.set(crPos, crData.get(id));
					
					pub.crList.add(crData.get(id));
					crData.get(id).addPub(pub);
					
				} else {
					
					
					crDup.get(cr.getCR().charAt(0)).put (cr.getCR(), indexCount);
					

					
					// todo: add new CR as separate function (make clusterId2Objects private again)
					
					cr.setID(indexCount+1);
					cr.setCID2(new CRCluster (indexCount+1, 1));
					cr.setCID_S(1);
					crData.add (cr);
					
					HashSet<Integer> tmp = new HashSet<Integer>();
					tmp.add(indexCount+1);
					crMatch.clusterId2Objects.put(cr.getCID2(), tmp);
//								crTab.crMatch.clusterId2Objects[cr.CID2] = [indexCount+1];
					indexCount++;
					
					pub.crList.add(cr);
					cr.addPub(pub);
				}
				crPos++;
					
			} 
				
//						this.noOfPubs++
//					this.noOfPubs += parser.noOfPubs

		};
	}
	
	*/
	
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
		
		crMatch.updateData (removed);
		
		
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
		
		/*
		crData.parallelStream().forEach( (CRType cr) -> {
			cr.propID = new SimpleIntegerProperty (cr.ID).asObject();
			cr.propCR = new SimpleStringProperty (cr.CR);
			cr.propAU = new SimpleStringProperty (cr.AU);
			cr.propAU_F = new SimpleStringProperty (cr.AU_F); 
			cr.propAU_L = new SimpleStringProperty (cr.AU_L);
			cr.propAU_A = new SimpleStringProperty (cr.AU_A);	
			cr.propTI = new SimpleStringProperty (cr.TI); 		
			cr.propJ = new SimpleStringProperty (cr.J);
			cr.propJ_N = new SimpleStringProperty (cr.J_N);
			cr.propJ_S = new SimpleStringProperty (cr.J_S);
			cr.propN_CR = new SimpleIntegerProperty (cr.N_CR).asObject();
			cr.propRPY = new SimpleIntegerProperty (cr.RPY).asObject();
			cr.propPAG = new SimpleStringProperty (cr.PAG);
			cr.propVOL = new SimpleStringProperty (cr.VOL);
			
			
			cr.propDOI = new SimpleStringProperty (cr.DOI);
			cr.propCID_S = new SimpleIntegerProperty (cr.CID_S).asObject();
			cr.propVI = new SimpleIntegerProperty (cr.VI).asObject();	
			cr.propCO = new SimpleIntegerProperty (cr.CO).asObject();	
			cr.propPERC_YR = new SimpleDoubleProperty (cr.PERC_YR).asObject();
			
			// DIE PROPERTIES ALS Werte???
			
			cr.propPERC_ALL = new SimpleDoubleProperty (cr.PERC_ALL).asObject();
			cr.propN_PYEARS = new SimpleIntegerProperty (cr.N_PYEARS).asObject();	
			cr.propPYEAR_PERC = new SimpleDoubleProperty (cr.PYEAR_PERC).asObject();
			cr.propN_PCT50 = new SimpleIntegerProperty (cr.N_PCT50).asObject();
			cr.propN_PCT75 = new SimpleIntegerProperty (cr.N_PCT75).asObject();
			cr.propN_PCT90 = new SimpleIntegerProperty (cr.N_PCT90).asObject();
			cr.propN_PYEARS2 = new SimpleIntegerProperty (cr.N_PYEARS2).asObject();	
		});
		*/
		
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
		getPub().filter(cond).forEach(pub -> pub.removeAllCRs(true));
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
	
	
//	public void filterByYear () {
//		filterByYear (this.getMaxRangeYear());
//	}
	
	
	public void setShowNull (boolean showNull) {
		this.showNull = showNull;
		crData.stream().forEach ( it -> { if (it.getRPY() == null) it.setVI(showNull);  });
	}
	
	public void setMapping (Integer id1, Integer id2, Double s, boolean isManual, boolean add, Long timestamp) {
		this.crMatch.setMapping(id1, id2, s, isManual, add, timestamp);
	}

	public void setMapping (Integer id1, Integer id2, Double s, boolean isManual, boolean add) {
		this.crMatch.setMapping(id1, id2, s, isManual, add, null);
	}
		
	public Map<Integer, Double> getMapping (Integer id1, Boolean isManual) {
		return this.crMatch.getMapping (id1, isManual); 
	}
	
	


	
//	public CRType getCR (int idx) {
//		return this.crData.get(idx);
//	}
	


	
	public void addCR(CRType cr) {
		crData.add(cr);
	}

	



	
	public Stream<Entry<Integer, Map<Integer, Double>>> getMatch (boolean manual) {
		return this.crMatch.match.get(manual).entrySet().stream();
	}









	public void matchManual(List<CRType> toMatch, ManualMatchType type, double threshold, boolean useVol, boolean usePag, boolean useDOI) {
		this.crMatch.matchManual(toMatch, type, threshold, useVol, usePag, useDOI);
	}

	public void matchUndo(double threshold, boolean useVol, boolean usePag, boolean useDOI) {
		this.crMatch.matchUndo(threshold, useVol, usePag, useDOI);
	}




	public void matchUpdateClusterId(double threshold, boolean useClustering, boolean useVol, boolean usePag, boolean useDOI) {
		this.crMatch.updateClusterId(threshold, useClustering, useVol, usePag, useDOI);
		
	}


	public void matchMerge() {
		this.crMatch.merge();
	}




	public void matchDoBlocking() {
		this.crMatch.doBlocking();
	}

	public boolean hasMatches () {
		return this.crMatch.hasMatches();
	}
	
	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}



	
}
