package main.cre.data;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import main.cre.data.db.CRTable_DB;
import main.cre.data.type.CRType;
import main.cre.data.type.PubType;

public abstract class CRTable {
 
	public static enum COMPARATOR { LT, LTE, EQ, GTE, GT };
	

	private boolean aborted;
	private int npctRange;
	
	public static CRTable get() {
//		return CRTable_MM.get();
		return CRTable_DB.get();
	}
	

	/**
	 * Initialize empty CRTable
	 */
	
	public abstract void init();
	
	public abstract Stream<CRType> getCR();

	/**
	 * 
	 * @param includePubsWithoutCRs default=false
	 * @return
	 */
	public abstract Stream<PubType> getPub (boolean includePubsWithoutCRs);
	
	public abstract Stream<PubType> getPub();
	

	public abstract CRType addCR(CRType cr);
	
	public abstract CRType addCR(CRType cr, boolean checkForDuplicatesAndSetId);
	
	
	/*
	 * We additionally store all pubs in allPubs
	 * This is later only used for export (to Scopus, WoS, CSV_Pub) when the user setting "include pubs without CRs" is set
	 */
	
	public abstract PubType addPub (PubType pub, boolean addCRs);
	
	public abstract PubType addPub (PubType pub, boolean addCRs, boolean checkForDuplicates);
	

	
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

	
	
	

	public abstract void removeCR (Predicate<CRType> cond);

	
	
	
	/**
	 * Remove list of CRs
	 * @param toDelete list of CRs to be deleted
	 */
	public abstract void removeCR (List<CRType> toDelete);
	

	/**
	 * Remove all but the given list of CRs
	 * @param toRetain list of CRs to be retained
	 */
	public abstract void retainCR (List<CRType> toRetain);
	
	
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
	public abstract void removePubByCR (List<CRType> selCR);
	
	
	
	
	/**
	 * Remove all citing publications OUTSIDE the given citing year (PY) range
	 * @param range
	 */
	public abstract void removePubByCitingYear (int[] range);
	

	
	
	/**
	 * Filter publications by year range
	 * Filtering = set VI property to 1 or 0
	 * @param from
	 * @param to
	 */
	public abstract void filterByYear (int[] range);
	

	
	public abstract void filterByCluster (List<CRType> sel);
	
	

	
	
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

