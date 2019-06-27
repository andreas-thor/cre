package main.cre.data.type.abs;

import java.util.HashMap;
import java.util.Map;

public class MatchPairGroup {

	private int crId1;
	private Map<Integer, Double> matches;
	
	public MatchPairGroup (int crId1) {
		this.crId1 = crId1;
		this.matches = new HashMap<Integer, Double>();
	}
	
	public void addMatch (int crId2, double sim) {
		this.matches.put(crId2,  sim);
	}
	
	public int getCrId1 () {
		return this.crId1;
	}
	
	public Map<Integer, Double> getMatches () {
		return this.matches;
	}
}
