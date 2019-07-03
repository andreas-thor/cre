package main.cre.data.type.mm;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.cre.data.CRSearch;
import main.cre.data.type.abs.CRTable;
import main.cre.data.type.abs.CRType;
import main.cre.data.type.abs.PubType;
import main.cre.data.type.abs.Statistics;
import main.cre.data.type.abs.Statistics.IntRange;
import main.cre.format.cre.Reader;
import main.cre.ui.statusbar.StatusBar;

public class CRTable_MM extends CRTable<CRType_MM, PubType_MM> {

	private static CRTable_MM crTab = null;

	private HashMap<CRType_MM, CRType_MM> crDataMap;	// map: CR -> CR to get duplicates
	
	private HashMap<PubType_MM, PubType_MM> allPubs; 
	


	
	
	private Statistics_MM statistics;
	
	private boolean duringUpdate;
	private boolean showNull;
	
	private Clustering_MM crmatch;
	
	public static CRTable_MM get() {
		if (crTab == null) {
			crTab = new CRTable_MM();
		}
		return crTab;
	}
	
	
	@Override
	public Reader getReader() {
		return new Reader_MM();
	}
	
	@Override
	public Statistics getStatistics() {
		return this.statistics;
	}
	
	@Override
	public Clustering_MM getClustering() {
		return this.crmatch;
	}




	
	private CRTable_MM () { 
		this.statistics = new Statistics_MM();
		init();
	}
	

	/**
	 * Initialize empty CRTable
	 */
	
