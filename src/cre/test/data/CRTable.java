package cre.test.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jfree.data.xy.DefaultXYDataset;

import cre.test.Main.EventCRFilter;
import cre.test.ui.StatusBar;
import groovy.beans.Bindable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CRTable {

	
	
	
	 
	public @Bindable DefaultXYDataset ds  = new DefaultXYDataset();
	
	public ArrayList<CRType> crData = new ArrayList<CRType>(); 
	public ObservableList<CRType> crDataObserved = FXCollections.observableArrayList(crData); // new   new ArrayList<CRType>();	// all CR data
	
	
	// this is automatic but too time consuming for filters
	// public ObservableList<CRType> crData = FXCollections.observableArrayList( item -> new Observable[] { item.getVIProp() });
	
	public List<PubType> pubData = new ArrayList<PubType>();	// all Publication data
	
	public boolean duringUpdate = false;
	public boolean abort = false;
	boolean showNull = false;
	
	private Map<Integer, Integer> sumPerYear = new HashMap<Integer, Integer>();	// year -> sum of CRs (also for years without any CR)
	private Map<Integer, Integer> crPerYear = new HashMap<Integer, Integer>();	// year -> number of CRs (<0, i.e., only years with >=1 CR are in the map)
	private Map<Integer, Integer> NCRperYearMedian = new HashMap<Integer, Integer>();	// year -> median of sumPerYear[year-range] ... sumPerYear[year+range]   
	
	public CRMatch crMatch;
	public StatusBar stat;	// status bar to indicate current information / progress
	
	private EventCRFilter eventFilter;
	
	
	private int medianRange;
	public File creFile;
	
	
	/**
	 * @param stat status panel
	 */
	public CRTable (StatusBar stat, EventCRFilter eventFilter) {
		this.stat = stat;
		this.eventFilter = eventFilter;
		this.crMatch = new CRMatch(this, stat);
	}
	
	
	/**
	 * Initialize empty CRTable
	 */
	
	public void init() {
//		crData.clear ();
		crData = new ArrayList<CRType>();
		showNull = true;
//		crMatch.clear();
		crMatch = new CRMatch(this, stat);
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

		
		// Determine number of CRs and sum of citations per year
		crPerYear = new HashMap<Integer, Integer>();
		sumPerYear = new HashMap<Integer, Integer>();
		crData.stream().forEach( it -> {
			if (it.getRPY() != null) {
				crPerYear.compute(it.getRPY(), (k, v) -> (v == null) ? 1 : v+1);
				sumPerYear.compute(it.getRPY(), (k, v) -> (v == null) ? it.getN_CR() : v+it.getN_CR());
			}
		});

		System.out.println("a" + System.currentTimeMillis());

		// "fill" sumPerYear with zeros for missing years for "smoother" chart lines
		if (crPerYear.size() > 0) {
			int min = crPerYear.keySet().stream().mapToInt(Integer::intValue).min().getAsInt();
			int max = crPerYear.keySet().stream().mapToInt(Integer::intValue).max().getAsInt();
			IntStream.rangeClosed(min, max).forEach(year -> {
				sumPerYear.compute(year, (k, v) -> (v == null) ? 0 : v);
			});
		} 
		
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
		

		// compute list of citing publications for each CR
		crData.stream().forEach ( it -> it.pubList = new ArrayList<PubType>() );
		pubData.stream().forEach ( (PubType pub) -> 
			pub.crList.stream().forEach ( (CRType cr) -> cr.pubList.add(pub) )
		);
				
		// compute N_PYEARS
		
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
		
		generateChart();
		duringUpdate = false;
		
		System.out.println(System.currentTimeMillis());
		
	}

	
	public void generateChart () {
		generateChart (-1);
	}
	
	public void generateChart (int medianRange) {
		
		if (medianRange>0) {
			this.medianRange = medianRange;
		}
		
		// generate data rows for chart
		NCRperYearMedian = new HashMap<Integer, Integer>();
		sumPerYear.forEach ((y, crs) -> {
			
			int median =  IntStream.rangeClosed(-this.medianRange, +this.medianRange)
				.map( it -> { return (sumPerYear.get(y+it)==null) ? 0 : sumPerYear.get(y+it);})
				.sorted()
				.toArray()[this.medianRange];
			NCRperYearMedian.put(y, crs - median);
		});
		
		System.out.println(System.currentTimeMillis());
		
		while (ds.getSeriesCount()>0) {
			ds.removeSeries(ds.getSeriesKey(ds.getSeriesCount()-1));
		}
		
		
		// generate chart lines
		ds.addSeries("Number of Cited References", new double[][] { 
			sumPerYear.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(it -> { return it.getKey(); })
				.mapToDouble(Double::valueOf)
				.toArray(),
			sumPerYear.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(it -> { return it.getValue(); })
				.mapToDouble(Double::valueOf)
				.toArray() }
		);
		
		ds.addSeries(String.format("Deviation from the %1$d-Year-Median", 2*this.medianRange+1), new double[][] { 
			NCRperYearMedian.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(it -> { return it.getKey(); })
				.mapToDouble(Double::valueOf)
				.toArray(),
			NCRperYearMedian.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(it -> { return it.getValue(); })
				.mapToDouble(Double::valueOf)
				.toArray() }
		);
		
		stat.setValue("", getInfoString());
	}
		
	
	public Map<Integer, int[]> getChartData () {
		
		return NCRperYearMedian.keySet()
			.stream()
			.collect (Collectors.toMap(
				year -> year, 
				year -> new int[] { sumPerYear.getOrDefault(year, 0), NCRperYearMedian.getOrDefault(year, 0)} ));

	}
	
	public int getMedianRange() {
		return this.medianRange;
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
		result.put("Number of different Cited References Years", crPerYear.size()); 
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
	 * Tooltip information for chart	
	 * @param year 
	 * @return
	 */
	public String getTooltip (Integer year) {
		return String.format("Year=%1$d, N_CR=%2$d", year, sumPerYear.get(year));
	}
	
	
	
	/**
	 * Remove publications based on list of indexes (e.g., selected by user in the table)
	 * @param idx list of CR indexes
	 */
//	public void remove (List<Integer> idx) {
//
//		System.out.println("Remove ");
//		idx.stream().sorted(Collections.reverseOrder()).forEach( id -> {
//			CRType cr = crData.remove(id.intValue());
//			cr.removed = true;
//		});
//		
//		updateData(true);
//	}
	
	
	public void remove (List<CRType> toDelete) {
		
		toDelete.stream().forEach( cr -> { cr.removed = true; }); 
		crData.removeAll(toDelete);
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
	public void filterByYear (int from, int to) {
		crData.stream().forEach ( it -> { it.setVI(((it.getRPY()!=null) && (from<=it.getRPY()) && (to>=it.getRPY())) || ((it.getRPY()==null) && (this.showNull))); });
		eventFilter.onUpdate(from, to);
	}
	
	
	public void filterByYear () {
		int[] range = this.getMaxRangeYear();
		filterByYear (range[0], range[1]);
	}
	
	
	public void setShowNull (boolean showNull) {
		this.showNull = showNull;
		crData.stream().forEach ( it -> { if (it.getRPY() == null) it.setVI(showNull);  });
		eventFilter.onUpdate(null, null);

	}
	
	/**
	 * 
	 * @return [min, max]
	 */
	public int[] getMaxRangeYear () {
		IntSummaryStatistics stats = crData.stream().filter (cr -> cr.getRPY() != null).map((CRType it) -> it.getRPY()).mapToInt(Integer::intValue).summaryStatistics();
		return (stats.getCount()==0) ? new int[] {-1, -1} : new int[] { stats.getMin(), stats.getMax() };
	}
	
	
	public int[] getMaxRangeCitingYear () {
		IntSummaryStatistics stats = pubData.stream().filter (pub -> pub.PY != null).map((PubType it) -> it.PY).mapToInt(Integer::intValue).summaryStatistics();
		return new int[] { stats.getMin(), stats.getMax() };
	}

	
	
	/**
	 * 
	 * @return [min, max]
	 */
	public int[] getMaxRangeNCR () {
		IntSummaryStatistics stats = crData.stream().map((CRType it) -> it.getN_CR()).mapToInt(Integer::intValue).summaryStatistics();
		return new int[] { stats.getMin(), stats.getMax() };
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
	
	public CRType getCR (int idx) {
		return this.crData.get(idx);
	}
	
	
	
}
