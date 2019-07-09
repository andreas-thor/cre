package main.cre.data.type.abs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import main.cre.data.type.abs.CRType.PERCENTAGE;
import main.cre.data.type.abs.Statistics.IntRange;
import main.cre.format.cre.Reader;
import main.cre.store.db.CRTable_DB;
import main.cre.store.mm.CRTable_MM;
import main.cre.store.mm.PubType_MM;

public abstract class CRTable <C extends CRType<P>, P extends PubType<C>>{
 
	public static enum COMPARATOR { LT, LTE, EQ, GTE, GT };
	
	
	public enum CRTypes { SB("Sleeping beauty"), CP("Constant performer"), HP ("Hot paper"), LC("Life cycle");
	    public final String label;
	    private CRTypes(String label) {
	        this.label = label;
	    }
	}
	
	public enum ZValueSymbol { PLUS('+'), ZERO('0'), MINUS ('-');
	    public final char label;
	    private ZValueSymbol(char label) {
	        this.label = label;
	    }
	}
	
	
	public static enum TABLE_IMPL_TYPES { MM, DB }
	public static TABLE_IMPL_TYPES type = TABLE_IMPL_TYPES.MM;
	public static String name = "test";

	private boolean aborted;
	private int npctRange = 1;
	
	private CRChartData chartData;

	
	
	public static CRTable<? extends CRType<?>, ? extends PubType<?>> get() {
		
		switch (type) {
		case MM: return CRTable_MM.get();
		case DB: return CRTable_DB.get();
		default: return null;
		}
	}
	
	
	public abstract Reader getReader();
	
	public abstract Statistics getStatistics();
	
	public abstract Clustering<C,P> getClustering();
	
	public CRChartData getChartData() {
		if (chartData == null) {
			chartData = new CRChartData();
		}
		return chartData;
	}
	
	/**
	 * Initialize empty CRTable
	 */
	
	public abstract void init();
	
	public Stream<C> getCR() {
		return getCR(false);
	}

	public abstract Stream<C> getCR(boolean sortById);

	/**
	 * 
	 * @param includePubsWithoutCRs default=false
	 * @return
	 */
	public abstract Stream<P> getPub (boolean includePubsWithoutCRs, boolean sortById);
	
	public Stream<P> getPub() {
		return this.getPub(false, false);
	}	

	public Stream<P> getPub (boolean includePubsWithoutCRs) {
		return this.getPub(includePubsWithoutCRs, false);
	};


	
	
	
	
	/*
	 * We additionally store all pubs in allPubs
	 * This is later only used for export (to Scopus, WoS, CSV_Pub) when the user setting "include pubs without CRs" is set
	 */
	
//	public abstract PubType addPub (PubType pub, boolean addCRs);
	
	public abstract P addPub (PubType_MM pub);
	

	
	/**
	 * Merge CRs based on clustering
	 */

	public abstract void merge ();
	
	
	
	/**
	 * Update computation of percentiles for all CRs
	 * Called after loading, deleting or merging of CRs
	 * @param removed Data has been removed --> adjust clustering data structures; adjust CR lists per publication
	 */
	
	public abstract void updateData () throws OutOfMemoryError; 

	


	

	
	
	
	/**
	 * Remove list of CRs
	 * @param toDelete list of CRs to be deleted
	 */
	public abstract void removeCR (List<C> toDelete);
	

	/**
	 * Remove all but the given list of CRs
	 * @param toRetain list of CRs to be retained
	 */
	public abstract void retainCR (List<C> toRetain);
	
	
	/**
	 * Remove all CRs without year (RPY)
	 */
	public abstract void removeCRWithoutYear ();

	
	/**
	 * Remove all CRS within a given RPY range
	 * @param range
	 */
	public abstract void removeCRByYear (IntRange range);

	
	/**
	 * Remove all CRs within a given N_CR range
	 * @param range
	 */
	public abstract void removeCRByN_CR(IntRange range);
	
	
	/**
	 * Remove all CRs < / <= / = / >= / > PERC_YR
	 * @param comp comparator (as string); TODO: ENUMERATION
	 * @param threshold
	 */
	
	public abstract void removeCRByPERC_YR (String comp, double threshold);
	
	
	/**
	 * Remove all citing publications, that do *not* reference any of the given CRs 
	 * @param selCR list of CRs
	 */
	public abstract void removePubByCR (List<C> selCR);
	
	
	
	
	/**
	 * Retail all citing publications within given citiny year (PY) range, 
	 * i.e., remove all citing publications OUTSIDE the given citing year (PY) range
	 * @param range
	 */
	public abstract void retainPubByCitingYear (IntRange range);
	

	
	
	/**
	 * Filter publications by year range
	 * Filtering = set VI property to 1 or 0
	 * @param from
	 * @param to
	 */
	public abstract void filterByYear (IntRange range);
	

	
	public abstract void filterByCluster (List<C> sel);
	
	

	
	
	public abstract void setShowNull (boolean showNull);
	
