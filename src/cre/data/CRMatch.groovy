package cre.data 

import groovy.transform.CompileStatic
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein
import cre.ui.StatusBar
import cre.ui.UIMatchPanelFactory

@CompileStatic
class CRMatch {


	class Pair {
		Integer id1
		Integer id2
		Double s
		public Pair(Integer id1, Integer id2, Double s) {
			super();
			this.id1 = id1;
			this.id2 = id2;
			this.s = s;
		}
	}
	
	private CRTable crTab
	private StatusBar stat
	private Map <Boolean, Map<Integer, Map<Integer, Double>>> match = [:]
	private TreeMap <Long, ArrayList<Pair>> timestampedPairs
	
	
	private Map<Integer, Integer> crId2Index = [:]						// object Id -> index in crData list
	public Map<CRCluster, List<Integer>> clusterId2Objects = [:]		// clusterId->[object ids]

	

	public CRMatch (CRTable crTab, StatusBar stat) {
		this.crTab = crTab
		this.stat = stat
		match = [:]
		match[false] = [:]		// automatic match result
		match[true] = [:]		// manual match result
		timestampedPairs = new TreeMap<Long, ArrayList<Pair>>()
	}


	public void updateData (boolean removed) throws OutOfMemoryError {
		
		// refresh mapping crId -> index
		crId2Index.clear()
		crTab.crData.eachWithIndex { CRType cr, int index -> crId2Index[cr.ID] = index }
		
		println System.currentTimeMillis()
		if (removed) {
//			println "removed"
//			println System.currentTimeMillis()
			List id = crId2Index.keySet() as List
			restrict(id)

//			println System.currentTimeMillis()
			clusterId2Objects.clear()
			crTab.crData.each { CRType cr ->
				if (clusterId2Objects[cr.CID2] == null) {
					clusterId2Objects[cr.CID2] = []
				}
				clusterId2Objects[cr.CID2] << cr.ID
			}
			
			crTab.crData.each { CRType cr ->
				cr.CID_S = clusterId2Objects[cr.CID2].size()
			}
			
			// remove CRs for each publication
			crTab.pubData.each { PubType pub -> pub.crList.removeAll { CRType cr -> !crTab.crData.contains(cr) } }
			
			
//			println System.currentTimeMillis()
//			println "removed done"
		}
	}



	/**
	 * Restrict match result to list of given ids
	 * Called when user deletes CRs
	 * @param id
	 */
	public void restrict (List<Integer> id) {
		[true, false].each { boolean tf ->
			match[tf].keySet().removeAll { Integer it -> !id.contains(it) }
			match[tf].each { Integer k, Map<Integer, Double> map ->
				match[tf][k].keySet().removeAll { Integer it -> !id.contains(it) }
			}
		}
	}


	public void setMapping (Integer id1, Integer id2, Double s, boolean isManual, boolean add, Long timestamp=null) {

		if (id1.equals(id2)) return
			// swap if needed so that always holds id1<id2
			if (id1.compareTo(id2)>0) {
				(id1,id2) = [id2, id1]
			}

		if (match[isManual][id1] == null) {
			match[isManual][id1] = [:]
		}

		if (match[!isManual][id1] == null) {
			match[!isManual][id1] = [:]
		}

		// store old value for undo operation of manual mappings
		if ((isManual) && (timestamp!=null)) {
			if (timestampedPairs.get(timestamp)==null) {
				timestampedPairs.put(timestamp, new ArrayList<Pair>())
			}
			timestampedPairs.get(timestamp).add(new Pair(id1, id2, match[isManual][id1][id2]))
		}
		
		double v = 0d
		if ((add) && (match[isManual][id1][id2] != null)) {
			v = match[isManual][id1][id2]
		}

		match[isManual][id1][id2] = (s==null) ? null : s+v
		
		
		
		
	}



	public Map getMapping (Integer id1, Boolean isManual) {
		match[isManual][id1]?:[:]
	}


	public void clear () {
		match[true] = [:]
		match[false] = [:]
		clusterId2Objects.clear()
	}

	public int size (boolean isManual) {
		(int) match[isManual].inject (0) { int res, Integer k, Map<Integer, Double> v -> res + v.size() }
	}

	public int getNoOfClusters () {
		clusterId2Objects.keySet().size()
	}

	/**
	 *
	 * @param matchers  Array of matchers; each matcher has three components: [attribute, simfunc, threshold]
	 * @param globalThreshold
	 * @param useClustering
	 */
	
