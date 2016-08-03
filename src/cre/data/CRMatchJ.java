package cre.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.orsoncharts.util.json.JSONObject;

import cre.ui.StatusBar;
import cre.ui.UIMatchPanelFactory;
import groovy.json.JsonBuilder;
import groovy.transform.CompileStatic;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

@CompileStatic
class CRMatchJ {


	class Pair {
		Integer id1;
		Integer id2;
		Double s;
		public Pair(Integer id1, Integer id2, Double s) {
			super();
			this.id1 = id1;
			this.id2 = id2;
			this.s = s;
		}
		
		public Pair (String s) {
			String[] split = s.split ("/");
			this.id1 = Integer.valueOf(split[0]);
			this.id2 = Integer.valueOf(split[1]);
			this.s = Double.valueOf(split[2]);
		}
		
		public String toString () {
			return (this.id1 + "/" + this.id2 + "/" + this.s);
		}
		
	}
	
	private CRTable crTab;
	private StatusBar stat;
	private Map <Boolean, Map<Integer, Map<Integer, Double>>> match = new HashMap<Boolean, Map<Integer, Map<Integer,Double>>>();
	private TreeMap <Long, ArrayList<Pair>> timestampedPairs;
	
	
	private Map<Integer, Integer> crId2Index = new HashMap<Integer, Integer>();						// object Id -> index in crData list
	public Map<CRCluster, List<Integer>> clusterId2Objects = new HashMap<CRCluster, List<Integer>>();		// clusterId->[object ids]

	public boolean hasMatches () {
		return (match.get(true).size()>0) || (match.get(false).size()>0); 
	}
	
	public JSONObject getJSON() {
		
		// TODO: J8 implement
		return null;
		
//		JsonBuilder jb = new JsonBuilder()
//		
//		jb (
//			MATCH_AUTO: match[false].collect { Integer key, Map<Integer, Double> val -> val.collect { Integer k, Double v -> [key, k, v]}  },
//			MATCH_MANU: match[true].collect { Integer key, Map<Integer, Double> val -> val.collect { Integer k, Double v -> [key, k, v]}  }
//		) as JSONObject
		
		
	}
	
	public void parseJSON (JSONObject j) {

		// TODO: J8 implement

//		j.MATCH_AUTO.each { it.each { triple -> List l = triple as List; setMapping (l[0] as int, l[1] as int, l[2] as double, false, false) } }   
//		j.MATCH_MANU.each { it.each { triple -> List l = triple as List; setMapping (l[0] as int, l[1] as int, l[2] as double, true, false) } }   
//		
//		updateData (false)	// updates crId2Index
//		updateClusterId2Objects()	// updates clusterId2Objects
	}
	
	

	public CRMatchJ (CRTable crTab, StatusBar stat) {
		this.crTab = crTab;
		this.stat = stat;
		match = new HashMap<Boolean, Map<Integer,Map<Integer,Double>>>();
		match.put(false, new HashMap<Integer,Map<Integer,Double>>());		// automatic match result
		match.put(true,  new HashMap<Integer,Map<Integer,Double>>());		// manual match result
		timestampedPairs = new TreeMap<Long, ArrayList<Pair>>();
	}


	private void updateClusterId2Objects () {
		
		clusterId2Objects.clear();
		crTab.crData.forEach ( cr -> {
			if (clusterId2Objects.get(cr.CID2) == null) {
				clusterId2Objects.put(cr.CID2, new ArrayList<Integer>());
			}
			clusterId2Objects.get(cr.CID2).add (cr.ID);
		});
		
		crTab.crData.forEach ( cr -> {
			cr.CID_S = clusterId2Objects.get(cr.CID2).size();
		});

	}
	
	public void updateData (boolean removed) throws OutOfMemoryError {
		
		// refresh mapping crId -> index
		crId2Index.clear();
		
		int idx = 0;
		for (Iterator<CRType> it = crTab.crData.iterator(); it.hasNext();) {
			crId2Index.put(it.next().ID, idx++);
		}
		
		System.out.println(System.currentTimeMillis());
		
		if (removed) {
//			println "removed"
//			println System.currentTimeMillis()
			Set<Integer> id = crId2Index.keySet();
			restrict(id);

//			println System.currentTimeMillis()

			updateClusterId2Objects();
						
			// remove deleted CRs for each publication
			crTab.pubData.forEach ( pub -> { pub.crList.removeIf ( cr -> { return cr.removed; }); });
			
			// remove pubs that have no CRs anymore
			crTab.pubData.removeIf ( pub -> { return pub.crList.size()==0; });

			
			
//			println System.currentTimeMillis()
//			println "removed done"
		}
	}



