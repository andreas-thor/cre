package cre.test.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import cre.test.data.type.CRType;
import cre.test.ui.StatusBar;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class CRMatch {


	public static enum ManualMatchType { 
		SAME ("Same"), 
		DIFFERENT ("Different"),
		EXTRACT ("Extract");
		
		public final String label;
		ManualMatchType(String label) {
			this.label = label;
		}
	};
	
	

	
	
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
	public Map <Boolean, Map<Integer, Map<Integer, Double>>> match = new HashMap<Boolean, Map<Integer, Map<Integer,Double>>>();
	private TreeMap <Long, ArrayList<Pair>> timestampedPairs;
	
	
	private Map<Integer, Integer> crId2Index = new HashMap<Integer, Integer>();						// object Id -> index in crData list
	public Map<CRCluster, Set<Integer>> clusterId2Objects = new HashMap<CRCluster, Set<Integer>>();		// clusterId->[object ids]

	public boolean hasMatches () {
		return (match.get(true).size()>0) || (match.get(false).size()>0); 
	}
	
	

	public CRMatch (CRTable crTab) {
		this.crTab = crTab;
		match = new HashMap<Boolean, Map<Integer,Map<Integer,Double>>>();
		match.put(false, new HashMap<Integer,Map<Integer,Double>>());		// automatic match result
		match.put(true,  new HashMap<Integer,Map<Integer,Double>>());		// manual match result
		timestampedPairs = new TreeMap<Long, ArrayList<Pair>>();
	}


	private void updateClusterId2Objects () {
		
		clusterId2Objects.clear();
		crTab.getCR().forEach ( cr -> {
			if (clusterId2Objects.get(cr.getCID2()) == null) {
				clusterId2Objects.put(cr.getCID2(), new HashSet<Integer>());
			}
			clusterId2Objects.get(cr.getCID2()).add (cr.getID());
		});
		
		crTab.getCR().forEach ( cr -> {
			cr.setCID_S(clusterId2Objects.get(cr.getCID2()).size());
		});

	}
	
	public void updateData (boolean removed) throws OutOfMemoryError {
		
		// refresh mapping crId -> index
		crId2Index.clear();
		
		int idx = 0;
		for (Iterator<CRType> it = crTab.crData.iterator(); it.hasNext();) {
			crId2Index.put(it.next().getID(), idx++);
		}
		
		System.out.println(System.currentTimeMillis());
		System.out.println("CRID2Index ist " + crId2Index.size());
		if (removed) {
//			println "removed"
//			println System.currentTimeMillis()
			Set<Integer> id = crId2Index.keySet();
			restrict(id);

//			println System.currentTimeMillis()

			updateClusterId2Objects();
						
			// remove deleted CRs for each publication
			// NAM crTab.pubData.forEach ( pub -> { pub.crList.removeIf ( cr -> { return cr.removed; }); });
			
			// remove pubs that have no CRs anymore
			// NAM crTab.pubData.removeIf ( pub -> { return pub.crList.size()==0; });

			
			
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
 


	public Map<Integer, Double> getMapping (Integer id1, Boolean isManual) {
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

	public boolean doBlocking () { return true; }

//	public boolean doBlocking2 () {
//		
//		
//		// standard blocking: year + first letter of last name
//		StatusBar.get().setValue(String.format("Blocking of %d objects...", CRStats.getSize()));
//		
//		Map<String, ArrayList<Integer>> blocks = new HashMap<String, ArrayList<Integer>>();	// block key -> list of indexes (not IDs)!
//		int idx = 0;
//		for (CRType cr: crTab.crData) {
//			if ((cr.getRPY() != null) && (cr.getAU_L() != null) && (cr.getAU_L().length() > 0)) {
//				String blockkey = cr.getRPY() + cr.getAU_L().substring(0,1).toLowerCase();
//				blocks.putIfAbsent(blockkey, new ArrayList<Integer>());
//				blocks.get(blockkey).add(idx);
//			}
//			idx++;
//		}
//
//		match.put(false, new HashMap<Integer,Map<Integer,Double>>());		// remove automatic match result, but preserve manual matching
//		Levenshtein l = new Levenshtein();
//		
//		AtomicLong testCount = new AtomicLong(0);
//		
//		
//		Date startdate = new Date();
//		Long stop1 = System.currentTimeMillis(); 
//		double threshold = 0.5;
//
//		// TODO: handle missing values
//		// TODO: incorporate title (from scopus)
//		
//		
//		StatusBar.get().initProgressbar(blocks.size(), String.format("Matching %d objects in %d blocks", CRStats.getSize(), blocks.size()));
//		
//		// Matching: author lastname & journal name
//		List<Pair> matchResult = blocks.entrySet().parallelStream().map( entry -> {
//
//			StatusBar.get().incProgressbar();
//
//			List<Pair> result = new ArrayList<Pair>();
//			ArrayList<Integer> crlist = entry.getValue();
//			
//			// allX = List of all AU_L values;
//			List<String> allX = crlist.stream().map ( it -> crTab.getCR(it).getAU_L().toLowerCase() ).collect (Collectors.toList());
//
//			// compareY = List of compare string 
//			ArrayList<String> compareY = new ArrayList<String>(allX);
//			
//			// ySize is used to re-adjust the index (correctIndex = ySize-yIdx-1)
//			int xIndx = 0;
//			for (String x: allX) {
//				
//				// TODO: compareY als array und dann copyof statt remove + transform
//				compareY.remove(0);	// remove first element
//				int yIndx = 0;
//				for (double s1: l.batchCompareSet(compareY.toArray(new String[compareY.size()]), x)) {
//					if (s1>=threshold) {
//
//						// the two CR to be compared
//						CRType[] comp_CR = new CRType[] { crTab.getCR(crlist.get(xIndx)), crTab.getCR(crlist.get(xIndx+yIndx+1/*ySize-yIndx-1*/)) };
//						
//						// increasing sim + weight if data is available; weight for author is 2
//						double sim = 2*s1;
//						double weight = 2;
//						
//						// compare Journal name (weight=1)
//						String[] comp_J = new String[] { comp_CR[0].getJ_N() == null ? "" : comp_CR[0].getJ_N(), comp_CR[1].getJ_N() == null ? "" : comp_CR[1].getJ_N() };
//						if ((comp_J[0].length()>0) && (comp_J[1].length()>0)) {
//							sim += 1.0* l.getSimilarity(comp_J[0].toLowerCase(), comp_J[1].toLowerCase());
//							weight += 1.0;
//						}
//						
//						// compare title (weight=5)
//						// ignore if both titles are empty; set sim=0 if just one is emtpy; compute similarity otherwise
//						String[] comp_T = new String[] { comp_CR[0].getTI() == null ? "" : comp_CR[0].getTI(), comp_CR[1].getTI() == null ? "" : comp_CR[1].getTI() };
//						if ((comp_T[0].length()>0) || (comp_T[1].length()>0)) {
//							sim += 5.0 * (((comp_T[0].length()>0) && (comp_T[1].length()>0)) ? l.getSimilarity(comp_T[0].toLowerCase(), comp_T[1].toLowerCase()) : 0.0);
//							weight += 5.0;
//						}
//						
//						double s = sim/weight;		// weighted average of AU_L, J_N, and TI
//						if (s>=threshold) {
////							setMapping(comp_CR[0].ID, comp_CR[1].ID, s, false, true);
//							testCount.incrementAndGet();
//							
//							// cannot invoke setMapping in a parallel stream -> collect result ... 
//							result.add(new Pair (comp_CR[0].getID(), comp_CR[1].getID(), s));
//						}
//					}
//					yIndx++;
//				}
//			
//				xIndx++;
//			}
//		
//			return result;})
//		.flatMap(it -> it.stream())
//        .collect(Collectors.toList());
//		
//		// ... and invoke sequentially
//		matchResult.forEach(it -> { setMapping(it.id1, it.id2, it.s, false, true); });
//		
//		
//		Long stop2 = System.currentTimeMillis();
//		System.out.println("Match time is " + ((stop2-stop1)/100) + " deci-seconds");
//		System.out.println("TestCount == " + testCount);
//		System.out.println("CRMatch> matchresult size is " + size(false));
//		StatusBar.get().setValue(String.format("%1$s: Matching done", startdate), 0);
//		
//		return updateClusterId(threshold, false, false, false, false);
//	}
	

	/**
	 *
	 * @param threshold
	 * @param useClustering
	 * @param stat
	 * @param useVol
	 * @param usePag
	 * @param useDOI
	 * @return true if okay; false otherwise
	 */
	
	public boolean updateClusterId (double threshold, boolean useClustering, boolean useVol, boolean usePag, boolean useDOI) {
		
		StatusBar.get().setValue ("Prepare clustering ...");
		
		// initialize clustering; each objects forms its own cluster
		// useClustering = true => re-use first cluster component
		
		// TODO: J8 check (groovy uses with index ...)
		
		clusterId2Objects = new HashMap<CRCluster, Set<Integer>>();
		crTab.getCR().forEach ( it -> {
			it.setCID2(useClustering ? new CRCluster (it.getCID2().c1, it.getID()) : new CRCluster (it.getID(), 1));
			it.setCID_S(1);
			clusterId2Objects.put (it.getCID2(), new HashSet<Integer>());
			clusterId2Objects.get (it.getCID2()).add(it.getID());
		});
		return clusterMatch( null, threshold, useVol, usePag, useDOI);	// null == all objects are considered for clustering
		
		
		
	}
	
	
	
	/**
	 * clustering based on match correspondences (above threshold; different cluster ids)
	 * @param id list of object ids to be considered for clustering (null=all objects)
	 * @param threshold
	 * @param stat
	 * @return true if okay; false if interrupted
	 */
	
	private boolean clusterMatch (Set<Integer> id, double threshold, boolean useVol, boolean usePag, boolean useDOI) {
		return true;
	}
	
//	private boolean clusterMatch (Set<Integer> id, double threshold, boolean useVol, boolean usePag, boolean useDOI) {
//		
//		System.out.println("ClusterMatch with Threshold " + threshold);
//		System.out.println("CRID2Index ist " + crId2Index.size());
//
//
//
//		if (id == null) {	// if no ids are given, use all ids with match pairs
//			id = new HashSet<Integer>();
//			id.addAll(match.get(false).keySet());
//			id.addAll(match.get(true).keySet());
//		}
//
//		StatusBar.get().initProgressbar(id.size(), String.format("Clustering with threshold %1$f...", threshold));
//		
//		// for all domain objects (id1)
//		for (int id1: id) {
//			
//			if (Thread.interrupted()) return false;
//			StatusBar.get().incProgressbar();
//			
//			// for all matching range objects (id2 with id1<id2) 
//			Map<Integer, Double> tmpMap1 = match.get(false).get(id1) == null ? new HashMap<Integer, Double>() : match.get(false).get(id1);
//			Map<Integer, Double> tmpMap2 = match.get(true ).get(id1) == null ? new HashMap<Integer, Double>() : match.get(true ).get(id1);
//			
//			Set<Integer> allId2 = new HashSet<Integer>(tmpMap1.keySet());
//			allId2.addAll(tmpMap2.keySet());
//			allId2.forEach( id2 -> {
//	
//				double s = -1d;	// -1 if neither in automatic nor in manual (due to redo)
//				if (tmpMap2.get(id2) != null) {
//					s = tmpMap2.get(id2);
//				} else if (tmpMap1.get(id2) != null) {
//					s = tmpMap1.get(id2);
//				}
//
//				if (s>=threshold) {
//					
//					CRType cr1 = crTab.getCR(crId2Index.get(id1));
//					CRType cr2 = crTab.getCR(crId2Index.get(id2));
//					
//					CRCluster minId = (cr1.getCID2().compareTo(cr2.getCID2())<0) ? cr1.getCID2() : cr2.getCID2();
//					CRCluster maxId = (cr1.getCID2().compareTo(cr2.getCID2())>0) ? cr1.getCID2() : cr2.getCID2();
//					
//					boolean vol = (!useVol) || ((cr1.getVOL()!=null) && (cr2.getVOL()!=null) && (cr1.getVOL().equals (cr2.getVOL()))); // || (cr1.VOL.equals("")) || (cr2.VOL.equals(""))
//					boolean pag = (!usePag) || ((cr1.getPAG()!=null) && (cr2.getPAG()!=null) && (cr1.getPAG().equals (cr2.getPAG()))); // || (cr1.PAG.equals("")) || (cr2.PAG.equals(""))
//					boolean doi = (!useDOI) || ((cr1.getDOI()!=null) && (cr2.getDOI()!=null) && (cr1.getDOI().equalsIgnoreCase (cr2.getDOI()))); // || (cr1.DOI.equals("")) || (cr2.DOI.equals(""))
//	
//					// merge if different clusters and manual-same (s==2) or all criteria are true
//					if ((minId.compareTo(maxId)!=0) && ((s==2) || ((vol) && (pag) && (doi)))) {
//						clusterId2Objects.get(minId).addAll (clusterId2Objects.get(maxId));
//						int size = clusterId2Objects.get(minId).size();
//						clusterId2Objects.get(minId).forEach( it -> { crTab.getCR(crId2Index.get(it)).setCID_S(size); });
//						clusterId2Objects.get(maxId).forEach( it -> { crTab.getCR(crId2Index.get(it)).setCID2(minId); });
//						clusterId2Objects.remove(maxId);
//					}
//				}
//			});
//		}
//		
//		StatusBar.get().setValue ("Clustering done");
//		System.out.println("OnFilter");
//		return true;
//	}
	
	
	
	/**
	 * Manual specification that the list of CRs (id) should be considered pair-wise the same/different
	 * @param id
	 * @param isSame true (same) or false (different)
	 * @param stat
	 * @param matchThreshold
	 * @param useVol
	 * @param usePag
	 * @param useDOI
	 * @throws Exception 
	 */
	public boolean matchManual (List<CRType> selCR, ManualMatchType matchType, double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {
		return true;
	}
	
//	public boolean matchManual (List<CRType> selCR, ManualMatchType matchType, double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {
//		
//		// TODO: Performanter machen
//		
//		
//		Long timestamp = System.currentTimeMillis();		// used to group together all individual mapping pairs of match operation
//		if (selCR==null) System.out.println("SelCR==null");
//		selCR.stream().forEach(cr -> { if (cr==null) System.out.println("Huch"); });
//		
//		
//		Set<Integer> crIds = selCR.stream().map(cr -> cr.getID()).collect(Collectors.toSet());
//		// System.out.println(crIds);
//		 
//		// manual-same is indicated by similarity = 2; different = -2
//		if ((matchType==ManualMatchType.SAME) || (matchType==ManualMatchType.DIFFERENT)) {
//			for (Integer id1: crIds) {
//				for (Integer id2: crIds) {
//					if (id1<id2) setMapping(id1, id2, ((matchType==ManualMatchType.SAME) ? 2d : -2d), true, false, timestamp);
//				}
//			}
//		}
//		if (matchType==ManualMatchType.EXTRACT) {
//			for (Integer id1: crIds) {
//				for (Integer id2: clusterId2Objects.get(crTab.getCR(crId2Index.get(id1)).getCID2())) {
//					setMapping(id1, id2, -2d, true, false, timestamp);
//				}
//			}
//		}
//
//		
//		if (matchType==ManualMatchType.SAME) {	// cluster using the existing cluster
//			return clusterMatch(crIds, matchThreshold, useVol, usePag, useDOI);
//		} else { 	// re-initialize the clusters that may be subject to split and rerun clustering for all objects of the clusters
//			// find all relevant clusters
//			return clusterMatch(reInitObjects (selCR.stream().map ( cr -> cr.getCID2()).collect(Collectors.toSet())), matchThreshold, useVol, usePag, useDOI);
//			
//		}
//		
//	}
	
	
	/**
	 * Re-initializes all objects of the given clusters, i.e., cluster Id is set to object id (=> individual clusters)
	 * @param clusterIds
	 * @return list of object ids
	 */
	private Set<Integer> reInitObjects (Collection<CRCluster> clusterIds) { return null; }
	
//	private Set<Integer> reInitObjects (Collection<CRCluster> clusterIds) {
//		
////		println "Clusterids ${clusterIds}"
//		Set<Integer> objects = new HashSet<Integer>();
//		
//		clusterIds.stream().forEach( clid -> { clusterId2Objects.get(clid).stream().forEach( it -> { objects.add(it); }); });
////		println "Objects ${objects}"
//		
//		// initialize clustering
//		objects.stream().forEach ( it -> {
//			int index = crId2Index.get(it);
////				println "crId = ${it}, index = ${index}, cluster = ${crTab.crData[index].CID2}"
//			
//			crTab.getCR(index).setCID2(new CRCluster (crTab.getCR(index).getCID2().c1, it));
//			crTab.getCR(index).setCID_S(1);
//			
//			Set<Integer> tmp = new HashSet<Integer>();
//			tmp.add(it);
//			clusterId2Objects.put(crTab.getCR(index).getCID2(), tmp);
//			
////				println crTab.crData[index]
//		});
//		
////			println clusterId2Objects
//		return objects;
//	}
	
	
	

	public boolean matchUndo (double matchThreshold, boolean useVol, boolean usePag, boolean useDOI)  {
		return true;
	}
	
//	public boolean matchUndo (double matchThreshold, boolean useVol, boolean usePag, boolean useDOI)  {
//		
//		
//		if (timestampedPairs.keySet().size()==0) return true;
// 
//		Long lastTimestamp = timestampedPairs.lastKey();
//		List<Pair> undoPairs = timestampedPairs.get(lastTimestamp);
//		
//		// redo individual mapping pairs
//		undoPairs.forEach (p -> setMapping(p.id1, p.id2, p.s, true, false));
//		
//		// get relevant cluster ids and remove
//		Set<CRCluster> clusterIds = undoPairs.stream().map (p -> crTab.getCR(crId2Index.get(p.id1)).getCID2() ).collect(Collectors.toSet());
//		clusterIds.addAll(undoPairs.stream().map (p -> crTab.getCR(crId2Index.get(p.id2)).getCID2() ).collect(Collectors.toSet()));
//
//		// remove last undo/able operation and re/cluster
//		timestampedPairs.remove(lastTimestamp);
//		return clusterMatch(reInitObjects (clusterIds), matchThreshold, useVol, usePag, useDOI);
//		
//	}
		
	
	public void merge () { }
	
//	public void merge () {
//		
//		StatusBar.get().initProgressbar(clusterId2Objects.size(), String.format("Merging %d clusters...", clusterId2Objects.size()));
//				
//		for (Map.Entry<CRCluster, Set<Integer>> entry: clusterId2Objects.entrySet()) {
////		clusterId2Objects.eachWithIndex { cid, crlist, cidx ->
//
//			StatusBar.get().incProgressbar();
//			
//			if (entry.getValue().size()>1) {
//				
//				int max_N_CR = 0;
//				CRType crMax = null;
//				CRType cr;
////				int sum_N_CR = 0;
//	
//				// sum all N_CR; find max
//				for (Integer it: entry.getValue()) {
////					int idx = crId2Index.get(it);
//					cr = crTab.getCR(crId2Index.get(it));
////					sum_N_CR += crTab.getCR(idx).getN_CR();
//					if (cr.getN_CR()>max_N_CR) {
//						max_N_CR = cr.getN_CR();
//						crMax = cr;
//					}
//				};
//				
//				final CRType crMax2 = crMax;
//				// update cluster representative; invalidate all others (set mergedTo)
//				for (Integer it: entry.getValue()) {
////					int idx = crId2Index.get(it);
//					final CRType crDel = crTab.getCR(crId2Index.get(it));
//					
//					if (crDel == crMax) {
////						crTab.getCR(idx).setN_CR(sum_N_CR);
//						crDel.setCID_S(1);
//					} else {
//						crDel.getPub().forEach ( pub -> { 
//							crMax2.addPub(pub, true);
//						});
//						crDel.removeAllPubs(true); 
//					}
//				};
//				
//			}
//		}
//		
//		/*
//		// for all CRs that will eventually be removed: add the cluster representative to the crList of each publication and remove to-be-removed CRs
//		for (PubType pub: crTab.pubData) {
//			
//			final PubType p = pub;
//			
//			// change to-be-deleted-CRs to CR cluster representative & group by CR  
//			Map<CRType, Long> result = pub.crList.stream()
//					.map ( it -> it.mergedTo < 0 ? it : crTab.getCR(it.mergedTo))
//					.collect( Collectors.groupingBy( Function.identity(), Collectors.counting() ));
//			
//			// decrease N_CR in case multiple CRs are merged to the same publication
//			result.forEach((cr, count) -> {
//				if (count>1) {
////					cr.setN_CR((int) (cr.getN_CR()-count+1));
//					
//					
//					if (cr.getID()==65) {
//						System.out.println("Doppelte Referenzierung");
//						System.out.println(p.PY);
//						System.out.println("\n");
//					}
//					
//				}
//			});
//			
////			pub.crList = new ArrayList<CRType>(result.keySet());
//			pub.crList = new HashSet<CRType>(result.keySet());
//			
//			
//			
////			pub.crList.addAll (
////				pub.crList.stream()
////					.filter ( it -> it.mergedTo >= 0)
////					.map ( it -> crTab.getCR(it.mergedTo))
////					.collect(Collectors.toList())
////			);
//			
////			if (pub.crList.size() != (new ArrayList<CRType>(new HashSet<CRType>(pub.crList))).size()) {
////				
////				System.out.println("Hier");
////				
////			}
//			
////			pub.crList = (ArrayList<CRType>) pub.crList.stream().distinct().collect(Collectors.toList());
////			pub.crList = new ArrayList<CRType>(new HashSet<CRType>(pub.crList));	// in case, both merged CRs are in a list of the same publication
////			pub.crList.removeIf(it -> { return (it.mergedTo >= 0); });				// remove CRs that will be removed
//		
//		}
//		*/
//		// remove all invalidated CRs
//		crTab.crData.removeIf ( it -> it.getN_CR()<1 );
//		crTab.updateData(true);
//		StatusBar.get().setValue ("Merging done"); 
//		
//	}
	


}