	public void doBlocking () {
		
		// standard blocking: year + first letter of last name
		stat.setValue("${new Date()}: Start Blocking of ${crTab.crData.size()} objects", 0)
		Map<String, ArrayList<Integer>> blocks = [:]	// block key -> list of indexes (not IDs)!
		crTab.crData.eachWithIndex { CRType cr, Integer idx ->
			cr.blockkey = cr.RPY + ((cr.AU_L+"  ")[0..0]).toLowerCase()
			if (blocks[cr.blockkey]==null) blocks[cr.blockkey] = new ArrayList<Integer>()
			blocks[cr.blockkey] << idx
		}
		println "${new Date()}: Blocking done (${blocks.size()} blocks)"
//		println blocks
		
		match[false] = [:]	// remove automatic match result, but preserve manual matching
		Levenshtein l = new Levenshtein();
		long progMax = blocks.size()
		long progCount = 0
		double s, s2
		
		String d = "${new Date()}: "
		double threshold = 0.5

		// TODO: handle missing values
		// TODO: incorporate title (from scopus)
		
		// Matching: author lastname & journal name
		blocks.each { String b, ArrayList<Integer> crlist ->
			
			progCount++
			stat.setValue("${d}Matching in progress ...", ((progCount.doubleValue()/progMax.doubleValue()*100)).intValue())
			
			// allX = List of all AU_L values;
			// compareY = List of compare string is in reverse order, i.e., pop() (takes last element) actually removes the "logical" first element
			List allX = crlist.collect { Integer it -> crTab.crData[it].AU_L.toLowerCase() }
			List compareY = crlist.collect { crTab.crData[it].AU_L.toLowerCase() }.reverse()
			
			// ySize is used to re-adjust the index (correctIndex = ySize-yIdx-1)
			int ySize = compareY.size()

			allX.eachWithIndex { x, xIndx ->
				
				compareY.pop()
				l.batchCompareSet(compareY as String[], x).eachWithIndex { double s1, int yIndx ->
					if (s1>=threshold) {
						s2 = l.getSimilarity((crTab.crData[crlist[xIndx]].J_N?:"").toLowerCase(), (crTab.crData[crlist[ySize-yIndx-1]].J_N?:"").toLowerCase())
						s = (2*s1+s2)/3.0		// weighted average of AU_L and J_N
						if (s>=threshold) {
							setMapping(crTab.crData[crlist[xIndx]].ID, crTab.crData[crlist[ySize-yIndx-1]].ID, s, false, true)
						}
					}
				}
			}
		
		}
		
		println "CRMatch> matchresult size is " + size(false)
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
		crTab.crData.eachWithIndex { CRType it, Integer idx ->
			crTab.crData[idx].CID2 = useClustering ? new CRCluster (it.CID2.c1, it.ID) : new CRCluster (it.ID, 1)
			crTab.crData[idx].CID_S = 1
			clusterId2Objects[crTab.crData[idx].CID2] = [it.ID]
		}
		clusterMatch( null, threshold, useVol, usePag, useDOI)	// null == all objects are considered for clustering
		
		
		
	}
	
	
	
