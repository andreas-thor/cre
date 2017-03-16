package cre.test.data.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import cre.test.data.CRStats;
import cre.test.data.CRTable;
import cre.test.data.match.CRMatch.ManualMatchType;
import cre.test.data.type.CRType;
import cre.test.ui.StatusBar;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class CRMatch2 {

	private static CRMatch2 crmatch = null;
	
	public static enum ManualMatchType2 { 
		SAME ("Same"), 
		DIFFERENT ("Different"),
		EXTRACT ("Extract");
		
		public final String label;
		ManualMatchType2(String label) {
			this.label = label;
		}
	
	};
	
	/**
	 * matchResult: M -> (CR1 -> (CR2 -> sim)
	 * match pair (CR1, CR2, sim) with CR1.ID<CR2.ID
	 * M=true --> manual match; M=false --> automatic match
	 * sim=similarity : -2=manual NON MATCH, +2=manual MATCH, in [0,1]=automatic match
	 */
	private Map <Boolean, Map<CRType, Map<CRType, Double>>> matchResult;
	private TreeMap <Long, ArrayList<CRPair2>> timestampedPairs;

	
	/**
	 * clusterResult
	 */
	
	
	
	private CRMatch2 () {
		matchResult = new HashMap<Boolean, Map<CRType, Map<CRType,Double>>>();
		matchResult.put(false, new HashMap<CRType,Map<CRType,Double>>());		// automatic match result
		matchResult.put(true,  new HashMap<CRType,Map<CRType,Double>>());		// manual match result
		timestampedPairs = new TreeMap<Long, ArrayList<CRPair2>>();
	}
	
	public static CRMatch2 get() {
		
		if (crmatch==null) {
			crmatch = new CRMatch2();
		}
	 	return crmatch;
	}
	
	
	public void generateAutoMatching () {
	
		// parameters
		final double threshold = 0.5;
		final double weight_author = 2.0;
		final double weight_journal = 1.0;
		final double weight_title = 5.0;
		
		// standard blocking: year + first letter of last name
		StatusBar.get().setValue(String.format("Blocking of %d objects...", CRStats.getSize()));
		Map<String, List<CRType>> blocks = CRTable.get().getCR().collect(Collectors.groupingBy(
			cr -> ((cr.getRPY() != null) && (cr.getAU_L() != null) && (cr.getAU_L().length() > 0)) ? cr.getRPY() + cr.getAU_L().substring(0,1).toLowerCase() : "",
			Collectors.toList()
		));

		
		StatusBar.get().initProgressbar(blocks.size(), String.format("Matching %d objects in %d blocks", CRStats.getSize(), blocks.size()));
		matchResult.put(false, new HashMap<CRType,Map<CRType,Double>>());		// remove automatic match result, but preserve manual matching
		Levenshtein l = new Levenshtein();
		
		AtomicLong testCount = new AtomicLong(0);
		Long stop1 = System.currentTimeMillis(); 
		
		// TODO: handle missing values
		// TODO: incorporate title (from scopus)
		
		
		// Matching: author lastname & journal name
		List<CRPair2> matchResult = blocks.entrySet().parallelStream().map ( entry -> {

			StatusBar.get().incProgressbar();

			List<CRPair2> result = new ArrayList<CRPair2>();
			if (entry.getKey().equals("")) return result;	// non-matchable block 

			List<CRType> crlist = entry.getValue();
			
			// allX = List of all AU_L values; compareY = List of compare string 
			List<String> allX = crlist.stream().map ( cr -> cr.getAU_L().toLowerCase()).collect (Collectors.toList());
			ArrayList<String> compareY = new ArrayList<String>(allX);
			
			// ySize is used to re-adjust the index (correctIndex = ySize-yIdx-1)
			int xIndx = 0;
			for (String x: allX) {
				
				// TODO: compareY als array und dann copyof statt remove + transform
				compareY.remove(0);	// remove first element
				int yIndx = 0;
				for (double s1: l.batchCompareSet(compareY.toArray(new String[compareY.size()]), x)) {
					if (s1>=threshold) {

						// the two CRs to be compared
						CRType[] comp_CR = new CRType[] { crlist.get(xIndx), crlist.get(xIndx+yIndx+1/*ySize-yIndx-1*/) };
						
						// increasing sim + weight if data is available; weight for author is 2
						double sim = weight_author*s1;
						double weight = weight_author;
						
						// compare Journal name (weight=1)
						String[] comp_J = new String[] { comp_CR[0].getJ_N() == null ? "" : comp_CR[0].getJ_N(), comp_CR[1].getJ_N() == null ? "" : comp_CR[1].getJ_N() };
						if ((comp_J[0].length()>0) && (comp_J[1].length()>0)) {
							sim += weight_journal* l.getSimilarity(comp_J[0].toLowerCase(), comp_J[1].toLowerCase());
							weight += weight_journal;
						}
						
						// compare title (weight=5)
						// ignore if both titles are empty; set sim=0 if just one is emtpy; compute similarity otherwise
						String[] comp_T = new String[] { comp_CR[0].getTI() == null ? "" : comp_CR[0].getTI(), comp_CR[1].getTI() == null ? "" : comp_CR[1].getTI() };
						if ((comp_T[0].length()>0) || (comp_T[1].length()>0)) {
							sim += weight_title * (((comp_T[0].length()>0) && (comp_T[1].length()>0)) ? l.getSimilarity(comp_T[0].toLowerCase(), comp_T[1].toLowerCase()) : 0.0);
							weight += weight_title;
						}
						
						double s = sim/weight;		// weighted average of AU_L, J_N, and TI
						if (s>=threshold) {
							// cannot invoke setMapping in a parallel stream -> collect result ... 
							result.add(new CRPair2 (comp_CR[0], comp_CR[1], s));
							testCount.incrementAndGet();
						}
					}
					yIndx++;
				}
				xIndx++;
			}
		
			return result;
		})
		.flatMap(it -> it.stream())
        .collect(Collectors.toList());
		
		// ... and invoke sequentially
		matchResult.forEach(it -> { addPair(it, false, true, null); });
		
		
		Long stop2 = System.currentTimeMillis();
		System.out.println("Match time is " + ((stop2-stop1)/100) + " deci-seconds");
		System.out.println("TestCount == " + testCount);
		System.out.println("CRMatch> matchresult size is " + getSize(false));
		StatusBar.get().setValue("Matching done");
		
//		return updateClusterId(threshold, false, false, false, false);		
		
	}
	
	
	public boolean matchManual (List<CRType> selCR, ManualMatchType matchType, double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {
		
		// TODO: Performanter machen
		
		
		Long timestamp = System.currentTimeMillis();		// used to group together all individual mapping pairs of match operation
		if (selCR==null) System.out.println("SelCR==null");
		selCR.stream().forEach(cr -> { if (cr==null) System.out.println("Huch"); });
		
		// manual-same is indicated by similarity = 2; different = -2
		if ((matchType==ManualMatchType.SAME) || (matchType==ManualMatchType.DIFFERENT)) {
			double sim = (matchType==ManualMatchType.SAME) ? 2d : -2d;
			for (CRType cr1: selCR) {
				for (CRType cr2: selCR) {
					if (cr1.getID()<cr2.getID()) addPair(new CRPair2 (cr1, cr2, sim), true, false, timestamp);
				}
			}
		}
		if (matchType==ManualMatchType.EXTRACT) {
			double sim = -2d;
			for (Integer id1: crIds) {
				for (Integer id2: clusterId2Objects.get(crTab.getCR(crId2Index.get(id1)).getCID2())) {
					setMapping(id1, id2, -2d, true, false, timestamp);
				}
			}
		}

		
		if (matchType==ManualMatchType.SAME) {	// cluster using the existing cluster
			return clusterMatch(crIds, matchThreshold, useVol, usePag, useDOI);
		} else { 	// re-initialize the clusters that may be subject to split and rerun clustering for all objects of the clusters
			// find all relevant clusters
			return clusterMatch(reInitObjects (selCR.stream().map ( cr -> cr.getCID2()).collect(Collectors.toSet())), matchThreshold, useVol, usePag, useDOI);
			
		}
		
	}	
	
	
	public int getSize (boolean isManual) {
		return matchResult.get(isManual).entrySet().stream().map ( entry -> entry.getValue().size()).reduce(0, (a,b) -> a+b);
	}
	
	public void addPair (CRPair2 matchPair, boolean isManual, boolean add, Long timestamp) {

		if (matchPair.cr1==matchPair.cr2) return;
		
		matchResult.get( isManual).putIfAbsent(matchPair.cr1, new HashMap<CRType, Double>());
		matchResult.get(!isManual).putIfAbsent(matchPair.cr1, new HashMap<CRType, Double>());

		// store old value for undo operation of manual mappings
		if ((isManual) && (timestamp!=null)) {
			timestampedPairs.putIfAbsent(timestamp, new ArrayList<CRPair2>());
			timestampedPairs.get(timestamp).add(new CRPair2(matchPair.cr1, matchPair.cr2, (matchResult.get(isManual)).get(matchPair.cr1).get(matchPair.cr2)));
		}

		// update value
		double v = add ? matchResult.get(isManual).get(matchPair.cr1).getOrDefault(matchPair.cr2, 0d) : 0d;
		matchResult.get(isManual).get(matchPair.cr1).put(matchPair.cr2, (matchPair.s==null) ? null : matchPair.s+v);
	}
	
	
}
