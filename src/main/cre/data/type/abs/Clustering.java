package main.cre.data.type.abs;

import java.util.List;
import java.util.Set;

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

	public abstract void addManuMatching (List<C> selCR, ManualMatchType matchType, double matchThreshold, boolean useVol, boolean usePag, boolean useDOI);

	public abstract void generateAutoMatching ();

	public abstract void undoManuMatching (double matchThreshold, boolean useVol, boolean usePag, boolean useDOI);

	public abstract void updateClustering (ClusteringType type, Set<C> changeCR, double threshold, boolean useVol, boolean usePag, boolean useDOI);

	public abstract long getNumberOfMatches (boolean manual);
	
	public abstract long getNumberOfClusters();
}
