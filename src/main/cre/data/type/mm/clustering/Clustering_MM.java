package main.cre.data.type.mm.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.Clustering;
import main.cre.data.type.mm.CRTable_MM;
import main.cre.data.type.mm.CRType_MM;
import main.cre.data.type.mm.PubType_MM;
import main.cre.ui.statusbar.StatusBar;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class Clustering_MM extends Clustering<CRType_MM, PubType_MM> {

	
	/**
	 * matchResult: M -> (CR1 -> (CR2 -> sim)
	 * match pair (CR1, CR2, sim) with CR1.ID<CR2.ID
	 * M=true --> manual match; M=false --> automatic match
	 * sim=similarity : -2=manual NON MATCH, +2=manual MATCH, in [0,1]=automatic match
	 */
	public Map <Boolean, Map<CRType_MM, Map<CRType_MM, Double>>> matchResult;
	private TreeMap <Long, ArrayList<CRPair>> timestampedPairs;

	private CRTable_MM crTab;
	
	
	public Clustering_MM (CRTable_MM crTab) {
		this.crTab = crTab;
		init();
	}
	
	
	
	public void init() {
		matchResult = new HashMap<Boolean, Map<CRType_MM, Map<CRType_MM,Double>>>();
		matchResult.put(false, new HashMap<CRType_MM,Map<CRType_MM,Double>>());		// automatic match result
		matchResult.put(true,  new HashMap<CRType_MM,Map<CRType_MM,Double>>());		// manual match result
		timestampedPairs = new TreeMap<Long, ArrayList<CRPair>>();
	}
	
	@Override
	public void generateAutoMatching () {
	
		// parameters
		final double threshold = 0.5;
		final double weight_author = 2.0;
		final double weight_journal = 1.0;
		final double weight_title = 5.0;
		
		// standard blocking: year + first letter of last name
		StatusBar.get().setValue(String.format("Blocking of %d objects...", CRTable.get().getStatistics().getNumberOfCRs()));
		Map<String, List<CRType_MM>> blocks = crTab.getCR().collect(Collectors.groupingBy(
			cr -> ((cr.getRPY() != null) && (cr.getAU_L() != null) && (cr.getAU_L().length() > 0)) ? cr.getRPY() + cr.getAU_L().substring(0,1).toLowerCase() : "",
			Collectors.toList()
		));

		StatusBar.get().initProgressbar(blocks.entrySet().stream().mapToInt(entry -> (entry.getValue().size()*(entry.getValue().size()-1))/2).sum(), String.format("Matching %d objects in %d blocks", CRTable.get().getStatistics().getNumberOfCRs(), blocks.size()));
		matchResult.put(false, new HashMap<CRType_MM,Map<CRType_MM,Double>>());		// remove automatic match result, but preserve manual matching
		Levenshtein l = new Levenshtein();
		
		AtomicLong testCount = new AtomicLong(0);
		Long stop1 = System.currentTimeMillis(); 
		
		// TODO: handle missing values
		// TODO: incorporate title (from scopus)
		
		
		// Matching: author lastname & journal name
		List<CRPair> matchResult = blocks.entrySet().parallelStream().map ( entry -> {

			StatusBar.get().incProgressbar(entry.getValue().size()*(entry.getValue().size()-1)/2);
			
			List<CRPair> result = new ArrayList<CRPair>();
			if (entry.getKey().equals("")) return result;	// non-matchable block 

			List<CRType_MM> crlist = entry.getValue();
			
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
						CRType_MM[] comp_CR = new CRType_MM[] { crlist.get(xIndx), crlist.get(xIndx+yIndx+1/*ySize-yIndx-1*/) };
						
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
							result.add(new CRPair (comp_CR[0], comp_CR[1], s));
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
		
		assert testCount.get() == getNumberOfMatches(false);
		
		StatusBar.get().setValue("Matching done");
		updateClustering(Clustering.ClusteringType.INIT, null, threshold, false, false, false);
		
	}
	
	@Override
	public void addManuMatching (List<CRType_MM> selCR, Clustering.ManualMatchType matchType, double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {
		
		
		assert selCR != null;
		assert selCR.stream().filter(cr -> cr==null).count() == 0;
		
		Long timestamp = System.currentTimeMillis();		// used to group together all individual mapping pairs of match operation
		
		// manual-same is indicated by similarity = 2; different = -2
		if ((matchType==Clustering.ManualMatchType.SAME) || (matchType==Clustering.ManualMatchType.DIFFERENT)) {
			double sim = (matchType==Clustering.ManualMatchType.SAME) ? 2d : -2d;
			for (CRType_MM cr1: selCR) {
				for (CRType_MM cr2: selCR) {
					if (cr1.getID()<cr2.getID()) addPair(new CRPair (cr1, cr2, sim), true, false, timestamp);
				}
			}
		}

		if (matchType==Clustering.ManualMatchType.EXTRACT) {
			for (CRType_MM cr1: selCR) {
				cr1.getCluster().getCR().filter(cr2 -> cr1!=cr2).forEach(cr2 -> addPair (new CRPair (cr1, cr2, -2d), true, false, timestamp));
			}
		}
		
		
		Set<CRType_MM> changeCR = selCR.stream().flatMap(cr -> cr.getCluster().getCR()).distinct().collect(Collectors.toSet());
		
		updateClustering(Clustering.ClusteringType.REFRESH, changeCR, matchThreshold, useVol, usePag, useDOI);
	}	
	

	@Override
	public void undoManuMatching (double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {
		
		// check if undo-able operations are available
		if (timestampedPairs.keySet().size()==0) return;

		// copy old values and remove last undo/able operation 
		Long lastTimestamp = timestampedPairs.lastKey();
		List<CRPair> undoPairs = timestampedPairs.get(lastTimestamp);
		undoPairs.forEach(pair -> addPair(pair, true, false, null));
		
		// get changed CRs and update clustering
		Set<CRType_MM> changeCR = undoPairs.stream().map(pair -> pair.cr1).distinct().collect(Collectors.toSet());
		changeCR.addAll(undoPairs.stream().map(pair -> pair.cr2).distinct().collect(Collectors.toSet()));
		updateClustering(Clustering.ClusteringType.REFRESH, changeCR, matchThreshold, useVol, usePag, useDOI);
	}
	
	
	@Override
	public void updateClustering (Clustering.ClusteringType type, Set<CRType_MM> changeCR, double threshold, boolean useVol, boolean usePag, boolean useDOI) {
		

		int pbSize = matchResult.get(false).size()+matchResult.get(true).size();
		
		if (type == Clustering.ClusteringType.INIT) {	// consider manual (automatic?) matches only
			crTab.getCR().forEach(cr -> cr.setCluster(new CRCluster(cr)));
			pbSize = matchResult.get(false).size();
		}
		
		if (type == Clustering.ClusteringType.REFRESH) {
			((changeCR == null) ? crTab.getCR() : changeCR.stream()).forEach(cr -> cr.setCluster (new CRCluster(cr, cr.getCluster().getC1())));
		}

		StatusBar.get().initProgressbar(pbSize, String.format("Clustering %d objects (%s) with threshold %.2f", CRTable.get().getStatistics().getNumberOfCRs(), type.label, threshold));
		
		// automatic matches
		matchResult.get(false).forEach((cr1, pairs) -> {
			
			StatusBar.get().incProgressbar();
			
			pairs.forEach((cr2, sim) -> {
				
				boolean changed = (changeCR == null) || (changeCR.contains(cr1) && changeCR.contains(cr2));
				boolean manualDifferent = (matchResult.get(true).get(cr1) != null) && (matchResult.get(true).get(cr1).get(cr2) != null) && (matchResult.get(true).get(cr1).get(cr2) == -2d);
				
				if (changed && (cr1.getCluster() != cr2.getCluster()) && (sim >= threshold) && !manualDifferent) {
					
					boolean vol = (!useVol) || ((cr1.getVOL()!=null) && (cr2.getVOL()!=null) && (cr1.getVOL().equals (cr2.getVOL()))); // || (cr1.VOL.equals("")) || (cr2.VOL.equals(""))
					boolean pag = (!usePag) || ((cr1.getPAG()!=null) && (cr2.getPAG()!=null) && (cr1.getPAG().equals (cr2.getPAG()))); // || (cr1.PAG.equals("")) || (cr2.PAG.equals(""))
					boolean doi = (!useDOI) || ((cr1.getDOI()!=null) && (cr2.getDOI()!=null) && (cr1.getDOI().equalsIgnoreCase (cr2.getDOI()))); // || (cr1.DOI.equals("")) || (cr2.DOI.equals(""))
					
					if (vol && pag && doi) {
						cr1.getCluster().merge(cr2.getCluster());
					}
				}
			});
		});

		// add manual matches
		if (type != Clustering.ClusteringType.INIT) {
			matchResult.get(true).forEach((cr1, pairs) -> {
				StatusBar.get().incProgressbar();
				pairs.forEach((cr2, sim) -> {
					boolean changed = (changeCR == null) || (changeCR.contains(cr1) && changeCR.contains(cr2));
					if (changed && (sim!=null) && (sim==2d)) {
						cr1.getCluster().merge(cr2.getCluster());
					}
				});
			});
		}
		
		
		StatusBar.get().setValue("Clustering done");

		
	}
	
	

	
	
	
	public void addPair (CRType_MM cr1, CRType_MM cr2, double s, boolean isManual, boolean add, Long timestamp) {
		addPair(new CRPair (cr1, cr2, s), isManual, false, null);
	}

	private void addPair (CRPair matchPair, boolean isManual, boolean add, Long timestamp) {

		if (matchPair.cr1==matchPair.cr2) return;
		
		matchResult.get( isManual).putIfAbsent(matchPair.cr1, new HashMap<CRType_MM, Double>());
		matchResult.get(!isManual).putIfAbsent(matchPair.cr1, new HashMap<CRType_MM, Double>());

		// store old value for undo operation of manual mappings
		if ((isManual) && (timestamp!=null)) {
			timestampedPairs.putIfAbsent(timestamp, new ArrayList<CRPair>());
			timestampedPairs.get(timestamp).add(new CRPair(matchPair.cr1, matchPair.cr2, (matchResult.get(isManual)).get(matchPair.cr1).get(matchPair.cr2)));
		}

		// update value
		double v = add ? matchResult.get(isManual).get(matchPair.cr1).getOrDefault(matchPair.cr2, 0d) : 0d;
		matchResult.get(isManual).get(matchPair.cr1).put(matchPair.cr2, (matchPair.s==null) ? null : matchPair.s+v);
	}



	@Override
	public long getNumberOfMatches(boolean manual) {
		return matchResult.get(manual).entrySet().stream().mapToLong( entry -> entry.getValue().size()).sum();
	}



	@Override
	public long getNumberOfClusters() {
		return crTab.getCR().map(cr -> cr.getCluster()).distinct().count();
	}


	
	
}