	@Override
	public void init() {
		
		
		if (allPubs != null) {
			for (PubType_MM pub: allPubs.keySet()) {
				pub.removeAllCRs(true);
			}
		}
		
		this.setNpctRange(1);
		crDataMap = new HashMap<CRType_MM, CRType_MM>();
		allPubs = new HashMap<PubType_MM, PubType_MM>();
		
		crmatch = new Clustering_MM(this);
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
	@Override
	public Stream<PubType_MM> getPub (boolean includePubsWithoutCRs) {
		return includePubsWithoutCRs ? allPubs.keySet().stream() : getCR().flatMap(cr -> cr.getPub()).distinct();
	}
	

	


	public CRType_MM addCR(CRType_MM cr, boolean checkForDuplicatesAndSetId) {
		
		if (checkForDuplicatesAndSetId) {
			CRType_MM crMain = this.crDataMap.get(cr);
			if (crMain == null) {
				this.crDataMap.put(cr, cr);
				cr.setID(this.crDataMap.size());
				cr.setCluster(new CRCluster(cr));
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
	public PubType_MM addPub (PubType_MM pub) {
		
		pub.setID(this.allPubs.size()+1);
		this.allPubs.put(pub, pub);
		
		
		for(CRType_MM cr: pub.getCR().collect(Collectors.toSet())) {
			
			CRType_MM crMain = this.crDataMap.get(cr);
			if (crMain == null) {
				this.crDataMap.put(cr, cr);
			} else {
				pub.removeCR(cr, false);	
				pub.addCR(crMain, false);
				crMain.addPub(pub, false);	
			}
		}
		
		return pub;
	}
	

	
	/**
	 * Merge CRs based on clustering
	 */

	@Override
	public void merge () {
		
		// get all clusters with size > 1
		Set<CRCluster> clusters = getCR().filter(cr -> cr.getClusterSize()>1).map(cr -> cr.getCluster()).distinct().collect(Collectors.toSet());
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
		getCR().forEach(cr -> cr.setCluster(new CRCluster(cr)));
		crmatch.init();
		
		updateData();
		StatusBar.get().setValue("Merging done");

	}
	
	
	
	/**
	 * Update computation of percentiles for all CRs
	 * Called after loading, deleting or merging of CRs
	 * @param removed Data has been removed --> adjust clustering data structures; adjust CR lists per publication
	 */
	@Override
	public void updateData () throws OutOfMemoryError {

		
		duringUpdate = true;		// mutex to avoid chart updates during computation
		
		System.out.println("update Data");
		System.out.println(System.currentTimeMillis());
		
		System.out.println("Compute Ranges in CRTable_MM");
		
		IntRange range_RPY = getStatistics().getMaxRangeRPY();
		IntRange range_PY  = getStatistics().getMaxRangePY();

		
		int[] NCR_ALL = new int[1];	// NCR overall (array length=1; array to make it effectively final)
		int[] NCR_RPY = new int[range_RPY.getSize()];	// (sum of) NCR by RPY
		int[] CNT_RPY = new int[range_RPY.getSize()];	// number of CRs by RPY
		
		// Group CRs by RPY, compute NCR_ALL and NCR_RPY
		System.out.println("mapRPY_CRs");
		CRTable.get().getCR().forEach(cr -> {
			NCR_ALL[0] += cr.getN_CR();
			if (cr.getRPY()!=null) {
				NCR_RPY[cr.getRPY()-range_RPY.getMin()] += cr.getN_CR();
				CNT_RPY[cr.getRPY()-range_RPY.getMin()] += 1;
			}
		});
		
		
		getCR()
			.filter(cr -> cr.getRPY()!=null)
			.collect(Collectors.groupingBy(CRType::getRPY, Collectors.mapping(Function.identity(), Collectors.toList())))
			.entrySet().stream().parallel()
			.forEach(rpyGroup -> {	
				int rpy = rpyGroup.getKey().intValue();
				List<CRType_MM> crList = rpyGroup.getValue();
				
				computeForAllCRsOfTheSameRPY (rpy, rpy-range_RPY.getMin(), range_PY, NCR_ALL[0], NCR_RPY, crList);
			}
		);		
		
		
		getChartData().updateChartData(range_RPY.getMin(), range_RPY.getMax(), NCR_RPY, CNT_RPY);
		
		duringUpdate = false;
		
	}

	
	private void computeForAllCRsOfTheSameRPY (int rpy, int rpyIdx, IntRange range_PY, int NCR_ALL, int[] NCR_RPY, List<CRType_MM> crList) {
		
		int crSize = crList.size();

		int firstPY = (rpy<=range_PY.getMin()) ? range_PY.getMin() : rpy;	// usually: rpy<=range_PY[0] 
		int lastPY = range_PY.getMax();
		if (lastPY < firstPY) return;
		int pySize = lastPY-firstPY+1;
		
		int[][] NCR_CR_PY = new int[crSize][pySize];	
		int[] NCR_CR = new int[crSize];	
		int[] NCR_CR_all = new int[crSize];	
		int[] NPYEARS_CR = new int[crSize];
		int[] NCR_PY = new int[pySize];	
		int[] NCR = new int[1];
		
		
		for (int x=0; x<crSize; x++) {

			final int crIdx = x;
			CRType<?> cr = crList.get(crIdx);
			
			NCR_CR_all[crIdx] = cr.getN_CR();
			
//			int[] NPYEARS = new int[1];
			cr.getPub().filter(pub -> pub.getPY() != null).forEach(pub -> {
				
				if ((pub.getPY()>=firstPY) && (pub.getPY()<=lastPY)) {	// PY is out of range
				
					int pyIdx = pub.getPY()-firstPY;
					
					if (NCR_CR_PY[crIdx][pyIdx]==0) {	// we found a citation from a new PY
//						NPYEARS[0]++;
						NPYEARS_CR[crIdx]++;
					}
					NCR_CR_PY[crIdx][pyIdx]++;
					NCR_CR[crIdx]++;
					NCR_PY[pyIdx]++;
					NCR[0]++;
				}
			});
			
//			cr.setN_PYEARS   (NPYEARS[0]);
		}
		
		
		
			
		computeCRIndicators(rpyIdx, crSize, pySize, NCR_ALL, NCR_RPY, NCR_CR_PY, NCR_CR, NCR_CR_all, NPYEARS_CR, NCR_PY, NCR, 
			(int crIdx, int N_PYEARS, double PYEAR_PERC, double PERC_YR, double PERC_ALL, int[] N_PCT, int[] N_PCT_AboveAverage, String SEQUENCE, String TYPE) -> { 
				CRType<?> cr = crList.get(crIdx);
				
				if (crIdx == 0) {
					System.out.println(cr.getID());
				}
				
				cr.setN_PYEARS   (N_PYEARS);
				cr.setPYEAR_PERC (PYEAR_PERC);
				cr.setPERC_YR 	 (PERC_YR);
				cr.setPERC_ALL	 (PERC_ALL);
				cr.setN_PCT		(N_PCT);
				cr.setN_PCT_AboveAverage(N_PCT_AboveAverage);
				cr.setSEQUENCE(SEQUENCE);
				cr.setTYPE(TYPE);
			}				
		);
			
	}
	
	

	
	
	private void removeCR (Predicate<CRType_MM> cond) {
		
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
	@Override
	public void retainCR (List<CRType_MM> toRetain) {
		getCR().forEach(cr -> cr.setFlag(true));
		toRetain.forEach(cr -> cr.setFlag(false));
		removeCR(cr -> cr.isFlag());
	}
	
	
	/**
	 * Remove all CRs without year (RPY)
	 */
	@Override
	public void removeCRWithoutYear () {
		removeCR (cr -> cr.getRPY() == null);
	}

	
	/**
	 * Remove all CRS within a given RPY range
	 * @param range
	 */
	@Override
	public void removeCRByYear (IntRange range) {
		removeCR (cr -> ((cr.getRPY()!=null) && (range.getMin() <= cr.getRPY()) && (cr.getRPY() <= range.getMax())));
	}

	
	/**
	 * Remove all CRs within a given N_CR range
	 * @param range
	 */
	@Override
	public void removeCRByN_CR(IntRange range) {
		removeCR (cr -> (range.getMin() <= cr.getN_CR()) && (cr.getN_CR() <= range.getMax()));
	}
	
	
	/**
	 * Remove all CRs < / <= / = / >= / > PERC_YR
	 * @param comp comparator (as string); TODO: ENUMERATION
	 * @param threshold
	 */
	@Override
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
	@Override
	public void removePubByCR (List<CRType_MM> selCR) {
		selCR.stream().flatMap (cr -> cr.getPub()).forEach(pub -> pub.setFlag(true));
		removePub (pub -> !pub.isFlag());
		getPub().forEach(pub -> pub.setFlag(false));
		
//		removePub (pub -> !selCR.stream().flatMap (cr -> cr.getPub()).distinct().collect(Collectors.toList()).contains(pub));
	}
	
	
	
	private void removePub (Predicate<PubType_MM> cond) {
		getPub().filter(cond).collect(Collectors.toList()).forEach(pub -> pub.removeAllCRs(true));
		removeCR(cr -> cr.getN_CR()==0);
	}
	
	/**
	 * Retail all citing publications within given citiny year (PY) range, 
	 * i.e., remove all citing publications OUTSIDE the given citing year (PY) range
	 * @param range
	 */
	@Override
	public void retainPubByCitingYear (IntRange range) {
		removePub (pub -> (pub.getPY()==null) || (range.getMin() > pub.getPY()) || (pub.getPY() > range.getMax()));
	}
	
	

	
	
	/**
	 * Filter publications by year range
	 * Filtering = set VI property to 1 or 0
	 * @param from
	 * @param to
	 */
	@Override
	public void filterByYear (IntRange range) {
		if (!duringUpdate) {
			getCR().forEach ( it -> { it.setVI(((it.getRPY()!=null) && (range.getMin()<=it.getRPY()) && (range.getMax()>=it.getRPY())) || ((it.getRPY()==null) && (this.showNull))); });
		}
	}
	

	@Override
	public void filterByCluster (List<CRType_MM> sel) {
		if (!duringUpdate) {
			getCR().forEach(cr -> cr.setVI(false));
			sel.stream().map(cr -> cr.getCluster()).flatMap(cluster -> cluster.getCR()).forEach ( cr -> cr.setVI(true) );
		}
	}
	
	

	
	@Override
	public void setShowNull (boolean showNull) {
		this.showNull = showNull;
		getCR().forEach ( cr -> { if (cr.getRPY() == null) cr.setVI(showNull);  });
	}
	
	@Override
	public void showAll() {
		this.showNull = true;
		getCR().forEach ( cr -> cr.setVI(true) );
	}






	
	

}

