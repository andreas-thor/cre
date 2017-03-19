package cre.test.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cre.test.data.match.CRCluster;
import cre.test.data.match.CRMatch2;
import cre.test.data.type.CRType;
import cre.test.data.type.PubType;
import cre.test.ui.StatusBar;

public class CRTable {

	private static CRTable crTab = null;
	
	private List<CRType> crData;
	private Map<Character, HashMap<String, CRType>> crDup; // first character -> (crString -> CR )

	private int[][] chartData;
	
	private boolean duringUpdate;
	private boolean aborted;
	private boolean showNull;
	

	
	public static CRTable get() {
		if (crTab == null) {
			crTab = new CRTable();
		}
		return crTab;
	}
	
	
	private CRTable () { 
		init();
	}
	

	/**
	 * Initialize empty CRTable
	 */
	
	public void init() {
		crData = new ArrayList<CRType>();
		crDup = new HashMap<Character,  HashMap<String, CRType>>();
		duringUpdate = false;
		aborted = false;
		showNull = true;
		chartData = new int[][] {{0},{0},{0}};
		setAborted(false);
	}
	
	
	public Stream<CRType> getCR() {
		return crData.stream();
	}
	
	public Stream<PubType> getPub() {
		return crData.stream().flatMap(cr -> cr.getPub()).distinct();
	}
	

	public void addCR(CRType cr) {
		String crS = cr.getCR();
		char cr1 = crS.charAt(0);
		crDup.putIfAbsent(cr1, new HashMap<String,CRType>());
		crDup.get(cr1).put(crS, cr);
		crData.add(cr);
	}
	
	
	
	public void addPubs(List<PubType> pubs) {
		
		pubs.stream().flatMap(pub -> pub.getCR()).distinct().collect(Collectors.toList()).stream().forEach(cr -> { // make a copy to avoid concurrent modifcation
			
			String crS = cr.getCR();
			char cr1 = crS.charAt(0);
			crDup.putIfAbsent(cr1, new HashMap<String,CRType>());
			CRType crMain = crDup.get(cr1).get(crS); 
			if (crMain == null) {
				// add current CR to list
				crDup.get(cr1).put(crS, cr);
				crData.add(cr);
				cr.setID(crData.size());
				cr.setCID2(new CRCluster (cr));
			} else {
				// merge current CR with main CR
				cr.getPub().collect(Collectors.toList()).stream().forEach(crPub -> {		// make a copy to avoid concurrent modification
					crPub.addCR(crMain, true);
					crPub.removeCR(cr, true);
				});
			}
		});
		
	}	
	
	/**
	 * Merge CRs based on clustering
	 */

	public void merge () {
		
		// get all clusters with size > 1
		Set<CRCluster> clusters = getCR().filter(cr -> cr.getCID_S()>1).map(cr -> cr.getCID2()).distinct().collect(Collectors.toSet());
		StatusBar.get().setValue(String.format("Merging of %d clusters...", clusters.size()));

		// merge clusters
		clusters.forEach(cl -> {
			
			StatusBar.get().incProgressbar();
			
			// get mainCR = CR with highest number of citations
			CRType crMain = cl.getMainCR();
			Set<CRType> crMerge = cl.getCR().collect(Collectors.toSet());
			crMerge.remove(crMain);

			// merge CRs with main CR
			for (CRType cr:crMerge) {
				cr.getPub().collect(Collectors.toList()).stream().forEach(crPub -> {		// make a copy to avoid concurrent modification
					crPub.addCR(crMain, true);
					crPub.removeCR(cr, true);
				});
			}
			
			// remove merged CRs
			this.crData.removeAll(crMerge);
		});
		
		// reset clusters and match result
		getCR().forEach(cr -> cr.setCID2(new CRCluster(cr)));
		CRMatch2.get().init();
	}
	
	
	
	/**
	 * Update computation of percentiles for all CRs
	 * Called after loading, deleting or merging of CRs
	 * @param removed Data has been removed --> adjust clustering data structures; adjust CR lists per publication
	 */
	
	public void updateData () throws OutOfMemoryError {

		
		duringUpdate = true;		// mutex to avoid chart updates during computation
		
		System.out.println("update Data");
		System.out.println(System.currentTimeMillis());
		
		this.chartData = Indicators.update();
		
		duringUpdate = false;
		
	}

	
	public int[][] getChartData () {
		return this.chartData;
	}
	
		