	/**
	 * Restrict match result to list of given ids
	 * Called when user deletes CRs
	 * @param id
	 */
	public void restrict (Set<Integer> id) {

		for (boolean b: new boolean[] {true, false}) {
			match.get(b).entrySet().removeIf(entry -> !id.contains(entry.getKey()) );
			match.get(b).entrySet().forEach(entry -> {
				entry.getValue().entrySet().removeIf(entry2 -> !id.contains(entry2.getKey()) );
			});
		}
	}

	public void setMapping (Integer id1, Integer id2, Double s, boolean isManual, boolean add) {
		setMapping(id1, id2, s, isManual, add, null);
	}

	public void setMapping (Integer id1, Integer id2, Double s, boolean isManual, boolean add, Long timestamp) {

		if (id1.equals(id2)) return;
		
		// swap if needed so that always holds id1<id2
		if (id1.compareTo(id2)>0) {
			Integer temp = id1;
			id1 = id2;
			id2 = temp;
		}

		match.get( isManual).putIfAbsent(id1, new HashMap<Integer, Double>());
		match.get(!isManual).putIfAbsent(id1, new HashMap<Integer, Double>());

		// store old value for undo operation of manual mappings
		if ((isManual) && (timestamp!=null)) {
			timestampedPairs.putIfAbsent(timestamp, new ArrayList<Pair>());
			timestampedPairs.get(timestamp).add(new Pair(id1, id2, (match.get(isManual)).get(id1).get(id2)));
		}

		// update value
		double v = add ? match.get(isManual).get(id1).getOrDefault(id2, 0d) : 0d;
		match.get(isManual).get(id1).put(id2, (s==null) ? null : s+v);
	}



	public Map getMapping (Integer id1, Boolean isManual) {
		return match.get(isManual).getOrDefault(id1, new HashMap<Integer, Double>());
	}


	public void clear () {
		match.put(false, new HashMap<Integer,Map<Integer,Double>>());		
		match.put(true,  new HashMap<Integer,Map<Integer,Double>>());		
		clusterId2Objects.clear();
	}

	
	public int size (boolean isManual) {
		return match.get(isManual).entrySet().stream().map ( entry -> entry.getValue().size()).reduce(0, (a,b) -> a+b);
	}

	public int getNoOfClusters () {
		return clusterId2Objects.keySet().size();
	}

	/**
	 *
	 * @param matchers  Array of matchers; each matcher has three components: [attribute, simfunc, threshold]
	 * @param globalThreshold
	 * @param useClustering
	 * @throws Exception 
	 */
	
