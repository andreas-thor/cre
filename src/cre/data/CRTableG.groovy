package cre.data 
  
import java.util.HashMap
import java.util.List
import java.util.Map

import org.jfree.data.xy.DefaultXYDataset

import cre.ui.StatusBar
import groovy.beans.Bindable
import groovy.transform.CompileStatic


/**
 * Basic data structure for list of CR
 * @author thor
 *
 */
@CompileStatic
class CRTableG {

	@Bindable DefaultXYDataset ds  = new DefaultXYDataset()
	@Bindable List<CRType> crData = new ArrayList<CRType>()	// all CR data
	List<PubType> pubData = new ArrayList<PubType>()	// all Publication data
	
	boolean duringUpdate = false
	boolean abort = false
	boolean showNull = false
	
	private Map<Integer, Integer> sumPerYear = [:]	// year -> sum of CRs (also for years without any CR)
	private Map<Integer, Integer> crPerYear = [:]	// year -> number of CRs (<0, i.e., only years with >=1 CR are in the map)
	private Map<Integer, Integer> NCRperYearMedian = [:]	// year -> median of sumPerYear[year-range] ... sumPerYear[year+range]   
	
	public CRMatch crMatch
	public StatusBar stat	// status bar to indicate current information / progress
	
	private int medianRange
	
	
	
	/**
	 * @param stat status panel
	 */
	public CRTableG(StatusBar stat) {
		this.stat = stat
		this.crMatch = new CRMatch(this, stat)
	}
	
	
	/**
	 * Initialize empty CRTable
	 */
	
	public void init() {
		crData.clear ()
		showNull = true
		crMatch.clear()
		pubData.clear()
	}
	
	/**
	 * Update computation of percentiles for all CRs
	 * Called after loading, deleting or merging of CRs
	 * @param removed Data has been removed --> adjust clustering data structures; adjust CR lists per publication
	 */
	
	public void updateData (boolean removed) throws OutOfMemoryError {

		println "update Data"
		println System.currentTimeMillis()
		
		duringUpdate = true		// mutex to avoid chart updates during computation
		
		// update match
		crMatch.updateData (removed)
		
		
		// Determine number of CRs and sum of citations per year
		crPerYear = [:]
		sumPerYear = [:]
		crData.each { CRType it ->
			if (it.RPY != null) {
				crPerYear[it.RPY] = (crPerYear[it.RPY]?:0) + 1
				sumPerYear[it.RPY] = (sumPerYear[it.RPY]?:0) + it.N_CR
			} 
		}

		println System.currentTimeMillis()
		
		// "fill" sumPerYear with zeros for missing years for "smoother" chart lines
		if (crPerYear.size() > 0) {
			(crPerYear.keySet().min()..crPerYear.keySet().max()).each { sumPerYear[it] = sumPerYear[it]?:0 }
		}	 
		
		println System.currentTimeMillis()
		
		// compute PERC_YR and PERC_ALL
		int sum = sumPerYear.inject (0) { int r, int k, int v -> r+v } as Integer
		crData.each { CRType it ->
			if (it.RPY != null) {
				it.PERC_YR  = ((it.N_CR as double)/sumPerYear[it.RPY])
			}
			it.PERC_ALL = ((it.N_CR as double)/sum)
		}

		println System.currentTimeMillis()
		
		generateChart()
		
		duringUpdate = false
		
		println System.currentTimeMillis()
		
	}

	
	public generateChart (int medianRange=-1) {
		
		if (medianRange>0) {
			this.medianRange = medianRange
		}
		
		// generate data rows for chart
		NCRperYearMedian = [:]
		sumPerYear.each { y, crs -> NCRperYearMedian[y] = crs - ((-this.medianRange..this.medianRange).collect { sumPerYear[y+it]?:0 }.sort {it}[this.medianRange]) }
		
		println System.currentTimeMillis()
		
		while (ds.getSeriesCount()>0) {
			ds.removeSeries(ds.getSeriesKey(ds.getSeriesCount()-1))
		}
		
		// generate chart lines
		sumPerYear = sumPerYear.sort { it.key }
		ds.addSeries("Number of Cited References", [sumPerYear.collect  { it.key }, sumPerYear.collect  { it.value }] as double[][])
		NCRperYearMedian = NCRperYearMedian.sort { it.key }
		ds.addSeries("Deviation from the ${2*this.medianRange+1}-Year-Median", [NCRperYearMedian.collect  { it.key }, NCRperYearMedian.collect  { it.value }] as double[][])
		
		stat.setValue("", 0, getInfoString())
	}
		
	
	public HashMap<Integer, int[]> getChartData () {
		
		HashMap<Integer, int[]> result = new HashMap<Integer, int[]>()
		NCRperYearMedian.each { 
			result[it.key] = [sumPerYear[it.key]?:0, it.value] as int[]
		}
		
		return result
	}
	
