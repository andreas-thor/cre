package cre.data 

import groovy.beans.Bindable
import groovy.transform.CompileStatic

import org.jfree.data.xy.DefaultXYDataset

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein
import cre.data.CRMatch.Pair
import cre.data.source.FileImport
import cre.data.source.Scopus
import cre.data.source.WoS
import cre.ui.StatusBar
import cre.ui.UIMatchPanelFactory


/**
 * Basic data structure for list of CR
 * @author thor
 *
 */
@CompileStatic
class CRTable {

	
	public class FileTooLargeException extends Exception {
		
		int numberOfCRs;

		public FileTooLargeException(int numberOfCRs) {
			super();
			this.numberOfCRs = numberOfCRs;
		}
		
		
	}
	
	public class AbortedException extends Exception { }
	
	public class UnsupportedFileFormatException extends Exception { }
	
	
	
		


	public static Map<String, String> line = [
		'NCR_PER_YEAR' : 'Number of Cited References',
		'DEV_FROM_MED' : 'Deviation from the Median'
	]
	
	@Bindable DefaultXYDataset ds  = new DefaultXYDataset()
	@Bindable List<CRType> crData = new ArrayList<CRType>()	// all CR data
	List<PubType> pubData = new ArrayList<PubType>()	// all Publication data
	
	boolean duringUpdate = false
	boolean abort = false
	
//	private long noOfPubs = 0
	private Map<Integer, Integer> sumPerYear = [:]	// year -> sum of CRs (also for years without any CR)
	private Map<Integer, Integer> crPerYear = [:]	// year -> number of CRs (<0, i.e., only years with >=1 CR are in the map)
	private Map<Integer, Integer> NCRperYearMedian = [:]	// year -> median of sumPerYear[year-range] ... sumPerYear[year+range]   
	
	CRMatch crMatch = new CRMatch()
	private Map<Integer, Integer> crId2Index = [:]						// object Id -> index in crData list
	private Map<CRCluster, List<Integer>> clusterId2Objects = [:]		// clusterId->[object ids]

	
	
	StatusBar stat	// status bar to indicate current information / progress
	
	private int medianRange
	
	
	
	/**
	 * @param stat status panel
	 */
	public CRTable (StatusBar stat) {
		this.stat = stat
	}
	
	
	/**
	 * Initialize empty CRTable
	 */
	
	public void init() {
//		noOfPubs = 0
		crData.clear ()
		crMatch.clear(false)
		crMatch.clear(true)
		clusterId2Objects.clear()
		pubData.clear()
	}
	
	/**
	 * Update computation of percentiles for all CRs
	 * Called after loading, deleting or merging of CRs
	 * @param removed Data has been removed --> adjust clustering data structures
	 */
	
