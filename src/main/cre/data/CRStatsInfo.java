package main.cre.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import main.cre.data.type.abs.PubType;

public class CRStatsInfo {

	private static CRStatsInfo crStatsInfo = null;
	
	public static final int MISSING = -1;
	public static final int NONE = 0;
	
	private long noOfPubs;
	private long noOfCitingPubs;
	private long noOfPubsWithoutPY;
	
	private int rangePY[];
	private HashSet<Integer> allPY;
	private HashMap<Integer, HashMap<Integer, Integer>> mapPY2RPY2NCR;

	private long noOfCRs;
	private int rangeRPY[];
	private HashSet<Integer> allRPY;
	
	public static CRStatsInfo get() {
		if (crStatsInfo == null) {
			crStatsInfo = new CRStatsInfo();
		}
		return crStatsInfo;
	}
	
	private CRStatsInfo ()  {
		
	}
	
	public void init() {
		noOfPubs = 0;
		noOfCitingPubs = 0;
		noOfPubsWithoutPY = 0;
		rangePY = new int[]{-1, -1};
		allPY = new HashSet<Integer>();
		
		noOfCRs = 0;
		rangeRPY = new int[]{-1, -1};
		allRPY = new HashSet<Integer>();
		
		mapPY2RPY2NCR = new HashMap<Integer, HashMap<Integer, Integer>>();
	}
	
	
	public void updateStats (PubType<?> pub) {
		noOfPubs++;
		noOfCitingPubs += (pub.getSizeCR()>0) ? 1 : 0;
		noOfPubsWithoutPY += (pub.getPY() == null) ? 1 : 0;
		
		int py = MISSING;
		if (pub.getPY() != null) {
			py = pub.getPY().intValue(); 
			rangePY[0] = ((rangePY[0]==-1) || (rangePY[0]>py)) ? py : rangePY[0];  
			rangePY[1] = ((rangePY[1]==-1) || (rangePY[1]<py)) ? py : rangePY[1];
			allPY.add(py);
		}
		
		final int pyKey = py;
		noOfCRs += pub.getSizeCR();
		pub.getCR().forEach(cr -> {
			int rpy = MISSING;
			if (cr.getRPY() != null) {
				rpy = cr.getRPY().intValue(); 
				rangeRPY[0] = ((rangeRPY[0]==-1) || (rangeRPY[0]>rpy)) ? rpy : rangeRPY[0];  
				rangeRPY[1] = ((rangeRPY[1]==-1) || (rangeRPY[1]<rpy)) ? rpy : rangeRPY[1];  
				allRPY.add(rpy);
			}	
			
			mapPY2RPY2NCR.putIfAbsent(pyKey, new HashMap<Integer, Integer>());
			mapPY2RPY2NCR.get(pyKey).compute(rpy, (k, v) -> (v==null) ? 1 : v+1);
			
			
			
		});
	}
	
	public int[] getRangeRPY() {
		return rangeRPY;
	}

	public int[] getRangePY() {
		return rangePY;
	}
	
	public long getNumberOfPubs () {
		return noOfPubs;
	}

	public long getNumberOfPubsWithoutPY () {
		return noOfPubsWithoutPY;
	}
	
	public long getNumberOfCRsWithoutRPY () {
		return getNumberOfCRs(new int[] {2, 1}, true, new int[] {NONE, NONE}, true);	// 2>1 => no CRs with RPY are counted 
	}

	public long getNumberOfCRsWithoutPY () {
		return getNumberOfCRs(new int[] {NONE, NONE}, true, new int[] {2, 1}, true);	// 2>1 => no CRs with PY are counted 
	}
	
	public long getNumberOfCRs () {
		return getNumberOfCRs(new int[] {NONE, NONE}, true, new int[] {NONE, NONE}, true);
	}
	
	
	
	public long getNumberOfCRs (int rpyRange[], boolean includeWithoutRPY, int pyRange[], boolean includeWithoutPY) {
		
		long result = 0;
		if (mapPY2RPY2NCR == null) return result;
		
		for (Entry<Integer, HashMap<Integer, Integer>> py: mapPY2RPY2NCR.entrySet()) {
			
			if ((py.getKey().intValue()==MISSING) && (!includeWithoutPY)) continue;
			if ((pyRange[0]!=NONE) && (py.getKey().intValue()!=MISSING) && (py.getKey().intValue()<pyRange[0])) continue;
			if ((pyRange[1]!=NONE) && (py.getKey().intValue()!=MISSING) && (py.getKey().intValue()>pyRange[1])) continue;
			
			for (Entry<Integer, Integer> rpy: py.getValue().entrySet()) {
				
				if ((rpy.getKey().intValue()==MISSING) && (!includeWithoutRPY)) continue;
				if ((rpyRange[0]!=NONE) && (rpy.getKey().intValue()!=MISSING) && (rpy.getKey().intValue()<rpyRange[0])) continue;
				if ((rpyRange[1]!=NONE) && (rpy.getKey().intValue()!=MISSING) && (rpy.getKey().intValue()>rpyRange[1])) continue;
				
				result += rpy.getValue().intValue();
			}
		}
		
		
		return result;
	}
	
	
	@Override
	public String toString() {
		String result = "\n";
		
		result += "\n* noOfPubs=" + noOfPubs;
		result += "\n* noOfCitingPubs=" + noOfCitingPubs;
		result += "\n* noOfPubsWithoutPY=" + noOfPubsWithoutPY;
		
		result += "\n* minPY=" + rangePY[0];
		result += "\n* maxPY=" + rangePY[1];
		result += "\n* distinctPY=" + allPY.size();
		
		result += "\n* noOfCRs=" + noOfCRs;
		result += "\n* minRPY=" + rangeRPY[0];
		result += "\n* maxRPY=" + rangeRPY[1];
		result += "\n* distinctRPY=" + allRPY.size();
		
		result += "\n* PY-RPY-pairs ...";
		for (Entry<Integer, HashMap<Integer, Integer>> x: mapPY2RPY2NCR.entrySet()) {
			for (Entry<Integer, Integer> y: x.getValue().entrySet()) {
				result += "\n* PY=" + x.getKey() + " / RPY=" + y.getKey() + " / N_CR=" + y.getValue().intValue();
			}
		}
		
		result += "\n " + getNumberOfCRs(new int[]{2014, 2016}, false, new int[] {2017, 2017}, false);
		
		return result;
		
	}
}
