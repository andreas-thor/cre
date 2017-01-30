package cre.test.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CRTable {

	

	

	
	protected ArrayList<CRType> crData = new ArrayList<CRType>(); 

	
	// public ObservableList<CRType> crDataObserved = FXCollections.observableArrayList(crData); // new   new ArrayList<CRType>();	// all CR data
	
	// this is automatic but too time consuming for filters
	// public ObservableList<CRType> crData = FXCollections.observableArrayList( item -> new Observable[] { item.getVIProp() });
	
	public List<PubType> pubData = new ArrayList<PubType>();	// all Publication data
	
	public boolean duringUpdate = false;
	
	public boolean abort = false;
	boolean showNull = false;
	
	private Map<Integer, Integer> sumPerYear = new HashMap<Integer, Integer>();	// year -> sum of CRs (also for years without any CR)
//	private Map<Integer, Integer> crPerYear = new HashMap<Integer, Integer>();	// year -> number of CRs (<0, i.e., only years with >=1 CR are in the map)
	
	public CRMatch crMatch;
	
//	private CRTableEvent eventHandler;
	
	public File creFile;
	
	
	/**
	 * @param stat status panel
	 */
	public CRTable (/* CRTableEvent eventHandler */) {
//		this.eventHandler = eventHandler;
		this.crMatch = new CRMatch(this);
	}
	



	/**
	 * Initialize empty CRTable
	 */
	
	public void init() {
//		crData.clear ();
		crData = new ArrayList<CRType>();
		showNull = true;
//		crMatch.clear();
		crMatch = new CRMatch(this);
//		pubData.clear();
		pubData = new ArrayList<PubType>();
		creFile = null;
	}
	
	
	
	public void createCRList () {
		
		// TODO: initialize crDup  when "no init" mode  
		HashMap<Character,  HashMap<String,Integer>> crDup = new HashMap<Character,  HashMap<String,Integer>>(); // first character -> (crString -> id )
		int indexCount = 0;
	
		for (PubType pub: pubData) {
				
			int crPos = 0;
			for (CRType cr: pub.crList) {
					
				// if CR already in database: increase N_CR by 1; else add new record
				crDup.putIfAbsent(cr.getCR().charAt(0), new HashMap<String,Integer>());
				Integer id = crDup.get(cr.getCR().charAt(0)).get(cr.getCR());
				if (id != null) {
					crData.get(id).setN_CR(crData.get(id).getN_CR() + 1);
					pub.crList.set(crPos, crData.get(id));
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
				}
				crPos++;
					
			} 
				
//						this.noOfPubs++
//					this.noOfPubs += parser.noOfPubs

		};
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
		crMatch.updateData (removed);
		System.out.println("match done:" + System.currentTimeMillis());

		// compute list of citing publications for each CR
		crData.stream().forEach ( it -> it.pubList = new ArrayList<PubType>() );
		pubData.stream().forEach ( (PubType pub) -> 
			pub.crList.stream().forEach ( (CRType cr) -> cr.pubList.add(pub) )
		);
		
		
		// Determine sum citations per year
		int[] rangeYear = getMaxRangeYear();
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
		int[] rangePub = getMaxRangeCitingYear();
		crData.stream().forEach( cr -> {
			cr.setN_PYEARS((int) cr.pubList.stream().filter(pub -> pub.PY!=null).mapToInt(pub -> pub.PY).distinct().count());
			cr.setPYEAR_PERC( (cr.getRPY()==null) ? null : ((double)cr.getN_PYEARS()) /  (rangePub[1]-Math.max(rangePub[0], cr.getRPY())+1));
		});
		
		System.out.println("c1: " + System.currentTimeMillis());
		
		
		Indicators.computeNPCT(crData, rangePub[1]);
		
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
		
	
		


	
	/**
	 * Meta data about CR table 
	 * @return Map with meta data
	 */
	public Map<String, Integer> getInfo() {
		int[] years = getMaxRangeYear();
		Map<String, Integer> result = new HashMap<String, Integer>();
		result.put("Number of Cited References", crData.size());
		result.put("Number of Cited References (shown)", (int) crData.stream().filter( (CRType it) -> it.getVI()).count()); 
		result.put("Number of Cited References Clusters", crMatch.getNoOfClusters());
		result.put("Number of different Cited References Years", (int)crData.stream().filter (it -> it.getRPY() != null).mapToInt(it -> it.getRPY()).distinct().count()); 
		result.put("Minimal Cited References Year", years[0]);
		result.put("Maximal Cited References Year", years[1]);
		result.put("Number of Citing Publications", pubData.size());
		return result;
	}
	
	
	
	public String getInfoString() {
		int[] years = getMaxRangeYear();
		return String.format("%1$d CRs (%2$d shown), %3$d clusters, %4$d-%5$d ",
			crData.size(),
			crData.stream().filter ( (CRType it) -> it.getVI()).count(),
			crMatch.getNoOfClusters(), 
			years[0], 
			years[1]);
	}
	


	
	
	/**
	 * Remove publications based on list of indexes (e.g., selected by user in the table)
	 * @param idx list of CR indexes
	 */
	public void remove (List<CRType> toDelete) {
		
		toDelete.stream().forEach( cr -> { cr.removed = true; }); 
		crData.removeIf(cr -> cr.removed);
		updateData(true);
	}
	
	
	public void retain (List<CRType> toRetain) {
		
		crData.stream().forEach(cr -> cr.removed = true);
		toRetain.stream().forEach( cr -> { cr.removed = false; }); 
		crData.removeIf(cr -> cr.removed);
		updateData(true);
	}
	
	/**
	 * Remove all citing publications, that do not reference any of the given CRs (idx)
	 * @param idx list of CR indexes
	 */
	public void removeByCR (List<CRType> selCR) {
		
		pubData.removeIf ( pub -> {
			
			// if crList of publications does not contain any of the CRs 
			if (!pub.crList.stream().anyMatch ( cr -> { return selCR.contains (cr); } )) {
				pub.crList.forEach ( cr -> { cr.setN_CR(cr.getN_CR() - 1); } );	// remove number of CRs by 1
				return true;	// delete pub
			} else {
				return false;
			}
		});
		
		// remove all CRs that are not referenced anymore
		crData.removeIf (  it -> { it.removed = (it.getN_CR() < 1); return it.removed; });
		updateData(true);
	}
	
	
	
	
	
	
	public int getNumberWithoutYear () {
		return (int) crData.stream().filter( (CRType it) -> it.getRPY() == null).count();  
	}
	
	public void removeWithoutYear () {
		crData.removeIf( (CRType cr) -> { cr.removed = (cr.getRPY() == null); return cr.removed; });
		updateData(true);
	}

	
	

	/**
	 * Count / Remove publications based on year range (from <= x <= to)	
	 * Property "removed" is set for cascading deletions (e.g., Citing Publications) 
	 * @param from
	 * @param to
	 */
	
	public long getNumberByYear (int[] range) {
		return crData.stream().filter( cr -> ((cr.getRPY()!=null) && (range[0] <= cr.getRPY()) && (cr.getRPY() <= range[1]))).count();
	}
	
	public void removeByYear (int from, int to) {
		crData.removeIf( (CRType cr) -> { cr.removed = ((cr.getRPY()!=null) && (from <= cr.getRPY()) && (cr.getRPY() <= to)); return cr.removed; });
		updateData(true);
	}

	public long getNumberOfPubsByCitingYear (int[] range) {
		return pubData.stream().filter( pub -> ((pub.PY!=null) && (range[0] <= pub.PY) && (pub.PY <= range[1]))).count();
	}

	public long getNumberOfPubs () {
		return pubData.size();
	}
	
	/**
	 * Remove all citing publications OUTSIDE the given year range
	 * @param from
	 * @param to
	 */
	public void removeByCitingYear (int[] range) {
		
		pubData.removeIf  ( (PubType pub) -> {
		
			if ((pub.PY==null) || (range[0] > pub.PY) || (pub.PY > range[1])) {
				pub.crList.forEach ( cr -> { cr.setN_CR(cr.getN_CR() - 1); } );	// remove number of CRs by 1
				return true;	// delete pub
			} else {
				return false;
			}
		});
		
		// remove all CRs that are not referenced anymore
		crData.removeIf (  it -> { it.removed = (it.getN_CR() < 1); return it.removed; });
		updateData(true);				
	}
	
	
	/**
	 * Count / Remove publications based on NCR range (from <= N_CR <= to)
	 * @param from
	 * @param to
	 */
	
	public long getNumberByNCR (int[] range) {
		return crData.stream().filter(cr -> ((range[0] <= cr.getN_CR()) && (cr.getN_CR() <= range[1]))).count();
	}
	
	public void removeByNCR(int[] range) {
		crData.removeIf ( (CRType cr) -> { cr.removed = ((range[0] <= cr.getN_CR()) && (cr.getN_CR() <= range[1])); return cr.removed; });
		updateData(true);
	}
	
	/**
	 * Count / Remove publiations based on PERC_YR
	 * @param comp Comparator (<, <=, =, >=, or >)
	 * @param threshold
	 * @return
	 */
	
	public long getNumberByPercentYear (String comp, double threshold) {
		switch (comp) {
			case "<" : return crData.stream().filter( cr -> cr.getPERC_YR() <  threshold ).count(); 
			case "<=": return crData.stream().filter( cr -> cr.getPERC_YR() <= threshold ).count();
			case "=" : return crData.stream().filter( cr -> cr.getPERC_YR() == threshold ).count();
			case ">=": return crData.stream().filter( cr -> cr.getPERC_YR() >= threshold ).count();
			case ">" : return crData.stream().filter( cr -> cr.getPERC_YR() >  threshold ).count();
		}
		return 0;
	}
	
	public void removeByPercentYear (String comp, double threshold) {
		switch (comp) {
			case "<" : crData.removeIf ( (CRType it) -> { it.removed = (it.getPERC_YR() <  threshold); return it.removed; }); break;
			case "<=": crData.removeIf ( (CRType it) -> { it.removed = (it.getPERC_YR() <= threshold); return it.removed; }); break;
			case "=" : crData.removeIf ( (CRType it) -> { it.removed = (it.getPERC_YR() == threshold); return it.removed; }); break;
			case ">=": crData.removeIf ( (CRType it) -> { it.removed = (it.getPERC_YR() >= threshold); return it.removed; }); break;
			case ">" : crData.removeIf ( (CRType it) -> { it.removed = (it.getPERC_YR() >  threshold); return it.removed; }); break;
		}
		updateData(true);
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
	
	/**
	 * 
	 * @return [min, max]
	 */
	public int[] getMaxRangeYear () {
		IntSummaryStatistics stats = crData.stream().filter (cr -> cr.getRPY() != null).mapToInt(it -> it.getRPY()).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}
	
	
	public int[] getMaxRangeCitingYear () {
		IntSummaryStatistics stats = pubData.stream().filter (pub -> pub.PY != null).mapToInt(it -> it.PY).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}

	
	
	/**
	 * 
	 * @return [min, max]
	 */
	public int[] getMaxRangeNCR () {
		IntSummaryStatistics stats = crData.stream().map((CRType it) -> it.getN_CR()).mapToInt(Integer::intValue).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
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
	
	

	public int getSize () {
		return this.crData.size();
	}
	
	public int getSizePub () {
		return this.pubData.size();
	}
	
	public int getSizeMatch (boolean manual) {
		return this.crMatch.match.get(manual).size();
	}
	
	public CRType getCR (int idx) {
		return this.crData.get(idx);
	}
	

	public Stream<CRType> getCR () {
		return this.crData.stream();
	}
	
	public void add(CRType cr) {
		crData.add(cr);
	}

	
	public Stream<PubType> getPub () {
		return this.pubData.stream();
	}
	
	public Stream<Entry<Integer, Map<Integer, Double>>> getMatch (boolean manual) {
		return this.crMatch.match.get(manual).entrySet().stream();
	}




	
	
}
