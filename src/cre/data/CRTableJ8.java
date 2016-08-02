package cre.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jfree.data.xy.DefaultXYDataset;

import cre.ui.StatusBar;
import groovy.beans.Bindable;

public class CRTableJ8 {

	
	@Bindable DefaultXYDataset ds  = new DefaultXYDataset();
	@Bindable List<CRType> crData = new ArrayList<CRType>();	// all CR data
	List<PubType> pubData = new ArrayList<PubType>();	// all Publication data
	
	boolean duringUpdate = false;
	boolean abort = false;
	boolean showNull = false;
	
	private Map<Integer, Integer> sumPerYear = new HashMap<Integer, Integer>();	// year -> sum of CRs (also for years without any CR)
	private Map<Integer, Integer> crPerYear = new HashMap<Integer, Integer>();	// year -> number of CRs (<0, i.e., only years with >=1 CR are in the map)
	private Map<Integer, Integer> NCRperYearMedian = new HashMap<Integer, Integer>();	// year -> median of sumPerYear[year-range] ... sumPerYear[year+range]   
	
	public CRMatch crMatch;
	public StatusBar stat;	// status bar to indicate current information / progress
	
	private int medianRange;
	
	
	/**
	 * @param stat status panel
	 */
	public CRTableJ8 (StatusBar stat) {
		this.stat = stat;
		// TODO: CRMATCH ADJUST
//		this.crMatch = new CRMatch(this, stat);
	}
	
	
	/**
	 * Initialize empty CRTable
	 */
	
	public void init() {
		crData.clear ();
		showNull = true;
		crMatch.clear();
		pubData.clear();
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
		
		
		// Determine number of CRs and sum of citations per year
		crPerYear = new HashMap<Integer, Integer>();
		sumPerYear = new HashMap<Integer, Integer>();
		crData.stream().forEach( it -> {
			if (it.RPY != null) {
				crPerYear.compute(it.RPY, (k, v) -> (v == null) ? 1 : v+1);
				sumPerYear.compute(it.RPY, (k, v) -> (v == null) ? it.N_CR : v+it.N_CR);
			}
		});

		System.out.println(System.currentTimeMillis());

		// "fill" sumPerYear with zeros for missing years for "smoother" chart lines
		if (crPerYear.size() > 0) {
			int min = crPerYear.keySet().stream().mapToInt(Integer::intValue).min().getAsInt();
			int max = crPerYear.keySet().stream().mapToInt(Integer::intValue).max().getAsInt();
			IntStream.rangeClosed(min, max).forEach(year -> {
				sumPerYear.compute(year, (k, v) -> (v == null) ? 0 : v);
			});
		} 
		
		System.out.println(System.currentTimeMillis());
		
		// compute PERC_YR and PERC_ALL
		int sum = sumPerYear.values().stream().mapToInt(Integer::intValue).sum(); 
		crData.stream().forEach ( it -> {
			if (it.RPY != null) {
				it.PERC_YR  = ((double)it.N_CR)/sumPerYear.get(it.RPY);
			}
			it.PERC_ALL = ((double)it.N_CR)/sum;
		});

		System.out.println(System.currentTimeMillis());
		
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
		// TODO: CHECK!!!
		ds.addSeries("Number of Cited References", sumPerYear
			.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByKey())
			.toArray(double[][]::new));
		
		ds.addSeries("Deviation from the ${2*this.medianRange+1}-Year-Median", NCRperYearMedian
			.entrySet()
			.stream()
			.sorted(Map.Entry.comparingByKey())
			.toArray(double[][]::new));	

		
		// groovy
//		sumPerYear = sumPerYear.sort { it.key }
//		ds.addSeries("Number of Cited References", [sumPerYear.collect  { it.key }, sumPerYear.collect  { it.value }] as double[][])
//		NCRperYearMedian = NCRperYearMedian.sort { it.key }
//		ds.addSeries("Deviation from the ${2*this.medianRange+1}-Year-Median", [NCRperYearMedian.collect  { it.key }, NCRperYearMedian.collect  { it.value }] as double[][])
		