	/**
	 * clustering based on match correspondences (above threshold; different cluster ids)
	 * @param id list of object ids to be considered for clustering (null=all objects)
	 * @param threshold
	 * @param stat
	 * @throws Exception
	 */
	
	
	private void clusterMatch (List<Integer> id, double threshold, boolean useVol, boolean usePag, boolean useDOI) throws Exception {
		
		String d = "${new Date()}: "
		double mSize = (size(false)+size(true)).doubleValue()
		int count = 0

		if (id == null) {	// if no ids are given, use all ids with match pairs
			id = new ArrayList<Integer>()
			id.addAll(match[false].keySet())
			id.addAll(match[true].keySet())
		}
		
		// for all domain objects (id1)
		id.each { Integer id1 ->
			
			if (Thread.interrupted()) throw new Exception();
			stat.setValue (d + "Clustering with threshold ${threshold} in progress ...", Math.round ((double)(++count/mSize)*100d).intValue())
			
			// for all matching range objects (id2 with id1<id2) 
			Map<Integer, Double> tmpMap1 = match[false][id1]?:(Map<Integer, Double>)[:]
			Map<Integer, Double> tmpMap2 = match[true][id1]?:(Map<Integer, Double>)[:]
			(tmpMap1.keySet()+tmpMap2.keySet()).each { Integer id2 ->
	
				double s = (tmpMap2[id2]?:tmpMap1[id2])?:-1d	// -1 if neither in automatic nor in manual (due to redo) 
				if (s>=threshold) {
					
					CRType cr1 = crTab.crData[crId2Index[id1]]
					CRType cr2 = crTab.crData[crId2Index[id2]]
					
					CRCluster minId = (cr1.CID2.compareTo(cr2.CID2)<0) ? cr1.CID2 : cr2.CID2
					CRCluster maxId = (cr1.CID2.compareTo(cr2.CID2)>0) ? cr1.CID2 : cr2.CID2
					
					boolean vol = (!useVol) || (cr1.VOL.equals (cr2.VOL)) // || (cr1.VOL.equals("")) || (cr2.VOL.equals(""))
					boolean pag = (!usePag) || (cr1.PAG.equals (cr2.PAG)) // || (cr1.PAG.equals("")) || (cr2.PAG.equals(""))
					boolean doi = (!useDOI) || (cr1.DOI.equalsIgnoreCase (cr2.DOI)) // || (cr1.DOI.equals("")) || (cr2.DOI.equals(""))
	
					// merge if different clusters and manual-same (s==2) or all criteria are true
					if ((minId.compareTo(maxId)!=0) && ((s==2) || ((vol) && (pag) && (doi)))) {
						clusterId2Objects[minId].addAll (clusterId2Objects[maxId])
						clusterId2Objects[minId].unique()
						int size = clusterId2Objects[minId].size()
						clusterId2Objects[minId].each { crTab.crData[crId2Index[it]].CID_S = size }
						clusterId2Objects[maxId].each { crTab.crData[crId2Index[it]].CID2 = minId }
						clusterId2Objects.remove(maxId)
					}
				}
			}
		}
		
		stat.setValue ("${new Date()}: Clustering done", 0, crTab.getInfoString())
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
		
		List<Integer> crIds = idx.collect { Integer it -> crTab.crData[it].ID }
		
		// manual-same is indicated by similarity = 2; different = -2
		if ((matchType==UIMatchPanelFactory.matchSame) || (matchType==UIMatchPanelFactory.matchDifferent)) {
			crIds.each { Integer id1 ->
				crIds.each { Integer id2 ->
					if (id1<id2) setMapping(id1, id2, ((matchType==UIMatchPanelFactory.matchSame) ? 2d : -2d), true, false, timestamp)
				}
			}
		}
		if (matchType==UIMatchPanelFactory.matchExtract) {
			crIds.each { Integer id1 ->
				clusterId2Objects[crTab.crData[crId2Index[id1]].CID2].each { Integer id2 ->
					setMapping(id1, id2, -2d, true, false, timestamp)
				}
			}
		}

		
		if (matchType==UIMatchPanelFactory.matchSame) {	// cluster using the existing cluster
			clusterMatch(crIds, matchThreshold, useVol, usePag, useDOI)
		} else { 	// re-initialize the clusters that may be subject to split and rerun clustering for all objects of the clusters
			// find all relevant clusters
			clusterMatch(reInitObjects (idx.collect { Integer it -> crTab.crData[it].CID2 }.unique()), matchThreshold, useVol, usePag, useDOI)
		}
		
	}
	
	
	/**
	 * Re-initializes all objects of the given clusters, i.e., cluster Id is set to object id (=> individual clusters)
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
//				println "crId = ${it}, index = ${index}, cluster = ${crTab.crData[index].CID2}"
			
			crTab.crData[index].CID2 = new CRCluster (crTab.crData[index].CID2.c1, it)
			crTab.crData[index].CID_S = 1
			clusterId2Objects[crTab.crData[index].CID2] = [it]
//				println crTab.crData[index]
		}
		
//			println clusterId2Objects
		objects
	}
	
	
	public matchUndo (double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {
		
		
		if (timestampedPairs.keySet().size()==0) return
 
		Long lastTimestamp = timestampedPairs.lastKey()
		List<Pair> undoPairs = timestampedPairs.get(lastTimestamp)
		
		// redo individual mapping pairs
		undoPairs.each { Pair p -> setMapping(p.id1, p.id2, p.s, true, false)}
		
		// get relevant cluster ids and remove
		Collection<CRCluster> clusterIds = undoPairs.collect { Pair p -> crTab.crData[crId2Index[p.id1]].CID2 }
		clusterIds.addAll(undoPairs.collect { Pair p -> crTab.crData[crId2Index[p.id2]].CID2 })

		// remove last undo/able operation and re/cluster
		timestampedPairs.remove(lastTimestamp)
		clusterMatch(reInitObjects (clusterIds.unique()), matchThreshold, useVol, usePag, useDOI)
		
	}
		
	
	public void merge () {
		
		stat.setValue ("Start merging ...", 0)
				
		clusterId2Objects.eachWithIndex { cid, crlist, cidx ->
			
			stat.setValue ("Merging in progress ...", ((cidx.doubleValue()/clusterId2Objects.keySet().size().doubleValue())*100).intValue())
			
			if (crlist.size()>1) {
				
				int max_N_CR = 0
				int max_cr = 0
				int sum_N_CR = 0
	
				// sum all N_CR; find max
				crlist.each { it ->
					int idx = crId2Index[it]
					sum_N_CR += crTab.crData[idx].N_CR
					if (crTab.crData[idx].N_CR>max_N_CR) {
						max_N_CR = crTab.crData[idx].N_CR
						max_cr = idx
					}
				}
				
				// update cluster representative; invalidate all others (set mergedTo)
				crlist.each {
					int idx = crId2Index[it]
					if (idx == max_cr) {
						crTab.crData[idx].N_CR = sum_N_CR
						crTab.crData[idx].CID_S = 1
					} else {
						crTab.crData[idx].mergedTo = max_cr
					}
				}
				
			}
			
			
		}
		
		// for all CRs that will eventually be removed: add the cluster representative to the crList of each publication
		crTab.pubData.each { PubType pub ->
			pub.crList.addAll (pub.crList.findAll { CRType it -> it.mergedTo >= 0 }.collect { CRType it -> crTab.crData[it.mergedTo] })
			pub.crList.unique()	// in case, both merged CRs are in a list of the same publication
		}
		
		// remove all invalidated CRs
		crTab.crData.removeAll { CRType it -> it.mergedTo >= 0 }
		crTab.updateData(true)
		stat.setValue ("Merging done", 0, crTab.getInfoString())
		
	}
	


}
