package main.cre.data.type.abs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import main.cre.data.CRChartData;
import main.cre.data.CRChartData.SERIESTYPE;
import main.cre.data.type.db.CRTable_DB;
import main.cre.data.type.mm.CRTable_MM;
import main.cre.data.type.mm.PubType_MM;

public abstract class CRTable <C extends CRType<P>, P extends PubType<C>>{
 
	public static enum COMPARATOR { LT, LTE, EQ, GTE, GT };
	
	public static enum TABLE_IMPL_TYPES { MM, DB }
	public static TABLE_IMPL_TYPES type = TABLE_IMPL_TYPES.MM;

	private boolean aborted;
	private int npctRange;
	
	private CRChartData chartData;
	protected int[] range_RPY;
	protected int[] range_PY;
	protected int[] NCR_ALL;		// NCR overall (array length=1; array to make it effectively final)
	protected int[] NCR_RPY;		// (sum of) NCR by RPY
	protected int[] CNT_RPY;		// number of CRs by RPY
	
	
	public static CRTable<? extends CRType<?>, ? extends PubType<?>> get() {
		
		switch (type) {
		case MM: return CRTable_MM.get();
		case DB: return CRTable_DB.get();
		default: return null;
		}
	}
	
	
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
	
	public abstract Stream<C> getCR();

	/**
	 * 
	 * @param includePubsWithoutCRs default=false
	 * @return
	 */
	public abstract Stream<P> getPub (boolean includePubsWithoutCRs);
	
	public Stream<P> getPub() {
		return this.getPub(false);
	}	


	
	
	
	
	/*
	 * We additionally store all pubs in allPubs
	 * This is later only used for export (to Scopus, WoS, CSV_Pub) when the user setting "include pubs without CRs" is set
	 */
	
//	public abstract PubType addPub (PubType pub, boolean addCRs);
	
	public abstract P addPub (PubType_MM pub, boolean addCRs, boolean checkForDuplicates);
	

	
	/**
	 * Merge CRs based on clustering
	 */

	public abstract void merge ();
	
	
	
	/**
	 * Update computation of percentiles for all CRs
	 * Called after loading, deleting or merging of CRs
	 * @param removed Data has been removed --> adjust clustering data structures; adjust CR lists per publication
	 */
	
	public void updateData () throws OutOfMemoryError {
		
		System.out.println("Compute Ranges in CRTable");
		this.range_RPY = getStatistics().getMaxRangeRPY();
		this.range_PY  = getStatistics().getMaxRangePY();
		this.NCR_ALL = new int[1];
		this.NCR_RPY = new int[this.range_RPY[1]-this.range_RPY[0]+1];
		this.CNT_RPY = new int[this.range_RPY[1]-this.range_RPY[0]+1];
		
		// Group CRs by RPY, compute NCR_ALL and NCR_RPY
		System.out.println("mapRPY_CRs");
		CRTable.get().getCR().forEach(cr -> {
			this.NCR_ALL[0] += cr.getN_CR();
			if (cr.getRPY()!=null) {
				this.NCR_RPY[cr.getRPY()-this.range_RPY[0]] += cr.getN_CR();
				this.CNT_RPY[cr.getRPY()-this.range_RPY[0]] += 1;
			}
		});
		
		updateChartData();
	}

	
	private void updateChartData () {
		
		int medianRange = getChartData().getMedianRange();
		
		// compute difference to median
		int[] RPY_MedianDiff = new int[this.NCR_RPY.length];	// RPY_idx -> SumNCR - (median of sumPerYear[year-range] ... sumPerYear[year+range])   
		for (int rpyIdx=0; rpyIdx<this.NCR_RPY.length; rpyIdx++) {
			int[] temp = new int[2*medianRange+1];
			for (int m=-medianRange; m<=medianRange; m++) {
				temp[m+medianRange] = (rpyIdx+m<0) || (rpyIdx+m>this.NCR_RPY.length-1) ? 0 : this.NCR_RPY[rpyIdx+m];
			}
			Arrays.sort(temp);
			RPY_MedianDiff[rpyIdx] = this.NCR_RPY[rpyIdx] - temp[medianRange];
		}
		
		
		getChartData().init(this.range_RPY[0], this.range_RPY[1]);
		getChartData().addSeries(SERIESTYPE.NCR, this.NCR_RPY);
		getChartData().addSeries(SERIESTYPE.MEDIANDIFF, RPY_MedianDiff);
		getChartData().addSeries(SERIESTYPE.CNT, this.CNT_RPY);
	}
	

	
	
	
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
	public abstract void removeCRByYear (int[] range);

	
	/**
	 * Remove all CRs within a given N_CR range
	 * @param range
	 */
	public abstract void removeCRByN_CR(int[] range);
	
	
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
	public abstract void retainPubByCitingYear (int[] range);
	

	
	
	/**
	 * Filter publications by year range
	 * Filtering = set VI property to 1 or 0
	 * @param from
	 * @param to
	 */
	public abstract void filterByYear (int[] range);
	

	
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
	
	


}

