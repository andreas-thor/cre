package main.cre.data.type.mm;

import java.util.Arrays;
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
import main.cre.data.type.abs.CRType.PERCENTAGE;
import main.cre.data.type.abs.PubType;
import main.cre.data.type.abs.Statistics;
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
					cr.setCluster(new CRCluster(cr));
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
	
	public void updateData () throws OutOfMemoryError {

		
		duringUpdate = true;		// mutex to avoid chart updates during computation
		
		System.out.println("update Data");
		System.out.println(System.currentTimeMillis());
		
		super.updateData();
		
		
		getCR()
			.filter(cr -> cr.getRPY()!=null)
			.collect(Collectors.groupingBy(CRType::getRPY, Collectors.mapping(Function.identity(), Collectors.toList())))
			.entrySet().stream().parallel()
			.forEach(rpyGroup -> {	
				computeForAllCRsOfTheSameRPY (rpyGroup.getKey().intValue(), rpyGroup.getValue());
			}
		);		
		
		duringUpdate = false;
		
	}

	
	private void computeForAllCRsOfTheSameRPY (int rpy, List<CRType_MM> crList) {
		
		int firstPY = (rpy<=this.range_PY[0]) ? this.range_PY[0] : rpy;	// usually: rpy<=range_PY[0] 
		int lastPY = this.range_PY[1];
		if (lastPY < firstPY) return;
		
		int pySize = lastPY-firstPY+1;
		int crSize = crList.size();
		
		int[][] NCR_CR_PY = new int[crSize][pySize];	
		int[] NCR_CR = new int[crSize];	
		int[] NCR_PY = new int[pySize];	
		int[] NCR = new int[1];
		
		
		for (int x=0; x<crSize; x++) {

			final int crIdx = x;
			CRType<?> cr = crList.get(crIdx);
			int[] NPYEARS = new int[1];
			cr.getPub().filter(pub -> pub.getPY() != null).forEach(pub -> {
				
				if ((pub.getPY()>=firstPY) && (pub.getPY()<=lastPY)) {	// PY is out of range
				
					int pyIdx = pub.getPY()-firstPY;
					
					if (NCR_CR_PY[crIdx][pyIdx]==0) {	// we found a citation from a new PY
						NPYEARS[0]++;
					}
					NCR_CR_PY[crIdx][pyIdx]++;
					NCR_CR[crIdx]++;
					NCR_PY[pyIdx]++;
					NCR[0]++;
				}
			});
			
			cr.setN_PYEARS   (NPYEARS[0]);
		}
		
		
		
		int noPYWithoutCR = 0;	// number of PY where we do not have any Publication citing a CR in RPY
		for (int pyIdx=0; pyIdx<pySize; pyIdx++) {
			if (NCR_PY[pyIdx]==0) noPYWithoutCR++;
		}
		
//		if (noPYWithoutCR>0) {
//			System.out.println(String.format("RPY=%d, PYSize=%d, w/o=%d", rpy, pySize, noPYWithoutCR));
//		}
		
		
 
		int[][] borders = this.getPercentileBorders(NCR_CR_PY, crSize, pySize, NCR_PY);

		
		for (int crIdx=0; crIdx<crSize; crIdx++) {
			
//			System.out.println("CR x=" + x);
//			final int crIdx = x;
			
			int[] NPCT = new int[PERCENTAGE.values().length];
			int[] NPCT_AboveAverage = new int[PERCENTAGE.values().length];
			
			int[] type = new int[11];
			char[] sequence = new char[pySize];
			
			/* just for debugging */
//			double[] expectedArray = new double[pySize];
//			double[] zvalueArray  = new double[pySize];
			
			
			for (int pyIdx=0; pyIdx<pySize; pyIdx++) {
				
				double expected = (1.0d*NCR_CR[crIdx]*NCR_PY[pyIdx]/NCR[0]);
				double zvalue = (expected == 0) ? 0 : (NCR_CR_PY[crIdx][pyIdx] - expected) / Math.sqrt(expected);

				
				/* just for debugging */
//				expectedArray[pyIdx] = expected;
//				zvalueArray[pyIdx] = zvalue;
//				System.out.println(String.format("CR=%d\tPY=%d\tExpected=%10.2f\tzValue=%10.2f", crIdx, pyIdx, expected, zvalue));
				
				sequence[pyIdx] = (zvalue>1) ? '+' : ((zvalue<-1) ? '-' : '0');
				
				type[0]  +=                      (zvalue<-1)?0:1;	// # at least average
				type[1]  += ((pyIdx< 3) 		&& (zvalue<-1)) ? 1 : 0;	// # below average in the first 3 py
				type[2]  += ((pyIdx>=3) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the 4th+ py 
				type[3]  += ((pyIdx< 3) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the first 3 py 
				type[4]  += ((pyIdx< 4) 		&& (zvalue<=1)) ? 1 : 0;	// # average or lower in the first 4 py
				type[5]  += ((pyIdx>=4) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the 5th+ py 
				type[6]  += ((pySize-pyIdx<=3) 	&& (zvalue<=1)) ? 1: 0;		// # average or lower in the last 3 py
				type[7]  += (NCR_CR_PY[crIdx][pyIdx]>0) ? 1 : 0;			// # no of citing years with at least 1 citation
				type[8]  += ((pyIdx==0) || (sequence[pyIdx-1]=='-') ||  (sequence[pyIdx]=='+') || ((sequence[pyIdx-1]=='0') && (sequence[pyIdx]=='0'))) ? 1:0;
				type[9]  +=                      (zvalue>1)?1:0;			// above average
				type[10] += 1;	// # citing years
				
				
				for (int b=0; b<PERCENTAGE.values().length; b++) {
					if (borders[pyIdx][b]<NCR_CR_PY[crIdx][pyIdx]) {
						NPCT[b]++;
						if (zvalue>1) {
							NPCT_AboveAverage[b]++;
						}
					}
				}

			}
			
			// Sleeping Beauty = Publication which has been cited below average in the first three citing years ("-"; z<-1) at least twice and above average ("+"; z>1) in the following citing years at least once
			boolean sbeauty   = (type[1]>=2) && (type[2]>=1);
			
			// Constant Performer = Publication which has been cited in more than 80% of the citing years at least once. In more than 80% of the citing years it has been cited at least on the average level 
			boolean constant  = ((1.0d*type[0]/type[10])>0.8) && ((1.0d*type[7]/type[10])>0.8);
			
			// Hot Paper = Publication which has been cited above average ("+"; z>1) in the first three years after publication at least twice
			boolean hotpaper  = (type[3]>=2);
			
			// Life cycle = Publication which has been cited in the first four years in at least two years on the average level ("0"; -1<=z<=1) or lower ("-"; z<-1), in at least two years of the following years above average ("+"; z>1), and in the last three years on the average level ("0"; -1<=z<=1) or lower ("-"; z<-1)
			boolean lifecycle = (type[4]>=2) && (type[5]>=2) && (type[6]>1);
			
		
			
			
			
			
			CRType<?> cr = crList.get(crIdx);
			
			cr.setPYEAR_PERC (((double)cr.getN_PYEARS()) / (pySize-noPYWithoutCR));
			cr.setPERC_YR 	 (((double)cr.getN_CR())       / NCR_RPY[rpy-range_RPY[0]]);
			cr.setPERC_ALL	 (((double)cr.getN_CR())       / NCR_ALL[0]);
			
			cr.setN_PCT(PERCENTAGE.P50,  NPCT[PERCENTAGE.P50.ordinal()]);
			cr.setN_PCT(PERCENTAGE.P75,  NPCT[PERCENTAGE.P75.ordinal()]);
			cr.setN_PCT(PERCENTAGE.P90,  NPCT[PERCENTAGE.P90.ordinal()]);
			cr.setN_PCT(PERCENTAGE.P99,  NPCT[PERCENTAGE.P99.ordinal()]);
			cr.setN_PCT(PERCENTAGE.P999, NPCT[PERCENTAGE.P999.ordinal()]);

			cr.setN_PCT_AboveAverage(PERCENTAGE.P50,  NPCT_AboveAverage[PERCENTAGE.P50.ordinal()]);
			cr.setN_PCT_AboveAverage(PERCENTAGE.P75,  NPCT_AboveAverage[PERCENTAGE.P75.ordinal()]);
			cr.setN_PCT_AboveAverage(PERCENTAGE.P90,  NPCT_AboveAverage[PERCENTAGE.P90.ordinal()]);
			cr.setN_PCT_AboveAverage(PERCENTAGE.P99,  NPCT_AboveAverage[PERCENTAGE.P99.ordinal()]);
			cr.setN_PCT_AboveAverage(PERCENTAGE.P999, NPCT_AboveAverage[PERCENTAGE.P999.ordinal()]);
			
			cr.setSEQUENCE(new String (sequence));

			StringBuffer typeLabel = new StringBuffer();
			if (sbeauty) 	typeLabel.append (typeLabel.length()>0?" + ":"").append("Sleeping beauty");
			if (constant) 	typeLabel.append (typeLabel.length()>0?" + ":"").append("Constant performer");
			if (hotpaper) 	typeLabel.append (typeLabel.length()>0?" + ":"").append("Hot paper");
			if (lifecycle) 	typeLabel.append (typeLabel.length()>0?" + ":"").append("Life cycle");
			cr.setTYPE(typeLabel.toString());
			
			/*
			if (lifecycle && sbeauty) 	cr.setTYPE("Sleeping beauty / Life cycle");	// Delayed performer
			if (hotpaper && sbeauty) 	cr.setTYPE("Sleeping beauty / Hot paper");	// Delayed performer
			if (hotpaper && lifecycle) 	cr.setTYPE("Hot Paper / Life Cycle");
			if (evergreen) 				cr.setTYPE("Evergreen performer");
			 */
			/*
			if ((cr.getID()==117407) || (cr.getID()==11988)) {
				System.out.println("\n-----------------\n" + cr.getID());
				System.out.println();
//				System.out.println("expectedArray");
//				System.out.println(Arrays.toString(expectedArray));
				System.out.println("zvalueArray");
				System.out.println(Arrays.toString(zvalueArray));
//				System.out.println("sequence");
//				System.out.println(Arrays.toString(sequence));
				System.out.println("NCR_PY");
				System.out.println(Arrays.toString(NCR_PY));
				System.out.println("NCR_CR[crIdx]");
				System.out.println(NCR_CR[crIdx]);
				System.out.println("NCR_CR_PY[crIdx]");
				System.out.println(Arrays.toString(NCR_CR_PY[crIdx]));
				System.out.println("cr.getN_CR()");
				System.out.println(cr.getN_CR());
				System.out.println("NCR[0]");
				System.out.println(NCR[0]);
				System.out.println("borders");
				System.out.println(Arrays.deepToString(borders));
				System.out.println("firstPY");
				System.out.println(firstPY);
				System.out.println("lastPY");
				System.out.println(lastPY);
				System.out.println("NPCT");
				System.out.println(Arrays.toString(NPCT));
				System.out.println("NPCT_AboveAverage");
				System.out.println(Arrays.toString(NPCT_AboveAverage));
			}
			*/
			
		}
		
		

		
	}
	
	
	
	private int[][] getPercentileBorders (int[][] NCR_CR_PY, int crSize, int pySize, int[] NCR_PY) {

		int rangeSize_NPCT = CRTable.get().getNpctRange();
		
		
		int[][] borders = new int[pySize][];	// borders (50%, 75%, 90%, 99%, 99.9%) for each PY
		for (int pyIdx=0; pyIdx<pySize; pyIdx++) {
			
			int rangeStart = (pyIdx-rangeSize_NPCT>=0) ? pyIdx-rangeSize_NPCT : 0;
			int rangeEnd = (pyIdx+rangeSize_NPCT<pySize) ? pyIdx+rangeSize_NPCT : pySize-1;
			
			int[] temp = new int[(rangeEnd-rangeStart+1)*crSize];
			
			for (int rIdx=0; rIdx<rangeEnd-rangeStart+1; rIdx++) {
				for (int crIdx=0; crIdx<crSize; crIdx++) {
					temp[rIdx*crSize + crIdx] = NCR_CR_PY[crIdx][rIdx+rangeStart];
				}
			}
			
			Arrays.sort(temp);
			
			borders[pyIdx] = new int[PERCENTAGE.values().length];
			for (PERCENTAGE perc: PERCENTAGE.values()) {
				borders[pyIdx][perc.ordinal()] = temp[Math.max(0, (int) Math.floor(perc.threshold * temp.length)-1)];
			}
		}
		
		return borders;
		
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
	 * Retail all citing publications within given citiny year (PY) range, 
	 * i.e., remove all citing publications OUTSIDE the given citing year (PY) range
	 * @param range
	 */
	@Override
	public void retainPubByCitingYear (int[] range) {
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






	
	

}