	public abstract void showAll();
	
	

	
	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}


	public int getNpctRange() {
		return npctRange;
	}


	public void setNpctRange(int npctRange) {
		if (npctRange>=0) {
			this.npctRange = npctRange;
		}
	}
	
	
	protected void computeCRIndicators (int rpyIdx, int crSize, int pySize, int NCR_ALL, int[] NCR_RPY, int[][] NCR_CR_PY, int[] NCR_CR, int[] NCR_CR_all, int[] NPYEARS_CR, int[] NCR_PY, int[] NCR, CRIndicatorsUpdate updateCR) {

		int noPYWithoutCR = 0;	// number of PY where we do not have any Publication citing a CR in RPY
		for (int pyIdx=0; pyIdx<pySize; pyIdx++) {
			if (NCR_PY[pyIdx]==0) noPYWithoutCR++;
		}
		
//		if (noPYWithoutCR>0) {
//			System.out.println(String.format("RPY=%d, PYSize=%d, w/o=%d", rpy, pySize, noPYWithoutCR));
//		}
 
		int[][] borders = this.getPercentileBorders(NCR_CR_PY, crSize, pySize, NCR_PY);

		
		for (int crIdx=0; crIdx<crSize; crIdx++) {
			
	//		System.out.println("CR x=" + x);
	//		final int crIdx = x;
			
			
			int[] NPCT = new int[PERCENTAGE.values().length];
			int[] NPCT_AboveAverage = new int[PERCENTAGE.values().length];
			
			int[] type = new int[11];
			char[] sequence = new char[pySize];
			
			/* just for debugging */
	//		double[] expectedArray = new double[pySize];
	//		double[] zvalueArray  = new double[pySize];
			
			
			for (int pyIdx=0; pyIdx<pySize; pyIdx++) {
				
				double expected = (1.0d*NCR_CR[crIdx]*NCR_PY[pyIdx]/NCR[0]);
				double zvalue = (expected == 0) ? 0 : (NCR_CR_PY[crIdx][pyIdx] - expected) / Math.sqrt(expected);
	
				
				/* just for debugging */
	//			expectedArray[pyIdx] = expected;
	//			zvalueArray[pyIdx] = zvalue;
	//			System.out.println(String.format("CR=%d\tPY=%d\tExpected=%10.2f\tzValue=%10.2f", crIdx, pyIdx, expected, zvalue));
				
				sequence[pyIdx] = (zvalue>1) ? ZValueSymbol.PLUS.label : ((zvalue<-1) ? ZValueSymbol.MINUS.label : ZValueSymbol.ZERO.label);
				
				type[0]  +=                      (zvalue<-1)?0:1;	// # at least average
				type[1]  += ((pyIdx< 3) 		&& (zvalue<-1)) ? 1 : 0;	// # below average in the first 3 py
				type[2]  += ((pyIdx>=3) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the 4th+ py 
				type[3]  += ((pyIdx< 3) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the first 3 py 
				type[4]  += ((pyIdx< 4) 		&& (zvalue<=1)) ? 1 : 0;	// # average or lower in the first 4 py
				type[5]  += ((pyIdx>=4) 		&& (zvalue> 1)) ? 1 : 0;	// # above average in the 5th+ py 
				type[6]  += ((pySize-pyIdx<=3) 	&& (zvalue<=1)) ? 1: 0;		// # average or lower in the last 3 py
				type[7]  += (NCR_CR_PY[crIdx][pyIdx]>0) ? 1 : 0;			// # no of citing years with at least 1 citation
				type[8]  += ((pyIdx==0) || (sequence[pyIdx-1]==ZValueSymbol.MINUS.label) ||  (sequence[pyIdx]==ZValueSymbol.PLUS.label) || ((sequence[pyIdx-1]==ZValueSymbol.ZERO.label) && (sequence[pyIdx]==ZValueSymbol.ZERO.label))) ? 1:0;
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
			
			StringBuffer typeLabel = new StringBuffer();
			if (sbeauty) 	typeLabel.append (typeLabel.length()>0?" + ":"").append(CRTypes.SB.label);
			if (constant) 	typeLabel.append (typeLabel.length()>0?" + ":"").append(CRTypes.CP.label);
			if (hotpaper) 	typeLabel.append (typeLabel.length()>0?" + ":"").append(CRTypes.HP.label);
			if (lifecycle) 	typeLabel.append (typeLabel.length()>0?" + ":"").append(CRTypes.LC.label);
			
			
			
			updateCR.update(crIdx, NPYEARS_CR[crIdx], ((double)NPYEARS_CR[crIdx]) / (pySize-noPYWithoutCR), ((double)NCR_CR_all[crIdx]) / NCR_RPY[rpyIdx], ((double)NCR_CR_all[crIdx]) / NCR_ALL, 
					NPCT, NPCT_AboveAverage, new String (sequence), typeLabel.toString());
		}	
	}
	
	
	
	private int[][] getPercentileBorders (int[][] NCR_CR_PY, int crSize, int pySize, int[] NCR_PY) {

		int rangeSize_NPCT = getNpctRange();
		
		
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
	
		


}

