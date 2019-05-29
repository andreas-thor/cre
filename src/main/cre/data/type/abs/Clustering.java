package main.cre.data.type.abs;

public class Clustering {

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

}