	public void doBlocking () throws Exception {
		
		// standard blocking: year + first letter of last name
		stat.setValue(String.format("%1$s: Start Blocking of ${crTab.crData.size()} objects", new Date()), 0);
		Map<String, ArrayList<Integer>> blocks = new HashMap<String, ArrayList<Integer>>();	// block key -> list of indexes (not IDs)!
		int idx = 0;
		for (CRType cr: crTab.crData) {
			if ((cr.RPY != null) && (cr.AU_L != null) && (cr.AU_L.length() > 0)) {
				String blockkey = cr.RPY + cr.AU_L.substring(0,1).toLowerCase();
				blocks.putIfAbsent(blockkey, new ArrayList<Integer>());
				blocks.get(blockkey).add(idx);
			}
			idx++;
		}

		System.out.println(String.format("%1$s: Blocking done (%2$d blocks)", new Date(), blocks.size()));
		
		match.put(false, new HashMap<Integer,Map<Integer,Double>>());		// remove automatic match result, but preserve manual matching
		Levenshtein l = new Levenshtein();
		long progMax = blocks.size();
		AtomicLong progCount = new AtomicLong(0);
		
		Date startdate = new Date();
		double threshold = 0.5;

		// TODO: handle missing values
		// TODO: incorporate title (from scopus)
		
		// Matching: author lastname & journal name
		blocks.entrySet().stream().forEach( entry -> {
			
		
			String b = entry.getKey();
			ArrayList<Integer> crlist = entry.getValue();
			
			progCount.incrementAndGet();
			stat.setValue(String.format("%1$s: Matching in progress ...", startdate), (int) ((100d*progCount.get())/progMax));
			
			// allX = List of all AU_L values;
			List<String> allX = crlist.stream().map ( it -> crTab.crData.get(it).AU_L.toLowerCase() ).collect (Collectors.toList());

			// compareY = List of compare string is in reverse order, i.e., pop() (takes last element) actually removes the "logical" first element
			ArrayList<String> compareY = new ArrayList<String>(allX);
			Collections.reverse(compareY);
			
			// ySize is used to re-adjust the index (correctIndex = ySize-yIdx-1)
			int ySize = compareY.size();

			int xIndx = 0;
			for (String x: allX) {
				
				// TODO: compareY als array und dann copyof statt pop+transform
				compareY.remove(0);
				int yIndx = 0;
				for (double s1: l.batchCompareSet(compareY.toArray(new String[compareY.size()]), x)) {
					if (s1>=threshold) {

						// the two CR to be compared
						CRType[] comp_CR = new CRType[] { crTab.crData.get(crlist.get(xIndx)), crTab.crData.get(crlist.get(ySize-yIndx-1)) };
						
						// increasing sim + weight if data is available; weight for author is 2
						double sim = 2*s1;
						double weight = 2;
						
						// compare Journal name (weight=1)
						String[] comp_J = new String[] { comp_CR[0].J_N == null ? "" : comp_CR[0].J_N, comp_CR[1].J_N == null ? "" : comp_CR[1].J_N };
						if ((comp_J[0].length()>0) && (comp_J[1].length()>0)) {
							sim += 1.0* l.getSimilarity(comp_J[0].toLowerCase(), comp_J[1].toLowerCase());
							weight += 1.0;
						}
						
						// compare title (weight=5)
						// ignore if both titles are empty; set sim=0 if just one is emtpy; compute similarity otherwise
						String[] comp_T = new String[] { comp_CR[0].TI == null ? "" : comp_CR[0].TI, comp_CR[1].TI == null ? "" : comp_CR[1].TI };
						if ((comp_T[0].length()>0) || (comp_T[1].length()>0)) {
							sim += 5.0 * (((comp_T[0].length()>0) && (comp_T[1].length()>0)) ? l.getSimilarity(comp_T[0].toLowerCase(), comp_T[1].toLowerCase()) : 0.0);
							weight += 5.0;
						}
						
						double s = sim/weight;		// weighted average of AU_L, J_N, and TI
						if (s>=threshold) {
							setMapping(comp_CR[0].ID, comp_CR[1].ID, s, false, true);
						}
					}
					yIndx++;
				}
			
				xIndx++;
			}
		
		});
		
		System.out.println("CRMatch> matchresult size is " + size(false));
		stat.setValue(String.format("%1$s: Matching done", startdate), 0);
		
		updateClusterId(threshold, false, false, false, false);
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
		
		stat.setValue ("${new Date()}: Prepare clustering ...", 0);
		
		// initialize clustering; each objects forms its own cluster
		// useClustering = true => re-use first cluster component
		
		// TODO: J8 check (groovy uses with index ...)
		
		clusterId2Objects = new HashMap<CRCluster, List<Integer>>();
		crTab.crData.forEach ( it -> {
			it.CID2 = useClustering ? new CRCluster (it.CID2.c1, it.ID) : new CRCluster (it.ID, 1);
			it.CID_S = 1;
			clusterId2Objects.put (it.CID2, new ArrayList<Integer>(Arrays.asList(it.ID)));
		});
		clusterMatch( null, threshold, useVol, usePag, useDOI);	// null == all objects are considered for clustering
		
		
		
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
					
					boolean vol = (!useVol) || (cr1.VOL?.equals (cr2.VOL)) // || (cr1.VOL.equals("")) || (cr2.VOL.equals(""))
					boolean pag = (!usePag) || (cr1.PAG?.equals (cr2.PAG)) // || (cr1.PAG.equals("")) || (cr2.PAG.equals(""))
					boolean doi = (!useDOI) || (cr1.DOI?.equalsIgnoreCase (cr2.DOI)) // || (cr1.DOI.equals("")) || (cr2.DOI.equals(""))
	
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
	
	
	public void matchUndo (double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {
		
		
		if (timestampedPairs.keySet().size()==0) return;
 
		Long lastTimestamp = timestampedPairs.lastKey();
		List<Pair> undoPairs = timestampedPairs.get(lastTimestamp);
		
		// redo individual mapping pairs
		undoPairs.forEach (p -> setMapping(p.id1, p.id2, p.s, true, false));
		
		// get relevant cluster ids and remove
		Collection<CRCluster> clusterIds = undoPairs.collect { Pair p -> crTab.crData[crId2Index[p.id1]].CID2 }
		clusterIds.addAll(undoPairs.collect { Pair p -> crTab.crData[crId2Index[p.id2]].CID2 })

		// remove last undo/able operation and re/cluster
		timestampedPairs.remove(lastTimestamp)
		clusterMatch(reInitObjects (clusterIds.unique()), matchThreshold, useVol, usePag, useDOI)
		
	}
		
	
	public void merge () {
		
		stat.setValue ("Start merging ...", 0);
				
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