		stat.setValue("", 0, getInfoString());
	}
		
	
	public Map<Integer, int[]> getChartData () {
		
		return NCRperYearMedian.keySet()
			.stream()
			.collect (Collectors.toMap(
				year -> year, 
				year -> new int[] { sumPerYear.getOrDefault(year, 0), NCRperYearMedian.getOrDefault(year, 0)} ));
		
		
//		HashMap<Integer, int[]> result = new HashMap<Integer, int[]>()
//		NCRperYearMedian.each { 
//			result[it.key] = [sumPerYear[it.key]?:0, it.value] as int[]
//		}
//		
//		return result
	}
	
	public int getMedianRange() {
		return this.medianRange;
	}
	

	
	/**
	 * Meta data about CR table 
	 * @return Map with meta data
	 */
	public Map<String, Integer> getInfo() {
		List<Integer> years = getMaxRangeYear();
		Map<String, Integer> result = new HashMap<String, Integer>();
		result.put("Number of Cited References", crData.size());
		result.put("Number of Cited References (shown)", (int) crData.stream().filter( (CRType it) -> it.VI == 1).count()); 
		result.put("Number of Cited References Clusters", crMatch.getNoOfClusters());
		result.put("Number of different Cited References Years", crPerYear.size()); 
		result.put("Minimal Cited References Year", years.get(0));
		result.put("Maximal Cited References Year", years.get(1));
		result.put("Number of Citing Publications", pubData.size());
		return result;
	}
	
	public String getInfoString() {
		List<Integer> years = getMaxRangeYear();
		return String.format("%1$d CRs (%2$d shown), %3$d clusters, %4$d-%5$d ",
			crData.size(),
			crData.stream().filter ( (CRType it) -> it.VI==1).count(),
			crMatch.getNoOfClusters(), 
			years.get(0), 
			years.get(1));
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
	public void remove (List<Integer> idx) {
		idx.sort();
		Iterator crIt = crData.iterator();
		int lastIdx = 0;
		idx.each { 
			(lastIdx..it).each { crIt.next() }		// iterate until next CR to remove
			crIt.remove()							// remove current
			lastIdx = it+1	
		}
		updateData(true)
	}
	
	
	/**
	 * Remove all citing publications, that do not reference any of the given CRs (idx)
	 * @param idx list of CR indexes
	 */
	public void removeByCR (List<Integer> idx) {
		
		List<CRType> selCR = idx.collect { crData[it] }
		
		pubData.removeAll { PubType pub ->
			
			// if crList of publications does not contain any of the CRs 
			if (!pub.crList.any { CRType cr -> selCR.contains (cr) }) {
				pub.crList.each { CRType cr -> cr.N_CR-- }	// remove number of CRs by 1
				true	// delete pub
			} else {
				false
			}
		}
		
		// remove all CRs that are not referenced anymore
		crData.removeAll { CRType it -> it.N_CR < 1}
		updateData(true)
	}
	
	
	public int getNumberWithoutYear () {
		return (int) crData.stream().filter( (CRType it) -> it.RPY == null).count();  
	}
	
	public void removeWithoutYear () {
		crData.removeIf( (CRType cr) -> { return cr.RPY == null; });
		updateData(true);
	}


	/**
	 * Remove publications based on year range (from <= x <= to)	
	 * @param from
	 * @param to
	 */
	public void removeByYear (int from, int to) {
		crData.removeIf( (CRType cr) -> { return (from <= cr.RPY) && (cr.RPY <= to); });
		updateData(true);
	}

	
	/**
	 * Remove publications based on NCR range (from <= N_CR <= to)
	 * @param from
	 * @param to
	 */
	public void removeByNCR(int from, int to) {
		crData.removeIf ( (CRType cr) -> { return (from <= cr.N_CR) && (cr.N_CR <= to); });
		updateData(true);
	}
	
	public void removeByPercentYear (String comp, double threshold) {
		switch (comp) {
			case "<" : crData.removeIf ( (CRType it) -> { return it.PERC_YR <  threshold; }); break;
			case "<=": crData.removeIf ( (CRType it) -> { return it.PERC_YR <= threshold; }); break;
			case "=" : crData.removeIf ( (CRType it) -> { return it.PERC_YR == threshold; }); break;
			case ">=": crData.removeIf ( (CRType it) -> { return it.PERC_YR >= threshold; }); break;
			case ">" : crData.removeIf ( (CRType it) -> { return it.PERC_YR >  threshold; }); break;
		}
		updateData(true);
	}
	
	
	/**
	 * Filter publications by year range
	 * Filtering = set VI property to 1 or 0
	 * @param from
	 * @param to
	 */
	public void filterByYear (double from, double to) {
		crData.each { CRType it -> it.VI = ((it.RPY!=null) && (from<=it.RPY) && (to>=it.RPY)) || ((it.RPY==null) && (this.showNull)) ? 1 : 0 }
	}
	
	
	public void setShowNull (boolean showNull) {
		this.showNull = showNull;
		crData.each { CRType it -> if (it.RPY == null) it.VI = showNull ? 1 : 0  }
	}
	
	/**
	 * 
	 * @return [min, max]
	 */
	public List<Integer> getMaxRangeYear () {
		List<Integer> years = crData.collect { CRType it ->  it.RPY }	
		[years.min(), years.max()]
	}
	
	
	/**
	 * 
	 * @return [min, max]
	 */
	public List<Integer> getMaxRangeNCR () {
		List<Integer> NCRs = crData.collect { CRType it -> it.N_CR }
		[NCRs.min(), NCRs.max()]
	}
	

	
	public void setMapping (Integer id1, Integer id2, Double s, boolean isManual, boolean add, Long timestamp) {
		this.crMatch.setMapping(id1, id2, s, isManual, add, timestamp);
	}

	public void setMapping (Integer id1, Integer id2, Double s, boolean isManual, boolean add) {
		this.crMatch.setMapping(id1, id2, s, isManual, add, null);
	}
		
	public Map getMapping (Integer id1, Boolean isManual) {
		return this.crMatch.getMapping (id1, isManual); 
	}
	
	

	
	
	

	
	
}
