package main.cre.data.type.mm;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.cre.data.CRSearch;
import main.cre.data.Indicators;
import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.Clustering;
import main.cre.data.type.abs.PubType;
import main.cre.data.type.mm.clustering.CRCluster_MM;
import main.cre.data.type.mm.clustering.CRMatch2;
import main.cre.ui.statusbar.StatusBar;

public class CRTable_MM extends CRTable<CRType_MM, PubType_MM> {

	private static CRTable_MM crTab = null;

	private HashMap<CRType_MM, CRType_MM> crDataMap;	// map: CR -> CR to get duplicates
	
	private HashMap<PubType_MM, PubType_MM> allPubs; 
	
	
	private boolean duringUpdate;
	private boolean showNull;
	
	private CRMatch2 crmatch;
	
	public static CRTable_MM get() {
		if (crTab == null) {
			crTab = new CRTable_MM();
		}
		return crTab;
	}
	
	

	
	private CRTable_MM () { 
		init();
	}
	

	/**
	 * Initialize empty CRTable
	 */
	
	public void init() {
		
		
		if (allPubs != null) {
			for (PubType_MM pub: allPubs.keySet()) {
				pub.removeAllCRs(true);
			}
		}
		
		this.setNpctRange(1);
		crDataMap = new HashMap<CRType_MM, CRType_MM>();
		allPubs = new HashMap<PubType_MM, PubType_MM>();
		
		crmatch = new CRMatch2(this);
		duringUpdate = false;
		this.setAborted(false);
		showNull = true;
		
		
		CRSearch.get().init();
		
	}	
	
	@Override
	public Stream<CRType_MM> getCR() {
		return crDataMap.keySet().stream();
	}

	/**
	 * 
	 * @param includePubsWithoutCRs default=false
	 * @return
	 */
	public Stream<PubType_MM> getPub (boolean includePubsWithoutCRs) {
		return includePubsWithoutCRs ? allPubs.keySet().stream() : getCR().flatMap(cr -> cr.getPub()).distinct();
	}
	

	


	@Override
	public CRType_MM addCR(CRType_MM cr, boolean checkForDuplicatesAndSetId) {
		
		if (checkForDuplicatesAndSetId) {
			CRType_MM crMain = this.crDataMap.get(cr);
			if (crMain == null) {
				this.crDataMap.put(cr, cr);
				cr.setID(this.crDataMap.size());
				cr.setCluster(new CRCluster_MM(cr));
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
	
//	public PubType addPub (PubType pub, boolean addCRs) {
//		return addPub (pub, addCRs, false);
//	}
	
	@Override
	public PubType_MM addPub (PubType_MM pub, boolean addCRs, boolean checkForDuplicates) {
		
		if (checkForDuplicates) {
			PubType_MM pubMain = this.allPubs.get(pub);
			if (pubMain == null) {
				this.allPubs.put(pub, pub);
				pub.setID(this.allPubs.size());
			} else {
				pub = pubMain;
			}
		} else {
			pub.setID(this.allPubs.size()+1);
			this.allPubs.put(pub, pub);
		}
		
		if (addCRs) {
		
			for(CRType_MM cr: pub.getCR().collect(Collectors.toSet())) {
				
				CRType_MM crMain = this.crDataMap.get(cr);
				if (crMain == null) {
					this.crDataMap.put(cr, cr);
					cr.setID(this.crDataMap.size());
					cr.setCluster(new CRCluster_MM(cr));
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
		Set<CRCluster_MM> clusters = getCR().filter(cr -> cr.getClusterSize()>1).map(cr -> cr.getCluster()).distinct().collect(Collectors.toSet());
		StatusBar.get().setValue(String.format("Merging of %d clusters...", clusters.size()));

		// merge clusters
		clusters.forEach(cl -> {
			
			StatusBar.get().incProgressbar();
			
			// get mainCR = CR with highest number of citations
			CRType_MM crMain = cl.getMainCR();
			Set<CRType_MM> crMerge = cl.getCR().collect(Collectors.toSet());
			crMerge.remove(crMain);

			// merge CRs with main CR
			for (CRType_MM cr:crMerge) {
				cr.getPub().collect(Collectors.toList()).stream().forEach(crPub -> {		// make a copy to avoid concurrent modification
					crPub.addCR(crMain, true);
					crPub.removeCR(cr, true);
				});
			}
			
			// remove merged CRs
			this.crDataMap.keySet().removeAll(crMerge);
		});
		
		// reset clusters and match result
		getCR().forEach(cr -> cr.setCluster(new CRCluster_MM(cr)));
		crmatch.init();
		
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
		
		Indicators.get().update();
		Indicators.get().updateChartData();
		
		duringUpdate = false;
		
	}

	
	
	

	public void removeCR (Predicate<CRType_MM> cond) {
		
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
	@Override
	public void removeCR (List<CRType_MM> toDelete) {
		getCR().forEach(cr -> cr.setFlag(false));
		toDelete.forEach(cr -> cr.setFlag(true));
		removeCR(cr -> cr.isFlag());
	}
	

	/**
	 * Remove all but the given list of CRs
	 * @param toRetain list of CRs to be retained
	 */
	public void retainCR (List<CRType_MM> toRetain) {
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
	public void removePubByCR (List<CRType_MM> selCR) {
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
	

	
	public void filterByCluster (List<CRType_MM> sel) {
		if (!duringUpdate) {
			getCR().forEach(cr -> cr.setVI(false));
			sel.stream().map(cr -> cr.getCluster()).flatMap(cluster -> cluster.getCR()).forEach ( cr -> cr.setVI(true) );
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




	@Override
	public void addManuMatching(List<CRType_MM> selCR, Clustering.ManualMatchType matchType, double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {
		crmatch.addManuMatching(selCR, matchType, matchThreshold, useVol, usePag, useDOI);
	}

	@Override
	public void generateAutoMatching() {
		crmatch.generateAutoMatching();
	}

	@Override
	public void undoManuMatching(double matchThreshold, boolean useVol, boolean usePag, boolean useDOI) {
		crmatch.undoManuMatching(matchThreshold, useVol, usePag, useDOI);
	}

	@Override
	public void updateClustering(Clustering.ClusteringType type, Set<CRType_MM> changeCR, double threshold, boolean useVol, boolean usePag, boolean useDOI) {
		crmatch.updateClustering(type, changeCR, threshold, useVol, usePag, useDOI);
	}
	
	@Override
	public long getNumberOfMatches (boolean manual) {
		return crmatch.getSize(manual);
	}

	@Override
	public long getNumberOfClusters() {
		return getCR().map(cr -> cr.getCluster()).distinct().count();
	}

	
	

}

