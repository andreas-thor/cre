package main.cre.data.type.abs;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

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
	
	public Stream<Entry<Integer, Double>> getMatches () {
		return this.matches.entrySet().stream().sorted((e1, e2) -> e1.getKey()-e2.getKey());
	}
}
