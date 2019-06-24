package main.cre.data.type.abs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public abstract class Clustering<C extends CRType<P>, P extends PubType<C>> {

	
	public static enum ManualMatchType { 
		SAME ("Same"), 
		DIFFERENT ("Different"),
		EXTRACT ("Extract");
		
		public final String label;
		
		ManualMatchType(String label) {
			this.label = label;
		}
		
	}

	public static enum ClusteringType {
		INIT ("Init"), 
		REFRESH ("Refresh");
		
		public final String label;
		ClusteringType (String label) {
			this.label = label;
		}
	}
	
	
	
	public void  crossCompareCR(List<CRType<?>> crlist, Levenshtein l, BiConsumer<CRType<?>[], Double> onNewPair) {
		
		// parameters
		final double threshold = 0.5;
				
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
					CRType<?>[] comp_CR = new CRType<?>[] { crlist.get(xIndx), crlist.get(xIndx+yIndx+1/*ySize-yIndx-1*/) };
					double s = simCR (comp_CR, s1, l);
					if (s >= threshold) {
						onNewPair.accept(comp_CR, s);
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
	
	public double simCR (CRType<?>[] comp_CR, double sim_author, Levenshtein l) {
		
		// parameters
		final double weight_author = 2.0;
		final double weight_journal = 1.0;
		final double weight_title = 5.0;
		
		// increasing sim + weight if data is available; weight for author is 2
		double sim = weight_author*sim_author;
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
		
		return sim/weight;		// weighted average of AU_L, J_N, and TI
	}
	
	

	public abstract void addManuMatching (List<C> selCR, ManualMatchType matchType, double matchThreshold, boolean useVol, boolean usePag, boolean useDOI);

	public abstract void generateAutoMatching ();

	public abstract void undoManuMatching (double matchThreshold, boolean useVol, boolean usePag, boolean useDOI);

	public abstract void updateClustering (ClusteringType type, Set<C> changeCR, double threshold, boolean useVol, boolean usePag, boolean useDOI);

	public abstract long getNumberOfMatches (boolean manual);
	
	public abstract long getNumberOfClusters();
}
