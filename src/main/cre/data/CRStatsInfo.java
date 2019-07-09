package main.cre.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import main.cre.data.type.abs.PubType;
import main.cre.data.type.abs.Statistics.IntRange;

public class CRStatsInfo {

	private static CRStatsInfo crStatsInfo = null;
	
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
		
		int py = IntRange.MISSING;
		if (pub.getPY() != null) {
			py = pub.getPY().intValue(); 
			rangePY[0] = ((rangePY[0]==-1) || (rangePY[0]>py)) ? py : rangePY[0];  
			rangePY[1] = ((rangePY[1]==-1) || (rangePY[1]<py)) ? py : rangePY[1];
			allPY.add(py);
		}
		
		final int pyKey = py;
		noOfCRs += pub.getSizeCR();
		pub.getCR().forEach(cr -> {
			int rpy = IntRange.MISSING;
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
	
	public IntRange getRangeRPY() {
		return new IntRange(rangeRPY[0], rangeRPY[1]);
	}

	public IntRange getRangePY() {
		return new IntRange(rangePY[0], rangePY[1]);
	}
	
	public long getNumberOfPubs () {
		return noOfPubs;
	}

	public long getNumberOfPubsWithoutPY () {
		return noOfPubsWithoutPY;
	}
	
	public long getNumberOfCRsWithoutRPY () {
		return getNumberOfCRs(new IntRange (2, 1), true, new IntRange (IntRange.NONE, IntRange.NONE), true);	// 2>1 => no CRs with RPY are counted 
	}

	public long getNumberOfCRsWithoutPY () {
		return getNumberOfCRs(new IntRange (IntRange.NONE, IntRange.NONE), true, new IntRange (2, 1), true);	// 2>1 => no CRs with PY are counted 
	}
	
	public long getNumberOfCRs () {
		return getNumberOfCRs(new IntRange (IntRange.NONE, IntRange.NONE), true, new IntRange (IntRange.NONE, IntRange.NONE), true);
	}
	
	
	
	public long getNumberOfCRs (IntRange rpyRange, boolean includeWithoutRPY, IntRange pyRange, boolean includeWithoutPY) {
		
		long result = 0;
		if (mapPY2RPY2NCR == null) return result;
		
		for (Entry<Integer, HashMap<Integer, Integer>> py: mapPY2RPY2NCR.entrySet()) {
			
			if ((py.getKey().intValue()==IntRange.MISSING) && (!includeWithoutPY)) continue;
			if ((pyRange.getMin()!=IntRange.NONE) && (py.getKey().intValue()!=IntRange.MISSING) && (py.getKey().intValue()<pyRange.getMin())) continue;
			if ((pyRange.getMax()!=IntRange.NONE) && (py.getKey().intValue()!=IntRange.MISSING) && (py.getKey().intValue()>pyRange.getMax())) continue;
			
			for (Entry<Integer, Integer> rpy: py.getValue().entrySet()) {
				
				if ((rpy.getKey().intValue()==IntRange.MISSING) && (!includeWithoutRPY)) continue;
				if ((rpyRange.getMin()!=IntRange.NONE) && (rpy.getKey().intValue()!=IntRange.MISSING) && (rpy.getKey().intValue()<rpyRange.getMin())) continue;
				if ((rpyRange.getMax()!=IntRange.NONE) && (rpy.getKey().intValue()!=IntRange.MISSING) && (rpy.getKey().intValue()>rpyRange.getMax())) continue;
				
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
		
		result += "\n " + getNumberOfCRs(new IntRange (2014, 2016), false, new IntRange (2017, 2017), false);
		
		return result;
		
	}
}