	private void removeCR (Predicate<CRType> cond) {
		crData.removeIf( cr ->  { 
			if (cond.test(cr)) {
				cr.removeAllPubs(true);
				return true;
			} else {
				return false;
			}
		});
		updateData();
	}

	/**
	 * Remove list of CRs
	 * @param toDelete list of CRs to be deleted
	 */
	public void removeCR (List<CRType> toDelete) {
		removeCR(cr -> toDelete.contains(cr));
	}
	

	/**
	 * Remove all but the given list of CRs
	 * @param toRetain list of CRs to be retained
	 */
	public void retainCR (List<CRType> toRetain) {
		removeCR(cr -> !toRetain.contains(cr));
	}
	
	
	/**
	 * Remove all CRs without year (RPY)
	 */
	public void removeCRWithoutYear () {
		removeCR (cr -> cr.getRPY() == null);
	}

	
	/**
	 * Remove all CRS within a given RPY range
	 * @param range
	 */
	public void removeCRByYear (int[] range) {
		removeCR (cr -> ((cr.getRPY()!=null) && (range[0] <= cr.getRPY()) && (cr.getRPY() <= range[1])));
	}

	
	/**
	 * Remove all CRs within a given N_CR range
	 * @param range
	 */
	public void removeCRByN_CR(int[] range) {
		removeCR (cr -> (range[0] <= cr.getN_CR()) && (cr.getN_CR() <= range[1]));
	}
	
	
	/**
	 * Remove all CRs < / <= / = / >= / > PERC_YR
	 * @param comp comparator (as string); TODO: ENUMERATION
	 * @param threshold
	 */
	
	public void removeCRByPERC_YR (String comp, double threshold) {
		switch (comp) {
			case "<" : removeCR (cr -> cr.getPERC_YR() <  threshold); break;
			case "<=": removeCR (cr -> cr.getPERC_YR() <= threshold); break;
			case "=" : removeCR (cr -> cr.getPERC_YR() == threshold); break;
			case ">=": removeCR (cr -> cr.getPERC_YR() >= threshold); break;
			case ">" : removeCR (cr -> cr.getPERC_YR() >  threshold); break;
		}
	}
	
	
	/**
	 * Remove all citing publications, that do *not* reference any of the given CRs 
	 * @param selCR list of CRs
	 */
	public void removePubByCR (List<CRType> selCR) {
		removePub (pub -> !selCR.stream().flatMap (cr -> cr.getPub()).distinct().collect(Collectors.toList()).contains(pub));
	}
	
	
	
	private void removePub (Predicate<PubType> cond) {
		getPub().filter(cond).collect(Collectors.toList()).forEach(pub -> pub.removeAllCRs(true));
		removeCR(cr -> cr.getN_CR()==0);
	}
	
	/**
	 * Remove all citing publications OUTSIDE the given citing year (PY) range
	 * @param range
	 */
	public void removePubByCitingYear (int[] range) {
		removePub (pub -> (pub.PY==null) || (range[0] > pub.PY) || (pub.PY > range[1]));
	}
	
	

	
	
	/**
	 * Filter publications by year range
	 * Filtering = set VI property to 1 or 0
	 * @param from
	 * @param to
	 */
	public void filterByYear (int[] range) {
		if (!duringUpdate) {
			crData.stream().forEach ( it -> { it.setVI(((it.getRPY()!=null) && (range[0]<=it.getRPY()) && (range[1]>=it.getRPY())) || ((it.getRPY()==null) && (this.showNull))); });
		}
	}
	

	
	public void filterByCluster (List<CRType> sel) {
		Set<CRCluster> clusters = sel.stream().map(cr -> cr.getCID2()).distinct().collect(Collectors.toSet());
		crData.stream().forEach ( it -> it.setVI( clusters.contains(it.getCID2()) ));
	}
	
	
	

	
	
	public void setShowNull (boolean showNull) {
		this.showNull = showNull;
		crData.stream().forEach ( it -> { if (it.getRPY() == null) it.setVI(showNull);  });
	}
	
	public void showAll() {
		this.showNull = true;
		crData.stream().forEach ( it -> it.setVI(true) );
	}
	
	

	
	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}






	
}