	public void updateData (boolean removed) throws OutOfMemoryError {

		println "update Data"
		println System.currentTimeMillis()
		
		duringUpdate = true		// mutex to avoid chart updates during computation
		
		// refresh mapping crId -> index
		crId2Index.clear()
		crData.eachWithIndex { CRType cr, int index -> crId2Index[cr.ID] = index }
		
		println System.currentTimeMillis()
		if (removed) {
//			println "removed"
//			println System.currentTimeMillis()
			List id = crId2Index.keySet() as List
			crMatch.restrict(id)

//			println System.currentTimeMillis()
			clusterId2Objects.clear()
			crData.each { CRType cr ->
				if (clusterId2Objects[cr.CID2] == null) { 
					clusterId2Objects[cr.CID2] = []
				}
				clusterId2Objects[cr.CID2] << cr.ID
			}
			
			crData.each { CRType cr ->
				cr.CID_S = clusterId2Objects[cr.CID2].size()
			}
			
//			println System.currentTimeMillis()
//			println "removed done"
		}
		
		
		// Determine number of CRs and sum of citations per year
		crPerYear = [:]
		sumPerYear = [:]
		crData.each { CRType it ->
			crPerYear[it.RPY] = (crPerYear[it.RPY]?:0) + 1
			sumPerYear[it.RPY] = (sumPerYear[it.RPY]?:0) + it.N_CR 
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
			it.PERC_YR  = ((it.N_CR as double)/sumPerYear[it.RPY])
			it.PERC_ALL = ((it.N_CR as double)/sum)
//			it.PERC_YR  = Math.round (10000*(it.N_CR as float)/sumPerYear[it.RPY])/100f
//			it.PERC_ALL = Math.round (10000*(it.N_CR as float)/sum)/100f
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
		ds.addSeries(line['NCR_PER_YEAR'], [sumPerYear.collect  { it.key }, sumPerYear.collect  { it.value }] as double[][])
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
	 * 
	 * @param matchers  Array of matchers; each matcher has three components: [attribute, simfunc, threshold]
	 * @param globalThreshold
	 * @param useClustering
	 */
	
	public void doBlocking () {
		
		// standard blocking: year + first letter of last name
		stat.setValue("${new Date()}: Start Blocking of ${crData.size()} objects", 0)
		Map<String, ArrayList<Integer>> blocks = [:]	// block key -> list of indexes (not IDs)!
		crData.eachWithIndex { CRType cr, Integer idx ->
			cr.blockkey = cr.RPY + ((cr.AU_L+"  ")[0..0]).toLowerCase()
			if (blocks[cr.blockkey]==null) blocks[cr.blockkey] = new ArrayList<Integer>()  
			blocks[cr.blockkey] << idx
		}
		println "${new Date()}: Blocking done (${blocks.size()} blocks)"
//		println blocks
		
		crMatch.clear(false)	// remove automatic match result, but preserve manual matching
		Levenshtein l = new Levenshtein();
		long progMax = blocks.size()
		long progCount = 0
		double s, s2
		
		String d = "${new Date()}: "
		double threshold = 0.5 

		// Matching: author lastname & journal name
		blocks.each { String b, ArrayList<Integer> crlist ->
			
			progCount++
			stat.setValue("${d}Matching in progress ...", ((progCount.doubleValue()/progMax.doubleValue()*100)).intValue())
			
			// allX = List of all AU_L values;
			// compareY = List of compare string is in reverse order, i.e., pop() (takes last element) actually removes the "logical" first element
			List allX = crlist.collect { Integer it -> crData[it].AU_L.toLowerCase() }
			List compareY = crlist.collect { crData[it].AU_L.toLowerCase() }.reverse()	
			
			// ySize is used to re-adjust the index (correctIndex = ySize-yIdx-1)
			int ySize = compareY.size()

			allX.eachWithIndex { x, xIndx -> 
				
				compareY.pop()
				l.batchCompareSet(compareY as String[], x).eachWithIndex { double s1, int yIndx ->
					if (s1>=threshold) {
						s2 = l.getSimilarity((crData[crlist[xIndx]].J_N?:"").toLowerCase(), (crData[crlist[ySize-yIndx-1]].J_N?:"").toLowerCase())
						s = (2*s1+s2)/3.0		// weighted average of AU_L and J_N
						if (s>=threshold) {
							crMatch.setMapping(crData[crlist[xIndx]].ID, crData[crlist[ySize-yIndx-1]].ID, s, false, true)
						}
					}
				}
			}
		
		}
		
		println "CRMatch> matchresult size is " + crMatch.size(false)
		stat.setValue("${d}Matching done", 0)
		
		updateClusterId(threshold, false, false, false, false)
	}
	

	/**
	 * 
	 * @param threshold
	 * @param useClustering
	 * @param stat
	 * @param useVol
	 * @param usePag
	 * @param useDOI
	 * @throws Exception
	 */
	
	public void updateClusterId (double threshold, boolean useClustering, boolean useVol, boolean usePag, boolean useDOI) throws Exception {
		
		stat.setValue ("${new Date()}: Prepare clustering ...", 0)
		
		// initialize clustering; each objects forms its own cluster
		// useClustering = true => re-use first cluster component
		clusterId2Objects = [:]
		crData.eachWithIndex { CRType it, Integer idx -> 
			crData[idx].CID2 = useClustering ? new CRCluster (it.CID2.c1, it.ID) : new CRCluster (it.ID, 1)
			crData[idx].CID_S = 1
			clusterId2Objects[crData[idx].CID2] = [it.ID]
		}
//		this.noOfCRClusters = crData.size()
		
		clusterMatch( null, threshold, useVol, usePag, useDOI)	// null == all objects are considered for clustering 
		
		
		
	}
	
	
	/**
	 * clustering based on match correspondences (above threshold; different cluster ids)
	 * @param id list of object ids to be considered for clustering (null=all objects)
	 * @param threshold
	 * @param stat
	 * @throws Exception
	 */
	
	
	private void clusterMatch (List id, double threshold, boolean useVol, boolean usePag, boolean useDOI) throws Exception {
		
		String d = "${new Date()}: " 
		double mSize = crMatch.size(false).doubleValue()
		int count = 0

		crMatch.eachPair (id) { Integer id1, Integer id2, double s -> 
			
			if (Thread.interrupted()) {
				throw new Exception()
			}
			 
			stat.setValue (d + "Clustering with threshold ${threshold} in progress ...", Math.round ((double)(++count/mSize)*100d).intValue())
			
			if (s>=threshold) {
				
				CRType cr1 = crData[crId2Index[id1]]
				CRType cr2 = crData[crId2Index[id2]]
				
				CRCluster minId = (cr1.CID2.compareTo(cr2.CID2)<0) ? cr1.CID2 : cr2.CID2
				CRCluster maxId = (cr1.CID2.compareTo(cr2.CID2)>0) ? cr1.CID2 : cr2.CID2
				
				boolean vol = (!useVol) || (cr1.VOL.equals (cr2.VOL)) // || (cr1.VOL.equals("")) || (cr2.VOL.equals(""))
				boolean pag = (!usePag) || (cr1.PAG.equals (cr2.PAG)) // || (cr1.PAG.equals("")) || (cr2.PAG.equals(""))
				boolean doi = (!useDOI) || (cr1.DOI.equalsIgnoreCase (cr2.DOI)) // || (cr1.DOI.equals("")) || (cr2.DOI.equals(""))

				if ((cr1.ID.equals(6707)) || (cr1.ID.equals(60487)) || (cr1.ID.equals (60536)) || (cr1.ID.equals (-1))) {
					println "Hier"
					println "${id1},${id2} with sim ${s}"
					println "min={$minId}, max=${maxId}"
					println "vol = ${vol}, pag=${pag}, doi=${doi}"
				}

				// merge if different clusters and manual-same (s==2) or all criterias are true
				if ((minId.compareTo(maxId)!=0) && ((s==2) || ((vol) && (pag) && (doi)))) {
					clusterId2Objects[minId].addAll (clusterId2Objects[maxId])
					clusterId2Objects[minId].unique()
					int size = clusterId2Objects[minId].size()
					clusterId2Objects[minId].each { crData[crId2Index[it]].CID_S = size }
					clusterId2Objects[maxId].each { crData[crId2Index[it]].CID2 = minId }
					clusterId2Objects.remove(maxId)
//					this.noOfCRClusters--
				}
			}
		}
		
		stat.setValue ("${new Date()}: Clustering done", 0, getInfoString())
	}
	
	
	
	/**
	 * Manual specification that the list of CRs (id) should be considered pair-wise the same/different 
	 * @param id
	 * @param isSame true (same) or false (different)
	 * @param stat
	 * @param matchThreshold
	 * @param useVol
	 * @param usePag
	 * @param useDOI
	 */
	public void matchManual (List<Integer> idx, int matchType, double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {
		
		// TODO: Performanter machen
		
		
		Long timestamp = System.currentTimeMillis()		// used to group together all individual mapping pairs of match operation
		
		List<Integer> crIds = idx.collect { Integer it -> crData[it].ID }
		
		// manual-same is indicated by similarity = 2; different = -2
		if ((matchType==UIMatchPanelFactory.matchSame) || (matchType==UIMatchPanelFactory.matchDifferent)) {
			crIds.each { Integer id1 ->
				crIds.each { Integer id2 ->
					if (id1<id2) crMatch.setMapping(id1, id2, ((matchType==UIMatchPanelFactory.matchSame) ? 2d : -2d), true, false, timestamp) 
				}
			}
		}
		if (matchType==UIMatchPanelFactory.matchExtract) {
			crIds.each { Integer id1 ->
				clusterId2Objects[crData[crId2Index[id1]].CID2].each { Integer id2 ->
					crMatch.setMapping(id1, id2, -2d, true, false, timestamp)
				}
			}
		}

		
		if (matchType==UIMatchPanelFactory.matchSame) {	// cluster using the existing cluster 
			clusterMatch(crIds, matchThreshold, useVol, usePag, useDOI)
		} else { 	// re-initialize the clusters that may be subject to split and rerun clustering for all objects of the clusters
			// find all relevant clusters 
			clusterMatch(reInitObjects (idx.collect { Integer it -> crData[it].CID2 }.unique()), matchThreshold, useVol, usePag, useDOI)
		}
		
	}
	
	
	/**
	 * Re-initializes all objects of the given clusters, i.e., cluster Id is ste to object id (=> individual clusters)
	 * @param clusterIds
	 * @return list of object ids
	 */
	private List<Integer> reInitObjects (Collection<CRCluster> clusterIds) {
		
//		println "Clusterids ${clusterIds}"
		List<Integer> objects = []
		clusterIds.each { CRCluster clid -> clusterId2Objects[clid].each { Integer it -> objects << it } }
		objects.unique()
//		println "Objects ${objects}"
		
		// initialize clustering
		objects.each { Integer it ->
			int index = crId2Index[it]
//				println "crId = ${it}, index = ${index}, cluster = ${crData[index].CID2}"
			
			crData[index].CID2 = new CRCluster (crData[index].CID2.c1, it)
			crData[index].CID_S = 1
			clusterId2Objects[crData[index].CID2] = [it]
//				println crData[index]
		}
		
//			println clusterId2Objects
		objects
	}
	
	
	public matchUndo (double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {
		
		
		if (crMatch.timestampedPairs.keySet().size()==0) return

		Long lastTimestamp = crMatch.timestampedPairs.lastKey()
		List<Pair> undoPairs = crMatch.timestampedPairs.get(lastTimestamp)
		
		// redo individual mapping pairs
		undoPairs.each { Pair p -> crMatch.setMapping(p.id1, p.id2, p.s, true, false)}
		
		// get relevant cluster ids and remove 
		Collection<CRCluster> clusterIds = undoPairs.collect { Pair p -> crData[crId2Index[p.id1]].CID2 }
		clusterIds.addAll(undoPairs.collect { Pair p -> crData[crId2Index[p.id2]].CID2 })

		// remove last undo/able operation and re/cluster		
		crMatch.timestampedPairs.remove(lastTimestamp)
		clusterMatch(reInitObjects (clusterIds.unique()), matchThreshold, useVol, usePag, useDOI)
		
	}
		
	
	public void merge () {
		
		stat.setValue ("Start merging ...", 0)
				
		clusterId2Objects.eachWithIndex { cid, crlist, idx -> 
			
			stat.setValue ("Merging in progress ...", ((idx.doubleValue()/clusterId2Objects.keySet().size().doubleValue())*100).intValue())
			
			if (crlist.size()>1) {
				
				int max_N_CR = 0
				int max_cr = 0
				int sum_N_CR = 0
	
				// sum all N_CR; find max; invalidate all CRs			
				crlist.each { it -> 
					idx = crId2Index[it]
					sum_N_CR += crData[idx].N_CR
					crData[idx].CID2 = null
					if (crData[idx].N_CR>max_N_CR) {
						max_N_CR = crData[idx].N_CR
						max_cr = idx 
					}
				}
				
				// re-validate max-CR  
				crData[max_cr].N_CR = sum_N_CR
				crData[max_cr].CID2 = cid
				crData[max_cr].CID_S = 1
			}
		}
		
		// remove all invalidated CRs
		crData.removeAll { CRType it -> it.CID2 == null }
		updateData(true)
		stat.setValue ("Merging done", 0, getInfoString())
		
	}
	
	
	/**
	 * Meta data about CR table 
	 * @return Map with meta data
	 */
	public Map<String, Integer> getInfo() {
		List<Integer> years = getMaxRangeYear()
		[
			"Number of Cited References": crData.size(), 
			"Number of Cited References (shown)": crData.findAll { CRType it -> it.VI==1}.size() , 
			"Number of Cited References Clusters": clusterId2Objects.keySet().size(), 
			"Number of different Cited References Years": crPerYear.size(), 
			"Minimal Cited References Year": years[0], 
			"Maximal Cited References Year": years[1],
			"Number of Citing Publications": pubData.size()
		]
	}
	
	public String getInfoString() {
		List<Integer> years = getMaxRangeYear()
		"${crData.size()} CRs (${crData.findAll { CRType it -> it.VI==1}.size()} shown), ${clusterId2Objects.keySet().size()} clusters, ${years[0]}-${years[1]} "
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

		println "remove"
		println System.currentTimeMillis()
		
		idx.sort()
		Iterator crIt = crData.iterator()
		int lastIdx = 0
		idx.each { 
			(lastIdx..it).each { crIt.next() }
			crIt.remove()
			lastIdx = it+1
		}

		updateData(true)
	}
	

	/**
	 * Remove publications based on year range (from <= x <= to)	
	 * @param from
	 * @param to
	 */
	public void removeByYear (int from, int to) {
		crData.removeAll { CRType it -> (from <= it.RPY) && (it.RPY <= to) }
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
		if (comp.equals("<")) {
			crData.removeAll { CRType it -> it.PERC_YR < threshold }
		}
		if (comp.equals("<=")) {
			crData.removeAll { CRType it -> it.PERC_YR <= threshold }
		}
		if (comp.equals("=")) {
			crData.removeAll { CRType it -> it.PERC_YR == threshold }
		}
		if (comp.equals(">=")) {
			crData.removeAll { CRType it -> it.PERC_YR >= threshold }
		}
		if (comp.equals(">")) {
			crData.removeAll { CRType it -> it.PERC_YR > threshold }
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
		crData.each { CRType it -> it.VI = ((from<=it.RPY) && (to>=it.RPY)) ? 1 : 0 }
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
	

	
	/**
	 * Load data files from Web Of Science (WOS)
	 * @param files array of files
	 */
	public void loadDataFiles (File[] files, int maxCR, int[] yearRange) throws UnsupportedFileFormatException, FileTooLargeException, AbortedException, OutOfMemoryError {
		
		this.abort = false	// can be changed by "wait dialog"
		
		String d = "${new Date()}: "
		stat.setValue(d + "Loading files ...", 0, "")
		
		HashMap<Character,  HashMap<String,Integer>> crDup = [:]	// first character -> (crString -> id )
		int indexCount = 0
		this.init()
		
		int stepCount = 0
		int stepSize = 5
		
		
		files.eachWithIndex { File f, int idx ->

			int fileSizeStep = (int) (f.length()*stepSize/100)
			long fileSizeRead = 0
			
			BufferedReader br = new BufferedReader(new FileReader(f)) 
			String line = br.readLine()
			
			FileImport parser = null 
			if (line!=null) { 
				if (line.contains("FN Thomson Reuters Web of")) parser = new WoS(yearRange, br)
				if (line.contains("Scopus")) parser = new Scopus(yearRange, br)
			}
			
			if (parser == null) throw new UnsupportedFileFormatException()
			
			
			while (parser.hasNextPub()) {
			
//			while ((line = br.readLine()) != null) {
			
				// Check for abort by user
				if (this.abort) {
					this.init()
					this.updateData(false);
					stat.setValue("${new Date()}: Loading WOS files aborted", 0)
					this.abort = false
					throw new AbortedException()
				}
				
				
//				cr = parser.parseLine(line)
				PubType pub = parser.getNextPub()

				// update status bar
				//				fileSizeRead += line.length()+1
				fileSizeRead += pub.length;
				if (stepCount*fileSizeStep < fileSizeRead) {
					stat.setValue (d + "Loading WOS file ${idx+1} of ${files.length}", stepCount*stepSize)
					stepCount++
				}
				
								
				pub.crList.eachWithIndex { CRType cr, int crPos -> 
//				for (CRType cr: fe.crList) {
//				if (cr != null) {
						
//					println cr.CR
//					println cr.CR[0]
					
					
					// if CR already in database: increase N_CR by 1; else add new record
					if (crDup[cr.CR.charAt(0)] == null) crDup[cr.CR.charAt(0)] = [:]
					Integer id = crDup[cr.CR.charAt(0)][cr.CR]
					if (id != null) {
						crData[id].N_CR++
						pub.crList[crPos] = crData[id]
						 
//						println cr.CR
					} else {
						crDup[cr.CR.charAt(0)][cr.CR] = indexCount
						
						if ((maxCR>0) && (indexCount==maxCR)) {
							this.updateData(false);
							stat.setValue("${new Date()}: Loading WOS files aborted", 0)
							throw new FileTooLargeException (indexCount);
						}
						
						cr.ID = indexCount+1
						cr.CID2 = new CRCluster (indexCount+1, 1)
						cr.CID_S = 1
						crData << cr
						
	//					crData << new CRType (
	//						indexCount+1,
	//						cr,
	//						author,
	//						firstname,
	//						lastname,
	//						journal,
	//						journal_name,
	//						journal_short,
	//						1,
	//						year,
	//						pagVol["P"],
	//						pagVol["V"],
	//						doi,
	//						c,		// each CR forms its own cluster
	//						1,		// cluster size is 1 (by default)
	//						1,		// is visible by default
	//						0		// default color
	//					)
						
						clusterId2Objects[cr.CID2] = [indexCount+1]
						indexCount++
					}
					
				}
				
				this.pubData << pub
//				this.noOfPubs++
			}
			
//			this.noOfPubs += parser.noOfPubs
		}

		
//		long timeEnd = System.currentTimeMillis()
//		long overall = timeEnd-timeStart
//		println "Overall: ${overall}"
//		for (int i=0; i<timeDiffs.length; i++) {
//			long percent = (long) (timeDiffs[i]*100 / overall)
//			println "${i} : ${percent}%"
//		}
//		
//		println indexCount
		
		
		this.updateData(false);
		stat.setValue("${new Date()}: Loading WOS files done", 0, getInfoString())
	}
	
}