	public int getMedianRange() {
		return this.medianRange
	}
	

	
	/**
	 * Meta data about CR table 
	 * @return Map with meta data
	 */
	public Map<String, Integer> getInfo() {
		List<Integer> years = getMaxRangeYear()
		[
			"Number of Cited References": crData.size(), 
			"Number of Cited References (shown)": crData.findAll { CRType it -> it.VI==1 }.size() , 
			"Number of Cited References Clusters": crMatch.getNoOfClusters(),
			"Number of different Cited References Years": crPerYear.size(), 
			"Minimal Cited References Year": years[0], 
			"Maximal Cited References Year": years[1],
			"Number of Citing Publications": pubData.size()
		]
	}
	
	public String getInfoString() {
		List<Integer> years = getMaxRangeYear()
		"${crData.size()} CRs (${crData.findAll { CRType it -> it.VI==1}.size()} shown), ${crMatch.getNoOfClusters()} clusters, ${years[0]}-${years[1]} "
	}
	

	/**
	 * Tooltip information for chart	
	 * @param year 
	 * @return
	 */
	public String getTooltip (Integer year) {
		return "Year=${year}, N_CR=${sumPerYear[year]}"
	}
	
	
	
	/**
	 * Remove publications based on list of indexes (e.g., selected by user in the table)
	 * @param idx list of CR indexes
	 */
	public void remove (List<Integer> idx) {
		idx.sort()
		Iterator crIt = crData.iterator()
		int lastIdx = 0
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
		crData.findAll { CRType it -> it.RPY == null}.size()  
	}
	
	public void removeWithoutYear () {
		crData.removeAll { CRType it -> it.RPY == null}
//		crData.removeAll { CRType it -> (100 <= it.RPY) && (it.RPY <= 2300) }
		updateData(true)
	}


	/**
	 * Remove publications based on year range (from <= x <= to)	
	 * @param from
	 * @param to
	 */
	public void removeByYear (int from, int to) {
		long t1 = System.currentTimeMillis();
		crData.removeAll { CRType it -> (from <= it.RPY) && (it.RPY <= to) }
		long t2 = System.currentTimeMillis();
		println ("GROOVY: " + (t2-t1));
		updateData(true)
	}
	


	
	/**
	 * Remove publications based on NCR range (from <= N_CR <= to)
	 * @param from
	 * @param to
	 */
	public void removeByNCR(int from, int to) {
		crData.removeAll { CRType it -> (from <= it.N_CR) && (it.N_CR <= to) }
		updateData(true)
	}
	
	public void removeByPercentYear (String comp, double threshold) {
		switch (comp) {
			case "<" : crData.removeAll { CRType it -> it.PERC_YR <  threshold }; break
			case "<=": crData.removeAll { CRType it -> it.PERC_YR <= threshold }; break
			case "=" : crData.removeAll { CRType it -> it.PERC_YR == threshold }; break
			case ">=": crData.removeAll { CRType it -> it.PERC_YR >= threshold }; break
			case ">" : crData.removeAll { CRType it -> it.PERC_YR >  threshold }; break 
		}
		updateData(true)
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
		this.showNull = showNull
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
	

	
	public void setMapping (Integer id1, Integer id2, Double s, boolean isManual, boolean add, Long timestamp=null) {
		this.crMatch.setMapping(id1, id2, s, isManual, add, timestamp)
	}
		
	public Map getMapping (Integer id1, Boolean isManual) {
		this.crMatch.getMapping (id1, isManual) 
	}
	
	
}



