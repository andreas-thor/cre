package main.cre.data.type.abs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public abstract class Clustering<C extends CRType<P>, P extends PubType<C>> {

	@FunctionalInterface
	public interface NewMatchingPair<C>  {
		void accept(C cr1, C cr2, double sim);
	}
	
	// weights for weighted similarity   
	private final double weight_author = 2.0;
	private final double weight_journal = 1.0;
	private final double weight_title = 5.0;
	private final double min_threshold = 0.5;
	
	public static enum ManualMatchType { SAME, DIFFERENT, EXTRACT }

	public static enum ClusteringType { INIT, REFRESH }
	
	
	
	public void  crossCompareCR(List<C> crlist, Levenshtein l, NewMatchingPair<C> onNewPair) {
		
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
				if (s1>=min_threshold) {

					// the two CRs to be compared
					C cr1 = crlist.get(xIndx);
					C cr2 = crlist.get(xIndx+yIndx+1);
					double s = simCR (cr1, cr2, s1, l);
					if (s >= min_threshold) {
						onNewPair.accept(cr1, cr2, s);
					}
				}
				yIndx++;
			}
			xIndx++;
		}
	}
	
	
	/**
	 * Computes similarity of two CRs based on a given authors similarity 
	 * @param comp_CR
	 * @param sim_author
	 * @param l
	 * @return
	 */
	
	public double simCR (C cr1, C cr2, double sim_author, Levenshtein l) {
		
		// increasing sim + weight if data is available; weight for author is 2
		double sim = weight_author*sim_author;
		double weight = weight_author;
		
		// compare Journal name (weight=1)
		String[] comp_J = new String[] { cr1.getJ_N() == null ? "" : cr1.getJ_N(), cr2.getJ_N() == null ? "" : cr2.getJ_N() };
		if ((comp_J[0].length()>0) && (comp_J[1].length()>0)) {
			sim += weight_journal* l.getSimilarity(comp_J[0].toLowerCase(), comp_J[1].toLowerCase());
			weight += weight_journal;
		}
		
		// compare title (weight=5)
		// ignore if both titles are empty; set sim=0 if just one is emtpy; compute similarity otherwise
		String[] comp_T = new String[] { cr1.getTI() == null ? "" : cr1.getTI(), cr2.getTI() == null ? "" : cr2.getTI() };
		if ((comp_T[0].length()>0) || (comp_T[1].length()>0)) {
			sim += weight_title * (((comp_T[0].length()>0) && (comp_T[1].length()>0)) ? l.getSimilarity(comp_T[0].toLowerCase(), comp_T[1].toLowerCase()) : 0.0);
			weight += weight_title;
		}
		
		return sim/weight;		// weighted average of AU_L, J_N, and TI
	}
	
	
	public void generateInitialClustering () {
		generateAutoMatching();
		updateClustering(Clustering.ClusteringType.INIT, null, min_threshold, false, false, false);
	}
	
	
	public abstract void generateAutoMatching ();
	
	
	public abstract Set<C> addManuMatching (List<C> selCR, ManualMatchType matchType);

	
	public void addManuMatching (List<C> selCR, Clustering.ManualMatchType matchType, double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {
		updateClustering(Clustering.ClusteringType.REFRESH, addManuMatching(selCR, matchType), matchThreshold, useVol, usePag, useDOI);
	}
	
	
	public abstract Set<C> undoManuMatching ();
	
	
	public void undoManuMatching (double threshold, boolean useVol, boolean usePag, boolean useDOI) {
		updateClustering(Clustering.ClusteringType.REFRESH, undoManuMatching(), threshold, useVol, usePag, useDOI);
	}

	public abstract void updateClustering (ClusteringType type, Set<C> changeCR, double threshold, boolean useVol, boolean usePag, boolean useDOI);

	public abstract long getNumberOfMatches (boolean manual);
	
	public abstract long getNumberOfClusters();
	
	public abstract Stream<MatchPairGroup> getMatchPairGroups(boolean manual);
	
	
}
