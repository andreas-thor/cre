package cre.test.data;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cre.test.data.match.CRCluster;
import cre.test.data.match.CRMatch2;
import cre.test.data.type.CRType;
import cre.test.data.type.PubType;
import cre.test.ui.StatusBar;
import nz.sodium.Cell;

public class CRTable {

	public static enum COMPARATOR { LT, LTE, EQ, GTE, GT };
	
	private static CRTable crTab = null;
	
	private HashMap<CRType, CRType> crDataMap;	// map: CR -> CR to get duplicates
	
	private HashMap<PubType, PubType> allPubs;
	
	
	private CRChartData chartData;	
	
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
		
		
		if (allPubs != null) {
			for (PubType pub: allPubs.keySet()) {
				pub.removeAllCRs(true);
			}
		}
		
		
		crDataMap = new HashMap<CRType, CRType>();
		allPubs = new HashMap<PubType, PubType>();
		
		CRMatch2.get().init();
		duringUpdate = false;
		aborted = false;
		showNull = true;
		
		chartData = new CRChartData(0, 0);
		
		setAborted(false);
		CRSearch.get().init();
		
	}	
	
	public Stream<CRType> getCR() {
		return crDataMap.keySet().stream();
	}

	/**
	 * 
	 * @param includePubsWithoutCRs default=false
	 * @return
	 */
	public Stream<PubType> getPub (boolean includePubsWithoutCRs) {
		return includePubsWithoutCRs ? allPubs.keySet().stream() : getCR().flatMap(cr -> cr.getPub()).distinct();
	}
	
	public Stream<PubType> getPub() {
		return getPub(false);
	}
	

	public CRType addCR(CRType cr) {
		return this.addCR(cr, false);
	}
	
	public CRType addCR(CRType cr, boolean checkForDuplicatesAndSetId) {
		
		if (checkForDuplicatesAndSetId) {
			CRType crMain = this.crDataMap.get(cr);
			if (crMain == null) {
				this.crDataMap.put(cr, cr);
				cr.setID(this.crDataMap.size());
				cr.setCID2(new CRCluster (cr));
				return cr;
			} else {
				return crMain;
			}
		} else {
			crDataMap.put(cr, cr);
			return cr;
		}
	}
	
	
	/*
	 * We additionally store all pubs in allPubs
	 * This is later only used for export (to Scopus, WoS, CSV_Pub) when the user setting "include pubs without CRs" is set
	 */
	
	public PubType addPub (PubType pub, boolean addCRs) {
		return addPub (pub, addCRs, false);
	}
	
	public PubType addPub (PubType pub, boolean addCRs, boolean checkForDuplicates) {
		
		if (checkForDuplicates) {
			PubType pubMain = this.allPubs.get(pub);
			if (pubMain == null) {
				this.allPubs.put(pub, pub);
				pub.setID(this.allPubs.size());
			} else {
				pub = pubMain;
			}
		} else {
			this.allPubs.put(pub, pub);
			pub.setID(this.allPubs.size());
		}
		
		if (addCRs) {
		
			for(CRType cr: pub.getCR().collect(Collectors.toSet())) {
				
				CRType crMain = this.crDataMap.get(cr);
				if (crMain == null) {
					this.crDataMap.put(cr, cr);
					cr.setID(this.crDataMap.size());
					cr.setCID2(new CRCluster (cr));
				} else {
					pub.removeCR(cr, false);	
					pub.addCR(crMain, false);
					crMain.addPub(pub, false);	
				}
			}
		}
		
		return pub;
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
			this.crDataMap.keySet().removeAll(crMerge);
		});
		
		// reset clusters and match result
		getCR().forEach(cr -> cr.setCID2(new CRCluster(cr)));
		CRMatch2.get().init();
		
		updateData();
		StatusBar.get().setValue("Merging done");

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
		
		Indicators.update();
		this.updateChartData();
		
		duringUpdate = false;
		
	}

	
	public void updateChartData () throws OutOfMemoryError {
		this.chartData = Indicators.getChartData(UserSettings.get().getMedianRange());
	}
	
	
	public CRChartData getChartData () {
		return this.chartData;
	}	

	private void removeCR (Predicate<CRType> cond) {
		
		crDataMap.keySet().removeIf( cr ->  { 
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
		getCR().forEach(cr -> cr.setFlag(false));
		toDelete.forEach(cr -> cr.setFlag(true));
		removeCR(cr -> cr.isFlag());
	}
	

	/**
	 * Remove all but the given list of CRs
	 * @param toRetain list of CRs to be retained
	 */
	public void retainCR (List<CRType> toRetain) {
		getCR().forEach(cr -> cr.setFlag(true));
		toRetain.forEach(cr -> cr.setFlag(false));
		removeCR(cr -> cr.isFlag());
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
		selCR.stream().flatMap (cr -> cr.getPub()).forEach(pub -> pub.setFlag(true));
		removePub (pub -> !pub.isFlag());
		getPub().forEach(pub -> pub.setFlag(false));
		
//		removePub (pub -> !selCR.stream().flatMap (cr -> cr.getPub()).distinct().collect(Collectors.toList()).contains(pub));
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
		removePub (pub -> (pub.getPY()==null) || (range[0] > pub.getPY()) || (pub.getPY() > range[1]));
	}
	
	

	
	
	/**
	 * Filter publications by year range
	 * Filtering = set VI property to 1 or 0
	 * @param from
	 * @param to
	 */
	public void filterByYear (int[] range) {
		if (!duringUpdate) {
			getCR().forEach ( it -> { it.setVI(((it.getRPY()!=null) && (range[0]<=it.getRPY()) && (range[1]>=it.getRPY())) || ((it.getRPY()==null) && (this.showNull))); });
		}
	}
	

	
	public void filterByCluster (List<CRType> sel) {
		if (!duringUpdate) {
			getCR().forEach(cr -> cr.setVI(false));
			sel.stream().map(cr -> cr.getCID2()).flatMap(cluster -> cluster.getCR()).forEach ( cr -> cr.setVI(true) );
		}
	}
	
	

	
	
	public void setShowNull (boolean showNull) {
		this.showNull = showNull;
		getCR().forEach ( cr -> { if (cr.getRPY() == null) cr.setVI(showNull);  });
	}
	
	public void showAll() {
		this.showNull = true;
		getCR().forEach ( cr -> cr.setVI(true) );
	}
	
	

	
	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

}

